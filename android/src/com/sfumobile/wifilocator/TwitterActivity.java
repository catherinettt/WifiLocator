package com.sfumobile.wifilocator;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterActivity extends Activity implements OnClickListener{

	private Twitter twitter;
	private ListView tweetListView;
	private EditText tweetText;
	private Button tweetButton;
	private Button cancelButton;
	private String zone;
	private ArrayList<String> tweet_text;
	private ArrayAdapter<String> adapter;
	private AccessToken accessToken;
	private SharedPreferences prefs;
	private List<Tweet> tweets;
	private QueryResult result;
	
	RequestToken requestToken;
	public final static String TOKEN_FILE = "access_token";
	private final String CALLBACKURL = "SFUMobile://wifilocator";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter_view);
		
        prefs = getSharedPreferences(TOKEN_FILE,0);
		
		tweetListView = (ListView)this.findViewById(R.id.TweetListView);
		tweetText     = (EditText)this.findViewById(R.id.tweetText);
		cancelButton  = (Button)this.findViewById(R.id.cancelButton);
		tweetButton   = (Button)this.findViewById(R.id.tweetButton);
		
		tweetText.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
		tweetButton.setOnClickListener(this);
		
		twitter = new TwitterFactory().getInstance();
		tweets = null;
		tweet_text = new ArrayList<String>();
		tweetText.setTextColor(Color.GRAY);
		
        Bundle extras = getIntent().getExtras();
        if(extras!=null){
        	zone = extras.getString("zone");
        }
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		login();
			
		try {
			if(zone!=null){
				result = twitter.search(new Query(zone.replaceAll(" ", "")));
				tweets = result.getTweets();
				
				for(Tweet tweet : tweets){
					tweet_text.add("@" + tweet.getFromUser() + " - " + tweet.getText());
				}
			}
			
				
			if(tweet_text.size() < 1){
				tweet_text.add("No Tweets");
			}
				
			adapter = new ArrayAdapter<String>(this, R.layout.tweet_row, R.id.tweetText, tweet_text);
			tweetListView.setAdapter((ListAdapter)adapter);
			
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			Log.e("Twitter", e.getMessage());
			
			tweet_text = new ArrayList<String>();
			tweet_text.add("No Tweets");
			adapter = new ArrayAdapter<String>(this, R.layout.tweet_row, R.id.tweetText, tweet_text);
			tweetListView.setAdapter((ListAdapter)adapter);
		}
		
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Uri uri = intent.getData();
		try {
			if(uri != null){
				String verifier = uri.getQueryParameter("oauth_verifier");
				accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
				twitter.setOAuthAccessToken(accessToken);
				saveToken(accessToken);
			}
		} catch (TwitterException e) {
			Log.e("onNewIntent", "" + e.getMessage());
		}
	}
	
	void saveToken(AccessToken token){
		final Editor edit = prefs.edit();
		edit.putString("token", token.getToken());
		edit.putString("secret", token.getTokenSecret());
		edit.commit();
	}
	
	public void login() {
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(OAuthConsumer.CONSUMER_KEY, OAuthConsumer.CONSUMER_SECRET);

		if(!prefs.contains("token") | !prefs.contains("secret")){
			try {
				requestToken = twitter.getOAuthRequestToken(CALLBACKURL);
				String authUrl = requestToken.getAuthorizationURL();
				
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				this.startActivity(intent);
				
			} catch (TwitterException e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
				Log.e("Login", e.getMessage());
			}
		}
		else{
	        accessToken = new AccessToken(prefs.getString("token", ""), prefs.getString("secret", ""));
			twitter.setOAuthAccessToken(accessToken);	
		}
	}
	
	void tweet (String message){
		Log.d("Twitter","TWEET");
		if (accessToken != null) {
			Log.d("Twitter","TWEET");
			Status status = null;
			try {
				status = twitter.updateStatus(message);
			} catch (Exception e) {
				Toast.makeText(this, "Error:" + e.getMessage(),Toast.LENGTH_LONG).show();
				Log.d("Timeline",""+e.getMessage());
			}
			if(status != null){
				Toast.makeText(this, "Updated Twitter Status",Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void onClick(View v) {
		switch(v.getId()){
		case R.id.tweetText:
			tweetText.setText("#" + zone + " ");
			tweetText.setTextColor(Color.BLACK);
			tweetText.setSelection(zone.length() + 2);
			break;
		case R.id.cancelButton:
			tweetText.setText("Tweet");
			tweetText.setTextColor(Color.GRAY);
			break;
	
		case R.id.tweetButton:
			String text = tweetText.getText().toString();
			if(text.compareTo("Tweet")!=0){
				tweet(tweetText.getText().toString());
			}
			else{
				Toast.makeText(this, "Enter some text",Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

}
