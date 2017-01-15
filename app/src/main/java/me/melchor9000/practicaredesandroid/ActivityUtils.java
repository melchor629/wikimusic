package me.melchor9000.practicaredesandroid;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

final class ActivityUtils {

    /**
     * Ajusta los márgenes para que el contenido se vea correctamente al girar la pantalla
     * @param layout contenedor
     * @param activity actividad a la que pertenece el contenedor
     */
    static void onConfigurationChanged(LinearLayout layout, Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        if(metrics.heightPixels < metrics.widthPixels) { // LANDSCAPE
            if(rotation == Surface.ROTATION_90 || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N)
                layout.setPaddingRelative(0, 0, softKeyBarHeight(activity), 0);
            else
                layout.setPaddingRelative(softKeyBarHeight(activity), 0, 0, 0);
        } else if(metrics.widthPixels < metrics.heightPixels) { // PORTRAIT
            layout.setPaddingRelative(0, 0, 0, softKeyBarHeight(activity));
        }
    }

    /**
     * Cambia el tamaño de la imágen dependiendo de la rotación de la pantalla, para que se mantenga
     * cuadrada todo el tiempo
     * @param image vista de imágen
     * @param layout contenedor
     * @param activity actividad a la que pertenecen la vista de imágen y el contenedor
     */
    static void onLinearLayoutChanged(ImageView image, View layout, Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if(metrics.heightPixels < metrics.widthPixels) {
            image.getLayoutParams().height = layout.getWidth() - softKeyBarHeight(activity);
        } else {
            image.getLayoutParams().height = layout.getWidth();
        }
        image.requestLayout();
    }

    /**
     * Permite obtener el tamaño de la barra de botones en pantalla, tanto si está en vertical,
     * como si está en horizontal
     * @param activity actividad
     * @return tamaño en px (no dp) de la barra de botones
     */
    private static int softKeyBarHeight(Activity activity) {
        // http://stackoverflow.com/questions/29398929/how-get-height-of-the-status-bar-and-soft-key-buttons-bar
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableWidth = metrics.widthPixels;
        int usableHeight = metrics.heightPixels;
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realWidth = metrics.widthPixels;
        int realHeight = metrics.heightPixels;
        if(realWidth == usableWidth) return realHeight - usableHeight;
        else if(realHeight == usableHeight) return realWidth - usableWidth;
        else return -1;
    }
}
