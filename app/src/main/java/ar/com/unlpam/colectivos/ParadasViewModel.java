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

import java.util.HashMap;
import java.util.Map;

public class ParadasViewModel extends BaseViewModel {

    private final MutableLiveData<JSONArray> _paradas = new MutableLiveData<>();
    public LiveData<JSONArray> paradas = _paradas;

    public ParadasViewModel(@NonNull Application application) {
        super(application);
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
                        _paradas.setValue(jsonArray);
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

    private String getCityFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
        return prefs.getString("city", "");
    }
}