package com.moutamid.spotifyapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.moutamid.spotifyapp.adapters.ArtistsAdapter;
import com.moutamid.spotifyapp.listners.ArtistClickListen;
import com.moutamid.spotifyapp.models.ArtistModel;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

public class ArtistActivity extends AppCompatActivity {

    RecyclerView rc;
    EditText editText;
    Button search;
    ArrayList<ArtistModel> list;
    ArtistModel model;
    ArtistsAdapter adapter;
    private SpotifyAppRemote mSpotifyAppRemote;
    private static final String REDIRECT_URI = "spotify-app:/oauth";
    private String CLIENT_ID, token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        rc = findViewById(R.id.rc);
        editText = findViewById(R.id.editText);
        search = findViewById(R.id.search);

        rc.setLayoutManager(new LinearLayoutManager(this));
        rc.setHasFixedSize(false);

        CLIENT_ID = getResources().getString(R.string.CLIENT_ID);

        token = Stash.getString("token");
        Log.d("Artists", token);

        list = new ArrayList<>();

        search.setOnClickListener(v -> {
            if (editText.getText().toString().isEmpty()){
                Toast.makeText(this, "Please Provide A Name", Toast.LENGTH_SHORT).show();
            } else {
                new SearchSpotifyTask(this).execute(editText.getText().toString());
//                rc.getAdapter().notifyDataSetChanged();
            }
        });


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
                            Toast.makeText(ArtistActivity.this, "connected", Toast.LENGTH_SHORT).show();
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

    public class SearchSpotifyTask extends AsyncTask<String, String, String> {
        ArtistActivity artistActivity;

        public SearchSpotifyTask(ArtistActivity artistActivity) {
            this.artistActivity = artistActivity;
        }

        @Override
        protected String doInBackground(String... strings) {
            SpotifyApi api = new SpotifyApi();
            api.setAccessToken(token);
            /*if (Looper.myLooper() == null) {
                Looper.prepare();
            }*/
            // Toast.makeText(ArtistActivity.this, strings[0], Toast.LENGTH_SHORT).show();
            SpotifyService service = api.getService();
            ArtistsPager results = service.searchArtists(strings[0]);
            List<Artist> artists = results.artists.items;
            list.clear();
            runOnUiThread(() -> {
                for (int i = 0; i < artists.size(); i++) {
                    Artist artist = artists.get(i);
                    // Paul
                    if(artist.images.size() > 0 && artist.genres.size() > 0){
                        model = new ArtistModel(artist.id, artist.images.get(0).url, artist.name, artist.genres.get(0), artist.followers.total);
                    } else {
                        model = new ArtistModel(artist.id, "", artist.name, artist.type, artist.followers.total);
                    }
                    list.add(model);
                    adapter = new ArtistsAdapter(ArtistActivity.this, list, clickListen);
                    rc.setAdapter(adapter);
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    ArtistClickListen clickListen = new ArtistClickListen() {
        @Override
        public void onClick(ArtistModel model) {
            Toast.makeText(ArtistActivity.this, model.getName() + "\n" + model.getId(), Toast.LENGTH_SHORT).show();
        }
    };

}