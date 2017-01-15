package me.melchor9000.practicaredesandroid.lastfm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Resultado de buscar algo<br>
 * Sirve para recoger comportamiento común entre los distintos tipos de resultados de búsqueda
 */
public abstract class SearchResult {
    String name;
    String url;
    Map<String, String> images = new HashMap<>();

    public SearchResult(JSONObject o) throws JSONException {
        name = o.getString("name");
        url = o.getString("url");
        JSONArray images = o.getJSONArray("image");
        for(int i = 0; i < images.length(); i++) {
            this.images.put(images.getJSONObject(i).getString("size"), images.getJSONObject(i).getString("#text"));
        }
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getImageForSize(String size) {
        return images.get(size);
    }
}
