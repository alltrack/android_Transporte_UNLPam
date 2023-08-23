package ar.com.unlpam.colectivos;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {
    private static VolleySingleton mVolleyS = null;

    //Este objeto es la cola que usará la aplicación
    private RequestQueue mRequestQueue;

    private VolleySingleton(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static VolleySingleton getInstance(Context context) {
        if (mVolleyS == null) {
            mVolleyS = new VolleySingleton(context);
        }
        return mVolleyS;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }


}