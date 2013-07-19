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
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.plus.PlusClient;

public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {

	public static final String TAG = "First-App";
	public static final String CREATE_ACCOUNT_URL = "http://10.0.2.2:9000/users";
	public static final String ENCODING = "UTF-8";
	public static final String CONTENT_TYPE = "application/json;charset=utf8";
	
	private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
	
	public static final int[] loginMethodToggleButtons = {
		R.id.toggleFacebook,
		R.id.toggleGoogle
	};
	
	private ProgressDialog progressDialog;
    private PlusClient googlePlusClient;
    private ConnectionResult connectionResult;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        googlePlusClient = new PlusClient.Builder(this, this, this)
        	.setVisibleActivities("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity")
        	.build();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Signing in...");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLVE_ERR && resultCode == RESULT_OK) {
	        connectionResult = null;
	        googlePlusClient.connect();
        } else {
        	Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        }
    }
    
    public void toggleLoginMethodButton(int buttonId) {
    	ToggleButton toggleButton = (ToggleButton) findViewById(buttonId);
		boolean loginMethodChecked = false;
    	if(toggleButton.isChecked()) {
    		// Uncheck other login methods
    		for(int id: loginMethodToggleButtons) {
    			if(id != buttonId) {
    				ToggleButton loginMethodButton = (ToggleButton) findViewById(id);
    				loginMethodButton.setChecked(false);
    			}
    		}
    		loginMethodChecked = true;
    	} else {
    		for(int id: loginMethodToggleButtons) {
				ToggleButton loginMethodButton = (ToggleButton) findViewById(id);
				if(loginMethodButton.isChecked()) {
					loginMethodChecked = true;
					break;
				}
    		}
    	}

    	EditText usernameField = (EditText) findViewById(R.id.username);
		if (loginMethodChecked) {
    		usernameField.setText("");
    		usernameField.setEnabled(false);
		} else {
			usernameField.setEnabled(true);
		}
	
    }
    
    /**
     * This is called when the 'Login With Facebook' button is toggled. 
     */
    public void toggleFacebookLogin(View view) {
    	toggleLoginMethodButton(R.id.toggleFacebook);
    }
    
    /**
     * This is called when the 'Login With Google' button is toggled. 
     */
    public void toggleGoogleLogin(View view) {
    	toggleLoginMethodButton(R.id.toggleGoogle);
    }
    
    /** This is called when the 'Create Account' button is clicked. */
    public void createAccountOnClick(View view) {
    	Log.i(TAG, "createAccountOnClick");
    	if(((ToggleButton) findViewById(R.id.toggleFacebook)).isChecked()) {
    		authenticateWithFacebook();
    	} else if(((ToggleButton) findViewById(R.id.toggleGoogle)).isChecked()) {
        	authenticateWithGoogle();
    	} else {
	    	EditText usernameField = (EditText) findViewById(R.id.username);
	    	String username = usernameField.getText().toString();
	    	Log.i(TAG, String.format("Email Address: %s", username));
	    	createAccount(username);
    	}
    }

	public void authenticateWithFacebook() {
		Session.openActiveSession(this, true, new Session.StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				if(session.isOpened()) {
					Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
						@Override
						public void onCompleted(GraphUser user, Response response) {
							Object email = user.getProperty("email");
							Log.i(TAG, String.format("Email Address From Facebook: %s", email));
							if(email == null) {
								Log.e(TAG, "No email address was returned from Facebook.");
							} else {
								createAccount(email.toString());
							}
						}
					});
				}
			}
		});
	}
	
	public void authenticateWithGoogle() {
		googlePlusClient.connect();
	}
    
    /**
     * Attempts to create the given account.
     * @param username The username to create.
     */
    protected void createAccount(String username) {
    	String deviceType = String.format("%s|Android|%s", Build.MODEL, Build.VERSION.RELEASE);
    	String deviceId = Secure.getString(FirstAppApplication.getAppContext().getContentResolver(), Secure.ANDROID_ID);;
    	
    	Log.d(TAG, String.format("Device Type: %s, Device ID: %s", deviceType, deviceId));
    	
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
    	updateText(description, true);
    }
    
    /** 
     * Called when an activity was successful.
     * This will display the given text and hide the progress indicator.
     * @param description The text to display.
     */
    public void success(String description) {
    	Log.i(TAG, String.format("Success: %s", description));
    	updateText(description, false);
    }
    
    /** 
     * Called when an activity failed.
     * This will display the given text and hide the progress indicator.
     * @param description The text to display.
     */
    public void failure(String description) {
    	Log.e(TAG, String.format("Failure: %s", description));
    	updateText(description, false);
    }
    
    /**
     * Resets the result text to its default value.
     */
    public void resetResult() {
    	updateText("", false);
    }
    
    /**
     * Sets the text in the results text field and turns the progress indicator on or off.
     * @param text The text to display.
     * @param isWorking Whether or not to display the progress indicator.
     */
    public void updateText(String text, boolean isWorking) {
    	Log.d(TAG, String.format("Setting Result Text.  Text: %s, isWorking: %b", text, isWorking));
    	
    	if(isWorking) {
    		progressDialog.setMessage(text);
    		progressDialog.show();
    	} else {
    		progressDialog.hide();
    		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    	}
    }
    
    public void onConnectionFailed(ConnectionResult result) {
    	if (progressDialog.isShowing()) {
    		// The user clicked the sign-in button already. Start to resolve
    		// connection errors. Wait until onConnected() to dismiss the
    		// connection dialog.
    		if (result.hasResolution()) {
    			try {
    				result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
    			} catch (SendIntentException e) {
    				googlePlusClient.connect();
    			}
    		}
    	}

    	// Save the intent so that we can start an activity when the user clicks
    	// the sign-in button.
    	connectionResult = result;
    }

    public void onConnected(Bundle connectionHint) {
    	// We've resolved any connection errors.
    	progressDialog.dismiss();
    	String accountName = googlePlusClient.getAccountName();
        Toast.makeText(this, accountName + " is connected.", Toast.LENGTH_LONG).show();
    }
    
    public void onDisconnected() {
    	Log.d(TAG, "disconnected");
    }

    
}