package ar.com.unlpam.colectivos;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ReclamoActivity extends BaseActivity {

    private static final String TAG = "ReclamoActivity";

    // ViewModel
    private ReclamoViewModel viewModel;

    // UI
    private AutoCompleteTextView sp_Colectivos;
    private AutoCompleteTextView sp_Conductor;
    private AutoCompleteTextView sp_Estado;
    private EditText txt_reclamo;
    private Button btn_add_foto;
    private Button btn_delete_foto;
    private Button btn_enviar;
    private ImageView img_preview;
    private MaterialCardView card_preview;

    // Foto
    private Boolean isFoto = false;
    private Bitmap foto;
    private final int colectivo_select = 0;

    // Uri para la foto capturada
    private Uri photoUri;

    // ActivityResultLaunchers modernos
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reclamo);

        viewModel = new ViewModelProvider(this).get(ReclamoViewModel.class);

        setupUI();
        setupActivityResultLaunchers();
        setupSpinners();
        setupButtons();
        observeViewModel();

        viewModel.fetchColectivos();
    }

    private void setupUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        txt_reclamo = findViewById(R.id.txt_reclamo_mensaje);
        sp_Colectivos = findViewById(R.id.sp_reclamo_colectivos);
        sp_Estado = findViewById(R.id.sp_reclamo_estado);
        sp_Conductor = findViewById(R.id.sp_conductor);
        btn_delete_foto = findViewById(R.id.btn_delete_foto);
        btn_add_foto = findViewById(R.id.btn_add_foto);
        btn_enviar = findViewById(R.id.btn_enviar);
        img_preview = findViewById(R.id.img_preview);
        card_preview = findViewById(R.id.card_preview);

        card_preview.setVisibility(View.GONE);
        btn_add_foto.setVisibility(View.VISIBLE);
    }

    private void setupActivityResultLaunchers() {
        // Launcher para seleccionar foto de galería
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        try {
                            foto = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                            showPhoto();
                        } catch (IOException e) {
                            Log.e(TAG, "Error loading image: " + e.getMessage());
                            showErrorDialog("Error cargando imagen");
                        }
                    }
                }
        );

        // Launcher para permiso de cámara
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    Log.d(TAG, "Camera permission result: " + isGranted);
                    if (isGranted) {
                        openSystemCamera();
                    } else {
                        Log.e(TAG, "Camera permission DENIED");
                        showMensajeDialog(
                                R.drawable.emoji_sorry,
                                "Se necesita permiso de cámara para tomar fotos",
                                "Permiso denegado"
                        );
                    }
                }
        );

// Launcher para capturar foto con cámara del sistema
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    Log.d(TAG, "Camera result - success: " + success + ", photoUri: " + photoUri);
                    if (success && photoUri != null) {
                        try {
                            foto = MediaStore.Images.Media.getBitmap(getContentResolver(), photoUri);
                            Log.d(TAG, "Photo loaded successfully");
                            showPhoto();
                        } catch (IOException e) {
                            Log.e(TAG, "Error loading camera image", e);
                            showErrorDialog("Error cargando foto");
                        }
                    } else {
                        Log.w(TAG, "Camera was cancelled or failed");
                    }
                }
        );



    }

    private void setupSpinners() {
        // AutoComplete de Estado del vehículo
        String[] strEstados = {"Muy Bueno", "Bueno", "Malo"};
        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                strEstados
        );
        sp_Estado.setAdapter(adapterEstado);

        // AutoComplete de Conductor
        String[] strEstadosConductor = {"Muy Bien", "Bien", "Mal"};
        ArrayAdapter<String> adapterConductor = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                strEstadosConductor
        );
        sp_Conductor.setAdapter(adapterConductor);
    }

    private void setupButtons() {
        btn_delete_foto.setOnClickListener(v -> {
            card_preview.setVisibility(View.GONE);
            btn_add_foto.setVisibility(View.VISIBLE);
            isFoto = false;
            if (foto != null && !foto.isRecycled()) {
                foto.recycle();
                foto = null;
            }
        });

        btn_add_foto.setOnClickListener(v -> showPhotoSourceDialog());

        btn_enviar.setOnClickListener(v -> enviarReclamo());
    }

    private void observeViewModel() {
        viewModel.colectivos.observe(this, colectivos -> {
            if (colectivos != null && !colectivos.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        colectivos
                );
                sp_Colectivos.setAdapter(adapter);

                // Seleccionar el primero por defecto
                if (!colectivos.isEmpty()) {
                    sp_Colectivos.setText(colectivos.get(colectivo_select), false);
                }
            }
        });

        viewModel.isLoading.observe(this, isLoading -> {
            if (isLoading) {
                showLoading();
            } else {
                hideLoading();
            }
        });

        viewModel.error.observe(this, errorState -> {
            if (errorState != null) {
                showErrorDialog(errorState.message);
            }
        });
    }

    private void showPhotoSourceDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dl_button_select, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();


        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        alertDialog.show();


        View btn_galery = dialogView.findViewById(R.id.btn_galery);
        View btn_camera = dialogView.findViewById(R.id.btn_camera);

        if (btn_galery != null) {
            btn_galery.setOnClickListener(v -> {
                alertDialog.dismiss();
                openGallery();
            });
        }

        if (btn_camera != null) {
            btn_camera.setOnClickListener(v -> {
                alertDialog.dismiss();
                requestCameraPermissionAndOpen();
            });
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        pickImageLauncher.launch(Intent.createChooser(intent, "Seleccionar Imagen"));
    }

    private void requestCameraPermissionAndOpen() {
        Log.d(TAG, "requestCameraPermissionAndOpen called");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission already granted");
            openSystemCamera();
        } else {
            Log.d(TAG, "Requesting camera permission");
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openSystemCamera() {
        try {
            Log.d(TAG, "openSystemCamera - START");

            // Crear archivo para la foto
            File photoFile = new File(
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "reclamo_" + System.currentTimeMillis() + ".jpg"
            );

            Log.d(TAG, "Photo file created: " + photoFile.getAbsolutePath());

            // Obtener Uri usando FileProvider
            photoUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );

            Log.d(TAG, "Photo URI: " + photoUri);

            // Lanzar cámara del sistema
            takePictureLauncher.launch(photoUri);

            Log.d(TAG, "Camera launcher executed");

        } catch (Exception e) {
            Log.e(TAG, "Error opening camera", e);
            showErrorDialog("Error abriendo cámara: " + e.getMessage());
        }
    }


    private void showPhoto() {
        card_preview.setVisibility(View.VISIBLE);
        btn_add_foto.setVisibility(View.GONE);
        img_preview.setImageBitmap(foto);
        isFoto = true;
    }

    private void enviarReclamo() {
        // Validaciones
        if (sp_Colectivos.getText().toString().trim().isEmpty()) {
            sp_Colectivos.setError("Seleccione un colectivo");
            return;
        }

        if (sp_Estado.getText().toString().trim().isEmpty()) {
            sp_Estado.setError("Seleccione el estado");
            return;
        }

        if (sp_Conductor.getText().toString().trim().isEmpty()) {
            sp_Conductor.setError("Seleccione el desempeño");
            return;
        }

        if (txt_reclamo.getText().toString().trim().isEmpty()) {
            txt_reclamo.setError("Ingrese un comentario");
            return;
        }

        onPreStartConnection();

        String url = getString(R.string.RestApiHttps) + getString(R.string.setClaim);

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                Request.Method.POST,
                url,
                response -> {
                    onConnectionFinished();
                    handleReclamoResponse(response);
                },
                error -> {
                    onConnectionFailed(error.toString());
                    Log.e(TAG, "Error: " + error);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("email", getUserEmail());
                params.put("vehiculo", sp_Colectivos.getText().toString());
                params.put("estado", sp_Estado.getText().toString());
                params.put("reclamo", txt_reclamo.getText().toString());
                params.put("conductor", sp_Conductor.getText().toString());

                if (isFoto && foto != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    foto.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                    byte[] byteArray = stream.toByteArray();
                    String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                    params.put("file", encodedImage);
                    params.put("ext", "jpg");
                }

                return params;
            }
        };

        addToQueue(multipartRequest);
    }

    private void handleReclamoResponse(NetworkResponse response) {
        String resultResponse = new String(response.data);
        try {
            JSONObject result = new JSONObject(resultResponse);
            if (result.getString("resu").equalsIgnoreCase("1")) {
                showMensajeDialog(
                        R.drawable.emoji_ok,
                        getString(R.string.messageSendClaim),
                        getString(R.string.titleSendClaim)
                );
            } else {
                showMensajeDialog(
                        R.drawable.emoji_sorry,
                        getString(R.string.messageErrorSendClaim),
                        getString(R.string.titleErrorSendClaim)
                );
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing response: " + e.getMessage());
        }
    }

    private String getUserEmail() {
        AccountManager manager = AccountManager.get(this);
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<>();

        for (Account account : accounts) {
            possibleEmails.add(account.name);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            return possibleEmails.get(0);
        }
        return "";
    }

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
                    finish();
                });

        AlertDialog alert = builder.create();
        alert.show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Liberar bitmap para evitar memory leaks
        if (foto != null && !foto.isRecycled()) {
            foto.recycle();
            foto = null;
        }
    }
}