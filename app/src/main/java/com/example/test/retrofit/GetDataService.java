package com.example.test.retrofit;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.POST;

public interface GetDataService {
    @POST("/test")
    Observable<Response<Void>> postAllDataFromThread3();
}
