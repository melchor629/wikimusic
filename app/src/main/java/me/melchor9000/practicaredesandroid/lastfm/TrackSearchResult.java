package me.melchor9000.practicaredesandroid.lastfm;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Un resultado de una canción al buscar canciones
 */
public final class TrackSearchResult extends SearchResult {
    private String artist;

    public TrackSearchResult(JSONObject o) throws JSONException {
        super(o);
        artist = o.getString("artist");
    }

    public String getArtist() {
        return artist;
    }
}
