package com.cube.nanotimer.util.helper;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.cube.nanotimer.R;
import com.cube.nanotimer.util.FormatterService;

public class GUIUtils {

  public static void expandView(final LinearLayout v) {
    v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    final int targetHeight = v.getMeasuredHeight();

    v.getLayoutParams().height = 0;
    v.setVisibility(View.VISIBLE);
    Animation a = new Animation() {
      @Override
      protected void applyTransformation(float interpolatedTime, Transformation t) {
        v.getLayoutParams().height = interpolatedTime == 1
            ? LayoutParams.WRAP_CONTENT
            : (int) (targetHeight * interpolatedTime);
        v.requestLayout();
      }

      @Override
      public boolean willChangeBounds() {
        return true;
      }
    };

    // 1dp/ms
    a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
    v.startAnimation(a);
  }

  public static void collapseView(final View v) {
    final int initialHeight = v.getMeasuredHeight();

    Animation a = new Animation() {
      @Override
      protected void applyTransformation(float interpolatedTime, Transformation t) {
        if (interpolatedTime == 1) {
          v.setVisibility(View.GONE);
        } else {
          v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
          v.requestLayout();
        }
      }

      @Override
      public boolean willChangeBounds() {
        return true;
      }
    };

    // 1dp/ms
    a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
    v.startAnimation(a);
  }

  public static int getColorCodeBetween(int color1, int color2, float stepProgression) {
    int a1 = (color1 >> 24) & 0xFF;
    int r1 = (color1 >> 16) & 0xFF;
    int g1 = (color1 >> 8) & 0xFF;
    int b1 = (color1) & 0xFF;
    int a2 = (color2 >> 24) & 0xFF;
    int r2 = (color2 >> 16) & 0xFF;
    int g2 = (color2 >> 8) & 0xFF;
    int b2 = (color2) & 0xFF;
    int a = (int) ((a2 - a1) * stepProgression) + a1;
    int r = (int) ((r2 - r1) * stepProgression) + r1;
    int g = (int) ((g2 - g1) * stepProgression) + g1;
    int b = (int) ((b2 - b1) * stepProgression) + b1;

    int res = a << 24;
    res |= r << 16;
    res |= g << 8;
    res |= b;
    return res;
  }

  public static void setSessionTimeCellText(TextView tv, long time, int timeInd, int bestInd, int worstInd) {
    String strTime = FormatterService.INSTANCE.formatSolveTime(time);
    StringBuilder sbTimes = new StringBuilder();
    if (timeInd == bestInd) {
      sbTimes.append("<font color='").append(tv.getContext().getResources().getColor(R.color.green)).append("'>");
      sbTimes.append(strTime).append("</font>");
      tv.setText(Html.fromHtml(sbTimes.toString()));
    } else if (timeInd == worstInd) {
      sbTimes.append("<font color='").append(tv.getContext().getResources().getColor(R.color.red)).append("'>");
      sbTimes.append(strTime).append("</font>");
      tv.setText(Html.fromHtml(sbTimes.toString()));
    } else {
      tv.setText(strTime);
    }
  }

  public static AlertDialog showLoadingIndicator(Context context) {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    ProgressBar pbar = new ProgressBar(context);
    pbar.setIndeterminate(true);
    AlertDialog dialog = builder.setView(pbar).create();
    dialog.show();
    return dialog;
  }

}
