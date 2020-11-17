package com.cucutain.staffspartans.Retrofit;

import com.cucutain.staffspartans.Model.FCMResponse;
import com.cucutain.staffspartans.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

//put firebase key
public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=keyFirebase"

    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);

}
