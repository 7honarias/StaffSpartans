package com.cucutain.staffspartans.Retrofit;

import com.cucutain.staffspartans.Model.FCMResponse;
import com.cucutain.staffspartans.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA0W2hrPU:APA91bF-gVtaiQKbO992yxVoZwjYqVMLabGQZizy4-p54-21cGmFLIX0jlkKcYMmr9jHjNVywRJc7Z-n9dQzK9RctJaS-97f70u6HZ3f7A1oi0PcJwEWW4RrYUIMli_QGc0dxWr3ru7j"

    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);

}