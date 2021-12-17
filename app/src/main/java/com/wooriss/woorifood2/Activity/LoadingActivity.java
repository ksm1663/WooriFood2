package com.wooriss.woorifood2.Activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.wooriss.woorifood2.Activity.KftcFile.DownloadFile;
import com.wooriss.woorifood2.Code;
import com.wooriss.woorifood2.Fragment.JoinFragment;
import com.wooriss.woorifood2.MainActivity;
import com.wooriss.woorifood2.Model.User;
import com.wooriss.woorifood2.R;

import java.util.Arrays;

public class LoadingActivity extends AppCompatActivity {
    private View layLoadingMain;
    private TextView textTitle;
    private LinearLayout layImage;
    private LinearLayout layInputLogin;
    private FrameLayout layLoading;
    private CircularProgressBar progressLoading;
    private LottieAnimationView lotLoading;
    private AutoCompleteTextView dropdownDomain;
    private FloatingActionButton btnOpenJoin;
    private TextView textMail;
    private TextView textPassword;
    private Button btnLogin;

    private ArrayAdapter<String> dropdownAdapter;

    private FirebaseAuth mAuth; // 인증기능을 가지고 있는 객
    private FirebaseDatabase mDatabase;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        findViews();
        init();
        setListener();

        // 파일 다운로드 시작
        new DownloadFile(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        // 활동 초기화할 때 유저가 이미 로그인 되어 있는지 확인
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) { // 이미 가입된 회원일 때 처리 => 아이디만 미리 세팅해주기
            String mail = currentUser.getEmail();
            String[] tmp = mail.split("@");

            Log.d(Code.LOG_TAG, " 기인증 회원 (Uid): " + tmp[0] + "/" + tmp[1]);
            textMail.setText(tmp[0]);
            dropdownDomain.setText(dropdownAdapter.getItem(Arrays.asList(getResources().getStringArray(R.array.domains)).indexOf("@" + tmp[1])).toString(), false);

        } else
            Log.d(Code.LOG_TAG, " first time visit!!!");
    }


    private void findViews() {
        layLoadingMain=findViewById(R.id.layLoadingMain);
        textTitle = findViewById(R.id.textTitle);
        layImage = findViewById(R.id.layImage);
        layLoading = findViewById(R.id.layLoading);
        layInputLogin = findViewById(R.id.layInputLogin);
        progressLoading = findViewById(R.id.progressLoading);
        lotLoading = findViewById(R.id.lotLoading);
        dropdownDomain = findViewById(R.id.dropdownDomain);
        btnOpenJoin = findViewById(R.id.btnOpenJoin);
        textMail = findViewById(R.id.textMail);
        textPassword = findViewById(R.id.textPassword);
        btnLogin = findViewById(R.id.btnLogin);
    }

    private void init() {
        layLoading.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.ani_zoom_in));
        textTitle.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.ani_slid_in_top));
        layImage.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.ani_slid_in_bottom));
        lotLoading.setAnimation(R.raw.js_loadingimage);
        lotLoading.playAnimation();

        dropdownAdapter = new ArrayAdapter<>(
                this, R.layout.item_dropdown, getResources().getStringArray(R.array.domains)
        );

        dropdownDomain.setAdapter(dropdownAdapter);

        mAuth = FirebaseAuth.getInstance(); // 인증기능 수행할 일
        mDatabase = FirebaseDatabase.getInstance();

    }



    private void setListener() {
        addListenerToBtnOpenJoin();
        addListenerToBtnLogin();
        addListenerToDropdownDomain();
    }

    // 로그인 버튼 클릭
    private void addListenerToBtnLogin() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = getEmail();
                String pw = textPassword.getText().toString();

                Log.d(Code.LOG_TAG, "mail : "  + mail + "/ pw : " + pw);
                if (mail.length() == 0 || pw.length() == 0) {
                    Snackbar.make(layLoadingMain, getString(R.string.popReqInputLogInfo), Snackbar.LENGTH_SHORT).show();
                }
                else {
                    mAuth.signInWithEmailAndPassword(mail, pw)
                            .addOnCompleteListener(LoadingActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {   // 로그인 성공
                                        FirebaseUser fuser = mAuth.getCurrentUser();
                                        getUserInfo (fuser);  // 여기서 showMainActivity 호출


                                    } else {                    // 로그인 실패
                                        // If sign in fails, display a message to the user.
                                        if(task.getException()==null) {
                                            Snackbar.make(layLoadingMain, getString(R.string.popFailLogin), Snackbar.LENGTH_SHORT).show();
                                            return;
                                        }
                                        if (task.getException().toString().contains("FirebaseAuthInvalidUserException"))
                                            Snackbar.make(layLoadingMain, getString(R.string.popWrongMail), Snackbar.LENGTH_SHORT).show();
                                        else if (task.getException().toString().contains("FirebaseAuthInvalidCredentialsException"))
                                            Snackbar.make(layLoadingMain, getString(R.string.popWrongPassword), Snackbar.LENGTH_SHORT).show();
                                        else
                                            Snackbar.make(layLoadingMain, getString(R.string.popFailLogin), Snackbar.LENGTH_SHORT).show();

                                    }
                                }
                            });



                }
            }
        });
    }



    private void getUserInfo(FirebaseUser f_user) {
        // 데이터베이스 읽기  - 한번 읽기 (로드된 후 변경되지 않거나 능동적으로 수신대기할 필요 없는 경우 사용)
        // firebase 에서 로그인 한 유저 정보 가져오기
        mDatabase.getReference().child("users").child(f_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user ==null)
                    Log.d(Code.LOG_TAG, "There is no user data");
                else {
                    showMainActivity(f_user, user);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(Code.LOG_TAG, "onCancelled of addListenerForSingleValueEvent");
            }
        });
    }

    public void showMainActivity (FirebaseUser fuser, User _user) {

        Log.d(Code.LOG_TAG, fuser.getEmail() + " / " + _user.getUser_mail());

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("f_user", fuser);
        intent.putExtra("user", _user);

        startActivity(intent);
        finish();
    }

    private String getEmail() {
        return textMail.getText().toString() + dropdownDomain.getText().toString();
    }


    // 회원가입 버튼 클릭 (플로팅버튼)
    private void addListenerToBtnOpenJoin() {
        btnOpenJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JoinFragment fragment = new JoinFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

                if (!fragment.isVisible()) {
                    fragmentTransaction.setCustomAnimations(R.anim.ani_slid_in_top, R.anim.ani_slid_out_top
                            , R.anim.ani_slid_in_top, R.anim.ani_slid_out_top).replace(R.id.layJoinViewContainer, fragment).addToBackStack(null).commit();

                    setHideJoinBtn(true);
                }
            }
        });
    }


    public void setHideJoinBtn(boolean is) {
        if (is) {
            btnOpenJoin.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.ani_zoom_out));
            btnOpenJoin.setVisibility(View.GONE);
        } else {
            btnOpenJoin.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.ani_zoom_in));
            btnOpenJoin.setVisibility(View.VISIBLE);
        }
    }


    private void addListenerToDropdownDomain() {
        dropdownDomain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
            }
        });
    }

    public void setProgressBar(int all, int cur) {
        float tmp = (float) cur / (float) all * 100;
        progressLoading.setProgress(tmp);
    }

    public void setProgressBarNoDownload(int val) {
        progressLoading.setProgress((float) val);
    }

    public void showLogin() {
        animationForLogin();
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if(view==null)
            view = new View(this);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void animationForLogin() {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.ani_zoom_out);
        layLoading.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                layImage.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.ani_slid_out_bottom));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layLoading.setVisibility(View.GONE);
                layImage.setVisibility(View.GONE);

                btnOpenJoin.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.ani_zoom_in));
                btnOpenJoin.setVisibility(View.VISIBLE);

                layInputLogin.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.ani_fade_in));
                layInputLogin.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

}
