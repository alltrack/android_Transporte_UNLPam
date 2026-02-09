package ar.com.unlpam.colectivos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import com.google.android.material.card.MaterialCardView;

public class SelectCityActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Seleccione una sede...");
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // General Pico
        MaterialCardView cardPicoIngenieria = findViewById(R.id.cardPicoIngenieria);
        MaterialCardView cardPicoHumanas = findViewById(R.id.cardPicoHumanas);
        MaterialCardView cardPicoVeterinaria = findViewById(R.id.cardPicoVeterinaria);

        // Santa Rosa
        MaterialCardView cardSantaRosaAgronomia = findViewById(R.id.cardSantaRosaAgronomia);
        MaterialCardView cardSantaRosaSalud = findViewById(R.id.cardSantaRosaSalud);
        MaterialCardView cardSantaRosaEconomica = findViewById(R.id.cardSantaRosaEconomica);
        MaterialCardView cardSantaRosaNaturales = findViewById(R.id.cardSantaRosaNaturales);

        // Listeners General Pico
        cardPicoIngenieria.setOnClickListener(v -> {
            saveSelection(prefs, "GP", "Ingeniería");
            navigateToMain();
        });

        cardPicoHumanas.setOnClickListener(v -> {
            saveSelection(prefs, "GP", "Ciencias Humanas");
            navigateToMain();
        });

        cardPicoVeterinaria.setOnClickListener(v -> {
            saveSelection(prefs, "GP", "Ciencias Veterinarias");
            navigateToMain();
        });

        // Listeners Santa Rosa
        cardSantaRosaAgronomia.setOnClickListener(v -> {
            saveSelection(prefs, "SR", "Agronomía");
            navigateToMain();
        });

        cardSantaRosaSalud.setOnClickListener(v -> {
            saveSelection(prefs, "SR", "Ciencias de la Salud");
            navigateToMain();
        });

        cardSantaRosaEconomica.setOnClickListener(v -> {
            saveSelection(prefs, "SR", "Ciencias Económicas y Jurídicas");
            navigateToMain();
        });

        cardSantaRosaNaturales.setOnClickListener(v -> {
            saveSelection(prefs, "SR", "Ciencias Naturales y Exactas");
            navigateToMain();
        });
    }

    private void saveSelection(SharedPreferences prefs, String city, String faculty) {
        prefs.edit()
                .putString("city", city)
                .putString("faculty", faculty)
                .apply();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}