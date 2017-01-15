package me.melchor9000.practicaredesandroid.spotify;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.melchor9000.practicaredesandroid.FutureResponse;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class SpotifyApi {
    private static final String BASE_URL = "https://api.spotify.com/v1";
    private static final OkHttpClient client = new OkHttpClient();

    // https://developer.spotify.com/web-api/user-guide/
    // https://developer.spotify.com/web-api/search-item/

    public static class Search {
        public static FutureResponse<List<Track>> search(String query, String type, int limit, int offset) {
            final FutureResponse<List<Track>> fr = new FutureResponse<>();
            get("/search", map(
                    "q", query,
                    "type", type
            ), limit, offset).setCallback(new FutureResponse.ResponseCallback<JSONObject>() {
                @Override
                public void done(JSONObject o, Exception e) {
                    if(e != null) fr.onFailure(e);
                    else {
                        try {
                            List<Track> previewUrls = new ArrayList<>();
                            JSONArray items = o.getJSONObject("tracks").getJSONArray("items");
                            for(int i = 0; i < items.length(); i++) {
                                previewUrls.add(new Track(items.getJSONObject(i)));
                            }
                            fr.onResponse(previewUrls);
                        } catch (JSONException e1) {
                            fr.onFailure(e1);
                        }
                    }
                }
            });
            return fr;
        }

        public static FutureResponse<List<Track>> search(String query, String type) {
            return search(query, type, 20, 0);
        }
    }

    private static FutureResponse<JSONObject> get(String method, Map<String, Object> values, int limit, int offset) {
        String url = url(method, values, offset, limit);
        Request request = new Request.Builder()
                .url(url)
                .build();

        final FutureResponse<JSONObject> fr = new FutureResponse<>();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                fr.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject json = new JSONObject(response.body().string());
                    if(json.optJSONObject("error") == null)
                        fr.onResponse(json);
                    else
                        fr.onFailure(new RuntimeException(json.getJSONObject("error").getString("message")));
                } catch (JSONException e) {
                    fr.onFailure(e);
                }
            }
        });
        return fr;
    }

    private static String url(String path, Map<String, Object> values, int offset, int limit) {
        String query = BASE_URL + path + "?";
        for(Map.Entry<String, Object> entry : values.entrySet()) {
            query += entry.getKey() + "=" + safeUrlEncoder(entry.getValue().toString()) + "&";
        }
        return query + (limit > 0 ? "&offset=" + offset + "&limit=" + limit : "a");
    }

    private static String safeUrlEncoder(String p) {
        try {
            return URLEncoder.encode(p, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static Map<String, Object> map(Object... a) {
        Map<String, Object> map = new HashMap<>();
        for(int i = 0; i < a.length; i +=2) {
            if(a[i+1] != null) map.put((String) a[i], a[i+1]);
        }
        return map;
    }

    public static class Track {
        private String name, artist, album, url;

        private Track(JSONObject track) throws JSONException {
            name = track.getString("name");
            artist = track.getJSONArray("artists").getJSONObject(0).getString("name");
            album = track.getJSONObject("album").getString("name");
            url = track.optString("preview_url");
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

        @Override
        public String toString() {
            return name + " de " + artist + " - " + album;
        }
    }
}
