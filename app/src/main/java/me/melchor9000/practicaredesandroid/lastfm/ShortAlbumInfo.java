package me.melchor9000.practicaredesandroid.lastfm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public final class ShortAlbumInfo {
    private String name;
    private String artist;
    private String url;
    private Map<String, String> images = new HashMap<>();
    private int playCount;

    public ShortAlbumInfo(JSONObject album) throws JSONException {
        name = album.getString("name");
        artist = album.getJSONObject("artist").getString("name");
        url = album.getString("url");
        JSONArray images = album.getJSONArray("image");
        for(int i = 0; i < images.length(); i++) {
            this.images.put(images.getJSONObject(i).getString("size"), images.getJSONObject(i).getString("#text"));
        }
        playCount = album.getInt("playcount");
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

    public int getPlayCount() {
        return playCount;
    }

    public String getImageForSize(String size) {
        return images.get(size == null ? "" : size);
    }
}
