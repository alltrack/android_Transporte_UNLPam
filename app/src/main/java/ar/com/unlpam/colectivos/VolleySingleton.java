package ar.com.unlpam.colectivos;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {
    private static VolleySingleton instance;
    private RequestQueue requestQueue;
    private static Context appContext;

    private VolleySingleton(Context context) {
        appContext = context.getApplicationContext();
        requestQueue = Volley.newRequestQueue(appContext);
    }

    public static synchronized void getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context);
        }
    }

    public static VolleySingleton getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "VolleySingleton must be initialized in Application.onCreate() first!");
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(appContext);
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }

}