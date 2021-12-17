package com.wooriss.woorifood2.Activity.KftcFile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.wooriss.woorifood2.Activity.LoadingActivity;
import com.wooriss.woorifood2.Code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

// 인자 : <Params, Progress, Result>
// Params : doInBackground 파라미터 타입. execute 메소드 인자 값
// Progress : doInBackground 작업 시 진행 단위 타입으로 onProgressUpdate 파라미터 타입
// Result : doInBackground 리턴 값으로 onPostExecute 파라미터 타입
public class DownloadFile extends AsyncTask<String, Integer, Long> {

    private final Context context;
    private DBHelper dbHelper;
    private SQLiteDatabase sqLiteDatabase;
    private ResponseBody responseBody;

    private int retryCount=0;
    private int totalLineCnt = 0;

    public DownloadFile(Context context) {
        this.context = context;

        dbHelper = new DBHelper(context, Code.DATABASE_NAME, null, Code.DATABASE_VERSION);
        sqLiteDatabase = dbHelper.getWritableDatabase();

        sqLiteDatabase.execSQL(Code.SQL_DELETE_ENTRIES);
        sqLiteDatabase.execSQL(Code.SQL_CREATE_ENTRIES);

        if (!dbHelper.hasDataInTable(sqLiteDatabase)){ // 테이블 내 (완전한) 데이터 없음
            Log.d(Code.LOG_TAG, "Need to download data from KFTC");
            kftcFileDownload();
        } else {
            Log.d(Code.LOG_TAG, "Don't need to download data!");
            ((LoadingActivity)context).setProgressBarNoDownload(100);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((LoadingActivity)context).showLogin();
                }
            }, 2000);


        }
    }

    // 백그라운드 작업 실행 전 onPreExecute() 실행됨
    // 로딩 중 이미지를 띄워놓거나, 스레드 작업 이전에 수행할 동작 구현
    @Override
    protected void onPreExecute() {
        Log.d(Code.LOG_TAG, "onPreExcute");
    }


    // doInBackground() : 새로 만든 쓰레드에서 백그라운드 작업 수행. execute() 메소드를 호출할 때 사용된 파라미터 전달 받음
    // 중간 진행상태 UI 업데이트 하려면 publishProgress() 메소드 호출
    // publishProgress() 호출될 때 마다 자동으로 onProgressUpdate() 호출됨
    // doInBackground 작업이 끝나면 onPostExcuted()로 결과 파라미터 리턴, 그 리턴값으 스레드 작업 끝났을 때의 동작 구현
    @Override
    protected Long doInBackground(String... strings) { // 전달된 URL 사용 작업 strings[i]
        Log.d(Code.LOG_TAG, "doInBackground");

        if (responseBody != null)
            inputDataofKFTC (responseBody);
        return null;
    }

    // 파일 다운로드 퍼센티지 표시 작업 values[i]
    @Override
    protected void onProgressUpdate(Integer... values) {
        ((LoadingActivity)context).setProgressBar(totalLineCnt, values[0]);
    }

    // AsyncTask 종료 시 호출
    // doInbackground 에서 받아온 total 값 사용 장소
    @Override
    protected void onPostExecute (Long result) {
        Log.d(Code.LOG_TAG, "onPostExecute");

        if (dbHelper!=null)
            dbHelper.close();
        if(sqLiteDatabase!=null)
            sqLiteDatabase.close();

        ((LoadingActivity)context).showLogin();

    }



    private void kftcFileDownload() {
        RetrofitserviceKFTC kftcService = new RetrofitFactoryKFTC().create();
        Call<ResponseBody> kftcCall = kftcService.getKftcData(
                Code.KFTC_DOWNLOAD_FILENAME, Code.KFTC_DOWNLOAD_FILENAME, Code.KFTC_DOWNLOAD_MODE);

        kftcCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.d(Code.LOG_TAG, "SUCCESS GET KFTC DATA ! ");
                responseBody = response.body();
                execute();

            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.d(Code.LOG_TAG, "FAILED GET KFTC DATA : " + t.getMessage());
                if (retryCount ++ <Code.KFTC_DOWNLOAD_TOTAL_RETRIES) {
                    Log.d(Code.LOG_TAG, "Retrying...(" + retryCount + " out of " + Code.KFTC_DOWNLOAD_TOTAL_RETRIES+")");
                    retry();
                }
            }

            private void retry() {
                kftcCall.clone().enqueue(this);
            }
        });
    }

    // 파일의 라인 하나씩 받아와서 데이터베이스 넣는 작업 시작
    // 파일 총크기, 첫 라인길이로 전체 라인수를 알아내서 프로그래스바 현황 업데이트시 활용
    private void inputDataofKFTC(ResponseBody responseBody) {
        int curLine = 0;
        // 데이터베이스 insert 속도 개선 위해 트랜잭션 시작
        sqLiteDatabase.beginTransaction();
        try {
            int flag = 0;
            long totalSize = responseBody.contentLength();
            String line;


            InputStream is = responseBody.byteStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "euc-kr"));

            while ((line = bufferedReader.readLine()) != null) {
                if (flag == 0) {
                    int oneLineLen = line.getBytes("euc-kr").length;
                    totalLineCnt = (int)totalSize / oneLineLen;
                    Log.d(Code.LOG_TAG, "contentLength : " + totalSize + ", totalLineCnt : " + totalLineCnt);
                    flag = 1;
                }

                curLine++;

                // sql insert 시작
                dbHelper.insertToTable(sqLiteDatabase, line, curLine, false);

                // 프로그래스바 ui 업데이트
                publishProgress(curLine);

            } // end while
            dbHelper.insertToTable(sqLiteDatabase, line, curLine, true);
            Log.d(Code.LOG_TAG, " real total line count : " + curLine);

            sqLiteDatabase.setTransactionSuccessful();

            dbHelper.insertToTableLast(sqLiteDatabase);

            bufferedReader.close();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sqLiteDatabase.inTransaction())
                sqLiteDatabase.endTransaction();
        }
    }


    private class RetrofitFactoryKFTC {
        public RetrofitserviceKFTC create() {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Code.FKTC_DOWNLOAD_BASE_URL)
                    .client((getUnsafeOkHttpClient().build())) // SSL 우회
                    .build();
            return retrofit.create(RetrofitserviceKFTC.class);
        }
    }


    private interface RetrofitserviceKFTC {
        @GET("/common/download1.jsp")
        Call<ResponseBody> getKftcData(@Query("filename") String filename,
                                       @Query("sysfilename") String sysfilename,
                                       @Query("mode") String mode);
    }

    // 안전하지 않음으로 HTTPS 통과
    private OkHttpClient.Builder getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };
            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @SuppressLint("BadHostnameVerifier")
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }




}
