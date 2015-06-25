package com.rjfun.cordova.qq;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.rjfun.cordova.ext.CordovaPluginExt;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.open.utils.HttpUtils.HttpStatusException;
import com.tencent.open.utils.HttpUtils.NetworkUnavailableException;
import com.tencent.tauth.IRequestListener;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

@SuppressWarnings("deprecation")
public class QQPlugin extends CordovaPluginExt implements IUiListener, IRequestListener {
	private static final String LOGTAG = "QQPlugin";
	
    /** Cordova Actions. */
	public static final String ACTION_SET_OPTIONS = "setOptions";
	public static final String ACTION_SHARE = "share";

	public static int ERR_SUCCESS = 0;
    public static int ERR_CANCELLED = 1;
    public static int ERR_NOTINSTALLED = 2;
    public static int ERR_APPID = 3;
    public static int ERR_APPKEY = 4;
    public static int ERR_API = 5;
    public static int ERR_DATA = 6;
    public static int ERR_FAILED = 7;

    /* options */
	public static final String OPT_LICENSE = "license";
    public static final String OPT_IS_TESTING = "isTesting";
    public static final String OPT_LOG_VERBOSE = "logVerbose";

	public static final String OPT_APPID = "appId";
    public static final String OPT_APPKEY = "appKey";
    public static final String OPT_APPNAME = "appName";

	public static final String OPT_MESSAGE = "message";
    public static final String OPT_SUBJECT = "subject";
    public static final String OPT_IMAGE = "image";
	public static final String OPT_URL = "url";
    public static final String OPT_QQZONE = "qqZone";

    protected boolean inited = false;

    protected boolean licensed = false;
	protected boolean isTesting = false;
	protected boolean logVerbose = false;

    protected String appId = "";
    protected String appKey = "";
    protected String appName = "";

    protected Tencent mTencent = null;
	public static CallbackContext currentCallbackContext = null;

	protected String __getProductShortName() {
		return "QQ";
	}

    @Override
    public boolean execute(String action, JSONArray inputs, CallbackContext callbackContext) throws JSONException {
        PluginResult result = null;
        
    	if (ACTION_SET_OPTIONS.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            this.setOptions(options);
            result = new PluginResult(Status.OK);
            
        } else if (ACTION_SHARE.equals(action)) {
            JSONObject options = inputs.optJSONObject(0);
            if(options.length() > 1) {
                this.setOptions(options);
            }
            boolean isOk = this.share( options );

            // we send callback in qq callback
            currentCallbackContext = callbackContext;
            return true;

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
    	
        cordova.setActivityResultCallback(this);
	}
	
    public void setOptions(JSONObject options) {
    	Log.d(LOGTAG, "setOptions" );
    	
    	if(options != null) {
    		if(options.has(OPT_IS_TESTING)) this.isTesting = options.optBoolean(OPT_IS_TESTING);
    		if(options.has(OPT_LOG_VERBOSE)) this.logVerbose = options.optBoolean(OPT_LOG_VERBOSE);

            if(options.has(OPT_APPID)) this.appId = options.optString(OPT_APPID);
            if(options.has(OPT_APPKEY)) this.appKey = options.optString(OPT_APPKEY);
            if(options.has(OPT_APPNAME)) this.appName = options.optString(OPT_APPNAME);
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
    	
        if(! this.inited) {
            this.mTencent = Tencent.createInstance(this.appId, this.getActivity().getApplicationContext());
            this.inited = true;
    	}
        
        String message = args.optString(OPT_MESSAGE);
        String subject = args.optString(OPT_SUBJECT);
        String image = args.optString(OPT_IMAGE);
        String url = args.optString(OPT_URL);

        final boolean qqZone = args.optBoolean(OPT_QQZONE);
        final Bundle params = new Bundle();
        if(qqZone) {
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
            params.putString(QzoneShare.SHARE_TO_QQ_TITLE, subject);
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY,  message);
            params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL,  url);
            ArrayList<String> images = new ArrayList<String>();
            images.add( image );
            params.putStringArrayList(QQShare.SHARE_TO_QQ_IMAGE_URL, images);
            params.putString(QzoneShare.SHARE_TO_QQ_APP_NAME,  this.appName + "" + this.appId);

        } else {
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
            params.putString(QQShare.SHARE_TO_QQ_TITLE, subject);
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY,  message);
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  url);
            params.putString(QzoneShare.SHARE_TO_QQ_IMAGE_URL, image);
            params.putString(QQShare.SHARE_TO_QQ_APP_NAME,  this.appName + "" + this.appId);
            //params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,  0);
        }

        final Activity activity = this.getActivity();
        final IUiListener qqDelegate = this;
        activity.runOnUiThread(new Runnable(){
            @Override
            public void run() {
                if(qqZone) {
                    mTencent.shareToQzone(activity, params, qqDelegate);
                } else {
                    mTencent.shareToQQ(activity, params, qqDelegate);
                }
            }
        });

        return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    mTencent.onActivityResult(requestCode, resultCode, intent);
	    super.onActivityResult(requestCode, resultCode, intent);
	}

    @SuppressLint("DefaultLocale")
	protected void fireQQEvent(int errCode, String errStr) {
        if(currentCallbackContext != null) {
            PluginResult result = null;
            if((errCode == ERR_SUCCESS) || (errCode == ERR_CANCELLED)) {
                result = new PluginResult(Status.OK, (errCode == ERR_SUCCESS));
            } else {
                result = new PluginResult(Status.ERROR, errCode);
            }

            if(result != null) sendPluginResult(result, currentCallbackContext);
            currentCallbackContext = null;

        } else {
            String obj = __getProductShortName();
            String json = String.format("{'errCode':%d,'errStr':'%s'}", errCode, errStr);
            Log.d(LOGTAG, json);
            fireEvent(obj, "QQEvent", json);
        }
    }

	@Override
	public void onComplete(JSONObject arg0) {
		fireQQEvent(ERR_SUCCESS, "Complete");
	}

	@Override
	public void onComplete(Object arg0) {
		fireQQEvent(ERR_SUCCESS, "Complete");
	}

	@Override
	public void onCancel() {
		fireQQEvent(ERR_CANCELLED, "Cancelled");
	}

	@Override
	public void onConnectTimeoutException(ConnectTimeoutException arg0) {
		fireQQEvent(ERR_FAILED, "ConnectTimeout");
	}

	@Override
	public void onHttpStatusException(HttpStatusException arg0) {
		fireQQEvent(ERR_FAILED, "HttpStatusException");
	}

	@Override
	public void onIOException(IOException arg0) {
		fireQQEvent(ERR_FAILED, "IOException");
	}

	@Override
	public void onJSONException(JSONException arg0) {
		fireQQEvent(ERR_DATA, "JSONException");
	}

	@Override
	public void onMalformedURLException(MalformedURLException arg0) {
		fireQQEvent(ERR_DATA, "MalformedURL");
	}

	@Override
	public void onNetworkUnavailableException(NetworkUnavailableException arg0) {
		fireQQEvent(ERR_FAILED, "NetworkUnavailable");
	}

	@Override
	public void onSocketTimeoutException(SocketTimeoutException arg0) {
		fireQQEvent(ERR_FAILED, "SocketTimeout");
	}

	@Override
	public void onUnknowException(Exception arg0) {
		fireQQEvent(ERR_FAILED, "UnknowException");
	}

	@Override
	public void onError(UiError arg0) {
		fireQQEvent(ERR_FAILED, "Error");
	}
}
