package me.melchor9000.practicaredesandroid;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jaeger.library.StatusBarUtil;
import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.AutoLinkOnClickListener;
import com.luseen.autolinklibrary.AutoLinkTextView;
import com.squareup.picasso.Picasso;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.melchor9000.practicaredesandroid.lastfm.LastFMApi;
import me.melchor9000.practicaredesandroid.lastfm.Tag;
import me.melchor9000.practicaredesandroid.lastfm.TrackInfo;
import me.melchor9000.practicaredesandroid.spotify.SpotifyApi;

public class TrackActivity extends AppCompatActivity {
    @BindView(R.id.track_image) ImageView image;
    @BindView(R.id.track_bio) AutoLinkTextView bio;
    @BindView(R.id.track_listeners) TextView listeners;
    @BindView(R.id.track_name) TextView name;
    @BindView(R.id.track_artist) TextView artist;
    @BindView(R.id.track_album) TextView album;
    @BindView(R.id.track_duration) TextView duration;
    @BindView(R.id.track_scrobbles) TextView scrobbles;
    @BindView(R.id.track_tagsLayout) LinearLayout tagsLayout;
    @BindView(R.id.track_listenersLayout) RelativeLayout listenersLayout;
    @BindView(R.id.track_scrobblesLayout) RelativeLayout scrobblesLayout;
    @BindView(R.id.progressBar) ProgressBar progressBar;
    @BindView(R.id.track_layout) LinearLayout layout;
    private final Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        ButterKnife.bind(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        StatusBarUtil.setTranslucent(this);

        Intent i = getIntent();
        if(i == null) {
            finish();
            return;
        }

        //Pedimos la info a LastFM
        LastFMApi.Track.getInfo(i.getStringExtra("track"), i.getStringExtra("artist"), Locale.getDefault().getLanguage()).setCallback(new FutureResponse.ResponseCallback<TrackInfo>() {
            @Override
            public void done(final TrackInfo o, final Exception e) {
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        showInfo(o, e);
                    }
                });
            }
        });

        //La magia de la imágen cuadrada
        // http://stackoverflow.com/questions/3779173/determining-the-size-of-an-android-view-at-runtime
        findViewById(R.id.activity_track).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ActivityUtils.onLinearLayoutChanged(image, findViewById(R.id.activity_track), TrackActivity.this);
            }
        });

        //Si apretamos el artista, vamos a la pantalla del artista
        artist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(TrackActivity.this, ArtistActivity.class);
                i.putExtra("artist", artist.getText());
                startActivity(i);
            }
        });

        //Si apretamos el álbum, vamos a la pantalla del álbum
        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(TrackActivity.this, AlbumActivity.class);
                i.putExtra("artist", artist.getText());
                i.putExtra("album", album.getText());
                startActivity(i);
            }
        });

        //Si apretamos la duración, intentamos poner la muestra de 30s de Spotify
        duration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder sb = new StringBuilder(name.getText());
                sb.append(" artist:").append(artist.getText());
                if(album.getText().length() != 0)
                    sb.append(" album:").append(album.getText().toString().replace(" EP", ""));
                SpotifyApi.Search.search(sb.toString(), "track").setCallback(new FutureResponse.ResponseCallback<List<SpotifyApi.Track>>() {
                    @Override
                    public void done(List<SpotifyApi.Track> o, Exception e) {
                        if(o != null) {
                            if(o.size() > 0) {
                                Iterator<SpotifyApi.Track> it = o.iterator();
                                boolean done = false;
                                while(it.hasNext() && !done) {
                                    final SpotifyApi.Track track = it.next();
                                    done = track.getName().toLowerCase().contains(name.getText().toString().toLowerCase());
                                    if(done) {
                                        Intent intent = new Intent(TrackActivity.this, PlayerService.class);
                                        intent.putExtra("url", track.getUrl());
                                        startService(intent);
                                        h.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(TrackActivity.this, track.toString(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                }
                            } else {
                                h.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(TrackActivity.this, getString(R.string.no_preview), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } else {
                            Log.e("TrackActivity", ":(", e);
                        }
                    }
                });
            }
        });

        //La magia de los enlaces en la biografía
        bio.addAutoLinkMode(AutoLinkMode.MODE_URL);
        bio.setAutoLinkOnClickListener(new AutoLinkOnClickListener() {
            @Override
            public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                while(matchedText.charAt(0) == ' ') matchedText = matchedText.substring(1);
                if(!matchedText.startsWith("http")) matchedText = "http://" + matchedText;
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(matchedText));
                startActivity(i);
            }
        });

        onConfigurationChanged(null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if(newConfig != null) super.onConfigurationChanged(newConfig);
        //La magia para que la barra de botones de abajo no se ponga encima del contenido
        ActivityUtils.onConfigurationChanged(layout, this);
    }

    private void showInfo(TrackInfo o, Exception e) {
        if(e != null) {
            Toast.makeText(this, "Error!! " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } else {
            //Mostramos la información de la canción en la pantalla
            if(o.getAlbumImageForSize("extralarge") != null && !o.getAlbumImageForSize("extralarge").isEmpty())
                Picasso.with(this).load(o.getAlbumImageForSize("extralarge")).fit().into(image);
            bio.setAutoLinkText(o.getBio());
            listeners.setText(String.format(Locale.getDefault(), "%d", o.getListeners()));
            name.setText(o.getName());
            artist.setText(o.getArtist());
            album.setText(o.getAlbum());
            scrobbles.setText(String.format(Locale.getDefault(), "%d", o.getPlayCount()));
            duration.setText(o.getDurationAsString());
            Iterator<Tag> tags = o.getTags();
            while(tags.hasNext()) {
                final Tag tag = tags.next();
                View tagView = LayoutInflater.from(this)
                        .inflate(R.layout.tag, tagsLayout, false);
                ((TextView) tagView.findViewById(R.id.tag_name)).setText(tag.getName());
                tagsLayout.addView(tagView);
                tagView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(tag.getUrl()));
                        startActivity(i);
                    }
                });
            }

            //Si no hay una biografía decente, se busca en inglés
            if(o.getBio().length() < 10) {
                LastFMApi.Track.getInfo(o.getName(), o.getArtist(), null).setCallback(new FutureResponse.ResponseCallback<TrackInfo>() {
                    @Override
                    public void done(final TrackInfo o, Exception e) {
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                bio.setAutoLinkText(o.getBio());
                            }
                        });
                    }
                });
            }
        }
        progressBar.setVisibility(View.INVISIBLE);
    }
}
