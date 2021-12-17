package com.wooriss.woorifood2.Fragment;

import android.app.AlertDialog;
import android.content.ContentProvider;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.wooriss.woorifood2.Code;
import com.wooriss.woorifood2.Dialog.SearchBranchDialog;
import com.wooriss.woorifood2.MainActivity;
import com.wooriss.woorifood2.Model.User;
import com.wooriss.woorifood2.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class UserInfoFragment extends Fragment {

    private Context context;
    private FirebaseUser f_user;
    private User user;

    private View layUserInfoMain;
    private TextView textTitle;
    private TextView textUsername;
    private TextInputLayout textUserMail;
    private EditText editBranch;
    private TextInputLayout textUserBranchAddr;

    private AutoCompleteTextView classTextView;

    private Button btnEdit;

    public UserInfoFragment() { }

    public static UserInfoFragment newInstance (Bundle bundle) {
        UserInfoFragment userInfoFragment = new UserInfoFragment();
        userInfoFragment.setArguments(bundle);
        return userInfoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            f_user = getArguments().getParcelable("f_user");
            user = (User) getArguments().getSerializable("user");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(container!=null)
            context = container.getContext();
        return inflater.inflate(R.layout.fragment_user_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        findViews(view);
        init();

        setListener();

    }

    private void findViews(View v) {
        layUserInfoMain = v.findViewById(R.id.layUserInfoMain);
        textTitle = v.findViewById(R.id.text_title);
        textUsername = v.findViewById(R.id.name_edit_text);
        textUserMail = v.findViewById(R.id.name_text_input);
        editBranch = v.findViewById(R.id.branch_edit_text);
        editBranch.setInputType(InputType.TYPE_NULL);
        textUserBranchAddr = v.findViewById(R.id.branch_text_input);
        btnEdit = v.findViewById(R.id.save_button);
        classTextView = v.findViewById(R.id.class_text_view);
    }

    private void init() {
        // DropDown Setting
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                R.layout.item_dropdown_outline,
                context.getResources().getStringArray(R.array.positions)
        );
        classTextView.setAdapter(adapter);

        textTitle.setText(user.getUser_name()+"님\n안녕하세요!");
        textUsername.setText(user.getUser_name());
        textUserMail.setHelperText( user.getUser_mail());

        editBranch.setText(user.getBranch_name());
        textUserBranchAddr.setHelperText(user.getBranch_addr());

        // false 해야 Filtering 동작 안함
        classTextView.setText(user.getUser_position(),false);
    }

    private void setListener() {
        addListenerToEditBtn();
        addListenerToBranchEdit();
    }


    // 수정버튼 클릭 시
    private void addListenerToEditBtn() {
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isUserInfoChanged())
                    new AlertDialog.Builder(context)
                            .setTitle("정보 수정")
                            .setMessage("저장하시겠습니까?")
//                            .setIcon(android.R.drawable.ic_menu_save)
                            .setCancelable(false)
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    ((MainActivity)context).updateUserInfo(
                                            editBranch.getText().toString(),
                                            textUserBranchAddr.getHelperText().toString(),
                                            classTextView.getText().toString());
                                    //spinPosition.getSelectedItem().toString());

                                }})
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // 취소 시 처리 로직
                                }
                            })
                            .show();
                else
                    Snackbar.make(layUserInfoMain, getString(R.string.textNoChanged), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void addListenerToBranchEdit() {
        editBranch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SearchBranchDialog(context, new SearchBranchDialog.ICustomDialogEventListener() {
                    @Override
                    public void customDialogEvent(HashMap<String, String> _branch_info) {
                        // Do something with the value here, e.g. set a variable in the calling activity
                        if (_branch_info != null) {
                            //branch_info = _branch_info;
                            editBranch.setText(_branch_info.get("branch_name"));
                            textUserBranchAddr.setHelperText(_branch_info.get("branch_addr"));
                        }
                    }
                });
            }
        });
    }

    // 기존 데이터와 변경 된 것이 있는지 확인 (변경가능 데이터 : 지점, 직급)
    private boolean isUserInfoChanged() {

        if ((editBranch.getText().toString().equals(user.getBranch_name())) &&
                (classTextView.getText().toString().equals(user.getUser_position()))) {

            Log.d(Code.LOG_TAG, "info is not changed");
            return false;
        }
        Log.d(Code.LOG_TAG, "info is changed");
        return true;
    }
}
