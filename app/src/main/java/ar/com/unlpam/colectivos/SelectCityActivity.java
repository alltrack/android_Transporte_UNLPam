package ar.com.unlpam.colectivos;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.view.View;
import android.widget.ImageButton;

public class SelectCityActivity extends BaseActivity {

    ImageButton btn_select_pico;
    ImageButton btn_select_staRosa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);

        btn_select_pico = (ImageButton) findViewById(R.id.btnPico);
        btn_select_staRosa = (ImageButton) findViewById(R.id.btnSantaRosa);

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        btn_select_pico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor edit = settings.edit();
                edit.putString("city", "GP");
                edit.commit();

                Intent mainIntent = new Intent(SelectCityActivity.this, MainActivity.class);
                startActivity(mainIntent);
            }
        });

        btn_select_staRosa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor edit = settings.edit();
                edit.putString("city", "SR");
                edit.commit();

                Intent mainIntent = new Intent(SelectCityActivity.this, MainActivity.class);
                startActivity(mainIntent);
            }
        });
    }
}


