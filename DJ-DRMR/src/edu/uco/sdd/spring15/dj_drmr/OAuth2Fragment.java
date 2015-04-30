package edu.uco.sdd.spring15.dj_drmr;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import com.soundcloud.api.ApiWrapper;
import com.soundcloud.api.Endpoints;
import com.soundcloud.api.Token;
import com.soundcloud.api.examples.CreateWrapper;
import com.soundcloud.api.examples.GetResource;

import edu.uco.sdd.spring15.dj_drmr.stream.SoundcloudResource;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OAuth2Fragment extends DialogFragment {
	
//	public static ApiWrapper wrapper;
 
    private WebView webViewOauth;
    private Resources res;
    
	private ApiWrapper wrapper;
	public static Token token;
	
	private boolean loggedIn = false;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initialize();
    }
    
    private void initialize() {
    	res = getResources();
    	
        // set up the ApiWrapper
        URI redirect = null;
		try {
			redirect = new URI(SoundcloudResource.REDIRECT_URI_STRING);
		} catch (URISyntaxException e) {
			// invalid URI format
			e.printStackTrace();
		}
		wrapper = new ApiWrapper(SoundcloudResource.CLIENT_ID, SoundcloudResource.CLIENT_SECRET, redirect, null);
    }
 
    private void saveAccessToken(String url) {
    	Uri result = Uri.parse(url);
        final String error = result.getQueryParameter("error");
        final String code = result.getQueryParameter("code");
            
        // login to soundcloud
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                	if (code != null) {
                		token = wrapper.authorizationCode(code, Token.SCOPE_NON_EXPIRING);
	                    SharedPreferences sharedPreferences = getActivity()
	    	                    .getSharedPreferences(getResources().getString(R.string.shared_prefs), 0);
	    	            final Editor edit = sharedPreferences.edit();
	    	            edit.putString("access", token.access);
	    	            edit.putString("refresh", token.refresh);
	    	            edit.commit();
	    	            setLoggedIn(true);
	    	            Log.d("OAuth2Fragment", "access = " + token.access + ", refresh = " + token.refresh);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        // wait for the runnable to finish - 
        // added this to keep the login screen from appearing twice
        while (!loggedIn) { /* wait for login to complete */ }
        
        Intent intent2;
        if (token == null) {
        	intent2 = new Intent(getActivity(), Login.class);
        } else {
        	intent2 = new Intent(getActivity(), DjdrmrMain.class);
        }
        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent2);
        return;
    }
    
    private void setLoggedIn(boolean loggedIn) {
    	this.loggedIn = loggedIn;
    }
 
    @Override
    public void onViewCreated(View arg0, Bundle arg1) {
        super.onViewCreated(arg0, arg1);
        //load the url of the oAuth login page
      URI url = wrapper.authorizationCodeUrl(Endpoints.CONNECT, Token.SCOPE_NON_EXPIRING);
		webViewOauth.loadUrl(url.toASCIIString());
		//set the web client
		webViewOauth.setWebViewClient(new WebViewClient(){
			@Override
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {
	            //check if the login was successful and the access token returned
	            //this test depend of your API
				Log.e("OAuth2Fragment", "url=" + url);
	            if (url.contains(SoundcloudResource.REDIRECT_URI_STRING)) {
	            	// save the token
	                saveAccessToken(url);
	                return true;
	            } else {
	            	return false;
	            }
	        }
		});
		//activates JavaScript (just in case)
		WebSettings webSettings = webViewOauth.getSettings();
		webSettings.setJavaScriptEnabled(true);
    }
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        //Retrieve the webview
        View v = inflater.inflate(R.layout.webview, container, false);
        webViewOauth = (WebView) v.findViewById(R.id.web_oauth);
        getDialog().setTitle("Log in with SoundCloud");
        return v;
    }
}
