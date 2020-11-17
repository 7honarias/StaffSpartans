package com.cucutain.staffspartans;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cucutain.staffspartans.Common.Common;
import com.cucutain.staffspartans.Interface.IBarberServicesLoadListener;
import com.cucutain.staffspartans.Interface.IBottomSheetDialogOnDismissListener;
import com.cucutain.staffspartans.Interface.IOnShoppingItemSeleted;
import com.cucutain.staffspartans.Model.BarberServices;
import com.cucutain.staffspartans.Model.ShoppingItem;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;

public class DoneServicesActivity extends AppCompatActivity implements IBarberServicesLoadListener, IOnShoppingItemSeleted, IBottomSheetDialogOnDismissListener {


    List<ShoppingItem> shoppingItems = new ArrayList<>();



    @BindView(R.id.txt_customer_name)
    TextView txt_customer_name;

    @BindView(R.id.txt_customer_phone)
    TextView txt_customer_phone;

    @BindView(R.id.chip_group_services)
    ChipGroup chip_group_services;

    @BindView(R.id.chip_group_shopping)
    ChipGroup chip_group_shopping;

    @BindView(R.id.edt_services)
    AppCompatAutoCompleteTextView edt_services;




    TextView add_shopping;

    @BindView(R.id.btn_finish)
    Button btn_finish;



    AlertDialog dialog;
    IBarberServicesLoadListener iBarberServicesLoadListener;
    HashSet<BarberServices> serviceAdded = new HashSet<>();

    LayoutInflater inflater;
    Uri fileUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_done_services);

        add_shopping = findViewById(R.id.add_shopping);
        ButterKnife.bind(this);

        init();
        initView();


        setCustomerInformation();

        loadBarberServices();
    }

    private void initView() {

        btn_finish.setEnabled(true);



        getSupportActionBar().setTitle("Checkout");


        btn_finish.setOnClickListener(view -> {

            dialog.dismiss();

            TotalPriceFragment fragment = TotalPriceFragment.getInstance(DoneServicesActivity.this);
            Bundle bundle = new Bundle();
            bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(serviceAdded));
            bundle.putString(Common.SHOPPING_LIST, new Gson().toJson(shoppingItems));
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(),"Price");



        });

        add_shopping.setOnClickListener(view -> {
            ShoppingFragment shoppingFragment = ShoppingFragment.getInstance(DoneServicesActivity.this);
            shoppingFragment.show(getSupportFragmentManager(),"Shopping");
        });



    }

   /* private void uploadPicture(Uri fileUri) {
        if (fileUri != null)
        {
            dialog.show();
            String fileName = Common.getFileName(getContentResolver(),fileUri);
            String path = new StringBuilder("Custumer_Pictures/")
                    .append(fileName)
                    .toString();

            storageReference = FirebaseStorage.getInstance().getReference(path);

            UploadTask uploadTask = storageReference.putFile(fileUri);

            Task<Uri> task = uploadTask.continueWithTask(task1 -> {
                if (!task1.isSuccessful())
                    Toast.makeText(DoneServicesActivity.this, "failed", Toast.LENGTH_SHORT).show();

                    return storageReference.getDownloadUrl();

            }).addOnCompleteListener(task12 -> {
                if (task12.isSuccessful())
                {

                    String url = task12.getResult().toString()
                            .substring(0,
                                    task12.getResult().toString().indexOf("&token"));

                    Log.d("DOWNLOADABLE_LINK", url);

                    dialog.dismiss();

                    TotalPriceFragment fragment = TotalPriceFragment.getInstance(DoneServicesActivity.this);
                    Bundle bundle = new Bundle();
                    bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(serviceAdded));
                    bundle.putString(Common.SHOPPING_LIST, new Gson().toJson(shoppingItems));
                    bundle.putString(Common.IMAGE_DOWNLOADABLE_URL,url);
                    fragment.setArguments(bundle);
                    fragment.show(getSupportFragmentManager(),"Price");


                }
                else
                {
                    uploadPicture(fileUri);
                }
            }).addOnFailureListener(e -> {
                dialog.dismiss();
                Toast.makeText(DoneServicesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });



        }
        else
        {
            Toast.makeText(this, "imagen vacia", Toast.LENGTH_SHORT).show();
        }
    }*/




    private void init() {
        dialog = new SpotsDialog.Builder().setContext(this)
                .setCancelable(false)
                .build();

        iBarberServicesLoadListener = this;

        inflater = LayoutInflater.from(this);
    }



    private void loadBarberServices() {
        dialog.show();
        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selected_salon.getSalonId())
                .collection("Services")
                .get()
                .addOnFailureListener(e -> iBarberServicesLoadListener.onBarberServicesLoadFailed(e.getMessage())).addOnCompleteListener(task -> {
            if (task.isSuccessful())
            {
                List<BarberServices> barberServices = new ArrayList<>();
                for (DocumentSnapshot barberSnapShot:task.getResult())
                {
                    BarberServices services = barberSnapShot.toObject(BarberServices.class);
                    barberServices.add(services);
                }
                iBarberServicesLoadListener.onBarberServicesLoadSuccess(barberServices);

            }

        });



    }

    private void setCustomerInformation() {
        txt_customer_name.setText(Common.currentBookingInformation.getCustomeName());
        txt_customer_phone.setText(Common.currentBookingInformation.getCustomerPhone());

    }

    @Override
    public void onBarberServicesLoadSuccess(List<BarberServices> barberServicesList) {

        List<String> nameServices = new ArrayList<>();
        Collections.sort(barberServicesList, new Comparator<BarberServices>() {
            @Override
            public int compare(BarberServices barberServices, BarberServices t1) {
                return barberServices.getName().compareTo(t1.getName());
            }
        });
        for (BarberServices barberServices:barberServicesList)
            nameServices.add(barberServices.getName());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item,nameServices);
        edt_services.setThreshold(1);
        edt_services.setAdapter(adapter);
        edt_services.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                int index = nameServices.indexOf(edt_services.getText().toString().trim());

                if (!serviceAdded.contains(barberServicesList.get(index))) {
                    serviceAdded.add(barberServicesList.get(index));
                    Chip item = (Chip) inflater.inflate(R.layout.chip_item, null);
                    item.setText(edt_services.getText().toString());
                    item.setTag(i);
                    edt_services.setText("");

                    item.setOnCloseIconClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            chip_group_services.removeView(view);
                            serviceAdded.remove((int) item.getTag());
                        }
                    });
                    chip_group_services.addView(item);


                } else {
                    edt_services.setText("");
                }
            }
        });
        dialog.dismiss();

    }

    @Override
    public void onBarberServicesLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();

    }

    @Override
    public void onShoppingItemSelected(ShoppingItem shoppingItem) {

        shoppingItems.add(shoppingItem);

        Chip item = (Chip) inflater.inflate(R.layout.chip_item, null);
        item.setText(shoppingItem.getName());
        item.setTag(shoppingItems.indexOf(shoppingItem));
        edt_services.setText("");

        item.setOnCloseIconClickListener(view -> {
            chip_group_shopping.removeView(view);
            shoppingItems.remove((int) item.getTag());
        });
        chip_group_shopping.addView(item);





    }

    @Override
    public void onDismissBottomDialog(boolean fromButton) {
        if (fromButton)
            finish();
    }

}
