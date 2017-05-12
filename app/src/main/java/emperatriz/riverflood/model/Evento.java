package emperatriz.riverflood.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ramon on 24/04/2017.
 */



public class Evento implements Serializable {

    public static final int FUTBOL=0, BALONCESTO=1, F1=2, MOTOS=3, TENIS=4, DESCONOCIDO=5, CICLISMO=6, BOXEO=7, ATLETISMO=8;

    public String nombre, competicion, url;
    public int tipo, fondo;
    public Date hora;
    public boolean directo;
    public ArrayList<Link> links;
}
