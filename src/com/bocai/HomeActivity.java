
package com.bocai;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.bocai.model.User;
import com.bocai.net.FileCache;
import com.bocai.net.NetworkUtility;
import com.bocai.util.Macros;
import com.bocai.util.ToastFire;

import java.text.DecimalFormat;
import java.util.*;

public class HomeActivity extends TabStackActivityGroup
{
    public static interface ActivityTitleSource
    {
        public abstract String getActivityTitle();
    }

    public static interface ToolbarItemSource
    {

        public abstract View[] getToolbarItems();
    }

    class ActivityResultRecord
    {
        Intent data;
        int requestCode;
        int resultCode;
    }

    public HomeActivity()
    {
        firstLaunch = true;
        handler = new Handler();
        stackToRestore = null;
        pendingActivityResult = null;
        appUpdateListener = new BocaiApplication.AppUpdateListener() {

            public void updateAvailable(final int newVersion, final String message, final boolean optional)
            {
                Runnable runnable = new Runnable() {

                    public void run()
                    {
                        HomeActivity.this.showAppUpdateDialog(newVersion, message, optional);
                    }
                };
                handler.post(runnable);
            }
        };
      
    }

    public static ImageButton makeCancelButton(final Activity activity, final Handler handler)
    {
    	Log.i("HomeActivity", "makeCancelButton method");
    	ImageButton imageButton = new ImageButton(activity);
        android.widget.LinearLayout.LayoutParams layoutParams = new android.widget.LinearLayout.LayoutParams(-1, -1);
        imageButton.setPadding(10, 10, 0, 10);
        imageButton.setLayoutParams(layoutParams);
        imageButton.setBackgroundDrawable(null);
        imageButton.setImageResource(R.drawable.button_cancel);
        android.view.View.OnClickListener onClickListener = new android.view.View.OnClickListener() {

            public void onClick(View view)
            {
                Runnable runnable = new Runnable() {

                    public void run()
                    {
                        ((HomeActivity)activity.getParent()).popNavigationStack();
                    }
                };
                handler.post(runnable);
            }
        };
        
        imageButton.setOnClickListener(onClickListener);
        return imageButton;
    }

    private void selectTab(int index)
    {
    	
    }

   private void showClearCacheDialog()
    {
     
	   FileCache.checkCache(true);
        long l = FileCache.cacheSize();

        DecimalFormat decimalFormat = (DecimalFormat)DecimalFormat.getInstance();
        decimalFormat.setMaximumFractionDigits(1);
        String s1;
        String s2;
        Object aobj[];
        String s3;
        android.app.AlertDialog.Builder builder;
        String s4;
        android.app.AlertDialog.Builder builder1;
        android.content.DialogInterface.OnClickListener onClickListener;
        if(l > 0x100000L)
        {
            StringBuilder stringbuilder = new StringBuilder();
            double d = (double)l / 1048576D;
            String s = decimalFormat.format(d);
            s1 = stringbuilder.append(s).append("MB").toString();
        } else
        if(l > 1024L)
        {
            StringBuilder stringbuilder1 = new StringBuilder();
            double d1 = (double)l / 1024D;
            String s5 = decimalFormat.format(d1);
            s1 = stringbuilder1.append(s5).append("KB").toString();
        } else
        {
            s1 = (new StringBuilder()).append(l).append(" bytes").toString();
        }
        s2 = getString(R.string.clear_cache_msg);
        aobj = new Object[1];
        aobj[0] = s1;
        s3 = String.format(s2, aobj);
        builder = new android.app.AlertDialog.Builder(this);
        s4 = getString(R.string.menu_clear_cache);
        builder1 = builder.setTitle(s4).setMessage(s3);
        onClickListener = new android.content.DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialoginterface, int i)
            {
                FileCache.getInstance().clear();
            	finish();
            }

        };
        
        builder1.setPositiveButton(getString(R.string.ok), onClickListener).setNegativeButton(getString(R.string.cancel_btn), null).show();
    }

    public Intent createAuthenticationIntent(boolean flag)
    {
    	Log.i("HomeActivity", "createAuthenticationIntent method");
    	Intent intent = new Intent(this, com.bocai.AuthenticationActivity.class);
        if(flag)
        intent.putExtra("loadSpot", flag);
        intent.setFlags(0x20000000);
        return intent;
    }

    public Intent createSpotIntent()
    {
    	Log.i("HomeActivity", "createSpotItent method");
    	Intent intent = new Intent(this, com.bocai.SpotActivity.class);
        intent.setFlags(0x20000000);
        return intent;
    }

    public void hideKeyboard()
    {
    	Log.i("HomeActivity", "hideKeyboard method");
    	View view = getCurrentFocus();
        if(view == null)
            view = getWindow().getDecorView();
        if(view == null)
        {
            return;
        } else
        {
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService("input_method");
            android.os.IBinder ibinder = view.getWindowToken();
            inputMethodManager.hideSoftInputFromWindow(ibinder, 0);
        }
    }
    
   

    void loadHomeScreen()
    {
    	Log.i("HomeActivity", "loadHomeScreen method");
    	setContentView(R.layout.main);
        toolbar = (ViewGroup)findViewById(R.id.toolbar);
        View view = findViewById(R.id.title_logo);
        android.view.View.OnClickListener onClickListener = new android.view.View.OnClickListener() {

            public void onClick(View view1)
            {
                popNavigationStackToRoot();
            }
        };
        
        view.setOnClickListener(onClickListener);
        setUseTransitions(true);

        Log.i("Home", "after check...");
        Intent intent = new Intent(this, com.bocai.BrowseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        TabStackActivityGroup.TabSpec tabspec = (new TabStackActivityGroup.TabSpec("browse")).setRootIntent(intent);
        addTab(tabspec); 
        
        selectTab(0);
        configure();
        if(getLocalActivityManager().getCurrentId() != null)
            return;
        if(tabs == null)
        {
            return;
        } else
        {
            TabStackActivityGroup.TabSpec tabspec1 = tabs.get(currentTab);
            Intent intent2 = tabspec1.rootIntent;
            pushIntent(currentTab, intent2);
            return;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
    	Log.i("HomeActivity", "onActivityResult method");
    	String s = (new StringBuilder()).append("onActivityResult(").append(requestCode).append(", ").append(resultCode).append(", ").append(intent).append(")").toString();
        Log.d("Home", s);
        String s1 = Macros.FS_DEFAULT_GET_STRING("NAV_STACK");
        if(s1 != null && s1.length() > 0)
        {
            String as[] = TextUtils.split(s1, ",");
            stackToRestore = as;
            Macros.FS_DEFAULT_REMOVE("NAV_STACK");
            pendingActivityResult = new ActivityResultRecord();
            pendingActivityResult.requestCode = resultCode;
            pendingActivityResult.resultCode = resultCode;
            pendingActivityResult.data = intent;
            String s2 = (new StringBuilder()).append("onActivityResult: Need to restore stack: ").append(s1).toString();
            Log.d("Home", s2);
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    public void onCreate(Bundle bundle)
    {
    	Log.i("HomeActivity", "onCreate method");
    	super.onCreate(bundle);
        getWindow().setBackgroundDrawable(null);
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
    	Log.i("HomeActivity", "onCreateOptionsMenu method");
    	super.onCreateOptionsMenu(menu);
        menu.add(0, LOGIN_JOIN_MENU, 0, R.string.menu_login).setIcon(R.drawable.ic_menu_login);
        menu.add(0, FEED_BACK_MENU, 0, R.string.menu_feedback);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
    	Log.i("HomeActivity", "onOptionsItemSelected method");
    	String s = (new StringBuilder()).append("onOptionsItemSelected(").append(menuItem).append(")").toString();
    	Log.d("Home", s);
    	boolean bool = false;
    	String str = "onOptionsItemSelected(" + menuItem + ")";
    	Log.d("Home", str);
    	
    	if(menuItem.getItemId() == LOGIN_JOIN_MENU){
    	 	if(User.isLoggedIn()){
    	 		Macros.FS_APPLICATION().clearAccount("logout");
    	 	}
    	 	showAuthenticationActivity(false);
    	 	bool = true;
    	}else if (menuItem.getItemId() == FEED_BACK_MENU){
    		showClearCacheDialog();
//       	Uri localUri = Uri.parse(getString(R.string.feedback_url));   
//       	Intent localIntent = new Intent("android.intent.action.VIEW", localUri);  
//          startActivity(localIntent);
    		//NOTE: change the feedback menu to exit
    	
    	}else{
    		showClearCacheDialog();
    		bool = super.onOptionsItemSelected(menuItem);
    	}
         return bool;	
    }

    protected void onPause()
    {
    	Log.i("HomeActivity", "onPause method");
        super.onPause();
        Macros.FS_APPLICATION().pause();
    }

    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	Log.i("HomeActivity", "onPrepareOptionsMenu method");
    	if(User.isLoggedIn()) {
            menu.getItem(0).setTitle(R.string.menu_logout);
        }
        else {
            menu.getItem(0).setTitle(R.string.menu_login);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    protected void onResume()
    {
    	Log.i("HomeActivity", "onResume method");
    	TabStackActivityGroup.TabSpec localTabSpec;
        int l = 0;
        int i1 = 0;
        String str1;
        TabStackActivityGroup.ActivityRecord localActivityRecord1;
        Class<?> localObject;
        Log.d("Home", "onResume");
        BocaiApplication localBocaiApplication = Macros.FS_APPLICATION();
        localBocaiApplication.resume();
        super.onResume();             
        
        if (this.firstLaunch)
        {
          loadHomeScreen();
          this.firstLaunch = false;
        }
        if (this.stackToRestore != null)
        {
          localTabSpec = (TabStackActivityGroup.TabSpec)tabs.get(currentTab);
          String[] arrayOfString = this.stackToRestore;
          int k = arrayOfString.length;
          if (l < k){
            str1 = arrayOfString[l];
            int i2 = localTabSpec.navStack.size();
            if (i1 < i2)
            {
              localActivityRecord1 = (TabStackActivityGroup.ActivityRecord)localTabSpec.navStack.get(i1);
              if ((localActivityRecord1.activity != null) && (!(localActivityRecord1.activity.getClass().getName().equals(str1))))
                localObject = null;
            }
            
            try
            {
            	localObject = Class.forName(str1);
                if (localObject != null)
                {
                  String str2 = "onResume: Restoring stack entry: " + localObject;
                  Log.d("Home", str2);
                  Intent localIntent = new Intent(this,localObject);
                  localIntent.setFlags(536870912);
                  pushIntent(localIntent);
                }
                l += 1;
              localActivityRecord1 = (TabStackActivityGroup.ActivityRecord)localTabSpec.navStack.peek();
            }   catch (ClassNotFoundException localClassNotFoundException){
          	   String str3 = "onResume: Restoring stack, unable to get class for name '" + str1 + "'";
                 Log.e("Home", str3);
            }
          }
          
          if (this.pendingActivityResult != null)
          {
            TabStackActivityGroup.ActivityRecord localActivityRecord2 = (TabStackActivityGroup.ActivityRecord)localTabSpec.navStack.peek();
            if (localActivityRecord2.activity instanceof TabStackActivityGroup.ActivityResultListener)
            {
              TabStackActivityGroup.ActivityResultListener localActivityResultListener = (TabStackActivityGroup.ActivityResultListener)localActivityRecord2.activity;
              int requestCode = this.pendingActivityResult.requestCode;
              int resultCode = this.pendingActivityResult.resultCode;
              Intent localIntent3 = this.pendingActivityResult.data;
              localActivityResultListener.handleActivityResult(requestCode, resultCode, localIntent3);
            }
            this.pendingActivityResult = null;
          }
        }
        BocaiApplication.AppUpdateListener localAppUpdateListener = this.appUpdateListener;
        //localBocaiApplication.checkForAppUpdate(localAppUpdateListener);
        localBocaiApplication.checkForAppUpdate2(localAppUpdateListener,this);
        localBocaiApplication.setHasChecked(true);
    }
    
    
	public boolean popNavigationStack(int i, Intent intent)
    {
		Log.i("HomeActivity", "popNavigationStack method");
		hideKeyboard();
        boolean flag = super.popNavigationStack(i, intent);
        Activity activity = getLocalActivityManager().getCurrentActivity();
        if(activity != null && (activity instanceof ActivityTitleSource))
        {
            String s = ((ActivityTitleSource)activity).getActivityTitle();
            setTitle(s);
        } else
        {
            setTitle(null);
        }
        if(activity != null && (activity instanceof ToolbarItemSource))
        {
            View aview[] = ((ToolbarItemSource)activity).getToolbarItems();
            setToolbarItems(aview);
        }
        return flag;
    }
	
    public void popNavigationStackToRoot()
    {
    	Log.i("HomeActivity", "popNavigationStackToRoot method");
    	hideKeyboard();
        super.popNavigationStackToRoot();
        Activity activity = getLocalActivityManager().getCurrentActivity();
        if(activity != null && (activity instanceof ActivityTitleSource))
        {
            String s = ((ActivityTitleSource)activity).getActivityTitle();
            setTitle(s);
        } else
        {
            setTitle(null);
        }
        if(activity == null)
            return;
        if(!(activity instanceof ToolbarItemSource))
        {
            return;
        } else
        {
            View aview[] = ((ToolbarItemSource)activity).getToolbarItems();
            setToolbarItems(aview);
            return;
        }
    }

    public void pushIntent(int i, Intent intent)
    {
    	Log.i("HomeActivity", "pushIntent method");
    	super.pushIntent(i, intent, 0);
        Activity activity = getLocalActivityManager().getCurrentActivity();
        if(activity != null && (activity instanceof ActivityTitleSource))
        {
            String s = ((ActivityTitleSource)activity).getActivityTitle();
            setTitle(s);
        } else
        {
            setTitle(null);
        }
        if(activity == null)
            return;
        if(!(activity instanceof ToolbarItemSource))
        {
            return;
        } else
        {
            View aview[] = ((ToolbarItemSource)activity).getToolbarItems();
            setToolbarItems(aview);
            return;
        }
    }

    public void pushIntent(Intent intent)
    {
    	Log.i("HomeActivity", "pushIntent method");
    	super.pushIntent(intent);
        Activity activity = getLocalActivityManager().getCurrentActivity();
        if(activity != null && (activity instanceof ActivityTitleSource))
        {
            String s = ((ActivityTitleSource)activity).getActivityTitle();
            setTitle(s);
        } else
        {
            setTitle(null);
        }
        if(activity == null)
            return;
        if(!(activity instanceof ToolbarItemSource))
        {
            return;
        } else
        {
            View aview[] = ((ToolbarItemSource)activity).getToolbarItems();
            setToolbarItems(aview);
            return;
        }
    }

    public void replaceToolbar(View toolBarViews[])
    {
    	Log.i("HomeActivity", "replaceToolbar method");
    	int i = toolbar.getChildCount();
        toolbar.getChildAt(0).setVisibility(8);
        toolbar.getChildAt(1).setVisibility(8);
        ViewGroup viewgroup = toolbar;
        int j = i - 2;
        viewgroup.removeViews(2, j);
        if(toolBarViews == null)
            return;
        int k = toolBarViews.length;
        int l = 0;
        do
        {
            if(l >= k)
                return;
            View view = toolBarViews[l];
            toolbar.addView(view);
            l++;
        } while(true);
    }

    public void setTitle(String s)
    {
    	Log.i("HomeActivity", "setTitle method");
        if(s != null) {
            toolbar.setVisibility(8);
            TextView textview = (TextView)findViewById(R.id.title_text);
            textview.setText(s);
            textview.setVisibility(0);
        } else {
            toolbar.setVisibility(0);
            findViewById(R.id.title_text).setVisibility(8);
        }
    }

    public void setToolbarItems(View aview[])
    {
        int i = toolbar.getChildCount();
        ViewGroup viewgroup = toolbar;
        int j = i - 2;
        viewgroup.removeViews(2, j);
        if(aview != null)
        {
            View aview1[] = aview;
            int k = aview1.length;
            for(int l = 0; l < k; l++)
            {
                View view = aview1[l];
                toolbar.addView(view);
            }

        }
        toolbar.getChildAt(0).setVisibility(0);
        toolbar.getChildAt(1).setVisibility(0);
        toolbar.startLayoutAnimation();
    }

    public void showAuthenticationActivity(boolean flag)
    {
    	Log.i("HomeActivity", "showAuthenticationActivity method");
    	if(((TabStackActivityGroup.ActivityRecord)((TabStackActivityGroup.TabSpec)tabs.get(currentTab)).navStack.peek()).activity instanceof AuthenticationActivity)
        {
            return;
        } else
        {
            Intent intent = createAuthenticationIntent(flag);
            pushIntent(intent);
            return;
        }
    }

    public void showSpotActivity()
    {
    	Log.i("HomeActivity", "showSpotActivity method");
    	if(((TabStackActivityGroup.ActivityRecord)((TabStackActivityGroup.TabSpec)tabs.get(currentTab)).navStack.peek()).activity instanceof SpotActivity)
        {
            return;
        } else
        {
            Intent intent = createSpotIntent();
            pushIntent(intent);
            return;
        }
    }

/*    public void startActivityForResult(Intent intent, int i)
    {
    	Log.i("HomeActivity", "startActivityForResult method");
    	String s = (new StringBuilder()).append("startActivityForResult(").append(intent).append(",").append(i).append(")").toString();
        Log.d("Home", s);
        TabStackActivityGroup.TabSpec tabspec = (TabStackActivityGroup.TabSpec)tabs.get(currentTab);
        StringBuilder stringbuilder = new StringBuilder();
        for(Iterator<ActivityRecord> iterator = tabspec.navStack.iterator(); iterator.hasNext();)
        {
            String s1 = iterator.next().activity.getClass().getName();
            stringbuilder.append(s1).append(',');
        }

        int l = stringbuilder.length() - 1;
        if(stringbuilder.charAt(l) == ',')
            stringbuilder.setLength(l);
        String s2 = (new StringBuilder()).append("Serializing tab stack: '").append(stringbuilder).append("'").toString();
        Log.d("Home", s2);
        String s3 = stringbuilder.toString();
        Macros.FS_DEFAULT_SET_STRING("NAV_STACK", s3);
        super.startActivityForResult(intent, i);
    }*/
    
    private void showAppUpdateDialog(int i, String s, final boolean optional)
    {
        final Dialog dialog = new Dialog(this,optional,null) {

            public boolean onKeyDown(int j, KeyEvent keyevent)
            {
                boolean flag2;
                if(j == 4 && isShowing() && !optional)
                {
                    finish();
                    flag2 = false;
                } else
                {
                    flag2 = super.onKeyDown(j, keyevent);
                }
                return flag2;
            }
        };
        
        if(!optional) {
        	dialog.setCancelable(false);
        }
        dialog.requestWindowFeature(1);
        dialog.getWindow().getDecorView().setBackgroundResource(R.drawable.app_update_bg);
        View view = getLayoutInflater().inflate(R.layout.app_update_dialog, null);
        SpannableString spannablestring = new SpannableString(s);
        Linkify.addLinks(spannablestring, 15);
        ((TextView)view.findViewById(R.id.update_message)).setText(spannablestring);
        view.requestLayout();
        Button button = (Button)view.findViewById(R.id.updateButton);
        android.view.View.OnClickListener onclicklistener = new android.view.View.OnClickListener() {

            public void onClick(View view1)
            {
                Intent intent;
                String s1 = Macros.FS_APPLICATION().getUpdateURL();
                intent = new Intent("android.intent.action.VIEW");
                Uri uri = Uri.parse(s1);
                intent.setData(uri);
                startActivity(intent);
                
                setResult(-1);
                dialog.dismiss();
            }
        };
        
        button.setOnClickListener(onclicklistener);
        Button button1 = (Button)view.findViewById(R.id.cancelButton);
        if(!optional)
            button1.setText("Quit");
        android.view.View.OnClickListener onclicklistener1 = new android.view.View.OnClickListener() {

            public void onClick(View view1)
            {
                if(optional)
                {
                	dialog.cancel();
                    return;
                } else
                {
                    finish();
                    return;
                }
            }
        };
        button1.setOnClickListener(onclicklistener1);
        dialog.setContentView(view);
        dialog.show();
        TextView textview = (TextView)dialog.findViewById(R.id.update_message);
        android.text.method.MovementMethod movementmethod = LinkMovementMethod.getInstance();
        textview.setMovementMethod(movementmethod);
    }

    public static final int TAB_BROWSE = 0;
    public static final int TAB_SPOT = 1;
    static final int TOOLBAR_MODE_CANCEL = 1;
    ImageButton cancelButton;
    private boolean firstLaunch;
    private boolean exitLaunch = false;
    final Handler handler;
    ImageButton mapButton;
    private ActivityResultRecord pendingActivityResult;
    ImageButton searchButton;
    ImageButton spotButton;
    private String stackToRestore[];
    ViewGroup toolbar;
    private static final int LOGIN_JOIN_MENU = 1;
    private static final int FEED_BACK_MENU = 11;
    private BocaiApplication.AppUpdateListener appUpdateListener;

}
