package me.melchor9000.practicaredesandroid.lastfm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Representa un album según lastfm
 */
public final class AlbumInfo {
    private String name;
    private String artist;
    private String url;
    private Map<String, String> images = new HashMap<>();
    private int listeners;
    private int playCount;
    private List<Tag> tags = new ArrayList<>();
    private List<Track> tracks = new ArrayList<>();
    private String biohtml = "";

    public AlbumInfo(JSONObject album) throws JSONException {
        name = album.getString("name");
        artist = album.getString("artist");
        url = album.getString("url");
        JSONArray images = album.getJSONArray("image");
        for(int i = 0; i < images.length(); i++) {
            this.images.put(images.getJSONObject(i).getString("size"), images.getJSONObject(i).getString("#text"));
        }
        listeners = album.getInt("listeners");
        playCount = album.getInt("playcount");
        JSONArray tags = album.getJSONObject("tags").getJSONArray("tag");
        for(int i = 0; i < tags.length(); i++) {
            this.tags.add(new Tag(tags.getJSONObject(i)));
        }
        JSONArray tracks = album.getJSONObject("tracks").getJSONArray("track");
        for(int i = 0; i < tracks.length(); i++) {
            this.tracks.add(new Track(tracks.getJSONObject(i)));
        }
        if(album.has("wiki")) {
            biohtml = album.getJSONObject("wiki").getString("content");
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

    public String getUrl() {
        return url;
    }

    public String getImageForSize(String size) {
        return images.get(size == null ? "" : size);
    }

    public int getListeners() {
        return listeners;
    }

    public int getPlayCount() {
        return playCount;
    }

    public Iterator<Tag> getTags() {
        return tags.iterator();
    }

    public Iterator<Track> getTracks() {
        return tracks.iterator();
    }

    public String getBio() {
        return biohtml;
    }

    public class Track {
        private String name, url, artist;
        private int duration;

        private Track(JSONObject track) throws JSONException {
            name = track.getString("name");
            url = track.getString("url");
            artist = track.getJSONObject("artist").getString("name");
            duration = track.getInt("duration");
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public String getArtist() {
            return artist;
        }

        public int getDuration() {
            return duration;
        }

        public String getDurationAsString() {
            int sec = duration % 60;
            int min = duration / 60 % 60;
            int hour= duration / 3600;

            String d = (min < 10 ? "0" + min : min) + ":" + (sec < 10 ? "0" + sec : sec);
            if(hour != 0) d = hour + ":" + d;
            return d;
        }
    }
}
