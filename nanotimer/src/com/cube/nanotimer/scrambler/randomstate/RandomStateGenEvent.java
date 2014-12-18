package com.cube.nanotimer.scrambler.randomstate;

import com.cube.nanotimer.vo.CubeType;

public class RandomStateGenEvent {

  public enum State { PREPARING, GENERATING, IDLE, STOPPING };
  public enum GenerationLaunch { AUTO, MANUAL, PLUGGED };

  private State state;
  private CubeType cubeType;
  private GenerationLaunch generationLaunch;
  private int curScramble;
  private int totToGen;

  public RandomStateGenEvent(State state, CubeType cubeType, GenerationLaunch generationLaunch, int curScramble, int totToGen) {
    this.state = state;
    this.cubeType = cubeType;
    this.generationLaunch = generationLaunch;
    this.curScramble = curScramble;
    this.totToGen = totToGen;
  }

  public State getState() {
    return state;
  }

  public CubeType getCubeType() {
    return cubeType;
  }

  public GenerationLaunch getGenerationLaunch() {
    return generationLaunch;
  }

  public String getCubeTypeName() {
    return cubeType == null ? "" : cubeType.getName();
  }

  public int getCurScramble() {
    return curScramble;
  }

  public int getTotalToGenerate() {
    return totToGen;
  }

}
