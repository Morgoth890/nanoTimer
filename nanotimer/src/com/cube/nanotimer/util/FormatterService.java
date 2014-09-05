package com.cube.nanotimer.util;

import com.cube.nanotimer.App;
import com.cube.nanotimer.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public enum FormatterService {
  INSTANCE;

  public String formatSolveTime(Long solveTime) {
    return formatSolveTime(solveTime, null);
  }

  public String formatSolveTime(Long solveTime, String defaultValue) {
    if (solveTime == null) {
      return defaultValue == null ? "" : defaultValue;
    }
    if (solveTime == -1) {
      return App.INSTANCE.getContext().getString(R.string.DNF);
    }
    if (solveTime == -2) {
      return App.INSTANCE.getContext().getString(R.string.NA);
    }
    StringBuilder sb = new StringBuilder();
    int minutes = (int) (solveTime / 60000);
    int seconds = (int) (solveTime / 1000) % 60;
    int hundreds = (int) (solveTime / 10) % 100;
    if (minutes > 0) {
      sb.append(minutes).append(":");
      sb.append(String.format("%02d", seconds));
    } else {
      sb.append(seconds);
    }
    sb.append(".").append(String.format("%02d", hundreds));
    return sb.toString();
  }

  public String formatDateTime(Long ms) {
    if (ms == null) {
      return "";
    }
    SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy - HH:mm:ss", Locale.ENGLISH);
    return sdf.format(new Date(ms));
  }

  /**
   * Format the times of different steps to a String
   * @param times a list of times in ms
   * @return the formatted steps times
   */
  public String formatStepsTimes(List<Long> times) {
    StringBuilder sb = new StringBuilder();
    if (times != null) {
      for (int i = 0; i < times.size(); i++) {
        sb.append(formatSolveTime(times.get(i)));
        if (i < times.size() - 1) {
          sb.append(" / ");
        }
      }
    }
    return sb.toString();
  }

}
