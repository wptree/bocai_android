// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ReviewConfirmActivity.java

package com.bocai;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.webkit.*;

// Referenced classes of package com.bocai:
//            HomeActivity

public class ReviewConfirmActivity extends Activity {

	public static final String RESET_SPOT = "reset-spot";
	public static final String REVIEW_ID = "reviewID";
	android.view.View.OnClickListener addCallback;
	android.view.View.OnClickListener doneCallback;
	final Handler handler;
	View progress;
	String reviewID;
	View toolbarItems[];
	StringBuilder url;
	WebView webView;

	public ReviewConfirmActivity() {
		handler = new Handler();
		addCallback = new android.view.View.OnClickListener() {

			public void onClick(View view) {
				Runnable runnable = new Runnable() {

					public void run() {
						Intent intent = new Intent();
						intent.putExtra("reset-spot", true);
						((HomeActivity) getParent())
								.popNavigationStack(-1, intent);
					}
				};
				handler.post(runnable);
			}
		};
		doneCallback = new android.view.View.OnClickListener() {

			public void onClick(View view) {
				Runnable runnable = new Runnable() {

					public void run() {
						((HomeActivity) getParent()).popNavigationStackToRoot();
					}
				};
				handler.post(runnable);
			}
		};
	}

	void initWithReviewID(String s) {
		reviewID = s;
		StringBuilder stringbuilder1;
		String s1;
		Runnable runnable;
		if (url == null) {
			StringBuilder stringbuilder = new StringBuilder();
			url = stringbuilder;
		} else {
			url.setLength(0);
		}
		stringbuilder1 = url.append("http://www.bocai007.com/reviews/");
		s1 = reviewID;
		stringbuilder1.append(s1).append("/success.html5?client=android");
		runnable = new Runnable() {

			public void run() {
				if (webView == null) {
					return;
				} else {
					WebView webview = webView;
					String s2 = url.toString();
					webview.loadUrl(s2);
					return;
				}
			}
		};
		handler.post(runnable);
	}

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.review_conf);
		progress = findViewById(R.id.progress);
		webView = (WebView) findViewById(R.id.webview);
		webView.setVerticalScrollbarOverlay(true);
		webView.setBackgroundColor(0);
		webView.getSettings().setJavaScriptEnabled(true);
		WebViewClient webviewclient = new WebViewClient() {

			public void onPageFinished(WebView webview2, String s) {
				super.onPageFinished(webview2, s);
				progress.setVisibility(8);
			}

			public void onPageStarted(WebView webview2, String s, Bitmap bitmap) {
				super.onPageStarted(webview2, s, bitmap);
				progress.setVisibility(0);
			}
		};
		webView.setWebViewClient(webviewclient);
		ViewGroup viewgroup = (ViewGroup) getLayoutInflater().inflate(R.layout.review_conf_toolbar, null);
		toolbarItems = new View[viewgroup.getChildCount()];
		int i = 0;
		
		while (true) {
			if (viewgroup.getChildCount() <= 0)
				return;
			View view1 = viewgroup.getChildAt(0);
			viewgroup.removeView(view1);
			toolbarItems[i++] = view1;
			if (view1.getId() == R.id.btn_cancel) {
				view1.setOnClickListener(doneCallback);
			}
			if(view1.getId() == R.id.btn_add) {
		        view1.setOnClickListener(addCallback);
			}
		}
		/*
		 * _L2: View view1; int j; if(viewgroup.getChildCount() <= 0) return;
		 * view1 = viewgroup.getChildAt(0); viewgroup.removeView(view1); View
		 * aview1[] = toolbarItems; j = i + 1; aview1[i] = view1;
		 * if(view1.getId() != 0x7f08004f) break; Loop/switch isn't completed
		 * android.view.View.OnClickListener onclicklistener = doneCallback;
		 * view1.setOnClickListener(onclicklistener); _L4: i = j; if(true) goto
		 * _L2; else goto _L1 _L1: if(view1.getId() != 0x7f08000c) goto _L4;
		 * else goto _L3 _L3: android.view.View.OnClickListener onclicklistener1
		 * = addCallback; view1.setOnClickListener(onclicklistener1); goto _L4
		 */
	}

	public boolean onKeyDown(int i, KeyEvent keyevent) {
		boolean flag;
		if (i == 4)
			flag = false;
		else
			flag = super.onKeyDown(i, keyevent);
		return flag;
	}

	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	protected void onResume() {
		super.onResume();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			String s = bundle.getString("reviewID");
			if (s != null)
				initWithReviewID(s);
		}
		HomeActivity homeactivity = (HomeActivity) getParent();
		homeactivity.replaceToolbar(toolbarItems);
	}

}
