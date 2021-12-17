package com.wooriss.woorifood2.Model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/*
 - 작성일 : 2021.10.19
 - 작성자 : 김성미
 - 기능 : 사용자 정보 클래스
 - 비고 :
 - 수정이력 :
*/
public class User implements Serializable {
    //    private String uid;
    private String user_mail;
    private String user_name;
    private String user_position;
    private String branch_name;
    private String branch_addr;

    public User () {}


    public User(String _user_mail, String _user_name,String _user_position, String _branch_name, String _branch_addr) {
//        this.uid = uid;
        user_mail = _user_mail;
        user_name = _user_name;
        user_position = _user_position;
        branch_name = _branch_name;
        branch_addr = _branch_addr;
    }

//    public String getUid() {
//        return uid;
//    }

    public String getUser_mail() {
        return user_mail;
    }
    public String getUser_name() { return user_name; }
    public String getUser_position() {return user_position;}
    public String getBranch_name() { return branch_name; }
    public String getBranch_addr() { return branch_addr; }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public void setBranch_addr(String branch_addr) {
        this.branch_addr = branch_addr;
    }
    public void setBranch_name(String branch_name) {
        this.branch_name = branch_name;
    }
    public void setUser_position(String user_position) {
        this.user_position = user_position;
    }

}
