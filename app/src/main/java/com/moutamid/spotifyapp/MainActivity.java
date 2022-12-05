package com.moutamid.spotifyapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.fxn.stash.Stash;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "spotify-app:/oauth";
    private String CLIENT_ID;
    private static final String SCOPES = "user-read-recently-played,user-library-modify,user-read-email,user-read-private,user-top-read,user-follow-read,streaming,app-remote-control,playlist-read-private,playlist-read-collaborative,playlist-modify-private,playlist-modify-public,user-follow-modify,user-modify-playback-state,user-read-playback-state";
    private RequestQueue queue;
    private SpotifyAppRemote mSpotifyAppRemote;
    private SharedPreferences.Editor editor;
    private SharedPreferences msharedPreferences;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CLIENT_ID = getResources().getString(R.string.CLIENT_ID);

        msharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        queue = Volley.newRequestQueue(this);

        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{SCOPES});
        builder.setShowDialog(true);
        AuthorizationRequest request = builder.build();

        token = Stash.getString("token", "");
//        if (!token.isEmpty()){
//            startActivity(new Intent(MainActivity.this, ArtistActivity.class));
//            finish();
//        }


        findViewById(R.id.login).setOnClickListener(v -> AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request));

    }

    private void waitForUserInfo() {
        UserService userService = new UserService(queue, msharedPreferences);
        userService.get(() -> {
            User user = userService.getUser();
            editor = getSharedPreferences("SPOTIFY", 0).edit();
            editor.putString("userid", user.id);
            Log.d("STARTING", "GOT USER INFORMATION");
            // We use commit instead of apply because we need the information stored immediately
            editor.commit();
            startActivity(new Intent(MainActivity.this, ArtistActivity.class));
        });
    }

   /*@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null) {
            AuthorizationResponse response = AuthorizationResponse.fromUri(uri);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    Stash.put("token", response.getAccessToken());
                    Log.d("STARTING", "GOT AUTH TOKEN");
                    editor = getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("token", response.getAccessToken());
                    editor.apply();
                    waitForUserInfo();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }*/

    @Override
    protected void onStart() {
        super.onStart();

        /**
         ConnectionParams connectionParams =
         new ConnectionParams.Builder(CLIENT_ID)
         .setRedirectUri(REDIRECT_URI)
         .showAuthView(true)
         .build();

         SpotifyAppRemote.disconnect(mSpotifyAppRemote);

         SpotifyAppRemote.connect(this, connectionParams,
         new Connector.ConnectionListener() {

        @Override
        public void onConnected(SpotifyAppRemote spotifyAppRemote) {
        mSpotifyAppRemote = spotifyAppRemote;
        Log.d("MainActivity12", "Connected! Yay!");

        // Now you can start interacting with App Remote
        if (spotifyAppRemote.isConnected()){
        startActivity(new Intent(MainActivity.this, ArtistActivity.class));
        finish();
        }
        }

        @Override
        public void onFailure(Throwable throwable) {
        Log.e("MainActivity12", throwable.getMessage(), throwable);
        if (throwable instanceof NotLoggedInException || throwable instanceof UserNotAuthorizedException) {
        // Show login button and trigger the login flow from auth library when clicked
        } else if (throwable instanceof CouldNotFindSpotifyApp) {
        // Show button to download Spotify
        }
        }
        });
         */

    }

    private void connected() {
        // Then we will write some more code here.
        // waitForUserInfo();
        mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity12", track.name + " by " + track.artist.name);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    Stash.put("token", response.getAccessToken());
                    Log.d("response", "GOT AUTH TOKEN");
                    editor = getSharedPreferences("SPOTIFY", 0).edit();
                    editor.putString("token", response.getAccessToken());
                    editor.apply();
                    waitForUserInfo();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Log.d("response", response.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }
}