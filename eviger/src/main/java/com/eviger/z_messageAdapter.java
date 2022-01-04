package com.eviger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class z_messageAdapter extends RecyclerView.Adapter<z_messageAdapter.ViewHolder> {

    interface OnStateClickListener {
        void onStateClick(z_message message, int position);
    }

    private final z_messageAdapter.OnStateClickListener onClickListener;

    private final LayoutInflater inflater;
    private final ArrayList<z_message> messages;

    z_messageAdapter(z_messageAdapter.OnStateClickListener onClickListener, Context context, ArrayList<z_message> messages) {
        this.onClickListener = onClickListener;
        this.messages = messages;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public z_messageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(viewType == 0 ? R.layout.z_message_out : R.layout.z_message_in, parent, false);
        return new z_messageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull z_messageAdapter.ViewHolder holder, int position) {
        z_message message = messages.get(position);
        holder.dateView.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(message.getDate() * 1000L)));
        holder.messageTextView.setText(message.getMessage());
        holder.itemView.setOnClickListener(v -> onClickListener.onStateClick(message, position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public int getItemViewType(int position) {
        z_message message = messages.get(position);
        return message.isOutgoing() ? 0 : 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView messageTextView, dateView;

        ViewHolder(View view) {
            super(view);
            messageTextView = view.findViewById(R.id.textMessage);
            dateView = view.findViewById(R.id.dateMessage);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData() {
        notifyDataSetChanged();
    }

}
