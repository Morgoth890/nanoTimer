package com.cube.nanotimer.scrambler.randomstate;

import com.cube.nanotimer.scrambler.randomstate.square1.RSSquare1Scrambler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

@RunWith(JUnit4.class)
public class RandomStateSquare1Test {

  @Test
  public void basicTest() {
    new RSSquare1Scrambler().genTables();
    RSSquare1Scrambler randomScrambler = new RSSquare1Scrambler();
    String[] scramble1 = randomScrambler.getNewScramble(null);

    // save to files
    /*try {
      FileUtils.compressToGzipAndPersist(Square1Solver.shapes, "square1_shapes.dat");
      FileUtils.compressToGzipAndPersist(Square1Solver.evenShapeDistance, "square1_even_shape_distance.dat");
      FileUtils.compressToGzipAndPersist(Square1Solver.oddShapeDistance, "square1_odd_shape_distance.dat");
      FileUtils.compressToGzipAndPersist(Utils.toSingleDimensionByteArray(Square1Solver.cornersDistance), "square1_corners_distance.dat");
      FileUtils.compressToGzipAndPersist(Utils.toSingleDimensionByteArray(Square1Solver.edgesDistance), "square1_edges_distance.dat");

      byte[][] array = Utils.toTwoDimensionalByteArray(FileUtils.loadCompressedGzip("square1_edges_distance.dat"), 40320);
      Assert.assertTrue(Arrays.deepEquals(array, Square1Solver.edgesDistance));

//      ArrayList<Square1State> shapes = (ArrayList<Square1State>) FileUtils.loadCompressedGzipSerializable("square1_shapes.dat");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (DataFormatException e) {
      e.printStackTrace();
    }*/

    System.out.println(Arrays.toString(scramble1));
  }

}
