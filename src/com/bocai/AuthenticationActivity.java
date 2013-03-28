package com.bocai;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import org.json.JSONException;
import org.json.JSONObject;

import com.bocai.net.AsyncHTTPRequest;
import com.bocai.net.AsyncHTTPResponseHandler;
import com.bocai.util.RestConstants;
import com.bocai.util.ToastFire;
import com.bocai.model.User;
import com.bocai.util.Macros;
import com.bocai.R;

public class AuthenticationActivity extends Activity implements AsyncHTTPResponseHandler,HomeActivity.ToolbarItemSource
{
	
	    public static final String LOAD_SPOT_ON_COMPLETE = "loadSpot";
	    private static final String LOG_TAG = "AuthActivity";
	    static final int MODE_JOIN = 1;
	    static final int MODE_LOGIN = 0;
	    public static final String RETURN_AUTH_USER = "returnUser";
	    private static final String EMAIL_REG_EXP = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	    ViewFlipper flipper;
	    final Handler handler;
	    final Runnable hideProgress;
	    EditText joinConfirmField;
	    EditText joinEmailField;
	    EditText joinPassField;
	    boolean loadSpotControllerOnComplete;
	    EditText loginEmailField;
	    EditText loginPassField;
	    int mode = 0;
	    android.widget.RadioGroup.OnCheckedChangeListener modeCallback;
	    ProgressDialog progressDialog;
	    boolean returnAuthenticatedUser;
	    final Runnable showPreviousActivity;
	    final Runnable showSpotActivity;
	    Button submitJoinButton;
	    Button submitLoginButton;
	    android.widget.TextView.OnEditorActionListener textFieldActionListener;
	
    public AuthenticationActivity()
    {
    	loadSpotControllerOnComplete = false;
        returnAuthenticatedUser = false;
        mode = 0;
        handler = new Handler();
   
        showSpotActivity = new Runnable() {

            public void run()
            {
            	Log.w(LOG_TAG, "showSpotActivity");
               HomeActivity homeactivity = (HomeActivity)getParent();
               homeactivity.popNavigationStack();
               homeactivity.showSpotActivity();
            }
        };
     
		hideProgress = new Runnable() {

            public void run()
            {
                if(progressDialog == null)
                {
                    return;
                } else
                {
                    progressDialog.dismiss();
                    return;
                }
            }
        };
       
		showPreviousActivity = new Runnable() {

            public void run()
            {
              ((HomeActivity)getParent()).popNavigationStack();
            	Log.w("Auth", "showPreviousActivity");
            }
        };
      
		modeCallback = new android.widget.RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {	
				flipper.setDisplayedChild(BUTTON_ID_TO_MODE(checkedId));
			}
        };
        
	  textFieldActionListener = new android.widget.TextView.OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView view, int actionId,KeyEvent event) {
					if(event != null){
					  if(event.getAction() == KeyEvent.KEYCODE_SOFT_LEFT){
					     if(mode ==1 && view == loginPassField){
					    	 submitForm();
					  }
					     return true;
					}
				}
					return false;
			}
        };
    }
    
    private int BUTTON_ID_TO_MODE(int paramInt)
    {
     
    	if(paramInt == 2131230737){
    	  mode = 1;
    	}else if(paramInt == 2131230738){
    	  mode = 0;
    	}else{
    	  mode = 1;
    	}
    	return mode;
    }


    void configureAnimations()
    {
        Animation inAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        inAnimation.setDuration(200L);
        flipper.setInAnimation(inAnimation);
        Animation outAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        outAnimation.setDuration(100L);
        flipper.setOutAnimation(outAnimation);
    }

    void displayErrors(JSONObject jsonObject){
    	
    	final String error =  jsonObject.optString("errorMsg"); 

    	    Runnable runnable = new Runnable()
    	    {
    	      public void run()
    	      {
    	    	  android.app.AlertDialog.Builder builder = (new android.app.AlertDialog.Builder(AuthenticationActivity.this)).setTitle(getString(R.string.whoops));
    	    	  builder.setMessage(error).setPositiveButton(getString(R.string.try_again), null).show();      
    	      }
    	    };
    	    handler.post(runnable);
    }

    void doFinish()
    {
        if(loadSpotControllerOnComplete)
        {
            handler.post(showSpotActivity);
        } else{
            if(!returnAuthenticatedUser);
            handler.post(showPreviousActivity);
        }
    }

    void hideKeyboard()
    {
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
            return;
        }
    }

    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.auth);
        joinEmailField = (EditText)findViewById(R.id.edit_email);
        joinPassField = (EditText)findViewById(R.id.edit_password);
        joinConfirmField = (EditText)findViewById(R.id.edit_confirm);
        loginEmailField = (EditText)findViewById(R.id.edit_login_email);
        loginPassField = (EditText)findViewById(R.id.edit_login_password);
        submitJoinButton = (Button)findViewById(R.id.btn_submit_join);
        submitLoginButton = (Button)findViewById(R.id.btn_submit_login);
        flipper = (ViewFlipper)findViewById(R.id.flipper);
  
        joinConfirmField.setOnEditorActionListener(textFieldActionListener);
      
        loginPassField.setOnEditorActionListener(textFieldActionListener);
       
        android.view.View.OnClickListener joinListener = new android.view.View.OnClickListener() {

            public void onClick(View view)
            {
                submitForm();
            }
        };
		submitJoinButton.setOnClickListener(joinListener);
        
        android.view.View.OnClickListener loginListener = new android.view.View.OnClickListener() {

            public void onClick(View view)
            {
                submitForm();
            }
        };
		submitLoginButton.setOnClickListener(loginListener);
        RadioGroup radiogroup = (RadioGroup)findViewById(R.id.grp_login_join);
        radiogroup.setOnCheckedChangeListener(modeCallback);
        configureAnimations();
    }

    @Override
    //Called when a key was pressed down and not handled by any of the views inside of the activity.
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_BACK){
    	  return false;
    	}
		return super.onKeyDown(keyCode, event);
	}
    
    @Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	protected void onPause()
    {
        if(progressDialog != null)
            progressDialog.dismiss();
        super.onPause();
    }

	protected void onResume()
    {
        super.onResume();
        loadSpotControllerOnComplete = false;
        returnAuthenticatedUser = false;
        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
        {
        	loadSpotControllerOnComplete = bundle.getBoolean("loadSpot");
        	returnAuthenticatedUser = bundle.getBoolean("returnUser");
        }
    }

    void showProgress(String message)
    {
        if(progressDialog == null)
        {
        	progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(true);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    void submitForm()
    {
    	 hideKeyboard();
         String url;
         AsyncHTTPRequest asyncHttpRequest;
         if(mode == MODE_LOGIN){
        	  url = RestConstants.LOGIN_URL;
         }else{
        	  url = RestConstants.JOIN_URL;
         }
        
         asyncHttpRequest = new AsyncHTTPRequest(url);
         asyncHttpRequest.setTimeout(20000);
         asyncHttpRequest.responseHandler = this;
         asyncHttpRequest.requestMethod = AsyncHTTPRequest.POST_METHOD;
         if(mode == MODE_LOGIN)
         {
             String email = loginEmailField.getText().toString();
             if(email == null || !email.matches(EMAIL_REG_EXP)){
            	 ToastFire.fire(this, getString(R.string.invalid_email));
            	 return;
             }
             asyncHttpRequest.addPostParam("email", email);
             String password = loginPassField.getText().toString();
             if(password == null || password.trim().length() < 3){
            	 ToastFire.fire(this, getString(R.string.pass_length));
            	 return;
             }
             asyncHttpRequest.addPostParam("password", password);
             asyncHttpRequest.addPostParam("remeberMe", "true");
             String login_progress = getString(R.string.login_progress);
             showProgress(login_progress);
           
             
         } else
         {
             String email = joinEmailField.getText().toString();
             if(email == null || !email.matches(EMAIL_REG_EXP)){
            	 ToastFire.fire(this, getString(R.string.invalid_email));
            	 return;
             }
             asyncHttpRequest.addPostParam("email", email);
             String password = joinPassField.getText().toString();
             if(password == null || password.trim().length() < 3){
            	 ToastFire.fire(this,  getString(R.string.pass_length));
            	 return;
             }
             asyncHttpRequest.addPostParam("password", password);
             String password_confirm = joinConfirmField.getText().toString();
             if(!password_confirm.equals(password)){
            	 ToastFire.fire(this, getString(R.string.pass_ne));
            	 return;
             }
             asyncHttpRequest.addPostParam("passConfirm", password_confirm);
             String join_progress = getString(R.string.join_progress);
             showProgress(join_progress);
             
         }
         
         asyncHttpRequest.execute();
    }

    void saveAccount(JSONObject jsonObject){
    	
    String email = jsonObject.optString("email");
    Macros.FS_DEFAULT_SET_STRING("email", email);
    String password;
    User user;
    if(mode == 0)
    	password = joinPassField.getText().toString();
    else
    	password = loginPassField.getText().toString();
    Macros.FS_DEFAULT_SET_STRING("password", password);
    user = new User(jsonObject);
    User.archiveUser(user);
    if(returnAuthenticatedUser)
    {
        doFinish();
        return;
    }
    if(loadSpotControllerOnComplete)
    {
        handler.post(showSpotActivity);
    } else
    {
        doFinish();
        return;
    }
}
    
    @Override
	public void handleError(String errorMsg, InputStream stream, long length)
			throws IOException {
		
    	 final String error = errorMsg;
    	  Runnable runnable = new Runnable() {
  			
	            public void run()
	            {
	                if(progressDialog != null)
	                    progressDialog.dismiss();	                
	                android.app.AlertDialog.Builder builder = (new android.app.AlertDialog.Builder(AuthenticationActivity.this)).setTitle(R.string.network_error);
	                builder.setMessage(error).setPositiveButton(R.string.try_again, null).show();
	            }
	        };
		handler.post(runnable);
	}
    
	@Override
	public void handleResponse(AsyncHTTPRequest asyncHttpRequest,
			InputStream stream, long length) throws IOException {
		handler.post(hideProgress);
		String s = AsyncHTTPRequest.toString(stream, length);
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(s);
			jsonObject.put("cookies", asyncHttpRequest.responseCookies);
			boolean success = jsonObject.getBoolean("success");
			if(success){
				saveAccount(jsonObject);
			}else{
				 displayErrors(jsonObject);
			}
		} catch (JSONException e) {
			StringBuilder stringBuilder = (new StringBuilder()).append("Error parsing authentication response: ");
            stringBuilder.append(e.getLocalizedMessage());
            Log.e("AuthActivity", stringBuilder.toString(), e);
            e.printStackTrace();
		}
	
	}	
	
	@Override
	public View[] getToolbarItems() {
		return null;
	}
}
