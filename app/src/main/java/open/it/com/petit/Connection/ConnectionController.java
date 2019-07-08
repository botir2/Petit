package open.it.com.petit.Connection;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import open.it.com.petit.R;

/**
 * Created by user on 2017-11-10.
 */

public class ConnectionController<T> implements Runnable {
    private final static String TAG = ConnectionController.class.getSimpleName();

    private OkHttpClient okHttpClient;
    private Context context;
    private Handler handler;

    private List list;
    private Class<T[]> cls;
    private LinkedHashMap<String, Object> data;
    private String url;
    private String method;

    private int statusCode = ConnectCode.HTTP_NOT_FOUND;

    public ConnectionController(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        this.okHttpClient = new OkHttpClient();
    }

    public ConnectionController(Context context) {
        this.context = context;
        this.okHttpClient = new OkHttpClient();
    }

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

    private void get() {
        Log.d(TAG, "GET");
        if (cls == null)
            return;
        if (url.equals(""))
            return;
        Request request = new Request.Builder()
                .url(url).build();
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            statusCode = response.code();

            if (response.code() == ConnectCode.HTTP_NOT_FOUND || response.code() == ConnectCode.HTTP_CLIENT_TIMEOUT) {
                if (handler != null)
                    handler.sendMessage(handler.obtainMessage(ConnectCode.HTTP_NOT_FOUND));
                return ;
            }

            String jsonData = response.body().string();
            Gson gson = new GsonBuilder().create();
            T[] arr = gson.fromJson(jsonData, cls);
            Log.d(TAG, jsonData);
            list = Arrays.asList(arr);
        } catch (IOException e) {
            e.printStackTrace();
            if (handler != null)
                handler.sendMessage(handler.obtainMessage(ConnectCode.HTTP_NOT_FOUND));
        } finally {
            response.body().close();
        }
    }

    private void post() {
        Log.d(TAG, "POST");
        FormBody.Builder builder = new FormBody.Builder();
        String key[] = new String[data.size()];
        Object values[] = new Object[data.size()];

        int i = 0;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            key[i] = entry.getKey();
            values[i ++] = entry.getValue();
        }

        for (i = 0 ; i < data.size() ; i ++) {
            builder.add(key[i], values[i].toString());
        }

        Log.d(TAG, context.getString(R.string.db_host) + url);
        RequestBody body = builder.build();
        Request request = new Request.Builder().url(context.getString(R.string.db_host) + url)
                .post(body).build();
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            statusCode = response.code();
            if (response.code() == ConnectCode.HTTP_NOT_FOUND || response.code() == ConnectCode.HTTP_CLIENT_TIMEOUT) {
                if (handler != null)
                    handler.sendMessage(handler.obtainMessage(ConnectCode.HTTP_NOT_FOUND));
                return ;
            }
            if (handler != null)
                handler.sendMessage(handler.obtainMessage(ConnectCode.HTTP_OK));
        } catch (IOException e) {
            e.printStackTrace();
            if (handler != null)
                handler.sendMessage(handler.obtainMessage(ConnectCode.HTTP_NOT_FOUND));
        } finally {
            response.body().close();
        }
    }

    public ConnectionController<T> setMethod(String method) {
        this.method = method;
        return this;
    }

    public ConnectionController<T> setClass(Class<T[]> cls) {
        this.cls = cls;
        return this;
    }

    public ConnectionController<T> setHash(LinkedHashMap<String, Object> data) {
        this.data = data;
        return this;
    }

    public ConnectionController<T> setUrl(String url) {
        this.url = url;
        return this;
    }

    public <T> List<T> getResult() {
        return list;
    }

    public int getStatus() {
        return statusCode;
    }
}