package com.willli.smart_scan;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface ScanResultDao {

  @Insert
  void insert(ScanResult scanResult);

  @Query("SELECT * FROM scan_results ORDER BY timestamp DESC")
  List<ScanResult> getAllScanResults();

  @Query("SELECT * FROM scan_results WHERE batchId = :batchId ORDER BY timestamp DESC")
  List<ScanResult> getScanResultsByBatch(String batchId);

  @Query("SELECT DISTINCT batchId FROM scan_results GROUP BY batchId ORDER BY MAX(timestamp) DESC")
  List<String> getAllBatchIds();

  @Delete
  void delete(ScanResult scanResult);

  @Query("DELETE FROM scan_results WHERE batchId = :batchId")
  void deleteBatch(String batchId);

  @Query("DELETE FROM scan_results")
  void deleteAll();
}
