package com.wooriss.woorifood2.Activity.KftcFile;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.wooriss.woorifood2.Code;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        Log.d(Code.LOG_TAG, "DBHelper");
    }

    // 앱 설치 후 SQLiteOpenHelper가 최초로 이용되는 순간 한 번 호출출
    // 전체 앱 내에서 가장 처음 한 번만 수행하면 되는 코드 작성 (보통 테이블 생성하는 코드 작성)
    // 만약 테이블 생성 잘못해서 수정해도 한번만 호출되므로 수정부분 반영안됨
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(Code.LOG_TAG, "onCreate");
        sqLiteDatabase.execSQL(Code.SQL_CREATE_ENTRIES);
    }

    // 데이터베이스 버전이 변경될 때만 호출
    // 본 클래스 생성자에 전달되는 버전 변경될 때마다 호출, 테이블 스키마 변경하기 위한 용도
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.d(Code.LOG_TAG, "onUpgrade");
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    // 테이블이 데이터를 가지고 있는지 확인 (데이터가 정상적으로 채워진 테이블인지 여부)
    public boolean hasDataInTable(SQLiteDatabase sqLiteDatabase) {
        Cursor cursor = sqLiteDatabase.rawQuery(Code.SQL_CHECK_TABLE_OK, null);
        int resultCount = cursor.getCount();
        cursor.close();
        if (resultCount > 0)
            return true;
        else
            return false;
    }


    // 테이블에 데이터 insert
    // 500 라인 씩 받아서 union 사용하여 한번에 insert 방식 (더 빠르다고 해서 해봄.. 빠른거 같기도하고...똑같은거 같기도..)
    private StringBuilder sql = null;
    public void insertToTable (SQLiteDatabase sqLiteDatabase, String str, int cur, boolean isLast) {
        if (isLast == false) {
            String [] splitTmp = str.split("\\|");
            if (cur == 1) {
                sql = new StringBuilder();
                sql.append("INSERT INTO " + Code.TABLE_NAME +
                        " (code, name, branch_name, tel, fax, zip, addr) ");
            }


            if (cur % 500 == 0) {
                sqLiteDatabase.execSQL(sql.toString().replaceFirst("UNION ", ""));
                sql.setLength(0);
                sql.append("INSERT INTO " + Code.TABLE_NAME +
                        " (code, name, branch_name, tel, fax, zip, addr) ");
            }

            StringBuilder strTmp = new StringBuilder();
            for (String tmp : splitTmp) {
                strTmp.append(" '").append(tmp).append("',");
            }
            strTmp = new StringBuilder(strTmp.substring(0, strTmp.length() - 1));
            sql.append("UNION SELECT ").append(strTmp);
        } else
        {
            sqLiteDatabase.execSQL(sql.toString().replaceFirst("UNION ", ""));
            sql.setLength(0);
            sql.append("INSERT INTO " + Code.TABLE_NAME +
                    " (code, name, branch_name, tel, fax, zip, addr) ");
        }
    }

    public void insertToTableLast(SQLiteDatabase sqLiteDatabase) {
        Log.d(Code.LOG_TAG, "last insert");
        sqLiteDatabase.execSQL(Code.SQL_INSERT_LAST_FLAG);
    }

}