
package com.bocai.util;

import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;

public class DebugUtilities
{

    public DebugUtilities()
    {
    }

    public static void collectAndSendLog(final Context context)
    {
        PackageManager packagemanager = context.getPackageManager();
        final Intent intent = new Intent("com.xtralogic.logcollector.intent.action.SEND_LOG");
        boolean flag;
        if(packagemanager.queryIntentActivities(intent, 0x10000).size() > 0)
            flag = true;
        else
            flag = false;
        if(!flag)
        {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
            String s = context.getString(0x7f090000);
            android.app.AlertDialog.Builder builder1 = builder.setTitle(s).setIcon(0x108009b).setMessage("Install the free and open source Log Collector application to collect the device log and send it to the developer.");
            android.content.DialogInterface.OnClickListener onclicklistener = new android.content.DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialoginterface, int i)
                {
                    Uri uri = Uri.parse("market://search?q=pname:com.xtralogic.android.logcollector");
                    Intent intent1 = new Intent("android.intent.action.VIEW", uri);
                    intent1.addFlags(0x10000000);
                    context.startActivity(intent1);
                }

                //final Context val$context;

            
            {
                //context = context1;
                //super();
            }
            }
;
            builder1.setPositiveButton(0x104000a, onclicklistener).setNegativeButton(0x1040000, null).show();
            return;
        } else
        {
            android.app.AlertDialog.Builder builder2 = new android.app.AlertDialog.Builder(context);
            String s1 = context.getString(0x7f090000);
            android.app.AlertDialog.Builder builder3 = builder2.setTitle(s1).setIcon(0x108009b).setMessage("Run Log Collector application.\nIt will collect the device log and send it to <support email>.\nYou will have an opportunity to review and modify the data being sent.");
            android.content.DialogInterface.OnClickListener onclicklistener1 = new android.content.DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialoginterface, int i)
                {
                    intent.addFlags(0x10000000);
                    intent.putExtra("com.xtralogic.logcollector.intent.extra.SEND_INTENT_ACTION", "android.intent.action.SENDTO");
                    Uri uri = Uri.parse("mailto:bugs@bocai.com");
                    intent.putExtra("com.xtralogic.logcollector.intent.extra.DATA", uri);
                    intent.putExtra("com.xtralogic.logcollector.intent.extra.ADDITIONAL_INFO", "Additonal info: <additional info from the device (firmware revision, etc.)>\n");
                    intent.putExtra("android.intent.extra.SUBJECT", "Application failure report");
                    intent.putExtra("com.xtralogic.logcollector.intent.extra.FORMAT", "time");
                    Context context1 = context;
                    Intent intent8 = intent;
                    context1.startActivity(intent8);
                }

                //final Context val$context;
               // final Intent val$intent;

            
            {
            	//super();
                //intent = intent1;
                //context = context1;
                
            }
            }
;
            builder3.setPositiveButton(0x104000a, onclicklistener1).setNegativeButton(0x1040000, null).show();
            return;
        }
    }

    public static final String ACTION_SEND_LOG = "com.xtralogic.logcollector.intent.action.SEND_LOG";
    public static final String EXTRA_ADDITIONAL_INFO = "com.xtralogic.logcollector.intent.extra.ADDITIONAL_INFO";
    public static final String EXTRA_BUFFER = "com.xtralogic.logcollector.intent.extra.BUFFER";
    public static final String EXTRA_DATA = "com.xtralogic.logcollector.intent.extra.DATA";
    public static final String EXTRA_FILTER_SPECS = "com.xtralogic.logcollector.intent.extra.FILTER_SPECS";
    public static final String EXTRA_FORMAT = "com.xtralogic.logcollector.intent.extra.FORMAT";
    public static final String EXTRA_SEND_INTENT_ACTION = "com.xtralogic.logcollector.intent.extra.SEND_INTENT_ACTION";
    public static final String EXTRA_SHOW_UI = "com.xtralogic.logcollector.intent.extra.SHOW_UI";
    public static final String LOG_COLLECTOR_PACKAGE_NAME = "com.xtralogic.android.logcollector";
//  private static final String SUPPORT_EMAIL = "bugs@bocai.com";
}
