package com.library.libcamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class WebPageActivity extends AppCompatActivity {
    private WebView wvWeb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view_activity);
        wvWeb = findViewById(R.id.wvWeb);
        initialize();
    }

    @Override
    public void onBackPressed() {
        if (wvWeb.canGoBack()) {
            wvWeb.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            if (resultCode == Activity.RESULT_OK) {
                wvWeb.evaluateJavascript(
                        "javascript: " + "updateFromNative(\"" + CacheImage.getCacheImage(this) + "\")",
                        null
                );
            }
        }
    }

    private void initialize() {
        wvWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        wvWeb.getSettings().setJavaScriptEnabled(true);
        wvWeb.getSettings().setAllowFileAccess(true);
        wvWeb.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        wvWeb.getSettings().setUseWideViewPort(true);
        wvWeb.getSettings().setGeolocationEnabled(true);
        wvWeb.getSettings().setAppCacheEnabled(true);
        wvWeb.getSettings().setDatabaseEnabled(true);
        wvWeb.getSettings().setDomStorageEnabled(true);
        wvWeb.addJavascriptInterface(new WebVCamBridgeInterface(), "Android");
        String url = getIntent().getStringExtra("url");
        if (url == null || url.isEmpty()) {
            wvWeb.loadUrl("http://35.202.109.216/camera");
            return;
        }
        wvWeb.loadUrl(url);
    }

    class WebVCamBridgeInterface {
        @JavascriptInterface
        public void showAndroidCamera(String jsonString) {
            String afterReplace = jsonString.replace("\\", "");
            String finalJsonString = afterReplace.substring(1, afterReplace.length() - 1);
            JSONObject jo = null;
            try {
                jo = new JSONObject(finalJsonString);
                int isFrontCamera = jo.getInt("isFrontCamera");
                String instruction = jo.getString("title");
                Intent intent = new Intent(WebPageActivity.this, CameraActivity.class);
                intent.putExtra("isFrontCamera", isFrontCamera);
                intent.putExtra("instruction", instruction);
                startActivityForResult(intent, 200);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }
}
