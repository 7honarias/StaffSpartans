package com.cucutain.staffspartans;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cucutain.staffspartans.Adapter.MyShoppingItemAdapter;
import com.cucutain.staffspartans.Common.SpacesItemDecoration;
import com.cucutain.staffspartans.Interface.IOnShoppingItemSeleted;
import com.cucutain.staffspartans.Interface.IShoppingDataLoadListener;
import com.cucutain.staffspartans.Model.ShoppingItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class ShoppingFragment extends BottomSheetDialogFragment implements IShoppingDataLoadListener, IOnShoppingItemSeleted {

    IOnShoppingItemSeleted callBackToActivity;
    CollectionReference shoppingItemRef;
    IShoppingDataLoadListener iShoppingDataLoadListener;
    Unbinder unbinder;

    @BindView(R.id.chip_group)
    ChipGroup chipGroup;
    @BindView(R.id.chip_wax)
    Chip chip_wax;
    @BindView(R.id.chip_spray)
    Chip chip_spray;
    @BindView(R.id.chip_body_care)
    Chip chip_body_care;
    @BindView(R.id.chip_hair_care)
    Chip chip_hair_care;
    @BindView(R.id.recycler_items)
    RecyclerView recyclerView;

    @OnClick(R.id.chip_wax)
    void waxLoadClick()
    {
        setSelectedChip(chip_wax);
        loadShoppingItem("Gel");
    }
    @OnClick(R.id.chip_spray)
    void sprayLoadClick()
    {
        setSelectedChip(chip_spray);
        loadShoppingItem("Cera");
    }
    @OnClick(R.id.chip_hair_care)
    void hairCareLoadClick()
    {
        setSelectedChip(chip_hair_care);
        loadShoppingItem("Shampoo");
    }
    @OnClick(R.id.chip_body_care)
    void bodyCareLoadClick()
    {
        setSelectedChip(chip_body_care);
        loadShoppingItem("Cera");
    }

    public ShoppingFragment(IOnShoppingItemSeleted callBackToActivity) {
        this.callBackToActivity = callBackToActivity;
    }

    private static ShoppingFragment instance;


    public static ShoppingFragment getInstance(IOnShoppingItemSeleted iOnShoppingItemSeleted) {
        return instance==null?new ShoppingFragment(iOnShoppingItemSeleted):instance;
    }

    private void setSelectedChip(Chip chip) {
        for (int i=0; i <chipGroup.getChildCount();i++ )
        {
            Chip chipItem = (Chip)chipGroup.getChildAt(i);
            if (chipItem.getId() != chip.getId())
            {
                chipItem.setChipBackgroundColorResource(android.R.color.darker_gray);
                chipItem.setTextColor(getResources().getColor(android.R.color.white));
            }
            else
            {
                chipItem.setChipBackgroundColorResource(android.R.color.holo_orange_dark);
                chipItem.setTextColor(getResources().getColor(android.R.color.white));
            }
        }
    }
    private void loadShoppingItem(String itemMenu) {
        shoppingItemRef = FirebaseFirestore.getInstance()
                .collection("Shopping")
                .document(itemMenu)
                .collection("Items");

        shoppingItemRef.get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        iShoppingDataLoadListener.onShoppingDataLoadFailed(e.getMessage());

                    }
                }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                {
                    List<ShoppingItem> shoppingItems = new ArrayList<>();
                    for (DocumentSnapshot itemSnapShot:task.getResult())
                    {
                        ShoppingItem shoppingItem = itemSnapShot.toObject(ShoppingItem.class);
                        shoppingItem.setId(itemSnapShot.getId());
                        shoppingItems.add(shoppingItem);

                    }
                    iShoppingDataLoadListener.onShoppingDataLoadSuccess(shoppingItems);
                }

            }
        });


    }

    public ShoppingFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_shopping, container, false);

        unbinder = ButterKnife.bind(this,itemView);
        iShoppingDataLoadListener = this;



        initView();
        init();


        return itemView;
    }

    private void init() {
        loadShoppingItem("Wax");

    }

    private void initView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        recyclerView.addItemDecoration(new SpacesItemDecoration(8));
    }


    @Override
    public void onShoppingDataLoadSuccess(List<ShoppingItem> shoppingItemList) {
        MyShoppingItemAdapter adapter = new MyShoppingItemAdapter(getContext(),shoppingItemList, this);
        recyclerView.setAdapter(adapter);


    }

    @Override
    public void onShoppingDataLoadFailed(String message) {
        Toast.makeText(getContext(), ""+message, Toast.LENGTH_SHORT).show();


    }

    @Override
    public void onShoppingItemSelected(ShoppingItem shoppingItem) {

        callBackToActivity.onShoppingItemSelected(shoppingItem);

    }
}
