package com.willli.smart_scan;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = { ScanResult.class }, version = 1, exportSchema = false)
public abstract class ScanDatabase extends RoomDatabase {

  private static volatile ScanDatabase INSTANCE;

  public abstract ScanResultDao scanResultDao();

  public static ScanDatabase getDatabase(final Context context) {
    if (INSTANCE == null) {
      synchronized (ScanDatabase.class) {
        if (INSTANCE == null) {
          INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
              ScanDatabase.class, "scan_database")
              .allowMainThreadQueries() // 注意：生产环境中建议使用异步操作
              .build();
        }
      }
    }
    return INSTANCE;
  }
}
