package com.rjfun.cordova.qq;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.rjfun.cordova.ext.CordovaPluginExt;

public class LLPayPlugin extends CordovaPluginExt {
	private static final String LOGTAG = "LLPayPlugin";
	
    /** Cordova Actions. */
	public static final String ACTION_SET_OPTIONS = "setOptions";
	public static final String ACTION_SHARE = "share";

    /* options */
	public static final String OPT_LICENSE = "license";
    public static final String OPT_IS_TESTING = "isTesting";
    public static final String OPT_LOG_VERBOSE = "logVerbose";

    protected boolean licensed = false;
	protected boolean isTesting = false;
	protected boolean logVerbose = false;

	protected String __getProductShortName() {
		return "LLPay";
	}

    @Override
    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {
        PluginResult result = null;
        
    	if (ACTION_SET_OPTIONS.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            this.setOptions(options);
            result = new PluginResult(Status.OK);
            
        } else if (ACTION_SHARE.equals(action)) {
            JSONObject args = inputs.optJSONObject(0);
        	
        	boolean isOk = this.share( args );
        	result = new PluginResult(isOk ? Status.OK : Status.ERROR);
            
        } else {
            Log.w(LOGTAG, String.format("Invalid action passed: %s", action));
            result = new PluginResult(Status.INVALID_ACTION);
        }
        
        if(result != null) sendPluginResult(result, callbackContext);
        
        return true;
    }
    
    @Override
    protected void pluginInitialize() {
    	super.pluginInitialize();
    	
	}
	
    public void setOptions(JSONObject options) {
    	Log.d(LOGTAG, "setOptions" );
    	
    	if(options != null) {
    		if(options.has(OPT_LICENSE)) validateLicense(options.optString(OPT_LICENSE));
    		if(options.has(OPT_IS_TESTING)) this.isTesting = options.optBoolean(OPT_IS_TESTING);
    		if(options.has(OPT_LOG_VERBOSE)) this.logVerbose = options.optBoolean(OPT_LOG_VERBOSE);
    	}
    }
    
    @SuppressLint("DefaultLocale")
	private void validateLicense(String license) {
    	String[] fields = license.split("/");
    	if(fields.length >= 2) {
        	String userid = fields[0];	
        	String key = fields[1];
        	String genKey2 = this.md5( __getProductShortName().toLowerCase() + " licensed to " + userid + " by floatinghotpot" );
        	licensed = key.equalsIgnoreCase(genKey2);
    	}
    	
    	if(licensed) {
    		Log.w(LOGTAG, "valid license");
    	}
    }

    public final String md5(final String s) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
        }
        return "";
    }
	
	public boolean share(JSONObject args) {
    	Log.d(LOGTAG, "share" );
    	
    	if(! licensed) {
    		Date now = new Date();
    		
    		DateFormat format = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
    		try {
				Date expireDate = format.parse("01-10-2015");
				if(now.compareTo(expireDate) > 0) {
					Log.w(LOGTAG, "trial expired, need a license");
					return false;
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	String content4Pay = args.toString();
    	MobileSecurePayer msp = new MobileSecurePayer();
        boolean bRet = msp.pay(content4Pay, mHandler, Constants.RQF_PAY, this.getActivity(), false);
    	
    	return bRet;
	}
	
	public void firePayEndEvent(final String strRet) {
	    final Activity activity = getActivity();
	    activity.runOnUiThread(new Runnable(){
            @Override
            public void run() {
            	Log.d(LOGTAG, "onLLPayEnd: " + strRet );
            	fireEvent("LLPay","onLLPayEnd", "{\"ret\":"+strRet+"}");
            }
	    });
	}
	
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            String strRet = (String) msg.obj;
            switch (msg.what) {
                case Constants.RQF_PAY: {
                	firePayEndEvent( strRet );
                }
                break;
            }
            super.handleMessage(msg);
        }
    };

}
