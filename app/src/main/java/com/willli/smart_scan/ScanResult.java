package com.willli.smart_scan;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "scan_results")
public class ScanResult {
  @PrimaryKey(autoGenerate = true)
  private int id;
  private String content;
  private String format;
  private long timestamp;
  private String batchId; // 批次ID，用于标识批量扫描

  public ScanResult() {
  }

  @Ignore
  public ScanResult(String content, String format, long timestamp, String batchId) {
    this.content = content;
    this.format = format;
    this.timestamp = timestamp;
    this.batchId = batchId;
  }

  // Getters
  public int getId() {
    return id;
  }

  public String getContent() {
    return content;
  }

  public String getFormat() {
    return format;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public String getBatchId() {
    return batchId;
  }

  // Setters
  public void setId(int id) {
    this.id = id;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public void setBatchId(String batchId) {
    this.batchId = batchId;
  }
}
