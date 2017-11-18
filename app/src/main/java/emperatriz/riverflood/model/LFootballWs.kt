package emperatriz.riverflood.model


import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast


import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Comparator
import java.util.Date
import java.util.HashMap

import emperatriz.riverflood.DetalleEvento
import emperatriz.riverflood.Sys

import java.lang.System.`in`

class LFootballWs private constructor() : GestorPagina {

    private val avs = HashMap<String,String>()
    private var eventos: ArrayList<Evento>? = null
    private var textoRuso = ArrayList<String>()

    override fun updateUrl(url: String) {
        this.url_=url
    }

    override val nombre: String
        get() = "lfootball.ws"

    override val id: Int
        get() = 2

    val javascript: String
        get() = "(function() { return (document.getElementsByTagName('html')[0].innerHTML); })();"

    var url_: String= "http://lfootball.ws"



    internal var eventoIndex: Int = 0


    override fun parseData() {

        textoRuso = ArrayList()
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
            Sys.init().w!!.settings.domStorageEnabled = true
            Sys.init().w!!.loadUrl(url_)
        }

    }

    override fun parseDataRefresh() {
        textoRuso = ArrayList()
        eventos = ArrayList()

        Sys.init().w!!.setWebViewClient(object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                Sys.init().w!!.evaluateJavascript(javascript
                ) { html -> parseData2(html) }
            }
        })

        Sys.init().w!!.loadUrl(url_)
    }

    fun parseData2(html: String) {
        var html = html


        val ret = ArrayList<Evento>()
        html = html.replace("\\u003C", "<").replace("&quot;", "").replace("\\\"", "\"")
        val sdf = SimpleDateFormat("MMM dd HH:mm yyyy zzz")
        val sdf2 = SimpleDateFormat("dd/MM/yyyy ")

        val doc = Jsoup.parse(html)
        val elements = doc.getElementsByTag("li")
        for (i in elements.indices) {
            try {
                val ev = Evento()
                val li = elements[i]
                val n = li.getElementsByTag("a")[0].attr("title")
                val c = li.getElementsByClass("liga")[0].text()
                var u = li.getElementsByClass("link")[0].attr("href")
                u = u.substring(u.indexOf("http:"), u.indexOf(".html") + 5)
                val he1 = li.getElementsByClass("liveCon")
                val he2 = li.getElementsByClass("date")
                var h = ""
                if (he1.size > 0) {
                    h = he1[0].text()
                } else if (he2.size > 0) {
                    h = he2[0].text()
                }

                if (h.toUpperCase() == "LIVE") {
                    ev.directo = true
                    ev.hora = Date()
                } else {
                    ev.directo = false
                    ev.hora = sdf.parse(h + " " + Calendar.getInstance().get(Calendar.YEAR) + " +0300")
                }
                ev.nombre = n
                ev.competicion = c
                ev.tipo = Evento.FUTBOL
                ev.links = ArrayList()
                ev.fondo = Sys.init().getImagenDeporte(ev.tipo)
                ev.url = u

                textoRuso.add(ev.nombre!!)
                textoRuso.add(ev.competicion!!)
                eventos!!.add(ev)

            } catch (ex: Exception) {

            }

        }


        val t = Traduce()
        t.execute(textoRuso)
    }

    override fun getLinks(index: Int) {
        eventoIndex = index
        parseData3()
    }

    private fun parseData3() {

        if (eventos!![eventoIndex].url!!.length > 0) {
            Sys.init().w!!.setWebViewClient(object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    Sys.init().w!!.evaluateJavascript("(function() { return (document.getElementsByClassName('live-table')[0].innerHTML); })();"
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

        html = html.replace("\\u003C", "<").replace("&quot;", "").replace("\\\"", "\"").replace("\\n", "").replace("\\t", "")
        val doc = Jsoup.parse("<table>$html</table>")
        val trs = doc.getElementsByTag("tr")
        for (tr in trs) {
            try {
                val tds = tr.getElementsByTag("td")
                if (tds[7].getElementsByTag("a")[0].attr("href").contains("acestream:")) {
                    val l = Link()
                    val idioma = tds[5].html()
                    l.idioma = Link.DESCONOCIDO
                    when (idioma) {
                        "Русский" -> l.idioma = Link.RU
                        "Украинский" -> l.idioma = Link.UCR
                        "Английский" -> l.idioma = Link.ENG
                        "Испанский" -> l.idioma = Link.ESP
                        "Шведский" -> l.idioma = Link.SUE
                        "Итальянский" -> l.idioma = Link.ITA
                        "Португальский" -> l.idioma = Link.POR
                    }
                    l.kbps = Integer.parseInt(tds[3].html().replace(" kbps", ""))
                    l.acestream = tds[7].getElementsByTag("a")[0].attr("href")
                    l.resolution = Link.NORES
                    l.fps = Link.NOFPS

                    eventos!![eventoIndex].links!!.add(l)

                }
            } catch (ex: Exception) {

            }

        }
        eventos!![eventoIndex].url = ""

        Sys.init().evento!!.populateLinks(eventos!![eventoIndex])


    }

    internal inner class Traduce : AsyncTask<ArrayList<String>, ArrayList<String>, String>() {


        override fun doInBackground(vararg params: ArrayList<String>): String {

            if (params[0].size == 0) {
                return ""
            }
            try {
                val url2 = "https://translate.yandex.net/api/v1.5/tr.json/translate"
                var parameters = "key=trnsl.1.1.20170513T131304Z.228f2f52265bc677.1465dc14bd5022195b5b316e39dadf3d39eb5b6d&lang=en"
                for (text in params[0]) {
                    parameters += "&text=" + URLEncoder.encode(text, "UTF-8")
                }
                val url = URL(url2)
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.doOutput = true
                urlConnection.requestMethod = "POST" // hear you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8") // here you are setting the `Content-Type` for the data you are sending which is `application/json`
                urlConnection.setRequestProperty("charset", "utf-8")
                urlConnection.setRequestProperty("Content-Length", Integer.toString(parameters.length))
                urlConnection.connect()


                try {
                    val wr = DataOutputStream(urlConnection.outputStream)
                    wr.writeBytes(parameters)
                    wr.flush()
                    wr.close()

                    val ins = BufferedReader(InputStreamReader(urlConnection.inputStream, "UTF-8"))
                    val sb = StringBuilder()
                    var c: Int
                    c = ins.read()
                    while (c >= 0){
                        sb.append(c.toChar())
                        c = ins.read()
                    }
                    return sb.toString()
                } finally {
                    urlConnection.disconnect()
                }
            } catch (e: Exception) {
                return ""
            }


        }

        override fun onPostExecute(response: String) {
            var response = response
            try {

                response = response.toUpperCase().replace("SAXONY", "BAYERN")
                val json = JSONObject(response)
                val jArray = json.getJSONArray("TEXT")

                var i = 0
                for (ev in eventos!!) {
                    ev.nombre = jArray.get(i++).toString().trim { it <= ' ' }
                    ev.competicion = jArray.get(i++).toString().trim { it <= ' ' }
                }
            } catch (ex: Exception) {

            }

            Sys.init().panel!!.populateGrid(eventos!!)
        }
    }

    companion object {

        private var instance: LFootballWs? = null

        fun init(): LFootballWs {
            if (instance == null)
                instance = LFootballWs()
            return instance!!
        }
    }


}
