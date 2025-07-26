package com.willli.smart_scan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BatchScanAdapter extends RecyclerView.Adapter<BatchScanAdapter.ViewHolder> {
  private List<ScanResult> scanResults;
  private SimpleDateFormat dateFormat;

  public BatchScanAdapter(List<ScanResult> scanResults) {
    this.scanResults = scanResults;
    this.dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_scan_result, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    ScanResult result = scanResults.get(position);
    holder.contentTextView.setText(result.getContent());
    holder.formatTextView.setText(result.getFormat());
    holder.timeTextView.setText(dateFormat.format(new Date(result.getTimestamp())));
    holder.indexTextView.setText(String.valueOf(scanResults.size() - position));
  }

  @Override
  public int getItemCount() {
    return scanResults.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView contentTextView;
    TextView formatTextView;
    TextView timeTextView;
    TextView indexTextView;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      contentTextView = itemView.findViewById(R.id.content_text);
      formatTextView = itemView.findViewById(R.id.format_text);
      timeTextView = itemView.findViewById(R.id.time_text);
      indexTextView = itemView.findViewById(R.id.index_text);
    }
  }
}
