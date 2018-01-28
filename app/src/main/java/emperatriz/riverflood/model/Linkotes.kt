package emperatriz.riverflood.model

import android.webkit.WebView
import android.webkit.WebViewClient
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import emperatriz.riverflood.Sys
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Linkotes private constructor() : GestorPagina {

    private val avs = HashMap<String, String>()
    private var cola: Queue<String> = LinkedList()
    private var eventos: ArrayList<Evento>? = null
    var url: String = ""

    override fun updateUrl(url: String) {

       this.url=url


    }

    override val nombre: String
        get() = "arenavision"

    override val id: Int
        get() = 0

    internal var eventoIndex: Int = 0


    override fun parseData() {
        if (eventos != null && eventos!!.size > 0) {
            Sys.init().panel!!.populateGrid(eventos!!)
        } else {
                Sys.init().w!!.setWebViewClient(object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        Sys.init().w!!.evaluateJavascript("(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();"
                        ) {
                            html -> parseDataSchedule(html)
                        }
                    }
                })

                Sys.init().w!!.loadUrl(url)
        }
    }

    override fun parseDataRefresh() {

        Sys.init().w!!.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                Sys.init().w!!.evaluateJavascript("(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();"
                ) { html -> parseDataSchedule(html) }
            }
        })

        Sys.init().w!!.loadUrl(url)

    }


    fun parseDataSchedule(html: String) {
        var html = html
        val ret = ArrayList<Evento>()
        html = html.replace("\\u003C", "<")
        val sdf = SimpleDateFormat("dd/MM/yyyy")

        val sdf2 = SimpleDateFormat("dd/MM/yyyy HH:mm")
        var today= sdf.format(Date())
        var tom = Calendar.getInstance()
        tom.add(Calendar.DAY_OF_MONTH,1)
        var tomorrow = sdf.format(tom.time)

        val doc = Jsoup.parse(html)
        var h4 = doc.getElementsByTag("h4")

        var todayIndex=0;

        var idx=0
        for (h in h4){
            try{
                if (today.equals(h.html().split(" ")[1])){
                    todayIndex=idx
                }
            }catch(ex:Exception){

            }
            idx++
        }


        val days = doc.getElementsByTag("table")
        for (d in days.indices){
            if (d==todayIndex || d==todayIndex+1){
                val elements = days[d].getElementsByTag("td")
                var i = 0
                while (i < elements.size) {
                    try {
                        val t = elements[i].html()
                        val s = elements[i + 1].html()
                        val n = elements[i + 2].html()
                        val l = elements[i + 3].text().replace("av","")

                        val ev = Evento()
                        ev.hora = sdf2.parse((if (d==todayIndex) today+" " else tomorrow+" ") + t)
                        ev.directo = false
                        ev.nombre = n.split("(")[0].trim()
                        ev.competicion = n.split("(")[1].replace(")","").replace("SPANISH LA LIGA 2", "SEGUNDA DIVISIÓN").replace("SPANISH LA LIGA", "PRIMERA DIVISIÓN")
                        ev.tipo = Evento.DESCONOCIDO
                        when (s) {
                            "SOCCER" -> ev.tipo = Evento.FUTBOL
                            "USA MLS" -> ev.tipo = Evento.FUTBOL
                            "BASKETBALL" -> ev.tipo = Evento.BALONCESTO
                            "TENNIS" -> ev.tipo = Evento.TENIS
                            "FORMULA 1" -> ev.tipo = Evento.F1
                            "MOTORSPORT" -> ev.tipo = Evento.F1
                            "MOTOGP" -> ev.tipo = Evento.MOTOS
                            "CYCLING" -> ev.tipo = Evento.CICLISMO
                            "BOXING" -> ev.tipo = Evento.BOXEO
                            "ATHLETICS" -> ev.tipo = Evento.ATLETISMO
                        }
                        ev.fondo = Sys.init().getImagenDeporte(ev.tipo)
                        ev.links = ArrayList()


                        var language=""
                        for (linkText: String in l.split(" ")){
                            if (linkText.endsWith(":")){
                                language = linkText.replace(":","").toUpperCase()
                            }else{
                                val ln = Link()
                                ln.url = linkText.trim()
                                ln.acestream=null
                                ln.kbps = 2000
                                ln.idioma = Link.DESCONOCIDO
                                when (language) {
                                    "SPA" -> ln.idioma = Link.ESP
                                    "ENG" -> ln.idioma = Link.ENG
                                    "POR" -> ln.idioma = Link.POR
                                    "ITA" -> ln.idioma = Link.ITA
                                    "GER" -> ln.idioma = Link.GER
                                    "FRE" -> ln.idioma = Link.FRA
                                }
                                ev.links!!.add(ln)
                            }
                        }

                        if (Sys.init().horaValidaEvento(ev.hora)) {
                            ret.add(ev)
                        }
                        i = i + 4
                    } catch (ex: ParseException) {
                        i = i + 1
                    } catch (ex: Exception) {
                        i = i + 4
                    }


                }
            }
        }

        eventos = ret
        Sys.init().panel!!.populateGrid(eventos!!)
    }

    override fun getLinks(index: Int) {
        eventoIndex = index
        Sys.init().evento!!.populateLinks(eventos!![eventoIndex])
        cola = LinkedList()
        for (l in eventos!![index].links!!) {
            if (!cola.contains(l.url) && (l.acestream == null || l.acestream!!.length<10)) {
                cola.add(l.url)
            }
        }
        parseData3()
    }

    private fun parseData3() {

        if (cola.size > 0) {
//            Sys.init().w!!.setWebViewClient(object : WebViewClient() {
//                override fun onPageFinished(view: WebView, url: String) {
//                    Sys.init().w!!.evaluateJavascript("(function() { return (document.getElementsByTagName('html')[0].innerHTML); })();"
//                    ) { html -> parseData4(cola.poll(), html) }
//                }
//            })
//            Sys.init().w!!.loadUrl(url + "/"+("0" + cola.peek()).substring(cola.peek().length - 1))
            do {
                Fuel.post("https://linkotes.com/arenavision/aj_canal.php", listOf("id" to cola.poll(), "nocatxe" to "0")).responseJson() { request, response, result ->
                    try {
                        var json = JSONObject(result.get().content)
                        var av = json.get("id") as Integer
                        var ac = json.get("ace") as String
                        avs.put(av.toString(), ac)
                        for (evento in eventos!!) {
                            for (l in evento.links!!) {
                                try {
                                    l.acestream = avs.get(l.url).toString()
                                    l.fps = Link.NOFPS
                                    l.resolution = Link.NORES

                                } catch (ex: Exception) {
                                }
                            }

                        }
                        Sys.init().evento!!.populateLinks(eventos!![eventoIndex])
                    } catch (ex: Exception) {
                    }

                }
            }while(cola.size>0)
        } else {
            for (evento in eventos!!) {
                for (l in evento.links!!) {
                    try {
                        l.acestream = avs.get(l.url).toString()
                        l.fps = Link.NOFPS
                        l.resolution = Link.NORES
                    } catch (ex: Exception) {
                    }

                }
            }
            Sys.init().evento!!.populateLinks(eventos!![eventoIndex])

        }

    }

    private fun parseData4(av: String, html: String) {
        var html = html
        try {
            html = html.replace("\\u003C", "<")

            val start = html.indexOf("acestream:")
            val end = html.indexOf("\"", start + "acestream:".length)
            val ac = html.substring(start, end - 1)

            avs.put(av, ac)
            for (evento in eventos!!) {
                for (l in evento.links!!) {
                    try {
                        l.acestream = avs.get(l.url).toString()
                        l.fps = Link.NOFPS
                        l.resolution = Link.NORES
                    } catch (ex: Exception) {
                    }

                }
            }
            Sys.init().evento!!.populateLinks(eventos!![eventoIndex])
        } catch (ex: Exception) {
            avs.put(av, "")
        }

        parseData3()
    }

    companion object {

        private var instance: Linkotes? = null

        fun init(): Linkotes {
            if (instance == null){
                instance = Linkotes()
            }

            return instance!!
        }
    }
}