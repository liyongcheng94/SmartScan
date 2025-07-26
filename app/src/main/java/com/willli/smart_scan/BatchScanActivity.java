package com.willli.smart_scan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
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
  private DecoratedBarcodeView barcodeView;
  private ScanDatabase database;
  private List<ScanResult> scanResults;
  private BatchScanAdapter adapter;
  private RecyclerView recyclerView;
  private TextView countTextView;
  private Button finishButton;
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

      if (barcodeView == null || recyclerView == null || countTextView == null || finishButton == null) {
        throw new RuntimeException("无法找到必要的界面元素");
      }

      finishButton.setOnClickListener(v -> {
        if (isScanning) {
          stopScanning();
        } else {
          startScanning();
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
