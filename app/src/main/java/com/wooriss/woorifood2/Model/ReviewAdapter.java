package com.wooriss.woorifood2.Model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.wooriss.woorifood2.Code;
import com.wooriss.woorifood2.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ReviewSet> reviewList;
    private DetailImageViewAdapter detailImageViewAdapter;
    private Context context;
    private int viewType;

    public ReviewAdapter(List<ReviewSet> reviewList, Context context) {

        this.context = context;
        this.reviewList = reviewList;

    }

    public void notifyDetailImageViewAdaper() {
        if (detailImageViewAdapter != null)
            detailImageViewAdapter.notifyDataSetChanged();
    }


    // 아이템 뷰를 위한 뷰홀더 객체를 생성하여 리턴
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        this.viewType = viewType;
        Log.d(Code.LOG_TAG, "Code.ViewType : " + viewType);
        if (viewType == Code.ViewType.MY_SIKDANG) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_myreview, parent, false);
            return new ReviewAdapter.ReviewItemHolder(view);
        } else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.item_detail_sikdang, parent, false);
            return new ReviewAdapter.ReviewItemHolder(view);
        }
    }


    // position 에 해당하는 데이터를 뷰홀더의 아이템 뷰에 표시
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ReviewSet reviewSet = reviewList.get(position);

        Timestamp uploadDate = reviewSet.getReview().getTimestamp();
        User user = reviewSet.getUser();
//            String uploadUser = reviewSet.getUser().getUser_name();
        double ratingTaste = reviewSet.getReview().getTaste();

        int price = reviewSet.getReview().getPrice();
        int luxury = reviewSet.getReview().getLuxury();
        int visit = reviewSet.getReview().getVisit();
        int complex = reviewSet.getReview().getComplex();


        String comment = reviewSet.getReview().getComment();

        List<Uri> userImages = reviewSet.getImages();

        detailImageViewAdapter = new DetailImageViewAdapter(userImages, context, viewType);
//            ((ReviewItemHolder) holder).getImageListInDetailView().setHasFixedSize(true);
        ((ReviewAdapter.ReviewItemHolder) holder).getImageListInDetailView().setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        ((ReviewAdapter.ReviewItemHolder) holder).getImageListInDetailView().setAdapter(detailImageViewAdapter);

        if ((userImages == null) || (userImages.size() <=0))
            ((ReviewAdapter.ReviewItemHolder) holder).getImageListInDetailView().setVisibility(View.GONE);
        else
            ((ReviewAdapter.ReviewItemHolder) holder).getImageListInDetailView().setVisibility(View.VISIBLE);

        if ((comment == null) || (comment.length() <=0))
            ((ReviewAdapter.ReviewItemHolder) holder).getLayComment().setVisibility(View.GONE);
        else
            ((ReviewAdapter.ReviewItemHolder) holder).getLayComment().setVisibility(View.VISIBLE);

//        DateFormat format = new SimpleDateFormat("yyyy-MM-dd (HH:mm:ss)");
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        ((ReviewAdapter.ReviewItemHolder) holder).getTextUploadDate().setText(format.format(uploadDate.toDate()));


        String reviewerInfo = user.getUser_name() + " " + user.getUser_position() + " (" + user.getBranch_name() + ")";
        ((ReviewAdapter.ReviewItemHolder) holder).getTextUploadUser().setText(reviewerInfo);



        ((ReviewAdapter.ReviewItemHolder) holder).getRatingTaste().setRating((float) ratingTaste);

        ((ReviewAdapter.ReviewItemHolder) holder).getTextComment().setText(comment);

        if (price == Code.PriceType.CHEAP)
            ((ReviewAdapter.ReviewItemHolder) holder).getRadioPrice().setText("쌈");
        else if (price == Code.PriceType.NORMAL)
            ((ReviewAdapter.ReviewItemHolder) holder).getRadioPrice().setText("보통");
        else if (price == Code.PriceType.EXPENSIVE)
            ((ReviewAdapter.ReviewItemHolder) holder).getRadioPrice().setText("비쌈");

        if (luxury == Code.LuxuryType.BAD)
            ((ReviewAdapter.ReviewItemHolder) holder).getRadioLuxury().setText("없음");
        else if (luxury == Code.LuxuryType.NORMAL)
            ((ReviewAdapter.ReviewItemHolder) holder).getRadioLuxury().setText("무난");
        else if (luxury == Code.LuxuryType.GOOD)
            ((ReviewAdapter.ReviewItemHolder) holder).getRadioLuxury().setText("고급");

        if (visit == Code.VisitType.FIRST)
            ((ReviewAdapter.ReviewItemHolder) holder).getRadioVisit().setText("1차");
        else if (visit == Code.VisitType.SECOND)
            ((ReviewAdapter.ReviewItemHolder) holder).getRadioVisit().setText("2차");
        else if (visit == Code.VisitType.THIRD)
            ((ReviewAdapter.ReviewItemHolder) holder).getRadioVisit().setText("3차");

        if (complex == Code.ComplexType.COZY)
            ((ReviewAdapter.ReviewItemHolder) holder).getRadioComplex().setText("여유");
        else if (complex == Code.ComplexType.NORMAL)
            ((ReviewAdapter.ReviewItemHolder) holder).getRadioComplex().setText("보통");
        else if (complex == Code.ComplexType.BUZY)
            ((ReviewAdapter.ReviewItemHolder) holder).getRadioComplex().setText("혼잡");
//            ((ReviewItemHolder)holder).getImageListInDetailView().setAdapter();
    }


    @Override
    public int getItemCount() {
        if (reviewList != null)
            return reviewList.size();
        else
            return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return reviewList.get(position).getViewType();
    }

    // inner or inner Class : 이미지리스트 안에 들어갈 아이템
    public class ReviewItemHolder extends RecyclerView.ViewHolder {
        private TextView textUploadDate;
        private TextView textUploadUser;
        private TextView textReviewerStr;
        private RatingBar ratingTaste;

        private RadioButton radioPrice;
        private RadioButton radioLuxury;
        private RadioButton radioVisit;
        private RadioButton radioComplex;
        private LinearLayout layComment;
        private TextView textComment;

        private RecyclerView imageListInDetailView;


        ReviewItemHolder(View itemView) {
            super(itemView);
            textUploadDate = itemView.findViewById(R.id.textUploadDate);
            textUploadUser = itemView.findViewById(R.id.textUploadUser);
            ratingTaste = itemView.findViewById(R.id.ratingTaste);

            textReviewerStr = itemView.findViewById(R.id.textReviewerStr);

            radioPrice = itemView.findViewById(R.id.radioPrice);
            radioLuxury = itemView.findViewById(R.id.radioLuxury);
            radioVisit = itemView.findViewById(R.id.radioVisit);
            radioComplex = itemView.findViewById(R.id.radioComplex);
            layComment = itemView.findViewById(R.id.layComment);
            textComment = itemView.findViewById(R.id.textComment);

            imageListInDetailView = itemView.findViewById(R.id.imageListInDetailView);

            //클릭 이벤트 달고 싶으면 itemView.setOnClickListener
        }

        public TextView getTextUploadDate() {
            return textUploadDate;
        }

        public TextView getTextUploadUser() {
            return textUploadUser;
        }

        public RatingBar getRatingTaste() {
            return ratingTaste;
        }

        public RadioButton getRadioPrice() {
            return radioPrice;
        }

        public RadioButton getRadioLuxury() {
            return radioLuxury;
        }

        public RadioButton getRadioVisit() {
            return radioVisit;
        }

        public RadioButton getRadioComplex() {
            return radioComplex;
        }

        public LinearLayout getLayComment() {
            return layComment;
        }

        public TextView getTextComment() {
            return textComment;
        }

        public RecyclerView getImageListInDetailView() {
            return imageListInDetailView;
        }

        public TextView getTextReviewerStr() {
            return textReviewerStr;
        }
    }
}