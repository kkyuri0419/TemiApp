package com.example.temixxdk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MsgAdapterRecyclerView extends RecyclerView.Adapter<MsgAdapterRecyclerView.ViewHolder> {
    ArrayList<MsgModelRecyclerView> msgModelRecyclerViews;
    Context context;

    public MsgAdapterRecyclerView(Context context, ArrayList<MsgModelRecyclerView> msgModelRecyclerViews){
        this.context = context;
        this.msgModelRecyclerViews = msgModelRecyclerViews;
    }

    @NonNull
    @Override
    public MsgAdapterRecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item_recyclerview,parent,false);
        return new MsgAdapterRecyclerView.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MsgAdapterRecyclerView.ViewHolder holder, int position) {
        holder.imageView.setImageResource(msgModelRecyclerViews.get(position).image);
        holder.mstatus.setText(msgModelRecyclerViews.get(position).msgStatus);
        holder.msg.setText(msgModelRecyclerViews.get(position).msgVal);
    }

    @Override
    public int getItemCount() {
        return msgModelRecyclerViews.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageView;
        public TextView mstatus;
        public TextView msg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            mstatus = itemView.findViewById(R.id.msgStatus);
            msg = itemView.findViewById(R.id.msgVal);
        }
    }
}
