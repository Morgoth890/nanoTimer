package com.cube.nanotimer.gui.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.cube.nanotimer.App;
import com.cube.nanotimer.R;
import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.session.TimesStatistics;
import com.cube.nanotimer.util.FormatterService;
import com.cube.nanotimer.util.helper.GUIUtils;
import com.cube.nanotimer.util.helper.ScreenUtils;
import com.cube.nanotimer.vo.SessionDetails;
import com.cube.nanotimer.vo.SolveType;

import java.util.List;

public class SessionDetailDialog extends DialogFragment {

  private static final int PAGE_LINES_COUNT = 10;
  private static final int TIMES_PER_LINE = 4;
  private static final int SESSION_TIMES_HEIGHT_DP = 26;
  private static final int BEST_AVERAGES_HEIGHT_DP = 22;
  private static final int MIN_TIMES_FOR_AVERAGE = 5;
  private static final String ARG_SOLVETYPE = "solvetype";

  private LayoutInflater inflater;
  private TableLayout sessionTimesLayout;
  private Button buMore;
  private List<Long> sessionTimes;
  private SolveType solveType;
  private int bestInd;
  private int worstInd;
  private int curPageInd = 0;

  public static SessionDetailDialog newInstance(SolveType solveType) {
    SessionDetailDialog sessionDetailDialog = new SessionDetailDialog();
    Bundle bundle = new Bundle();
    bundle.putSerializable(ARG_SOLVETYPE, solveType);
    sessionDetailDialog.setArguments(bundle);
    return sessionDetailDialog;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final View v = getActivity().getLayoutInflater().inflate(R.layout.sessiondetail_dialog, null);
    solveType = (SolveType) getArguments().getSerializable(ARG_SOLVETYPE);
    App.INSTANCE.getService().getSessionDetails(solveType, new DataCallback<SessionDetails>() {
      @Override
      public void onData(final SessionDetails data) {
        getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            displaySessionDetails(v, data);
          }
        });
      }
    });

    final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(v).create();
    dialog.setCanceledOnTouchOutside(true);
    return dialog;
  }

  private void displaySessionDetails(View v, SessionDetails sessionDetails) {
    sessionTimes = sessionDetails.getSessionTimes();
    TimesStatistics session = new TimesStatistics(sessionTimes);
    bestInd = (sessionTimes.size() < MIN_TIMES_FOR_AVERAGE) ? -1 : session.getBestTimeInd(sessionTimes.size());
    worstInd = (sessionTimes.size() < MIN_TIMES_FOR_AVERAGE) ? -1 : session.getWorstTimeInd(sessionTimes.size());
    buMore = (Button) v.findViewById(R.id.buMore);
    inflater = getActivity().getLayoutInflater();

    if (solveType.isBlind()) {
      v.findViewById(R.id.bestAveragesLayout).setVisibility(View.GONE);
      v.findViewById(R.id.trBestMeanOfThree).setVisibility(View.VISIBLE);
      v.findViewById(R.id.trAccuracy).setVisibility(View.VISIBLE);
      ((TextView) v.findViewById(R.id.tvBestMeanOfThree)).setText(FormatterService.INSTANCE.formatSolveTime(getBestMeanOf(sessionTimes, 3)));
      ((TextView) v.findViewById(R.id.tvAccuracy)).setText(FormatterService.INSTANCE.formatPercentage(session.getAccuracy(sessionTimes.size())));
      ((TextView) v.findViewById(R.id.tvLabelAverage)).setText(R.string.session_success_average);
      long successAverage = session.getSuccessAverageOf(sessionTimes.size(), MIN_TIMES_FOR_AVERAGE, true);
      ((TextView) v.findViewById(R.id.tvAverage)).setText(FormatterService.INSTANCE.formatSolveTime(successAverage));
      if (successAverage > 0) {
        // Process worst ind for blind: the worst should not be a DNF as the average is calculated only based on successes
        long worst = -1;
        for (int i = 0; i < sessionTimes.size(); i++) {
          if (sessionTimes.get(i) > worst) {
            worst = sessionTimes.get(i);
            worstInd = i;
          }
        }
      } else {
        bestInd = -1;
        worstInd = -1;
      }
    } else {
      setupBestAverages(v, sessionTimes);
      ((TextView) v.findViewById(R.id.tvAverage)).setText(FormatterService.INSTANCE.formatSolveTime(session.getAverageOf(Math.max(MIN_TIMES_FOR_AVERAGE, sessionTimes.size()))));
    }

    ((TextView) v.findViewById(R.id.tvSolves)).setText(String.valueOf(sessionDetails.getSessionSolvesCount()));
    sessionTimesLayout = (TableLayout) v.findViewById(R.id.sessionTimesLayout);

    int sessionTimesCount = sessionTimes.size();
    if (sessionTimesCount == 0) {
      TableRow tr = new TableRow(getActivity());
      for (int i = 0; i < TIMES_PER_LINE; i++) {
        TextView tv = getNewSolveTimeTextView();
        tr.addView(tv);
      }
      sessionTimesLayout.addView(tr);
    } else if (sessionTimesCount < TIMES_PER_LINE) {
      TableRow tr = new TableRow(getActivity());
      for (Long time : sessionTimes) {
        TextView tv = getNewSolveTimeTextView();
        tv.setText(FormatterService.INSTANCE.formatSolveTime(time));
        tr.addView(tv);
      }
      sessionTimesLayout.addView(tr);
    } else {
      addSolveTimesPage();
    }

    adjustTableLayoutParams(sessionTimesLayout, SESSION_TIMES_HEIGHT_DP);

    buMore.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        addSolveTimesPage();
      }
    });
  }

  private long getBestMeanOf(List<Long> times, int n) {
    long best = Long.MAX_VALUE;
    for (int i = 0; i <= times.size() - n; i++) {
      TimesStatistics session = new TimesStatistics(times.subList(i, Math.min(i + n, times.size())));
      long mean = session.getMeanOf(n);
      if (mean > 0 && mean < best) {
        best = mean;
      }
    }
    return best == Long.MAX_VALUE ? -2 : best;
  }

  private long getBestAverageOf(List<Long> times, int n) {
    long best = Long.MAX_VALUE;
    for (int i = 0; i <= times.size() - n; i++) {
      TimesStatistics session = new TimesStatistics(times.subList(i, Math.min(i + n, times.size())));
      long avg = session.getAverageOf(n);
      if (avg > 0 && avg < best) {
        best = avg;
      }
    }
    return best == Long.MAX_VALUE ? -2 : best;
  }

  private void setupBestAverages(View v, List<Long> times) {
    long avg5 = getBestAverageOf(times, 5);
    long avg12 = getBestAverageOf(times, 12);
    long avg50 = getBestAverageOf(times, 50);
    long avg100 = getBestAverageOf(times, 100);

    if (avg5 < 0 && avg12 < 0 && avg50 < 0 && avg100 < 0) {
      v.findViewById(R.id.bestAveragesLayout).setVisibility(View.GONE);
      return;
    }
    TableLayout bestAveragesTableLayout = (TableLayout) v.findViewById(R.id.bestAveragesTableLayout);
    TableRow trHeaders = new TableRow(getActivity());
    TableRow trAverages = new TableRow(getActivity());
    if (avg5 > 0) {
      addToBestAveragesTable(trHeaders, trAverages, 5, avg5);
    }
    if (avg12 > 0) {
      addToBestAveragesTable(trHeaders, trAverages, 12, avg12);
    }
    if (avg50 > 0) {
      addToBestAveragesTable(trHeaders, trAverages, 50, avg50);
    }
    if (avg100 > 0) {
      addToBestAveragesTable(trHeaders, trAverages, 100, avg100);
    }
    bestAveragesTableLayout.addView(trHeaders);
    bestAveragesTableLayout.addView(trAverages);
    adjustTableLayoutParams(bestAveragesTableLayout, BEST_AVERAGES_HEIGHT_DP);
  }

  private void addToBestAveragesTable(TableRow trHeaders, TableRow trAverages, int avgHeader, long average) {
    TextView tv = getNewSolveTimeTextView();
    tv.setTextColor(getResources().getColor(R.color.lightblue));
    tv.setText(String.valueOf(avgHeader));
    tv.setTypeface(null, Typeface.BOLD);
    trHeaders.addView(tv);

    tv = getNewSolveTimeTextView();
    tv.setText(FormatterService.INSTANCE.formatSolveTime(average));
    trAverages.addView(tv);
  }

  private void addSolveTimesPage() {
    TableRow tr = new TableRow(getActivity());
    int sessionTimesCount = sessionTimes.size();
    int timesPerPage = PAGE_LINES_COUNT * TIMES_PER_LINE;
    int pageStartInd = curPageInd * timesPerPage;
    int pageEndInd = Math.min(sessionTimesCount, pageStartInd + timesPerPage);
    for (int i = pageStartInd; i < pageEndInd; i++) {
      TextView tv = getNewSolveTimeTextView();
      GUIUtils.setSessionTimeCellText(tv, sessionTimes.get(i), i, bestInd, worstInd);
      tr.addView(tv);
      if (i % TIMES_PER_LINE == 3) {
        sessionTimesLayout.addView(tr);
        tr = new TableRow(getActivity());
      }
    }
    if (pageEndInd < sessionTimesCount) { // some more times remain
      buMore.setVisibility(View.VISIBLE);
    } else { // all times are displayed
      buMore.setVisibility(View.GONE);
      // add remaining cells to have the same cells count than above lines
      if (sessionTimesCount % TIMES_PER_LINE != 0) {
        sessionTimesLayout.addView(tr);
        for (int i = 0; i < TIMES_PER_LINE - (sessionTimesCount % TIMES_PER_LINE); i++) {
          TextView tv = getNewSolveTimeTextView();
          tr.addView(tv);
        }
      }
    }
    adjustTableLayoutParams(sessionTimesLayout, SESSION_TIMES_HEIGHT_DP);
    curPageInd++;
  }

  private void adjustTableLayoutParams(TableLayout tableLayout, int tvHeightDp) {
    int startInd = curPageInd * PAGE_LINES_COUNT;
    for (int i = startInd; i < tableLayout.getChildCount(); i++) {
      if (tableLayout.getChildAt(i) instanceof TableRow) {
        TableRow tr = (TableRow) tableLayout.getChildAt(i);
        for (int j = 0; j < tr.getChildCount(); j++) {
          TextView tv = (TextView) tr.getChildAt(j);
          LayoutParams params = (LayoutParams) tv.getLayoutParams();
          params.setMargins(2, 2, 2, 2);
          params.height = ScreenUtils.dipToPixels(tvHeightDp);
          tv.setLayoutParams(params);
        }
      }
    }
  }

  private TextView getNewSolveTimeTextView() {
    TextView tv = (TextView) inflater.inflate(R.layout.timecell_textview, null);
    tv.setTextSize(15);
    return tv;
  }

}
