package com.bocai.util;

import android.content.Context;
import android.widget.Toast;

public class ToastFire {

	public static void fire(Context context, CharSequence text){
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}
		
	public static void fire(Context context, CharSequence text,int duration){
		Toast.makeText(context, text, duration).show();
	}
	
	public static void fire(Context context,int resourceId,int duration){
		Toast.makeText(context,resourceId,duration);
	}
	
	public static void fire(Context context,int resourceId){
		Toast.makeText(context,resourceId,Toast.LENGTH_SHORT);
	}
	
	
}
