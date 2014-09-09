package com.cube.nanotimer.services;

import com.cube.nanotimer.services.db.DataCallback;
import com.cube.nanotimer.vo.CubeType;
import com.cube.nanotimer.vo.SolveAverages;
import com.cube.nanotimer.vo.SolveTime;
import com.cube.nanotimer.vo.SolveTimeAverages;
import com.cube.nanotimer.vo.SolveType;

import java.util.List;

public interface Service {
  void getCubeTypes(boolean getEmpty, DataCallback<List<CubeType>> callback);
  void getSolveTypes(CubeType cubeType, DataCallback<List<SolveType>> callback);
  void saveTime(SolveTime solveTime, DataCallback<SolveAverages> callback);
  void deleteTime(SolveTime solveTime, DataCallback<SolveAverages> callback);
  void getSolveAverages(SolveType solveType, DataCallback<SolveAverages> callback);
  void getHistory(SolveType solveType, DataCallback<List<SolveTime>> callback);
  void getHistory(SolveType solveType, long from, DataCallback<List<SolveTime>> callback);
  void deleteHistory(DataCallback<Void> callback);
  void deleteHistory(SolveType solveType, DataCallback<Void> callback);
  void getSessionTimes(SolveType solveType, DataCallback<List<Long>> callback);
  void startNewSession(SolveType solveType, long startTs, DataCallback<Void> callback);
  void saveSolveTypesOrder(List<SolveType> solveTypes, DataCallback<Void> callback);
  void getSolveTimeAverages(SolveTime solveTime, DataCallback<SolveTimeAverages> callback);

  void addSolveType(SolveType solveType, DataCallback<Integer> callback);
  void addSolveTypeSteps(SolveType solveType, DataCallback<Void> callback);
  void updateSolveType(SolveType solveType, DataCallback<Void> callback);
  void deleteSolveType(SolveType solveType, DataCallback<Void> callback);
}
