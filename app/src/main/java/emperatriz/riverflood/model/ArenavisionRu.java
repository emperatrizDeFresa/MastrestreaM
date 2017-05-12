package emperatriz.riverflood.model;


import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import emperatriz.riverflood.Sys;

public class ArenavisionRu implements GestorPagina {

    private HashMap avs = new HashMap();
    private Queue<String> cola = new LinkedList<String>();
    private ArrayList<Evento> eventos;
    private String scheduleUrl="";

    @Override
    public String getNombre() {
        return "arenavision.ru";
    }

    @Override
    public int getId() {
        return 1;
    }

    public String getJavascript() {
        return "(function() { return ('<html><table>'+document.getElementsByTagName('table')[0].innerHTML+'</table></html>'); })();";
    }

    public String getUrl() {
        return "http://www.arenavision.ru";
    }

    private static ArenavisionRu instance;

    public static ArenavisionRu init(){
        if (instance==null)
            instance = new ArenavisionRu();
        return instance;
    }

    private ArenavisionRu(){}


    public void parseData(){
        if (eventos!=null && eventos.size()>0){
            Sys.init().panel.populateGrid(eventos);
        }else{
            if (scheduleUrl.length()==0){
                Sys.init().w.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        Sys.init().w.evaluateJavascript("(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String html) {
                                        parseDataMain(html);
                                    }
                                });
                    }
                });

                Sys.init().w.loadUrl(getUrl());
            }else{
                Sys.init().w.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        Sys.init().w.evaluateJavascript(getJavascript(),
                                new ValueCallback<String>() {
                                    @Override
                                    public void onReceiveValue(String html) {
                                        parseDataSchedule(html);
                                    }
                                });
                    }
                });

                Sys.init().w.loadUrl(scheduleUrl);
            }

        }
    }

    public void parseDataRefresh(){

            Sys.init().w.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    Sys.init().w.evaluateJavascript("(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();",
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String html) {
                                    parseDataMain(html);
                                }
                            });
                }
            });

            Sys.init().w.loadUrl(getUrl());

    }

    public void parseDataMain(String html){
        try{
            html = html.replace("\\u003C","<");
            Document doc = Jsoup.parse(html);
            Elements elements = doc.getElementsByTag("nav");
            String url =  elements.get(0).getElementsByTag("li").get(1).getElementsByTag("a").get(0).attr("href");
            scheduleUrl = getUrl()+url.replace("\\\"","");
        }catch (Exception ex){
            scheduleUrl="http://www.arenavision.ru/-schedule-";
        }
        parseData();
    }

    public void parseDataSchedule(String html){
        ArrayList<Evento> ret = new ArrayList<Evento>();
        html = html.replace("\\u003C","<");
        SimpleDateFormat sdf  = new SimpleDateFormat("dd/MM/yyyy HH:mm zzz");

        Document doc = Jsoup.parse(html);
        Elements elements = doc.getElementsByTag("td");
        for (int i=0;i<elements.size();i=i+6){
            try{
                String d = elements.get(i).html().replace("&nbsp;","").replace("<br />","<br>").replace("<br/>","<br>").replace("\\n","").replace("\\t","");
                String t = Sys.limpia(elements.get(i+1).html().replace("CEST","+0200").replace("CET","+0100"));
                String s = Sys.limpia(elements.get(i+2).html());
                String c = Sys.limpia(elements.get(i+3).html());
                String n = Sys.limpia(elements.get(i+4).html());
                String l = elements.get(i+5).html().replace("&mbsp;","").replace("<br />","<br>").replace("<br/>","<br>").replace("\\n","").replace("\\t","");

                Evento ev = new Evento();
                ev.hora = sdf.parse(d+" "+t);
                ev.directo=false;
                ev.nombre = n;
                ev.competicion = c.replace("SPANISH LA LIGA 2","SEGUNDA DIVISIÓN").replace("SPANISH LA LIGA","PRIMERA DIVISIÓN");
                ev.tipo = Evento.DESCONOCIDO;
                switch (s){
                    case "SOCCER":ev.tipo = Evento.FUTBOL;break;
                    case "BASKETBALL":ev.tipo = Evento.BALONCESTO;break;
                    case "TENNIS":ev.tipo = Evento.TENIS;break;
                    case "FORMULA 1":ev.tipo = Evento.F1;break;
                    case "MOTOGP":ev.tipo = Evento.MOTOS;break;
                    case "CYCLING":ev.tipo = Evento.CICLISMO;break;
                    case "BOXING":ev.tipo = Evento.BOXEO;break;
                    case "ATHLETICS":ev.tipo = Evento.ATLETISMO;break;
                }
                ev.fondo = Sys.init().getImagenDeporte(ev.tipo);
                ev.links = new ArrayList<Link>();
                for (String s1 : l.split("<br>")) {
                    for (String s2 : s1.split("\\[")[0].trim().split("-")) {
                        Link ln = new Link();
                        ln.url = s2;
//                        if (!cola.contains(s2) && Sys.init().horaValidaEvento(ev.hora)){
//                            cola.add(s2);
//                        }
                        ln.kbps = 2350;
                        ln.idioma = Link.DESCONOCIDO;
                        switch (s1.replace("]","").split("\\[")[1].trim()){
                            case "SPA":ln.idioma = Link.ESP;break;
                            case "ENG":ln.idioma = Link.ENG;break;
                            case "POR":ln.idioma = Link.POR;break;
                            case "ITA":ln.idioma = Link.ITA;break;
                            case "GER":ln.idioma = Link.GER;break;
                        }
                        ev.links.add(ln);
                    }

                }

                if (Sys.init().horaValidaEvento(ev.hora)) {
                    ret.add(ev);
                }
            }catch (Exception ex){
                int isd=ex.hashCode();
            }
        }
        eventos = ret;
        Sys.init().panel.populateGrid(eventos);
    }

    int eventoIndex;

    public void getLinks(int index){
        eventoIndex=index;
        Sys.init().evento.populateLinks(eventos.get(eventoIndex));
        cola = new LinkedList<String>();
        for (Link l : eventos.get(index).links){
            if (!cola.contains(l.url) && l.acestream==null){
                cola.add(l.url);
            }
        }
        parseData3();
    }

    private void parseData3(){

        if (cola.size()>0){
            Sys.init().w.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    Sys.init().w.evaluateJavascript("(function() { return (document.getElementsByTagName('html')[0].innerHTML); })();",
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String html) {
                                    parseData4(cola.poll(), html);
                                }
                            });
                }
            });

            Sys.init().w.loadUrl("http://www.arenavision.ru/av"+cola.peek());
        }else{
            for (Evento evento : eventos){
                for(Link l : evento.links){
                    try{l.acestream = avs.get(l.url).toString();}catch (Exception ex){}
                }
            }
            Sys.init().evento.populateLinks(eventos.get(eventoIndex));

        }

    }

    private void parseData4(String av, String html){
        try{
            html = html.replace("\\u003C","<");

            int start = html.indexOf("acestream:");
            int end = html.indexOf("\"",start+"acestream:".length());
            String ac = html.substring(start,end-1);

            avs.put(av,ac);
            for (Evento evento : eventos){
                for(Link l : evento.links){
                    try{l.acestream = avs.get(l.url).toString();}catch (Exception ex){}
                }
            }
            Sys.init().evento.populateLinks(eventos.get(eventoIndex));
        }catch (Exception ex){
            avs.put(av,"");
        }

        parseData3();
    }
}
