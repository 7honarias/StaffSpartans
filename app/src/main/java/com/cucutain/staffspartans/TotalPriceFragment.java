package com.cucutain.staffspartans;


import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cucutain.staffspartans.Adapter.MyConfrirmShoppingItemAdapter;
import com.cucutain.staffspartans.Common.Common;
import com.cucutain.staffspartans.Interface.IBottomSheetDialogOnDismissListener;
import com.cucutain.staffspartans.Model.BarberServices;
import com.cucutain.staffspartans.Model.FCMResponse;
import com.cucutain.staffspartans.Model.FCMSendData;
import com.cucutain.staffspartans.Model.MyToken;
import com.cucutain.staffspartans.Model.ShoppingItem;
import com.cucutain.staffspartans.Retrofit.IFCMService;
import com.cucutain.staffspartans.Retrofit.RetrofitClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class TotalPriceFragment extends BottomSheetDialogFragment{
    private Unbinder unbinder;

    private String image_url;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @BindView(R.id.chip_group_servicesf)
    ChipGroup chip_group_services;

    @BindView(R.id.recycler_view_shopping)
    RecyclerView recycler_view_shopping;

    @BindView(R.id.txt_salon_name)
    TextView txt_salon_name;

    @BindView(R.id.txt_customer_name)
    TextView txt_customer_name;

    @BindView(R.id.txt_customer_phone)
    TextView txt_customer_phone;

    @BindView(R.id.txt_barber_name)
    TextView txt_barber_name;

    @BindView(R.id.txt_time)
    TextView txt_time;

    @BindView(R.id.txt_total_price)
    TextView txt_total_price;

    @BindView(R.id.btn_confirm)
    Button btn_confirm;

    private HashSet<BarberServices> servicesAdded;
    private List<ShoppingItem> shoppingItemList;

    private IFCMService ifcmService;

    IBottomSheetDialogOnDismissListener iBottomSheetDialogOnDismissListener;

    private AlertDialog dialog;

    private static TotalPriceFragment instance;

    public TotalPriceFragment(IBottomSheetDialogOnDismissListener iBottomSheetDialogOnDismissListener) {
        this.iBottomSheetDialogOnDismissListener = iBottomSheetDialogOnDismissListener;
    }

    public static TotalPriceFragment getInstance(IBottomSheetDialogOnDismissListener iBottomSheetDialogOnDismissListener){
        return instance == null?new TotalPriceFragment(iBottomSheetDialogOnDismissListener):instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_total_price,container,false);

        unbinder = ButterKnife.bind(this,itemView);

        init();
        
        initView();

        getBundle(getArguments());

        setInformation();

        return itemView;
    }

    private void setInformation() {
        txt_salon_name.setText(Common.selected_salon.getName());
        txt_barber_name.setText(Common.currentBarber.getName());
        txt_time.setText(Common.convertTimeSlotToString(Common.currentBookingInformation.getSlot().intValue()));
        txt_customer_name.setText(Common.currentBookingInformation.getCustomeName());
        txt_customer_phone.setText(Common.currentBookingInformation.getCustomerPhone());

        if (servicesAdded.size()>0 )
        {
            int i = 0;

            for (BarberServices services:servicesAdded)
            {
                Chip chip = (Chip)getLayoutInflater().inflate(R.layout.chip_item,null);
                chip.setText(services.getName());
                chip.setTag(i);
                chip.setOnCloseIconClickListener(view -> {
                    servicesAdded.remove((int)view.getTag());
                    chip_group_services.removeView(view);

                    calculatePrice();
                });
                chip_group_services.addView(chip);

                i++;
            }
        }
        if(shoppingItemList.size()>0)
        {
            MyConfrirmShoppingItemAdapter adapter = new MyConfrirmShoppingItemAdapter(getContext(),shoppingItemList);
            recycler_view_shopping.setAdapter(adapter);
        }
        calculatePrice();



    }

    private double calculatePrice() {
        double price = Common.DEFAULT_PRICE;
        for (BarberServices services:servicesAdded)
            price+=services.getPrice();
        for (ShoppingItem shoppingItem:shoppingItemList)
            price+=shoppingItem.getPrice();

        txt_total_price.setText(new StringBuilder(Common.MONEY_SION)
        .append(price));



        return price;


    }

    private void getBundle(Bundle arguments) {
        this.servicesAdded = new Gson()
                .fromJson(arguments.getString(Common.SERVICES_ADDED),
                        new TypeToken<HashSet<BarberServices>>(){}.getType());
        this.shoppingItemList = new Gson()
                .fromJson(arguments.getString(Common.SHOPPING_LIST),
                        new TypeToken<List<ShoppingItem>>(){}.getType());

        image_url = arguments.getString(Common.IMAGE_DOWNLOADABLE_URL);
    }

    private void initView()
    {
        recycler_view_shopping.setHasFixedSize(true);
        recycler_view_shopping.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,false));

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();



                DocumentReference bookingSet = FirebaseFirestore.getInstance()
                        .collection("AllSalon")
                        .document(Common.state_name)
                        .collection("Branch")
                        .document(Common.selected_salon.getSalonId())
                        .collection("Barbers")
                        .document(Common.currentBarber.getBarberId())
                        .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                        .document(Common.currentBookingInformation.getBookingId());

                bookingSet
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful())
                                {
                                    if (task.getResult().exists())
                                    {
                                        Map<String,Object> dataUpdate = new HashMap<>();
                                        dataUpdate.put("done",true);
                                        bookingSet.update(dataUpdate)
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        dialog.dismiss();
                                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())
                                                {
                                                    //createInvoice();
                                                    sendNotificationUpdateToUser(Common.currentBookingInformation.getCustomerPhone());

                                                }
                                            }
                                        });

                                    }
                                }

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }


    private void createInvoice() {
        /*CollectionReference invoiceRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Invoices");

        Invoice invoice = new Invoice();
        invoice.setBarberId(Common.currentBarber.getBarberId());
        invoice.setBarberName(Common.currentBarber.getName());
        invoice.setSalonName(Common.currentSalon.getName());
        invoice.setSalonAddress(Common.currentSalon.getAddress());

        invoice.setCustomerName(Common.currentBookingInformation.getCustomeName());
        invoice.setCustomerPhone(Common.currentBookingInformation.getCustomerPhone());


        invoice.setImageUrl(image_url);

        invoice.setBarberServices(new ArrayList<BarberServices>(servicesAdded));
        invoice.setShoppingItemList(shoppingItemList);
        invoice.setFinalPrice(calculatePrice());

        invoiceRef.document()
                .set(invoice)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            sendNotificationUpdateToUser(Common.currentBookingInformation.getCustomerPhone());
                        }

                    }
                });

                */


    }

    private void sendNotificationUpdateToUser(String customerPhone) {
        FirebaseFirestore.getInstance()
                .collection("Tokens")
                .whereEqualTo("userPhone",Common.currentBookingInformation.getCustomerPhone())
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()&& task.getResult().size()>0)
                        {
                            MyToken myToken = new MyToken();
                            for (DocumentSnapshot tokenSnapShot:task.getResult())
                                myToken = tokenSnapShot.toObject(MyToken.class);

                            FCMSendData fcmSendData = new FCMSendData();
                            Map<String, String> dataSend = new HashMap<>();
                            dataSend.put("update_done", "true");
                            //Rating
                            dataSend.put(Common.RATING_STATE_KEY,Common.state_name);
                            dataSend.put(Common.RATING_SALON_ID,Common.selected_salon.getSalonId());
                            dataSend.put(Common.RATING_SALON_NAME,Common.selected_salon.getName());
                            dataSend.put(Common.RATING_BARBER_ID,Common.currentBarber.getBarberId());




                            fcmSendData.setTo(myToken.getToken());
                            fcmSendData.setData(dataSend);

                            compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<FCMResponse>() {
                                        @Override
                                        public void accept(FCMResponse fcmResponse) throws Exception {

                                            iBottomSheetDialogOnDismissListener.onDismissBottomDialog(true);
                                            dialog.dismiss();
                                            dismiss();
                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {

                                            Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }));
                        }

                    }
                });
    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(getContext())
                .setCancelable(false).build();
        ifcmService = RetrofitClient.getInstance().create(IFCMService.class);
    }


}