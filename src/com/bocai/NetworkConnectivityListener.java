
package com.bocai;

import android.content.*;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.util.*;


public class NetworkConnectivityListener
{
	public static final class State
    {

        public static State valueOf(String s)
        {
           
        	if (s.equalsIgnoreCase("UNKNOWN")) {
        		return UNKNOWN;
        	} else if (s.equalsIgnoreCase("CONNECTED")) {
        		return CONNECTED;
        	} else if (s.equalsIgnoreCase("NOT_CONNECTED")) {
        		return NOT_CONNECTED;
        	}
        	return null;
        }

        public static State[] values()
        {
            return (State[])$VALUES.clone();
        }

        private static final State $VALUES[];
        public static final State CONNECTED;
        public static final State NOT_CONNECTED;
        public static final State UNKNOWN;

        static 
        {
            UNKNOWN = new State("UNKNOWN", 0);
            CONNECTED = new State("CONNECTED", 1);
            NOT_CONNECTED = new State("NOT_CONNECTED", 2);
            $VALUES = new State[3];
            $VALUES[0] = UNKNOWN;
            $VALUES[1] = CONNECTED;
        }

        private State(String s, int i)
        {
          
        }
    }

    private class ConnectivityBroadcastReceiver extends BroadcastReceiver
    {

        public void onReceive(Context context, Intent intent)
        {
            if(!intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE") || !mListening)
            {
                StringBuilder stringbuilder = (new StringBuilder()).append("onReceived() called with ");
                String s = mState.toString();
                String s1 = stringbuilder.append(s).append(" and ").append(intent).toString();
                Log.w(LOG_TAG, s1);
                return;
            }
     
            if(intent.getBooleanExtra("noConnectivity", false))
            {
                NetworkConnectivityListener.this.mState = State.NOT_CONNECTED;
            } else
            {
                NetworkConnectivityListener.this.mState = State.CONNECTED;
            }

            NetworkConnectivityListener.this.mNetworkInfo = (NetworkInfo)intent.getParcelableExtra("networkInfo");
            NetworkConnectivityListener.this.mOtherNetworkInfo = (NetworkInfo)intent.getParcelableExtra("otherNetwork");
            NetworkConnectivityListener.this.mReason = intent.getStringExtra("reason");
            NetworkConnectivityListener.this.mIsFailover = intent.getBooleanExtra("isFailover", false);
            Iterator<Handler> iterator = mHandlers.keySet().iterator();
            do
            {
                if(!iterator.hasNext())
                    return;
                Handler handler = iterator.next();
                int j = ((Integer)mHandlers.get(handler)).intValue();
                Message message = Message.obtain(handler, j, NetworkConnectivityListener.this);
                handler.sendMessage(message);
            } while(true);
        }

        private ConnectivityBroadcastReceiver()
        {            
        	super();
        }

    }

    public NetworkConnectivityListener()
    {
    	mHandlers = new HashMap<Handler, Integer>();
    	mState = State.UNKNOWN;
    	mReceiver = new ConnectivityBroadcastReceiver();
    }

    public NetworkInfo getNetworkInfo()
    {
        return mNetworkInfo;
    }

    public NetworkInfo getOtherNetworkInfo()
    {
        return mOtherNetworkInfo;
    }

    public String getReason()
    {
        return mReason;
    }

    public State getState()
    {
        return mState;
    }

    public boolean isFailover()
    {
        return mIsFailover;
    }

    public void registerHandler(Handler handler, int i)
    {
        Integer integer = Integer.valueOf(i);
        mHandlers.put(handler, integer);
    }

    public void startListening(Context context)
    {
        if(!mListening)
        {
            mContext = context;
            IntentFilter intentfilter = new IntentFilter();
            intentfilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            mContext.registerReceiver(mReceiver, intentfilter);
            mListening = true;
        }
    }

    public void stopListening()
    {
        if(mListening)
        {  
            mContext.unregisterReceiver(mReceiver);
            mContext = null;
            mNetworkInfo = null;
            mOtherNetworkInfo = null;
            mIsFailover = false;
            mReason = null;
            mListening = false;
        }
    }

    public void unregisterHandler(Handler handler)
    {
        mHandlers.remove(handler);
    }

    private static final String LOG_TAG = "NetworkConnectivityListener";
    private Context mContext;
    private HashMap<Handler, Integer> mHandlers;
    private boolean mIsFailover;
    private boolean mListening;
    private NetworkInfo mNetworkInfo;
    private NetworkInfo mOtherNetworkInfo;
    private String mReason;
    private ConnectivityBroadcastReceiver mReceiver;
    private State mState;

}
