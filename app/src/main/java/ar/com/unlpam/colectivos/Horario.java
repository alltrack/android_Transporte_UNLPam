package ar.com.unlpam.colectivos;

import androidx.annotation.NonNull;

import java.util.Date;

public class Horario implements Comparable<Horario> {
    Date hora;
    boolean is_lunes;
    boolean is_martes;
    boolean is_miercoles;
    boolean is_jueves;
    boolean is_viernes;
    boolean is_sabado;
    boolean is_domingo;

    public Horario(Date hora, boolean is_lunes, boolean is_martes, boolean is_miercoles, boolean is_jueves, boolean is_viernes, boolean is_sabado, boolean is_domingo) {
        this.hora = hora;
        this.is_lunes = is_lunes;
        this.is_martes = is_martes;
        this.is_miercoles = is_miercoles;
        this.is_jueves = is_jueves;
        this.is_viernes = is_viernes;
        this.is_sabado = is_sabado;
        this.is_domingo = is_domingo;
    }

    public Horario(Date hora, int is_lunes, int is_martes, int is_miercoles, int is_jueves, int is_viernes, int is_sabado, int is_domingo) {
        this.hora = hora;
        if (is_lunes == 1)
            this.is_lunes = true;
        else
            this.is_lunes = false;

        if (is_martes == 1)
            this.is_martes = true;
        else
            this.is_martes = false;

        if (is_miercoles == 1)
            this.is_miercoles = true;
        else
            this.is_miercoles = false;

        if (is_jueves == 1)
            this.is_jueves = true;
        else
            this.is_jueves = false;

        if (is_viernes == 1)
            this.is_viernes = true;
        else
            this.is_viernes = false;

        if (is_sabado == 1)
            this.is_sabado = true;
        else
            this.is_sabado = false;

        if (is_domingo == 1)
            this.is_domingo = true;
        else
            this.is_domingo = false;

    }

    @Override
    public int compareTo(@NonNull Horario horario) {
        if (hora.before(horario.hora)) {
            return -1;
        }
        if (hora.after(horario.hora)) {
            return 1;
        }
        return 0;
    }
}
