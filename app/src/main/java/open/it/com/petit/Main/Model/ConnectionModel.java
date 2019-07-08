package open.it.com.petit.Main.Model;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import open.it.com.petit.Connection.ConnectCode;
import open.it.com.petit.Main.Callback.ConnectionCallback;
import open.it.com.petit.R;

/**
 * Created by user on 2017-11-10.
 */

// Runnable : 쓰레드 class
public class ConnectionModel<T> implements Runnable {
    private final static String TAG = ConnectionModel.class.getSimpleName();

    private OkHttpClient okHttpClient; // 서버 연결을 위한 객체
    private Context context;

    private List list; // 결과를 받을 List객체
    private Class<T[]> cls; // 제네릭 data class의 배열
    private LinkedHashMap<String, Object> data; // post를 위한 hashmap
    private String url;
    private String php;
    private String queryString;
    private String method; // get or post 구분자

    private int statusCode = ConnectCode.HTTP_NOT_FOUND; // 결과 code
    private ConnectionCallback callback;

    public ConnectionModel(Context context, ConnectionCallback callback) {
        this.context = context;
        this.okHttpClient = new OkHttpClient();
        this.callback = callback;
        this.url = context.getString(R.string.db_host);
    }

    // Thread start method 호출 시 불러지는 method
    @Override
    public void run() {
        if (method.equals(""))
            return ;
        switch (method) {
            case "GET":
                get();
                break;
            case "POST":
                post();
                break;
        }
    }

    // get 방식 method
    private void get() {
        Log.d(TAG, "GET");
        if (cls == null)
            return;
        if (queryString.equals(""))
            return;

        // 요청 객체. 일단 url을 담아놓음.
        Request request = new Request.Builder()
                .url(url + php + "?" + queryString).build();
        Response response = null;
        try {
            // 요청을 불러 실행을 시켜 응답을 받는다.
            response = okHttpClient.newCall(request).execute();
            statusCode = response.code();

            // response code가 404 에러나 타임아웃이면 종료
            if (response.code() == ConnectCode.HTTP_NOT_FOUND || response.code() == ConnectCode.HTTP_CLIENT_TIMEOUT) {
                callback.onHttpFailure();
                return ;
            }

            String jsonData = response.body().string(); // 서버에서 받아온 json string
            Gson gson = new GsonBuilder().create(); // json메세지 파싱을 위한 객체
            T[] arr = gson.fromJson(jsonData, cls); // data class에 파싱을 하여 맵핑한다
            Log.d(TAG, jsonData);
            list = Arrays.asList(arr); // data class의 결과를 list에 넣음.
            callback.onGetSuccess(); // get 방식이 성공했다고 알리는 callback
        } catch (IOException e) {
            e.printStackTrace();
            callback.onHttpFailure(); // 연결에 실패했다고 알리는 callback
        } finally {
            response.body().close();
        }
    }

    private void post() {
        Log.d(TAG, "POST");
        //post에 실어 나르는 data를 담는 객체
        FormBody.Builder builder = new FormBody.Builder();
        String key[] = new String[data.size()]; // hashmap 에서의 key 배열
        Object values[] = new Object[data.size()]; // hashmap 에서의 value 배열

        int i = 0;
        // data entrySet : hash안에 담긴 모든 데이터.
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            key[i] = entry.getKey(); // hash의 key를 가져와 넣음.
            values[i ++] = entry.getValue(); // hash의 value를 가져와 넣음.
        }

        // formbody에 파싱된 값을 넣는다.
        for (i = 0 ; i < data.size() ; i ++) {
            builder.add(key[i], values[i].toString());
        }

        Log.d(TAG, url + php);
        RequestBody body = builder.build();
        Request request = new Request.Builder().url(url + php)
                .post(body).build();
        Response response = null;

        try {
            response = okHttpClient.newCall(request).execute();
            statusCode = response.code();
            Log.d(TAG, "status Code " + statusCode);
            if (response.code() == ConnectCode.HTTP_NOT_FOUND || response.code() == ConnectCode.HTTP_CLIENT_TIMEOUT) {
                callback.onHttpFailure();
                return ;
            }

            callback.onPostSuccess(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
            callback.onHttpFailure();
        } finally {
            response.body().close();
        }
    }

    public ConnectionModel<T> init() {
        this.list = null;
        this.cls = null;
        this.data = null;
        this.php = null;
        this.queryString = null;
        this.method = null;
        return this;
    }

    public ConnectionModel<T> setMethod(String method) {
        this.method = method;
        return this;
    }

    public ConnectionModel<T> setClass(Class<T[]> cls) {
        this.cls = cls;
        return this;
    }

    public ConnectionModel<T> setHash(LinkedHashMap<String, Object> data) {
        this.data = data;
        return this;
    }

    public ConnectionModel<T> setPhp(String php) {
        this.php = php;
        return this;
    }

    public ConnectionModel<T> setQueryString(String queryString) {
        this.queryString = queryString;
        return this;
    }

    public <T> List<T> getResult() {
        return list;
    }

    public int getStatus() {
        return statusCode;
    }
}