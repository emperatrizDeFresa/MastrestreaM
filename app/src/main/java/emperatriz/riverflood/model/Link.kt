package emperatriz.riverflood.model

/**
 * Created by ramon on 24/04/2017.
 */

class Link {

    var url: String? = null
    var acestream: String? = null
    var idioma: Int = 0
    var kbps: Int = 0
    var fps: Int = 0
    var resolution: Int = 0

    companion object {

        val ESP = 0
        val ENG = 1
        val POR = 2
        val ITA = 3
        val RU = 4
        val DESCONOCIDO = 5
        val GER = 6
        val UCR = 7
        val SUE = 8
        val CRO = 9
        val LET = 10
        val FRA = 11
        val FPS50 = 0
        val FPS25 = 1
        val NOFPS = 2
        val HD1080 = 0
        val HD720 = 1
        val SD576 = 2
        val NORES = 4
    }
}
