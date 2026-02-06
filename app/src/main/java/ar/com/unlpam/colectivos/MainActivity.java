package ar.com.unlpam.colectivos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Locale;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.maps.android.ui.IconGenerator;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends BaseActivity implements OnMapReadyCallback {

    private MainViewModel viewModel;

    // UI
    private GoogleMap map;
    private LinearLayout bottomSheet;
    private LinearLayout bottomSheetBus;
    protected BottomSheetBehavior<LinearLayout> bsb;
    protected BottomSheetBehavior<LinearLayout> bsbBus;

    // Markers
    private final List<Pair<Integer, Marker>> listMarkerBus = new ArrayList<>();
    private final List<Pair<Integer, Marker>> listMarkerBusText = new ArrayList<>();
    private final List<Pair<Integer, Marker>> listMarkerText = new ArrayList<>();
    private final List<Pair<Integer, Marker>> listMarkerParadas = new ArrayList<>();

    // Location
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private FusedLocationProviderClient fusedLocationProviderClient;

    // Para navegación
    private String latitud_to_show;
    private String longitud_to_show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupUI();
        setupMap();
        observeViewModel();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                    return;
                }

                if (bsb.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bsb.setState(BottomSheetBehavior.STATE_HIDDEN);
                    return;
                }

                if (bsb.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    return;
                }

                if (bsbBus.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bsbBus.setState(BottomSheetBehavior.STATE_HIDDEN);
                    return;
                }

                if (bsbBus.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bsbBus.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    return;
                }

                // fallback → comportamiento normal
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        });

    }

    private void setupUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBus = findViewById(R.id.bottomSheetBus);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        bsb = BottomSheetBehavior.from(bottomSheet);
        bsb.setState(BottomSheetBehavior.STATE_HIDDEN);

        bsbBus = BottomSheetBehavior.from(bottomSheetBus);
        bsbBus.setState(BottomSheetBehavior.STATE_HIDDEN);

        Button button2 = findViewById(R.id.btn_ir);
        button2.setOnClickListener(v -> showMensajeDialog(
                R.drawable.emoji_warning,
                getString(R.string.messageGoOut),
                getString(R.string.titleGoOut)
        ));

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void observeViewModel() {
        // Observar paradas
        viewModel.paradas.observe(this, paradas -> {
            if (paradas != null && !paradas.isEmpty()) {
                drawParadas(paradas);
            }
        });

        // Observar buses
        viewModel.buses.observe(this, buses -> {
            if (buses != null) {
                drawBuses(buses);
            }
        });

        // Observar loading
        viewModel.isLoading.observe(this, isLoading -> {
            if (isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });

        // Observar errores
        viewModel.error.observe(this, errorState -> {
            if (errorState != null) {
                showErrorDialog(errorState.message);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "On Resume");
        viewModel.startPolling();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "On Pause");
        viewModel.stopPolling();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "On Stop");
        viewModel.stopPolling();
    }


    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        onPreStartConnection();

        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();

        //Notificar al ViewModel que el mapa está listo
        viewModel.setMapReady(true);

        //Fetch inicial de paradas
        viewModel.fetchParadas();

        // Setup marker click listener

        map.setOnMarkerClickListener(marker -> {
            MarkerTag dataTag = (MarkerTag) marker.getTag();
            if (dataTag == null) return false;

            switch (dataTag.getTipo()) {
                case "parada":
                    handleParadaClick(dataTag.getId());
                    return false;
                case "bus":
                    handleBusClick(dataTag.getId());
                    return false;
                case "text":
                    return true;
            }
            return false;
        });
    }

    private void handleParadaClick(int id) {
        for (Pair<Integer, Marker> p : listMarkerText) {
            Marker m = p.second;
            m.setVisible(p.first != id);
        }
        uploadBsbData(id);
        bsbBus.setState(BottomSheetBehavior.STATE_HIDDEN);
        bsb.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void handleBusClick(int id) {
        for (Pair<Integer, Marker> p : listMarkerText) {
            p.second.setVisible(true);
        }
        uploadBsbBusData(id);
        bsb.setState(BottomSheetBehavior.STATE_HIDDEN);
        bsbBus.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    // Dibuja buses
    private void drawBuses(ArrayList<Bus> buses) {
        deleteAllMarkerBus();

        for (Bus bus : buses) {
            LatLng latLng = new LatLng(bus.getLatitud(), bus.getLongitud());

            // Procesar imagen Base64
            Bitmap foto = BitmapFactory.decodeResource(getResources(), R.drawable.imagen_no_disponible);
            if (bus.getImagenBase64() != null && !bus.getImagenBase64().isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(bus.getImagenBase64(), Base64.DEFAULT);
                    foto = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                } catch (Exception e) {
                    Log.e(TAG, "Error decoding bus image: " + e.getMessage());
                }
            }
            bus.setFoto(foto);

            // Marker de texto con número
            IconGenerator icg = new IconGenerator(this);
            icg.setStyle(IconGenerator.STYLE_PURPLE);
            icg.setContentPadding(3, 1, 3, 1);
            Bitmap bm = icg.makeIcon(bus.getAlias());

            Marker markerText = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(bm))
                    .anchor(0.5f, 2.15f)
            );
            assert markerText != null;
            markerText.setTag(new MarkerTag(bus.getId(), "text"));
            listMarkerBusText.add(new Pair<>(bus.getId(), markerText));

            // Marker del bus con rotación
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_mov_colectivo);
            Bitmap rotatedIcon = rotateBitmap(icon, bus.getSentido());

            Marker marker = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(bus.getAlias())
                    .icon(BitmapDescriptorFactory.fromBitmap(rotatedIcon))
            );
            assert marker != null;
            marker.setTag(new MarkerTag(bus.getId(), "bus"));
            listMarkerBus.add(new Pair<>(bus.getId(), marker));
        }
    }

    // Dibuja paradas
    private void drawParadas(ArrayList<Parada> paradas) {
        // Limpiar markers anteriores de paradas
        for (Pair<Integer, Marker> pair : listMarkerParadas) {
            pair.second.remove();
        }
        for (Pair<Integer, Marker> pair : listMarkerText) {
            pair.second.remove();
        }
        listMarkerParadas.clear();
        listMarkerText.clear();

        for (Parada parada : paradas) {
            LatLng latLng = new LatLng(parada.getLat(), parada.getLon());

            // Marker de texto con denominación
            IconGenerator icg = new IconGenerator(this);
            icg.setContentPadding(3, 3, 3, 3);
            icg.setStyle(IconGenerator.STYLE_ORANGE);
            Bitmap bm = icg.makeIcon(parada.getDenominacion());

            Marker markerText = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(bm))
                    .anchor(0.5f, 2.7f)
            );
            assert markerText != null;
            markerText.setTag(new MarkerTag(parada.getId(), "text"));
            listMarkerText.add(new Pair<>(parada.getId(), markerText));

            // Marker de parada
            Marker marker = map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(parada.getDenominacion())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_parada_2))
            );
            assert marker != null;
            marker.setTag(new MarkerTag(parada.getId(), "parada"));
            listMarkerParadas.add(new Pair<>(parada.getId(), marker));
        }

        // Centrar cámara según ciudad
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String city = prefs.getString("city", "");

        if (city.equalsIgnoreCase("GP")) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(-35.65662, -63.75682), 13));
        } else if (city.equalsIgnoreCase("SR")) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(-36.626270, -64.291310), 13));
        }
    }

    // Cargar info de parada en el BottomSheet
    private void uploadBsbData(int id) {

        Calendar now = Calendar.getInstance();

        TextView bsb_txt_denominacion = bottomSheet.findViewById(R.id.bsb_txt_denominacion);
        TextView bsb_txt_direccion = bottomSheet.findViewById(R.id.bsb_txt_direccion);
        TextView bsb_txt_tiempo_aprox = bottomSheet.findViewById(R.id.bsb_txt_tiempo_aprox);
        TextView bsb_txt_title_horarios = bottomSheet.findViewById(R.id.bsb_txt_title_horarios);
        TextView bsb_txt_horarios = bottomSheet.findViewById(R.id.bsb_txt_horarios);

        Parada parada = viewModel.findParadaById(id);
        if (parada == null) return;

        bsb_txt_denominacion.setText(parada.getDenominacion());
        bsb_txt_direccion.setText(parada.getDireccion());

        StringBuilder horariosText = new StringBuilder();
        String tiempoText = "";
        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());

        boolean controlNextParada = false;
        for (Horario h : parada.getHorarios(now.get(Calendar.DAY_OF_WEEK) - 1)) {
            horariosText.append(dateFormat.format(h.hora)).append(" hs. | ");

            if (compareTimes(h.hora, now.getTime())) {
                if (!controlNextParada) {
                    int diff = differencesTimes(h.hora, now.getTime());
                    tiempoText = "Próximo micro en " + diff + " min. aprox.";
                    controlNextParada = true;
                }
            }
        }

        if (horariosText.length() > 0) {
            horariosText.setLength(horariosText.length() - 2);
        }

        bsb_txt_horarios.setText(horariosText.toString());

        // ⚠️ NUEVO: Manejar visibilidad del tiempo aproximado
        if (tiempoText.isEmpty()) {
            bsb_txt_tiempo_aprox.setVisibility(View.GONE);
        } else {
            bsb_txt_tiempo_aprox.setText(tiempoText);
            bsb_txt_tiempo_aprox.setVisibility(View.VISIBLE);
        }

        // Manejar visibilidad del título de horarios
        if (horariosText.length() == 0) {
            bsb_txt_title_horarios.setVisibility(View.GONE);
            // ⚠️ OPCIONAL: Si quieres ocultar también el contenedor de horarios
            findViewById(R.id.horarios_container).setVisibility(View.GONE);
        } else {
            bsb_txt_title_horarios.setVisibility(View.VISIBLE);
            findViewById(R.id.horarios_container).setVisibility(View.VISIBLE);
        }

        latitud_to_show = parada.getLat().toString();
        longitud_to_show = parada.getLon().toString();
    }

    // Cargar info de bus en el BottomSheet
    @SuppressLint("SetTextI18n")
    private void uploadBsbBusData(int id) {
        TextView bsb_bus_txt_alias = bottomSheetBus.findViewById(R.id.bsb_bus_txt_alias);
        TextView bsb_bus_txt_patente = bottomSheetBus.findViewById(R.id.bsb_bus_txt_patente);
        TextView bsb_bus_txt_ultima_posicion = bottomSheetBus.findViewById(R.id.bsb_bus_txt_ultima_posicion);
        ImageView bsb_bus_img_foto = bottomSheetBus.findViewById(R.id.bsb_bus_img_foto);

        Bus bus = viewModel.findBusById(id);
        if (bus == null) return;

        bsb_bus_txt_alias.setText(bus.getAlias());
        bsb_bus_txt_patente.setText("Patente: " + bus.getPatente());
        bsb_bus_img_foto.setImageBitmap(bus.getFoto());
        bsb_bus_txt_ultima_posicion.setText("Ultimo Registro: " + bus.getFecha());
    }

    // Helpers de tiempo
    private boolean compareTimes(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();

        c1.setTime(d1);
        c2.setTime(d2);

        int h1 = c1.get(Calendar.HOUR_OF_DAY);
        int m1 = c1.get(Calendar.MINUTE);

        int h2 = c2.get(Calendar.HOUR_OF_DAY);
        int m2 = c2.get(Calendar.MINUTE);

        if (h1 < h2) return false;
        if (h1 > h2) return true;

        return m1 > m2;
    }

    private int differencesTimes(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();

        c1.setTime(d1);
        c2.setTime(d2);

        int t1 = c1.get(Calendar.HOUR_OF_DAY) * 60
                + c1.get(Calendar.MINUTE);

        int t2 = c2.get(Calendar.HOUR_OF_DAY) * 60
                + c2.get(Calendar.MINUTE);

        return t1 - t2;
    }

    // Eliminar markers de buses
    private void deleteAllMarkerBus() {
        for (Pair<Integer, Marker> pair : listMarkerBus) {
            pair.second.remove();
        }
        for (Pair<Integer, Marker> pair : listMarkerBusText) {
            pair.second.remove();
        }
        listMarkerBus.clear();
        listMarkerBusText.clear();
    }

    // Rotar bitmap para orientar el bus
    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    // ⚠️ OVERRIDE: Dialog con navegación a Google Maps
    @Override
    public void showMensajeDialog(int emoji, String message, String title) {
        if (isFinishing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle(title)
                .setIcon(emoji)
                .setCancelable(false)
                .setPositiveButton("Ok", (dialog, id) -> {
                    dialog.cancel();
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitud_to_show + "," + longitud_to_show);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                })
                .setNegativeButton("Cancelar", (dialog, id) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

    // ===== PERMISOS DE UBICACIÓN =====

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
                updateLocationUI();
            }
        }
    }

    private void updateLocationUI() {
        if (map == null) return;

        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    // ===== MENU =====

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
