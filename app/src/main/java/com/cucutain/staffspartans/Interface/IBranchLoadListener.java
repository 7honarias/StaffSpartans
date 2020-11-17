package com.cucutain.staffspartans.Interface;

import com.cucutain.staffspartans.Model.Salon;

import java.util.List;

public interface IBranchLoadListener {
    void onBranchLoadSuccess(List<Salon> salonList);
    void onBranchLoadFailed(String message);
}
