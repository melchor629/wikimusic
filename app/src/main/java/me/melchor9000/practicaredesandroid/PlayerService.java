package me.melchor9000.practicaredesandroid;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;

/**
 * A ver, esto es solo para probar. En la pantalla de canción, si se hace click en la duración puede
 * pasar dos cosas:<br>
 * &nbsp;&nbsp;- Salga un error y se cierre el programa<br>
 * &nbsp;&nbsp;- Se escuche (parte de) la canción durante 30 segundos<br>
 * Todo esto gracias a la API de Spotify
 */
public class PlayerService extends Service {
    private MediaPlayer player;

    @Override
    public void onCreate() {
        player = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String url = intent.getStringExtra("url");
            if (player.isPlaying()) {
                player.stop();
                player.release();
                player = new MediaPlayer();
                player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                        String error = "";
                        switch (i) {
                            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                                error = "UNKNOWN - ";
                                break;
                            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                                error = "SERVER_DIED - ";
                                break;
                        }
                        switch (i1) {
                            case MediaPlayer.MEDIA_ERROR_IO:
                                error += "IO";
                                break;
                            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                                error += "MALFORMED";
                                break;
                            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                                error += "UNSUPPORTED";
                                break;
                            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                                error += "TIMED_OUT";
                                break;
                            default:
                                error += "SYSTEM";
                        }
                        Log.e("PlayerService", error);
                        return true;
                    }
                });
            } else {
                player.reset();
            }

            try {
                player.setDataSource(url);
                player.prepareAsync();
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            onDestroy();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if(player.isPlaying()) player.stop();
        player.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
