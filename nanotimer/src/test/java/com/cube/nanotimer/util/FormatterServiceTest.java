package com.cube.nanotimer.util;

import android.test.AndroidTestCase;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FormatterServiceTest extends AndroidTestCase {
  @Test
  public void testSolveTimeFormat() {
    long locTime = 2004;
    Assert.assertEquals("2.00", FormatterService.INSTANCE.formatSolveTime(locTime, "", false));
    locTime = 2005;
    Assert.assertEquals("2.01", FormatterService.INSTANCE.formatSolveTime(locTime, "", false));
    locTime = 2009;
    Assert.assertEquals("2.01", FormatterService.INSTANCE.formatSolveTime(locTime, "", false));
    locTime = 2005;
    Assert.assertEquals("2.005", FormatterService.INSTANCE.formatSolveTime(locTime, "", true));
  }
}
