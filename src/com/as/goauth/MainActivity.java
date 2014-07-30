package com.as.goauth;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;


public class MainActivity extends Activity implements
ConnectionCallbacks, OnConnectionFailedListener, OnClickListener {

	
	 /* Request code used to invoke sign in user interactions. */
	  private static final int RC_SIGN_IN = 0;

	private static final String PIC_SIZE = "400";

	  /* Client used to interact with Google APIs. */
	  private GoogleApiClient mGoogleApiClient;

	  /* A flag indicating that a PendingIntent is in progress and prevents
	   * us from starting further intents.
	   */
	  private boolean mIntentInProgress;
	  
	  /* Track whether the sign-in button has been clicked so that we know to resolve
	   * all issues preventing sign-in without waiting.
	   */
	  private boolean mSignInClicked;

	  /* Store the connection result from onConnectionFailed callbacks so that we can
	   * resolve them when the user clicks sign-in.
	   */
	  private ConnectionResult mConnectionResult;
	  
	  private SignInButton bSignIn;
	  private Button bSignOut;
	  private LinearLayout proLayout;
	  private ImageView proPic;
	  private TextView proName;
	  private TextView proEmail;
	  private Button bShare;
	  private Button bRevoke;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //declaring variables
        bSignIn = (SignInButton) findViewById(R.id.sign_in_button);
        bSignOut = (Button) findViewById(R.id.sign_out);
        bShare = (Button) findViewById(R.id.share_button);
        bRevoke = (Button) findViewById(R.id.revokeAccess);
        proLayout = (LinearLayout) findViewById(R.id.pro_layout);
        proPic = (ImageView) findViewById(R.id.pro_Pic);
        proName = (TextView) findViewById(R.id.pro_Name);
        proEmail = (TextView) findViewById(R.id.pro_Email);
        
        //button Onlicklisteners
        bSignIn.setOnClickListener(this);
        bSignOut.setOnClickListener(this);
        bShare.setOnClickListener(this);
        bRevoke.setOnClickListener(this);
        
        //settings options for GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(Plus.API)
        .addScope(Plus.SCOPE_PLUS_LOGIN)
        .build();

    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		 switch (v.getId()) {
	        case R.id.sign_in_button:
	            // Signin with Google+
	            signIn();
	            break;
	        case R.id.sign_out:
	            // Signout with Google+
	            signOut();
	            break;
	        case R.id.share_button:
	            // Signout with Google+
	            shareLink();
	            break;
	        case R.id.revokeAccess:
	            // Signout with Google+
	            revokeAccess();
	            break;
	 
	        }
	    }
		
	private void revokeAccess() {
		Log.i("Revoke","Revoke Access");
		// Prior to disconnecting, run clearDefaultAccount().
		Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
		Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient)
		    .setResultCallback(new ResultCallback<Status>() {

		@Override
		public void onResult(Status arg0) {
			// mGoogleApiClient is now disconnected and access has been revoked.
		    // Trigger app logic to comply with the developer policies
			
			 mGoogleApiClient.connect();
             UpdateLayout(false);
		}

		});

	}

	private void shareLink() {
		Log.i("share","Sharing Link");
		// Launch the Google+ share dialog with attribution to your app.
	      Intent shareIntent = new PlusShare.Builder(this)
	          .setType("text/plain")
	          .setText("Sign in with Google+ Tutorial")
	          .setContentUrl(Uri.parse("http://blog.arunsharma.me/"))
	          .getIntent();

	      startActivityForResult(shareIntent, 0);

	}

	private void signOut() {
		if (mGoogleApiClient.isConnected()) {
		      Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
		      mGoogleApiClient.disconnect();
		      mGoogleApiClient.connect();
		      UpdateLayout(false);
		    }


	}

	private void signIn() {
		if(!mGoogleApiClient.isConnecting()) {
		    mSignInClicked = true;
		    resolveSignInError();
		}
	}

	
    
    
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
      }

      protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
          mGoogleApiClient.disconnect();
        }
      }

    
    private void resolveSignInError() {
      if (mConnectionResult.hasResolution()) {
        try {
          mIntentInProgress = true;
          mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
        } catch (SendIntentException e) {
          // The intent was canceled before it was sent.  Return to the default
          // state and attempt to connect to get an updated ConnectionResult.
          mIntentInProgress = false;
          mGoogleApiClient.connect();
        }
      }
    }

    
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
    	  if (requestCode == RC_SIGN_IN) {
    	    if (responseCode != RESULT_OK) {
    	      mSignInClicked = false;
    	    }

    	    mIntentInProgress = false;

    	    if (!mGoogleApiClient.isConnecting()) {
    	      mGoogleApiClient.connect();
    	    }
    	  }
    	}
    

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		
		
		if (!mIntentInProgress) {
		    // Store the ConnectionResult so that we can use it later when the user clicks
		    // 'sign-in'.
		    mConnectionResult = result;

		    if (mSignInClicked) {
		      // The user has already clicked 'sign-in' so we attempt to resolve all
		      // errors until the user is signed in, or they cancel.
		      resolveSignInError();
		    }
		  }
		
	}


	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		mSignInClicked = false;
		  Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();
		  
		  //Updating Profile Information
		//  UpdateProfile();
		  //Updating Layout
		  new GetToken().execute();
		  UpdateLayout(true);
	}

	private class GetToken extends AsyncTask<Void, Void, String>{

		@Override
		protected String doInBackground(Void... params) {
			String token = "ashok";
			try {
				token = GoogleAuthUtil.getToken(MainActivity.this, Plus.AccountApi.getAccountName(mGoogleApiClient), 
						"oauth2:https://www.googleapis.com/auth/plus.me");
			} catch (UserRecoverableAuthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GoogleAuthException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return token;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Toast.makeText(MainActivity.this, result+"kumar", Toast.LENGTH_LONG).show();
			Toast.makeText(MainActivity.this, "this is not an error", Toast.LENGTH_LONG).show();
		}
		
	}


	private void UpdateLayout(boolean signinStatus) {
		// TODO Auto-generated method stub
		if(signinStatus)
		{
			bSignIn.setVisibility(View.GONE);
			bSignOut.setVisibility(View.VISIBLE);
			bShare.setVisibility(View.VISIBLE);
			bRevoke.setVisibility(View.VISIBLE);
			proLayout.setVisibility(View.VISIBLE);
			
		}
		else{
			bSignIn.setVisibility(View.VISIBLE);
			bSignOut.setVisibility(View.GONE);
			bShare.setVisibility(View.GONE);
			bRevoke.setVisibility(View.GONE);
			proLayout.setVisibility(View.GONE);
		}
			
	}

	private void UpdateProfile() {
		// TODO Auto-generated method stub
		if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
		    Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
		    String personName = currentPerson.getDisplayName();
		    String personPhoto = currentPerson.getImage().getUrl();
		    String personGooglePlusProfile = currentPerson.getUrl();
		    String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
		    
		    proName.setText(personName);
		    proEmail.setText(email);
		    
		    personPhoto = personPhoto.substring(0, personPhoto.length() - 2)
                    + PIC_SIZE;
		    
		    new UpdateImage().execute(personPhoto);

		  }

	}
	
	   private class UpdateImage extends AsyncTask<String, Void, Bitmap> {
		   
	 
	        @Override
	        protected Bitmap doInBackground(String... URL) {
	 
	            String imageURL = URL[0];
	 
	            Bitmap bitmap = null;
	            try {
	                // Download Image from URL
	                InputStream input = new java.net.URL(imageURL).openStream();
	                // Decode Bitmap
	                bitmap = BitmapFactory.decodeStream(input);
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	            return bitmap;
	        }
	 
	        @Override
	        protected void onPostExecute(Bitmap result) {
	            // Set the bitmap into ImageView
	            proPic.setImageBitmap(result);
	          
	        }
	    }

	@Override
	public void onConnectionSuspended(int arg0) {
		// TODO Auto-generated method stub
		 mGoogleApiClient.connect();
	}

}
