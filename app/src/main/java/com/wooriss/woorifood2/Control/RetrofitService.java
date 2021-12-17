package com.wooriss.woorifood2.Control;

import com.google.gson.JsonObject;
import com.wooriss.woorifood2.Model.PageListSikdang;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface RetrofitService {
    @GET("/v2/local/search/keyword.json")
    Call<PageListSikdang> getSikdangList(@Header("Authorization") String KakaoAK,
                                         @Query("query") String query,  // 질의어
                                         @Query("x") String x,
                                         @Query("y") String y,
                                         @Query("radius") int radius,   // 중심 좌표부터의 반경거리. 특정 지역을 중심으로 검색하려고 할 경우 중심좌표로 쓰일 x,y와 함께 사용
                                         //단위 meter, 0~20000 사이의 값
                                         @Query("page") int page,      // 페이지 결과 값 1~45 사이의 값 (기본값: 1)
//                          @Query("size") int size,    // 기본값 15
                                         @Query("sort") String sort); //결과 정렬 순서, distance 정렬을 원할 때는 기준 좌표로 쓰일 x, y와 함께 사용
    //distance 또는 accuracy (기본값: accuracy)

    @GET("/v2/local/search/address.json")
    Call<JsonObject> getAddress(@Header("Authorization") String KakaoAK,
                                @Query("query") String query,
                                @Query("page") int page,
                                @Query("size") int size);


}