package com.cube.nanotimer.scrambler.randomstate;

import com.cube.nanotimer.vo.ThreeCubeState;

import java.util.ArrayList;
import java.util.List;

public class ThreeSolver {

  private static final int DEFAULT_MAX_SOLUTION_LENGTH = 23;
  private static final int MAX_PHASE2_SOLUTION_LENGTH = 12;
  private static final int SAFE_PHASE1_ITERATIONS_LIMIT = 30;
  private static final int SEARCH_TIME_MIN = 150; // time in ms during which to search for a better solution

  public static final boolean SHOW_PHASE_SEPARATOR = false; // for debug

  private ThreeCubeState initialState;
  private List<Byte> solution1;
  private List<Byte> solution2;
  private List<Byte> bestSolution1;
  private List<Byte> bestSolution2;
  private long searchStartTs;
  private int maxSolutionLength;

  private boolean mustStop = false;

  static final Move[] moves;
  static final Move[] moves1;
  static final Move[] moves2;
  static final Move[] allMoves2;
  private static final byte[] slices;
  private static final byte[] opposites;

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

  private boolean phase1(int cornerOrientation, int edgeOrientation, int eEdgeCombination, int depth, byte lastMove, byte oldLastMove) throws InterruptedException {
    boolean foundSolution = false;
    if (depth == 0) {
      if (cornerOrientation == 0 && edgeOrientation == 0 && eEdgeCombination == 0) {
        // check that last move is not a phase 2 move
        if (lastMove >= 0) {
          Move m = moves[lastMove];
          for (int i = 0; i < allMoves2.length; i++) {
            if (allMoves2[i] == m) {
              return false;
            }
          }
        }
        // generate phase 2 state
        ThreeCubeState state = new ThreeCubeState(initialState);
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
        int maxDepth = Math.min(MAX_PHASE2_SOLUTION_LENGTH, maxSolutionLength - solution1.size());
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
    if (mustStop) {
      throw new InterruptedException("Scramble interruption requested.");
    }

    if (StateTables.pruningCornerOrientation[cornerOrientation][eEdgeCombination] <= depth &&
        StateTables.pruningEdgeOrientation[edgeOrientation][eEdgeCombination] <= depth) {
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
          corOri = StateTables.transitCornerOrientation[corOri][i];
          edgOri = StateTables.transitEdgeOrientation[edgOri][i];
          edgCom = StateTables.transitEEdgeCombination[edgCom][i];
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

    if (StateTables.pruningCornerPermutation[cornerPermutation][eEdgePermutation] <= depth &&
        StateTables.pruningUDEdgePermutation[udEdgePermutation][eEdgePermutation] <= depth) {
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
          corPerm = StateTables.transitCornerPermutation[corPerm][i];
          udEdgPerm = StateTables.transitUDEdgePermutation[udEdgPerm][i];
          eEdgPerm = StateTables.transitEEdgePermutation[eEdgPerm][i];
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

  public String[] getSolution(ThreeCubeState cubeState) {
    return getSolution(cubeState, null);
  }

  public String[] getSolution(ThreeCubeState cubeState, ScrambleConfig config) {
//    genTables(); // (now generated from ScramblerService)
    if (config != null && config.getMaxLength() > 0) {
      maxSolutionLength = config.getMaxLength();
    } else {
      maxSolutionLength = DEFAULT_MAX_SOLUTION_LENGTH;
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

    try {
      for (int i = 0; i < SAFE_PHASE1_ITERATIONS_LIMIT && (bestSolution1 == null || System.currentTimeMillis() < searchStartTs + SEARCH_TIME_MIN); i++) {
        if (phase1(cornerOrientation, edgeOrientation, eEdgeCombination, i, (byte) -1, (byte) -1)) {
          solution1 = new ArrayList<Byte>();
          solution2 = new ArrayList<Byte>();
        }
      }
    } catch (InterruptedException e) {
      // ignore, user requested stop
    }

    String[] solution = null;
    if (bestSolution2 != null) {
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
//    Log.i("[NanoTimer]", "solution time: " + (System.currentTimeMillis() - searchStartTs));

    return solution;
  }

  public static void genTables() {
    if (StateTables.transitCornerPermutation == null) {
      StateTables.generateTables(moves1, moves2);
    }
  }

  public void stop() {
    mustStop = true;
  }

  // Performs moves on a cube state, and also modifies the first cubies positions
  private void applyMoves(ThreeCubeState state, List<Byte> moves) {
    for (Byte m : moves) {
      Move move = ThreeSolver.moves[m];
      state.edgePermutations = StateTables.getPermResult(state.edgePermutations, move.edgPerm);
      state.cornerPermutations = StateTables.getPermResult(state.cornerPermutations, move.corPerm);
      state.edgeOrientations = StateTables.getOrientResult(state.edgeOrientations, move.edgPerm, move.edgOrient, 2);
      state.cornerOrientations = StateTables.getOrientResult(state.cornerOrientations, move.corPerm, move.corOrient, 3);
    }
  }

}
