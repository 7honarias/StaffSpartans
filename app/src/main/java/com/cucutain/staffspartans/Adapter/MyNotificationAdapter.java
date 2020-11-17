package com.cucutain.staffspartans.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.cucutain.staffspartans.Model.MyDiffCallBack;
import com.cucutain.staffspartans.Model.MyNotification;
import com.cucutain.staffspartans.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyNotificationAdapter extends RecyclerView.Adapter<MyNotificationAdapter.MyViewHolder> {

    Context context;
    List<MyNotification> myNotifications;

    public MyNotificationAdapter(Context context, List<MyNotification> myNotifications) {
        this.context = context;
        this.myNotifications = myNotifications;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_notification_item,parent,false);


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.txt_notification_content.setText(myNotifications.get(position).getContent());
        holder.txt_notification_content.setText(myNotifications.get(position).getTitle());



    }

    @Override
    public int getItemCount() {
        return myNotifications.size();
    }

    public void updateList(List<MyNotification> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MyDiffCallBack(this.myNotifications,newList));
        myNotifications.addAll(newList);
        diffResult.dispatchUpdatesTo(this);


    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txt_notification_title)
        TextView txt_notification_title;
        @BindView(R.id.txt_notification_content)
        TextView txt_notification_content;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this,itemView);
        }
    }
}
