package com.eviger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class z_dialogAdapter extends RecyclerView.Adapter<z_dialogAdapter.ViewHolder> {

    interface OnStateClickListener {
        void onStateClick(z_dialog dialog, int position);
    }

    private final OnStateClickListener onClickListener;

    private final LayoutInflater inflater;
    private final List<Object[]> dialogs;

    z_dialogAdapter(OnStateClickListener onClickListener, Context context, List<Object[]> dialogs) {
        this.onClickListener = onClickListener;
        this.dialogs = dialogs;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public z_dialogAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.z_dialog_button, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(z_dialogAdapter.ViewHolder holder, int position) {
        Object[] temp = (Object[]) dialogs.toArray()[position];
        z_dialog dialog = (z_dialog) temp[1];
        holder.nameButtonDialog.setText(dialog.getUsername());
        holder.dateButtonDialog.setText(dialog.getDate());
        holder.messageButtonDialog.setText(dialog.getMessage());
        holder.itemView.setOnClickListener(v -> onClickListener.onStateClick(dialog, position));
    }

    @Override
    public int getItemCount() {
        return dialogs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameButtonDialog, dateButtonDialog, messageButtonDialog;

        ViewHolder(View view) {
            super(view);
            nameButtonDialog = view.findViewById(R.id.nameButtonDialog);
            dateButtonDialog = view.findViewById(R.id.dateButtonDialog);
            messageButtonDialog = view.findViewById(R.id.messageButtonDialog);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData() {
        messagesPage.dialogsAdapter.notifyDataSetChanged();
    }
}
