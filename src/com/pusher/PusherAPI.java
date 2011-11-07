package com.pusher;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.appcelerator.kroll.KrollDict;
import org.json.JSONObject;

public class PusherAPI {
	private String key;
	private String appID;
	private String secretKey;
	
	static final private String defaultHost = "api.pusherapp.com";
	
	public PusherAPI(String key, String appID, String secretKey) {
		this.key = key;
		this.appID = appID;
		this.secretKey = secretKey;
	}
	
	public void triggerEvent(String eventName, String channelName, KrollDict data, String socketID) {
		String path = "/apps/" + this.appID + "/channels/" + channelName + "/events";
		String bodyString = new JSONObject(data).toString();
		
		StringBuffer buffer = new StringBuffer();
		// Auth key
		buffer.append("auth_key=");
		buffer.append(this.key);
		// Timestamp
		buffer.append("&auth_timestamp=");
		buffer.append(System.currentTimeMillis() / 1000);
		// Auth_version
		buffer.append("&auth_version=1.0");
		// MD5 body
		buffer.append("&body_md5=");
		buffer.append(md5Representation(bodyString));
		// Event name
		buffer.append("&name=");
		buffer.append(eventName);
		
		if(socketID != null) {
			buffer.append("&socket_id=");
			buffer.append(socketID);
		}
		
		String query = buffer.toString();
		
		// Build signature
		buffer = new StringBuffer();
		buffer.append("POST\n");
		buffer.append(path);
		buffer.append("\n");
		buffer.append(query);
		
		String signature = hmacsha256Representation(buffer.toString());
		
		// Build URI
		buffer = new StringBuffer();
		buffer.append("http://");
		buffer.append(defaultHost);
		buffer.append(path);
		buffer.append("?");
		buffer.append(query);
		buffer.append("&auth_signature=");
		buffer.append(signature);
		
		DefaultHttpClient client = new DefaultHttpClient();
		
		try {
			HttpPost request = new HttpPost(buffer.toString());
			request.addHeader("Content-Type", "application/json");
			request.setEntity(new StringEntity(bodyString));
			client.execute(request);
		} catch (ClientProtocolException e) {
			throw new RuntimeException("Client protocol error");
		} catch (IOException e) {
			throw new RuntimeException("Network error");
		}
	}
	
	private String md5Representation(String data) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			byte[] digest = messageDigest.digest(data.getBytes("UTF8"));
			return byteArrayToString(digest);
		} catch(NoSuchAlgorithmException nsae) {
			throw new RuntimeException("No MD5 algorithm");
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException("No UTF-8");
		}
	}
	
	private String byteArrayToString(byte[] data) {
		BigInteger bigInteger = new BigInteger(1, data);
		String hash = bigInteger.toString(16);
		// Zero pad it
		while(hash.length() < 32) {
			hash = "0" + hash;
		}
		return hash;
	}
	
	private String hmacsha256Representation(String data) {
		try {
			final SecretKeySpec signingKey = new SecretKeySpec(this.secretKey.getBytes(), "HmacSHA256");
			final Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(signingKey);
			
			byte[] digest = mac.doFinal(data.getBytes("UTF-8"));
			digest = mac.doFinal(data.getBytes());
			
			BigInteger bigInteger = new BigInteger(1, digest);
			return String.format("%0" + (digest.length << 1) + "x", bigInteger);
		} catch(NoSuchAlgorithmException nsae) {
			throw new RuntimeException("No MD5 algorithm");
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException("No UTF-8");
		} catch(InvalidKeyException e) {
			throw new RuntimeException("Invalid key exception while converting to HMAC SHA256");
		}
	}
}
