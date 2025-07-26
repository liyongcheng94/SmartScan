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

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

  public interface OnItemClickListener {
    void onItemClick(ScanResult scanResult);
  }

  private List<ScanResult> scanResults;
  private SimpleDateFormat dateFormat;
  private OnItemClickListener onItemClickListener;

  public HistoryAdapter(List<ScanResult> scanResults, OnItemClickListener listener) {
    this.scanResults = scanResults;
    this.onItemClickListener = listener;
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.item_history, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    ScanResult result = scanResults.get(position);

    holder.contentTextView.setText(result.getContent());
    holder.formatTextView.setText(result.getFormat());
    holder.timeTextView.setText(dateFormat.format(new Date(result.getTimestamp())));
    holder.batchTextView.setText(result.getBatchId());

    holder.itemView.setOnClickListener(v -> {
      if (onItemClickListener != null) {
        onItemClickListener.onItemClick(result);
      }
    });
  }

  @Override
  public int getItemCount() {
    return scanResults.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    TextView contentTextView;
    TextView formatTextView;
    TextView timeTextView;
    TextView batchTextView;

    public ViewHolder(@NonNull View itemView) {
      super(itemView);
      contentTextView = itemView.findViewById(R.id.content_text);
      formatTextView = itemView.findViewById(R.id.format_text);
      timeTextView = itemView.findViewById(R.id.time_text);
      batchTextView = itemView.findViewById(R.id.batch_text);
    }
  }
}
