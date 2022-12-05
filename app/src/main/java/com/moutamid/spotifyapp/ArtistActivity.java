package com.moutamid.spotifyapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.moutamid.spotifyapp.adapters.ArtistsAdapter;
import com.moutamid.spotifyapp.adapters.ChipsAdapter;
import com.moutamid.spotifyapp.listners.ArtistClickListen;
import com.moutamid.spotifyapp.listners.ChipsClick;
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

    RecyclerView rc, chipRC;
    EditText editText;
    Button search, next;
    ArrayList<ArtistModel> list;
    ArrayList<ArtistModel> chips;
    ArtistModel model, chipsModel;
    ArtistsAdapter adapter;
    ChipsAdapter chipsAdapter;
    private SpotifyAppRemote mSpotifyAppRemote;
    private static final String REDIRECT_URI = "spotify-app:/oauth";
    private String CLIENT_ID, token;
    int limit = 0;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        rc = findViewById(R.id.rc);
        chipRC = findViewById(R.id.chips);
        editText = findViewById(R.id.editText);
        search = findViewById(R.id.search);
        next = findViewById(R.id.next);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setMessage("Fetching All the Artists");

        rc.setLayoutManager(new LinearLayoutManager(this));
        rc.setHasFixedSize(false);

//        chipRC.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        chipRC.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL));
        chipRC.setHasFixedSize(false);

        CLIENT_ID = getResources().getString(R.string.CLIENT_ID);

        token = Stash.getString("token");
        Log.d("Artists", token);

        list = new ArrayList<>();
        chips = new ArrayList<>();

        chipsModel = new ArtistModel();

        next.setOnClickListener(v -> {
            if (chips.size() > 0){
                Stash.put("selectedArtists", chips);
                startActivity(new Intent(this, PlayListActivity.class));
            } else {
                Toast.makeText(this, "Please Select At Least 1 Artist", Toast.LENGTH_SHORT).show();
            }
        });

        search.setOnClickListener(v -> {
            if (editText.getText().toString().isEmpty()){
                Toast.makeText(this, "Please Provide A Name", Toast.LENGTH_SHORT).show();
            } else {
                progressDialog.show();
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
            progressDialog.dismiss();
        }
    }

    ChipsClick chipsClick = new ChipsClick() {
        @Override
        public void onClick(int position) {
            --limit;
            chips.remove(position);
            chipsAdapter.notifyItemRemoved(position);
        }
    };

    ArtistClickListen clickListen = new ArtistClickListen() {
        @Override
        public void onClick(ArtistModel model) {
            chipsModel = model;
            if (limit < 4){
                ++limit;
                addChips();
            } else {
                Toast.makeText(ArtistActivity.this, "4 Artist already selected", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void addChips() {
        chips.add(chipsModel);
        chipsAdapter = new ChipsAdapter(ArtistActivity.this, chips, chipsClick);
        chipRC.setAdapter(chipsAdapter);
    }

}