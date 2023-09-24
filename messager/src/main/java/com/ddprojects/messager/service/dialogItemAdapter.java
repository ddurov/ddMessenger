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

import java.util.List;

public class dialogItemAdapter extends RecyclerView.Adapter<dialogItemAdapter.ViewHolder> {
    public interface OnStateClickListener {
        void onStateClick(Dialog dialog, int position);
    }

    private final OnStateClickListener onClickListener;
    private final LayoutInflater inflater;
    private final List<Dialog> dialogs;

    public dialogItemAdapter(OnStateClickListener onClickListener, Context context, List<Dialog> dialogs) {
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
    public void onBindViewHolder(dialogItemAdapter.ViewHolder holder, int position) {
        Dialog dialog = (Dialog) dialogs.toArray()[position];
        holder.dialogUserName.setText(dialog.getMessageUserName());
        holder.dialogDate.setText(convertTimestampToHuman(dialog.getMessageDate(), "d MMM yyyy, HH:mm"));
        holder.dialogText.setText(dialog.getMessageText());
        holder.itemView.setOnClickListener(v -> onClickListener.onStateClick(dialog, position));
    }

    @Override
    public int getItemCount() {
        return dialogs.size();
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

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(dialogItemAdapter adapter) {
        adapter.notifyDataSetChanged();
    }
}
