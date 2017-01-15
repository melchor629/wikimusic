package me.melchor9000.practicaredesandroid.lastfm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class TrackInfo {
    private String name;
    private String artist;
    private String album;
    private String url;
    private Map<String, String> images = new HashMap<>();
    private int listeners;
    private int playCount;
    private int duration;
    private List<Tag> tags = new ArrayList<>();
    private String biohtml = "";

    public TrackInfo(JSONObject track) throws JSONException {
        name = track.getString("name");
        duration = track.getInt("duration");
        url = track.getString("url");
        artist = track.getJSONObject("artist").getString("name");
        if(track.has("album")) {
            album = track.getJSONObject("album").getString("title");
            JSONArray images = track.getJSONObject("album").getJSONArray("image");
            for(int i = 0; i < images.length(); i++) {
                this.images.put(images.getJSONObject(i).getString("size"), images.getJSONObject(i).getString("#text"));
            }
        } else {
            album = "";
            images.put("", "");
        }
        listeners = track.getInt("listeners");
        playCount = track.getInt("playcount");
        JSONArray tags = track.getJSONObject("toptags").getJSONArray("tag");
        for(int i = 0; i < tags.length(); i++) {
            this.tags.add(new Tag(tags.getJSONObject(i)));
        }
        if(track.has("wiki")) {
            biohtml = track.getJSONObject("wiki").getString("content");
            int i = biohtml.indexOf("<a href=");
            if(i != -1) biohtml = biohtml.substring(0, i);
        }
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getUrl() {
        return url;
    }

    public String getAlbumImageForSize(String size) {
        return images.get(size == null ? "" : size);
    }

    public int getListeners() {
        return listeners;
    }

    public int getPlayCount() {
        return playCount;
    }

    public int getDuration() {
        return duration;
    }

    public String getDurationAsString() {
        int sec = (duration / 1000) % 60;
        int min = (duration / 1000) / 60 % 60;
        int hour= (duration / 1000) / 3600;

        String d = (min < 10 ? "0" + min : min) + ":" + (sec < 10 ? "0" + sec : sec);
        if(hour != 0) d = hour + ":" + d;
        return d;
    }

    public Iterator<Tag> getTags() {
        return tags.iterator();
    }

    public String getBio() {
        return biohtml;
    }
}
