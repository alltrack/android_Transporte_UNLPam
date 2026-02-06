package ar.com.unlpam.colectivos;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;


import com.android.volley.Request;

import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReclamoViewModel extends BaseViewModel {

    private final MutableLiveData<ArrayList<String>> _colectivos = new MutableLiveData<>();
    public LiveData<ArrayList<String>> colectivos = _colectivos;

    public ReclamoViewModel(@NonNull Application application) {
        super(application);
    }

   public void fetchColectivos() {
        showLoading();

        String url = getApplication().getString(R.string.RestApiHttps) +
                getApplication().getString(R.string.getBusLite);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    hideLoading();
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        parseColectivos(jsonArray);
                    } catch (JSONException e) {
                        onConnectionFailed("Error parsing colectivos: " + e.getMessage());
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

    private void parseColectivos(JSONArray jsonArray) {
        ArrayList<String> colectivosList = new ArrayList<>();

        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                String alias = jsonArray.getJSONObject(i).getString("al");
                colectivosList.add(alias);
            }
            _colectivos.setValue(colectivosList);
        } catch (JSONException e) {
            onConnectionFailed("Error parsing colectivos: " + e.getMessage());
        }
    }

    private String getCityFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        return prefs.getString("city", "");
    }
}