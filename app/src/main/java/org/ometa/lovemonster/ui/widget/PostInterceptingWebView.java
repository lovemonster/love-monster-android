package org.ometa.lovemonster.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

import org.ometa.lovemonster.Logger;

/**
 * Extended {@link WebView} which
 */
public class PostInterceptingWebView extends WebView {

    private static final Logger logger = new Logger(PostInterceptingWebView.class);

    public PostInterceptingWebView(Context context) {
        super(context);
    }

    public PostInterceptingWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostInterceptingWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    @Override
    public void postUrl(String url, byte[] postData) {
        logger.debug("method=postUrl url=" + url + " postData=" + new String(postData));
//        super.postUrl(url, postData);
    }
}
