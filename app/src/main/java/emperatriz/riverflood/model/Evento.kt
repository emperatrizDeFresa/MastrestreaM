package emperatriz.riverflood.model

import java.io.Serializable
import java.util.ArrayList
import java.util.Date

/**
 * Created by ramon on 24/04/2017.
 */


class Evento : Serializable {

    var nombre: String? = null
    var competicion: String? = null
    var url: String? = null
    var tipo: Int = 0
    var fondo: Int = 0
    var hora: Date? = null
    var directo: Boolean = false
    var links: ArrayList<Link>? = null

    companion object {

        val FUTBOL = 0
        val BALONCESTO = 1
        val F1 = 2
        val MOTOS = 3
        val TENIS = 4
        val DESCONOCIDO = 5
        val CICLISMO = 6
        val BOXEO = 7
        val ATLETISMO = 8
    }
}
