package ar.com.unlpam.colectivos;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Collections;
import java.util.Date;


public class Parada {
    int id;
    Double lat;
    Double lon;
    String denominacion;
    String direccion;
    ArrayList<Horario> horarios = new ArrayList<Horario>();
    String JSONDefinition;


    public Parada(int id, Double lat, Double lon, String denominacion, String direccion) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.denominacion = denominacion;
        this.direccion = direccion;
    }

    public Parada(int id, Double lat, Double lon, String denominacion, String direccion, JSONArray _horarios) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.denominacion = denominacion;
        this.direccion = direccion;

        for(int i=0; i< _horarios.length(); i++){
            JSONObject each = null;
            try {
                each = _horarios.getJSONObject(i);

                String string_date = each.getString("hs");
                DateFormat format = new SimpleDateFormat("kk:mm:ss");
                Date date = format.parse(string_date);


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

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public Parada(int id, Double lat, Double lon, String denominacion, String direccion, JSONArray _horarios, String JSONDefinition) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.denominacion = denominacion;
        this.direccion = direccion;
        this.JSONDefinition = JSONDefinition;


        for (int i = 0; i < _horarios.length(); i++) {
            JSONObject each = null;
            try {
                each = _horarios.getJSONObject(i);

                String string_date = each.getString("hs");
                DateFormat format = new SimpleDateFormat("kk:mm:ss");
                Date date = format.parse(string_date);


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

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

        public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public void setDenominacion(String denominacion) {
        this.denominacion = denominacion;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public ArrayList<Horario> getHorarios() {
        return horarios;
    }

    public void setHorarios(ArrayList<Horario> horarios) {
        this.horarios = horarios;
    }

    public ArrayList<Horario> getHorarios(int day_of_week) {

        ArrayList<Horario> resu = new ArrayList<Horario>();
        for(Horario hs: horarios){
            switch (day_of_week){
                case 0:
                    if(hs.is_domingo)
                        resu.add(hs);
                    break;
                case 1:
                    if(hs.is_lunes)
                        resu.add(hs);
                    break;

                case 2:
                    if(hs.is_martes)
                        resu.add(hs);
                    break;
                case 3:
                    if(hs.is_miercoles)
                        resu.add(hs);
                    break;
                case 4:
                    if(hs.is_jueves)
                        resu.add(hs);
                    break;
                case 5:
                    if(hs.is_viernes)
                        resu.add(hs);
                    break;
                case 6:
                    if(hs.is_sabado)
                        resu.add(hs);
                    break;
            }
        }

        Collections.sort(resu);
        return resu;

    }

    public String getJSONDefinition() {
        return JSONDefinition;
    }

    public void setJSONDefinition(String JSONDefinition) {
        this.JSONDefinition = JSONDefinition;
    }
}
