package com.wooriss.woorifood2.Control;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;
import com.wooriss.woorifood2.Code;
import com.wooriss.woorifood2.Fragment.MainListFragment;
import com.wooriss.woorifood2.Fragment.SearchFragment;
import com.wooriss.woorifood2.Model.PageListSikdang;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodLocation {

    public static String x;
    public static String y;

    public static String tmpX;
    public static String tmpY;

    private Context context;

    private static String searchKey;

    private SearchFragment searchfFragment;


    private static int cur_paging = 1; // 페이징을 위한 현재 페이지 번호
    private int flag_otherBranch = 0;

    public FoodLocation(Context context) {
        this.context = context;
    }

//
//    public FoodLocation(MainListFragment _context, String branchAddr) {
//        mainListFragment = _context;
//        getBranchPosition (branchAddr);
//    }
//
//    public FoodLocation(MainListFragment _context, String branchAddr, int flag) {
//        flag_otherBranch = flag;
//        mainListFragment = _context;
//    }
//
    public void setSearchfFragment (SearchFragment searchfFragment) {
        this.searchfFragment = searchfFragment;
    }


    public void getBranchPosition(String _branchAddr, int actionType, Fragment fragment) {
        Log.d(Code.LOG_TAG, "넘어온 주소(getBranchPosition) : " + _branchAddr);
        RetrofitService addrService = RetrofitFactory.create();

        Call<JsonObject> addrCall = addrService.getAddress(Code.KAKAO_API_KEY, _branchAddr, 1, 1);

        addrCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject j = response.body().getAsJsonArray("documents").get(0).getAsJsonObject();
                x = j.get("x").getAsString();
                y = j.get("y").getAsString();
                Log.d(Code.LOG_TAG, "지점주소 불러오기 완료! x : " + x + "/ y : " + y);

                if (flag_otherBranch == 0) {
                    tmpX = x;
                    tmpY = y;
                }

                // 지점주소 받아와서 처리해야할 액션들별로 실행 !
                switch (actionType){
                    case Code.NEED_SET_MAIN_LIST : // 메인리스트에 리뷰식당을 세팅될 수 있도록 단계 진행
                        ((MainListFragment)fragment).callSetMainList();
                        break;

                    default:
                        break;

                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d(Code.LOG_TAG, "지점 주소 불러오기 실패 ");
            }
        });
    }

    // 구한 좌표로 검색된 식당 리스트 구하기
    public void getSikdangList(String _searchKey) {
        searchKey = _searchKey;
        cur_paging = 1;

        RetrofitService findSikdangService = RetrofitFactory.create();
        Call<PageListSikdang> sikdangCall = findSikdangService.getSikdangList(Code.KAKAO_API_KEY, searchKey,
                tmpX, tmpY, Code.NEAR_KILOMETER, 1, "distance");
        sikdangCall.enqueue(callback);
    }

    // 리사이클뷰 페이징
    public void callItemWithPaging(int total_cnt, int upAndDown) {
        RetrofitService findSikdangService = RetrofitFactory.create();
        Call<PageListSikdang> sikdangCall;

        if ((upAndDown == 0) && (cur_paging > 1)) { // 스르롤 위로 && 현재 페이지가 1보다 큰 경우
        }else if (upAndDown == 1) { // 스르콜 아래로
            // 47 / 15 => 3.xx => 3 => 마지막 페이지는 4 => 현재가 4페이지면 그만되어야
            if (((total_cnt / Code.MAX_LIST_SIZE) + 1) >= ++cur_paging) {
                sikdangCall = findSikdangService.getSikdangList(Code.KAKAO_API_KEY, searchKey,
                        x, y, Code.NEAR_KILOMETER, cur_paging, "distance");
                Log.d(Code.LOG_TAG, "스크롤 제일 밑! 다음 페이지 불러오기");

                sikdangCall.enqueue(callback);
            }
        }
    }


    // Retrofit 내 Response 익명클래스 바깥으로 뺀 버전
    retrofit2.Callback<PageListSikdang> callback = new Callback<PageListSikdang>() {
        @Override
        public void onResponse(Call<PageListSikdang> call, Response<PageListSikdang> response) {
            if (response.isSuccessful()) {
                SearchFragment.hadSearched = 1;
                Log.d(Code.LOG_TAG, response + "");
                Log.d(Code.LOG_TAG, "식당 결과 수: " + response.body().getMeta().total_count + "");
                if (searchfFragment != null) {
                    if (response.body().getMeta().total_count > 0)
                        searchfFragment.setNullResult(false);
                    else
                        searchfFragment.setNullResult(true);
                }
                /*if ((searchfFragment != null) &&(response.body().getMeta().total_count > 0))
                    searchfFragment.setNullResult(false);

                else
                    searchfFragment.setNullResult(true);*/
                searchfFragment.setSikdangList(response, cur_paging); // 메인으로 결과 보내서 리스트 세팅!

            }
        }
        @Override
        public void onFailure(Call<PageListSikdang> call, Throwable t) {
        }
    };

}
