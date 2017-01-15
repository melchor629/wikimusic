package me.melchor9000.practicaredesandroid.lastfm;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Resultado de un álbum al buscar álbumes en lastfm
 */
public final class AlbumSearchResult extends SearchResult {
    private String artist;


    public AlbumSearchResult(JSONObject o) throws JSONException {
        super(o);
        artist = o.getString("artist");
    }

    public String getArtist() {
        return artist;
    }
}
