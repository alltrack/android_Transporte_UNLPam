package ar.com.unlpam.colectivos;

import android.app.Application;

public class GlobalClass extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Inicializa Volley al inicio de la app
        VolleySingleton.getInstance(this);
    }
}