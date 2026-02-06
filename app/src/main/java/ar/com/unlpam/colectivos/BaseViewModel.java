package ar.com.unlpam.colectivos;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;

public class BaseViewModel extends AndroidViewModel {

    protected VolleySingleton volley;
    protected RequestQueue requestQueue;
    protected String TAG;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<ErrorState> _error = new MutableLiveData<>();
    public LiveData<ErrorState> error = _error;

    private final MutableLiveData<MessageState> _message = new MutableLiveData<>();
    public LiveData<MessageState> message = _message;

    public BaseViewModel(@NonNull Application application) {
        super(application);

        TAG = application.getString(R.string.TAG);

        // ⚠️ Ya no pasa context, porque ya se inicializó en MyApplication
        volley = VolleySingleton.getInstance();
        requestQueue = volley.getRequestQueue();
    }

    public <T> void addToQueue(Request<T> request) {
        if (request != null) {
            request.setTag(this);

            if (requestQueue == null) {
                requestQueue = volley.getRequestQueue();
            }

            int timeoutMs = Integer.parseInt(
                    getApplication().getString(R.string.socketTimeOut)
            );

            request.setRetryPolicy(new DefaultRetryPolicy(
                    timeoutMs,
                    1,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            requestQueue.add(request);
        }
    }

    protected void showLoading() {
        _isLoading.postValue(true);
    }

    protected void hideLoading() {
        _isLoading.postValue(false);
    }

    protected void onConnectionFailed(String errorMessage) {
        hideLoading();

        String message = parseErrorMessage(errorMessage);
        boolean shouldRestart = !isKnownError(errorMessage);

        _error.postValue(new ErrorState(message, shouldRestart));
    }

    private String parseErrorMessage(String error) {
        String errorType = error;

        if (error.contains(":")) {
            errorType = error.substring(0, error.indexOf(":"));
        }

        switch (errorType) {
            case "com.android.volley.TimeoutError":
            case "com.android.volley.NoConnectionError":
                return getApplication().getString(R.string.messageErrorConnection);
            case "com.android.volley.ServerError":
                return getApplication().getString(R.string.messageErrorServer);
            case "com.android.volley.AuthFailureError":
                return getApplication().getString(R.string.messageErrorAuthenticationPost);
            default:
                return getApplication().getString(R.string.messageErrorDefault);
        }
    }

    private boolean isKnownError(String error) {
        return error.contains("TimeoutError") ||
                error.contains("ServerError") ||
                error.contains("AuthFailureError") ||
                error.contains("NoConnectionError");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }

    public static class ErrorState {
        public final String message;
        public final boolean shouldRestart;

        public ErrorState(String message, boolean shouldRestart) {
            this.message = message;
            this.shouldRestart = shouldRestart;
        }
    }

    public static class MessageState {
        public final String message;
        public final String title;

        public MessageState(String message, String title) {
            this.message = message;
            this.title = title;
        }
    }
}