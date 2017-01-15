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
 * Representa un artista según lastfm
 */
public final class ArtistInfo {
    private String name;
    private String url;
    private Map<String, String> images = new HashMap<>();
    private int listeners;
    private int playCount;
    private List<Tag> tags = new ArrayList<>();
    private String biohtml = "";

    public ArtistInfo(JSONObject artist) throws JSONException {
        name = artist.getString("name");
        url = artist.getString("url");
        JSONArray images = artist.getJSONArray("image");
        for(int i = 0; i < images.length(); i++) {
            this.images.put(images.getJSONObject(i).getString("size"), images.getJSONObject(i).getString("#text"));
        }
        listeners = artist.getJSONObject("stats").getInt("listeners");
        playCount = artist.getJSONObject("stats").getInt("playcount");
        JSONArray tags = artist.getJSONObject("tags").getJSONArray("tag");
        for(int i = 0; i < tags.length(); i++) {
            this.tags.add(new Tag(tags.getJSONObject(i)));
        }
        if(artist.has("bio")) {
            biohtml = artist.getJSONObject("bio").getString("content");
            int i = biohtml.indexOf("<a href=");
            if(i != -1) biohtml = biohtml.substring(0, i);
        }
    }

    public String getName() {
        return name;
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

    public String getBio() {
        return biohtml;
    }
}
