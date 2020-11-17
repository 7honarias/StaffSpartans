package com.cucutain.staffspartans.Interface;

import com.cucutain.staffspartans.Model.City;

import java.util.List;

public interface IOnAllStateLoadListener {

    void onAllStateLoadSuccess(List<City> cityList);
    void onAllStateLoadFailed(String message);
}
