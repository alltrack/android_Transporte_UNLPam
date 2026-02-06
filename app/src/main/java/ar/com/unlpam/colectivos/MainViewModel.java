package ar.com.unlpam.colectivos;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;

import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainViewModel extends BaseViewModel {

    // Cachés de datos
    private final ArrayList<Parada> cacheParadas = new ArrayList<>();
    private final ArrayList<Bus> cacheBus = new ArrayList<>();

    // LiveData para observar desde la Activity
    private final MutableLiveData<ArrayList<Parada>> _paradas = new MutableLiveData<>();
    public LiveData<ArrayList<Parada>> paradas = _paradas;

    private final MutableLiveData<ArrayList<Bus>> _buses = new MutableLiveData<>();
    public LiveData<ArrayList<Bus>> buses = _buses;

    private final MutableLiveData<Boolean> _isMapReady = new MutableLiveData<>(false);

    // Handler para polling
    private final Handler pollingHandler = new Handler(Looper.getMainLooper());
    private Runnable pollingRunnable;
    private boolean isPollingActive = false;
    private boolean isBusTaskRunning = false;

    private static final long POLLING_INTERVAL_MS = 10000; // 10 segundos

    public MainViewModel(@NonNull Application application) {
        super(application);
        setupPolling();
    }

    public void fetchParadas() {
        showLoading();

        String url = getApplication().getString(R.string.RestApiHttps) +
                getApplication().getString(R.string.getParadas);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    hideLoading();
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        parseParadas(jsonArray);
                    } catch (JSONException e) {
                        onConnectionFailed("Error parsing paradas: " + e.getMessage());
                    }
                },
                error -> onConnectionFailed(error.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("sede", getCityFromPreferences());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("sede", getCityFromPreferences());
                return headers;
            }
        };

        addToQueue(request);
    }

    public void fetchBuses() {
        if (isBusTaskRunning) {
            return; // Ya hay una petición en curso
        }

        isBusTaskRunning = true;

        String url = getApplication().getString(R.string.RestApiHttps) +
                getApplication().getString(R.string.getBus);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    isBusTaskRunning = false;
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        parseBuses(jsonArray);
                    } catch (JSONException e) {
                        isBusTaskRunning = false;
                        onConnectionFailed("Error parsing buses: " + e.getMessage());
                    }
                },
                error -> {
                    isBusTaskRunning = false;
                    onConnectionFailed(error.toString());
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("sede", getCityFromPreferences());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("sede", getCityFromPreferences());
                return headers;
            }
        };

        addToQueue(request);
    }

    // Parse paradas del JSON
    private void parseParadas(JSONArray jsonArray) {
        cacheParadas.clear();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject each = jsonArray.getJSONObject(i);

                Parada parada = new Parada(
                        each.getInt("id"),
                        each.getDouble("la"),
                        each.getDouble("lo"),
                        each.getString("de"),
                        each.getString("di"),
                        each.getJSONArray("hs")
                );

                cacheParadas.add(parada);
            }

            _paradas.setValue(new ArrayList<>(cacheParadas));

        } catch (JSONException e) {
            onConnectionFailed("Error parsing paradas: " + e.getMessage());
        }
    }

    // Parse buses del JSON
    private void parseBuses(JSONArray jsonArray) {
        cacheBus.clear();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject each = jsonArray.getJSONObject(i);

                String fecha = each.getString("fe") + " " + each.getString("ho");

                // La foto se procesará en la Activity (necesita Context para recursos)
                Bus bus = new Bus(
                        each.getInt("id"),
                        each.getDouble("la"),
                        each.getDouble("lo"),
                        fecha,
                        each.getString("al"),
                        each.getString("pa"),
                        each.getInt("se"),
                        null // La foto la procesamos en la UI
                );

                // Guardar la imagen base64 para que la Activity la procese
                bus.setImagenBase64(each.getString("im"));

                cacheBus.add(bus);
            }

            _buses.setValue(new ArrayList<>(cacheBus));

        } catch (JSONException e) {
            onConnectionFailed("Error parsing buses: " + e.getMessage());
        }
    }

    //  Setup del polling con Handler
    private void setupPolling() {
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPollingActive && _isMapReady.getValue() != null && _isMapReady.getValue()) {
                    fetchBuses();
                    pollingHandler.postDelayed(this, POLLING_INTERVAL_MS);
                }
            }
        };
    }

    // Iniciar polling
    public void startPolling() {
        if (!isPollingActive) {
            isPollingActive = true;
            pollingHandler.post(pollingRunnable);
        }
    }

    //  Detener polling
    public void stopPolling() {
        isPollingActive = false;
        pollingHandler.removeCallbacks(pollingRunnable);
    }

    // Notificar que el mapa está listo
    public void setMapReady(boolean ready) {
        _isMapReady.setValue(ready);
        if (ready && isPollingActive) {
            pollingHandler.post(pollingRunnable);
        }
    }

    // Buscar parada por ID
    public Parada findParadaById(int id) {
        for (Parada p : cacheParadas) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    // Buscar bus por ID
    public Bus findBusById(int id) {
        for (Bus b : cacheBus) {
            if (b.getId() == id) {
                return b;
            }
        }
        return null;
    }

    // Obtener ciudad de SharedPreferences
    private String getCityFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        return prefs.getString("city", "");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopPolling();
    }
}