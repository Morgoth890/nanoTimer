package com.cube.nanotimer.scrambler.randomstate;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThreeSolver {

  static class CubeState {
    public byte[] cornerPermutations = new byte[8];
    public byte[] cornerOrientations = new byte[8];
    public byte[] edgePermutations = new byte[12];
    public byte[] edgeOrientations = new byte[12];

    public CubeState() {
    }

    public CubeState(CubeState cubeState) {
      System.arraycopy(cubeState.cornerPermutations, 0, this.cornerPermutations, 0, 8);
      System.arraycopy(cubeState.cornerOrientations, 0, this.cornerOrientations, 0, 8);
      System.arraycopy(cubeState.edgePermutations, 0, this.edgePermutations, 0, 12);
      System.arraycopy(cubeState.edgeOrientations, 0, this.edgeOrientations, 0, 12);
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Corner permutations: ").append(Arrays.toString(cornerPermutations)).append("\n");
      sb.append("Corner orientations: ").append(Arrays.toString(cornerOrientations)).append("\n");
      sb.append("Edge permutations: ").append(Arrays.toString(edgePermutations)).append("\n");
      sb.append("Edge orientations: ").append(Arrays.toString(edgeOrientations));
      return sb.toString();
    }
  }

  // TODO : pass the following static variables as parameters to solution(...) instead
  private static final int MAX_SOLUTION_LENGTH = 23;
  private static final int MAX_PHASE2_SOLUTION_LENGTH = 12;
//  private static final int MAX_SEARCH_TIME = 10; // in seconds. will stop after that time if a solution was found, even if it is not optimal
  private static final int SAFE_PHASE1_ITERATIONS_LIMIT = 30;
  // TODO : could maybe reduce this (but test on slow devices)
  private static final int SEARCH_TIME_MIN = 150; // time in ms during which to search for a better solution

  // TODO : there seems to be a lot or phase 2 moves in the solution. see if normal

  public static final boolean SHOW_PHASE_SEPARATOR = false; // for debug

  private CubeState initialState;
  private List<Byte> solution1;
  private List<Byte> solution2;
  private List<Byte> bestSolution1;
  private List<Byte> bestSolution2;
  private long searchStartTs;

  private final Object solutionSyncHelper = new Object();
  private int solutionSearchCount = 0;

  static Move[] moves;
  static Move[] moves1;
  static Move[] moves2;
  static Move[] allMoves2;
  private static byte[] slices;
  private static byte[] opposites;

  // Transition tables
  private static int[][] transitCornerPermutation;
  private static short[][] transitCornerOrientation;
  private static short[][] transitEEdgeCombination;
  private static short[][] transitEEdgePermutation;
  private static int[][] transitUDEdgePermutation;
  private static short[][] transitEdgeOrientation;

  // Pruning tables
  private static byte[][] pruningCornerOrientation;
  private static byte[][] pruningEdgeOrientation;
  private static byte[][] pruningCornerPermutation;
  private static byte[][] pruningUDEdgePermutation;

  static {
    // Moves
    moves = new Move[] {
        Move.U, Move.U2, Move.UP,
        Move.D, Move.D2, Move.DP,
        Move.R, Move.R2, Move.RP,
        Move.L, Move.L2, Move.LP,
        Move.F, Move.F2, Move.FP,
        Move.B, Move.B2, Move.BP
    };
    moves1 = new Move[] {
        Move.U,
        Move.D,
        Move.R,
        Move.L,
        Move.F,
        Move.B
    };
    moves2 = new Move[] {
        Move.U,
        Move.D,
        Move.R2,
        Move.L2,
        Move.F2,
        Move.B2
    };
    allMoves2 = new Move[] {
        Move.U, Move.U2, Move.UP,
        Move.D, Move.D2, Move.DP,
        Move.R2,
        Move.L2,
        Move.F2,
        Move.B2
    };

    // Slices
    slices = new byte[moves.length];
    for (int i = 0; i < moves.length; i++) {
      slices[i] = (byte) (i / 3);
    }

    // Opposites
    opposites = new byte[6];
    opposites[0] = 1;
    opposites[1] = 0;
    opposites[2] = 3;
    opposites[3] = 2;
    opposites[4] = 5;
    opposites[5] = 4;
  }

  private boolean phase1(int cornerOrientation, int edgeOrientation, int eEdgeCombination, int depth, byte lastMove, byte oldLastMove) {
    boolean foundSolution = false;
    if (depth == 0) {
      if (cornerOrientation == 0 && edgeOrientation == 0 && eEdgeCombination == 0) {
        // check that last move is not a phase 2 move
        // TODO : should make sure that two different enum instances of same type are equal (otherwise should compare name, or an id or something like that)
        if (lastMove >= 0) {
          Move m = moves[lastMove];
          for (int i = 0; i < allMoves2.length; i++) {
            if (allMoves2[i] == m) {
              return false;
            }
          }
        }
        // generate phase 2 state
        CubeState state = new CubeState(initialState);
        applyMoves(state, solution1);

        byte[] udEdgePermutation = new byte[8];
        byte[] eEdgePermutation = new byte[4];
        for (int i = 0; i < state.edgePermutations.length; i++) {
          if (i < 4) {
            eEdgePermutation[i] = state.edgePermutations[i];
          } else {
            udEdgePermutation[i - 4] = (byte) (state.edgePermutations[i] - 4);
          }
        }

        // search for phase 2 solution
        int maxDepth = Math.min(MAX_PHASE2_SOLUTION_LENGTH, MAX_SOLUTION_LENGTH - solution1.size());
        int corPerm = IndexConvertor.packPermutation(state.cornerPermutations);
        int udEdgPerm = IndexConvertor.packPermutation(udEdgePermutation);
        int eEdgPerm = IndexConvertor.packPermutation(eEdgePermutation);
        for (int i = 0; i <= maxDepth && !foundSolution; i++) {
          foundSolution |= phase2(corPerm, udEdgPerm, eEdgPerm, i, lastMove, oldLastMove);
        }
      }
      return foundSolution;
    }
    if (bestSolution1 != null && System.currentTimeMillis() > searchStartTs + SEARCH_TIME_MIN) {
      return false;
    }

    if (pruningCornerOrientation[cornerOrientation][eEdgeCombination] <= depth &&
        pruningEdgeOrientation[edgeOrientation][eEdgeCombination] <= depth) {
      int curSolutionSize = solution1.size();
      for (byte i = 0; i < moves1.length; i++) {
        if ((lastMove >= 0 && i == slices[lastMove]) || // same face twice in a row
            (oldLastMove >= 0 && opposites[i] == slices[lastMove] && opposites[slices[lastMove]] == slices[oldLastMove])) { // opposite faces 3 times in a row
          continue;
        }
        int corOri = cornerOrientation;
        int edgOri = edgeOrientation;
        int edgCom = eEdgeCombination;
        for (int j = 0; j < 3; j++) {
          corOri = transitCornerOrientation[corOri][i];
          edgOri = transitEdgeOrientation[edgOri][i];
          edgCom = transitEEdgeCombination[edgCom][i];
          byte nextMove = (byte) (i * 3 + j);
          solution1.add(nextMove);
          foundSolution |= phase1(corOri, edgOri, edgCom, depth - 1, nextMove, lastMove);
          solution1.remove(curSolutionSize);
        }
      }
    }
    return foundSolution;
  }

  private boolean phase2(int cornerPermutation, int udEdgePermutation, int eEdgePermutation, int depth, byte lastMove, byte oldLastMove) {
    if (depth == 0) {
      if (cornerPermutation == 0 && udEdgePermutation == 0 && eEdgePermutation == 0) {
        //Log.i("[NanoTimer]", "tmp solution: " + Arrays.toString(solution1.toArray()) + " . " + Arrays.toString(solution2.toArray()));
        if (bestSolution1 == null || solution1.size() + solution2.size() < bestSolution1.size() + bestSolution2.size()) {
          //Log.i("[NanoTimer]", "replacing solution");
          bestSolution1 = new ArrayList<Byte>(solution1.size());
          for (Byte m : solution1) {
            bestSolution1.add(m);
          }
          bestSolution2 = new ArrayList<Byte>(solution2.size());
          for (Byte m : solution2) {
            bestSolution2.add(m);
          }
        }
        solution2 = new ArrayList<Byte>();
        return true;
      }
      return false;
    }

    if (pruningCornerPermutation[cornerPermutation][eEdgePermutation] <= depth &&
        pruningUDEdgePermutation[udEdgePermutation][eEdgePermutation] <= depth) {
      int curSolutionSize = solution2.size();
      for (byte i = 0; i < moves2.length; i++) {
        if ((lastMove >= 0 && i == slices[lastMove]) || // same face twice in a row
            (oldLastMove >= 0 && opposites[i] == slices[lastMove] && opposites[slices[lastMove]] == slices[oldLastMove])) { // opposite faces 3 times in a row
          continue;
        }
        int corPerm = cornerPermutation;
        int udEdgPerm = udEdgePermutation;
        int eEdgPerm = eEdgePermutation;
        int nSubMoves = (i < 2) ? 3 : 1;
        for (int j = 0; j < nSubMoves; j++) {
          corPerm = transitCornerPermutation[corPerm][i];
          udEdgPerm = transitUDEdgePermutation[udEdgPerm][i];
          eEdgPerm = transitEEdgePermutation[eEdgPerm][i];
          byte nextMove = (byte) (i * 3 + j + ((i < 2) ? 0 : 1));

          solution2.add(nextMove);
          if (phase2(corPerm, udEdgPerm, eEdgPerm, depth - 1, nextMove, lastMove)) {
            return true;
          }
          solution2.remove(curSolutionSize);
        }
      }
    }
    return false;
  }

  public String[] getSolution(CubeState cubeState) {
    synchronized (solutionSyncHelper) {
      solutionSearchCount++;
      if (transitCornerPermutation == null) {
        getTables();
      }
    }
    searchStartTs = System.currentTimeMillis();

    initialState = cubeState;
    solution1 = new ArrayList<Byte>();
    solution2 = new ArrayList<Byte>();
    bestSolution1 = null;
    bestSolution2 = null;

    int cornerOrientation = IndexConvertor.packOrientation(cubeState.cornerOrientations, 3);
    int edgeOrientation = IndexConvertor.packOrientation(cubeState.edgeOrientations, 2);
    boolean[] combinations = new boolean[cubeState.edgePermutations.length];
    for (int i = 0; i < cubeState.edgePermutations.length; i++) {
      combinations[i] = (cubeState.edgePermutations[i] < 4);
    }
    int eEdgeCombination = IndexConvertor.packCombination(combinations, 4);

    for (int i = 0; i < SAFE_PHASE1_ITERATIONS_LIMIT && (bestSolution1 == null || System.currentTimeMillis() < searchStartTs + SEARCH_TIME_MIN); i++) {
      if (phase1(cornerOrientation, edgeOrientation, eEdgeCombination, i, (byte) -1, (byte) -1)) {
        solution1 = new ArrayList<Byte>();
        solution2 = new ArrayList<Byte>();
      }
    }

    String[] solution = null;
    if (bestSolution1 != null) {
      int length = bestSolution1.size() + bestSolution2.size() + (SHOW_PHASE_SEPARATOR ? 1 : 0);
      solution = new String[length];
      int i = 0;
      for (Byte m : bestSolution1) {
        solution[i++] = moves[m].name;
      }
      if (SHOW_PHASE_SEPARATOR) {
        solution[i++] = ".";
      }
      for (Byte m : bestSolution2) {
        solution[i++] = moves[m].name;
      }
    }
    Log.i("[NanoTimer]", "solution time: " + (System.currentTimeMillis() - searchStartTs));

    synchronized (solutionSyncHelper) {
      solutionSearchCount--;
      solutionSyncHelper.notify();
    }

    return solution;
  }

  public void genTables() {
    if (transitCornerPermutation == null) {
      getTables();
    }
  }

  public void freeMemory() {
    synchronized (solutionSyncHelper) {
      while (solutionSearchCount > 0) {
        try {
          solutionSyncHelper.wait();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      StateTables.transitCornerPermutation = null;
      StateTables.transitCornerOrientation = null;
      StateTables.transitEEdgeCombination = null;
      StateTables.transitEEdgePermutation = null;
      StateTables.transitUDEdgePermutation = null;
      StateTables.transitEdgeOrientation = null;

      StateTables.pruningCornerOrientation = null;
      StateTables.pruningEdgeOrientation = null;
      StateTables.pruningCornerPermutation = null;
      StateTables.pruningUDEdgePermutation = null;
    }
  }

  private void getTables() {
    StateTables.generateTables(moves1, moves2);
    transitCornerPermutation = StateTables.transitCornerPermutation;
    transitCornerOrientation = StateTables.transitCornerOrientation;
    transitEEdgeCombination = StateTables.transitEEdgeCombination;
    transitEEdgePermutation = StateTables.transitEEdgePermutation;
    transitUDEdgePermutation = StateTables.transitUDEdgePermutation;
    transitEdgeOrientation = StateTables.transitEdgeOrientation;

    pruningCornerOrientation = StateTables.pruningCornerOrientation;
    pruningEdgeOrientation = StateTables.pruningEdgeOrientation;
    pruningCornerPermutation = StateTables.pruningCornerPermutation;
    pruningUDEdgePermutation = StateTables.pruningUDEdgePermutation;
  }

  // Performs moves on a cube state, and also modifies the first cubies positions
  private void applyMoves(CubeState state, List<Byte> moves) {
    for (Byte m : moves) {
      Move move = ThreeSolver.moves[m];
      state.edgePermutations = StateTables.getPermResult(state.edgePermutations, move.edgPerm);
      state.cornerPermutations = StateTables.getPermResult(state.cornerPermutations, move.corPerm);
      state.edgeOrientations = StateTables.getOrientResult(state.edgeOrientations, move.edgPerm, move.edgOrient, 2);
      state.cornerOrientations = StateTables.getOrientResult(state.cornerOrientations, move.corPerm, move.corOrient, 3);
    }
  }

}
