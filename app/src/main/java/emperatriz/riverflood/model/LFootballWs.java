package emperatriz.riverflood.model;


import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import emperatriz.riverflood.DetalleEvento;
import emperatriz.riverflood.Sys;

import static java.lang.System.in;

public class LFootballWs implements GestorPagina {

    private HashMap avs = new HashMap();
    private ArrayList<Evento> eventos;
    private String textoRuso="";

    @Override
    public String getNombre() {
        return "lfootball.ws";
    }

    @Override
    public int getId() {
        return 3;
    }

    public String getJavascript() {
        return "(function() { return (document.getElementsByTagName('html')[0].innerHTML); })();";
    }

    public String getUrl_() {
        //return "https://translate.google.com/translate?hl=en&sl=auto&tl=en&u=http%3A%2F%2Fwww.lfootball.ws";
        return "http://lfootball.ws";

    }

    private static LFootballWs instance;

    public static LFootballWs init(){
        if (instance==null)
            instance = new LFootballWs();
        return instance;
    }

    private LFootballWs(){}



    public void parseData(){
//        Traduce t = new Traduce();
//        t.execute("МАНЧЕСТЕР СИТИ - ЛЕСТЕР");
        textoRuso="";
        if (eventos!=null && eventos.size()>0){
            Sys.init().panel.populateGrid(eventos);
        }else{

            eventos = new ArrayList<Evento>();

            Sys.init().w.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, final String url) {
                    Sys.init().w.evaluateJavascript(getJavascript(),
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String html) {
                                        parseData2(html);
                                }
                            });
                }
            });
            Sys.init().w.getSettings().setDomStorageEnabled(true);
            Sys.init().w.loadUrl(getUrl_());
        }

    }

    public void parseDataRefresh(){
        textoRuso="";
        eventos = new ArrayList<Evento>();

        Sys.init().w.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Sys.init().w.evaluateJavascript(getJavascript(),
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String html) {
                                    parseData2(html);
                            }
                        });
            }
        });

        Sys.init().w.loadUrl(getUrl_());
    }

    public void parseData2(String html){


        ArrayList<Evento> ret = new ArrayList<Evento>();
        html = html.replace("\\u003C","<").replace("&quot;", "").replace("\\\"","\"");
        SimpleDateFormat sdf  = new SimpleDateFormat("MMM dd HH:mm yyyy zzz");
        SimpleDateFormat sdf2  = new SimpleDateFormat("dd/MM/yyyy ");

        Document doc = Jsoup.parse(html);
        Elements elements = doc.getElementsByTag("li");
        for (int i=0;i<elements.size();i++){
            try{
                Evento ev = new Evento();
                Element li = elements.get(i);
                String n = li.getElementsByTag("a").get(0).attr("title");
                String c = li.getElementsByClass("liga").get(0).text();
                String u = li.getElementsByClass("link").get(0).attr("href");
                u = u.substring(u.indexOf("http:"),u.indexOf(".html")+5);
                Elements he1 = li.getElementsByClass("liveCon");
                Elements he2 = li.getElementsByClass("date");
                String h="";
                if (he1.size()>0){
                    h = he1.get(0).text();
                } else if (he2.size()>0){
                    h = he2.get(0).text();
                }

                if (h.toUpperCase().equals("LIVE")){
                    ev.directo=true;
                    ev.hora = new Date();
                }else{
                    ev.directo=false;
                    ev.hora = sdf.parse(h+" "+Calendar.getInstance().get(Calendar.YEAR)+" +0300");
                }
                ev.nombre = n;
                ev.competicion=c;
                ev.tipo = Evento.FUTBOL;
                ev.links = new ArrayList<Link>();
                ev.fondo = Sys.init().getImagenDeporte(ev.tipo);
                ev.url=u;

                textoRuso+=ev.nombre+". "+ev.competicion+". ";
                eventos.add(ev);

            }catch (Exception ex){

            }
        }


            //Sys.init().panel.populateGrid(eventos);
        if (textoRuso.length()>0){
            textoRuso = textoRuso.substring(0, textoRuso.length()-2);
        }
        Traduce t = new Traduce();
        t.execute(textoRuso);
    }




    int eventoIndex;

    public void getLinks(int index){
        eventoIndex=index;
        parseData3();
    }

    private void parseData3(){

        if ( eventos.get(eventoIndex).url.length()>0){
            Sys.init().w.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    Sys.init().w.evaluateJavascript("(function() { return (document.getElementsByClassName('live-table')[0].innerHTML); })();",
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String html) {
                                    parseData4(html);
                                }
                            });
                }
            });

            Sys.init().w.loadUrl(eventos.get(eventoIndex).url);
        }else{
            Sys.init().evento.populateLinks(eventos.get(eventoIndex));
        }



    }

    private void parseData4(String html){

        html = html.replace("\\u003C","<").replace("&quot;", "").replace("\\\"","\"").replace("\\n","").replace("\\t","");
        Document doc = Jsoup.parse("<table>"+html+"</table>");
        Elements trs = doc.getElementsByTag("tr");
        for(Element tr : trs){
            try{
                Elements tds = tr.getElementsByTag("td");
                if (tds.get(7).getElementsByTag("a").get(0).attr("href").contains("acestream:")){
                    Link l = new Link();
                    String idioma = tds.get(5).html();
                    l.idioma = Link.DESCONOCIDO;
                    switch (idioma){
                        case "Русский":l.idioma = Link.RU;break;
                        case "Украинский":l.idioma = Link.UCR;break;
                        case "Английский":l.idioma = Link.ENG;break;
                        case "Испанский":l.idioma = Link.ESP;break;
                        case "Шведский":l.idioma = Link.SUE;break;
                        case "Итальянский":l.idioma = Link.ITA;break;
                    }
                    l.kbps = Integer.parseInt(tds.get(3).html().replace(" kbps",""));
                    l.acestream = tds.get(7).getElementsByTag("a").get(0).attr("href");
                    l.resolution = Link.NORES;
                    l.fps = Link.NOFPS;

                    eventos.get(eventoIndex).links.add(l);

                }
            }catch (Exception ex){

            }

        }
        eventos.get(eventoIndex).url="";

        Sys.init().evento.populateLinks(eventos.get(eventoIndex));


    }

    class Traduce extends AsyncTask<String, String, String> {


        protected String doInBackground(String... params) {


            try {
                String url2 = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20170513T131304Z.228f2f52265bc677.1465dc14bd5022195b5b316e39dadf3d39eb5b6d&text="+URLEncoder.encode(params[0],"UTF-8")+"&lang=en";
                URL url = new URL(url2);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream is = urlConnection.getInputStream();
                try {
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader bufferedReader = new BufferedReader(isr);
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                return "";
            }
        }

        protected void onPostExecute(String response) {
            try{

                String texto = response.substring(response.indexOf("[")+1,response.indexOf("]")-1);
                texto = texto.replace("[","").replace("]","").replace(".","###").replace("\"","").toUpperCase().replace("SAXONY","BAYERN");
                int i=0;
                String[] textos = texto.split("###");

                for (Evento ev : eventos){
                    ev.nombre = textos[i++].trim();
                    ev.competicion = textos[i++].trim();
                }
            }catch (Exception ex){
            }

            Sys.init().panel.populateGrid(eventos);
        }
    }



}
