package com.cucutain.staffspartans.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.cucutain.staffspartans.Common.CustomLoginDialog;
import com.cucutain.staffspartans.Interface.IDialogClickListener;
import com.cucutain.staffspartans.Interface.IGetBarberListener;
import com.cucutain.staffspartans.Interface.IRecyclerItemSelectedListener;
import com.cucutain.staffspartans.Interface.IUserLoginRememberListener;
import com.cucutain.staffspartans.Model.Barber;
import com.cucutain.staffspartans.Model.Salon;
import com.cucutain.staffspartans.R;
import com.cucutain.staffspartans.StaffHomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MySalonAdapter extends RecyclerView.Adapter<MySalonAdapter.MyViewHolder> implements IDialogClickListener {

    Context context;
    List<Salon> salonList;
    List<CardView> cardViewList;

    IUserLoginRememberListener iUserLoginRememberListener;
    IGetBarberListener iGetBarberListener;


    public MySalonAdapter(Context context, List<Salon> salonList,IUserLoginRememberListener iUserLoginRememberListener, IGetBarberListener iGetBarberListener) {
        this.context = context;
        this.salonList = salonList;
        cardViewList = new ArrayList<>();
        this.iGetBarberListener = iGetBarberListener;
        this.iUserLoginRememberListener = iUserLoginRememberListener;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.layout_salon,parent,false);
        return new MyViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        holder.txt_salon_name.setText(salonList.get(position).getName());
        holder.txt_salon_address.setText(salonList.get(position).getAddress());
        if (!cardViewList.contains(holder.card_salon))
            cardViewList.add(holder.card_salon);

        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener(){

            @Override
            public void onItemSelectedListener(View view, int pos) {

                Common.selectedSalon = salonList.get(pos);
                Common.selected_salon = salonList.get(pos);
                showLoginDialog();


            }
        });

    }

    private void showLoginDialog() {

        CustomLoginDialog.getInstance()
                .showLoginDialog("Inicio de sesion",
                        "INICIAR",
                        "CANCELAR",
                        context,
                        this);
    }

    @Override
    public int getItemCount() {
        return salonList.size();
    }





    @Override
    public void onClickPositiveButton(DialogInterface dialogInterface, String username, String password) {
        AlertDialog loading = new SpotsDialog.Builder().setCancelable(false)
                .setContext(context).build();

        loading.show();

        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selectedSalon.getSalonId())
                .collection("Barbers")
                .whereEqualTo("username",username)
                .whereEqualTo("password",password)
                .limit(1)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        loading.dismiss();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful())
                        {
                            if(task.getResult().size()>0)
                            {
                                dialogInterface.dismiss();
                                loading.dismiss();

                                iUserLoginRememberListener.onUserLoginSuccess(username);

                                Barber barber = new Barber();
                                for (DocumentSnapshot barberSnapShot:task.getResult())
                                {
                                    barber = barberSnapShot.toObject(Barber.class);
                                    barber.setBarberId(barberSnapShot.getId());
                                }
                                iGetBarberListener.onGetBarberSuccess(barber);



                                Intent staffHome = new Intent(context, StaffHomeActivity.class);
                                staffHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                staffHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(staffHome);

                            }
                            else
                            {
                                loading.dismiss();
                                Toast.makeText(context, "Usuario o contraseña incorrecto", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });



    }

    @Override
    public void onClickNegativeButton(DialogInterface dialogInterface) {
        dialogInterface.dismiss();

    }




    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txt_salon_name, txt_salon_address;
        CardView card_salon;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(com.cucutain.staffspartans.Interface.IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_salon_address = itemView.findViewById(R.id.txt_salon_address);
            txt_salon_name = itemView.findViewById(R.id.txt_salon_name);
            card_salon = itemView.findViewById(R.id.card_salon);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            iRecyclerItemSelectedListener.onItemSelectedListener(view,getAdapterPosition());
        }
    }
}
