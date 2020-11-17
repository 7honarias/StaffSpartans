package com.cucutain.staffspartans.Interface;

import com.cucutain.staffspartans.Model.BookingInformation;

import java.util.List;

public interface ITimeSlotLoadListener {
    void onTimeSlotLoadSuccess(List<BookingInformation> timeSlot);
    void onTimeSlotLoadFailed(String message);
    void onTimeSlotLoadEmpty();
}
