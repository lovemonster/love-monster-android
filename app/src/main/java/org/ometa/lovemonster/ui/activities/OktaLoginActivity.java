package org.ometa.lovemonster.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.ometa.lovemonster.Logger;
import org.ometa.lovemonster.R;
import org.ometa.lovemonster.service.LoveMonsterClient;
import org.ometa.lovemonster.service.NetworkHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides authentication support for Okta.
 */
public class OktaLoginActivity extends AppCompatActivity {

    private static final Logger logger = new Logger(OktaLoginActivity.class);

    private WebView webView;
    private ProgressBar progressBar;
    private RelativeLayout webViewContent;
    private View networkUnavailableMessage;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_okta_login);

        progressBar = (ProgressBar) findViewById(R.id.okta_login_progress_bar);

        webView = (WebView) findViewById(R.id.okta_login_web_view);
        webViewContent = (RelativeLayout) findViewById(R.id.okta_login_container);
        networkUnavailableMessage = findViewById(R.id.okta_login_network_unavailable_message);

        if (NetworkHelper.isUp(this)) {
            networkUnavailableMessage.setVisibility(View.GONE);
        } else {
            networkUnavailableMessage.setVisibility(View.VISIBLE);
        }

        webView.setVisibility(View.GONE);
        webView.setWebViewClient(new OktaAuthenticationWebView(this));
        webView.getSettings().setJavaScriptEnabled(true);

        progressBar.setVisibility(View.VISIBLE);
        webView.loadUrl(LoveMonsterClient.getInstance().getRootUrl());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        webView.setWebViewClient(null);
        webViewContent.removeAllViews();
        webView.destroy(); // http://stackoverflow.com/a/8949378/82156
    }

    /**
     * Extended {@link WebViewClient} which interacts with the okta web view.
     */
    private class OktaAuthenticationWebView extends WebViewClient {

        private final Activity activity;

        public OktaAuthenticationWebView(final Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onPageFinished(final WebView view, final String url) {
            if (url.startsWith(LoveMonsterClient.getInstance().getRootUrl())) {
                final String rawCookies = CookieManager.getInstance().getCookie(url);
                if (rawCookies != null) {
                    final String authCookies = parseOktaAuthCookies(rawCookies);
                    LoveMonsterClient.getInstance().authenticate(authCookies, new LoveMonsterClient.AuthenticationHandler() {
                        @Override
                        public void onSuccess() {
                            final Intent intent = new Intent(activity, LoveListActivity.class);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onFail(final List<String> errorMessages) {
                            final String errorMessage;
                            if (errorMessages.isEmpty()) {
                                errorMessage = "";
                            } else {
                                errorMessage = errorMessages.get(0);
                            }
                            onNetworkError(errorMessage);
                        }

                        @Override
                        public void onAuthenticationFailure() {
                            onFail(new ArrayList<String>());
                        }
                    });


                }
            } else {
                webView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            super.onPageFinished(view, url);
        }

        /**
         * Parses all cookies, and returns a cookies string with only the okta auth cookies.
         *
         * @param cookies
         *      all cookies
         * @return
         *      a cookies string (formated for the HTTP "Cookie" header) with only okta auth cookies
         */
        private String parseOktaAuthCookies(final String cookies) {
            final StringBuilder cookieBuilder = new StringBuilder();
            for (final String cookie : cookies.split(";")) {
                if (isOktaAuthCookie(cookie)) {
                    cookieBuilder.append(cookie).append(";");
                }
            }

            return cookieBuilder.toString();
        }

        /**
         * Determines if a cookie is needed for Okta authentication.
         *
         * @param cookie
         *      the individual cookie
         * @return
         *      true if the cooke is for okta auth, and false otherwise
         */
        private boolean isOktaAuthCookie(final String cookie) {
            final String trimmedCookie = cookie.trim();

            return trimmedCookie.startsWith("SimpleSAMLAuthToken=")
                    || trimmedCookie.startsWith("SimpleSAMLSessionID=")
                    || trimmedCookie.startsWith("og_cookie=")
                    || trimmedCookie.startsWith("ogwall-pcookie=");
        }

        @Override
        public void onReceivedError(final WebView view, final WebResourceRequest request, final WebResourceError error) {
            super.onReceivedError(view, request, error);
            onNetworkError(error.toString());
        }

    }

    /**
     * Invoked on a network error. Used to message the user that something went wrong.
     */
    private void onNetworkError(final String error) {
        logger.debug("method=onReceivedError error=" + error.toString());
        webView.loadUrl("about:blank");

        progressBar.setVisibility(View.GONE);
        networkUnavailableMessage.setVisibility(View.VISIBLE);

        Toast.makeText(
                this,
                getString(R.string.okta_login_error_message),
                Toast.LENGTH_LONG
        ).show();
    }

}
