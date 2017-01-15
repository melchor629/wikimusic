package me.melchor9000.practicaredesandroid.lastfm;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Un <i>tag</i> de lastfm
 */
public final class Tag {
    private String name, url;

    Tag(JSONObject o) throws JSONException {
        name = o.getString("name");
        url = o.getString("url");
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
