package com.sfumobile.wifilocator;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
//import android.widget.Toast;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Button;
//import com.sfumobile.wifilocator.DBAdapter;
import com.sfumobile.wifilocator.HttpGET;

public class WifiLocatorActivity extends Activity implements OnClickListener{
    
	private String bssid, zone, address;
	private WifiManager wm;
	private WifiInfo info;
	private TextView bssidText, macText, zoneText;
	private Button pollButton, friendButton;
	private Handler handler;
	private DBAdapter db;
	private ImageView twitterIcon;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
/*
        db = new DBAdapter(this.getApplicationContext());
        db.createDatabase();
        db.openDataBase();
        
        Cursor c = db.getAP();
        c.moveToFirst();
        for(int i=0; i<c.getCount(); i++){
        	Log.d("STUFF", c.getString(c.getColumnIndex("bssid")));
        	c.moveToNext();
        }
        c.close();
        */
        bssidText   = (TextView)this.findViewById(R.id.bssidText);
        macText     = (TextView)this.findViewById(R.id.macText);
        zoneText    = (TextView)this.findViewById(R.id.zoneText);
        pollButton  = (Button)this.findViewById(R.id.pollButton);
        twitterIcon = (ImageView)this.findViewById(R.id.twitterIcon);
        friendButton = (Button)this.findViewById(R.id.friendButton);
      //  handler = new Handler();
        
        pollButton.setOnClickListener(this);
        twitterIcon.setOnClickListener(this);
        friendButton.setOnClickListener(this);
        
        try{
			poll();
		} catch (Exception e){
			e.printStackTrace();
		}
        wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        info = wm.getConnectionInfo();
        bssid = info.getBSSID();
    }
    
    public void onStart(){
    	super.onStart();
    	bssidText.setText(bssid);
    	
    	//zone = db.getZone(bssid);
    	zoneText.setText("-1");
    	/*
    	new Thread(new Runnable(){
    		public void run(){
    			while(true){
    				try {
    					Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
					e.printStackTrace();
					
    				handler.post(new Runnable(){
    					public void run(){
    						try{
    							poll();
    						} catch (Exception e){
    							e.printStackTrace();
    						}
    					}
    				});
					}
    			}
    		}
    	}).start();*/
    }
    
    public void poll(){
    	
        info = wm.getConnectionInfo();
     //   String test = "{\"list\": { \"Zones\": { \"zone_id\": \"2\", \"mac_address\": \"test\"} } }";
        //Alert user of hand-offs
   //     if(bssid.compareTo(info.getBSSID()) != 0){
        	bssid = info.getBSSID();
            bssidText.setText(bssid);
            address = "http://wifi-location.appspot.com/rest/BSSIDZones?feq_mac_address=" + bssid;
            
            try{
	           // JSONObject json = new JSONObject(test);
	            JSONObject json = HttpGET.connect(address);
	            JSONObject lists = json.getJSONObject("list");
	            JSONObject zones = lists.getJSONObject("BSSIDZones");
	            zone = zones.getString("zones");
	            
	        	zoneText.setText(zone);
            }
            catch(JSONException e){
            	Log.e("JSON Error:", e.getLocalizedMessage());
            	zone = "Unknown";
	        	zoneText.setText(zone);
            }
        	

            
			//int duration = Toast.LENGTH_SHORT;
			//Toast toast = Toast.makeText(this.getApplicationContext(), "Handoff!", duration);
			//toast.show();
      //  }
     

    }

	public void onClick(View src) {
		@SuppressWarnings("unused")
		Intent myIntent;
		switch(src.getId()){
		case R.id.pollButton:
			try{
				poll();
			} catch (Exception e) {
				e.printStackTrace();
			}

			break;
		case R.id.friendButton:
    		Intent nextScreen = new Intent(getApplicationContext(),Friends.class);
    		startActivity(nextScreen);
    		break;
		case R.id.twitterIcon:
			myIntent = new Intent(src.getContext(), TwitterSignInActivity.class);
			myIntent.putExtra("zone", zone);
			startActivity(myIntent);
			break;
		}
	}
	
	public void onStop(){
		super.onStop();
	}
}
