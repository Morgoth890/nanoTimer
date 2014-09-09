package com.cube.nanotimer.scrambler.impl;

import com.cube.nanotimer.scrambler.Scrambler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClockScrambler implements Scrambler {

  private static final int MOVES_COUNT = 12;

  @Override
  public String[] getNewScramble() {
    List<ClockMove> clockMoves = new ArrayList<ClockMove>();
    List<String> strMoves = new ArrayList<String>();
    for (int i = 0; i < MOVES_COUNT; ) {
      ClockMove cm = ClockMove.getRandomMove();
      boolean cancelMove = false;
      for (int j = 0; j < i; j++) {
        if (cm.equalsCornerPos(clockMoves.get(j))) {
          cancelMove = true;
        }
      }
      if (cancelMove) {
        continue;
      }
      clockMoves.add(cm);
      strMoves.add(cm.toString() + " ");
      i++;
      if (i % 3 == 0) {
        strMoves.add("\n");
      }
    }
    return strMoves.toArray(new String[0]);
  }

  private static class ClockMove {
    boolean buttons[] = new boolean[4];
    int corner; // 1 to 4
    int hours;  // 1 to 6 and -1 to -5
    private static Random r = new Random();

    public static ClockMove getRandomMove() {
      ClockMove cm = new ClockMove();
      for (int i = 0; i < cm.buttons.length; i++) {
        cm.buttons[i] = r.nextBoolean();
      }
      cm.corner = r.nextInt(4) + 1;
      cm.hours = r.nextInt(11);
      if (cm.hours <= 4) {
        cm.hours -= 5; // -1 to -5
      } else {
        cm.hours -= 4; // 1 to 6
      }
      return cm;
    }

    public boolean equalsCornerPos(ClockMove m) {
      for (int i = 0; i < buttons.length; i++) {
        if (this.buttons[i] != m.buttons[i]) {
          return false;
        }
      }
      return this.corner == m.corner;
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("(");
      for (boolean b : buttons) {
        if (b) {
          sb.append("U");
        } else {
          sb.append("d");
        }
      }
      sb.append(",").append(String.format("%2d", corner)).append(",");
      sb.append(String.format("%2d", hours)).append(")");
      return sb.toString();
    }
  }

}