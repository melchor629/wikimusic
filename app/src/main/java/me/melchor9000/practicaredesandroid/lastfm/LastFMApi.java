package me.melchor9000.practicaredesandroid.lastfm;

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

/**
 * Realiza peticiones a LastFM
 */
public final class LastFMApi {
    private static final String API_KEY = "201a5fdd42fd8cc5577fd0646b3e8ba7";
    private static final String BASE_URL = "http://ws.audioscrobbler.com/2.0/";
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * Métodos para álbumes
     */
    public static class Album {

        /**
         * Busca álbumes que coincidan con lo que se busca
         * @param album álbum a buscar
         * @param limit número de álbumes por página
         * @param page página de resultados (empieza en 1)
         * @return un resultado que ya vendrá
         */
        public static FutureResponse<List<AlbumSearchResult>> search(String album, final int limit, int page) {
            final FutureResponse<List<AlbumSearchResult>> fr = new FutureResponse<>();
            get("album.search", map(
                "limit", limit,
                "page", page,
                "album", album
            )).setCallback(new FutureResponse.ResponseCallback<JSONObject>() {
                @Override
                public void done(JSONObject o, Exception e) {
                    if(e != null) fr.onFailure(e);
                    else {
                        try {
                            List<AlbumSearchResult> list = new ArrayList<>();
                            JSONArray albums = o.getJSONObject("results").getJSONObject("albummatches").getJSONArray("album");
                            //A veces LastFM nos devuelve mas items de lo que toca, yo pongo solo los ultimos
                            //que son los que nos interesan siempre
                            for(int i = Math.max(0, albums.length() - limit); i < albums.length(); i++) {
                                list.add(new AlbumSearchResult(albums.getJSONObject(i)));
                            }
                            fr.onResponse(list);
                        } catch (JSONException e1) {
                            fr.onFailure(e1);
                        }
                    }
                }
            });
            return fr;
        }

        /**
         * Obtiene la información de un álbum de un artista en un idioma dado o en inglés
         * @param album álbum
         * @param artist artista del álbum
         * @param lang idioma, o {@code null} para inglés
         * @return un resultado que ya vendrá
         */
        public static FutureResponse<AlbumInfo> getInfo(String album, String artist, String lang) {
            final FutureResponse<AlbumInfo> fr = new FutureResponse<>();
            get("album.getInfo", map(
                    "album", album,
                    "artist", artist,
                    "lang", lang
            )).setCallback(new FutureResponse.ResponseCallback<JSONObject>() {
                @Override
                public void done(JSONObject o, Exception e) {
                    if(e != null) fr.onFailure(e);
                    else {
                        try {
                            fr.onResponse(new AlbumInfo(o.getJSONObject("album")));
                        } catch (JSONException e1) {
                            fr.onFailure(e1);
                        }
                    }
                }
            });
            return fr;
        }
    }

    /**
     * Métodos para artistas
     */
    public static class Artist {

        /**
         * Busca artistas que coincidan con lo que se busca
         * @param artist artista a buscar
         * @param limit cantidad de resultados por página
         * @param page numero de la página de resultados (empieza en 1)
         * @return un resultado que ya vendrá
         */
        public static FutureResponse<List<ArtistSearchResult>> search(String artist, final int limit, int page) {
            final FutureResponse<List<ArtistSearchResult>> fr = new FutureResponse<>();
            get("artist.search", map(
                    "limit", limit,
                    "page", page,
                    "artist", artist
            )).setCallback(new FutureResponse.ResponseCallback<JSONObject>() {
                @Override
                public void done(JSONObject o, Exception e) {
                    if(e != null) fr.onFailure(e);
                    else {
                        try {
                            List<ArtistSearchResult> list = new ArrayList<>();
                            JSONArray albums = o.getJSONObject("results").getJSONObject("artistmatches").getJSONArray("artist");
                            for(int i = Math.max(0, albums.length() - limit); i < albums.length(); i++) {
                                list.add(new ArtistSearchResult(albums.getJSONObject(i)));
                            }
                            fr.onResponse(list);
                        } catch (JSONException e1) {
                            fr.onFailure(e1);
                        }
                    }
                }
            });
            return fr;
        }

        /**
         * Obtiene la información de un artista en un idioma
         * @param artist artista del cual obtener información
         * @param lang idioma en la que presentar el texto o {@code null} para inglés
         * @return un resultado que ya vendrá
         */
        public static FutureResponse<ArtistInfo> getInfo(String artist, String lang) {
            final FutureResponse<ArtistInfo> fr = new FutureResponse<>();
            get("artist.getInfo", map(
                    "artist", artist,
                    "lang", lang
            )).setCallback(new FutureResponse.ResponseCallback<JSONObject>() {
                @Override
                public void done(JSONObject o, Exception e) {
                    if(e != null) fr.onFailure(e);
                    else {
                        try {
                            fr.onResponse(new ArtistInfo(o.getJSONObject("artist")));
                        } catch (JSONException e1) {
                            fr.onFailure(e1);
                        }
                    }
                }
            });
            return fr;
        }

        /**
         * Obtiene los álbumes mas destacados para un artista
         * @param artist artista
         * @param limit numero de resultados por página
         * @param page página de los resultados (empieza por 1)
         * @return un resultado que ya vendrá
         */
        public static FutureResponse<List<ShortAlbumInfo>> getTopAlbums(String artist, final int limit, int page) {
            final FutureResponse<List<ShortAlbumInfo>> fr = new FutureResponse<>();
            get("artist.getTopAlbums", map(
                    "artist", artist,
                    "limit", limit,
                    "page", page
            )).setCallback(new FutureResponse.ResponseCallback<JSONObject>() {
                @Override
                public void done(JSONObject o, Exception e) {
                    if(e != null) fr.onFailure(e);
                    else {
                        try {
                            JSONArray array = o.getJSONObject("topalbums").getJSONArray("album");
                            List<ShortAlbumInfo> list = new ArrayList<>();
                            for(int i = Math.max(0, array.length() - limit); i < array.length(); i++) {
                                list.add(new ShortAlbumInfo(array.getJSONObject(i)));
                            }
                            fr.onResponse(list);
                        } catch(JSONException e1) {
                            fr.onFailure(e1);
                        }
                    }
                }
            });
            return fr;
        }
    }

    /**
     * Métodos para canciones
     */
    public static class Track {

        /**
         * Busca canciones que coincidan con lo buscado
         * @param track canción a buscar
         * @param artist artista (puede ser {@code null})
         * @param limit cantidad de resultados por página
         * @param page página de los resultados (empieza por 1)
         * @return un resultado que ya vendrá
         */
        public static FutureResponse<List<TrackSearchResult>> search(String track, String artist, final int limit, int page) {
            final FutureResponse<List<TrackSearchResult>> fr = new FutureResponse<>();
            get("track.search", map(
                    "limit", limit,
                    "page", page,
                    "track", track,
                    "artist", artist
            )).setCallback(new FutureResponse.ResponseCallback<JSONObject>() {
                @Override
                public void done(JSONObject o, Exception e) {
                    if(e != null) fr.onFailure(e);
                    else {
                        try {
                            List<TrackSearchResult> list = new ArrayList<>();
                            JSONArray albums = o.getJSONObject("results").getJSONObject("trackmatches").getJSONArray("track");
                            for(int i = Math.max(0, albums.length() - limit); i < albums.length(); i++) {
                                list.add(new TrackSearchResult(albums.getJSONObject(i)));
                            }
                            fr.onResponse(list);
                        } catch (JSONException e1) {
                            fr.onFailure(e1);
                        }
                    }
                }
            });
            return fr;
        }

        /**
         * Obtiene información sobre una canción de un artista en un idioma
         * @param track canción
         * @param artist artista
         * @param lang idioma en la que mostrar el texto o {@code null} para inglés
         * @return un resultado que ya vendrá
         */
        public static FutureResponse<TrackInfo> getInfo(String track, String artist, String lang) {
            final FutureResponse<TrackInfo> fr = new FutureResponse<>();
            get("track.getInfo", map(
                    "track", track,
                    "artist", artist,
                    "lang", lang
            )).setCallback(new FutureResponse.ResponseCallback<JSONObject>() {
                @Override
                public void done(JSONObject o, Exception e) {
                    if(e != null) fr.onFailure(e);
                    else {
                        try {
                            fr.onResponse(new TrackInfo(o.getJSONObject("track")));
                        } catch (JSONException e1) {
                            fr.onFailure(e1);
                        }
                    }
                }
            });
            return fr;
        }
    }

    /**
     * Hace una petición de un método a LastFM con unos parámetros
     * @param method metódo al que invocar
     * @param values parámetros del método
     * @return un resultado que ya vendrá
     */
    private static FutureResponse<JSONObject> get(String method, Map<String, Object> values) {
        String url = BASE_URL + query(method, values);
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
                    if(json.optInt("error") == 0)
                        fr.onResponse(json);
                    else
                        fr.onFailure(new RuntimeException(json.getString("message")));
                } catch (JSONException e) {
                    fr.onFailure(e);
                }
            }
        });
        return fr;
    }

    /**
     * Convierte el método y los valores al query de la petición HTTP. También se añade la
     * API KEY y que devuelva el resultado en json.
     * @param method método
     * @param values parámetros y sus valores
     * @return
     */
    private static String query(String method, Map<String, Object> values) {
        String query = "?method=" + method;
        for(Map.Entry<String, Object> entry : values.entrySet()) {
            query += "&" + entry.getKey() + "=" + safeUrlEncoder(entry.getValue().toString());
        }
        return query + "&format=json&api_key=" + safeUrlEncoder(API_KEY);
    }

    /**
     * URLEncoding pero sin la excepción
     * @param p string a codificar
     * @return string codificado
     */
    private static String safeUrlEncoder(String p) {
        try {
            return URLEncoder.encode(p, "UTF-8");
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
}
