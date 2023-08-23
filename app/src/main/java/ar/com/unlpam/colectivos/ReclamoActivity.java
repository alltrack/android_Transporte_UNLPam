package ar.com.unlpam.colectivos;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class ReclamoActivity extends BaseActivity {

    private Context ctx;

    private Spinner sp_Colectivos;
    private Spinner sp_Conductor;
    private Spinner sp_Estado;
    private EditText txt_reclamo;
    private Button btn_add_foto;
    private Button btn_delete_foto;
    private Button btn_enviar;
    private Boolean isFoto = false;
    private Bitmap foto;
    private ImageView img_preview;
    private RunTimePermission runTimePermission;
    private int colectivo_select = 0;
    private int codePicture;
    private Uri selectedImageUri;

    private static final int SELECT_PICTURE = 1;
    private static final int PICK_IMAGE = 2;


    private String selectedImagePath;

    private LayoutInflater inflater;

    protected getBusTask GetBusTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reclamo);

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

        txt_reclamo = (EditText) findViewById(R.id.txt_reclamo_mensaje);
        sp_Colectivos = (Spinner) findViewById(R.id.sp_reclamo_colectivos);
        sp_Estado = (Spinner) findViewById(R.id.sp_reclamo_estado);
        sp_Conductor = (Spinner) findViewById(R.id.sp_conductor);
        btn_delete_foto = (Button) findViewById(R.id.btn_delete_foto);
        btn_add_foto = (Button) findViewById(R.id.btn_add_foto);
        btn_enviar = (Button) findViewById(R.id.btn_enviar);
        img_preview = (ImageView) findViewById(R.id.img_preview);

        ArrayList<String> strEstados = new ArrayList<String>();
        strEstados.add("Muy Bueno");
        strEstados.add("Bueno");
        strEstados.add("Malo");

        ArrayAdapter<String> comboAdapter2;
        comboAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, strEstados);

        //Cargo el spinner con los datos
        sp_Estado.setAdapter(comboAdapter2);

        ArrayList<String> strEstadosConductor = new ArrayList<String>();
        strEstadosConductor.add("Muy Bien");
        strEstadosConductor.add("Bien");
        strEstadosConductor.add("Mal");

        ArrayAdapter<String> comboAdapter3;
        comboAdapter3 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, strEstadosConductor);

        //Cargo el spinner con los datos
        sp_Conductor.setAdapter(comboAdapter3);


        inflater = this.getLayoutInflater();

        btn_delete_foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_add_foto.setVisibility(View.VISIBLE);
                btn_delete_foto.setVisibility(View.GONE);
                img_preview.setVisibility(View.GONE);
                isFoto = false;
            }
        });

        btn_add_foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View dialogView = inflater.inflate(R.layout.dl_buttom_select, null);

                AlertDialog.Builder alert = new AlertDialog.Builder( ReclamoActivity.this )
                        .setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d( "AlertDialog", "Negative" );
                            }
                        });
                alert.setView(dialogView);

                final AlertDialog alertDialog = alert.create();

                alertDialog.show();

                ImageButton btn_galery = (ImageButton) alertDialog.findViewById(R.id.btn_galery);
                ImageButton btn_camera = (ImageButton) alertDialog.findViewById(R.id.btn_camera);

                btn_galery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        alertDialog.cancel();
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);

                    }
                });

                btn_camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.cancel();

                        runTimePermission = new RunTimePermission(ReclamoActivity.this);
                        runTimePermission.requestPermission(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }, new RunTimePermission.RunTimePermissionListener() {

                            @Override
                            public void permissionGranted() {
                                // First we need to check availability of play services
                                startActivityForResult(new Intent(ctx,CameraActivity.class),PICK_IMAGE);


                            }

                            @Override
                            public void permissionDenied() {

                               // finish();
                            }
                        });
                    }
                });

            }
        });

        btn_enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = getString(R.string.RestApiHttps) + getString(R.string.setClaim);

                VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        onConnectionFinished();
                        String resultResponse = new String(response.data);
                        try {
                            JSONObject result = new JSONObject(resultResponse);
                            if(result.getString("resu").equalsIgnoreCase("1")){
                                showMensajeDialog(R.drawable.emoji_ok,getString(R.string.messageSendClaim),getString(R.string.titleSendClaim));
                            }
                            else{
                                showMensajeDialog(R.drawable.emoji_sorry,getString(R.string.messageErrorSendClaim),getString(R.string.titleErrorSendClaim));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        onConnectionFailed(error.toString());
                        Log.i("Error", error.toString());
                        error.printStackTrace();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();

                        String email = getUserEmail();
                        params.put("email", email);
                        params.put("vehiculo", sp_Colectivos.getSelectedItem().toString());
                        params.put("estado", sp_Estado.getSelectedItem().toString());
                        params.put("reclamo", txt_reclamo.getText().toString());
                        params.put("conductor", sp_Conductor.getSelectedItem().toString());
                        //PARA JPG
                        if(isFoto) {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            foto.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                            byte[] byteArray = stream.toByteArray();
                            String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                            foto.recycle();

                            params.put("file", encodedImage);
                            params.put("ext", "jpg");
                        }

                        return params;
                    }


                };
                onPreStartConnection();
                addToQueue(multipartRequest);

            }
        });

        onPreStartConnection();
        GetBusTask = new getBusTask();
        GetBusTask.execute((Void) null);

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putBoolean("isFoto", isFoto);

        savedInstanceState.putInt("sp_colectivos_value", sp_Colectivos.getSelectedItemPosition());
        savedInstanceState.putInt("sp_Conductor_value", sp_Conductor.getSelectedItemPosition());
        savedInstanceState.putInt("sp_estado_value", sp_Estado.getSelectedItemPosition());
        savedInstanceState.putString("txt_reclamo_value", txt_reclamo.getText().toString());
        if(isFoto) {
            savedInstanceState.putInt("codePicture", codePicture);
            savedInstanceState.putString("selectedImagePath", selectedImagePath);
            if (selectedImageUri != null && !selectedImageUri.equals(Uri.EMPTY))
                savedInstanceState.putString("uri", selectedImageUri.toString());
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        isFoto = savedInstanceState.getBoolean("isFoto");

        selectedImagePath = savedInstanceState.getString("selectedImagePath");

        if(isFoto) {
            codePicture = savedInstanceState.getInt("codePicture");
            if (codePicture == PICK_IMAGE) {
                try {
                    foto = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse("file://"+selectedImagePath));
                    btn_add_foto.setVisibility(View.GONE);
                    btn_delete_foto.setVisibility(View.VISIBLE);
                    isFoto = true;

                    img_preview.setImageBitmap(foto);
                    img_preview.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            else  if (codePicture == SELECT_PICTURE) {

                selectedImageUri = Uri.parse(savedInstanceState.getString("uri"));

                try {
                    foto = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    btn_add_foto.setVisibility(View.GONE);
                    btn_delete_foto.setVisibility(View.VISIBLE);

                    img_preview.setImageBitmap(foto);
                    img_preview.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        colectivo_select = savedInstanceState.getInt("sp_colectivos_value");

        sp_Conductor.setSelection(savedInstanceState.getInt("sp_conductor_value"));
        sp_Estado.setSelection(savedInstanceState.getInt("sp_estado_value"));
        txt_reclamo.setText(savedInstanceState.getString("txt_reclamo_value"));



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
                        finish();
                    }
                });
        androidx.appcompat.app.AlertDialog alertError = errorDialog.create();
        alertError.show();
    }

    public String getUserEmail() {
        AccountManager manager = AccountManager.get(ctx);
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

    //UPDATED
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            codePicture = requestCode;

            if (requestCode == PICK_IMAGE) {
                String PATH = data.getStringExtra("PATH");
                String WHO = data.getStringExtra("WHO");


                //OI FILE Manager
                selectedImagePath = PATH;

                try {
                    foto = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse("file://"+selectedImagePath));
                    btn_add_foto.setVisibility(View.GONE);
                    btn_delete_foto.setVisibility(View.VISIBLE);
                    isFoto = true;

                    img_preview.setImageBitmap(foto);
                    img_preview.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            else  if (requestCode == SELECT_PICTURE) {
                selectedImageUri = data.getData();
                try {
                    foto = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    btn_add_foto.setVisibility(View.GONE);
                    btn_delete_foto.setVisibility(View.VISIBLE);
                    isFoto = true;

                    img_preview.setImageBitmap(foto);
                    img_preview.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //UPDATED!
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if(cursor!=null)
        {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else return null;
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

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }


    //carga el combo de los colectivos.
    private void uploadColectivos (JSONArray data){
        ArrayList<String> strColectivos = new ArrayList<String>();
        ArrayAdapter<String> comboAdapter;

        for(int i = 0; i<data.length();i++){

            try {
                strColectivos.add(data.getJSONObject(i).getString("al"));
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

        }
        //Implemento el adapter con el contexto, layout, listaFrutas
        comboAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, strColectivos);
        //Cargo el spinner con los datos
        sp_Colectivos.setAdapter(comboAdapter);

        if(colectivo_select != -1)
            sp_Colectivos.setSelection(colectivo_select);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(runTimePermission!=null){
            runTimePermission.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private class getBusTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            String url = getString(R.string.RestApiHttps) + getString(R.string.getBusLite);
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {
                            try {
                                uploadColectivos(new JSONArray(response));
                                onConnectionFinished();

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
                            .getDefaultSharedPreferences(ReclamoActivity.this);

                    String city = prefs.getString("city", "");
                    params.put("sede", city);
                    return params;

                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();

                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(ReclamoActivity.this);

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
        }
    }

}
