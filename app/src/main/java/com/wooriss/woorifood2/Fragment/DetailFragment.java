package com.wooriss.woorifood2.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.skydoves.transformationlayout.TransformationCompat;
import com.skydoves.transformationlayout.TransformationLayout;
import com.wooriss.woorifood2.Code;
import com.wooriss.woorifood2.MainActivity;
import com.wooriss.woorifood2.Model.Review;
import com.wooriss.woorifood2.Model.ReviewAdapter;
import com.wooriss.woorifood2.Model.ReviewSet;
import com.wooriss.woorifood2.Model.Sikdang;
import com.wooriss.woorifood2.Model.SikdangAdapter;
import com.wooriss.woorifood2.Model.User;
import com.wooriss.woorifood2.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DetailFragment extends Fragment implements View.OnTouchListener {

    private Context context;
    private FirebaseUser f_user;
    private User user;
    private Sikdang sikdang;

    private ImageView imgTitle;


    private FloatingActionButton btnUploadReview;
    private TextView sikdangNameText;
    private TextView sikdangAddrText;
    private TextView sikdangPhoneText;
    private RatingBar sikdangTasteAvgRating;

    private SeekBar seekPriceAvg;
    private SeekBar seekLuxuryAvg;

    private BarChart sikdangComplexChart;

    private RecyclerView reviewListInDetailView;
    private List<ReviewSet> reviewSetList;
    private ReviewAdapter reviewAdapter;


    public DetailFragment() {
    }

    public static DetailFragment newInstance(Bundle bundle) {
        DetailFragment detailFragment = new DetailFragment();
        detailFragment.setArguments(bundle);
        return detailFragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
        }

        if (getArguments() != null) {
            f_user = getArguments().getParcelable("f_user");
            user = (User) getArguments().getSerializable("user");
            sikdang = (Sikdang) getArguments().getSerializable("sikdang");


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container != null)
            context = container.getContext();
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startPostponedEnterTransition();

        findViews(view);
        init();
        setListener();

        downloadReviews();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return true;
    }

    private void findViews(View v) {
        imgTitle = v.findViewById(R.id.imgSikdangTitle);
        btnUploadReview = v.findViewById(R.id.btnUploadReview);
        sikdangNameText = v.findViewById(R.id.textSikdangName);
        sikdangAddrText = v.findViewById(R.id.textSikdangAddr);
        sikdangPhoneText = v.findViewById(R.id.textSikdangPhone);
        sikdangTasteAvgRating = v.findViewById(R.id.ratingSikdangTasteAvg);

        seekPriceAvg = v.findViewById(R.id.seekPrice);
        seekLuxuryAvg = v.findViewById(R.id.seekLuxury);


        sikdangComplexChart = v.findViewById(R.id.sikdangComplexChart);


        reviewListInDetailView = v.findViewById(R.id.reviewListInDetailView);
        reviewSetList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewSetList, getContext());

        //아래구분선 세팅
        reviewListInDetailView.addItemDecoration(new DividerItemDecoration(getActivity().getApplicationContext(), DividerItemDecoration.VERTICAL));

        reviewListInDetailView.setAdapter(reviewAdapter);
        reviewListInDetailView.setLayoutManager(new LinearLayoutManager(context));

    }

    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imgTitle.setTransitionName(sikdang.getId());
        }

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(55));

        if (sikdang.getTitleImageUri() != null)
            Glide.with(context)
                    .load(sikdang.getTitleImageUri())
                    .apply(requestOptions)
//                                                .circleCrop()
                    .into(imgTitle);
        else
            imgTitle.setImageResource(R.drawable.ic_logo);


    }

    private void setListener() {
        addListenerToPhoneText();
        addListenerToReviewUploadBtn();
    }

    private void setSikdangInfo() {

        sikdangNameText.setText(sikdang.getPlace_name());
        sikdangAddrText.setText(sikdang.getAddress_name());
        sikdangPhoneText.setText(sikdang.getPhone());
        sikdangTasteAvgRating.setRating((float) sikdang.getAvgTaste());

        seekPriceAvg.setProgress((int) (sikdang.getAvgPrice() * 10));
        seekPriceAvg.setOnTouchListener(this);
        seekLuxuryAvg.setProgress((int) (sikdang.getAvgLuxury() * 10));
        seekLuxuryAvg.setOnTouchListener(this);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(55));

        if (sikdang.getTitleImageUri() != null)
            Glide.with(context)
                    .load(sikdang.getTitleImageUri())
                    .apply(requestOptions)
                    .into(imgTitle);

        else
            imgTitle.setImageResource(R.drawable.ic_logo);

        List<BarEntry> barEntryList = new ArrayList<>();

        barEntryList.add(new BarEntry(1f, 0));
        barEntryList.add(new BarEntry(1f, sikdang.getAvgFirstComplex() * 10));
        barEntryList.add(new BarEntry(2f, sikdang.getAvgSecondComplex() * 10));
        barEntryList.add(new BarEntry(3f, sikdang.getAvgThirdComplex() * 10));
        barEntryList.add(new BarEntry(3f, 30));


        BarDataSet barDataSet = new BarDataSet(barEntryList, "");
        barDataSet.setColors(Color.TRANSPARENT, Color.parseColor("#f1c40f"),
                Color.parseColor("#e74c3c"), Color.parseColor("#3498db"), Color.TRANSPARENT);
//        barDataSet.setBarBorderWidth(0.1f);
//        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.4f);

        sikdangComplexChart.notifyDataSetChanged();
        sikdangComplexChart.invalidate();
        barData.setDrawValues(true);


        sikdangComplexChart.getDescription().setEnabled(false); // 차트 옆에 표기되는 desc
        sikdangComplexChart.setMaxVisibleValueCount(5); // 최대 보이는 그래프 개수
        sikdangComplexChart.setPinchZoom(false); // 두손가락으로 줌인, 줌아웃
        sikdangComplexChart.setDrawBarShadow(false); // 그래프 그림자
        sikdangComplexChart.setDrawGridBackground(true); // 격자구조
//        sikdangComplexChart.setEnabled(false);
        sikdangComplexChart.animateY(1000);
        sikdangComplexChart.getLegend().setEnabled(false);
        sikdangComplexChart.setTouchEnabled(false);

        sikdangComplexChart.getAxisLeft().setLabelCount(4, false);
        sikdangComplexChart.getAxisLeft().mAxisMaximum = 30;
        sikdangComplexChart.getAxisLeft().setGranularity(0.1f); // 1 단위마다 선 그리기
        sikdangComplexChart.getAxisLeft().setGranularityEnabled(true);
        sikdangComplexChart.getAxisLeft().setTextSize(14);
        sikdangComplexChart.getAxisLeft().setTextColor(Color.parseColor("#ffffff"));

        sikdangComplexChart.getAxisLeft().setDrawLabels(true);
        sikdangComplexChart.getAxisLeft().setValueFormatter(new MyValueFormatter(0));

        sikdangComplexChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        sikdangComplexChart.getXAxis().setGranularity(1f); // 간격
        sikdangComplexChart.getXAxis().setDrawGridLines(false); // 격자
        sikdangComplexChart.getXAxis().setDrawAxisLine(true); // 축 그림
        sikdangComplexChart.getXAxis().setDrawLabels(true); // 라벨
        sikdangComplexChart.getXAxis().setTextSize(12f);
        sikdangComplexChart.getXAxis().setTextColor(Color.parseColor("#ffffff"));

        sikdangComplexChart.getXAxis().setValueFormatter(new ValueFormatter() {
            private String[] gubun = {"1차", "2차", "3차", "", ""};

            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return gubun[(int) value - 1];
            }
        });

        sikdangComplexChart.getAxisRight().setEnabled(false);


        sikdangComplexChart.setFitBars(true);
        sikdangComplexChart.setDrawGridBackground(false);

        sikdangComplexChart.setData(barData);
        sikdangComplexChart.animateY(1000);


    }

    // 리뷰 등록 버튼 눌렀을 떄
    private void addListenerToReviewUploadBtn() {
        btnUploadReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) view.getContext()).transferToReview(sikdang);
            }
        });
    }

    // 전화번호 눌렀을 때
    private void addListenerToPhoneText() {
        sikdangPhoneText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = sikdangPhoneText.getText().toString().replaceAll("-", "");
                Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                startActivity(call);
            }
        });
    }


    //선택된 식당 리뷰들 가져와서 세팅
    private void downloadReviews() {
        // sikdangs 에서 해당 식당코드로 리뷰정보 가져오기
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("sikdangs").document(sikdang.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // 식당의 종합정보 세팅
                        sikdang = document.toObject(Sikdang.class);
                        setSikdangInfo();
                        sikdangComplexChart.notifyDataSetChanged();
                        sikdangComplexChart.invalidate();
                        imgTitle.invalidate();


                        document.getReference().collection("reviews").orderBy("timestamp", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    // 식당의 리뷰들 가져오기
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Review review = document.toObject(Review.class);

                                        ReviewSet reviewSet = new ReviewSet();
                                        reviewSet.images = new ArrayList<>();
                                        reviewSet.user = new User();
                                        reviewSet.review = review;
                                        reviewSetList.add(reviewSet);

                                        // 해당 리뷰의 등록자 정보 가져와서 저장
                                        setReviewerInfo (reviewSetList.size()-1, review.getReviewerUid());

                                        // 해당 리뷰의 사진 가져오기
                                        downloadImages(reviewSet.images, document.getId());
                                    }

                                    reviewAdapter.notifyDataSetChanged();

                                } else {
                                    Log.d(Code.LOG_TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
                    } else {
                        Log.d(Code.LOG_TAG, "No such document");
                    }
                } else {
                    Log.d(Code.LOG_TAG, "get failed with ", task.getException());
                }
            }
        });
    }


    // 리뷰 등록한 사람 정보 가져와서 세팅
    private void setReviewerInfo(int pos, String uid) {
        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.d(Code.LOG_TAG, "Error getting data", task.getException());
                } else {
                    reviewSetList.get(pos).user = task.getResult().getValue(User.class);
                    reviewAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    // 리뷰 이미지 가져오기
    private void downloadImages(List<Uri> images, String id) {
        // Get a default Storage bucket
        FirebaseStorage storageRef = FirebaseStorage.getInstance();
        StorageReference listRef = storageRef.getReference().child("reviewImages/" + id);

        listRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        Log.d("plz", "onSuccess in downloadImage");
                        for (StorageReference item : listResult.getItems()) {
                            // reference 의 아이템(이미지) url 받아오기
                            item.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        images.add(task.getResult());
                                        ((ReviewAdapter) reviewListInDetailView.getAdapter()).notifyDetailImageViewAdaper();
                                        reviewAdapter.notifyDataSetChanged();
                                    } else {
                                        // url 가져오지 못함
                                    }
                                }
                            });
                        }
                    } // end onSuccess
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //
            }
        });
    }


    // inner class for chart
    public class MyValueFormatter extends ValueFormatter {

        private float cutoff;
        private DecimalFormat format;
        String strs[] = {"-", "여유", "보통", "혼잡"};

        public MyValueFormatter(float cutoff) {
            this.cutoff = cutoff;
            this.format = new DecimalFormat("###,###,###,##0");
        }

        @Override
        public String getFormattedValue(float value) {
            if (value < cutoff) {
                return "-";
            }
//            Log.d("plz", "format.format(value) : " + format.format(value));
            return strs[(Integer.parseInt(format.format(value)) / 10)];//format.format(value);
        }
    }
}
