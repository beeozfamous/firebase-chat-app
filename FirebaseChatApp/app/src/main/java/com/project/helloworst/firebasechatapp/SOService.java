package com.project.helloworst.firebasechatapp;

import retrofit2.Call;
import retrofit2.http.GET;
import rx.Observable;

public interface SOService {

    @GET("/api/json/get/4yeIqGREL")
    Observable<VersionResponse> getVersion();

}
