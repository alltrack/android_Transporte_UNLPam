package ar.com.unlpam.colectivos;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class Parada {

    private int id;
    private Double lat;
    private Double lon;
    private String denominacion;
    private String direccion;
    private ArrayList<Horario> horarios = new ArrayList<>();
    private String jsonDefinition;


    public Parada(int id, Double lat, Double lon, String denominacion, String direccion, JSONArray _horarios) {
        this(id, lat, lon, denominacion, direccion, _horarios, null);
    }

    public Parada(int id, Double lat, Double lon, String denominacion, String direccion,
                  JSONArray _horarios, String jsonDefinition) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.denominacion = denominacion;
        this.direccion = direccion;
        this.jsonDefinition = jsonDefinition;

        parseHorarios(_horarios);
    }

    private void parseHorarios(JSONArray _horarios) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        for (int i = 0; i < _horarios.length(); i++) {
            try {
                JSONObject each = _horarios.getJSONObject(i);
                String stringDate = each.getString("hs");
                Date date = format.parse(stringDate);

                if (date != null) {
                    horarios.add(new Horario(
                            date,
                            each.getInt("lu"),
                            each.getInt("ma"),
                            each.getInt("mi"),
                            each.getInt("ju"),
                            each.getInt("vi"),
                            each.getInt("sa"),
                            each.getInt("do")
                    ));
                }
            } catch (JSONException | ParseException e) {
                Log.e("TRANSPORTE UNLPAM", "Error parseando respuesta", e);
            }
        }
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDenominacion() { return denominacion; }
    public void setDenominacion(String denominacion) { this.denominacion = denominacion; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLon() { return lon; }
    public void setLon(Double lon) { this.lon = lon; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public ArrayList<Horario> getHorarios() { return horarios; }
    public void setHorarios(ArrayList<Horario> horarios) { this.horarios = horarios; }

    public String getJSONDefinition() { return jsonDefinition; }
    public void setJSONDefinition(String jsonDefinition) { this.jsonDefinition = jsonDefinition; }

    //getHorarios por día
    public ArrayList<Horario> getHorarios(int dayOfWeek) {
        ArrayList<Horario> resultado = new ArrayList<>();

        for (Horario h : horarios) {
            if (isDayActive(h, dayOfWeek)) {
                resultado.add(h);
            }
        }

        Collections.sort(resultado);
        return resultado;
    }

    // Helper para verificar si horario es activo en un día
    private boolean isDayActive(Horario h, int dayOfWeek) {
        switch (dayOfWeek) {
            case 0: return h.is_domingo;
            case 1: return h.is_lunes;
            case 2: return h.is_martes;
            case 3: return h.is_miercoles;
            case 4: return h.is_jueves;
            case 5: return h.is_viernes;
            case 6: return h.is_sabado;
            default: return false;
        }
    }
}