package com.wooriss.woorifood2.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.skydoves.transformationlayout.TransformationCompat;
import com.wooriss.woorifood2.Code;
import com.wooriss.woorifood2.Control.FoodLocation;
import com.wooriss.woorifood2.Dialog.SearchBranchDialog;
import com.wooriss.woorifood2.MainActivity;
import com.wooriss.woorifood2.Model.Sikdang;
import com.wooriss.woorifood2.Model.SikdangAdapter;
import com.wooriss.woorifood2.Model.User;
import com.wooriss.woorifood2.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainListFragment extends Fragment {
    private Context context;
    private FirebaseUser f_user;
    private User user;

    private FirebaseDatabase mDatabase;
    private FirebaseFirestore fireStore;

    private FloatingActionButton btnMap;
    private EditText textBranch;
    private SwipeRefreshLayout swipeMainlist;
    private ImageView nonText;

    private RecyclerView mainListRecyclerView;
    public static SikdangAdapter reviewdSikdangAdapter;

    private FragmentManager fManager;
    private MapFragment reviewedMapFragment;

    public static String branch_otherName;
    private String branch_addr = "";

    public static List<Sikdang> mainSikdangList;


    public MainListFragment() { }

    public static MainListFragment newInstance (Bundle bundle) {
        MainListFragment mainListFragment = new MainListFragment();
        mainListFragment.setArguments(bundle);

        return mainListFragment;
    }

    private static int firstFlag = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            f_user = getArguments().getParcelable("f_user");
            user = (User) getArguments().getSerializable("user");


            reviewdSikdangAdapter = new SikdangAdapter(null);
            MainActivity.foodLocation.getBranchPosition(user.getBranch_addr(), Code.NEED_SET_MAIN_LIST, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(container!=null)
            context = container.getContext();
        return inflater.inflate(R.layout.fragment_mainlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);
        init();
        setListener();
    }

    private void findViews(View v) {
        btnMap = v.findViewById(R.id.btnMap);
        textBranch = v.findViewById(R.id.textBranch);

        nonText = v.findViewById(R.id.non_item);
        swipeMainlist = v.findViewById(R.id.swipeMainlist);
        mainListRecyclerView = v.findViewById(R.id.mainList);


    }

    private void init() {
        mDatabase = FirebaseDatabase.getInstance();
        fireStore = FirebaseFirestore.getInstance();

        mainListRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        //GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2);
        //mainListRecyclerView.setLayoutManager(gridLayoutManager);
        mainListRecyclerView.setAdapter(reviewdSikdangAdapter);


        fManager = getParentFragmentManager();
        textBranch.setHint(user.getBranch_name());

        branch_otherName= "";
    }


    private void setListener() {
        addListenerToMapBtn();
        addListenerToBranchTextBtn();
        addListenerToSwipeMainlist();

    }

    // 스와이프 실행 시
    private void addListenerToSwipeMainlist() {
        swipeMainlist.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // swipe 시 진행할 동작
                callSetMainList();
                // 업데이트가 끝났음을 알림
                swipeMainlist.setRefreshing(false);
            }
        });
    }

    // 지점 텍스트박스 눌렀을 때 버튼
    private void addListenerToBranchTextBtn() {
        textBranch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SearchBranchDialog(context, new SearchBranchDialog.ICustomDialogEventListener() {
                    @Override
                    public void customDialogEvent(HashMap<String, String> _branch_info) {
                        // Do something with the value here, e.g. set a variable in the calling activity
                        if (_branch_info != null) {
                            branch_otherName = _branch_info.get("branch_name");
                            textBranch.setText(branch_otherName);
                            branch_addr = _branch_info.get("branch_addr");
                            if (branch_addr.length()>0) {
                                MainActivity.foodLocation.getBranchPosition(branch_addr, Code.NEED_SET_MAIN_LIST, MainListFragment.this);
                            }
                        }
                    }
                });
            }
        });
    }

    // 지도 버튼 눌렀을 때 이벤트
    private void addListenerToMapBtn() {
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(Code.LOG_TAG, "getBranchName = " + user.getBranch_name());


                reviewedMapFragment = MapFragment.newInstance(Code.MapType.REVIEWED_MAP, getArguments());

                if (!reviewedMapFragment.isVisible()) {
                    fManager.beginTransaction().replace(R.id.main_container, reviewedMapFragment).addToBackStack(null).commit();
                }

            }
        });
    }

    public void callSetMainList() {
        Log.d(Code.LOG_TAG, "get ready!!!!");
        String x = FoodLocation.x;
        String y = FoodLocation.y;

        List<Sikdang> reviewedSikdangList = new ArrayList<>();
        fireStore.collection("sikdangs").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot result = task.getResult();
                List<DocumentSnapshot> documents = result.getDocuments();
                Log.d(Code.LOG_TAG, "result : " + result.size());
                for (DocumentSnapshot doc: documents) {
                    Sikdang sikdang = doc.toObject(Sikdang.class);
                    int distance = distance(Double.valueOf(y), Double.valueOf(x), Double.valueOf(sikdang.getY()), Double.valueOf(sikdang.getX()));
                    if ( distance <= Code.NEAR_KILOMETER) {
                        sikdang.setDistance(Integer.toString(distance));

                        reviewedSikdangList.add(sikdang);
                        // 식당 대표 이미지 셋팅
                        Log.d(Code.LOG_TAG, "sikdang.getTitleImageUri() : " + sikdang.getTitleImageUri());
                        if (sikdang.getTitleImageUri() == null) {
                            if (sikdang.getTitleImage() != null)
                                getImgSikdangTitle(reviewedSikdangList.get(reviewedSikdangList.size() - 1));
                        }
                    }
                }
                //**
                setReviewedSikdangList(reviewedSikdangList);
            }
        });
    }


    // 저장된 uri 없으면 식당 대표 이미지 세팅
    private void getImgSikdangTitle(Sikdang sikdang) {
        FirebaseStorage storageRef = FirebaseStorage.getInstance();
        StorageReference listRef = storageRef.getReference("reviewImages").child(sikdang.getTitleImage());

        listRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        Log.d(Code.LOG_TAG, "onSuccess in downloadImage : " + listResult.getItems().size());
                        if (listResult.getItems().size() > 0) {
                            listResult.getItems().get(0).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        sikdang.setTitleImageUri(task.getResult().toString());
                                        reviewdSikdangAdapter.notifyDataSetChanged();
                                    }else {
                                    }
                                }
                            });
                        }
                    } // end onSuccess
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(Code.LOG_TAG, e.getMessage());
                //
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setReviewedSikdangList(List<Sikdang> results) {
        Log.d(Code.LOG_TAG, "setReviewedSikdangList : " + results.size());
        if (results.size() > 0)
            setNullResult (false);
        else
            setNullResult (true);


        mainSikdangList = results;

        Collections.sort(mainSikdangList);
        reviewdSikdangAdapter.updateData(mainSikdangList);
        reviewdSikdangAdapter.notifyDataSetChanged();
    }


    private void setNullResult (boolean flag) {
        if (flag == true) {
            nonText.setVisibility(View.VISIBLE);
        }
        else
            nonText.setVisibility(View.GONE);
    }



    /**
     * 두 지점간의 거리 계산
     *
     * @param lat1 지점 1 위도
     * @param lon1 지점 1 경도
     * @param lat2 지점 2 위도
     * @param lon2 지점 2 경도
     * @return
     */
    private int distance(double lat1, double lon1, double lat2, double lon2) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        dist = dist * 1609.344;

        return (int)dist;
    }

    // This function converts decimal degrees to radians
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

}
