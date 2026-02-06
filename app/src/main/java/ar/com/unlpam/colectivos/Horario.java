package ar.com.unlpam.colectivos;

import androidx.annotation.NonNull;

import java.util.Date;

public class Horario implements Comparable<Horario> {

    // ⚠️ Ahora son private
    public Date hora;
    public boolean is_lunes;
    public boolean is_martes;
    public boolean is_miercoles;
    public boolean is_jueves;
    public boolean is_viernes;
    public boolean is_sabado;
    public boolean is_domingo;

    public Horario(Date hora, int is_lunes, int is_martes, int is_miercoles,
                   int is_jueves, int is_viernes, int is_sabado, int is_domingo) {
        this.hora = hora;
        this.is_lunes = (is_lunes == 1);
        this.is_martes = (is_martes == 1);
        this.is_miercoles = (is_miercoles == 1);
        this.is_jueves = (is_jueves == 1);
        this.is_viernes = (is_viernes == 1);
        this.is_sabado = (is_sabado == 1);
        this.is_domingo = (is_domingo == 1);
    }

    @Override
    public int compareTo(@NonNull Horario otro) {
        return hora.compareTo(otro.hora);
    }
}