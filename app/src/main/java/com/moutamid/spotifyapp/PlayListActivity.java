package com.moutamid.spotifyapp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fxn.stash.Stash;
import com.moutamid.spotifyapp.adapters.ArtistsAdapter;
import com.moutamid.spotifyapp.adapters.SongAdapter;
import com.moutamid.spotifyapp.models.ArtistModel;
import com.moutamid.spotifyapp.models.SongModel;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistsPager;
import kaaes.spotify.webapi.android.models.SnapshotId;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import kaaes.spotify.webapi.android.models.TracksPager;
import kaaes.spotify.webapi.android.models.UserPrivate;

public class PlayListActivity extends AppCompatActivity {

    ArrayList<ArtistModel> selectedArtists;
    private SpotifyAppRemote mSpotifyAppRemote;
    private static final String REDIRECT_URI = "spotify-app:/oauth";
    private String CLIENT_ID, token;
    ProgressDialog progressDialog;
    ArrayList<String> songslist;
    ArrayList<SongModel> songs;
    SongAdapter adapter;
    RecyclerView rc;
    Button sort, create;

    private RequestQueue requestQueue;

    // 35a177f0197792725c4d047c987c60d2

    // https://api.getsongbpm.com/song/?api_key=35a177f0197792725c4d047c987c60d2&id=983pB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setMessage("Creating A Playlist");

        rc = findViewById(R.id.songRC);
        songs = new ArrayList<>();
        sort = findViewById(R.id.sort);
        create = findViewById(R.id.create);

        rc.setLayoutManager(new LinearLayoutManager(this));
        rc.setHasFixedSize(false);

        selectedArtists = new ArrayList<>();
        songslist = new ArrayList<>();
        selectedArtists = Stash.getArrayList("selectedArtists", ArtistModel.class);

        CLIENT_ID = getResources().getString(R.string.CLIENT_ID);

        token = Stash.getString("token");
        Log.d("Artists", token);

        requestQueue = VolleySingleton.getmInstance(PlayListActivity.this).getRequestQueue();

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
                            progressDialog.show();
                            new SearchSpotifyTask().execute("");
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

        @Override
        protected String doInBackground(String... strings) {
            SpotifyApi api = new SpotifyApi();
            api.setAccessToken(token);
            /*if (Looper.myLooper() == null) {
                Looper.prepare();
            }*/
            // Toast.makeText(ArtistActivity.this, strings[0], Toast.LENGTH_SHORT).show();
            SpotifyService service = api.getService();
            UserPrivate user = service.getMe();
            Map<String, Object> map = new HashMap<>();
            Map<String, Object> query = new HashMap<>();
            Map<String, Object> body = new HashMap<>();
            String queries = "";

            map.put("name", "MyPlaylist");
            map.put("public", false);
            map.put("collaborative", false);
            map.put("description", "MyPlaylist");

            query.put("position", 0);
            body.put("position", 0);

            Playlist playlist = service.createPlaylist(user.id, map);
            Track track;

            for (int i=0; i< selectedArtists.size(); i++) {
                Tracks tracks = service.getArtistTopTrack(selectedArtists.get(i).getId(), user.country);
                List<Track> trackslist = tracks.tracks;
                for (int j=0; j < trackslist.size(); j++) {
                    track = trackslist.get(j);
                    String url = "https://api.getsongbpm.com/song/?api_key=35a177f0197792725c4d047c987c60d2&id=" + track.id;
                    Track finalTrack = track;
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONObject object = response.getJSONObject("song");
                            String key = object.getString("camelot");
                            String image = object.getString("img");
                            Toast.makeText(PlayListActivity.this, key + "\n" + finalTrack.id, Toast.LENGTH_SHORT).show();
                            songs.add(new SongModel(user.id, user.country, finalTrack.id, playlist.id, key, finalTrack.name, finalTrack.type, "", finalTrack.artists));
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        Log.d(TAG, "doInBackground: " + error.getMessage());
                    });
                    requestQueue.add(jsonObjectRequest);
                    runOnUiThread(() -> {
                        adapter = new SongAdapter(PlayListActivity.this, songs);
                        rc.setAdapter(adapter);
                    });
                    /*queries = queries + "spotify:track:" + track.id + ",";
                    query.put("uris", queries);*/
                }
                /*SnapshotId addtrack = service.addTracksToPlaylist(user.id, playlist.id, query, query);
                Log.d("Tracks", addtrack.snapshot_id);*/
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
        }
    }

}