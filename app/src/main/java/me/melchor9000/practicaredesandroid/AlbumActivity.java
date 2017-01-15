package me.melchor9000.practicaredesandroid;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.melchor9000.practicaredesandroid.lastfm.AlbumInfo;
import me.melchor9000.practicaredesandroid.lastfm.LastFMApi;
import me.melchor9000.practicaredesandroid.lastfm.Tag;

public class AlbumActivity extends AppCompatActivity {
    @BindView(R.id.album_image) ImageView image;
    @BindView(R.id.album_bio) AutoLinkTextView bio;
    @BindView(R.id.album_listeners) TextView listeners;
    @BindView(R.id.album_name) TextView name;
    @BindView(R.id.album_artist) TextView artist;
    @BindView(R.id.album_scrobbles) TextView scrobbles;
    @BindView(R.id.album_tagsLayout) LinearLayout tagsLayout;
    @BindView(R.id.album_listenersLayout) RelativeLayout listenersLayout;
    @BindView(R.id.album_scrobblesLayout) RelativeLayout scrobblesLayout;
    @BindView(R.id.album_tracks) LinearLayout tracksLayout;
    @BindView(R.id.progressBar2) ProgressBar progressBar;
    @BindView(R.id.album_layout) LinearLayout layout;
    private final Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        ButterKnife.bind(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        StatusBarUtil.setTranslucent(this);

        Intent i = getIntent();
        if(i == null) {
            finish();
            return;
        }

        //Pedimos la info a LastFM
        LastFMApi.Album.getInfo(i.getStringExtra("album"), i.getStringExtra("artist"), Locale.getDefault().getLanguage()).setCallback(new FutureResponse.ResponseCallback<AlbumInfo>() {
            @Override
            public void done(final AlbumInfo o, final Exception e) {
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
        findViewById(R.id.activity_album).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ActivityUtils.onLinearLayoutChanged(image, findViewById(R.id.activity_album), AlbumActivity.this);
            }
        });

        //Si clickamos en el artista, nos vamos allí
        artist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClass(AlbumActivity.this, ArtistActivity.class);
                i.putExtra("artist", artist.getText());
                startActivity(i);
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

    private void showInfo(AlbumInfo o, Exception e) {
        if(e != null) {
            Toast.makeText(this, "Error!! " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } else {
            //Mostramos la información del álbum en la pantalla
            if(o.getImageForSize("extralarge") != null && !o.getImageForSize("extralarge").isEmpty())
                Picasso.with(this).load(o.getImageForSize("extralarge")).fit().into(image);
            bio.setAutoLinkText(o.getBio());
            listeners.setText(String.format(Locale.getDefault(), "%d", o.getListeners()));
            name.setText(o.getName());
            artist.setText(o.getArtist());
            scrobbles.setText(String.format(Locale.getDefault(), "%d", o.getPlayCount()));

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

            Iterator<AlbumInfo.Track> tracks = o.getTracks();
            int i = 1;
            while(tracks.hasNext()) {
                final AlbumInfo.Track track = tracks.next();
                View trackView = LayoutInflater.from(this)
                        .inflate(R.layout.album_track, tracksLayout, false);
                final TextView title = (TextView) trackView.findViewById(R.id.album_track_title);
                final TextView duration = (TextView) trackView.findViewById(R.id.album_track_duration);
                title.setText(track.getName());
                duration.setText(track.getDurationAsString());
                ((TextView) trackView.findViewById(R.id.album_track_number)).setText(String.format(Locale.getDefault(), "%d", i++));
                tracksLayout.addView(trackView);

                duration.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        title.setPaddingRelative(0, 0, duration.getWidth(), 0);
                        title.requestLayout();
                    }
                });

                trackView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent();
                        i.setClass(AlbumActivity.this, TrackActivity.class);
                        i.putExtra("track", track.getName());
                        i.putExtra("artist", track.getArtist());
                        startActivity(i);
                    }
                });
            }

            //Si no hay una biografía decente, se busca en inglés
            if(o.getBio().length() < 10) {
                LastFMApi.Album.getInfo(o.getName(), o.getArtist(), null).setCallback(new FutureResponse.ResponseCallback<AlbumInfo>() {
                    @Override
                    public void done(final AlbumInfo o, Exception e) {
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
