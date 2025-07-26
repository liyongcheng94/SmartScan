package com.willli.smart_scan;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.google.zxing.ResultPoint;
import com.google.zxing.BarcodeFormat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ScanActivity extends AppCompatActivity {
  private static final int CAMERA_PERMISSION_REQUEST = 100;
  private DecoratedBarcodeView barcodeView;
  private ScanDatabase database;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_scan);

    database = ScanDatabase.getDatabase(this);

    barcodeView = findViewById(R.id.barcode_scanner);

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

    // 检查摄像头权限
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this,
          new String[] { Manifest.permission.CAMERA }, CAMERA_PERMISSION_REQUEST);
    } else {
      startScanning();
    }
  }

  private void startScanning() {
    barcodeView.decodeContinuous(new BarcodeCallback() {
      @Override
      public void barcodeResult(BarcodeResult result) {
        if (result.getText() != null) {
          // 保存扫描结果
          saveScanResult(result);

          // 返回结果
          Intent intent = new Intent();
          intent.putExtra("scan_result", result.getText());
          intent.putExtra("scan_format", result.getBarcodeFormat().toString());
          setResult(RESULT_OK, intent);
          finish();
        }
      }

      @Override
      public void possibleResultPoints(List<ResultPoint> resultPoints) {
        // 可选：处理可能的结果点
      }
    });
  }

  private void saveScanResult(BarcodeResult result) {
    ScanResult scanResult = new ScanResult(
        result.getText(),
        result.getBarcodeFormat().toString(),
        System.currentTimeMillis(),
        "single_scan" // 单次扫描标识
    );

    new Thread(() -> {
      database.scanResultDao().insert(scanResult);
    }).start();
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
    if (barcodeView != null) {
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
}
