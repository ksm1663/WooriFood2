package com.wooriss.woorifood2.Fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.skydoves.transformationlayout.TransformationCompat;
import com.skydoves.transformationlayout.TransformationLayout;
import com.wooriss.woorifood2.Activity.LoadingActivity;
import com.wooriss.woorifood2.Code;
import com.wooriss.woorifood2.Dialog.SearchBranchDialog;
import com.wooriss.woorifood2.Model.User;
import com.wooriss.woorifood2.R;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class JoinFragment extends Fragment {

    private Context context;

    private View layJoinMain;
    private LinearLayout layJoinView;
    private Button btnBackToLogin;
    private AutoCompleteTextView dropdownDomain;
    private AutoCompleteTextView dropdownPosition;

    private EditText textMailInJoin;
    private EditText textPasswordInJoin;
    private EditText textNameInJoin;
    private EditText textBranchInJoin;
    private Button btnJoinUs;

    private HashMap<String, String> branch_info;

    private FirebaseAuth mAuth ; // 인증기능을 가지고 있는 객
    private FirebaseDatabase mDatabase;


    public JoinFragment() {}

    public static JoinFragment newInstance(Bundle bundle) {
        JoinFragment reviewFragment = new JoinFragment();
        reviewFragment.setArguments(bundle);

        return reviewFragment;
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            TransformationLayout.Params params = getArguments().getParcelable("TransformationParams");
            TransformationCompat.onTransformationEndContainer(this, params);
        }
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(container != null)
            context = container.getContext();

        return inflater.inflate(R.layout.fragment_join, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);
        init();
        setListener();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((LoadingActivity)context).setHideJoinBtn(false);
    }

    private void findViews(View v) {
        layJoinMain = v.findViewById(R.id.layJoinMain);
        layJoinView = v.findViewById(R.id.layJoinView);
        btnBackToLogin = v.findViewById(R.id.btnBackToLogin);

        dropdownDomain = v.findViewById(R.id.dropdownDomain);
        dropdownPosition = v.findViewById(R.id.dropdownPosition);

        textMailInJoin = v.findViewById(R.id.textMailInJoin);
        textPasswordInJoin = v.findViewById(R.id.textPasswordInJoin);
        textNameInJoin = v.findViewById(R.id.textNameInJoin);
        textBranchInJoin = v.findViewById(R.id.textBranchInJoin);
        btnJoinUs = v.findViewById(R.id.btnJoinUs);
    }

    private void init() {
        mAuth = FirebaseAuth.getInstance(); // 인증기능 수행할 일
        mDatabase = FirebaseDatabase.getInstance();

        layJoinView.setTransitionName(getString(R.string.transitionNameJoinView));

        ArrayAdapter<String> dropdownDomainAdapter = new ArrayAdapter<>(
                context, R.layout.item_dropdown_outline, getResources().getStringArray(R.array.domains)
        );
        dropdownDomain.setAdapter(dropdownDomainAdapter);

        ArrayAdapter<String> dropdownPositionAdapter = new ArrayAdapter<>(
                context, R.layout.item_dropdown_outline, getResources().getStringArray(R.array.positions)
        );
        dropdownPosition.setAdapter(dropdownPositionAdapter);

    }

    private void setListener() {

        addListenerToBtnBackToLogin();
        addListenerToEditBranch();
        addListenerToDropdownDomain();
        addListenerToBtnJoinUs();
    }

    private void addListenerToBtnBackToLogin() {
        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.ani_slid_in_top, R.anim.ani_slid_out_top
                        , R.anim.ani_slid_in_top, R.anim.ani_slid_out_top).remove(JoinFragment.this).commit();
                fragmentManager.popBackStack();
            }
        });
    }

    // 브랜치 텍스트 박스 눌렀을 때 이벤트 정의
    private void addListenerToEditBranch() {
        textBranchInJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SearchBranchDialog(context, new SearchBranchDialog.ICustomDialogEventListener() {
                    @Override
                    public void customDialogEvent(HashMap<String, String> _branch_info) {
                        // Do something with the value here, e.g. set a variable in the calling activity
                        if (_branch_info != null) {
                            branch_info = _branch_info;
                            textBranchInJoin.setText(_branch_info.get("branch_name"));
                        }
                    }
                });

            }
        });
    }

    // 회원가입 버튼 클릭 시 이벤트
    private void addListenerToBtnJoinUs() {
        btnJoinUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = getEmail();
                String pw = textPasswordInJoin.getText().toString();
                String branch = textBranchInJoin.getText().toString();
                String name = textNameInJoin.getText().toString();
                String pos = dropdownPosition.getText().toString();

                if (mail.length() == 0 || pw.length() <= 6 || branch.length() == 0
                        || name.length() == 0) {
                    Snackbar.make(layJoinMain, getString(R.string.popReqInputJoinInfo), Snackbar.LENGTH_SHORT).show();
                }else {
                    mAuth.createUserWithEmailAndPassword(mail, pw)
                            .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {      // 계정 생성 성공
                                        // Sign in success, update UI with the signed-in user's information
                                        FirebaseUser fuser = mAuth.getCurrentUser();

                                        // 계정 정보 DB에 저장
                                        String uid = fuser.getUid();
                                        User u = new User(mail, name, pos, branch_info.get("branch_name"), branch_info.get("branch_addr"));

                                        mDatabase.getReference().child("users").child(uid).setValue(u).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Log.d(Code.LOG_TAG, "계정 생성 성공!");
                                                Toast.makeText(context, getString(R.string.popCreateSuccess), Toast.LENGTH_SHORT).show();
                                                ((LoadingActivity)context).showMainActivity (fuser, u);
                                                btnBackToLogin.performClick();
                                            }
                                        });

                                    } else {                        // 계정 생성 실패
                                        // If sign in fails, display a message to the user.
                                        if(task.getException()==null) {
                                            Snackbar.make(layJoinMain, getString(R.string.popCreateFailed), Snackbar.LENGTH_SHORT).show();
                                            return;
                                        }
                                        Log.d(Code.LOG_TAG, "계정 생성 실패!", task.getException());
                                        if (task.getException().toString().contains("FirebaseAuthUserCollisionException"))
                                            Snackbar.make(layJoinMain, getString(R.string.popAlreadyUser), Snackbar.LENGTH_SHORT).show();
                                        else
                                            Snackbar.make(layJoinMain, getString(R.string.popCreateFailed), Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(Code.LOG_TAG, "계정 생성 실패! : " + e.getMessage());
                            Snackbar.make(layJoinMain, getString(R.string.popCreateFailed), Snackbar.LENGTH_SHORT).show();
                        }
                    });

                }




            }
        });
    }

    private void addListenerToDropdownDomain() {
        dropdownDomain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LoadingActivity)context).hideKeyboard();
            }
        });
        dropdownPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LoadingActivity)context).hideKeyboard();
            }
        });
    }

    private String getEmail() {
        return textMailInJoin.getText().toString() + dropdownDomain.getText().toString();
    }
}
