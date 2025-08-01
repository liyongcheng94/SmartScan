package com.willli.smart_scan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
  private static final int STORAGE_PERMISSION_REQUEST = 200;
  private ScanDatabase database;
  private RecyclerView recyclerView;
  private HistoryAdapter adapter;
  private Button exportButton;
  private Button clearButton;
  private List<ScanResult> allResults;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_history);

    database = ScanDatabase.getDatabase(this);

    initViews();
    loadHistory();
  }

  private void initViews() {
    recyclerView = findViewById(R.id.recycler_view);
    exportButton = findViewById(R.id.export_button);
    clearButton = findViewById(R.id.clear_button);

    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    exportButton.setOnClickListener(v -> showExportOptionsDialog());
    clearButton.setOnClickListener(v -> showClearConfirmDialog());
  }

  private void loadHistory() {
    new Thread(() -> {
      allResults = database.scanResultDao().getAllScanResults();
      runOnUiThread(() -> {
        adapter = new HistoryAdapter(allResults, this::onItemClick);
        recyclerView.setAdapter(adapter);
      });
    }).start();
  }

  private void onItemClick(ScanResult scanResult) {
    // 解析发票信息
    String[] invoiceInfo = parseInvoiceInfo(scanResult.getContent());

    // 构建详情信息
    StringBuilder detailMessage = new StringBuilder();
    detailMessage.append("内容: ").append(scanResult.getContent()).append("\n")
        .append("格式: ").append(scanResult.getFormat()).append("\n")
        .append("时间: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(new Date(scanResult.getTimestamp())))
        .append("\n")
        .append("批次: ").append(scanResult.getBatchId());

    // 如果有发票信息，则显示
    if (!invoiceInfo[0].isEmpty() || !invoiceInfo[1].isEmpty() || !invoiceInfo[2].isEmpty()) {
      detailMessage.append("\n\n--- 发票信息 ---");
      if (!invoiceInfo[0].isEmpty()) {
        detailMessage.append("\n发票号码: ").append(invoiceInfo[0]);
      }
      if (!invoiceInfo[1].isEmpty()) {
        detailMessage.append("\n金额: ").append(invoiceInfo[1]);
      }
      if (!invoiceInfo[2].isEmpty()) {
        detailMessage.append("\n日期: ").append(invoiceInfo[2]);
      }
    }

    // 点击历史记录项的处理
    new AlertDialog.Builder(this)
        .setTitle("扫描结果详情")
        .setMessage(detailMessage.toString())
        .setPositiveButton("复制", (dialog, which) -> {
          android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(
              CLIPBOARD_SERVICE);
          android.content.ClipData clip = android.content.ClipData.newPlainText("scan_result", scanResult.getContent());
          clipboard.setPrimaryClip(clip);
          Toast.makeText(this, "已复制到剪贴板", Toast.LENGTH_SHORT).show();
        })
        .setNegativeButton("关闭", null)
        .show();
  }

  private void showExportOptionsDialog() {
    if (allResults == null || allResults.isEmpty()) {
      Toast.makeText(this, "没有数据可导出", Toast.LENGTH_SHORT).show();
      return;
    }

    // 计算今天的数据数量
    long todayStart = getTodayStartTimestamp();
    long todayEnd = getTodayEndTimestamp();

    long todayCount = 0;
    for (ScanResult result : allResults) {
      if (result.getTimestamp() >= todayStart && result.getTimestamp() <= todayEnd) {
        todayCount++;
      }
    }

    // 获取最新批次ID和该批次的数据数量
    String latestBatchId = getLatestBatchId();
    long batchCount = 0;
    if (latestBatchId != null && !latestBatchId.isEmpty()) {
      for (ScanResult result : allResults) {
        if (latestBatchId.equals(result.getBatchId())) {
          batchCount++;
        }
      }
    }

    // 构建选项列表
    java.util.List<String> optionsList = new java.util.ArrayList<>();
    optionsList.add("导出今天 (" + todayCount + " 条记录)");
    optionsList.add("导出全部 (" + allResults.size() + " 条记录)");

    if (latestBatchId != null && !latestBatchId.isEmpty() && batchCount > 0) {
      optionsList.add("导出最新批次 (" + batchCount + " 条记录)");
    }

    String[] options = optionsList.toArray(new String[0]);

    new AlertDialog.Builder(this)
        .setTitle("选择导出范围")
        .setItems(options, (dialog, which) -> {
          if (which == 0) {
            // 导出今天
            exportData(true, false, null);
          } else if (which == 1) {
            // 导出全部
            exportData(false, false, null);
          } else if (which == 2) {
            // 导出最新批次
            exportData(false, true, latestBatchId);
          }
        })
        .setNegativeButton("取消", null)
        .show();
  }

  private long getTodayStartTimestamp() {
    java.util.Calendar calendar = java.util.Calendar.getInstance();
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
    calendar.set(java.util.Calendar.MINUTE, 0);
    calendar.set(java.util.Calendar.SECOND, 0);
    calendar.set(java.util.Calendar.MILLISECOND, 0);
    return calendar.getTimeInMillis();
  }

  private long getTodayEndTimestamp() {
    java.util.Calendar calendar = java.util.Calendar.getInstance();
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
    calendar.set(java.util.Calendar.MINUTE, 59);
    calendar.set(java.util.Calendar.SECOND, 59);
    calendar.set(java.util.Calendar.MILLISECOND, 999);
    return calendar.getTimeInMillis();
  }

  private void exportData(boolean todayOnly, boolean batchOnly, String batchId) {
    try {
      List<ScanResult> dataToExport;
      String exportType;

      if (todayOnly) {
        long todayStart = getTodayStartTimestamp();
        long todayEnd = getTodayEndTimestamp();

        // 手动过滤今天的数据
        dataToExport = new java.util.ArrayList<>();
        for (ScanResult result : allResults) {
          if (result.getTimestamp() >= todayStart && result.getTimestamp() <= todayEnd) {
            dataToExport.add(result);
          }
        }
        exportType = "今天";

        if (dataToExport.isEmpty()) {
          Toast.makeText(this, "今天没有扫描数据", Toast.LENGTH_SHORT).show();
          return;
        }
      } else if (batchOnly && batchId != null) {
        // 过滤指定批次的数据
        dataToExport = new java.util.ArrayList<>();
        for (ScanResult result : allResults) {
          if (batchId.equals(result.getBatchId())) {
            dataToExport.add(result);
          }
        }
        exportType = "批次_" + batchId;

        if (dataToExport.isEmpty()) {
          Toast.makeText(this, "该批次没有扫描数据", Toast.LENGTH_SHORT).show();
          return;
        }
      } else {
        dataToExport = allResults;
        exportType = "全部";
      }

      checkStoragePermissionAndExport(dataToExport, exportType);
    } catch (Exception e) {
      Toast.makeText(this, "准备导出数据失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
  }

  // 保持向后兼容性的重载方法
  private void exportData(boolean todayOnly) {
    exportData(todayOnly, false, null);
  }

  private void checkStoragePermissionAndExport() {
    // 保持原有方法的兼容性，默认导出全部
    checkStoragePermissionAndExport(allResults, "全部");
  }

  private void checkStoragePermissionAndExport(List<ScanResult> dataToExport, String exportType) {
    try {
      if (dataToExport == null || dataToExport.isEmpty()) {
        Toast.makeText(this, "没有数据可导出", Toast.LENGTH_SHORT).show();
        return;
      }

      // Android 10及以上版本使用应用内部存储，不需要存储权限
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        exportToExcelWithSAF(dataToExport, exportType);
      } else {
        // Android 9及以下版本需要存储权限
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
          // 请求存储权限
          ActivityCompat.requestPermissions(this,
              new String[] {
                  Manifest.permission.WRITE_EXTERNAL_STORAGE,
                  Manifest.permission.READ_EXTERNAL_STORAGE
              },
              STORAGE_PERMISSION_REQUEST);
        } else {
          exportToExcelLegacy(dataToExport, exportType);
        }
      }
    } catch (Exception e) {
      Toast.makeText(this, "权限检查失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
  }

  private void exportToExcelWithSAF() {
    exportToExcelWithSAF(allResults, "全部");
  }

  private void exportToExcelWithSAF(List<ScanResult> dataToExport, String exportType) {
    if (dataToExport == null || dataToExport.isEmpty()) {
      Toast.makeText(this, "没有数据可导出", Toast.LENGTH_SHORT).show();
      return;
    }

    Toast.makeText(this, "正在导出" + exportType + "数据到Excel文件...", Toast.LENGTH_SHORT).show();

    new Thread(() -> {
      try {
        // 创建Excel文件
        String fileName = "扫描结果_" + exportType + "_" +
            new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) +
            ".xlsx";

        // 使用应用内部存储，不需要额外权限
        File internalDir = new File(getExternalFilesDir(null), "exports");
        if (!internalDir.exists()) {
          boolean dirCreated = internalDir.mkdirs();
          if (!dirCreated) {
            throw new RuntimeException("无法创建导出目录");
          }
        }

        File file = new File(internalDir, fileName);
        createExcelFile(file, dataToExport);

        runOnUiThread(() -> {
          Toast.makeText(this, "Excel文件已保存到应用目录\n" + file.getAbsolutePath(),
              Toast.LENGTH_LONG).show();

          // 尝试分享文件
          shareExcelFile(file);
        });

      } catch (Exception e) {
        android.util.Log.e("HistoryActivity", "Excel导出失败", e);
        runOnUiThread(() -> Toast.makeText(this, "导出失败: " + e.getMessage(),
            Toast.LENGTH_LONG).show());
      }
    }).start();
  }

  private void exportToExcelLegacy() {
    exportToExcelLegacy(allResults, "全部");
  }

  private void exportToExcelLegacy(List<ScanResult> dataToExport, String exportType) {
    if (dataToExport == null || dataToExport.isEmpty()) {
      Toast.makeText(this, "没有数据可导出", Toast.LENGTH_SHORT).show();
      return;
    }

    Toast.makeText(this, "正在导出" + exportType + "数据到Excel文件...", Toast.LENGTH_SHORT).show();

    new Thread(() -> {
      try {
        String fileName = "扫描结果_" + exportType + "_" +
            new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) +
            ".xlsx";

        // 尝试保存到Downloads目录，如果失败则保存到外部存储根目录
        File file;
        try {
          File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
          if (downloadsDir != null && (downloadsDir.exists() || downloadsDir.mkdirs())) {
            file = new File(downloadsDir, fileName);
          } else {
            file = new File(Environment.getExternalStorageDirectory(), fileName);
          }
        } catch (Exception e) {
          file = new File(Environment.getExternalStorageDirectory(), fileName);
        }

        createExcelFile(file, dataToExport);

        final File finalFile = file;
        runOnUiThread(() -> {
          Toast.makeText(this, "Excel文件已保存到: " + finalFile.getAbsolutePath(),
              Toast.LENGTH_LONG).show();

          shareExcelFile(finalFile);
        });

      } catch (Exception e) {
        android.util.Log.e("HistoryActivity", "Excel导出失败(Legacy)", e);
        runOnUiThread(() -> Toast.makeText(this, "导出失败: " + e.getMessage(),
            Toast.LENGTH_LONG).show());
      }
    }).start();
  }

  private void createExcelFile(File file) throws IOException {
    createExcelFile(file, allResults);
  }

  private void createExcelFile(File file, List<ScanResult> dataToExport) throws IOException {
    FileOutputStream fileOut = null;
    Workbook workbook = null;

    try {
      android.util.Log.d("HistoryActivity", "开始创建Excel文件: " + file.getAbsolutePath());

      workbook = new XSSFWorkbook();
      Sheet sheet = workbook.createSheet("扫描结果");

      // 创建标题行
      Row headerRow = sheet.createRow(0);
      headerRow.createCell(0).setCellValue("序号");
      headerRow.createCell(1).setCellValue("内容");
      headerRow.createCell(2).setCellValue("格式");
      headerRow.createCell(3).setCellValue("扫描时间");
      headerRow.createCell(4).setCellValue("批次ID");
      headerRow.createCell(5).setCellValue("发票号码");
      headerRow.createCell(6).setCellValue("金额");
      headerRow.createCell(7).setCellValue("日期");

      // 填充数据
      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
      for (int i = 0; i < dataToExport.size(); i++) {
        ScanResult result = dataToExport.get(i);
        Row row = sheet.createRow(i + 1);
        row.createCell(0).setCellValue(i + 1);
        row.createCell(1).setCellValue(result.getContent() != null ? result.getContent() : "");
        row.createCell(2).setCellValue(result.getFormat() != null ? result.getFormat() : "");
        row.createCell(3).setCellValue(dateFormat.format(new Date(result.getTimestamp())));
        row.createCell(4).setCellValue(result.getBatchId() != null ? result.getBatchId() : "");

        // 解析二维码内容提取发票信息
        String[] invoiceInfo = parseInvoiceInfo(result.getContent());
        row.createCell(5).setCellValue(invoiceInfo[0]); // 发票号码
        row.createCell(6).setCellValue(invoiceInfo[1]); // 金额
        row.createCell(7).setCellValue(invoiceInfo[2]); // 日期
      }

      // 手动设置列宽，避免使用 autoSizeColumn()
      // autoSizeColumn() 在Android上会失败，因为需要 java.awt.font.FontRenderContext
      sheet.setColumnWidth(0, 8 * 256); // 序号列：8字符宽度
      sheet.setColumnWidth(1, 30 * 256); // 内容列：30字符宽度
      sheet.setColumnWidth(2, 15 * 256); // 格式列：15字符宽度
      sheet.setColumnWidth(3, 20 * 256); // 时间列：20字符宽度
      sheet.setColumnWidth(4, 15 * 256); // 批次ID列：15字符宽度
      sheet.setColumnWidth(5, 20 * 256); // 发票号码列：20字符宽度
      sheet.setColumnWidth(6, 15 * 256); // 金额列：15字符宽度
      sheet.setColumnWidth(7, 15 * 256); // 日期列：15字符宽度

      // 确保父目录存在
      File parentDir = file.getParentFile();
      if (parentDir != null && !parentDir.exists()) {
        parentDir.mkdirs();
      }

      fileOut = new FileOutputStream(file);
      workbook.write(fileOut);

    } finally {
      if (fileOut != null) {
        try {
          fileOut.close();
        } catch (IOException e) {
          // 忽略关闭错误
        }
      }
      if (workbook != null) {
        try {
          workbook.close();
        } catch (IOException e) {
          // 忽略关闭错误
        }
      }
    }
  }

  private void shareExcelFile(File file) {
    try {
      if (!file.exists() || !file.canRead()) {
        Toast.makeText(this, "文件不存在或无法读取", Toast.LENGTH_SHORT).show();
        return;
      }

      Intent intent = new Intent(Intent.ACTION_SEND);
      intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

      // 尝试使用FileProvider分享文件
      try {
        Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
            this,
            getPackageName() + ".fileprovider",
            file);

        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "扫描结果导出");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "分享Excel文件"));

      } catch (Exception providerException) {
        // FileProvider失败，尝试直接使用文件URI (仅适用于较旧的Android版本)
        try {
          Uri fileUri = Uri.fromFile(file);
          intent.putExtra(Intent.EXTRA_STREAM, fileUri);
          intent.putExtra(Intent.EXTRA_SUBJECT, "扫描结果导出");

          startActivity(Intent.createChooser(intent, "分享Excel文件"));

        } catch (Exception directUriException) {
          // 分享失败，尝试打开文件夹
          openFileLocation(file);
        }
      }

    } catch (Exception e) {
      Toast.makeText(this, "分享文件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  private void openFileLocation(File file) {
    try {
      // 尝试打开文件所在文件夹
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setDataAndType(Uri.fromFile(file.getParentFile()), "resource/folder");

      if (intent.resolveActivityInfo(getPackageManager(), 0) != null) {
        startActivity(intent);
      } else {
        // 如果无法打开文件夹，显示文件路径
        Toast.makeText(this, "文件已保存到:\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
      }
    } catch (Exception e) {
      Toast.makeText(this, "文件已保存，但无法自动打开\n路径: " + file.getAbsolutePath(),
          Toast.LENGTH_LONG).show();
    }
  }

  private void showClearConfirmDialog() {
    new AlertDialog.Builder(this)
        .setTitle("确认清空")
        .setMessage("确定要清空所有历史记录吗？此操作不可恢复。")
        .setPositiveButton("确定", (dialog, which) -> clearAllHistory())
        .setNegativeButton("取消", null)
        .show();
  }

  private void clearAllHistory() {
    new Thread(() -> {
      database.scanResultDao().deleteAll();
      runOnUiThread(() -> {
        allResults.clear();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "历史记录已清空", Toast.LENGTH_SHORT).show();
      });
    }).start();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == STORAGE_PERMISSION_REQUEST) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        exportToExcelLegacy();
      } else {
        Toast.makeText(this, "需要存储权限才能导出文件", Toast.LENGTH_SHORT).show();
      }
    }
  }

  /**
   * 获取最新的批次ID
   * 
   * @return 最新的批次ID，如果没有数据则返回null
   */
  private String getLatestBatchId() {
    if (allResults == null || allResults.isEmpty()) {
      return null;
    }

    // 找到最新时间戳的记录的批次ID
    long latestTimestamp = 0;
    String latestBatchId = null;

    for (ScanResult result : allResults) {
      if (result.getBatchId() != null && !result.getBatchId().isEmpty()) {
        if (result.getTimestamp() > latestTimestamp) {
          latestTimestamp = result.getTimestamp();
          latestBatchId = result.getBatchId();
        }
      }
    }

    return latestBatchId;
  }

  /**
   * 解析二维码内容，提取发票号码、金额和日期
   * 
   * @param content 二维码内容
   * @return 包含发票号码、金额、日期的数组 [发票号码, 金额, 日期]
   */
  private String[] parseInvoiceInfo(String content) {
    String[] result = { "", "", "" }; // [发票号码, 金额, 日期]

    if (content == null || content.trim().isEmpty()) {
      return result;
    }

    try {
      // 按逗号分割内容
      String[] parts = content.split(",");

      if (parts.length >= 6) {
        // 发票号码：第4个字段（索引3）
        if (parts.length > 3 && parts[3] != null && !parts[3].trim().isEmpty()) {
          result[0] = parts[3].trim();
        }

        // 金额：第5个字段（索引4）
        if (parts.length > 4 && parts[4] != null && !parts[4].trim().isEmpty()) {
          result[1] = parts[4].trim();
        }

        // 日期：第6个字段（索引5）
        if (parts.length > 5 && parts[5] != null && !parts[5].trim().isEmpty()) {
          String dateStr = parts[5].trim();
          // 如果是8位数字格式（如20250721），转换为可读格式
          if (dateStr.matches("\\d{8}")) {
            try {
              String year = dateStr.substring(0, 4);
              String month = dateStr.substring(4, 6);
              String day = dateStr.substring(6, 8);
              result[2] = year + "-" + month + "-" + day;
            } catch (Exception e) {
              result[2] = dateStr;
            }
          } else {
            result[2] = dateStr;
          }
        }
      }
    } catch (Exception e) {
      // 解析失败时返回空值
      android.util.Log.w("HistoryActivity", "解析发票信息失败: " + e.getMessage());
    }

    return result;
  }
}
