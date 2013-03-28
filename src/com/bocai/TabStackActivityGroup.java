
package com.bocai;

import android.app.*;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewAnimator;
import java.util.LinkedList;
import java.util.Stack;


public class TabStackActivityGroup extends ActivityGroup
{
    class ActivityRecord
    {
        Activity activity;
        String id;
        int requestCode;
        View view;
        
        public String toString()
        {
            return (new StringBuilder())
            .append("{")
            .append(id).append(", ")
            .append(view).append(",")
            .append(activity).append(",")
            .append(requestCode).append("}").toString();
        }
    }

    public class TabSpec
    {
        String id;
        Stack<ActivityRecord> navStack;
        Intent rootIntent;

        public TabSpec(String id)
        {
        	super();
            this.id = id;
            navStack = new Stack<ActivityRecord>();
        }
        
        public TabSpec setRootIntent(Intent intent)
        {
            rootIntent = intent;
            return this;
        }

        public String toString()
        {
            StringBuilder stringbuilder = (new StringBuilder()).append("{");
            stringbuilder.append(id).append(",");
            stringbuilder.append(rootIntent).append(",");
            return stringbuilder.append(navStack).append("}").toString();
        }
    }

    public static interface ActivityResultListener
    {

        public abstract void handleActivityResult(int requestCode, int returnCode, Intent intent);
    }


    public TabStackActivityGroup()
    {
        super(true);
        configured = false;
        useTransitions = false;
        currentTab = -1;
        animationsDisabled = false;
        intentCounter = 0;
    }

    private void usePopAnimations()
    {
        if(animationsDisabled)
        {
            ((ViewAnimator)content).setInAnimation(null);
            ((ViewAnimator)content).setOutAnimation(null);
            return;
        } else
        {
            ViewAnimator viewanimator = (ViewAnimator)content;
            viewanimator.setInAnimation(rightIn);
            viewanimator.setOutAnimation(rightOut);
            return;
        }
    }

    private void usePushAnimations()
    {
        if(animationsDisabled)
        {
            ((ViewAnimator)content).setInAnimation(null);
            ((ViewAnimator)content).setOutAnimation(null);
            return;
        } else{
            ViewAnimator viewanimator = (ViewAnimator)content;
            viewanimator.setInAnimation(leftIn);
            viewanimator.setOutAnimation(leftOut);
            return;
        }
    }

    public void addTab(TabSpec tabspec)
    {
    	Log.i(LOG_TAG + " -------addTab--------", tabspec.toString());
        if(tabs == null)
        {
        	tabs = new LinkedList<TabSpec>();
        }
        tabs.add(tabspec);
    }

    protected void configure()
    {
        currentTab = 0;
        content = (ViewGroup)findViewById(android.R.id.tabcontent);
        
        if(useTransitions)
        {
            if(!(content instanceof ViewAnimator)){
            	Log.w("TabStackActivityGroup", "configure: useTransitions set to true but view ID 'tabcontent' is not a kind of ViewAnimator!");
            }
            ((ViewAnimator)content).setAnimateFirstView(false);
            leftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
            leftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
            rightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
            rightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
            leftIn.setDuration(500L);
            leftOut.setDuration(500L);
            rightIn.setDuration(500L);
            rightOut.setDuration(500L);
        }
        configured = true;
    }

    public int getCurrentTab()
    {
        return currentTab;
    }

    public Activity getRootActivity()
    {
        return getRootActivity(currentTab);
    }

    public Activity getRootActivity(int index)
    {

    	if(index >=0 && index < tabs.size()){
    		TabSpec tabspec =tabs.get(index);
    		if(!(tabspec.navStack.empty())){
    			ActivityRecord activityRecord = tabspec.navStack.firstElement();
    			return activityRecord.activity;
    		}
    	}
    	return null;        
    }

    protected void onDestroy()
    {
        for(TabSpec tabspec = tabs.get(currentTab); !tabspec.navStack.empty();){
            tabspec.navStack.pop();
        	}
            tabs.clear();
        tabs = null;
        content = null;
        super.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent keyEvent)
    {
    	Log.i("TabStackActivityGroup -------onKeyDown-------- ", "onKeyDown " + keyCode + ", " + keyEvent);

    	  boolean bool = false;
    	    Activity localActivity = getCurrentActivity();
            Log.i("TabStackActivityGroup -------onKeyDown---0----- ", "" + localActivity);
    	    if ((localActivity != null) && (localActivity.onKeyDown(keyCode, keyEvent))){
    	    	bool = true;
    	    }
    	      
    	   if (keyCode != KeyEvent.KEYCODE_BACK){
   	          Log.i("TabStackActivityGroup -------onKeyDown---1----- ", "" + bool);
   	          return bool;
    	   }
    	    TabSpec localTabSpec = tabs.get(currentTab);
    	   
    	    if (localTabSpec.navStack.empty()){
        	        Log.i("TabStackActivityGroup -------onKeyDown---2----- ", "" + bool);
        	        
    	        	return bool;
    	    }
    	    
    	    if(isAtRoot()){
    	       
    	    	android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
	        	builder.setTitle(getString(R.string.exit_msg));
	        	builder.setMessage(getString(R.string.exit_confirm));
	        	builder.setPositiveButton(getString(R.string.ok), new ExitAlertDlgClickListenerImp() );
	    		builder.setNegativeButton(getString(R.string.cancel_btn), null);
	        	builder.create().show();
    	    }else{
    	    	  bool = popNavigationStack();
    	    }
    	    
    	    return bool;
    
    }
    
    private class ExitAlertDlgClickListenerImp implements OnClickListener{

		@Override
		public void onClick(DialogInterface dialog, int which) {
			finish();
		}
    	
    }
    
    private boolean isAtRoot(){
    	 TabSpec tabspec = tabs.get(currentTab);
    	 int size = tabspec.navStack.size();
    	 return size == 1;
    }
    
    protected void onPause()
    {
        super.onPause();
    }

    protected void onResume()
    {
        super.onResume();
        if(!configured){
           configure();
        }
        if(getLocalActivityManager().getCurrentId() != null)
            return;
        if(tabs == null)
        {
            return;
        } else
        {
        
            TabSpec tabspec = tabs.get(currentTab);
            Intent intent = tabspec.rootIntent;
            pushIntent(currentTab, intent, 0);
            return;
        }
    }

    public boolean popNavigationStack()
    {
        return popNavigationStack(0, null);
    }

    public boolean popNavigationStack(int resultCode, Intent intent)
    {
    	Log.i("TabStackActivityGroup","popNavigationStack");
    	boolean flag;
        TabSpec tabspec = tabs.get(currentTab);
        if(!tabspec.navStack.empty())
        {
        	Log.i("TabStackActivityGroup.popNavigationStack","tabspec.navStack not empty1");
        	tabspec.navStack.pop();
        
            if(tabspec.navStack.empty())
            {
            	Log.i("TabStackActivityGroup.popNavigationStack","tabspec.navStack empty2");
            	content.removeAllViews();
                flag = false;
            } else
            {
            	Log.i("TabStackActivityGroup.popNavigationStack","tabspec.navStack not empty");
            	ActivityRecord activityrecord = (ActivityRecord)tabspec.navStack.peek();
                LocalActivityManager localactivitymanager;
                String s;
                Intent intent1;
                if(useTransitions)
                {
                    usePopAnimations();
                    View view = ((ViewAnimator)content).getCurrentView();
                    ((ViewAnimator)content).showPrevious();
                    content.removeView(view);
                } else
                {
                    content.removeAllViews();
                    content.addView(activityrecord.view);
                }
                localactivitymanager = getLocalActivityManager();
                if(resultCode != 0 && intent != null && (activityrecord.activity instanceof ActivityResultListener))
                {
                	Log.i("TabStackActivityGroup.popNavigationStack","handleActivityResult");
                	ActivityResultListener activityresultlistener = (ActivityResultListener)activityrecord.activity;
                    int requestCode = activityrecord.requestCode;
                    activityresultlistener.handleActivityResult(requestCode, resultCode, intent);
                }
                s = activityrecord.id;
                intent1 = activityrecord.activity.getIntent();
            	Log.i("TabStackActivityGroup.popNavigationStack","" + intent1);
                localactivitymanager.startActivity(s, intent1);
                flag = true;
                // TODO temprorily
                //flag = false;
            }
        } else
        {
        	Log.i("TabStackActivityGroup.popNavigationStack","tabspec.navStack empty1");
        	flag = false;
        }
        return flag;
    }

    public void popNavigationStackToRoot()
    {
    	
    	Log.i(LOG_TAG, "popNavigationStackToRoot method");
    	
    	int i = ((ViewAnimator)content).getDisplayedChild() - 1;
    	Log.i(LOG_TAG, "i============" + i);
        TabSpec tabspec = tabs.get(currentTab);
        boolean flag = false;
        while(tabspec.navStack.size() > 1){
        	 tabspec.navStack.pop();
        	 if(tabspec.navStack.size() > 1){
        		 ((ViewAnimator)content).removeViewAt(i);
        		 i = i - 1;
        	 }
        	 flag = true;
        }
        if(!flag){
        	Log.i(LOG_TAG, "already in root,returned.");
        	return;
        }
        
        ActivityRecord activityrecord = (ActivityRecord)tabspec.navStack.peek();
        LocalActivityManager localactivitymanager;
        String s;
        Intent intent;

        if(useTransitions)
        {
            Log.i(LOG_TAG, "useTransitions====showPrevious");
        	usePopAnimations();
            View view = ((ViewAnimator)content).getCurrentView();
            ((ViewAnimator)content).showPrevious();
            content.removeView(view);
            Log.i(LOG_TAG, "remove view from content===" + view.toString());
            Log.i(LOG_TAG, "remove view from content===" + view.getId());
        } else
        {
        	Log.i(LOG_TAG,"not useTransitions==========addView" + activityrecord.view);
        	content.removeAllViews();
            content.addView(activityrecord.view);
        }
        
        Log.i(LOG_TAG, "=======Start ActivityRecord===" + activityrecord.toString());
        
        localactivitymanager = getLocalActivityManager();
        s = activityrecord.id;
        intent = activityrecord.activity.getIntent();
        localactivitymanager.startActivity(s, intent);
    }

    public void pushIntent(int index, Intent intent, int j)
    {
        Log.i("TabStackActivityGroup", "pushIntent method");
    	TabSpec tabspec =tabs.get(index);
        StringBuilder stringbuilder = new StringBuilder();
        String s = tabspec.id;
        StringBuilder stringbuilder1 = stringbuilder.append(s);
        int k = intentCounter;
        int l = k + 1;
        intentCounter = l;
        String s1 = stringbuilder1.append(k).toString();
        Window window = getLocalActivityManager().startActivity(s1, intent);
        if(window == null){
        	 Log.i("TabStackActivityGroup", "window is null");
        	return;
        }
        ActivityRecord activityrecord = new ActivityRecord();
        activityrecord.id = s1;
        activityrecord.view = window.getDecorView();
        Activity activity = getLocalActivityManager().getCurrentActivity();
        activityrecord.activity = activity;
        activityrecord.requestCode = j;
        tabspec.navStack.push(activityrecord);
        
        Log.i(LOG_TAG, "=======Push ActivityRecord===" + activityrecord.toString());
        
        if(index == currentTab){
            if(useTransitions)
            {
                usePushAnimations();
                content.addView(activityrecord.view);
                ((ViewAnimator)content).showNext();
                Log.w("TabStackActivityGroup", "showNext.....");
            } else
            {
                content.addView(activityrecord.view);
            }
        }
        window.getDecorView().requestFocus(2);
       
    }

    public void pushIntent(Intent intent)
    {
        pushIntent(currentTab, intent, 0);
    }

    public void pushIntentForResult(Intent intent, int i)
    {
        pushIntent(currentTab, intent, i);
    }

    public void setCurrentTab(int i)
    {
        int j = currentTab;
        if(i != j)
            return;
        currentTab = i;
        animationsDisabled = true;
        content.removeAllViews();
        TabSpec tabspec = tabs.get(i);
        if(tabspec.navStack.empty())
        {
            Intent intent = tabspec.rootIntent;
            pushIntent(i, intent, 0);
            animationsDisabled = false;
            return;
        }
        ActivityRecord activityrecord1;
        LocalActivityManager localactivitymanager;
        String s;
        Intent intent1;
        if(useTransitions)
        {
            usePushAnimations();
            int k = tabspec.navStack.size();
            for(int l = 0; l < k; l++)
            {
                ActivityRecord activityrecord = (ActivityRecord)tabspec.navStack.get(l);
                ViewGroup viewgroup = content;
                View view = activityrecord.view;
                viewgroup.addView(view);
            }

            ViewAnimator viewanimator = (ViewAnimator)content;
            int i1 = k - 1;
            viewanimator.setDisplayedChild(i1);
        } else
        {
            ViewGroup viewgroup1 = content;
            View view1 = ((ActivityRecord)tabspec.navStack.peek()).view;
            viewgroup1.addView(view1);
        }
        activityrecord1 = (ActivityRecord)tabspec.navStack.peek();
        localactivitymanager = getLocalActivityManager();
        s = activityrecord1.id;
        intent1 = activityrecord1.activity.getIntent();
        localactivitymanager.startActivity(s, intent1);
        animationsDisabled = false;
    }

    public void setUseTransitions(boolean flag)
    {
        useTransitions = flag;
    }
     
    private static final String LOG_TAG = "TabStackActivityGroup";
    public static final String ROOT_ACTIVITY = "root";
    public static final String USE_TRANSITIONS = "useTransitions";
    public static final int VIEW_TRANSITION_DURATION = 500;
    protected boolean animationsDisabled;
    protected boolean configured;
    ViewGroup content;
    protected int currentTab;
    private int intentCounter;
    Animation leftIn;
    Animation leftOut;
    Animation rightIn;
    Animation rightOut;
    LinkedList<TabSpec> tabs;
    protected boolean useTransitions;
}
