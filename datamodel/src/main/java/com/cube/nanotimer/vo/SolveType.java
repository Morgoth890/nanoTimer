package com.cube.nanotimer.vo;

import java.io.Serializable;
import java.util.Arrays;

public class SolveType implements Serializable, NameHolder {

  private int id;
  private String name;
  private int cubeTypeId;
  private SolveTypeStep[] steps = new SolveTypeStep[0];
  private ScrambleType scrambleType;
  private boolean blind = false;

  public SolveType(String name, boolean blind, ScrambleType scrambleType, int cubeTypeId) {
    this.name = name;
    this.blind = blind;
    this.scrambleType = scrambleType;
    this.cubeTypeId = cubeTypeId;
  }

  public SolveType(int id, String name, boolean blind, ScrambleType scrambleType, int cubeTypeId) {
    this(name, blind, scrambleType, cubeTypeId);
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getCubeTypeId() {
    return cubeTypeId;
  }

  public ScrambleType getScrambleType() {
    return scrambleType;
  }

  public boolean isBlind() {
    return blind;
  }

  public SolveTypeStep[] getSteps() {
    return steps;
  }

  public void setSteps(SolveTypeStep[] steps) {
    this.steps = steps;
  }

  public boolean hasSteps() {
    return steps != null && steps.length > 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SolveType)) return false;

    SolveType solveType = (SolveType) o;

    if (id != solveType.id) return false;
    if (cubeTypeId != solveType.cubeTypeId) return false;
    if (blind != solveType.blind) return false;
    if (!name.equals(solveType.name)) return false;
    // Probably incorrect - comparing Object[] arrays with Arrays.equals
    if (!Arrays.equals(steps, solveType.steps)) return false;
    return scrambleType == solveType.scrambleType;
  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + name.hashCode();
    result = 31 * result + cubeTypeId;
    result = 31 * result + (steps != null ? Arrays.hashCode(steps) : 0);
    result = 31 * result + (scrambleType != null ? scrambleType.hashCode() : 0);
    result = 31 * result + (blind ? 1 : 0);
    return result;
  }
}
