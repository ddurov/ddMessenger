package com.ddprojects.messager.service;

import static com.ddprojects.messager.service.globals.cachedData;
import static com.ddprojects.messager.service.globals.convertTimestampToHuman;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.ddprojects.messager.R;
import com.ddprojects.messager.models.Message;
import com.ddprojects.messager.models.User;

public class messageItemAdapter extends RecyclerView.Adapter<messageItemAdapter.ViewHolder> {
    private final OnStateClickListener onClickListener;
    private final LayoutInflater inflater;
    private final observableHashMap<Integer, Message> messages;

    public messageItemAdapter(OnStateClickListener onClickListener, Context context, observableHashMap<Integer, Message> messages) {
        this.onClickListener = onClickListener;
        this.messages = messages;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public messageItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.special_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull messageItemAdapter.ViewHolder holder, int position) {
        int key = (int) messages.keySet().toArray()[position];
        Message message = messages.get(key);
        if (message != null) {
            ConstraintLayout.LayoutParams layoutParams =
                    (ConstraintLayout.LayoutParams) holder.body.getLayoutParams();
            if (message.getSenderAId() != ((User) cachedData.get("user")).getAId()) {
                layoutParams.horizontalBias = 0;
                layoutParams.setMarginStart(4);
                layoutParams.setMarginEnd(72);
            } else {
                layoutParams.horizontalBias = 1;
                layoutParams.setMarginStart(72);
                layoutParams.setMarginEnd(4);
            }
            holder.body.setLayoutParams(layoutParams);
            holder.messageText.setText(message.getText());
            holder.messageTime.setText(convertTimestampToHuman(message.getTime(), "HH:mm"));
            holder.itemView.setOnClickListener(v -> onClickListener.onStateClick(message, position));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData() {
        this.notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ConstraintLayout body;
        final TextView messageText, messageTime;

        ViewHolder(View view) {
            super(view);
            body = view.findViewById(R.id.body);
            messageText = view.findViewById(R.id.textMessage);
            messageTime = view.findViewById(R.id.timeMessage);
        }
    }

    public interface OnStateClickListener {
        void onStateClick(Message message, int position);
    }
}