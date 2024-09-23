package com.moutamid.spotifyapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.wessam.library.LayoutImage;
import com.wessam.library.NetworkChecker;
import com.wessam.library.NoInternetLayout;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "com.moutamid.spotifyapp://callback";
    private String CLIENT_ID;
    public static final String SCOPES = "user-read-recently-played,user-library-modify,user-read-email,user-read-private,user-top-read,user-follow-read,streaming,app-remote-control,playlist-read-private,playlist-read-collaborative,playlist-modify-private,playlist-modify-public,user-follow-modify,user-modify-playback-state,user-read-playback-state";
    private RequestQueue queue;
    private SpotifyAppRemote mSpotifyAppRemote;
    private SharedPreferences.Editor editor;
    private SharedPreferences msharedPreferences;
    String token;
    AuthorizationRequest request;
    Button login, loginBro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Constants.checkApp(this);

        login = findViewById(R.id.login);
        loginBro = findViewById(R.id.loginBro);

        CLIENT_ID = getResources().getString(R.string.CLIENT_ID);

        msharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        queue = Volley.newRequestQueue(this);

        AuthorizationRequest.Builder builder =
                new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{SCOPES});
        builder.setShowDialog(true);
        request = builder.build();

        loginBro.setOnClickListener(v -> AuthorizationClient.openLoginInBrowser(this, request));
        login.setOnClickListener(v -> AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request));

        token = Stash.getString("token", "");
//        if (!token.isEmpty()){
//            startActivity(new Intent(MainActivity.this, ArtistActivity.class));
//            finish();
//        }

        /*if (!NetworkChecker.isNetworkConnected(this)){
            //Toast.makeText(this, "h", Toast.LENGTH_SHORT).show();
            new NoInternetLayout.Builder(this, R.layout.activity_main)
                    .animate()
                    .mainTitle("No Internet!")
                    .secondaryText("Please connect to the WiFi or Mobile Data.")
                    .setImage(LayoutImage.SHELL);
        }*/
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

   @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null) {
            AuthorizationResponse response = AuthorizationResponse.fromUri(uri);
// BQB9eGj9cl45TTR8Nc1G-BdnPxQy1SZ3y_V28lEfaNpmXlY3LRhE4bsVfkR4Q0ZJ0j9KTir6ZNy3HWqOGH-Du-mlDYYpYUIOY2IDodyolW_hadtAU465ZRuEGTOEcwbQugu8iDQsYRRt6hztWaZgi5OxtpYXjXhn9Dho_ONm7iyLaHaiYIesftfgtfLRI7MjG7-R43LKKwayJWwuokuMTBPhqHgA_3AVNiqj2K11N941GADoG3KWyotMTsXQ0zwaBa7mOZ_JoQOR4aY-n6OBisywH2UQOU7eSm0ZCXN4rwsGKMe5tqCQ2cFKkkiB_w
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    Stash.put("token", response.getAccessToken());
                    Log.d("response", "GOT AUTH TOKEN");
                    Log.d("response", "TOKEN " + response.getAccessToken());
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
    }

    @Override
    protected void onStart() {
        super.onStart();
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