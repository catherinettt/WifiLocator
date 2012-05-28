package com.sfumobile.wifilocator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;


public class RequestHandler {
	private WifiManager wm;
	private List<ScanResult> apList;
	
	public RequestHandler(Context context){
        wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
	}
	
	public Boolean wifi_check(){
		return wm.isWifiEnabled();
	}

	public String getBSSID(){
		return wm.getConnectionInfo().getBSSID();
	}
	
	public String getSSID(){
		return wm.getConnectionInfo().getSSID();
	}
	
	/*
	 * Gets the 3 strongest signal strengths and picks the most common zone
	 */
	public JSONObject getStrongestAP(){
		wm.startScan();
	    while(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION == null){
	    	try {
				this.wait(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	    apList = wm.getScanResults();
	    
	    Collections.sort(apList, new CompareScanResult());
	    
	    List<String> zones = new ArrayList<String>();
	    List<JSONObject> jsonResponses = new ArrayList<JSONObject>();
	    
	    //Get zone names for 3 strongest signal levels
	    if(apList.size() > 2){
		    for(ScanResult result : apList.subList(0, 3)){
		        JSONObject requestBody = new JSONObject();
				JSONObject response = null;
				try {
					requestBody.put("mac_address", result.BSSID);
					requestBody.put("user_id", WifiLocatorActivity.USER_ID);
					response = postRequest(requestBody,RequestConstants.GETZONE_URL);
				} catch (JSONException e) {
					e.printStackTrace();
				}
		        try {
		        	jsonResponses.add(response);
					zones.add(response.getString("zone_name"));
				} catch (JSONException e) {
					Log.e("Handler JSON Error:", e.getLocalizedMessage());
				}
		    }
	    }
	    //If there aren't 3 ap's, return the strongest one
	    else{
	    	try{
	    		String address =  RequestConstants.GETZONE_URL + WifiLocatorActivity.USER_ID + "/" + apList.get(0).BSSID;
	        	return HttpGET.connect(address);
	    	}
	    	catch(IndexOutOfBoundsException e){
	    		
	    	}
	    }
	    
	    //Check for the most common zone
	    if(zones.subList(2, 3).contains(zones.get(1))){
	    	return jsonResponses.get(1);
	    }
	    else{
	    	return jsonResponses.get(0);
		}
	}
	
	public JSONObject getZoneInfo(){
		
		/* Old method of picking the ap currently connected to
        String address =  url + WifiLocatorActivity.USER + "/" + wm.getConnectionInfo().getBSSID();
        JSONObject zone_info = HttpGET.connect(address);
		*/

		
		/*
    	JSONObject zone_info = getStrongestAP(); //"00:1f:45:64:12:f1"; 
        return zone_info;
        */
		
		JSONObject requestBody = new JSONObject();
		JSONObject response = null;
		try {
			Log.d("mac", getBSSID().toString());
			requestBody.put("mac_address", getBSSID().toString());
			requestBody.put("user_id", WifiLocatorActivity.USER_ID);
			response = postRequest(requestBody,RequestConstants.GETZONE_URL);
			Log.d("response", response.getString("zone_name"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return response;
	}
	
	public int sendFriendRequest(int id){

		JSONObject requestBody = new JSONObject();
		JSONObject response = null;
		try {
			requestBody.put("friend_id", id);
			requestBody.put("user_id", WifiLocatorActivity.USER_ID);
			response = postRequest(requestBody,RequestConstants.FRIEND_REQUEST_URL);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if(response!=null){
			try {
				return response.getInt("status");
			} catch (JSONException e) {
				Log.d("FriendRequest","Couldn't Convert Status to Int");
				return -1;
			}
		}
		return -1;
	}
	
	public static int acceptFriendRequest(int request_id){
		JSONObject requestBody = new JSONObject();
		JSONObject response = null;
		try {
			requestBody.put("request_id", request_id);
			response = postRequest(requestBody,	RequestConstants.ACCEPT_FRIEND_REQUEST_URL);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if(response!=null){
			try {
				return response.getInt("status");
			} catch (JSONException e) {
				Log.d("AcceptFriendRequest","Couldn't Convert Status to Int");
				return -1;
			}
		}
		return -1;
	}
	
	public ArrayList<JSONObject> getFriendRequests(){
		JSONObject requestBody = new JSONObject();
		JSONObject response = null;
		ArrayList<JSONObject> result;
		try {
			requestBody.put("user_id", WifiLocatorActivity.USER_ID);
			response = postRequest(requestBody,RequestConstants.GET_FRIEND_REQUESTS_URL);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if(response!=null){
			return parseFriendRequests(response);
		}
		result = new ArrayList<JSONObject>();
		return result;
	}
	
	public ArrayList<JSONObject> parseFriendRequests(JSONObject response){
		Log.d("FriendRequests",response.toString());
		ArrayList<JSONObject> result = new ArrayList<JSONObject>();
		try {
			if(response.getInt("status")!=2){
				JSONArray requests = response.getJSONArray("requests");
				for(int i=0; i < requests.length(); i++){
					result.add(requests.getJSONObject(i));
				}
				return result;
			}
		} catch (JSONException e) {
			Log.d("ParseFriendRequests",e.getLocalizedMessage());
		}
		return result;
	}
	
	public static JSONObject postRequest(JSONObject body, String url){
		
		HttpClient httpClient   = new DefaultHttpClient();
		HttpPost post           = new HttpPost(url);
		HttpResponse response   = null;
		JSONObject jsonResponse = null;

		post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");
		try {
			post.setEntity(new StringEntity(body.toString()));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        try {
			response= httpClient.execute(post);			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        try {
			BufferedReader r = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			String json = r.readLine();
			try {
				jsonResponse = new JSONObject(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return jsonResponse;
	}
	
}