package ar.com.unlpam.colectivos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class WelcomeActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 2000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences(WelcomeActivity.this);
                String city = prefs.getString("city", "");

                if(city.equalsIgnoreCase("")) {
                    Intent mainIntent = new Intent(WelcomeActivity.this, SelectCityActivity.class);

                    /*SharedPreferences.Editor edit = settings.edit();
                    edit.putString("city", "SR");
                    edit.commit();
                    Intent mainIntent = new Intent(WelcomeActivity.this, MainActivity.class);*/

                    startActivity(mainIntent);
                }
                else{
                    Intent mainIntent = new Intent(WelcomeActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                }

                overridePendingTransition(R.animator.fadein,R.animator.zoom_in);
            }
        },SPLASH_TIME_OUT);
    }
}
