package emperatriz.riverflood;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.view.Window;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import emperatriz.riverflood.model.ArenavisionIn;
import emperatriz.riverflood.model.ArenavisionRu;
import emperatriz.riverflood.model.Evento;
import emperatriz.riverflood.model.GestorPagina;
import emperatriz.riverflood.model.Link;
import emperatriz.riverflood.model.LivesportWs;

public class Sys {

    public WebView w;
    public Panel panel;
    public DetalleEvento evento;
    private static Sys instance;

    private ArrayList<GestorPagina> gestores = new ArrayList<GestorPagina>();

    private ArrayList<Integer> futbol;
    private ArrayList<Integer> baloncesto;
    private ArrayList<Integer> ciclismo;
    private ArrayList<Integer> motogp;
    private ArrayList<Integer> f1;
    private ArrayList<Integer> tenis;
    private ArrayList<Integer> boxeo;
    private ArrayList<Integer> atletismo;
    private ArrayList<Integer> desconocido;

    private int indexF,indexB,indexC,indexM, indexF2, indexB2, indexD, indexA, indexT;
    public static Sys init(){
        if (instance==null)
            instance = new Sys();
        return instance;
    }

    private Sys(){
        futbol = new ArrayList<Integer>(Arrays.asList(R.drawable.fut1, R.drawable.fut2, R.drawable.fut3, R.drawable.fut4, R.drawable.fut5, R.drawable.fut6, R.drawable.fut7));
        baloncesto = new ArrayList<Integer>(Arrays.asList(R.drawable.bal1, R.drawable.bal2, R.drawable.bal3));
        ciclismo = new ArrayList<Integer>(Arrays.asList(R.drawable.cic1, R.drawable.cic2, R.drawable.cic3));
        motogp = new ArrayList<Integer>(Arrays.asList(R.drawable.mot1, R.drawable.mot2, R.drawable.mot3));
        f1 = new ArrayList<Integer>(Arrays.asList(R.drawable.for1, R.drawable.for2, R.drawable.for3));
        tenis = new ArrayList<Integer>(Arrays.asList(R.drawable.ten1, R.drawable.ten2, R.drawable.ten3));
        boxeo = new ArrayList<Integer>(Arrays.asList(R.drawable.box1, R.drawable.box2));
        atletismo = new ArrayList<Integer>(Arrays.asList(R.drawable.atl1, R.drawable.atl2, R.drawable.atl3));
        desconocido = new ArrayList<Integer>(Arrays.asList(R.drawable.des1, R.drawable.des2));
        indexF=0;indexB=0;indexC=0;indexM=0; indexF2=0; indexB2=0; indexD=0;indexA=0;indexT=0;
        Collections.shuffle(futbol);
        Collections.shuffle(baloncesto);
        Collections.shuffle(ciclismo);
        Collections.shuffle(motogp);
        Collections.shuffle(f1);
        Collections.shuffle(tenis);
        Collections.shuffle(boxeo);
        Collections.shuffle(atletismo);
        Collections.shuffle(desconocido);

        gestores.add(ArenavisionIn.init());
        gestores.add(ArenavisionRu.init());
        gestores.add(LivesportWs.init());
    }

    public GestorPagina getSelectedGestor(Context context){
        int indexGestores = getPreferencia("indexGestores",0,context);
        return gestores.get(indexGestores);
    }



    public ArrayList<String> getGestores(){
        ArrayList<String> ret = new ArrayList<String>();
        for(GestorPagina g : gestores){
            ret.add(g.getNombre());
        }
        return ret;
    }


    public void selectGestorId(int id, Context context){
         guardaPreferencia("indexGestores",id,context);
    }

    public boolean horaValidaEvento(Date d){
        Calendar limite = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
        limite.add(Calendar.HOUR,6);
        long lim = limite.getTimeInMillis();
        limite.add(Calendar.HOUR,-10);
        long lim2 = limite.getTimeInMillis();;
        long dl = d.getTime();
        return lim>=dl && lim2<dl;
    }

    public static String limpia(String st){
        return st.replace("&mbsp;","").replace("<br />","<br>").replace("<br/>","<br>").replace("\\n","").replace("\\t","").replace("<br>"," ").replace("&Ntilde;","ñ").replace("&acute;","\'");
    }

    public int getImagenDeporte(int tipo){
        switch (tipo){
            case Evento.FUTBOL:{
                indexF++;
                if (indexF>=futbol.size()) indexF=0;
                return futbol.get(indexF);
            }
            case Evento.BALONCESTO:{
                indexB++;
                if (indexB>=baloncesto.size()) indexB=0;
                return baloncesto.get(indexB);
            }
            case Evento.BOXEO:{
                indexB2++;
                if (indexB2>=boxeo.size()) indexB2=0;
                return boxeo.get(indexB2);
            }
            case Evento.CICLISMO:{
                indexC++;
                if (indexC>=ciclismo.size()) indexC=0;
                return ciclismo.get(indexC);
            }
            case Evento.DESCONOCIDO:{
                indexD++;
                if (indexD>=desconocido.size()) indexD=0;
                return desconocido.get(indexD);
            }
            case Evento.F1:{
                indexF2++;
                if (indexF2>=f1.size()) indexF2=0;
                return f1.get(indexF2);
            }
            case Evento.MOTOS:{
                indexM++;
                if (indexM>=motogp.size()) indexM=0;
                return motogp.get(indexM);
            }
            case Evento.ATLETISMO:{
                indexA++;
                if (indexA>=atletismo.size()) indexA=0;
                return atletismo.get(indexA);
            }
            case Evento.TENIS:{
                indexT++;
                if (indexT>=tenis.size()) indexT=0;
                return tenis.get(indexT);
            }
        }
        return desconocido.get(0);
    }

    public int getImagenFlag(int tipo){
        switch (tipo){
            case Link.ENG:{
                return R.drawable.ing;
            }
            case Link.ESP:{
                return R.drawable.esp;
            }
            case Link.GER:{
                return R.drawable.ale;
            }
            case Link.ITA:{
                return R.drawable.ita;
            }
            case Link.POR:{
                return R.drawable.por;
            }
            case Link.RU:{
                return R.drawable.rus;
            }
            case Link.CRO:{
                return R.drawable.cro;
            }
            case Link.SUE:{
                return R.drawable.sue;
            }
            case Link.LET:{
                return R.drawable.let;
            }
            case Link.UCR:{
                return R.drawable.ucr;
            }

        }
        return R.drawable.des;
    }

    public static void openAppInPlayStore(String packageName, Context context) {
        Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
        boolean marketFound = false;

        // find all applications able to handle our rateIntent
        final List<ResolveInfo> otherApps = context.getPackageManager().queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp: otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName.equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );
                rateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                rateIntent.setComponent(componentName);
                context.startActivity(rateIntent);
                marketFound = true;
                break;

            }
        }

        // Si no está instalado el play store, se ejecuta desde el navegador
        if (!marketFound) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+packageName));
            context.startActivity(webIntent);
        }
    }

    private Dialog dialog;

    public void cargando(Context context){
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.waitdialog);
        TextView nombre = (TextView) dialog.findViewById(R.id.nombre);
        Typeface sf = Typeface.createFromAsset(context.getAssets(), "SF Movie Poster Condensed Bold.ttf");
        nombre.setTypeface(sf);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.show();
    }

    public boolean estaCargando(){
        return dialog!=null && dialog.isShowing();
    }

    public void cargado(){
        if (dialog!=null && dialog.isShowing()) dialog.dismiss();
    }

    public void guardaPreferencia(String key, int value, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key,value);
        editor.apply();
    }

    public int getPreferencia(String key, int defValue, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(key, defValue);
    }

    public boolean checkNewVersion(Context context){
        try {
            // Create a URL for the desired page
            URL url = new URL("https://drive.google.com/uc?export=download&id=0BxODOfYE_PuDWnhkRHRaTWN3T2M");

            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String v = in.readLine();
            in.close();
            String versionName = context.getPackageManager() .getPackageInfo(context.getPackageName(), 0).versionName;
            return !versionName.equals(v);
        } catch (Exception e) {
            return false;
        }
    }

    // Storage Permissions
    public  static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public static void sendAppItself(Activity paramActivity) throws IOException {
        PackageManager pm = paramActivity.getPackageManager();
        ApplicationInfo appInfo;
        try {
            appInfo = pm.getApplicationInfo(paramActivity.getPackageName(),
                    PackageManager.GET_META_DATA);
            Intent sendBt = new Intent(Intent.ACTION_SEND);
            sendBt.setType("*/*");
            sendBt.putExtra(Intent.EXTRA_STREAM,
                    Uri.parse("file://" + appInfo.publicSourceDir));

            paramActivity.startActivity(Intent.createChooser(sendBt,
                    "Comparte MasterstreaM enviándola a través de:"));
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    public static void sendAppItselfText(Activity paramActivity) throws IOException {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Descarga MasterstreaM desde aquí: http://goo.gl/59McZu");
        sendIntent.setType("text/plain");
        paramActivity.startActivity(sendIntent);
    }

}
