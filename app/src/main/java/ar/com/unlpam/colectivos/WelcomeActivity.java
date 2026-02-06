package ar.com.unlpam.colectivos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class WelcomeActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 3000; // 3 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Animación fade-in
        ImageView logo = findViewById(R.id.imageView);
        LinearLayout footer = findViewById(R.id.layout_footer);

        logo.setAlpha(0f);
        footer.setAlpha(0f);

        logo.animate().alpha(1f).setDuration(1000).start();
        footer.animate().alpha(1f).setDuration(1000).setStartDelay(300).start();


        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String city = prefs.getString("city", "");

            Intent intent;

            if (city.isEmpty()) {
                intent = new Intent(WelcomeActivity.this, SelectCityActivity.class);
            } else {
                intent = new Intent(WelcomeActivity.this, MainActivity.class);
            }

            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}