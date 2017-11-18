package emperatriz.riverflood.model


import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Comparator
import java.util.Date
import java.util.HashMap

import emperatriz.riverflood.Sys

class LivesportWs private constructor() : GestorPagina {
    override fun updateUrl(url: String) {
        this.url=url
    }

    private val avs = HashMap<String,String>()
    private var eventos: ArrayList<Evento>? = null

    override val nombre: String
        get() = "livesport.ws"

    override val id: Int
        get() = 1

    val javascript: String
        get() = "(function() { return ('<html>'+document.getElementsByClassName('drop-list-holder')[0].innerHTML+'</html>'); })();"

    var url: String = "http://livesport.ws/en"



    internal var eventoIndex: Int = 0


    private fun parseDataInternal() {


        Sys.init().w!!.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                Sys.init().w!!.evaluateJavascript(javascript
                ) { html -> parseData2(html) }
            }
        })

        Sys.init().w!!.loadUrl(url)


    }

    override fun parseData() {
        if (eventos != null && eventos!!.size > 0) {
            Sys.init().panel!!.populateGrid(eventos!!)
        } else {

            eventos = ArrayList()

            Sys.init().w!!.setWebViewClient(object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    Sys.init().w!!.evaluateJavascript(javascript
                    ) { html -> parseData2(html) }
                }
            })

            Sys.init().w!!.loadUrl(url)
        }

    }

    override fun parseDataRefresh() {
        eventos = ArrayList()

        Sys.init().w!!.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                Sys.init().w!!.evaluateJavascript(javascript
                ) { html -> parseData2(html) }
            }
        })

        Sys.init().w!!.loadUrl(url)
    }

    fun parseData2(html: String) {
        var html = html
        var esHoy = false

        val ret = ArrayList<Evento>()
        html = html.replace("\\u003C", "<").replace("&quot;", "\"")
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")
        val sdf2 = SimpleDateFormat("dd/MM/yyyy ")

        val doc = Jsoup.parse(html)
        val elements = doc.getElementsByTag("li")
        for (i in elements.indices) {
            try {
                val ev = Evento()
                val a = elements[i].getElementsByTag("a")[0]
                val href = a.attr("href")
                if (href.endsWith(".html\\\"")) {
                    val d = a.getElementsByTag("i")
                    if (d.size > 0) {
                        var hora = d[0].getElementsByTag("i").html()
                        if (hora == "LIVE") {
                            esHoy = true
                            ev.hora = Date()
                            ev.directo = true
                        } else if (hora != "OFFLINE") {
                            esHoy = true
                            hora = sdf2.format(Date()) + hora
                            ev.hora = sdf.parse(hora)
                            if (ev.hora!!.time < Calendar.getInstance().time.time) {
                                val cal = Calendar.getInstance()
                                cal.time = ev.hora
                                cal.add(Calendar.DAY_OF_YEAR, 1)
                                ev.hora = cal.time
                            }
                            ev.directo = false
                        } else {
                            continue
                        }
                    }
                    val c = a.getElementsByTag("span")[2]
                    ev.competicion = c.html()
                    try {
                        val n = a.getElementsByTag("div")[0]
                        ev.nombre = n.text().replace("\\n", "").trim({ it <= ' ' }).replace(" +".toRegex(), " ")
                    } catch (ex: Exception) {
                        ev.nombre = ""
                    }


                    val icon = a.getElementsByTag("img")[0]
                    val tipo = icon.attr("src").replace(".png\\\"", "")
                    ev.tipo = if (tipo.endsWith("football")) Evento.FUTBOL else if (tipo.endsWith("basketball")) Evento.BALONCESTO else if (tipo.endsWith("tennis")) Evento.TENIS else Evento.DESCONOCIDO
                    ev.fondo = Sys.init().getImagenDeporte(ev.tipo)
                    ev.links = ArrayList()
                    ev.url = "http://livesport.ws" + href.replace("\"", "").replace("\\", "")

                    if ((ev.directo || Sys.init().horaValidaEvento(ev.hora)) && ev.tipo != Evento.DESCONOCIDO) {
                        ret.add(ev)
                    }
                } else {
                    if (esHoy) {
                        break
                    }
                }
            } catch (ex: Exception) {
                val isd = ex.hashCode()
            }

        }





        eventos!!.addAll(ret)
        Sys.init().panel!!.populateGrid(eventos!!)


    }

    inner class CustomComparator : Comparator<Evento> {
        override fun compare(o1: Evento, o2: Evento): Int {
            return o1.hora!!.compareTo(o2.hora)
        }
    }

    override fun getLinks(index: Int) {
        eventoIndex = index
        parseData3()
    }

    private fun parseData3() {

        if (eventos!![eventoIndex].url!!.length > 0) {
            Sys.init().w!!.setWebViewClient(object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    Sys.init().w!!.evaluateJavascript("(function() { return (document.getElementsByTagName('html')[0].innerHTML); })();"
                    ) { html -> parseData4(html) }
                }
            })

            Sys.init().w!!.loadUrl(eventos!![eventoIndex].url)
        } else {
            Sys.init().evento!!.populateLinks(eventos!![eventoIndex])
        }


    }

    private fun parseData4(html: String) {
        var html = html

        html = html.replace("\\u003C", "<")
        val doc = Jsoup.parse(html)
        val trs = doc.getElementsByTag("tr")
        for (tr in trs) {
            try {
                val tds = tr.getElementsByTag("td")
                if (tds[6].getElementsByTag("a")[0].attr("href").contains("acestream:")) {
                    val l = Link()
                    var pais = tds[1].getElementsByTag("img")[0].attr("src").replace(".png\\\"", "")
                    val last = pais.lastIndexOf('/')
                    pais = pais.substring(last+1)
                    pais = pais.toUpperCase()
                    l.idioma = Link.DESCONOCIDO
                    when (pais) {
                        "RUSSIA" -> l.idioma = Link.RU
                        "UKRAINE" -> l.idioma = Link.UCR
                        "LATVIA" -> l.idioma = Link.LET
                        "ITALY" -> l.idioma = Link.ITA
                        "GERMANY" -> l.idioma = Link.GER
                        "SPAIN" -> l.idioma = Link.ESP
                        "PORTUGAL" -> l.idioma = Link.POR
                        "ENGLAND" -> l.idioma = Link.ENG
                        "SWEDEN" -> l.idioma = Link.SUE
                    }
                    l.kbps = Integer.parseInt(tds[2].html().replace(" kbps", ""))
                    l.acestream = tds[6].getElementsByTag("a")[0].attr("href").replace("\\\"", "")
                    val res = tds[5].html()
                    when (res) {
                        "576p" -> l.resolution = Link.SD576
                        "720p" -> l.resolution = Link.HD720
                        "1080p" -> l.resolution = Link.HD1080
                        else -> l.resolution = Link.NORES
                    }
                    val fps = tds[4].html()
                    when (fps) {
                        "25" -> l.fps = Link.FPS25
                        "50" -> l.fps = Link.FPS50
                        else -> l.fps = Link.NOFPS
                    }
                    eventos!![eventoIndex].links!!.add(l)

                }
            } catch (ex: Exception) {

            }

        }
        eventos!![eventoIndex].url = ""
        Sys.init().evento!!.populateLinks(eventos!![eventoIndex])

    }

    companion object {

        private var instance: LivesportWs? = null

        fun init(): LivesportWs {
            if (instance == null)
                instance = LivesportWs()
            return instance!!
        }
    }
}
