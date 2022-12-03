package com.moutamid.spotifyapp.models;

import java.io.Serializable;

public class ArtistModel implements Serializable {
    String id, image, name, type;
    int followers;

    public ArtistModel() {
    }

    public ArtistModel(String id, String image, String name, String type, int followers) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.type = type;
        this.followers = followers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }
}
