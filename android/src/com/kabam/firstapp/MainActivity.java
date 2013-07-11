package com.kabam.firstapp;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

	public static final String TAG = "First-App";
	public static final String CREATE_ACCOUNT_URL = "http://10.0.2.2:9000/users";
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
    public void createAccountOnClick(View view) {
    	Log.i(TAG, "createAccountOnClick");
    	EditText usernameField = (EditText) findViewById(R.id.username);
    	String username = usernameField.getText().toString();
    	Log.i(TAG, String.format("Username: %s", username));
    	createAccount(username);
    }
    
    /**
     * Attempts to create the given account.
     * @param username The username to create.
     */
    protected void createAccount(String username) {
    	String deviceType = "Android";
    	String deviceId = "1234";
    	
    	Map<String, String> data = new LinkedHashMap<String, String>();
    	data.put("username", username);
    	data.put("deviceType", deviceType);
    	data.put("deviceId", deviceId);
    	JSONObject jsonData = new JSONObject(data);
    	try {
			String json = jsonData.toString(2);
			new CreateAccountTask().execute(json);
		} catch (JSONException e) {
			Log.e(TAG, "Couldn't Create Request", e);
			failure("Couldn't Create Request");
		}
    }
    
    private class CreateAccountTask extends AsyncTask<String, Void, String> {
    	
    	protected Throwable t;
    	protected int statusCode;
    	
    	@Override
    	protected void onPreExecute() {
	      	startingActivity("Working...");
    	}
    	
	    @Override
    	protected String doInBackground(String... params) {
	    	Log.i(TAG, "Contacting Server");
	    	statusCode = 0;
	    	String jsonData = params.length > 0 ? params[0] : "";
	    	HttpClient httpClient = new DefaultHttpClient();
	    	HttpPost request = new HttpPost(CREATE_ACCOUNT_URL);
			try {
				request.setHeader("Content-Type", CONTENT_TYPE);
				request.setEntity(new StringEntity(jsonData, ENCODING));
				HttpResponse response = httpClient.execute(request);
				Log.d(TAG, String.format("Server Returned: %s", response.getStatusLine()));
				statusCode = response.getStatusLine().getStatusCode();
			} catch (HttpResponseException e) {
				statusCode = e.getStatusCode();
			} catch (Throwable t) {
				Log.i(TAG, "Caught Exception During Server Communication", t);
				statusCode = 0;
				this.t = t;
			}
			return null;
	    }
	    
	    @Override
	    protected void onPostExecute(String result) {
	    	if(statusCode >= 200 && statusCode < 300) {
	    		success("Account Created");
	    	} else if (statusCode > 0) {
	    		Log.e(TAG, String.format("Bad Response Code: %d", statusCode));
	    		if(statusCode == 409) {
	    			failure("Account Already Exists");
	    		} else {
	    			failure("A Server Error Occurred");
	    		}
	    	} else if (t instanceof IOException) {
	    		IOException e = (IOException) t;
	    		Log.e(TAG, "IO Exception", e);
	    		failure(String.format("A Network Error Occurred: %s", e.getMessage()));
	    	} else if (t != null) {
	    		Log.e(TAG, String.format("Unknown Exception: %s", t.toString()), t);
	    		failure(String.format("An Error Occurred: %s", t.toString()));
	    	} else {
	    		Log.e(TAG, String.format("No Exception Was Thrown But Connection Was Not Successful.  Status Code: %d", statusCode));
	    		failure("An Error Occurred");
	    	}
	    }
    }
    
    /** 
     * Called when starting an activity.
     * This will display the given text and show the progress indicator.
     * @param description The text to display.
     */
    public void startingActivity(String description) {
    	Log.i(TAG, String.format("Starting: %s", description));
    	updateText(description, true, Color.BLACK);
    }
    
    /** 
     * Called when an activity was successful.
     * This will display the given text and hide the progress indicator.
     * @param description The text to display.
     */
    public void success(String description) {
    	Log.i(TAG, String.format("Success: %s", description));
    	updateText(description, false, Color.GREEN);
    }
    
    /** 
     * Called when an activity failed.
     * This will display the given text and hide the progress indicator.
     * @param description The text to display.
     */
    public void failure(String description) {
    	Log.e(TAG, String.format("Failure: %s", description));
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
    	Log.d(TAG, String.format("Setting Result Text.  Text: %s, isWorking: %b, Color: %d", text, isWorking, color));
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