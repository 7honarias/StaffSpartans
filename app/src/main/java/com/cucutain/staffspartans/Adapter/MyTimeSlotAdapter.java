package com.cucutain.staffspartans.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cucutain.staffspartans.Common.Common;
import com.cucutain.staffspartans.DoneServicesActivity;
import com.cucutain.staffspartans.Interface.IRecyclerItemSelectedListener;
import com.cucutain.staffspartans.Model.BookingInformation;
import com.cucutain.staffspartans.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.MyViewHolder> {

    Context context;
    List<BookingInformation> timeSlotList;
    List<CardView> cardViewList;

    public MyTimeSlotAdapter(Context context, List<BookingInformation> timeSlotList) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        cardViewList = new ArrayList<>();

    }

    public MyTimeSlotAdapter(Context context){
        this.context = context;
        this.timeSlotList = new ArrayList<>();
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_time_slot,viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int i) {
        holder.txt_time_slot.setText(new StringBuilder(Common.convertTimeSlotToString(i)).toString());
        if (timeSlotList.size() == 0)
        {
            holder.txt_time_description.setText("Disponible");
            holder.txt_time_description.setTextColor(context.getResources()
                    .getColor(android.R.color.black));
            holder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.black));
            holder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
            //holder.card_time_slot.setEnabled(true);
            holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                @Override
                public void onItemSelectedListener(View view, int position) {

                }
            });
        }
        else
        {
            for (BookingInformation slotValue:timeSlotList)
            {
                int slot = Integer.parseInt(slotValue.getSlot().toString());
                if (slot == i)
                {

                    if (!slotValue.isDone()) {
                        holder.card_time_slot.setTag(Common.DISABLE_TAG);
                        holder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));

                        holder.txt_time_description.setText("Full");
                        holder.txt_time_description.setTextColor(context.getResources().getColor(android.R.color.white));
                        holder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.white));

                        //holder.card_time_slot.setEnabled(false);
                        holder.setiRecyclerItemSelectedListener((view, position) -> FirebaseFirestore.getInstance()
                                .collection("AllSalon")
                                .document(Common.state_name)
                                .collection("Branch")
                                .document(Common.selected_salon.getSalonId())
                                .collection("Barbers")
                                .document(Common.currentBarber.getBarberId())
                                .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                                .document(slotValue.getSlot().toString())
                                .get()
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show())
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().exists()) {

                                            Common.currentBookingInformation = task.getResult().toObject(BookingInformation.class);
                                            Common.currentBookingInformation.setBookingId(task.getResult().getId());
                                            Intent intent = new Intent(context, DoneServicesActivity.class);
                                            context.startActivity(intent);
                                        }

                                    }


                                }));
                    }
                    else
                    {
                        holder.card_time_slot.setTag(Common.DISABLE_TAG);
                        holder.card_time_slot.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));

                        holder.txt_time_description.setText("Done");
                        holder.txt_time_description.setTextColor(context.getResources().getColor(android.R.color.white));
                        holder.txt_time_slot.setTextColor(context.getResources().getColor(android.R.color.white));

                        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelectedListener(View view, int position) {
                                //add here fix crash
                            }
                        });

                    }

                }
                else
                {
                    if (holder.getiRecyclerItemSelectedListener()==null )
                    {

                        holder.setiRecyclerItemSelectedListener((view, position) -> {

                        });
                    }


                }
            }
        }
        if (!cardViewList.contains(holder.card_time_slot))
            cardViewList.add(holder.card_time_slot);



    }







    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_time_slot, txt_time_description;
        CardView card_time_slot;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public IRecyclerItemSelectedListener getiRecyclerItemSelectedListener() {
            return iRecyclerItemSelectedListener;
        }

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txt_time_slot = itemView.findViewById(R.id.txt_time_slot);
            txt_time_description = itemView.findViewById(R.id.txt_time_slot_description);
            card_time_slot = itemView.findViewById(R.id.card_time_slot);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view,getAdapterPosition());
        }
    }
}
