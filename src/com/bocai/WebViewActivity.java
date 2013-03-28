
package com.bocai;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.*;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WebViewActivity extends Activity implements HomeActivity.ToolbarItemSource {

	public static final String URL = "url";
	final Handler handler;
	final Runnable loadURL;
	TextView loadingMsg;
	ProgressBar progressBar;
	View toolbarItems[];
	String url;
	WebView webView;
	
	public WebViewActivity() {
		handler = new Handler();
		loadURL = new Runnable() {

			public void run() {
				if (webView == null) {
					return;
				} else {
					WebView webview = webView;
					webview.loadUrl(url);
					return;
				}
			}
		};
	}

	public View[] getToolbarItems() {
		return null;
	}

	void initWithURL(String paramString) {
		 this.url = paramString;
		 handler.post(loadURL);
	}

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.webview);
		progressBar = (ProgressBar) findViewById(R.id.progress);
		loadingMsg = (TextView) findViewById(R.id.loading_msg);
		webView = (WebView) findViewById(R.id.webview);
		webView.setVerticalScrollbarOverlay(true);
		webView.getSettings().setJavaScriptEnabled(true);
		WebViewClient webViewClient = new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				progressBar.setVisibility(8);
				loadingMsg.setVisibility(8);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				progressBar.setVisibility(0);
				loadingMsg.setText(url);
				loadingMsg.setVisibility(0);
			}
		};
		webView.setWebViewClient(webViewClient);
		WebChromeClient webChromeClient = new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				progressBar.setProgress(newProgress);
			}			
		};
		webView.setWebChromeClient(webChromeClient);
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean flag;
		if(keyCode == KeyEvent.KEYCODE_BACK){
		  if(webView != null && webView.canGoBack()){
		   webView.goBack();
		   flag = true;
		  }else{
			  flag = false;
		  }
		}else{
		  flag = super.onKeyDown(keyCode, event);
		}
		
		return flag;
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Bundle bundle = getIntent().getExtras();
		if (bundle == null)
			return;
		String s = bundle.getString("url");
		if (s == null) {
			return;
		} else {
			initWithURL(s);
			return;
		}
	}
}
