package com.willli.smart_scan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.google.zxing.ResultPoint;
import com.google.zxing.BarcodeFormat;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class BatchScanActivity extends AppCompatActivity {
  private static final int CAMERA_PERMISSION_REQUEST = 100;
  private static final int STORAGE_PERMISSION_REQUEST = 200;
  private DecoratedBarcodeView barcodeView;
  private ScanDatabase database;
  private List<ScanResult> scanResults;
  private BatchScanAdapter adapter;
  private RecyclerView recyclerView;
  private TextView countTextView;
  private Button finishButton;
  private Button exportButton;
  private String currentBatchId;
  private boolean isScanning = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_batch_scan);

    try {
      database = ScanDatabase.getDatabase(this);
      currentBatchId = "batch_" + UUID.randomUUID().toString().substring(0, 8);

      initViews();
      setupRecyclerView();

      // 检查摄像头权限
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this,
            new String[] { Manifest.permission.CAMERA }, CAMERA_PERMISSION_REQUEST);
      } else {
        startScanning();
      }
    } catch (Exception e) {
      Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
      finish();
    }
  }

  private void initViews() {
    try {
      // 先初始化数据列表
      scanResults = new ArrayList<>();

      barcodeView = findViewById(R.id.barcode_scanner);
      recyclerView = findViewById(R.id.recycler_view);
      countTextView = findViewById(R.id.count_text);
      finishButton = findViewById(R.id.finish_button);
      exportButton = findViewById(R.id.export_button);

      if (barcodeView == null || recyclerView == null || countTextView == null ||
          finishButton == null || exportButton == null) {
        throw new RuntimeException("无法找到必要的界面元素");
      }

      finishButton.setOnClickListener(v -> {
        if (isScanning) {
          stopScanning();
        } else {
          startScanning();
        }
      });

      exportButton.setOnClickListener(v -> {
        if (scanResults.isEmpty()) {
          Toast.makeText(this, "没有扫描结果可导出", Toast.LENGTH_SHORT).show();
        } else {
          exportCurrentBatch();
        }
      });

      updateUI();
    } catch (Exception e) {
      Toast.makeText(this, "界面初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
      finish();
    }
  }

  private void setupRecyclerView() {
    adapter = new BatchScanAdapter(scanResults);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setAdapter(adapter);

    // 配置支持的条形码格式，包括二维码和一维条形码
    Collection<BarcodeFormat> formats = Arrays.asList(
        // 二维码格式
        BarcodeFormat.QR_CODE,
        BarcodeFormat.DATA_MATRIX,
        BarcodeFormat.AZTEC,
        BarcodeFormat.PDF_417,
        // 一维条形码格式
        BarcodeFormat.CODE_128,
        BarcodeFormat.CODE_39,
        BarcodeFormat.CODE_93,
        BarcodeFormat.CODABAR,
        BarcodeFormat.EAN_13,
        BarcodeFormat.EAN_8,
        BarcodeFormat.ITF,
        BarcodeFormat.UPC_A,
        BarcodeFormat.UPC_E,
        BarcodeFormat.RSS_14,
        BarcodeFormat.RSS_EXPANDED);
    barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
  }

  private void startScanning() {
    try {
      isScanning = true;
      finishButton.setText("停止扫描");

      barcodeView.decodeContinuous(new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
          try {
            if (result.getText() != null && !isDuplicate(result.getText())) {
              // 保存扫描结果
              ScanResult scanResult = new ScanResult(
                  result.getText(),
                  result.getBarcodeFormat().toString(),
                  System.currentTimeMillis(),
                  currentBatchId);

              saveScanResult(scanResult);
              scanResults.add(0, scanResult); // 添加到列表顶部

              runOnUiThread(() -> {
                adapter.notifyItemInserted(0);
                recyclerView.scrollToPosition(0);
                updateUI();
              });
            }
          } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(BatchScanActivity.this,
                "处理扫描结果失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
          }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
          // 可选：处理可能的结果点
        }
      });

      barcodeView.resume();
    } catch (Exception e) {
      Toast.makeText(this, "启动扫描失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
      isScanning = false;
      finishButton.setText("开始扫描");
    }
  }

  private void stopScanning() {
    isScanning = false;
    finishButton.setText("开始扫描");
    barcodeView.pause();
  }

  private boolean isDuplicate(String content) {
    for (ScanResult result : scanResults) {
      if (result.getContent().equals(content)) {
        runOnUiThread(() -> Toast.makeText(this, "重复的二维码: " + content, Toast.LENGTH_SHORT).show());
        return true;
      }
    }
    return false;
  }

  private void saveScanResult(ScanResult scanResult) {
    new Thread(() -> {
      database.scanResultDao().insert(scanResult);
    }).start();
  }

  private void updateUI() {
    String countText = "已扫描: " + scanResults.size() + " 个";
    countTextView.setText(countText);
  }

  private void exportCurrentBatch() {
    if (scanResults.isEmpty()) {
      Toast.makeText(this, "没有扫描结果可导出", Toast.LENGTH_SHORT).show();
      return;
    }

    new AlertDialog.Builder(this)
        .setTitle("导出当前批次")
        .setMessage("是否导出当前批次的 " + scanResults.size() + " 条扫描记录？")
        .setPositiveButton("确定", (dialog, which) -> {
          // Android 10及以上版本使用应用内部存储，不需要存储权限
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            exportToExcel();
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
              exportToExcel();
            }
          }
        })
        .setNegativeButton("取消", null)
        .show();
  }

  private void exportToExcel() {
    Toast.makeText(this, "正在导出批次数据到Excel文件...", Toast.LENGTH_SHORT).show();

    new Thread(() -> {
      try {
        // 创建Excel文件
        String fileName = "批量扫描结果_" + currentBatchId + "_" +
            new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) +
            ".xlsx";

        File file;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
          // 使用应用内部存储，不需要额外权限
          File internalDir = new File(getExternalFilesDir(null), "exports");
          if (!internalDir.exists()) {
            boolean dirCreated = internalDir.mkdirs();
            if (!dirCreated) {
              throw new RuntimeException("无法创建导出目录");
            }
          }
          file = new File(internalDir, fileName);
        } else {
          // 使用外部存储
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
        }

        createExcelFile(file);

        final File finalFile = file;
        runOnUiThread(() -> {
          Toast.makeText(this, "Excel文件已保存到: " + finalFile.getAbsolutePath(),
              Toast.LENGTH_LONG).show();

          // 尝试分享文件
          shareExcelFile(finalFile);
        });

      } catch (Exception e) {
        android.util.Log.e("BatchScanActivity", "Excel导出失败", e);
        runOnUiThread(() -> Toast.makeText(this, "导出失败: " + e.getMessage(),
            Toast.LENGTH_LONG).show());
      }
    }).start();
  }

  private void createExcelFile(File file) throws IOException {
    FileOutputStream fileOut = null;
    Workbook workbook = null;

    try {
      android.util.Log.d("BatchScanActivity", "开始创建Excel文件: " + file.getAbsolutePath());

      workbook = new XSSFWorkbook();
      Sheet sheet = workbook.createSheet("批量扫描结果");

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
      for (int i = 0; i < scanResults.size(); i++) {
        ScanResult result = scanResults.get(i);
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

      // 设置列宽
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
      android.util.Log.w("BatchScanActivity", "解析发票信息失败: " + e.getMessage());
    }

    return result;
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
        intent.putExtra(Intent.EXTRA_SUBJECT, "批量扫描结果导出");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "分享Excel文件"));

      } catch (Exception providerException) {
        // FileProvider失败，尝试直接使用文件URI (仅适用于较旧的Android版本)
        try {
          Uri fileUri = Uri.fromFile(file);
          intent.putExtra(Intent.EXTRA_STREAM, fileUri);
          intent.putExtra(Intent.EXTRA_SUBJECT, "批量扫描结果导出");

          startActivity(Intent.createChooser(intent, "分享Excel文件"));

        } catch (Exception directUriException) {
          // 分享失败，显示文件路径
          Toast.makeText(this, "文件已保存到:\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
      }

    } catch (Exception e) {
      Toast.makeText(this, "分享文件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == CAMERA_PERMISSION_REQUEST) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        startScanning();
      } else {
        Toast.makeText(this, "需要摄像头权限才能扫描二维码", Toast.LENGTH_SHORT).show();
        finish();
      }
    } else if (requestCode == STORAGE_PERMISSION_REQUEST) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        exportToExcel();
      } else {
        Toast.makeText(this, "需要存储权限才能导出文件", Toast.LENGTH_SHORT).show();
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (barcodeView != null && isScanning) {
      barcodeView.resume();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (barcodeView != null) {
      barcodeView.pause();
    }
  }

  @Override
  public void onBackPressed() {
    if (scanResults.size() > 0) {
      Toast.makeText(this, "批量扫描完成，共扫描 " + scanResults.size() + " 个二维码",
          Toast.LENGTH_LONG).show();
    }
    super.onBackPressed();
  }
}
