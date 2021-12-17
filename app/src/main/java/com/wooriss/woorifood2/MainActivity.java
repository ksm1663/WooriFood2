package com.wooriss.woorifood2;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialog;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.transition.Explode;
import android.transition.TransitionInflater;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skydoves.transformationlayout.TransformationLayout;
import com.wooriss.woorifood2.Activity.SplashActivity;
import com.wooriss.woorifood2.Control.FoodLocation;
import com.wooriss.woorifood2.Fragment.DetailFragment;
import com.wooriss.woorifood2.Fragment.MainListFragment;
import com.wooriss.woorifood2.Fragment.MyReviewFragment;
import com.wooriss.woorifood2.Fragment.ReviewFragment;
import com.wooriss.woorifood2.Fragment.SearchFragment;
import com.wooriss.woorifood2.Fragment.UserInfoFragment;
import com.wooriss.woorifood2.Model.Sikdang;
import com.wooriss.woorifood2.Model.User;

import java.util.HashMap;
import java.util.Map;

//

public class MainActivity extends AppCompatActivity {

    private FirebaseUser f_user;
    private User user;
    private FirebaseDatabase mDatabase;
    private FirebaseFirestore fireStore;

    private NavigationBarView navigationBarView;
    private View layMain;

    public static FoodLocation foodLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.ani_fade_in, R.anim.ani_fade_out);
        setContentView(R.layout.activity_main);

        findViews();
        init();
        addListenerToNavigationBar();

        showInitNavi();

    }

    private void findViews() {
        navigationBarView = findViewById(R.id.bottom_navigation);
        layMain = findViewById(R.id.layMain);
    }

    private void init() {
        Intent intent = getIntent();
        f_user = (FirebaseUser) intent.getParcelableExtra("f_user");
        user = (User) intent.getSerializableExtra("user");
        Toast.makeText(this, user.getUser_name() + getString(R.string.popWelcome), Toast.LENGTH_SHORT).show();

        mDatabase = FirebaseDatabase.getInstance();
        fireStore = FirebaseFirestore.getInstance();

        foodLocation = new FoodLocation(this);

    }

    private void showInitNavi() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("f_user", f_user);
        bundle.putSerializable("user", user);
        transferTo(MainListFragment.newInstance(bundle));
    }

    // 네비게이션 바 이벤트
    private void addListenerToNavigationBar() {
        // 메뉴 클릭되었을 때 이벤트
        navigationBarView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Bundle bundle = new Bundle();
                bundle.putParcelable("f_user", f_user);
                bundle.putSerializable("user", user);

                switch (item.getItemId()) {
                    case R.id.page_1:// Respond to navigation item 1 click
                        transferTo(MainListFragment.newInstance(bundle));

                        return true;
                    case R.id.page_2:// Respond to navigation item 2 click
                        transferTo(SearchFragment.newInstance(bundle));

                        return true;
                    case R.id.page_3:// Respond to navigation item 3 click
                        transferTo(MyReviewFragment.newInstance(bundle));

                        return true;
                    case R.id.page_4:// Respond to navigation item 4 click
                        transferTo(UserInfoFragment.newInstance(bundle));

                        return true;
                    default:
                        return false;
                }
            }
        });

        // 똑같은 메뉴 또 클릭되었을 떄 이벤트 : 아무것도 안하기!
        navigationBarView.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
            }
        });
    }

    private void transferTo(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();
    }

    public void transfreToDetail(Sikdang sikdang, ImageView imageTitle) {

        Bundle bundle = new Bundle();
        bundle.putParcelable("f_user", f_user);
        bundle.putSerializable("user", user);
        bundle.putSerializable("sikdang", sikdang);

        DetailFragment detailFragment = DetailFragment.newInstance(bundle);
        if (!detailFragment.isVisible()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                detailFragment.setSharedElementReturnTransition(new Explode());
                getSupportFragmentManager()
                        .beginTransaction()
                        .addSharedElement(imageTitle, ViewCompat.getTransitionName(imageTitle))
                        .replace(R.id.main_container, detailFragment) //replace
                        .addToBackStack(TAG)
                        .commit();
            }
        }

    }

    public void transferToReview(Sikdang sikdang) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("f_user", f_user);
        bundle.putSerializable("user", user);
        bundle.putSerializable("sikdang", sikdang);
        ReviewFragment reviewFragment = ReviewFragment.newInstance(bundle);
        if (!reviewFragment.isVisible()) {
//            getSupportFragmentManager().beginTransaction().add(R.id.main_container, reviewFragment).addToBackStack(null).commit();
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.ani_slid_in_bottom, R.anim.ani_fade_out
                    ,0,  R.anim.ani_slid_out_bottom).replace(R.id.main_container, reviewFragment).addToBackStack(null).commit();
        }
    }


    // 유저 수정 정보 업데이트
    //@Override
    public void updateUserInfo(String newBranchName, String newBranchAddr, String newUserPosition) {
        Log.d("plz", "called updateUserInfo!!!! in MainActivity");

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + f_user.getUid() + "/" + "branch_name", newBranchName);
        childUpdates.put("/users/" + f_user.getUid() + "/" + "branch_addr", newBranchAddr);
        childUpdates.put("/users/" + f_user.getUid() + "/" + "user_position", newUserPosition);

        mDatabase.getReference().updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(Code.LOG_TAG, "save successed");
                Toast.makeText(MainActivity.this, getString(R.string.textSaveSuccess), Toast.LENGTH_SHORT).show();

                user.setBranch_name(newBranchName);
                user.setBranch_addr(newBranchAddr);
                user.setUser_position(newUserPosition);

                //***
                //MainActivity.foodLocation.getBranchPosition(newBranchAddr);

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(Code.LOG_TAG, "save failed");
                        Snackbar.make(layMain, getString(R.string.textSaveFailed), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }



    private Dialog progressDialog;
    public void progressON(Activity activity, String message) {

        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            //progressSET(message);
        } else {
            progressDialog = new AppCompatDialog(activity);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            progressDialog.setContentView(R.layout.dialog_loading);
            progressDialog.show();
        }


//        LottieAnimationView animationView = progressDialog.findViewById(R.id.iv_frame_loading);
//        animationView.addAnimatorListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
//            }
//        });

        final ImageView img_loading_frame = (ImageView) progressDialog.findViewById(R.id.iv_frame_loading);
        Glide.with(this).load(R.drawable.ic_walking_duck).into(img_loading_frame);

        TextView tv_progress_message = (TextView) progressDialog.findViewById(R.id.tv_progress_message);
        if (!TextUtils.isEmpty(message)) {
            tv_progress_message.setText(message);
        }
    }
    public void progressOFF() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


}
