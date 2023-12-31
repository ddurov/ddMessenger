package com.ddprojects.messager.service;

import static com.ddprojects.messager.service.globals.convertTimestampToHuman;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ddprojects.messager.R;
import com.ddprojects.messager.models.Dialog;

public class dialogItemAdapter extends RecyclerView.Adapter<dialogItemAdapter.ViewHolder> {
    private final OnStateClickListener onClickListener;
    private final LayoutInflater inflater;
    private final observableHashMap<Integer, Dialog> dialogs;

    public dialogItemAdapter(OnStateClickListener onClickListener, Context context, observableHashMap<Integer, Dialog> dialogs) {
        this.onClickListener = onClickListener;
        this.dialogs = dialogs;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public dialogItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.special_dialog, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull dialogItemAdapter.ViewHolder holder, int position) {
        int key = (int) dialogs.keySet().toArray()[position];
        Dialog dialog = dialogs.get(key);
        if (dialog != null) {
            holder.dialogUserName.setText(dialog.getPeerName());
            holder.dialogDate.setText(convertTimestampToHuman(dialog.getTime(), "d MMM yyyy, HH:mm"));
            holder.dialogText.setText(dialog.getText().replaceAll("\\n", " "));
            holder.itemView.setOnClickListener(v -> onClickListener.onStateClick(dialog, position));
        }
    }

    @Override
    public int getItemCount() {
        return dialogs.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData() {
        this.notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView dialogUserName, dialogDate, dialogText;

        ViewHolder(View view) {
            super(view);
            dialogUserName = view.findViewById(R.id.dialogUserName);
            dialogDate = view.findViewById(R.id.dialogDate);
            dialogText = view.findViewById(R.id.dialogText);
        }
    }

    public interface OnStateClickListener {
        void onStateClick(Dialog dialog, int position);
    }
}