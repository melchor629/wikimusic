package me.melchor9000.practicaredesandroid.lastfm;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Resultado de un artista al buscar aristas en lastfm
 */
public final class ArtistSearchResult extends SearchResult {
    private int listeners;

    public ArtistSearchResult(JSONObject o) throws JSONException {
        super(o);
        listeners = o.getInt("listeners");
    }

    public int getListeners() {
        return listeners;
    }

    @Override
    public String toString() {
        return "["+this.name+", "+this.url+", "+listeners+", "+images+"]";
    }
}
