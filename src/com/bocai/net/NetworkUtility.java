package com.bocai.net;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtility {

	public static boolean networkConnected(Context context){
		 ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		 NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		    for (NetworkInfo ni : netInfo) {
		    	if(ni.isConnected()){
		    		continue;
		    	}else{
		    		return false;
		    	}
		    }
		 
		 return true;
	}
	
   public static Intent createNetworkSettingIntent(){
	   Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
	   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	   return intent;
   }
	
	
}
