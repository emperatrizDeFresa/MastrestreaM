package emperatriz.riverflood;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import emperatriz.riverflood.model.Evento;

public class Panel extends Activity {

    WebView w;
    ProgressDialog dialog;
    LinearLayout animacion, noEvents;
    TextView noEvents1, noEvents2;
    GridView grid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panel);
        w = (WebView) findViewById(R.id.web);
        w.getSettings().setJavaScriptEnabled(true);
        Sys.init().w=w;
        Sys.init().panel = this;
        getActionBar().setTitle(null);
        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setBackgroundDrawable(new ColorDrawable(0xff000000));
//        getActionBar().setDisplayShowTitleEnabled(false);
//        getActionBar().setDisplayShowTitleEnabled(true);
        Spinner webs = new Spinner(this);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Sys.init().getGestores());
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        webs.setAdapter(spinnerArrayAdapter);
        webs.setSelection(Sys.init().getSelectedGestor(Panel.this).getId());
        getActionBar().setCustomView(webs);
        webs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int gid = Sys.init().getSelectedGestor(Panel.this).getId();
                Sys.init().selectGestorId(position,Panel.this);

                if (gid!=position) {

                    grid.removeAllViewsInLayout();

                    Sys.init().cargando(Panel.this);
                    Sys.init().getSelectedGestor(Panel.this).parseData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        noEvents = (LinearLayout)findViewById(R.id.noEvents);
        noEvents1 = (TextView)findViewById(R.id.noEvents1);
        noEvents2 = (TextView)findViewById(R.id.noEvents2);


        TextView nombre = (TextView) findViewById(R.id.nombre);
        Typeface sf = Typeface.createFromAsset(getAssets(), "SF Movie Poster Condensed Bold.ttf");
        nombre.setTypeface(sf);

        grid = (GridView) findViewById(R.id.grid);
        grid.setNumColumns(getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT?1:2);
        grid.setEmptyView(noEvents);



        animacion = (LinearLayout) findViewById(R.id.waitAnimation);

        Sys.init().cargando(Panel.this);
        Sys.init().getSelectedGestor(Panel.this).parseData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.panel, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                AlertDialog ad = new AlertDialog.Builder(new ContextThemeWrapper(Panel.this, android.R.style.Theme_Material_Light_Dialog)).create();

                ad.setMessage("¿Cómo quieres compartir la app?");
                ad.setCancelable(true);
                ad.setButton("Enviar archivo", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            Sys.sendAppItself(Panel.this);
                        }catch (Exception ex) {
                            Toast.makeText(Panel.this, "No se ha podido compartir ¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show();
                        }

                        return;
                    }
                });
                ad.setButton2("Enviar enlace", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            Sys.sendAppItselfText(Panel.this);
                        }catch (Exception ex) {
                            Toast.makeText(Panel.this, "No se ha podido compartir ¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show();
                        }

                        return;
                    }
                });
                ad.setButton3("Usando telepatía", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Toast.makeText(Panel.this, "( ͡° ͜ʖ ͡°)", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });
                ad.show();

                break;
            case R.id.action_refresh:
                grid.removeAllViewsInLayout();

                Sys.init().cargando(Panel.this);
                Sys.init().getSelectedGestor(Panel.this).parseDataRefresh();
                break;

        }
        return true;
    }


    public void populateGrid(final ArrayList<Evento> eventos){

        final TranslateAnimation anim = new TranslateAnimation(0,0,1000,0);
        anim.setDuration(400);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                noEvents1.setTextColor(eventos.size()>0?0xff000000:0xffffffff);
                noEvents2.setTextColor(eventos.size()>0?0xff000000:0xffffffff);

            }
        });




        grid.setAdapter(new EventoAdapter(eventos, Panel.this));
        grid.startAnimation(anim);
        Sys.init().cargado();
        Sys.verifyStoragePermissions(Panel.this);
        CheckVersion cv = new CheckVersion();
        cv.execute();

    }

    ProgressDialog pd;
    private class CheckVersion extends AsyncTask<String, Float, Boolean> {


        protected Boolean doInBackground(String... urls) {

            return Sys.init().checkNewVersion(Panel.this);

        }


        protected void onPostExecute(Boolean result) {
            if (result){
                AlertDialog ad = new AlertDialog.Builder(new ContextThemeWrapper(Panel.this, android.R.style.Theme_Material_Light_Dialog)).create();
                ad.setTitle("Actualización disponible");
                ad.setMessage("Hay una nueva versión que introduce nuevos fallos aún más catastróficos y desesperantes.\n\n ¿Quieres actualizar?");
                ad.setCancelable(true);
                ad.setButton("Por supuesto", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialogActualizar();

                        return;
                    }
                });
                ad.setButton2("Hemos venido a jugar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialogActualizar();

                        return;
                    }
                });
                ad.setButton3("Malo será", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialogActualizar();

                        return;
                    }
                });
                ad.show();

            }
        }
    }

    private void dialogActualizar(){
        pd = new ProgressDialog(new ContextThemeWrapper(Panel.this, android.R.style.Theme_Material_Light_Dialog));
        pd.setIndeterminate(true);
        pd.setProgressNumberFormat(null);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setCancelable(false);
        //pd.setMax(100);
        pd.setMessage("Descargando actualización");
        pd.show();

        Download down = new Download();
        down.execute("https://drive.google.com/uc?export=download&id=0BxODOfYE_PuDaFJidXlpMWpLOVE");
    }


    private class Download extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            if (downloadApk(params[0])) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                // intent.setDataAndType(Uri.fromFile(new
                // File(Sys.init().ctx.getFilesDir() + "/", "app.apk")),
                // "application/vnd.android.package-archive");
                intent.setDataAndType(Uri.fromFile(new File(PATH, "masterstream.apk")), "application/vnd.android.package-archive");
                startActivity(intent);
                return "OK";

            } else {
                return "NO";

            }

        }

        @Override
        protected void onPostExecute(String ok) {

            pd.dismiss();

            if (ok.equals("NO")){
                Toast.makeText(Panel.this, "No se ha descargado la actualización", Toast.LENGTH_SHORT).show();
            }
            else{
//                System.exit(0);
//                finish();
            }


        }

        private boolean downloadApk(String path) {
            boolean ret = false;
            String outputFileName = "masterstream.apk";
            try {
                // connecting to url
                URL u = new URL(path);
                HttpURLConnection c = (HttpURLConnection) u.openConnection();
                //c.setRequestMethod("GET");
                // c.setDoOutput(true);
                c.connect();
                int fileLength = c.getContentLength();
                String PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
                File file = new File(PATH);
                boolean creado = file.mkdirs();
                File outputFile = new File(file, outputFileName);


                // file input is from the url
                InputStream in = c.getInputStream();
                FileOutputStream fos = new FileOutputStream(outputFile);


                // here's the download code
                byte[] buffer = new byte[1024];
                int len1 = 0;
                long total = 0;
                int count;

                while ((len1 = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                    try{
                        total += len1;

                        //Sys.init().log("Progress: "+((int) (total * 100 / fileLength)));

                        //pd.setProgress((int) (total * 100 / fileLength));
                    }catch (Exception ex){}
                }
                fos.close();
                in.close();
                ret = true;
            } catch (Exception ex) {
                ret = false;
            }
            return ret;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Sys.REQUEST_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {



                } else {
                    Toast.makeText(Panel.this, "Sin los permisos necesarios la app no puede funcionar", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }


        }
    }

}
