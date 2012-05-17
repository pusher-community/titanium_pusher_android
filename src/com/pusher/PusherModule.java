/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package com.pusher;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.util.TiConvert;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.emorym.android_pusher.Pusher;

@Kroll.module(name="Pusher", id="com.pusher")
public class PusherModule extends KrollModule
{
	// private static final boolean DBG = TiConfig.LOGD;
	private Handler mHandler;
	public Pusher mPusher;
	private String mPusherKey;
	public PusherAPI mPusherAPI;
	private Boolean mReconnectAutomatically;
	private Integer mReconnectDelay;

	// You can define constants with @Kroll.constant, for example:
	// @Kroll.constant public static final String EXTERNAL_NAME = value;
	
	public PusherModule() {
		super();
	}
	
	@Override
	public void onDestroy(Activity activity) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				PusherModule.this.disconnect();
			}
		});
	}

	// Methods
	@Kroll.method
	public void setup(@SuppressWarnings("rawtypes") HashMap map) {
    @SuppressWarnings("unchecked")
	  KrollDict args = new KrollDict(map);

		mPusherKey = args.getString("key");
		mReconnectAutomatically = args.optBoolean("reconnectAutomatically", true);
		mReconnectDelay = args.optInt("reconnectDelay", 5);
		
		if(mPusherKey == null || mPusherKey.length() == 0) {
			throw new RuntimeException("Pusher key is required");
		}
		
		String appID = args.optString("appID", null);
		String secret = args.optString("secret", null);
		if(appID != null && secret != null) {
			mPusherAPI = new PusherAPI(mPusherKey, appID, secret);
		}
	}
	
	@Kroll.method(runOnUiThread=true)
	public void connect() {
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				
				Bundle bundleData = msg.getData();
				if(bundleData.getString("type").contentEquals("pusher")) {
					try {
						JSONObject message = new JSONObject(bundleData.getString("message"));
						Log.d("Pusher Message", message.toString());
						
						if(message.getString("event").equals("connection_established")) {
							PusherModule.this.fireEvent("connected", null);
						}
						
						if(PusherModule.this.hasListeners(message.getString("event"))) {
							KrollDict event = new KrollDict();
							event.put("name", message.getString("event"));
							
							JSONObject data = new JSONObject(message.getString("data"));
							event.put("data", KrollDict.fromJSON(data));
							
							PusherModule.this.fireEvent(message.getString("event"), event);
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				
				if(bundleData.getString("type").contentEquals("pusher:disconnected")) {
					PusherModule.this.fireEvent("disconnected", null);
				}
			}
		};
		
		mPusher = new Pusher(mReconnectAutomatically, mReconnectDelay);
		mPusher.addHandler(mHandler);
		mPusher.connect(mPusherKey);
	}
	
	@Kroll.method(runOnUiThread=true)
	public void disconnect() {
		if(mPusher != null) {
			mPusher.disconnect();
		}
	}
	
	@Kroll.method(runOnUiThread=true)
	public ChannelProxy subscribeChannel(String channel) {
		ChannelProxy channelProxy = new ChannelProxy();
		channelProxy.configure(this, channel);
		
		return channelProxy;
	}
	
	@Kroll.method
	public void sendEvent(String eventName, String channelName, Object data) throws org.json.JSONException {
    JSONObject jsonData = new JSONObject(TiConvert.toString(data));

		if(mPusherAPI != null) {
			mPusherAPI.triggerEvent(eventName, channelName, jsonData, mPusher.mSocketId);
		} else {
			Log.w("Pusher", "PusherAPI not configured because of missing appID or secret");
		}
	}
}
