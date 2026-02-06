package ar.com.unlpam.colectivos;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.google.android.material.navigation.NavigationView;

public abstract class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected static final String TAG = "BaseActivity";
    protected ProgressBar loadingProgressBar;
    protected AlertDialog.Builder errorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    protected void showLoading() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
        }
    }

    protected void hideLoading() {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.GONE);
        }
    }

    protected void addToQueue(Request<?> request) {
        if (request != null) {
            VolleySingleton.getInstance().addToRequestQueue(request);
        }
    }

  void onPreStartConnection() {
        showLoading();
    }

    public void onConnectionFinished() {
        hideLoading();
    }

    public void onConnectionFailed(String errorMessage) {
        hideLoading();
        showErrorDialog(parseErrorMessage(errorMessage));
    }

    protected String parseErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            return "Error de conexión";
        }

        if (errorMessage.contains("UnknownHostException") ||
                errorMessage.contains("ConnectException")) {
            return "No se pudo conectar al servidor. Verifica tu conexión a internet.";
        }

        if (errorMessage.contains("SocketTimeoutException") ||
                errorMessage.contains("TimeoutError")) {
            return "La conexión tardó demasiado. Intenta nuevamente.";
        }

        if (errorMessage.contains("NoConnectionError")) {
            return "Sin conexión a internet. Verifica tu red.";
        }

        return "Error: " + errorMessage;
    }

    protected void showErrorDialog(String message) {
        if (isFinishing()) return;

        errorDialog = new AlertDialog.Builder(this);
        errorDialog.setMessage(message)
                .setTitle("Error")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, id) -> dialog.cancel());

        AlertDialog alert = errorDialog.create();
        alert.show();
    }

    public void showMensajeDialog(int emoji, String message, String title) {
        if (isFinishing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle(title)
                .setIcon(emoji)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, id) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_mapa) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.nav_paradas) {
            startActivity(new Intent(this, ParadasActivity.class));
        } else if (id == R.id.nav_conductor_imprudente) {
            startActivity(new Intent(this, ReclamoActivity.class));
        } else if (id == R.id.nav_politicas) {
            startActivity(new Intent(this, PoliticasActivity.class));
        } else if (id == R.id.nav_selectCity) {
            startActivity(new Intent(this, SelectCityActivity.class));
        }
        return true;
    }
}