package com.wooriss.woorifood2.Model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.skydoves.transformationlayout.TransformationLayout;
import com.wooriss.woorifood2.Code;
import com.wooriss.woorifood2.Fragment.MainListFragment;
import com.wooriss.woorifood2.Fragment.SearchFragment;
import com.wooriss.woorifood2.MainActivity;
import com.wooriss.woorifood2.R;

import java.util.List;

public class SikdangAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Sikdang> sikdangs;
    private Uri titleUri;
    private Context context;

    public SikdangAdapter(List<Sikdang> sikdangs) {
        if (sikdangs == null)
            this.sikdangs = sikdangs;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();

        View itemView;
        Log.d(Code.LOG_TAG, "Code.ViewType : " + viewType);
        if (viewType == Code.ViewType.DEFAULT_SIKDANG) {
            Log.d(Code.LOG_TAG, "Code.ViewType.DEFAULT_SIKDANG" );
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sikdang, parent, false);
            return new SikdangItemViewHolder(itemView);
        } else if (viewType == Code.ViewType.MY_SIKDANG){
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_myreview, parent, false);
            return new ReviewedSikdangItemViewHolder(itemView);
        }
        else {
            Log.d(Code.LOG_TAG, "Code.ViewType.REVIEWED_SIKDANG");
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reviewed_sikdang, parent, false);
            return new ReviewedSikdangItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Sikdang sikdang = sikdangs.get(position);
        if (viewHolder instanceof SikdangItemViewHolder) {
            String place_name = sikdang.getPlace_name();
            String road_address_name = sikdang.getRoad_address_name();
            String distance = sikdang.getDistance();

            ((SikdangItemViewHolder) viewHolder).getItemPlaceNameView().setText(place_name);
            ((SikdangItemViewHolder) viewHolder).getItemRoadAddressNameView().setText(road_address_name);
            ((SikdangItemViewHolder) viewHolder).getItemDistanceView().setText(distance + "m");
        } else { // 리뷰 있는 홀더
            String place_name = sikdang.getPlace_name();
            String road_address_name = sikdang.getRoad_address_name();
            String distance = sikdang.getDistance();

            //String titleImage = sikdang.getTitleImage();
            String titleImageUri = sikdang.getTitleImageUri();

            int numRatings = sikdang.getNumRatings();
            double avgTaste = sikdang.getAvgTaste();
            float avgPrice = sikdang.getAvgPrice();
            float avgLuxury = sikdang.getAvgLuxury();

            ((ReviewedSikdangItemViewHolder) viewHolder).getItemPlaceNameView().setText(place_name);
            ((ReviewedSikdangItemViewHolder) viewHolder).getItemDistanceView().setText(distance + "m");
            ((ReviewedSikdangItemViewHolder) viewHolder).getItemReviewCnt().setText(numRatings + "");
            ((ReviewedSikdangItemViewHolder) viewHolder).getItemAvgTaste().setText(Math.round(sikdang.getAvgTaste() * 10) / 10.0 + "");
            ((ReviewedSikdangItemViewHolder) viewHolder).getItemAvgTasteBar().setRating((float) avgTaste);
            ((ReviewedSikdangItemViewHolder) viewHolder).getItemAvgPrice().setProgress((int) (avgPrice * 10));
            ((ReviewedSikdangItemViewHolder) viewHolder).getItemAvgLuxury().setProgress((int) (avgLuxury * 10));

//            Log.d(Code.LOG_TAG, "NOW : " + sikdang.getPlace_name() + " / ");
            if (titleImageUri != null) {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(55));
                Glide.with(context)
                        .load(titleImageUri)
                        .apply(requestOptions)
//                                                .circleCrop()
                        .into(((ReviewedSikdangItemViewHolder) viewHolder).getImgSikdangTitle());
            } else
                ((ReviewedSikdangItemViewHolder) viewHolder).getImgSikdangTitle().setImageResource(R.drawable.ic_logo);

            ViewCompat.setTransitionName(((ReviewedSikdangItemViewHolder) viewHolder).getImgSikdangTitle(), sikdang.getId());

        }

    }

    @Override
    public int getItemCount() {
        if (sikdangs != null)
            return sikdangs.size();
        else
            return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return sikdangs.get(position).getViewType();
    }

    public void updateData(List<Sikdang> sikdangs) {
        this.sikdangs = sikdangs;
    }


    // inner Class - 리뷰 이력 없는 리스트 아이템
    public class SikdangItemViewHolder extends RecyclerView.ViewHolder {
        private TextView itemPlaceNameView;
        private TextView itemRoadAddressNameView;
        private TextView itemDistanceView;

        SikdangItemViewHolder(View itemView) {
            super(itemView);
            itemPlaceNameView = itemView.findViewById(R.id.item_place_name);
            itemRoadAddressNameView = itemView.findViewById(R.id.item_road_address_name);
            itemDistanceView = itemView.findViewById(R.id.item_distance);

            // 리사이클뷰 내 아이템 클릭 시 이벤트
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //int pos = getAdapterPosition();
                    int pos = getAbsoluteAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {

                        Log.d("plz", SearchFragment.pageListSikdang.body().getDocuments().get(pos).getDistance());
                        Log.d("plz", SearchFragment.pageListSikdang.body().getDocuments().get(pos).getId());

                        new AlertDialog.Builder(view.getContext())
                                .setTitle(itemPlaceNameView.getText().toString())
                                .setMessage("리뷰를 등록하시겠습니까?")
                                .setCancelable(false)
                                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ((MainActivity)view.getContext()).transferToReview(sikdangs.get(pos));
                                    }})
                                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        // 취소 시 처리 로직
                                    }
                                })
                                .show();
                    }
                }
            });

        }

        public TextView getItemDistanceView() {
            return itemDistanceView;
        }

        public TextView getItemPlaceNameView() {
            return itemPlaceNameView;
        }

        public TextView getItemRoadAddressNameView() {
            return itemRoadAddressNameView;
        }
    }


    // inner Class - 리뷰 있는 리스트 아이템
    public class ReviewedSikdangItemViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {
        private TextView itemPlaceNameView;
        private TextView itemDistanceView;
        private TextView itemReviewCnt;
        private TextView itemAvgTaste;
        private RatingBar itemAvgTasteBar;

        private SeekBar itemAvgPrice;
        private SeekBar itemAvgLuxury;

        private ImageView imgSikdangTitle;


        ReviewedSikdangItemViewHolder(View itemView) {
            super(itemView);
            itemPlaceNameView = itemView.findViewById(R.id.item_place_name);
            itemDistanceView = itemView.findViewById(R.id.item_distance);
            itemReviewCnt = itemView.findViewById(R.id.item_reviewCnt);
            itemAvgTaste = itemView.findViewById(R.id.item_avgTaste);
            itemAvgTasteBar = itemView.findViewById(R.id.item_avgTasteBar);

            itemAvgPrice = itemView.findViewById(R.id.seekPrice_);
            itemAvgLuxury = itemView.findViewById(R.id.seekLuxury_);

            imgSikdangTitle = itemView.findViewById(R.id.imgSikdangTitle);


            itemAvgPrice.setOnTouchListener(this);
            itemAvgLuxury.setOnTouchListener(this);

            // 리사이클뷰 내 아이템 클릭 시 이벤트
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //int pos = getAdapterPosition();
                    int pos = getAbsoluteAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {

                        // for test
                        for (Fragment fragment : ((FragmentActivity) context).getSupportFragmentManager().getFragments()) {
                            if (fragment.isVisible()) {
                                if (fragment instanceof MainListFragment) {
                                    if (MainListFragment.mainSikdangList != null) {
                                        Log.d(Code.LOG_TAG, "pos : " + pos + ", mainSikdangList : " + MainListFragment.mainSikdangList.size());
                                        Log.d(Code.LOG_TAG, MainListFragment.mainSikdangList.get(pos).getPlace_name());
                                        Log.d(Code.LOG_TAG, MainListFragment.mainSikdangList.get(pos).getId());
                                    }
                                }
                            }
                        }

                        ((MainActivity)context).transfreToDetail(sikdangs.get(pos), imgSikdangTitle);

                        // end test


//                        else {
//                            if  (SearchFragment.pageListSikdang != null) {
//                                Log.d("plz", "pos : " + pos + ", searchSikdangList : " + SearchFragment.pageListSikdang.body().getDocuments().size());
//
//                                Log.d("plz", SearchFragment.pageListSikdang.body().getDocuments().get(pos).getPlace_name());
//                                Log.d("plz", SearchFragment.pageListSikdang.body().getDocuments().get(pos).getId());
//                            }
//                        }
                        // end for test

                        //((MainActivity)view.getContext()).transferToDetail(sikdangs.get(pos));
                    }
                }
            });

        }

        public TextView getItemDistanceView() {
            return itemDistanceView;
        }

        public TextView getItemPlaceNameView() {
            return itemPlaceNameView;
        }

        public RatingBar getItemAvgTasteBar() {
            return itemAvgTasteBar;
        }

        public SeekBar getItemAvgPrice() {
            return itemAvgPrice;
        }

        public SeekBar getItemAvgLuxury() {
            return itemAvgLuxury;
        }

        public TextView getItemAvgTaste() {
            return itemAvgTaste;
        }

        public TextView getItemReviewCnt() {
            return itemReviewCnt;
        }


        public ImageView getImgSikdangTitle() {
            return imgSikdangTitle;
        }


        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return true;
        }
    }

}