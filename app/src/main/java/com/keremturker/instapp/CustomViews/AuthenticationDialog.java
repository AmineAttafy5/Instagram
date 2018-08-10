package com.keremturker.instapp.CustomViews;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.keremturker.instapp.AuthenticationListener;
import com.keremturker.instapp.Constants;
import com.keremturker.instapp.R;

public class AuthenticationDialog extends Dialog {

    private AuthenticationListener authenticationListener;
    private Context context;
    private WebView webView;

    private final String url = Constants.BASE_URL
            + "oauth/authorize/?client_id="
            + Constants.INSTAGRAM_CLIENT_ID
            + "&redirect_uri="
            + Constants.REDIRECT_URI
            + "&response_type=token"
            + "&display=touch&scope=public_content";

    public AuthenticationDialog(@NonNull Context context, AuthenticationListener listener) {
        super(context);
        this.authenticationListener = listener;
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_dialog);
        initializeWebview();

    }

    private void initializeWebview() {

        webView = (WebView) findViewById(R.id.webview);
        Log.d("url", url);
        webView.setWebViewClient(new WebViewClient() {

            String access_token;
            boolean authComplete;

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //need to check  here
                if (url.contains("#access_token") && !authComplete) {
                    Uri uri = Uri.parse(url);
                    access_token = uri.getEncodedFragment();
                    //get the whole token  after '=' sign
                    access_token = access_token.substring(access_token.lastIndexOf("=") + 1);
                    Log.d("access_token", access_token);
                    authComplete = true;
                    authenticationListener.onCodeReceived(access_token);
                    dismiss();
                } else if (url.contains("?error")) {
                    Log.d("access_token", "getting error fetch access token");
                    Log.d("response", "" + url);
                    dismiss();

                }
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }
}
