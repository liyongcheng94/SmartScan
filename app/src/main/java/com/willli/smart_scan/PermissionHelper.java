package com.willli.smart_scan;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

  public static final int CAMERA_PERMISSION_REQUEST = 100;
  public static final int STORAGE_PERMISSION_REQUEST = 200;

  /**
   * 检查摄像头权限
   */
  public static boolean hasCameraPermission(Activity activity) {
    return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
  }

  /**
   * 请求摄像头权限
   */
  public static void requestCameraPermission(Activity activity) {
    ActivityCompat.requestPermissions(activity,
        new String[] { Manifest.permission.CAMERA },
        CAMERA_PERMISSION_REQUEST);
  }

  /**
   * 检查存储权限（根据Android版本）
   */
  public static boolean hasStoragePermission(Activity activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      // Android 10+ 不需要存储权限，使用SAF
      return true;
    } else {
      return ContextCompat.checkSelfPermission(activity,
          Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
  }

  /**
   * 请求存储权限
   */
  public static void requestStoragePermission(Activity activity) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
      ActivityCompat.requestPermissions(activity,
          new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
          STORAGE_PERMISSION_REQUEST);
    }
  }

  /**
   * 检查是否应该显示权限说明
   */
  public static boolean shouldShowRequestPermissionRationale(Activity activity, String permission) {
    return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
  }
}
