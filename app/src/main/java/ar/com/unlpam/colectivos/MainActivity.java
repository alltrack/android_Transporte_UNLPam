package ar.com.unlpam.colectivos;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.util.Base64;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity
        implements OnMapReadyCallback {

    private ArrayList<Parada> CACHE_PARADAS = new ArrayList<Parada>();
    private ArrayList<Bus> CACHE_BUS = new ArrayList<Bus>();
    private List<Pair<Integer, Marker>> listMarkerBus = new ArrayList<Pair<Integer, Marker>>();
    private List<Pair<Integer, Marker>> listMarkerBusText = new ArrayList<Pair<Integer, Marker>>();
    private List<Pair<Integer, Marker>> listMarkerText = new ArrayList<Pair<Integer, Marker>>();
    private List<Pair<Integer, Marker>> listMarkerParadas = new ArrayList<Pair<Integer, Marker>>();

    private GoogleMap map;
    private boolean isBusTaskrun = false;
    private boolean isTimerRun = false;
    private boolean isMapReady = false;

    private getParadasTask GetParadasTask;
    private RunTimePermission runTimePermission;
    private Timer timer;
    private Boolean centerPosition = false;

    protected static LinearLayout bottomSheet;
    protected static LinearLayout bottomSheetBus;

    protected BottomSheetBehavior bsb;
    protected BottomSheetBehavior bsbBus;

    protected getBusTask GetBusTask;

    private String latitud_to_show;
    private String longitud_to_show;

    public Context ctx;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ctx = this.getApplicationContext();

        bottomSheet = (LinearLayout) findViewById(R.id.bottomSheet);
        bottomSheetBus = (LinearLayout) findViewById(R.id.bottomSheetBus);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        bsb = BottomSheetBehavior.from(bottomSheet);
        bsb.setState(BottomSheetBehavior.STATE_HIDDEN);

        bsbBus = BottomSheetBehavior.from(bottomSheetBus);
        bsbBus.setState(BottomSheetBehavior.STATE_HIDDEN);

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        final Button button2 = findViewById(R.id.btn_ir);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showMensajeDialog(R.drawable.emoji_warning,getString(R.string.messageGoOut),getString(R.string.titleGoOut));

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "On Resume ");
        try{
            if(timer == null) {
                timer = new Timer();
                if(!isTimerRun && isMapReady) {
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            if (!isBusTaskrun) {
                                GetBusTask = new getBusTask();
                                GetBusTask.execute((Void) null);
                                Log.e(TAG, "ejecuta el timer");
                            }
                        }
                    };
                    timer.schedule(task, 0, 10000); //it executes this every 1000ms
                }
            }
        }
        catch (Exception e){

            Log.e(TAG,"ERROR EN TIMER RESTART");
            Log.e(TAG,e.getMessage());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "On Stop ");
        try{
            if(timer != null) {
                timer.cancel();
                timer.purge();
                timer = null;
            }
        }
        catch (Exception e){

            Log.e(TAG,"ERROR EN TIMER CANCEL");
            Log.e(TAG,e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "On Pause ");
        try{
            if(timer != null) {
                timer.cancel();
                timer.purge();
                timer = null;
            }
        }
        catch (Exception e){

            Log.e(TAG,"ERROR EN TIMER CANCEL");
            Log.e(TAG,e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        if (bsb.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bsb.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else if (bsb.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (bsbBus.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bsbBus.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else if (bsbBus.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bsbBus.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.

        map = googleMap;

        onPreStartConnection();
        GetParadasTask = new getParadasTask();
        GetParadasTask.execute((Void) null);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);

            Location location = map.getMyLocation();
            map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    if(!centerPosition)
                        if (location != null) {
                        centerPosition = true;
                        LatLng myLocation = new LatLng(location.getLatitude(),
                                location.getLongitude());
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,13));
                    }
                }
            });


        } else {
            runTimePermission = new RunTimePermission(MainActivity.this);
            runTimePermission.requestPermission(new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            }, new RunTimePermission.RunTimePermissionListener() {

                @Override
                public void permissionGranted() {
                    // First we need to check availability of play services
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    else{
                        map.setMyLocationEnabled(true);

                        map.setMyLocationEnabled(true);

                        Location location = map.getMyLocation();
                        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                            @Override
                            public void onMyLocationChange(Location location) {
                                if(!centerPosition)
                                    if (location != null) {
                                        centerPosition = true;
                                        LatLng myLocation = new LatLng(location.getLatitude(),
                                                location.getLongitude());
                                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,13));
                                    }
                            }
                        });
                    }
                }

                @Override
                public void permissionDenied() {

                   // finish();
                }
            });
        }


        timer = new Timer();
        if(!isTimerRun) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (!isBusTaskrun) {
                        GetBusTask = new getBusTask();
                        GetBusTask.execute((Void) null);
                        isBusTaskrun = true;
                        isMapReady = true;
                        Log.e(TAG, "ejecuta el timer");
                    }
                }
            };
            timer.schedule(task, 0, 10000); //it executes this every 1000ms
        }
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                MarkerTag dataTag = (MarkerTag)marker.getTag();

                switch (dataTag.getTipo()){
                    case "parada":
                        for(Pair p: listMarkerText){
                            Marker m = (Marker) p.second;
                            if(Integer.parseInt(p.first.toString()) == dataTag.getId()) {
                                m.setVisible(false);
                            }
                            else{
                                m.setVisible(true);
                            }

                        }
                        uploadBsbData(dataTag.getId());
                        bsbBus.setState(BottomSheetBehavior.STATE_HIDDEN);
                        bsb.setState(BottomSheetBehavior.STATE_EXPANDED);
                        break;


                    case "bus":
                        for(Pair p: listMarkerText){
                            Marker m = (Marker) p.second;
                            m.setVisible(true);
                        }

                        uploadBsbBusData(dataTag.getId());
                        bsb.setState(BottomSheetBehavior.STATE_HIDDEN);
                        bsbBus.setState(BottomSheetBehavior.STATE_EXPANDED);
                        break;
                    case "text":
                        return true;
                }

                return false;
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(runTimePermission!=null){
            runTimePermission.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

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

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }


    //Dibuja los colectivos en el mapa
    private void drawBus(JSONArray buses){
        GetBusTask.onCancelled();
        deleteAllMarkerBus();
        CACHE_BUS.clear();

        for(int i=0; i<buses.length(); i++){

            JSONObject each = null;
            try {
                each = buses.getJSONObject(i);

                String fecha = each.getString("fe") + " " + each.getString("ho");
                Bitmap foto = BitmapFactory.decodeResource(this.getResources(), R.drawable.imagen_no_disponible);

                if(!each.getString("im").equals("")){
                    byte[] decodedString = Base64.decode(each.getString("im"), Base64.DEFAULT);
                    foto = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                }



                CACHE_BUS.add(new Bus(each.getInt("id"),each.getDouble("la"), each.getDouble("lo"),fecha, each.getString("al"), each.getString("pa"),each.getInt("se"),foto));

                LatLng latlog = new LatLng(each.getDouble("la"), each.getDouble("lo"));


                IconGenerator icg = new IconGenerator(this);
                icg.setStyle(IconGenerator.STYLE_PURPLE);
                icg.setContentPadding(3,1,3,1);

                Bitmap bm = icg.makeIcon(each.getString("al"));

                Marker marker_text = map.addMarker(new MarkerOptions()
                        .position(latlog)
                        .icon(BitmapDescriptorFactory.fromBitmap(bm))
                        .anchor(0.5f,2.15f)
                );
                marker_text.setTag(new MarkerTag(each.getInt("id"),"text"));

                listMarkerBusText.add(new Pair(each.getInt("id"),marker_text));


                Bitmap icon = BitmapFactory.decodeResource(this.getResources(),
                        R.drawable.ic_mov_colectivo);

                Marker marker = map.addMarker(new MarkerOptions()
                        .position(latlog)
                        .title(each.getString("al"))
                        .icon(BitmapDescriptorFactory.fromBitmap(RotateBitmap(icon,each.getInt("se")))));
                listMarkerBus.add(new Pair(each.getInt("id"),marker));
                marker.setTag(new MarkerTag(each.getInt("id"),"bus"));



            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    //Dibuja las paradas en el mapa.
    private void drawParadas(JSONArray paradas) {

        CACHE_PARADAS.clear();
        listMarkerParadas.clear();

        for(int i=0; i<paradas.length(); i++){

            JSONObject each = null;
            try {
                each = paradas.getJSONObject(i);
                CACHE_PARADAS.add(new Parada(each.getInt("id"),each.getDouble("la"), each.getDouble("lo"),each.getString("de"), each.getString("di"), each.getJSONArray("hs")));
                LatLng latlog = new LatLng(each.getDouble("la"), each.getDouble("lo"));

                IconGenerator icg = new IconGenerator(this);
                icg.setContentPadding(3,3,3,3);
                icg.setStyle(IconGenerator.STYLE_ORANGE);

                Bitmap bm = icg.makeIcon(each.getString("de"));

                Marker marker_text = map.addMarker(new MarkerOptions()
                        .position(latlog)
                        .icon(BitmapDescriptorFactory.fromBitmap(bm))
                        .anchor(0.5f,2.7f)
                );
                marker_text.setTag(new MarkerTag(each.getInt("id"),"text"));

                listMarkerText.add(new Pair(each.getInt("id"),marker_text));

                Marker marker = map.addMarker(new MarkerOptions()
                                .position(latlog)
                                .title(each.getString("de"))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_parada_2))
                                );
                listMarkerParadas.add(new Pair(each.getInt("id"),marker));
                marker.setTag(new MarkerTag(each.getInt("id"),"parada"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(MainActivity.this);
        String city = prefs.getString("city", "");

        if(city.equalsIgnoreCase("GP"))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-35.65662, -63.75682), 13));
        else if(city.equalsIgnoreCase("SR"))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-36.626270, -64.291310), 13));


    }

    //Carga la informacion de la parada seleccionada en el BSB
    private void uploadBsbData(int id){
        String[] strDays = new String[]{
                "Domingo",
                "Lunes",
                "Martes",
                "Miércoles",
                "Jueves",
                "Viernes",
                "Sábado"};

        String[] strMonth = new String[]{
                "Enero",
                "Febrero",
                "Marzo",
                "Abril",
                "Mayo",
                "Junio",
                "Julio",
                "Agosto",
                "Septiembre",
                "Octubre",
                "Noviembre",
                "Diciembre"};

        Calendar now = Calendar.getInstance();

        TextView bsb_txt_denominacion = bottomSheet.findViewById(R.id.bsb_txt_denominacion);
        TextView bsb_txt_direccion = bottomSheet.findViewById(R.id.bsb_txt_direccion);
        TextView bsb_txt_tiempo_aprox = bottomSheet.findViewById(R.id.bsb_txt_tiempo_aprox);
        TextView bsb_txt_title_horarios = bottomSheet.findViewById(R.id.bsb_txt_title_horarios);
        TextView bsb_txt_horarios = bottomSheet.findViewById(R.id.bsb_txt_horarios);

        Parada each_parada = searchParada(id);

        bsb_txt_denominacion.setText(each_parada.getDenominacion());
        bsb_txt_direccion.setText(each_parada.getDireccion());

        //bsb_txt_title_horarios.setText("Horarios para hoy, " + strDays[now.get(Calendar.DAY_OF_WEEK) - 1] + " " +now.get(Calendar.DAY_OF_MONTH) + " de " + strMonth[now.get(Calendar.MONTH)]);

        String horarios_txt = "";
        String text_tiempo = "";

        DateFormat dateFormat = new SimpleDateFormat("kk:mm");


        boolean control_next_parada = false;
        for(Horario h: each_parada.getHorarios(now.get(Calendar.DAY_OF_WEEK) - 1)){

            horarios_txt += dateFormat.format(h.hora) + " hs. | ";


            if(CompareTimes(h.hora, now.getTime())) {
                if (!control_next_parada) {
                    text_tiempo = "Próximo micro en " + Integer.toString(diferencesTimes(h.hora, now.getTime())) +" min. aprox.";
                    control_next_parada = true;
                }
            }
        }

        if(horarios_txt.length() > 0){
            horarios_txt = horarios_txt.substring(0, horarios_txt.length()-2);
        }

        bsb_txt_horarios.setText(horarios_txt);

        bsb_txt_tiempo_aprox.setText(text_tiempo);

        if(horarios_txt.equalsIgnoreCase(""))
            bsb_txt_title_horarios.setVisibility(View.GONE);

        latitud_to_show = each_parada.getLat().toString();
        longitud_to_show = each_parada.getLon().toString();
    }

    //Carga la informacion de la bus seleccionada en el BSB
    private void uploadBsbBusData(int id){

        TextView bsb_bus_txt_alias = bottomSheetBus.findViewById(R.id.bsb_bus_txt_alias);
        TextView bsb_bus_txt_patente = bottomSheetBus.findViewById(R.id.bsb_bus_txt_patente);
        TextView bsb_bus_txt_ultima_posicion = bottomSheetBus.findViewById(R.id.bsb_bus_txt_ultima_posicion);
        ImageView bsb_bus_img_foto = bottomSheetBus.findViewById(R.id.bsb_bus_img_foto);

        Bus each_bus = searchBus(id);

        bsb_bus_txt_alias.setText(each_bus.getAlias());
        bsb_bus_txt_patente.setText("Patente: " + each_bus.getPatente());
        bsb_bus_img_foto.setImageBitmap(each_bus.getFoto());

        bsb_bus_txt_ultima_posicion.setText("Ultimo Registro: " + each_bus.getFecha());

    }



    //busca en la cache de paradas la parada segun el id
    private Parada searchParada(int id){
        for(Parada p : CACHE_PARADAS){
            if (p.getId() == id)
                return p;
        }
        return null;
    }

    //busca en la cache de Bus el bus segun el id
    private Bus searchBus(int id){
        for(Bus b : CACHE_BUS){
            if (b.getId() == id)
                return b;
        }
        return null;
    }



    //Compara dos horas  devuelve true si la primera es mayor a la segunda
    private boolean CompareTimes(Date d1, Date d2) {
        if (d1.getHours() < d2.getHours()) {
            return false;
        }
        if (d1.getHours() > d2.getHours()) {
            return true;

        } else {
            return (d1.getMinutes() > d2.getMinutes());
        }
    };

    private int diferencesTimes(Date d1, Date d2) {
        int hs = d1.getHours() - d2.getHours();
        int min = d1.getMinutes() - d2.getMinutes();

        return hs * 60 + min;
    };

    //elimina los marker de los vehiculos
    private void deleteAllMarkerBus(){
        for(int i=0; i<listMarkerBus.size();i++) {
            listMarkerBus.get(i).second.remove();
            listMarkerBusText.get(i).second.remove();
        }
        listMarkerBus.clear();
        listMarkerBusText.clear();

    }

    //rota un bitmap usada para orientar la imagen con el sentido del colectivo
    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    public void showMensajeDialog(int emoji, String message, String title){
        errorDialog = new androidx.appcompat.app.AlertDialog.Builder(this);
        errorDialog
                .setMessage(message)
                .setTitle(title)
                .setIcon(emoji)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitud_to_show+","+longitud_to_show);
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        startActivity(mapIntent);
                    }
                })

                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();

                }
        });
        androidx.appcompat.app.AlertDialog alertError = errorDialog.create();
        alertError.show();
    }
    /************************* CLASES ASYNCRONAS INTERNAS ******************************/

    private class getParadasTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getString(R.string.RestApiHttps) + getString(R.string.getParadas);
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {
                            try {
                                onConnectionFinished();
                                drawParadas(new JSONArray(response));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            onConnectionFailed(error.toString());
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(MainActivity.this);

                    String city = prefs.getString("city", "");
                    params.put("sede", city);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();

                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(MainActivity.this);

                    String city = prefs.getString("city", "");
                    params.put("sede", city);
                    return params;
                }
            };
            addToQueue(postRequest);

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
        }
    }

    private class getBusTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            isBusTaskrun = true;
            String url = getString(R.string.RestApiHttps) + getString(R.string.getBus);
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {
                            try {
                                isBusTaskrun = false;
                                drawBus(new JSONArray(response));


                            } catch (JSONException e) {
                                isBusTaskrun = false;
                                e.printStackTrace();
                            }


                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            isBusTaskrun = false;
                            onConnectionFailed(error.toString());
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(MainActivity.this);

                    String city = prefs.getString("city", "");
                    params.put("sede", city);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();

                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(MainActivity.this);

                    String city = prefs.getString("city", "");
                    params.put("sede", city);
                    return params;
                }
            };
            addToQueue(postRequest);

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            isBusTaskrun = false;
        }
    }

}
