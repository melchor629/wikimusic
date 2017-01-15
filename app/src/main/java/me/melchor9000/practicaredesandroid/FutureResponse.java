package me.melchor9000.practicaredesandroid;

//No se, esto me ayuda a hacer el código mas bonito, de cierta forma
//Y asíncrono, eso es importante
public final class FutureResponse<T> {
    private ResponseCallback<T> cbk;

    public interface ResponseCallback<T> {
        void done(T o, Exception e);
    }

    public void onFailure(Exception e) {
        if(cbk != null) cbk.done(null, e);
    }

    public void onResponse(T r) {
        if(cbk != null) cbk.done(r, null);
    }

    public void setCallback(ResponseCallback<T> cbk) {
        this.cbk = cbk;
    }
}
