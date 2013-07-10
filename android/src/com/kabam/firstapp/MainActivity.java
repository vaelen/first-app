package com.kabam.firstapp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static final String CREATE_ACCOUNT_URL = "http://localhost:9000/users";
	public static final String ENCODING = "UTF-8";
	public static final String CONTENT_TYPE = "application/json;charset=utf8";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    /** This is called when the 'Create Account' button is clicked. */
    public void createAccount(View view) {
    	EditText usernameField = (EditText) findViewById(R.id.username);
    	String username = usernameField.getText().toString();
    	createAccount(username);
    }
    
    /**
     * Attempts to create the given account.
     * @param username The username to create.
     */
    protected void createAccount(String username) {
    	startingActivity("Working...");
    	
    	String deviceType = "Android";
    	String deviceId = "1234";
    	
    	Map<String, String> data = new LinkedHashMap<String, String>();
    	data.put("username", username);
    	data.put("deviceType", deviceType);
    	data.put("deviceId", deviceId);
    	JSONObject jsonData = new JSONObject(data);
    	try {
			String json = jsonData.toString(2);
			sendCreateAccount(json);
		} catch (JSONException e) {
			failure("Couldn't Create Request");
			e.printStackTrace();
		}
    }
    
    protected void sendCreateAccount(String jsonData) {
    	HttpClient httpClient = new DefaultHttpClient();
    	HttpPost request = new HttpPost(CREATE_ACCOUNT_URL);
    		try {
    			request.setHeader("Content-Type", CONTENT_TYPE);
    			request.setEntity(new StringEntity(jsonData, ENCODING));
				httpClient.execute(request);
				success("Account Created");
			} catch (UnsupportedEncodingException e) {
				// This should never happen.
				failure("UTF-8 Not Supported");
				e.printStackTrace();
			} catch (HttpResponseException e) {
				if(e.getStatusCode() == 409) {
					failure("Account Already Exists");
				} else {
					failure("A Server Error Occurred");
				}
			} catch (ClientProtocolException e) {
				// This shouldn't happen because the only ClientProtocolException we should get is a HttpResponseException.
				failure(String.format("An Error Occurred: %s", e.getMessage()));
				e.printStackTrace();
			} catch (IOException e) {
				failure(String.format("An Error Occurred: %s", e.getMessage()));
				e.printStackTrace();
			}
    }
    
    /** 
     * Called when starting an activity.
     * This will display the given text and show the progress indicator.
     * @param description The text to display.
     */
    public void startingActivity(String description) {
    	updateText(description, true, Color.BLACK);
    }
    
    /** 
     * Called when an activity was successful.
     * This will display the given text and hide the progress indicator.
     * @param description The text to display.
     */
    public void success(String description) {
    	updateText(description, false, Color.GREEN);
    }
    
    /** 
     * Called when an activity failed.
     * This will display the given text and hide the progress indicator.
     * @param description The text to display.
     */
    public void failure(String description) {
    	updateText(description, false, Color.RED);
    }
    
    /**
     * Resets the result text to its default value.
     */
    public void resetResult() {
    	updateText("", false, Color.BLACK);
    }
    
    /**
     * Sets the text in the results text field and turns the progress indicator on or off.
     * @param text The text to display.
     * @param isWorking Whether or not to display the progress indicator.
     * @param color The color of the text.
     */
    public void updateText(String text, boolean isWorking, int color) {
    	TextView resultLabel = (TextView) findViewById(R.id.result);
    	resultLabel.setText(text);
    	resultLabel.setTextColor(color);
    	ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
    	if(isWorking) {
    		progressBar.setVisibility(View.VISIBLE);
    	} else {
    		progressBar.setVisibility(View.INVISIBLE);
    	}
    }
    
}