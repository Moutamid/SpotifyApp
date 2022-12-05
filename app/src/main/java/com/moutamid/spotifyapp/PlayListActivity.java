package com.moutamid.spotifyapp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
    ProgressDialog progressDialog, pd;
    ArrayList<String> songslist;
    ArrayList<SongModel> songs;
    ArrayList<SongModel> sortedSongs;
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

        pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setTitle("Please Wait...");
        pd.setMessage("Creating A Playlist In Spotify");

        rc = findViewById(R.id.songRC);
        songs = new ArrayList<>();
        sort = findViewById(R.id.sort);
        create = findViewById(R.id.create);

        rc.setLayoutManager(new LinearLayoutManager(this));
        rc.setHasFixedSize(false);

        selectedArtists = new ArrayList<>();
        songslist = new ArrayList<>();
        sortedSongs = new ArrayList<>();
        selectedArtists = Stash.getArrayList("selectedArtists", ArtistModel.class);

        CLIENT_ID = getResources().getString(R.string.CLIENT_ID);

        token = Stash.getString("token");
        Log.d("token12", token);

        requestQueue = VolleySingleton.getmInstance(PlayListActivity.this).getRequestQueue();

        sort.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                for (SongModel s: songs) {
                    Log.d("token12", "Songs : " + s.getKey());
                }
                songs.sort(Comparator.comparing(SongModel::getKey));
                sortedSongs = songs;
                //Collections.reverse(sortedSongs);
                for (SongModel s: sortedSongs) {
                    Log.d("token12", "Sorted : " + s.getKey());
                }
                Stash.put("Sorted", sortedSongs);
                adapter = new SongAdapter(PlayListActivity.this, sortedSongs);
                rc.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });

        create.setOnClickListener(v -> {
            if(sortedSongs.size()>0){
                pd.show();
                new AddPlayListTask().execute("");
            } else {
                Toast.makeText(this, "Please Sort the Songs First", Toast.LENGTH_SHORT).show();
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

    public class AddPlayListTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            SpotifyApi api = new SpotifyApi();
            api.setAccessToken(token);

            SpotifyService service = api.getService();
            UserPrivate user = service.getMe();

            Map<String, Object> map = new HashMap<>();
            Map<String, Object> query = new HashMap<>();
            Map<String, Object> body = new HashMap<>();

            map.put("name", "MyPlaylist");
            map.put("public", false);
            map.put("collaborative", false);
            map.put("description", "MyPlaylist");

            String queries = "";

            query.put("position", 0);
            body.put("position", 0);

            for (int i =0; i<sortedSongs.size(); i++){
                queries = queries + "spotify:track:" + sortedSongs.get(i).getTrackID() + ",";
                query.put("uris", queries);
            }
            SnapshotId addtrack = service.addTracksToPlaylist(user.id, sortedSongs.get(0).getPlaylistID(), query, query);
            Log.d("Tracks", addtrack.snapshot_id);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            Toast.makeText(getApplicationContext(), "Playlist Added Successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), ArtistActivity.class));
            finish();
        }
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

            Log.d("Checking12", "Async");

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

            for (int i=0; i<selectedArtists.size(); i++) {
                Log.d("Checking12", "for i: " + i);
                Tracks tracks = service.getArtistTopTrack(selectedArtists.get(i).getId(), user.country);
                List<Track> trackslist = tracks.tracks;
                for (int j=0; j < trackslist.size(); j++) {
                    track = trackslist.get(j);
                    Log.d("Checking12", "for : j" + j);
                    // Rude+Boy+artist:Rihanna
                    String name = track.name;
                    String artist="";
                    if (name.contains(" ")){
                        name = name.replace(" ", "+");
                    }
                    if (track.artists.size() > 0){
                        artist = track.artists.get(0).name;
                        if (artist.contains(" ")){
                            artist = artist.replace(" ", "+");
                        }
                    }
//                  https://api.getsongbpm.com/search/?api_key=35a177f0197792725c4d047c987c60d2&type=both&lookup=song:Rude+Boy+artist:Rihanna
//                  String url = "https://api.getsongbpm.com/search/?api_key=35a177f0197792725c4d047c987c60d2&type=both&lookup=song:" + name + "+artist:" + artist;
                    String url = "https://songdata.io/track/" + track.id;
                    Log.d("Checking12", "track ID : " + track.id);
                    Log.d("Checking12", "URL : " + url);

                    try {
                        Document doc = Jsoup.connect(url).get();
                        Elements nodes = doc.getElementsByClass("py-1");
                        Element content = nodes.get(3);
                        Element value = content.child(1);
                        Log.d("Checking12", "Html" + value.text().toString());
                        songs.add(new SongModel(user.id, user.country, track.id, playlist.id, value.text().toString(), track.name, track.type, "", track.artists));
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            adapter = new SongAdapter(PlayListActivity.this, songs);
                            rc.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    /*Track finalTrack = track;
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            // 28EkxM2v1o3V5o4tz8wPWH

                            Log.d("Checking12", "JSON");
                            JSONArray object = response.getJSONArray("search");
                            String key = object.getJSONObject(0).getString("open_key");

                            Log.d("Checking12", "JSON OBJ : " + key);

                            // Toast.makeText(PlayListActivity.this, key + "\n" + finalTrack.id, Toast.LENGTH_SHORT).show();
                            songs.add(new SongModel(user.id, user.country, finalTrack.id, playlist.id, key, finalTrack.name, finalTrack.type, "", finalTrack.artists));
                            runOnUiThread(() -> {
                                adapter = new SongAdapter(PlayListActivity.this, songs);
                                rc.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            });
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        Log.d("Checking12", "doInBackground: " + error.getMessage());
                    });
                    requestQueue.add(jsonObjectRequest);*/
                    /*queries = queries + "spotify:track:" + track.id + ",";
                    query.put("uris", queries);*/
                }
                /* SnapshotId addtrack = service.addTracksToPlaylist(user.id, playlist.id, query, query);
                Log.d("Tracks", addtrack.snapshot_id); */
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