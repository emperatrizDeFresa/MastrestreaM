package emperatriz.riverflood.model;


import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import emperatriz.riverflood.Sys;

public class LivesportWs2 implements GestorPagina {

    private HashMap avs = new HashMap();
    private ArrayList<Evento> eventos;
    private int urlIndex=0;
    private boolean refresh;
    private String[] urls = new String[]{"http://livesport.ws/en/live-football","http://livesport.ws/en/basketball","http://livesport.ws/en/tennis"};

    @Override
    public String getNombre() {
        return "livesport.ws";
    }

    @Override
    public int getId() {
        return 3;
    }

    public String getJavascript() {
        return "(function() { return ('<html>'+document.getElementsByClassName('drop-list-holder')[0].innerHTML+'</html>'); })();";
    }

    public String getUrl() {

        return urls[urlIndex++];
    }

    private static LivesportWs2 instance;

    public static LivesportWs2 init(){
        if (instance==null)
            instance = new LivesportWs2();
        return instance;
    }

    private LivesportWs2(){}


    private void parseDataInternal(){


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

            Sys.init().w.loadUrl(getUrl());


    }

    public void parseData(){
        refresh=false;
        if (eventos!=null && eventos.size()>0){
            Sys.init().panel.populateGrid(eventos);
        }else{
            if (urlIndex==0){
                eventos = new ArrayList<Evento>();
            }
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

            Sys.init().w.loadUrl(getUrl());
        }

    }

    public void parseDataRefresh(){
        refresh=true;
        if (urlIndex==0){
            eventos = new ArrayList<Evento>();
        }
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

        Sys.init().w.loadUrl(getUrl());
    }

    public void parseData2(String html){
        boolean esHoy= false;

        ArrayList<Evento> ret = new ArrayList<Evento>();
        html = html.replace("\\u003C","<").replace("&quot;", "\"");
        SimpleDateFormat sdf  = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        SimpleDateFormat sdf2  = new SimpleDateFormat("dd/MM/yyyy ");

        Document doc = Jsoup.parse(html);
        Elements elements = doc.getElementsByTag("li");
        for (int i=0;i<elements.size();i++){
            try{
                Evento ev= new Evento();
                Element a = elements.get(i).getElementsByTag("a").get(0);
                String href = a.attr("href");
                if (href.endsWith(".html\\\"")){
                    Elements d = a.getElementsByTag("i");
                    if (d.size()>0){
                        String hora = d.get(0).getElementsByTag("i").html();
                        if (hora.equals("LIVE")){
                            esHoy=true;
                            ev.hora = new Date();
                            ev.directo=true;
                        }
                        else if (!hora.equals("OFFLINE")){
                            esHoy=true;
                            hora = sdf2.format(new Date())+hora;
                            ev.hora = sdf.parse(hora);
                            if (ev.hora.getTime()< Calendar.getInstance().getTime().getTime()){
                                Calendar cal  = Calendar.getInstance();
                                cal.setTime(ev.hora);
                                cal.add(Calendar.DAY_OF_YEAR,1);
                                ev.hora = cal.getTime();
                            }
                            ev.directo=false;
                        }else{
                            continue;
                        }
                    }
                    Element c = a.getElementsByTag("span").get(2);
                    ev.competicion=c.html();
                    Element n = a.getElementsByTag("div").get(0);
                    ev.nombre=n.text().replace("\\n","").trim().replaceAll(" +", " ");
                    ev.tipo = urlIndex==1?Evento.FUTBOL:urlIndex==2?Evento.BALONCESTO:urlIndex==3?Evento.TENIS:Evento.DESCONOCIDO;
                    ev.fondo = Sys.init().getImagenDeporte(ev.tipo);
                    ev.links = new ArrayList<Link>();
                    ev.url = "http://livesport.ws"+href.replace("\"","").replace("\\","");

                    if (ev.directo||Sys.init().horaValidaEvento(ev.hora)) {
                        ret.add(ev);
                    }
                }else{
                    if (esHoy){
                        break;
                    }
                }
            }catch (Exception ex){
                int isd=ex.hashCode();
            }
        }





        eventos.addAll(ret);
        if (urlIndex<urls.length){
            if (refresh){
                parseDataRefresh();
            }else{
                parseDataInternal();
            }
        }else{
            urlIndex=0;
            Collections.sort(eventos, new CustomComparator());
            Sys.init().panel.populateGrid(eventos);
        }

    }

    public class CustomComparator implements Comparator<Evento> {
        @Override
        public int compare(Evento o1, Evento o2) {
            return o1.hora.compareTo(o2.hora);
        }
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
                    Sys.init().w.evaluateJavascript("(function() { return (document.getElementsByTagName('html')[0].innerHTML); })();",
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

        html = html.replace("\\u003C","<");
        Document doc = Jsoup.parse(html);
        Elements trs = doc.getElementsByTag("tr");
        for(Element tr : trs){
            try{
                Elements tds = tr.getElementsByTag("td");
                if (tds.get(6).getElementsByTag("a").get(0).attr("href").contains("acestream:")){
                    Link l = new Link();
                    String pais = tds.get(1).getElementsByTag("img").get(0).attr("src").replace("\\\"/images/logo/country/","").replace(".png\\\"","");
                    pais = pais.toUpperCase();
                    l.idioma = Link.DESCONOCIDO;
                    switch (pais){
                        case "RUSSIA":l.idioma = Link.RU;break;
                        case "UKRAINE":l.idioma = Link.UCR;break;
                        case "LATVIA":l.idioma = Link.LET;break;
                        case "ITALY":l.idioma = Link.ITA;break;
                        case "GERMANY":l.idioma = Link.GER;break;
                        case "SPAIN":l.idioma = Link.ESP;break;
                        case "PORTUGAL":l.idioma = Link.POR;break;
                        case "ENGLAND":l.idioma = Link.ENG;break;
                        case "SWEDEN":l.idioma = Link.SUE;break;
                    }
                    l.kbps = Integer.parseInt(tds.get(2).html().replace(" kbps",""));
                    l.acestream = tds.get(6).getElementsByTag("a").get(0).attr("href").replace("\\\"","");
                    String res = tds.get(5).html();
                    switch (res){
                        case "576p":l.resolution = Link.SD576;break;
                        case "720p":l.resolution = Link.HD720;break;
                        case "1080p":l.resolution = Link.HD1080;break;
                        default:l.resolution = Link.NORES;break;
                    }
                    String fps = tds.get(4).html();
                    switch (fps){
                        case "25":l.fps = Link.FPS25;break;
                        case "50":l.fps = Link.FPS50;break;
                        default:l.fps = Link.NOFPS;break;
                    }
                    eventos.get(eventoIndex).links.add(l);

                }
            }catch (Exception ex){

            }

        }
        eventos.get(eventoIndex).url="";
        Sys.init().evento.populateLinks(eventos.get(eventoIndex));

    }
}
