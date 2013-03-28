package com.bocai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.bocai.util.Macros;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.webkit.URLUtil;

public class Updater {
	private static final String TAG = Updater.class.getName();
	private static final String VER_JSON = "verson.json";
	private static final String APK_TYPE = "application/vnd.android.package-archive";

	private String host;
	private Context context;
	private ProgressDialog progressDialog;
	private Handler handler = new Handler();
	private String apkFullName = null;
	private int versionCode = 0;
	private String versionName;
	private String updateDir;
	private String updateURL;
	
	public Updater(String host, Context context) {
		this(host, null, context);
	}

	public Updater(String host, String path, Context context) {
		this.host = host;
		this.context = context;
		this.updateDir = (path == null) ? Environment.getExternalStorageDirectory() + "/updates/" : Environment.getExternalStorageDirectory() + path;
	}

	public String getVesionName() {
		String versionName = null;
		try {
			versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}

		return versionName;
	}

	public int getVersionCode() {
		int versionCode = 0;
		try {
			versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}

		return versionCode;
	}

	public int getRemoteVersionCode() {
		if (versionCode == 0) {
			try {
				getRemoteJSON();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}
		return this.versionCode;
	}

	public String getRemoteVersionName() {
		if (this.versionName == null) {
			try {
				getRemoteJSON();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}
		return this.versionName;
	}

	public String getRemoteFileName() {
		if (this.apkFullName == null) {
			try {
				getRemoteJSON();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		}

		return this.apkFullName;
	}

	public boolean needUpdate() {
		return (getRemoteVersionCode() > getVersionCode());
	}

	public void showNewVersionUpdate() {
/*		String message = String.format("%s: %s, %s", "Found a new version", getRemoteFileName() + " " + this.getRemoteVersionCode(), "need update?");
		AlertDialog dialog = new AlertDialog.Builder(context).setTitle("Software Update").setMessage(message)
		// update
				.setPositiveButton("Update", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						progressDialog = new ProgressDialog(context);
						progressDialog.setTitle("Downloading update");
						progressDialog.setMessage("Please wait a moment");
						progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						dialog.dismiss();
						download(getUpdateURL());
					}
					// cancel
				}).setNegativeButton("Not now", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				}).create();
		dialog.show();*/
		String message = String.format("%s: %s, %s", context.getString(R.string.found_newversion), getRemoteVersionName(), context.getString(R.string.need_update));
		AlertDialog dialog = new AlertDialog.Builder(context).setTitle(context.getString(R.string.alertdialog_title)).setMessage(message)
		// update
				.setPositiveButton(context.getString(R.string.alertdialog_update_button), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						progressDialog = new ProgressDialog(context);
						progressDialog.setTitle(context.getString(R.string.progressdialog_title));
						progressDialog.setMessage(context.getString(R.string.progressdialog_message));
						progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						dialog.dismiss();
						download(getUpdateURL());
					}
					// cancel
				}).setNegativeButton(context.getString(R.string.alertdialog_cancel_button), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.dismiss();
					}
				}).create();
		dialog.show();
	}

	private void getRemoteJSON() throws ClientProtocolException, IOException, JSONException {
		//String url = host + VER_JSON;
		//String url = "www.bocai007.com/bocai/client/android/bocai_android.apk";
		String url = "http://www.bocai007.com/app_update.json?client=android";
		if (URLUtil.isHttpUrl(url)) {
			StringBuilder sb = new StringBuilder();
			HttpClient client = new DefaultHttpClient();
			HttpParams httpParams = client.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
			HttpConnectionParams.setSoTimeout(httpParams, 5000);
			HttpResponse response = client.execute(new HttpGet(url));
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"), 8192);

				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				reader.close();
			}

			JSONObject object = (JSONObject) new JSONTokener(sb.toString()).nextValue();
			this.apkFullName = object.getString("ApkFullName");
			//this.apkFullName = "bocai_android.apk";
			this.versionName = object.getString("VersionName");
			//this.versionName = "0.0.3";
			this.versionCode = Integer.valueOf(object.getInt("version"));
			this.updateURL = object.getString("url");
		}
/*		this.apkFullName = "com.bocai007";
		this.versionName = "0.0.3";
		this.versionCode = 1;*/
	}

	private String getUpdateURL() {
		//return this.host + this.apkFullName;
		//return "http://www.bocai007.com/bocai/client/android/bocai_android.apk";
		return updateURL;
	}

	private void download(final String url) {
		progressDialog.show();
		new Thread() {
			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				HttpResponse response;
				try {
					response = client.execute(get);
					HttpEntity entity = response.getEntity();
					long length = entity.getContentLength();
					InputStream is = entity.getContent();
					FileOutputStream fileOutputStream = null;
					if (is != null) {
						File f = new File(updateDir);
						if (!f.exists()) {
							f.mkdirs();
						}
						fileOutputStream = new FileOutputStream(new File(updateDir, getRemoteFileName()));

						byte[] buf = new byte[1024];
						int ch = -1;
						int count = 0;
						while ((ch = is.read(buf)) != -1) {
							fileOutputStream.write(buf, 0, ch);
							count += ch;
							Log.d(TAG, String.valueOf(count));
							if (length > 0) {
							}
						}
					}
					fileOutputStream.flush();
					if (fileOutputStream != null) {
						fileOutputStream.close();
					}

					handler.post(new Runnable() {
						public void run() {
							progressDialog.cancel();
							installUpdate();
						}
					});
				} catch (Exception e) {
					progressDialog.cancel();
					Log.e(TAG, e.getMessage());
				}
			}

		}.start();
	}

	private void installUpdate() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(updateDir, getRemoteFileName())), APK_TYPE);
		context.startActivity(intent);
        Macros.FS_DEFAULT_SET_LONG("FSAppUpdateTime", System.currentTimeMillis());
	}
}
