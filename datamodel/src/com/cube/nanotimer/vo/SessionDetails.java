package com.cube.nanotimer.vo;

import java.util.List;

public class SessionDetails {

  private int totalSolvesCount;
  private List<Long> sessionTimes;

  public SessionDetails() {
  }

  public int getTotalSolvesCount() {
    return totalSolvesCount;
  }

  public void setTotalSolvesCount(int totalSolvesCount) {
    this.totalSolvesCount = totalSolvesCount;
  }

  public List<Long> getSessionTimes() {
    return sessionTimes;
  }

  public void setSessionTimes(List<Long> sessionTimes) {
    this.sessionTimes = sessionTimes;
  }

  public int getSessionSolvesCount() {
    return (sessionTimes == null) ? 0 : sessionTimes.size();
  }

}
