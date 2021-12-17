package com.wooriss.woorifood2.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.wooriss.woorifood2.Code;
import com.wooriss.woorifood2.MainActivity;
import com.wooriss.woorifood2.R;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView (R.layout.activity_splash);

        initActivity();
    }


    // 스플래시 화면에서 넘어갈 때 뒤로가기 막기
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void initActivity() {
        Handler handler = new Handler();
        handler.postDelayed(new SplashHandler(), Code.SPLASH_DELAY);

        LottieAnimationView animationView = findViewById(R.id.lotSplash);
        animationView.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //
            }
        });
    }


    // Inner Class
    private class SplashHandler implements Runnable {
        @Override
        public void run() {
            startActivity(new Intent(getApplication(), LoadingActivity.class));
            SplashActivity.this.finish();
        }
    }
}
