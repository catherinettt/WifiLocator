package com.sfumobile.wifilocator;

import java.io.InputStream;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.sfumobile.wifilocator.request.FriendListRequest;
import com.sfumobile.wifilocator.request.HttpGET;
import com.sfumobile.wifilocator.request.MapRequest;
import com.sfumobile.wifilocator.request.RequestPackage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;


public class MapActivity extends Activity{
	private Drawable image;
	private ImageView img;
	private MapRequest  _req;
	private RequestPackage     _package;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	//	EditText url;
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zone_map);
		
		img = (ImageView) findViewById(R.id.imageView1);
		_req = new MapRequest(User.getInstance().get_map());
		image = getImage(_req.getURL());
		img.setImageDrawable(image);
		
	//	getimg map = new getimg();
    //	map.execute(img);

	}
	
	private static Drawable getImage(String url){
		try{
			InputStream is = (InputStream) new URL(url).getContent();
			Drawable d = Drawable.createFromStream(is,  "src");
			return d;
		} catch (Exception e){
			return null;
		}
	}
	
/*	private String gen_URL(String loc){
		String map_path;
		String url = host+loc;
		JSONObject map_addr = HttpGET.connect(url);
		try{
			map_path = host+map_addr.getString("map_name");
		} catch (JSONException jse) {
			map_path = null;
		}
		//String url = "http://wifi-location.appspot.com/media/zonemaps/map2.png";
		return map_path;
	}
	*/
/*	class getimg extends AsyncTask<ImageView, Drawable, Void> {	
		private ProgressDialog dialog = new ProgressDialog(MapActivity.this);
		
		@Override
		protected void onPreExecute(){
			dialog.setMessage("Loading...");
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.show();
		}
		@Override
		protected Void doInBackground(ImageView... params) {
			String zone = getIntent().getExtras().getString("zone");
		//	String[] splitter = zone.split(",");
			Log.d("URL", host+zone);
			image = getImage(host+zone);
			publishProgress(image);
			return null;
	      
		}

		@Override
		protected void onPostExecute(Void result){
			dialog.dismiss();
			img.setImageDrawable(image);
		}


	}*/

}