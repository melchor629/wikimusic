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
import android.widget.Button;
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
import butterknife.OnClick;
import me.melchor9000.practicaredesandroid.lastfm.ArtistInfo;
import me.melchor9000.practicaredesandroid.lastfm.LastFMApi;
import me.melchor9000.practicaredesandroid.lastfm.ShortAlbumInfo;
import me.melchor9000.practicaredesandroid.lastfm.Tag;

import static android.view.View.GONE;

public class ArtistActivity extends AppCompatActivity {
    @BindView(R.id.artist_image) ImageView image;
    @BindView(R.id.artist_bio) AutoLinkTextView bio;
    @BindView(R.id.artist_listeners) TextView listeners;
    @BindView(R.id.artist_name) TextView name;
    @BindView(R.id.artist_scrobbles) TextView scrobbles;
    @BindView(R.id.artist_tagsLayout) LinearLayout tagsLayout;
    @BindView(R.id.artist_listenersLayout) RelativeLayout listenersLayout;
    @BindView(R.id.artist_scrobblesLayout) RelativeLayout scrobblesLayout;
    @BindView(R.id.progressBar3) ProgressBar progressBar;
    @BindView(R.id.artist_albums_list) LinearLayout albumsList;
    @BindView(R.id.artist_more_albums) Button moreAlbums;
    @BindView(R.id.artist_layout) LinearLayout layout;
    private int page = 1;
    private final Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);
        ButterKnife.bind(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        StatusBarUtil.setTranslucent(this);

        Intent i = getIntent();
        if(i == null) {
            finish();
            return;
        }

        //Pedimos la info a LastFM
        name.setText(i.getStringExtra("artist"));
        LastFMApi.Artist.getInfo(i.getStringExtra("artist"), Locale.getDefault().getLanguage()).setCallback(new FutureResponse.ResponseCallback<ArtistInfo>() {
            @Override
            public void done(final ArtistInfo o, final Exception e) {
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        showInfo(o, e);
                    }
                });
            }
        });

        //Pedimos los 5 primeros álbumes a LastFM
        moreAlbums(null);

        //La magia de la imágen cuadrada
        // http://stackoverflow.com/questions/3779173/determining-the-size-of-an-android-view-at-runtime
        findViewById(R.id.activity_artist).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ActivityUtils.onLinearLayoutChanged(image, findViewById(R.id.activity_artist), ArtistActivity.this);
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
    }

    @OnClick(R.id.artist_more_albums)
    public void moreAlbums(View view) {
        moreAlbums.setEnabled(false);
        LastFMApi.Artist.getTopAlbums(name.getText().toString(), 5, page++).setCallback(new FutureResponse.ResponseCallback<List<ShortAlbumInfo>>() {
            @Override
            public void done(final List<ShortAlbumInfo> o, final Exception e) {
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        showAlbums(o, e);
                        moreAlbums.setEnabled(true);
                    }
                });
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

    private void showInfo(ArtistInfo o, Exception e) {
        if(e != null) {
            Toast.makeText(this, "Error!! " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } else {
            //Mostramos la información del artista en la pantalla
            if(o.getImageForSize("extralarge") != null && !o.getImageForSize("extralarge").isEmpty())
                Picasso.with(this).load(o.getImageForSize("extralarge")).fit().into(image);
            bio.setAutoLinkText(o.getBio());
            listeners.setText(String.format(Locale.getDefault(), "%d", o.getListeners()));
            name.setText(o.getName());
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

            //Si no hay una biografía decente, se busca en inglés
            if(o.getBio().length() < 10) {
                LastFMApi.Artist.getInfo(o.getName(), null).setCallback(new FutureResponse.ResponseCallback<ArtistInfo>() {
                    @Override
                    public void done(final ArtistInfo o, Exception e) {
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

    private void showAlbums(List<ShortAlbumInfo> o, Exception e) {
        if(e != null) {
            Toast.makeText(this, "Error2!! " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } else {
            //Mostramos los álbumes en su sitio correspondiente
            for(final ShortAlbumInfo album : o) {
                View albumView = LayoutInflater.from(this)
                        .inflate(R.layout.artist_album, albumsList, false);
                if(album.getImageForSize("medium") != null && !album.getImageForSize("medium").isEmpty())
                    Picasso.with(this).load(album.getImageForSize("medium")).fit().into((ImageView) albumView.findViewById(R.id.artist_album_image));
                ((TextView) albumView.findViewById(R.id.artist_album_name)).setText(album.getName());
                albumView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent();
                        i.setClass(ArtistActivity.this, AlbumActivity.class);
                        i.putExtra("album", album.getName());
                        i.putExtra("artist", name.getText());
                        startActivity(i);
                    }
                });
                albumsList.addView(albumView, albumsList.getChildCount() - 1);
            }
            if(o.size() < 5) {
                moreAlbums.setVisibility(GONE);
            }
        }
    }
}
