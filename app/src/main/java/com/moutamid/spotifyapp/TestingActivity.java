package com.moutamid.spotifyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.LibraryState;
import com.spotify.protocol.types.Track;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.PlaylistsPager;

public class TestingActivity extends AppCompatActivity {

    private TextView userView;
    private TextView songView;
    private Button addBtn;
    private Song song;
    private SpotifyAppRemote mSpotifyAppRemote;
    private SongService songService;
    private ArrayList<Song> recentlyPlayedTracks;
    private static final String REDIRECT_URI = "spotify-app:/oauth";
    private String CLIENT_ID, token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);

        songService = new SongService(getApplicationContext());
        addBtn = (Button) findViewById(R.id.add);

        CLIENT_ID = getResources().getString(R.string.CLIENT_ID);

        token = Stash.getString("token");
        Log.d("Artists", token);

        SharedPreferences sharedPreferences = this.getSharedPreferences("SPOTIFY", 0);
        // userView.setText(sharedPreferences.getString("userid", "No User"));

        // getTracks();

        addBtn.setOnClickListener(v -> {
            EditText ee = findViewById(R.id.editText);
            SearchSpotifyTask task = new SearchSpotifyTask();
            task.execute(ee.getText().toString());
        });
    }

    private View.OnClickListener addListener = v -> {
        songService.addSongToLibrary(this.song);
        if (recentlyPlayedTracks.size() > 0) {
            recentlyPlayedTracks.remove(0);
        }
        updateSong();
    };

    private void getTracks() {
        songService.getRecentlyPlayedTracks(() -> {
            recentlyPlayedTracks = songService.getSongs();
            updateSong();
        });
    }

    private void updateSong() {
        if (recentlyPlayedTracks.size() > 0) {
            songView.setText(recentlyPlayedTracks.get(0).getName());
            song = recentlyPlayedTracks.get(0);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
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
                            Toast.makeText(TestingActivity.this, "connected", Toast.LENGTH_SHORT).show();
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
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }
    
    public class SearchSpotifyTask extends AsyncTask<String, String, String>
    {
        @Override
        protected String doInBackground(String... strings) {
            SpotifyApi api = new SpotifyApi();
            api.setAccessToken(token);
            SpotifyService service = api.getService();
            ArtistsPager results = service.searchArtists("Paul");
            List<Artist> artists = results.artists.items;
            for (int i = 0; i < artists.size(); i++) {
                Artist artist = artists.get(i);
                // Paul
                Log.i("Artists", i + " " + artist.name);
            }
            return null;
        }
    }

}