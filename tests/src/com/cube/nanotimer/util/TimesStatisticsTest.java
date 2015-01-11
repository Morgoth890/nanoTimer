package com.cube.nanotimer.util;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import com.cube.nanotimer.session.TimesStatistics;
import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

public class TimesStatisticsTest extends AndroidTestCase {

  @SmallTest
  public void testEmptyTimes() {
    List<Long> times = new ArrayList<Long>();
    TimesStatistics timesStatistics = new TimesStatistics(times);
    Assert.assertEquals(-2, timesStatistics.getAverageOf(5));
    Assert.assertEquals(-2, timesStatistics.getMeanOf(5));
    Assert.assertEquals(-2, timesStatistics.getSuccessAverageOf(5, false));
    Assert.assertEquals(-2, timesStatistics.getSuccessMeanOf(5, false));
    Assert.assertEquals(-2, timesStatistics.getAccuracy(5, false));
  }

  @SmallTest
  public void testTimesSizeSmaller() {
    List<Long> times = new ArrayList<Long>();
    times.add(1000l);
    times.add(2000l);
    times.add(3000l);
    TimesStatistics timesStatistics = new TimesStatistics(times);
    Assert.assertEquals(-2, timesStatistics.getAverageOf(5));
    Assert.assertEquals(-2, timesStatistics.getMeanOf(5));
    Assert.assertEquals(-2, timesStatistics.getSuccessAverageOf(5, false));
    Assert.assertEquals(-2, timesStatistics.getSuccessMeanOf(5, false));
    Assert.assertEquals(-2, timesStatistics.getAccuracy(5, false));
  }

  @SmallTest
  public void testCalculateAverageWithNotEnoughTimes() {
    List<Long> times = new ArrayList<Long>();
    times.add(1000l);
    times.add(2000l);
    times.add(3000l);
    TimesStatistics timesStatistics = new TimesStatistics(times);
    Assert.assertEquals(-2, timesStatistics.getAverageOf(3));
    Assert.assertEquals(-2, timesStatistics.getSuccessAverageOf(3, true));
    Assert.assertEquals(-2, timesStatistics.getSuccessAverageOf(3, false));
  }

  @SmallTest
  public void testCalculateAllWithSmaller() {
    List<Long> times = new ArrayList<Long>();
    times.add(1000l);
    times.add(2000l);
    times.add(3000l);
    times.add(4000l);
    times.add(5000l);
    TimesStatistics timesStatistics = new TimesStatistics(times);
    Assert.assertEquals(3000, timesStatistics.getSuccessAverageOf(10, true));
    Assert.assertEquals(3000, timesStatistics.getSuccessMeanOf(10, true));
    Assert.assertEquals(100, timesStatistics.getAccuracy(10, true));
  }

  @SmallTest
  public void testAverage() {
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getAverageOf(5));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 5000)).getAverageOf(5));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 12000)).getAverageOf(5));
    Assert.assertEquals(4000, new TimesStatistics(getTimesList(200, 2000, 6000, 4000, 12000)).getAverageOf(5));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(3000, 1000, 4000, 2000, 5000)).getAverageOf(5));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(6000, 3000, 1000, 4000, 2000, 5000)).getAverageOf(5));
    Assert.assertEquals(4333, new TimesStatistics(getTimesList(-1, 6000, 3000, 1000, 4000, 2000, 5000)).getAverageOf(5));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(-1, -1, 6000, 3000, 1000, 4000, 2000, 5000)).getAverageOf(5));
  }

  @SmallTest
  public void testMean() {
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getMeanOf(5));
    Assert.assertEquals(2840, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 5000)).getMeanOf(5));
    Assert.assertEquals(4240, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 12000)).getMeanOf(5));
    Assert.assertEquals(4840, new TimesStatistics(getTimesList(200, 2000, 6000, 4000, 12000)).getMeanOf(5));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(3000, 1000, 4000, 2000, 5000)).getMeanOf(5));
    Assert.assertEquals(3200, new TimesStatistics(getTimesList(6000, 3000, 1000, 4000, 2000, 5000)).getMeanOf(5));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(-1, 6000, 3000, 1000, 4000, 2000, 5000)).getMeanOf(5));
  }

  @SmallTest
  public void testSuccessAverage() {
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getSuccessAverageOf(5, false));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 5000)).getSuccessAverageOf(5, false));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 12000)).getSuccessAverageOf(5, false));
    Assert.assertEquals(4000, new TimesStatistics(getTimesList(200, 2000, 6000, 4000, 12000)).getSuccessAverageOf(5, false));

    Assert.assertEquals(-2, new TimesStatistics(getTimesList(1000, -1, 3000, 4000, 5000)).getSuccessAverageOf(5, false));
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(-1, 6000, 3000, 1000, 4000, 2000, 5000)).getSuccessAverageOf(5, false));
    Assert.assertEquals(4333, new TimesStatistics(getTimesList(8000, -1, 6000, 3000, 1000, 4000, 2000, 5000)).getSuccessAverageOf(5, false));

    List<Long> times = getTimesList(6000, 8000, -1, 3000, 1000, 4000, 5000);
    Assert.assertEquals(4333, new TimesStatistics(times).getSuccessAverageOf(5, false));
    Assert.assertEquals(4500, new TimesStatistics(times).getSuccessAverageOf(12, true));
    Assert.assertEquals(-2, new TimesStatistics(times).getSuccessAverageOf(12, false));
//    Assert.assertEquals(-2, new TimesStatistics(times).getSuccessAverageOf(5, 7, true));
//    Assert.assertEquals(-2, new TimesStatistics(times).getSuccessAverageOf(5, 7, false));
//    Assert.assertEquals(-2, new TimesStatistics(times).getSuccessAverageOf(12, 7, true));
//    Assert.assertEquals(-2, new TimesStatistics(times).getSuccessAverageOf(12, 6, false));
//    Assert.assertEquals(4500, new TimesStatistics(times).getSuccessAverageOf(12, 6, true));
//    Assert.assertEquals(6000, new TimesStatistics(times).getSuccessAverageOf(3, 3, true));
  }

  @SmallTest
  public void testSuccessMean() {
    Assert.assertEquals(3000, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getSuccessMeanOf(5, false));
    Assert.assertEquals(2840, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 5000)).getSuccessMeanOf(5, false));
    Assert.assertEquals(4240, new TimesStatistics(getTimesList(200, 2000, 3000, 4000, 12000)).getSuccessMeanOf(5, false));
    Assert.assertEquals(4840, new TimesStatistics(getTimesList(200, 2000, 6000, 4000, 12000)).getSuccessMeanOf(5, false));

    Assert.assertEquals(3250, new TimesStatistics(getTimesList(1000, -1, 3000, 4000, 5000)).getSuccessMeanOf(5, true));
    Assert.assertEquals(-2, new TimesStatistics(getTimesList(1000, -1, 3000, 4000, 5000)).getSuccessMeanOf(5, false));
    Assert.assertEquals(3200, new TimesStatistics(getTimesList(-1, 6000, 3000, 1000, 4000, 2000, 5000)).getSuccessMeanOf(5, false));
    Assert.assertEquals(4400, new TimesStatistics(getTimesList(8000, -1, 6000, 3000, 1000, 4000, 2000, 5000)).getSuccessMeanOf(5, false));

    List<Long> times = getTimesList(6000, 8000, -1, 3000, 1000, 4000, 5000);
    Assert.assertEquals(4400, new TimesStatistics(times).getSuccessMeanOf(5, false));
    Assert.assertEquals(4400, new TimesStatistics(times).getSuccessMeanOf(5, true));
    Assert.assertEquals(-2, new TimesStatistics(times).getSuccessMeanOf(12, false));
    Assert.assertEquals(4500, new TimesStatistics(times).getSuccessMeanOf(12, true));
    Assert.assertEquals(5666, new TimesStatistics(times).getSuccessMeanOf(3, false));
    Assert.assertEquals(5666, new TimesStatistics(times).getSuccessMeanOf(3, true));
  }

  @SmallTest
  public void testAccuracy() {
    Assert.assertEquals(100, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getAccuracy(5, false));
    Assert.assertEquals(100, new TimesStatistics(getTimesList(1000)).getAccuracy(1, false));
    Assert.assertEquals(50, new TimesStatistics(getTimesList(1000, -1)).getAccuracy(2, false));
    Assert.assertEquals(50, new TimesStatistics(getTimesList(-1, 1000)).getAccuracy(2, false));
    Assert.assertEquals(25, new TimesStatistics(getTimesList(-1, 1000, -1, -1)).getAccuracy(4, false));
    Assert.assertEquals(0, new TimesStatistics(getTimesList(-1)).getAccuracy(1, false));
    Assert.assertEquals(-2, new TimesStatistics(getTimesList()).getAccuracy(0, false));
    Assert.assertEquals(0, new TimesStatistics(getTimesList(-1, 1000)).getAccuracy(1, false));
    Assert.assertEquals(-2, new TimesStatistics(getTimesList(-1, 1000)).getAccuracy(5, false));
    Assert.assertEquals(50, new TimesStatistics(getTimesList(-1, 1000, -1)).getAccuracy(2, false));
    Assert.assertEquals(75, new TimesStatistics(getTimesList(-1, 1000, 1000, 1000)).getAccuracy(5, true));
    Assert.assertEquals(-2, new TimesStatistics(getTimesList(-1, 1000, 1000, 1000)).getAccuracy(5, false));
    Assert.assertEquals(75, new TimesStatistics(getTimesList(-1, 1000, 1000, 1000)).getAccuracy(4, false));
    Assert.assertEquals(50, new TimesStatistics(getTimesList(-1, 1000, -1, 1000)).getAccuracy(10, true));
  }

  @SmallTest
  public void testBestTimeInd() {
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getBestTimeInd(4, false));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getBestTimeInd(4, true));
    Assert.assertEquals(0, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getBestTimeInd(5, false));
    Assert.assertEquals(1, new TimesStatistics(getTimesList(2000, 1000, 3000, 4000, 5000)).getBestTimeInd(5, false));
    Assert.assertEquals(0, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getBestTimeInd(5, true));
    Assert.assertEquals(1, new TimesStatistics(getTimesList(2000, 1000, 3000, 4000, 5000)).getBestTimeInd(5, true));
    Assert.assertEquals(4, new TimesStatistics(getTimesList(2000, 4000, -1, 5000, 1000)).getBestTimeInd(5, false));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, -1, 5000, 1000)).getBestTimeInd(5, true));
    Assert.assertEquals(4, new TimesStatistics(getTimesList(2000, 4000, -1, 5000, 1000, 3000)).getBestTimeInd(5, true));
    Assert.assertEquals(5, new TimesStatistics(getTimesList(2000, 4000, -1, 5000, 1000, 500)).getBestTimeInd(5, true));
    Assert.assertEquals(1, new TimesStatistics(getTimesList(2000, 1000, -1, 4000, 1000, 3000, -1)).getBestTimeInd(5, true));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1)).getBestTimeInd(5, true));
    Assert.assertEquals(5, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 500)).getBestTimeInd(5, true));
    Assert.assertEquals(4, new TimesStatistics(getTimesList(-1, -1, 8, 6, 4, 10, 3, 5)).getBestTimeInd(5, false));
    Assert.assertEquals(6, new TimesStatistics(getTimesList(-1, -1, 8, 6, 4, 10, 3, 5)).getBestTimeInd(5, true));
    Assert.assertEquals(5, new TimesStatistics(getTimesList(115, -1, 100, 111, 103, 82)).getBestTimeInd(5, true));

    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 500)).getBestTimeInd(10, false));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 500)).getBestTimeInd(10, true));
    Assert.assertEquals(5, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 500)).getBestTimeInd(6, false));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 500)).getBestTimeInd(6, true));
  }

  @SmallTest
  public void testWorstTimeInd() {
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getWorstTimeInd(4, false));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getWorstTimeInd(4, true));
    Assert.assertEquals(4, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getWorstTimeInd(5, false));
    Assert.assertEquals(3, new TimesStatistics(getTimesList(2000, 1000, 3000, 5000, 4000)).getWorstTimeInd(5, false));
    Assert.assertEquals(4, new TimesStatistics(getTimesList(1000, 2000, 3000, 4000, 5000)).getWorstTimeInd(5, true));
    Assert.assertEquals(4, new TimesStatistics(getTimesList(2000, 1000, 3000, 4000, 5000)).getWorstTimeInd(5, true));
    Assert.assertEquals(2, new TimesStatistics(getTimesList(2000, 4000, -1, 5000, 1000)).getWorstTimeInd(5, false));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, -1, 5000, 1000)).getWorstTimeInd(5, true));
    Assert.assertEquals(3, new TimesStatistics(getTimesList(2000, 4000, -1, 5000, 1000, 3000)).getWorstTimeInd(5, true));
    Assert.assertEquals(4, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 6000)).getWorstTimeInd(5, false));
    Assert.assertEquals(5, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 6000)).getWorstTimeInd(5, true));

    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 6000)).getWorstTimeInd(10, false));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 6000)).getWorstTimeInd(10, true));
    Assert.assertEquals(4, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 6000)).getWorstTimeInd(6, false));
    Assert.assertEquals(-1, new TimesStatistics(getTimesList(2000, 4000, 5000, 1000, -1, 6000)).getWorstTimeInd(6, true));
  }

  private List<Long> getTimesList(int... times) {
    List<Long> list = new ArrayList<Long>();
    for (int t : times) {
      list.add((long) t);
    }
    return list;
  }

}