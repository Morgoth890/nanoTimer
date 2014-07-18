package com.cube.nanotimer.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.SpannedString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.scrambler.ScramblerFactory;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.util.CubeSession;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.util.Utils;
import com.cube.nanotimer.util.YesNoListener;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveType;
import com.cube.nanotimer.vo.SolveTypeStep;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TimerActivity extends Activity {

  enum TimerState { STOPPED, RUNNING, INSPECTING }

  private TextView tvTimer;
  private TextView tvScramble;
  private TextView tvCubeType;
  private TextView tvRA5;
  private TextView tvRA12;
  private ViewGroup layout;
  private TableLayout sessionTimesLayout;
  private TableLayout averagesLayout;
  private TableLayout timerStepsLayout;

  private CubeType cubeType;
  private SolveType solveType;
  private String[] currentScramble;
  private SolveTime lastSolveTime;
  private CubeSession cubeSession;
  private SolveAverages solveAverages;
  private int currentOrientation;
  private List<Long> stepsTimes;
  private long stepStartTs;

  private final long REFRESH_INTERVAL = 25;
  private final int INSPECTION_LIMIT = 15;
  private Timer timer;
  private Handler timerHandler = new Handler();
  private Object timerSync = new Object();
  private long timerStartTs;
  private volatile TimerState timerState = TimerState.STOPPED;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.timer);
    App.INSTANCE.setContext(this);
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    currentOrientation = getResources().getConfiguration().orientation;

    cubeType = (CubeType) getIntent().getSerializableExtra("cubeType");
    solveType = (SolveType) getIntent().getSerializableExtra("solveType");
    cubeSession = new CubeSession();
    App.INSTANCE.getService().getSolveAverages(solveType, new SolveAverageCallback());

    initViews();

    resetTimer();
    setCubeTypeText();

    if (!solveType.hasSteps()) {
      App.INSTANCE.getService().getSessionTimes(solveType, new DataCallback<List<Long>>() {
        @Override
        public void onData(List<Long> data) {
          cubeSession = new CubeSession(data);
          refreshSessionFields();
        }
      });
    }

    generateScramble();
  }

  private void initViews() {
    tvTimer = (TextView) findViewById(R.id.tvTimer);
    tvScramble = (TextView) findViewById(R.id.tvScramble);
    tvCubeType = (TextView) findViewById(R.id.tvCubeType);
    tvRA5 = (TextView) findViewById(R.id.tvRA5);
    tvRA12 = (TextView) findViewById(R.id.tvRA12);
    sessionTimesLayout = (TableLayout) findViewById(R.id.sessionTimesLayout);
    averagesLayout = (TableLayout) findViewById(R.id.averagesLayout);
    timerStepsLayout = (TableLayout) findViewById(R.id.timerStepsLayout);

    if (solveType.hasSteps()) {
      findViewById(R.id.sessionLayout).setVisibility(View.GONE);
      averagesLayout.setColumnCollapsed(2, true);
      LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 4f);
      findViewById(R.id.tvAvgOf).setLayoutParams(layoutParams);
      findViewById(R.id.tvAvgOfFive).setLayoutParams(layoutParams);
      findViewById(R.id.tvAvgOfTwelve).setLayoutParams(layoutParams);
      findViewById(R.id.tvAvgOfHundred).setLayoutParams(layoutParams);
      timerStepsLayout.setVisibility(View.VISIBLE);
    } else {
      timerStepsLayout.setVisibility(View.GONE);
    }

    layout = (ViewGroup) findViewById(R.id.layoutTimer);
    layout.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View view, MotionEvent motionEvent) {
        if (timerState == TimerState.RUNNING) {
          if (solveType.hasSteps()) {
            nextSolveStep();
            if (stepsTimes.size() == solveType.getSteps().length) {
              stopTimer(true);
            }
          } else {
            stopTimer(true);
          }
          return false;
        } else if (timerState == TimerState.STOPPED && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
          startInspectionTimer();
        } else if (timerState == TimerState.INSPECTING && motionEvent.getAction() == MotionEvent.ACTION_UP) {
          stopInspectionTimer();
          startTimer();
        }
        return true;
      }
    });
  }

  private void setCubeTypeText() {
    StringBuilder sb = new StringBuilder();
    sb.append(cubeType.getName());
    if (!solveType.getName().equals(getString(R.string.def))) {
      sb.append(" (").append(solveType.getName()).append(")");
    }
    tvCubeType.setText(sb.toString());
  }

  @Override
  public void onBackPressed() {
    if (timerState == TimerState.RUNNING) {
      stopTimer(false);
      resetTimer();
    } else if (timerState == TimerState.INSPECTING) {
      stopInspectionTimer();
      resetTimer();
    } else {
      if (timer != null) {
        timer.cancel();
        timer.purge();
      }
      super.onBackPressed();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.timer_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (timerState == TimerState.STOPPED) {
      switch (item.getItemId()) {
        case R.id.itPlusTwo:
          if (lastSolveTime != null && lastSolveTime.getTime() > 0 && !lastSolveTime.isPlusTwo()) {
            lastSolveTime.plusTwo();
            App.INSTANCE.getService().saveTime(lastSolveTime, new SolveAverageCallback());
            tvTimer.setText(FormatterService.INSTANCE.formatSolveTime(lastSolveTime.getTime()));
            cubeSession.setLastAsPlusTwo();
            refreshSessionFields();
          }
          break;
        case R.id.itDNF:
          if (lastSolveTime != null && lastSolveTime.getTime() > 0) {
            lastSolveTime.setTime(-1);
            App.INSTANCE.getService().saveTime(lastSolveTime, new SolveAverageCallback());
            tvTimer.setText(FormatterService.INSTANCE.formatSolveTime(lastSolveTime.getTime()));
            cubeSession.setLastAsDNF();
            refreshSessionFields();
          }
          break;
        case R.id.itDelete:
          if (lastSolveTime != null) {
            App.INSTANCE.getService().removeTime(lastSolveTime, new SolveAverageCallback());
            cubeSession.deleteLast();
            refreshSessionFields();
            resetTimer();
          }
          break;
        case R.id.itNewSession:
          Utils.showYesNoConfirmation(this, getString(R.string.new_session_confirmation), new YesNoListener() {
            @Override
            public void onYes() {
              App.INSTANCE.getService().startNewSession(solveType, System.currentTimeMillis(), null);
              cubeSession.clearSession();
              refreshSessionFields();
            }
          });
          break;
      }
    }
    return true;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (newConfig.orientation != currentOrientation) {
      currentOrientation = newConfig.orientation;
      String timerText = tvTimer.getText().toString();
      SpannedString scrambleText = (SpannedString) tvScramble.getText();
      String cubeTypeText = tvCubeType.getText().toString();

      List<String> stepsHeaders = new ArrayList<String>();
      List<String> stepsTimes = new ArrayList<String>();
      if (solveType.hasSteps()) {
        for (int i = 0; i < timerStepsLayout.getChildCount(); i++) {
          TableRow tr = (TableRow) timerStepsLayout.getChildAt(i);
          stepsHeaders.add(((TextView) tr.getChildAt(0)).getText().toString());
          stepsTimes.add(((TextView) tr.getChildAt(1)).getText().toString());
        }
      }

      setContentView(R.layout.timer);
      initViews();

      if (timerState == TimerState.STOPPED) {
        tvTimer.setText(timerText);
      }
      tvScramble.setText(scrambleText);
      tvCubeType.setText(cubeTypeText);
      refreshSessionFields();
      refreshAvgFields();

      if (solveType.hasSteps()) {
        for (int i = 0; i < timerStepsLayout.getChildCount(); i++) {
          TableRow tr = (TableRow) timerStepsLayout.getChildAt(i);
          ((TextView) tr.getChildAt(0)).setText(stepsHeaders.get(i));
          ((TextView) tr.getChildAt(1)).setText(stepsTimes.get(i));
        }
      }
    }
  }

  private void refreshSessionFields() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        clearSessionTextViews();
        if (cubeSession == null) {
          return;
        }
        List<Long> sessionTimes = cubeSession.getSessionTimes();
        if (!sessionTimes.isEmpty()) {
          int bestInd = (sessionTimes.size() < 5) ? -1 : cubeSession.getBestTimeInd(sessionTimes.size());
          int worstInd = (sessionTimes.size() < 5) ? -1 : cubeSession.getWorstTimeInd(sessionTimes.size());
          for (int i = 0; i < sessionTimes.size(); i++) {
            String strTime = FormatterService.INSTANCE.formatSolveTime(sessionTimes.get(i));
            StringBuilder sbTimes = new StringBuilder();
            TextView tv = getSessionTextView(i);
            if (i == bestInd) {
              sbTimes.append("<font color='").append(getResources().getColor(R.color.green)).append("'>");
              sbTimes.append(strTime).append("</font>");
              tv.setText(Html.fromHtml(sbTimes.toString()));
            } else if (i == worstInd) {
              sbTimes.append("<font color='").append(getResources().getColor(R.color.red)).append("'>");
              sbTimes.append(strTime).append("</font>");
              tv.setText(Html.fromHtml(sbTimes.toString()));
            } else {
              tv.setText(strTime);
            }
          }
        }
        tvRA5.setText(FormatterService.INSTANCE.formatSolveTime(cubeSession.getAverageOfFive()));
        tvRA12.setText(FormatterService.INSTANCE.formatSolveTime(cubeSession.getAverageOfTwelve()));
      }
    });
  }

  private void clearSessionTextViews() {
    for (int i = 0; i < sessionTimesLayout.getChildCount(); i++) {
      TableRow tr = (TableRow) sessionTimesLayout.getChildAt(i);
      for (int j = 0; j < tr.getChildCount(); j++) {
        ((TextView) tr.getChildAt(j)).setText("");
      }
    }
  }

  private TextView getSessionTextView(int i) {
    View v = null;
    if (i >= 0 && i < 4) {
      v = ((TableRow) sessionTimesLayout.getChildAt(0)).getChildAt(i);
    } else if (i >= 4 && i < 8) {
      v = ((TableRow) sessionTimesLayout.getChildAt(1)).getChildAt(i - 4);
    } else if (i >= 8 && i < 12) {
      v = ((TableRow) sessionTimesLayout.getChildAt(2)).getChildAt(i - 8);
    }
    return (TextView) v;
  }

  private void startTimer() {
    timerStartTs = System.currentTimeMillis();
    if (solveType.hasSteps()) {
      stepsTimes = new ArrayList<Long>();
      stepStartTs = timerStartTs;
    }
    timer = new Timer();
    TimerTask timerTask = new TimerTask() {
      public void run() {
        timerHandler.post(new Runnable() {
          public void run() {
            synchronized (timerSync) {
              if (timerState == TimerState.RUNNING) {
                updateTimerText(System.currentTimeMillis() - timerStartTs);
              }
            }
          }
        });
      }
    };
    timer.schedule(timerTask, 1, REFRESH_INTERVAL);
    timerState = TimerState.RUNNING;
  }

  private void stopTimer(boolean save) {
    long time = (System.currentTimeMillis() - timerStartTs);
    timerState = TimerState.STOPPED;
    if (timer != null) {
      timer.cancel();
      timer.purge();
    }
    // update time once more to get the ms right
    // (as all ms do not necessarily appear when timing, some are skipped due to refresh interval)
    updateTimerText(time);
    if (save) {
      saveTime(time);
    }
    generateScramble();
  }

  private void startInspectionTimer() {
    timerStartTs = System.currentTimeMillis();
    enableScreenRotation(false);
    resetTimerText();
    timerState = TimerState.INSPECTING;
    layout.setBackgroundResource(R.color.nightblue);
    tvCubeType.setText(getString(R.string.inspection));
    timer = new Timer();
    TimerTask timerTask = new TimerTask() {
      public void run() {
        timerHandler.post(new Runnable() {
          public void run() {
            updateInspectionTimerText();
          }
        });
      }
    };
    timer.schedule(timerTask, 1, 1000);
  }

  private void stopInspectionTimer() {
    if (timer != null) {
      timer.cancel();
      timer.purge();
    }
    layout.setBackgroundResource(R.color.black);
    setCubeTypeText();
    timerState = TimerState.STOPPED;
    enableScreenRotation(true);
  }

  private void nextSolveStep() {
    long ts = System.currentTimeMillis();
    if (stepsTimes.size() < solveType.getSteps().length) {
      long time = ts - stepStartTs;
      stepsTimes.add(time);
      updateStepTimeText(stepsTimes.size() - 1, FormatterService.INSTANCE.formatSolveTime(time));
    }
    stepStartTs = ts;
  }

  private void resetTimer() {
    synchronized (timerSync) {
      if (timer != null) {
        timer.cancel();
        timer.purge();
      }
      timerStartTs = 0;
      resetTimerText();
    }
  }

  private void resetTimerText() {
    String defaultText = "0.00";
    tvTimer.setText(defaultText);
    if (solveType.hasSteps()) {
      for (int i = 0; i < solveType.getSteps().length; i++) { // TODO handle this to make sure that solve types length is not bigger than the number of rows
        SolveTypeStep sts = solveType.getSteps()[i];
        ((TextView) ((TableRow) timerStepsLayout.getChildAt(i)).getChildAt(0)).setText(sts.getName() + ": ");
        updateStepTimeText(i, defaultText);
      }
    }
  }

  private void saveTime(long time) {
    SolveTime solveTime = new SolveTime();
    solveTime.setTime(time);
    solveTime.setTimestamp(System.currentTimeMillis());
    solveTime.setSolveType(solveType);
    solveTime.setScramble(FormatterService.INSTANCE.formatScrambleAsSingleLine(currentScramble, cubeType));
    if (solveType.hasSteps()) {
      solveTime.setStepsTimes(stepsTimes.toArray(new Long[0]));
    }

    App.INSTANCE.getService().saveTime(solveTime, new SolveAverageCallback());
    if (cubeSession != null) {
      cubeSession.addTime(time);
      refreshSessionFields();
    }
  }

  private void updateStepTimeText(int id, String time) {
    if (id >= 0 && id < timerStepsLayout.getChildCount()) {
      ((TextView) ((TableRow) timerStepsLayout.getChildAt(id)).getChildAt(1)).setText(time);
    }
  }

  private String formatAvgField(Long f) {
    return FormatterService.INSTANCE.formatSolveTime(f, "-");
  }

  private String formatStepsAverages(List<Long> times) {
    StringBuilder sb = new StringBuilder();
    if (times != null) {
      for (int i = 0; i < times.size(); i++) {
        sb.append(FormatterService.INSTANCE.formatSolveTime(times.get(i)));
        if (i < times.size() - 1) {
          sb.append(" / ");
        }
      }
    }
    return sb.toString();
  }

  private synchronized void updateTimerText(long curTime) {
    tvTimer.setText(FormatterService.INSTANCE.formatSolveTime(curTime));
    if (solveType.hasSteps()) {
      updateStepTimeText(stepsTimes.size(), FormatterService.INSTANCE.
          formatSolveTime(System.currentTimeMillis() - stepStartTs));
    }
  }

  private synchronized void updateInspectionTimerText() {
    long curTime = System.currentTimeMillis() - timerStartTs;
    int seconds = (int) (curTime / 1000);
    tvTimer.setText(String.valueOf(seconds));
    if (seconds >= INSPECTION_LIMIT - 3 && seconds <= INSPECTION_LIMIT) {
      Utils.playSound(R.raw.beep);
      if (seconds == INSPECTION_LIMIT) {
        layout.setBackgroundResource(R.color.darkred);
      }
    }
  }

  private void generateScramble() {
    if (cubeType != null) {
      currentScramble = ScramblerFactory.getScrambler(cubeType).getNewScramble();
      tvScramble.setText(FormatterService.INSTANCE.formatToColoredScramble(currentScramble, cubeType));
    }
  }

  private void enableScreenRotation(boolean enable) {
    if (enable) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    } else {
      setRequestedOrientation(currentOrientation);
    }
  }

  private void refreshAvgFields() {
    if (!solveType.hasSteps()) {
      ((TextView) findViewById(R.id.tvAvgOfFive)).setText(formatAvgField(solveAverages.getAvgOf5()));
      ((TextView) findViewById(R.id.tvAvgOfTwelve)).setText(formatAvgField(solveAverages.getAvgOf12()));
      ((TextView) findViewById(R.id.tvAvgOfHundred)).setText(formatAvgField(solveAverages.getAvgOf100()));
      ((TextView) findViewById(R.id.tvBestOfFive)).setText(formatAvgField(solveAverages.getBestOf5()));
      ((TextView) findViewById(R.id.tvBestOfTwelve)).setText(formatAvgField(solveAverages.getBestOf12()));
      ((TextView) findViewById(R.id.tvBestOfHundred)).setText(formatAvgField(solveAverages.getBestOf100()));
      ((TextView) findViewById(R.id.tvLifetimeAvg)).setText(FormatterService.INSTANCE.formatSolveTime(
          solveAverages.getAvgOfLifetime(), getString(R.string.NA)));
      ((TextView) findViewById(R.id.tvLifetimeBest)).setText(FormatterService.INSTANCE.formatSolveTime(
          solveAverages.getBestOfLifetime(), getString(R.string.NA)));
    } else {
      ((TextView) findViewById(R.id.tvAvgOfFive)).setText(formatStepsAverages(solveAverages.getStepsAvgOf5()));
      ((TextView) findViewById(R.id.tvAvgOfTwelve)).setText(formatStepsAverages(solveAverages.getStepsAvgOf12()));
      ((TextView) findViewById(R.id.tvAvgOfHundred)).setText(formatStepsAverages(solveAverages.getStepsAvgOf100()));
    }
  }

  private class SolveAverageCallback extends DataCallback<SolveAverages> {
    @Override
    public void onData(final SolveAverages data) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          solveAverages = data;
          lastSolveTime = data.getSolveTime();
          refreshAvgFields();
        }
      });
    }
  }

}
