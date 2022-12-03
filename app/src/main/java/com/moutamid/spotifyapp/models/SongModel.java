package com.moutamid.spotifyapp.models;

import java.io.Serializable;
import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;

public class SongModel implements Serializable {
    String userID, userCount, trackID, playlistID, key;
    String name, type, image;
    List<ArtistSimple> artistList;

    public SongModel() {}

    public SongModel(String userID, String userCount, String trackID, String playlistID, String key, String name, String type, String image, List<ArtistSimple> artistList) {
        this.userID = userID;
        this.userCount = userCount;
        this.trackID = trackID;
        this.playlistID = playlistID;
        this.key = key;
        this.name = name;
        this.type = type;
        this.image = image;
        this.artistList = artistList;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserCount() {
        return userCount;
    }

    public void setUserCount(String userCount) {
        this.userCount = userCount;
    }

    public String getTrackID() {
        return trackID;
    }

    public void setTrackID(String trackID) {
        this.trackID = trackID;
    }

    public String getPlaylistID() {
        return playlistID;
    }

    public void setPlaylistID(String playlistID) {
        this.playlistID = playlistID;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<ArtistSimple> getArtistList() {
        return artistList;
    }

    public void setArtistList(List<ArtistSimple> artistList) {
        this.artistList = artistList;
    }
}
