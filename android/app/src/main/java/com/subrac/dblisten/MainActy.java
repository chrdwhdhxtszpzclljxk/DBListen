package com.subrac.dblisten;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Set;

public class MainActy extends AppCompatActivity {

    static public String basePath;
    static public String un;
    static public String pwd;
    static public String userid;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acty_main);
        verifyStoragePermissions(this);
        String path = getFilesDir().getAbsolutePath();
        basePath = path;
        setupWebView();

    }


    void setupWebView(){
        WebView wv = (WebView)findViewById(R.id.mainwv);
        WebSettings wst = wv.getSettings();
        wst.setJavaScriptEnabled(true);
        wst.setAllowFileAccessFromFileURLs(true);
        wst.setAllowUniversalAccessFromFileURLs(true);
        wst.setJavaScriptCanOpenWindowsAutomatically(true);

        wv.setWebChromeClient(
                new WebChromeClient(){
                    @Override
                    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                        Uri uri = Uri.parse(message);
                        if ( uri.getScheme().equals("js")) {
                            if (uri.getAuthority().equals("cpp")) {
                                System.out.println("js调用了Android的方法" + message);
                                HashMap<String, String> params = new HashMap<>();
                                Set<String> collection = uri.getQueryParameterNames();
                                String acionData = uri.getQueryParameter("cmd");
                                String par1 = uri.getQueryParameter("arg1");

                                if(acionData.equals("toast")){
                                    Toast.makeText(MainActy.this, par1, Toast.LENGTH_LONG).show();
                                }else if(acionData.equals("play")){
                                    String type = uri.getQueryParameter("type");
                                    String tnow = uri.getQueryParameter("tnow");
                                    String pubid = uri.getQueryParameter("pubid");
                                    Intent intent = new Intent();
                                    //intent.setClass(MainActy.this, PlayActy.class);
                                    Bundle bundle=new Bundle();
                                    bundle.putString("type",type );
                                    bundle.putString("tnow",tnow );
                                    bundle.putString("pubid",pubid );
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                }else if(acionData.equals("login")){
                                    MainActy.un = uri.getQueryParameter("un");
                                    MainActy.pwd = uri.getQueryParameter("pwd");
                                    MainActy.userid = uri.getQueryParameter("userid");
                                    //GL2JNILib.setUnPwd(MainActy.un,MainActy.pwd,MainActy.userid);
                                }

                                result.confirm("js调用了Android的方法成功啦");
                            }
                            return true;
                        }
                        return super.onJsPrompt(view, url, message, defaultValue, result);
                    }

                    @Override
                    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                        return super.onJsAlert(view, url, message, result);
                    }

                    @Override
                    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                        return super.onJsConfirm(view, url, message, result);
                    }
                }
        );

        wv.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i("webview", "url = "+url);
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                //这里进行无网络或错误处理，具体可以根据errorCode的值进行判断，做跟详细的处理。
                view.loadUrl("file:///android_asset/www/error.html");
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                Log.i("onPageStarted", url);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i("onPageFinished", url);
                super.onPageFinished(view, url);
            }
        });



        wv.loadUrl("file:///android_asset/www/index.html");
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

}
