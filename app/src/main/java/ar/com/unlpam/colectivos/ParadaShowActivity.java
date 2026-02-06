package ar.com.unlpam.colectivos;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.appcompat.widget.Toolbar;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ParadaShowActivity extends BaseActivity {

    private Parada parada;

    // TextViews de horarios
    private TextView textLunes, textMartes, textMiercoles, textJueves;
    private TextView textViernes, textSabado, textDomingo;

    // Cards
    private View cardLunes, cardMartes, cardMiercoles, cardJueves;
    private View cardViernes, cardSabado, cardDomingo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_paradas);

        setupUI();
        loadParadaData();
        displayHorarios();
    }

    private void setupUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        // Inicializar TextViews de horarios
        textLunes = findViewById(R.id.txt_text_hs_lunes);
        textMartes = findViewById(R.id.txt_text_hs_martes);
        textMiercoles = findViewById(R.id.txt_text_hs_miercoles);
        textJueves = findViewById(R.id.txt_text_hs_jueves);
        textViernes = findViewById(R.id.txt_text_hs_viernes);
        textSabado = findViewById(R.id.txt_text_hs_sabado);
        textDomingo = findViewById(R.id.txt_text_hs_domingo);

        // Inicializar Cards
        cardLunes = findViewById(R.id.card_lunes);
        cardMartes = findViewById(R.id.card_martes);
        cardMiercoles = findViewById(R.id.card_miercoles);
        cardJueves = findViewById(R.id.card_jueves);
        cardViernes = findViewById(R.id.card_viernes);
        cardSabado = findViewById(R.id.card_sabado);
        cardDomingo = findViewById(R.id.card_domingo);
    }

    private void displayHorarios() {
        if (parada == null) return;

        DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        // Arrays paralelos de cards y textos
        View[] cards = {
                cardDomingo, cardLunes, cardMartes, cardMiercoles,
                cardJueves, cardViernes, cardSabado
        };

        TextView[] texts = {
                textDomingo, textLunes, textMartes, textMiercoles,
                textJueves, textViernes, textSabado
        };

        // Limpiar textos
        for (TextView text : texts) {
            text.setText("");
        }

        boolean hayHorarios = false;

        // Procesar cada horario
        for (Horario h : parada.getHorarios()) {
            String horaFormateada = dateFormat.format(h.hora) + " hs.";

            boolean[] diasActivos = {
                    h.is_domingo,
                    h.is_lunes,
                    h.is_martes,
                    h.is_miercoles,
                    h.is_jueves,
                    h.is_viernes,
                    h.is_sabado
            };

            for (int i = 0; i < diasActivos.length; i++) {
                if (diasActivos[i]) {
                    addHorarioToDia(cards[i], texts[i], horaFormateada);
                    hayHorarios = true;
                }
            }
        }

        // Mostrar estado vacío si no hay horarios
        View emptyStateContainer = findViewById(R.id.empty_state_container);
        View horariosContainer = findViewById(R.id.horarios_container);

        if (hayHorarios) {
            emptyStateContainer.setVisibility(View.GONE);
            horariosContainer.setVisibility(View.VISIBLE);
        } else {
            emptyStateContainer.setVisibility(View.VISIBLE);
            horariosContainer.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void addHorarioToDia(View card, TextView text, String hora) {
        card.setVisibility(View.VISIBLE);

        if (text.getText().toString().isEmpty()) {
            text.setText(hora);
        } else {
            text.setText(text.getText() + "  |  " + hora);
        }
    }

    private void loadParadaData() {
        Bundle datos = getIntent().getExtras();
        if (datos == null) {
            finish();
            return;
        }

        String jsonDefinition = datos.getString("parada");
        if (jsonDefinition == null) {
            finish();
            return;
        }

        try {
            JSONObject json = new JSONObject(jsonDefinition);
            parada = new Parada(
                    json.getInt("id"),
                    json.getDouble("la"),
                    json.getDouble("lo"),
                    json.getString("de"),
                    json.getString("di"),
                    json.getJSONArray("hs"),
                    json.toString()
            );
        } catch (JSONException e) {
            Log.e("TRANSPORTE UNLPAM", "Error parseando JSON de paradas", e);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}