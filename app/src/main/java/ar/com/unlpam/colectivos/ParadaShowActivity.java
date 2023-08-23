package ar.com.unlpam.colectivos;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParadaShowActivity extends BaseActivity {

    private ViewPager viewPager;
    private Parada each_parada;

    public Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_paradas);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ctx = this.getApplicationContext();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Bundle datos = this.getIntent().getExtras();
        String JSONDefinition = datos.getString("parada");

        try {
            JSONObject each = new JSONObject(JSONDefinition);
            each_parada = new Parada(each.getInt("id"), each.getDouble("la"), each.getDouble("lo"), each.getString("de"), each.getString("di"), each.getJSONArray("hs"),each.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView title_Lu = (TextView) findViewById(R.id.txt_title_lunes);
        TextView title_Ma = (TextView) findViewById(R.id.txt_title_martes);
        TextView title_Mi = (TextView) findViewById(R.id.txt_title_miercoles);
        TextView title_Ju = (TextView) findViewById(R.id.txt_title_jueves);
        TextView title_Vi = (TextView) findViewById(R.id.txt_title_viernes);
        TextView title_Sa = (TextView) findViewById(R.id.txt_title_sabado);
        TextView title_Do = (TextView) findViewById(R.id.txt_title_domingo);

        TextView text_Lu = (TextView) findViewById(R.id.txt_text_hs_lunes);
        TextView text_Ma = (TextView) findViewById(R.id.txt_text_hs_martes);
        TextView text_Mi = (TextView) findViewById(R.id.txt_text_hs_miercoles);
        TextView text_Ju = (TextView) findViewById(R.id.txt_text_hs_jueves);
        TextView text_Vi = (TextView) findViewById(R.id.txt_text_hs_viernes);
        TextView text_Sa = (TextView) findViewById(R.id.txt_text_hs_sabado);
        TextView text_Do = (TextView) findViewById(R.id.txt_text_hs_domingo);

        title_Lu.setVisibility(View.INVISIBLE);
        title_Ma.setVisibility(View.INVISIBLE);
        title_Mi.setVisibility(View.INVISIBLE);
        title_Ju.setVisibility(View.INVISIBLE);
        title_Vi.setVisibility(View.INVISIBLE);
        title_Sa.setVisibility(View.INVISIBLE);
        title_Do.setVisibility(View.INVISIBLE);

        text_Lu.setVisibility(View.INVISIBLE);
        text_Ma.setVisibility(View.INVISIBLE);
        text_Mi.setVisibility(View.INVISIBLE);
        text_Ju.setVisibility(View.INVISIBLE);
        text_Vi.setVisibility(View.INVISIBLE);
        text_Sa.setVisibility(View.INVISIBLE);
        text_Do.setVisibility(View.INVISIBLE);

        DateFormat dateFormat = new SimpleDateFormat("kk:mm");

        for(Horario h:each_parada.getHorarios()){
            if(h.is_lunes){
                title_Lu.setVisibility(View.VISIBLE);
                text_Lu.setVisibility(View.VISIBLE);
                if (text_Lu.getText().equals(""))
                    text_Lu.setText(text_Lu.getText() + dateFormat.format(h.hora) + " hs.");
                else
                    text_Lu.setText(text_Lu.getText() + "  |  " + dateFormat.format(h.hora) + " hs.");
            }

            if(h.is_martes){
                title_Ma.setVisibility(View.VISIBLE);
                text_Ma.setVisibility(View.VISIBLE);
                if (text_Ma.getText().equals(""))
                    text_Ma.setText(text_Ma.getText() + dateFormat.format(h.hora) + " hs.");
                else
                    text_Ma.setText(text_Ma.getText() + "  |  " + dateFormat.format(h.hora) + " hs.");
            }

            if(h.is_miercoles){
                title_Mi.setVisibility(View.VISIBLE);
                text_Mi.setVisibility(View.VISIBLE);
                if (text_Mi.getText().equals(""))
                    text_Mi.setText(text_Mi.getText() + dateFormat.format(h.hora) + " hs.");
                else
                    text_Mi.setText(text_Mi.getText() + "  |  " + dateFormat.format(h.hora) + " hs.");
            }

            if(h.is_jueves){
                title_Ju.setVisibility(View.VISIBLE);
                text_Ju.setVisibility(View.VISIBLE);
                if (text_Ju.getText().equals(""))
                    text_Ju.setText(text_Ju.getText() + dateFormat.format(h.hora) + " hs.");
                else
                    text_Ju.setText(text_Ju.getText() + "  |  " + dateFormat.format(h.hora) + " hs.");
            }

            if(h.is_viernes){
                title_Vi.setVisibility(View.VISIBLE);
                text_Vi.setVisibility(View.VISIBLE);
                if (text_Vi.getText().equals(""))
                    text_Vi.setText(text_Vi.getText() + dateFormat.format(h.hora) + " hs.");
                else
                    text_Vi.setText(text_Vi.getText() + "  |  " + dateFormat.format(h.hora) + " hs.");
            }

            if(h.is_sabado){
                title_Sa.setVisibility(View.VISIBLE);
                text_Sa.setVisibility(View.VISIBLE);
                if (text_Sa.getText().equals(""))
                    text_Sa.setText(text_Sa.getText() + dateFormat.format(h.hora) + " hs.");
                else
                    text_Sa.setText(text_Sa.getText() + "  |  " + dateFormat.format(h.hora) + " hs.");
            }

            if(h.is_domingo){
                title_Do.setVisibility(View.VISIBLE);
                text_Do.setVisibility(View.VISIBLE);
                if (text_Do.getText().equals(""))
                    text_Do.setText(text_Do.getText() + dateFormat.format(h.hora) + " hs.");
                else
                    text_Do.setText(text_Do.getText() + "  |  " + dateFormat.format(h.hora) + " hs.");
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }



    //Dibuja las paradas en el mapa.
    private void showHorario(JSONObject parada) {

    }




}
