package com.example.temixxdk;


import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MainAdapterRecyclerView extends RecyclerView.Adapter<MainAdapterRecyclerView.ViewHolder> {
    ArrayList<MainModelRecyclerView> mainModelRecyclerViews;
    Context context;
    private TemiApplication mApp;
    boolean[] booleanlist = new boolean[TemiApplication.bSize];



    public MainAdapterRecyclerView(Context context, ArrayList<MainModelRecyclerView> mainModelRecyclerViews){
        this.context = context;
        this.mainModelRecyclerViews = mainModelRecyclerViews;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item_recyclerview,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        holder.imageView.setImageResource(mainModelRecyclerViews.get(position).getLangLogo());
        holder.textView.setText(mainModelRecyclerViews.get(position).getLocation().toUpperCase());


    }

    @Override
    public int getItemCount() {
        return mainModelRecyclerViews.size();
    }

    public void changeBackground(@NonNull ViewHolder holder, int position){
        holder.textView.setBackgroundResource(R.drawable.location_before);
    }

    public class ViewHoler{

    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textview);

            //클릭이벤트
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int i = getAdapterPosition();



                    Log.e(this.getClass().getName(), "Selected : "+i);
                    if (booleanlist[i]){
                        if (TemiApplication.showlocations.contains(textView.getText().toString())){
                            String tempWord = textView.getText().toString()+ ",    ";
                            TemiApplication.showlocations = TemiApplication.showlocations.replace(tempWord,"");
                        }
                        booleanlist[i] = false;
                        textView.setBackgroundResource(R.drawable.location_before);
                        textView.setTextColor(Color.parseColor("#88919C"));
                    }else{
                        TemiApplication.showlocations += textView.getText().toString()+ ",    ";
                        booleanlist[i] = true;
                        textView.setBackgroundResource(R.drawable.location_after);
                        textView.setTextColor(Color.parseColor("#193B68"));
                    }
                    TemiApplication.booleans = booleanlist;
//                    for (i =0 ; i < TemiApplication.booleans.length; i++){
//                        Log.e(this.getClass().getName(), "SENDING OF " +i+  " : " +String.valueOf(TemiApplication.booleans[i]));
//                    }
                    //리스너 객체의 메서드 호출
                    if (mListener != null){
                        mListener.onItemClick(v,i);
                    }

                }
            });
        }
    }
    public interface OnItemClickListener{
        void onItemClick(View v, int posision);
    }

    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }

}
