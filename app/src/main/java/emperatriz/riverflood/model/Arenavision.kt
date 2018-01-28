package emperatriz.riverflood.model

import android.webkit.WebView
import android.webkit.WebViewClient
import emperatriz.riverflood.Sys
import org.jsoup.Jsoup
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Arenavision private constructor() : GestorPagina {

    private val avs = HashMap<String, String>()
    private var cola: Queue<String> = LinkedList()
    private var eventos: ArrayList<Evento>? = null
    private var scheduleUrl = ""
    private var guide="";

    override fun updateUrl(url: String) {
        if (url.contains("@@")){
            this.url=url.split("@@")[0]
            this.guide=url.split("@@")[1]

        }else{
            this.url=url
        }

    }

    override val nombre: String
        get() = "arenavision"

    override val id: Int
        get() = 0

    var url: String = ""


    internal var eventoIndex: Int = 0


    override fun parseData() {
        if (eventos != null && eventos!!.size > 0) {
            Sys.init().panel!!.populateGrid(eventos!!)
        } else {
            if (scheduleUrl.length == 0) {
                Sys.init().w!!.setWebViewClient(object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        Sys.init().w!!.evaluateJavascript("(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();"
                        ) {
                            html -> parseDataMain(html)
                        }
                    }
                })

                Sys.init().w!!.loadUrl(url)
            } else {
                Sys.init().w!!.setWebViewClient(object : WebViewClient() {

                    override fun onPageFinished(view: WebView, url: String) {

                        Sys.init().w!!.evaluateJavascript("(function() { return ('<html><table>'+document.getElementsByTagName('html')[0].innerHTML+'</table></html>'); })();"
                        ) { html -> parseDataSchedule(html) }


                    }
                })

                Sys.init().w!!.loadUrl(scheduleUrl)
            }

        }
    }

    override fun parseDataRefresh() {

        Sys.init().w!!.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                Sys.init().w!!.evaluateJavascript("(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();"
                ) { html -> parseDataMain(html) }
            }
        })

        Sys.init().w!!.loadUrl(url)

    }

    fun parseDataMain(html: String) {
//        var html = html
//        try {
//            html = html.replace("\\u003C", "<")
//            val doc = Jsoup.parse(html)
//            val elements = doc.getElementsByTag("nav")
//            val urlN = elements[0].getElementsByTag("li")[1].getElementsByTag("a")[0].attr("href")
//            val last = urlN.lastIndexOf('/')-2
//            scheduleUrl = url + urlN.replace("\\\"", "").substring(last)
//        } catch (ex: Exception) {
            scheduleUrl = url+"/"+guide
//        }

        parseData()
    }

    fun parseDataSchedule(html: String) {
        var html = html
        val ret = ArrayList<Evento>()
        html = html.replace("\\u003C", "<")
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm zzz")

        val doc = Jsoup.parse(html)
        val elements = doc.getElementsByTag("td")
        var i = 0
        while (i < elements.size) {
            try {
                val d = elements[i].html().replace("&nbsp;", "").replace("<br />", "<br>").replace("<br/>", "<br>").replace("\\n", "").replace("\\t", "")
                val t = Sys.limpia(elements[i + 1].html().replace("CEST", "+0200").replace("CET", "+0100"))
                val s = Sys.limpia(elements[i + 2].html())
                val c = Sys.limpia(elements[i + 3].html())
                val n = Sys.limpia(elements[i + 4].html())
                val l = elements[i + 5].html().replace("&mbsp;", "").replace("<br />", "<br>").replace("<br/>", "<br>").replace("\\n", "").replace("\\t", "")

                val ev = Evento()
                ev.hora = sdf.parse(d + " " + t)
                ev.directo = false
                ev.nombre = n
                ev.competicion = c.replace("SPANISH LA LIGA 2", "SEGUNDA DIVISIÓN").replace("SPANISH LA LIGA", "PRIMERA DIVISIÓN")
                ev.tipo = Evento.DESCONOCIDO
                when (s) {
                    "SOCCER" -> ev.tipo = Evento.FUTBOL
                    "USA MLS" -> ev.tipo = Evento.FUTBOL
                    "BASKETBALL" -> ev.tipo = Evento.BALONCESTO
                    "TENNIS" -> ev.tipo = Evento.TENIS
                    "FORMULA 1" -> ev.tipo = Evento.F1
                    "MOTOGP" -> ev.tipo = Evento.MOTOS
                    "CYCLING" -> ev.tipo = Evento.CICLISMO
                    "BOXING" -> ev.tipo = Evento.BOXEO
                    "ATHLETICS" -> ev.tipo = Evento.ATLETISMO
                }
                ev.fondo = Sys.init().getImagenDeporte(ev.tipo)
                ev.links = ArrayList()
                for (s1 in l.split("<br>".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()) {
                    for (s2 in s1.split("\\[".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0].trim({ it <= ' ' }).split("-".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()) {
                        val ln = Link()
                        ln.url = s2
                        ln.acestream=null
                        //                        if (!cola.contains(s2) && Sys.init().horaValidaEvento(ev.hora)){
                        //                            cola.add(s2);
                        //                        }
                        ln.kbps = 2000
                        ln.idioma = Link.DESCONOCIDO
                        when (s1.replace("]", "").split("\\[".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[1].trim({ it <= ' ' })) {
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
                i = i + 6
            } catch (ex: ParseException) {
                i = i + 1
            } catch (ex: Exception) {
                i = i + 6
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
            Sys.init().w!!.setWebViewClient(object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    Sys.init().w!!.evaluateJavascript("(function() { return (document.getElementsByTagName('html')[0].innerHTML); })();"
                    ) { html -> parseData4(cola.poll(), html) }
                }
            })

            Sys.init().w!!.loadUrl(url + "/"+("0" + cola.peek()).substring(cola.peek().length - 1))
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

        private var instance: Arenavision? = null

        fun init(): Arenavision {
            if (instance == null){
                instance = Arenavision()
            }

            return instance!!
        }
    }
}