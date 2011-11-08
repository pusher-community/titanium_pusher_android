package com.pusher;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.util.Log;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

@Kroll.proxy(creatableInModule = PusherModule.class)
public class ChannelProxy extends KrollProxy
{
	private PusherModule mPusherModule;
	private String channelName;
	private Handler mHandler;
	
	public ChannelProxy(TiContext context) {
		super(context);
	}
	
	public void configure(PusherModule _pusherModule, String _channelName) {
		mPusherModule = _pusherModule;
		channelName = _channelName;
	
		mPusherModule.mPusher.subscribe(channelName);
		
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				
				Bundle bundleData = msg.getData();
				if(bundleData.getString("type").contentEquals("pusher") && bundleData.getString("channel") != null && bundleData.getString("channel").equals(channelName)) {
					try {
						JSONObject message = new JSONObject(bundleData.getString("message"));
						
						KrollDict event = new KrollDict();
						event.put("name", message.getString("event"));
						event.put("channel", message.getString("channel"));
						
						JSONObject data = new JSONObject(message.getString("data"));
						event.put("data", data);
						
						if(ChannelProxy.this.hasListeners(message.getString("event"))) {
							ChannelProxy.this.fireEvent(message.getString("event"), event);
						}
						
						if(ChannelProxy.this.hasListeners("event")) {
							ChannelProxy.this.fireEvent("event", event);
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		mPusherModule.mPusher.addHandler(mHandler);
	}
	
	@Kroll.method(runOnUiThread=true)
	public void unsubscribe() {
		mPusherModule.mPusher.unsubscribe(channelName);
		mPusherModule.mPusher.removeHandler(mHandler);
	}
	
	@Kroll.method
	public void sendEvent(String eventName, KrollDict data) {
		if(mPusherModule.mPusherAPI != null) {
			mPusherModule.mPusherAPI.triggerEvent(eventName, channelName, data, mPusherModule.mPusher.mSocketId);
		} else {
			Log.w("Pusher", "PusherAPI not configured because of missing appID or secret");
		}
	}
 }