package emperatriz.riverflood;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crystal.crystalpreloaders.widgets.CrystalPreloader;

import emperatriz.riverflood.model.Evento;

public class DetalleEvento extends Activity {

    WebView w;
    ProgressDialog dialog;
    int index;
    LinearLayout links;
    GridView grid;
    CrystalPreloader spinkit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evento);

        w = (WebView) findViewById(R.id.web);
        w.getSettings().setJavaScriptEnabled(true);
        Sys.init().w=w;
        Sys.init().evento = this;

        index = getIntent().getIntExtra("index",0);
        int imagenFondo = getIntent().getIntExtra("fondo", R.drawable.des1);
        String competicionText = getIntent().getStringExtra("competicion");
        String horaText = getIntent().getStringExtra("hora");
        String nombreText = getIntent().getStringExtra("nombre");

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);


        window.setStatusBarColor(0xff000000);


        TextView competicion = (TextView) findViewById(R.id.competicion);
        TextView nombre = (TextView) findViewById(R.id.nombre);
        TextView hora = (TextView) findViewById(R.id.hora);
        competicion.setText(competicionText);
        hora.setText(horaText);
        nombre.setText(nombreText);
        Typeface sf = Typeface.createFromAsset(getAssets(), "SF Movie Poster Condensed Bold.ttf");
        Typeface sf2 = Typeface.createFromAsset(getAssets(), "SF Movie Poster.ttf");
        nombre.setTypeface(sf);
        competicion.setTypeface(sf);
        hora.setTypeface(sf2);
        ImageView fondo = (ImageView) findViewById(R.id.imagenDeporte);
        fondo.setImageResource(imagenFondo);


        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenDensity = metrics.densityDpi;

        spinkit = (CrystalPreloader) findViewById(R.id.spin_kit);
        grid = (GridView) findViewById(R.id.grid);
        grid.setNumColumns(getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT?2:screenDensity>350?3:4);
        grid.setEmptyView(spinkit);

        Sys.init().getSelectedGestor(this).getLinks(index);

    }

    public void populateLinks(Evento evento){
        if (evento.links.size()==0){
            Toast.makeText(this,"No hay enlaces disponibles", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            if (grid.getAdapter()==null){
                grid.setAdapter(new LinkAdapter(evento.links, DetalleEvento.this));
            }else{
                ((LinkAdapter)grid.getAdapter()).notifyDataSetChanged();
            }
        }




    }
}
