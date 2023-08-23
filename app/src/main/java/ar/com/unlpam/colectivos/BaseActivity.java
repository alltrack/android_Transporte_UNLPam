package ar.com.unlpam.colectivos;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;

public class BaseActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener {

    protected VolleySingleton volley;
    protected RequestQueue fRequestQueue;
    protected ProgressDialog waitingDialog = null;
    protected boolean restart = false;
    protected AlertDialog.Builder errorDialog = null;
    protected String TAG;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        TAG = getResources().getString(R.string.TAG);

        //REST WEBSERVICE CLIENT UP
        volley = VolleySingleton.getInstance(getApplicationContext());
        fRequestQueue = volley.getRequestQueue();

    }



    // API REST FUNCTION
    public void addToQueue(Request request) {
        if (request != null) {
            request.setTag(this);
            if (fRequestQueue == null)
                fRequestQueue = volley.getRequestQueue();
            request.setRetryPolicy(new DefaultRetryPolicy(
                    Integer.parseInt(getString(R.string.socketTimeOut)), 1, DefaultRetryPolicy.DEFAULT_TIMEOUT_MS
            ));
            //onPreStartConnection();
            fRequestQueue.add(request);
        }
    }

    public void onPreStartConnection() {
        Log.e(TAG + " - BaseActivity", "ABRE EL WAITING DIALOG");
        if (this.waitingDialog != null) {
            this.waitingDialog.dismiss();
        }
        if(!((Activity) this).isFinishing())
            this.waitingDialog = ProgressDialog.show(this,"",
                    getString(R.string.waitingLabel), true, false);
    }

    public void onNullResponse() {
        if (this.waitingDialog != null) {
            this.waitingDialog.dismiss();
        }
        showMensajeDialog(getString(R.string.noneResponseLabel),"");
    }

    public void onConnectionFinished() {
        Log.e(TAG + " - BaseActivity", "CIERRA EL WAITING DIALOG");
        if (this.waitingDialog != null) {
            this.waitingDialog.dismiss();
            this.waitingDialog = null;
        }
    }

    public void showErrorDialog(String message){
        String showMessage = message;

        if(!message.equalsIgnoreCase(""))
            if(message.indexOf(":") != -1)
                message = message.substring(0, message.indexOf(":"));

        switch (message){
            case "com.android.volley.TimeoutError":showMessage = getString(R.string.messageErrorConnection); break;
            case "com.android.volley.ServerError":showMessage = getString(R.string.messageErrorServer); break;
            case "com.android.volley.AuthFailureError":showMessage = getString(R.string.messageErrorAuthenticationPost); break;
            case "com.android.volley.NoConnectionError":showMessage = getString(R.string.messageErrorConnection); break;
            default:showMessage = getString(R.string.messageErrorDefault); restart = true; break;
        };

        preCancelActivity();

        errorDialog = new AlertDialog.Builder(this);
        errorDialog
                .setMessage(showMessage)
                .setTitle(getString(R.string.errorDialogTitle))
                .setIcon(R.drawable.emoji_sorry)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        cancelActivity();

                        if(restart) {
                            Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                        }
                    }
                });
        AlertDialog alertError = errorDialog.create();
        alertError.show();
    }

    public void showMensajeDialog(String message, String title){

        errorDialog = new AlertDialog.Builder(this);
        errorDialog
                .setMessage(message)
                .setTitle(title)
                .setIcon(R.drawable.emoji_sorry)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertError = errorDialog.create();
        alertError.show();
    }

    public void showMensajeDialog(int emoji, String message, String title){

        errorDialog = new AlertDialog.Builder(this);
        errorDialog
                .setMessage(message)
                .setTitle(title)
                .setIcon(emoji)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertError = errorDialog.create();
        alertError.show();
    }

    public void onConnectionFailed(String error) {
        if (this.waitingDialog != null) {
            this.waitingDialog.dismiss();
            this.waitingDialog = null;
        }
        showErrorDialog(error);
    }

    public void preCancelActivity(){

    }
    public void cancelActivity(){

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_mapa) {
            Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mainActivity);
            // Handle the camera action
        }

        if (id == R.id.nav_paradas) {
            Intent paradasActivity = new Intent(getApplicationContext(), ParadasActivity.class);
            startActivity(paradasActivity);
            // Handle the camera action
        }

        if (id == R.id.nav_conductor_imprudente) {
            Intent reclamoActivity = new Intent(getApplicationContext(), ReclamoActivity.class);
            startActivity(reclamoActivity);
            // Handle the camera action
        }
        if (id == R.id.nav_politicas) {
            Intent politicaActivity = new Intent(getApplicationContext(), PoliticasActivity.class);
            startActivity(politicaActivity);
            // Handle the camera action
        }

        if (id == R.id.nav_selectCity) {
            Intent CityActivity = new Intent(getApplicationContext(), SelectCityActivity.class);
            startActivity(CityActivity);
            // Handle the camera action
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
