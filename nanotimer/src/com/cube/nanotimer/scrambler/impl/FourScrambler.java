package com.cube.nanotimer.scrambler.impl;

public class FourScrambler extends AbstractCubeScrambler {

  @Override
  protected String[][] getMoves() {
    return new String[][]{
        {"U", "D", "u", "d"},
        {"F", "B", "f", "b"},
        {"R", "L", "r", "l"}
    };
  }

  @Override
  protected int getMoveCount() {
    return 40;
  }
}
