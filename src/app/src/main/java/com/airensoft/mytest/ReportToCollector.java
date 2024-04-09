package com.airensoft.mytest;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

class ReportToCollector {
    private String _authorization = "Basic ZWxhc3RpYzo4RkRqd3M3bFlCcktjckFvOE84ag==";
    private String _url = "http://15.235.198.52:9200/mediacodec-test/_doc";

    public boolean POST(JSONObject body) {
        try {
            URL urlCon = new URL(_url);
            HttpURLConnection httpClient = (HttpURLConnection) urlCon.openConnection();

            httpClient.setRequestProperty("Accept", "application/json");
            httpClient.setRequestProperty("Content-type", "application/json");
            httpClient.setRequestProperty("Authorization", _authorization);

            // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
            httpClient.setDoOutput(true);
            // InputStream으로 서버로 부터 응답을 받겠다는 옵션.
            httpClient.setDoInput(true);

            OutputStream os = httpClient.getOutputStream();
            os.write(body.toString().getBytes());
            os.flush();

            // receive response as inputStream
            InputStream is = httpClient.getInputStream();

            Log.d(getClass().getName(), String.valueOf(httpClient.getResponseCode()));
            httpClient.disconnect();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}
