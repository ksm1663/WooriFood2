package com.wooriss.woorifood2.Fragment;

import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.wooriss.woorifood2.Code;
import com.wooriss.woorifood2.Control.FoodLocation;
import com.wooriss.woorifood2.Model.Sikdang;
import com.wooriss.woorifood2.Model.User;
import com.wooriss.woorifood2.R;

import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {


    private Context context;
    private FirebaseUser f_user;
    private User user;


    private MapView mapView;
    private static NaverMap naverMap;

    private double branchX = 0;
    private double branchY = 0;


    private RecyclerView recyclerView;

    private int mapFlag;

    public MapFragment(int _mapFlag) {
        mapFlag = _mapFlag;
    }

    public static MapFragment newInstance(int _mapFlag, Bundle bundle) {
        MapFragment mapFragment = new MapFragment(_mapFlag);
        mapFragment.setArguments(bundle);
        return mapFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            f_user = getArguments().getParcelable("f_user");
            user = (User) getArguments().getSerializable("user");


        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (container != null)
            context = container.getContext();

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);

        init(savedInstanceState);
    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        if (setCameraLocation()) { // 카메라 위치 선정 지점으로 성공 배치 시
            Log.d(Code.LOG_TAG, "onMapReaddy in setCameraLocation is true");


            String cur_branchName = user.getBranch_name();

            if (mapFlag == Code.MapType.SEARCH_MAP) {
                if (SearchFragment.pageListSikdang != null) {
                    branchX = Double.valueOf(FoodLocation.tmpX);
                    branchY = Double.valueOf(FoodLocation.tmpY);
                    moveCamera(branchX, branchY);
                    setMarkerSikdangList(SearchFragment.pageListSikdang.body().getDocuments());
                }
            } else if (mapFlag == Code.MapType.REVIEWED_MAP) {
                if (MainListFragment.mainSikdangList != null) {
                    if (MainListFragment.branch_otherName.length() > 0)
                        cur_branchName = MainListFragment.branch_otherName;

                    setMarkerSikdangList(MainListFragment.mainSikdangList);
                }

            } else{}
            setMarkerBranch(cur_branchName);
            addListenerToMap();
        }
    }

    // 지점 마커 표시
    private void setMarkerBranch(String cur_branchName) {
        Marker marker = new Marker();
        marker.setPosition(new LatLng(branchY, branchX));
//        marker.setIcon(MarkerIcons.BLACK);
//        marker.setIconTintColor(Color.BLUE);
        marker.setIcon(OverlayImage.fromResource(R.drawable.ic_marker_home));
        marker.setWidth(130);
        marker.setHeight(130);
        marker.setZIndex(100);
        marker.setCaptionText(cur_branchName);
        marker.setMap(naverMap);
    }

    // 현재 리사이클러뷰에 있는 식당 리스트 지도에 마커 표시
    private void setMarkerSikdangList(List<Sikdang> sikdangList) {

        int current_doc_cnt = sikdangList.size();
        Log.d(Code.LOG_TAG, "need marking sikdang size() : " + current_doc_cnt);
        for (int i=0; i < current_doc_cnt; i++) {
            Sikdang sikdang = sikdangList.get(i);
            Marker marker = new Marker();
            marker.setPosition(new LatLng(Double.valueOf(sikdang.getY()), Double.valueOf(sikdang.getX())));
//            marker.setIcon(MarkerIcons.BLACK);

            if (sikdangList.get(i).getViewType() == Code.ViewType.DEFAULT_SIKDANG)
                marker.setIcon(OverlayImage.fromResource(R.drawable.ic_marker_green));
//                marker.setIconTintColor(Color.GREEN);
            else
                marker.setIcon(OverlayImage.fromResource(R.drawable.ic_marker_red));
//                marker.setIconTintColor(Color.RED);

            marker.setWidth(130);
            marker.setHeight(130);
            marker.setCaptionText(sikdang.getPlace_name());
            marker.setTag(i);
            addListenerToMarker(marker);

            marker.setMap(naverMap);
        }
    }

    // 맵 클릭하면 정보뷰 닫기
    private void addListenerToMap() {
        naverMap.setOnMapClickListener(new NaverMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
                if (recyclerView.getVisibility() == View.VISIBLE) {
                    recyclerView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.ani_slid_out_bottom));
                    recyclerView.setVisibility(View.GONE);
                }

            }
        });
    }

    // 마커 클릭하면 정보리사이클뷰 표시하는 이벤트
    private void addListenerToMarker(Marker marker) {
        marker.setOnClickListener(new Overlay.OnClickListener() {
            @Override
            public boolean onClick(@NonNull Overlay overlay) {
                Marker tmpMarker = (Marker)overlay;
                int markerPos = (int)tmpMarker.getTag();
                // 리뷰있는 아이템, 리뷰없는 식당(아이템) 인지에 따라서 리싸이클러뷰 세로 사이즈 수정
                if (recyclerView.getAdapter().getItemViewType(markerPos) == Code.ViewType.DEFAULT_SIKDANG)
                    setRecyclerViewSize(Code.ViewType.DEFAULT_SIKDANG);
                else
                    setRecyclerViewSize(Code.ViewType.REVIEWED_SIKDANG);

                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                llm.scrollToPositionWithOffset(markerPos,0);

                if (recyclerView.getVisibility()==View.GONE) {
                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.ani_slid_in_bottom));
                }
                return true;
            }
        });
    }


    public void setRecyclerViewSize (int code) {
        if (code == Code.ViewType.DEFAULT_SIKDANG)
        {
            recyclerView.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 85, getResources().getDisplayMetrics());
            recyclerView.requestLayout();

        } else {
            recyclerView.getLayoutParams().height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, getResources().getDisplayMetrics());
            recyclerView.requestLayout();
        }

    }

    // 지도의 카메라 위치를 사용자 소속 지점 기준으로 변경
    private boolean setCameraLocation() {
        if (FoodLocation.x == null)
            return false;

        branchX = Double.valueOf(FoodLocation.x);
        branchY = Double.valueOf(FoodLocation.y);
        moveCamera(branchX, branchY);
        return true;
    }


    public void moveCamera(double x, double y) {
        Log.d(Code.LOG_TAG, "moveCamera : x : " + x + ", y : " + y);
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(y, x));
        naverMap.moveCamera(cameraUpdate);
    }

    private void findViews(View view) {
        //네이버 지도
        mapView = view.findViewById(R.id.map_container);
        recyclerView = view.findViewById(R.id.sikdangItemInMap);
    }

    private void init(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(context) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }

            @Override
            public boolean canScrollHorizontally() {
                return false;
            }
        });

        if (mapFlag == Code.MapType.SEARCH_MAP) {
            recyclerView.setAdapter(SearchFragment.sikdangAdapter);
        } else {
            recyclerView.setAdapter(MainListFragment.reviewdSikdangAdapter);
        }
        recyclerView.setVisibility(View.GONE);

    }


}
