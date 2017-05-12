package emperatriz.riverflood;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import emperatriz.riverflood.model.Evento;

/**
 * Created by ramon on 01/05/2017.
 */

public class EventoAdapter extends BaseAdapter {

    private ArrayList<Evento> eventos;
    private Context context;

    public EventoAdapter(ArrayList<Evento> eventos, Context context){
        this.eventos=eventos;
        this.context=context;
    }

    @Override
    public int getCount() {
        return eventos.size();
    }

    @Override
    public Object getItem(int position) {
        return eventos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.evento, parent, false);
        }

        final TextView competicion = (TextView) view.findViewById(R.id.competicion);
        final TextView nombre = (TextView) view.findViewById(R.id.nombre);
        final TextView hora = (TextView) view.findViewById(R.id.hora);
        final LinearLayout gradiente = (LinearLayout) view.findViewById(R.id.gradiente);
        final LinearLayout global = (LinearLayout) view.findViewById(R.id.global);

        competicion.setText(eventos.get(position).competicion.toUpperCase());
        hora.setText(eventos.get(position).directo?"AHORA":sdf.format(eventos.get(position).hora));
        nombre.setText(eventos.get(position).nombre.toUpperCase());
        Typeface sf = Typeface.createFromAsset(context.getAssets(), "SF Movie Poster Condensed Bold.ttf");
        Typeface sf2 = Typeface.createFromAsset(context.getAssets(), "SF Movie Poster.ttf");
        nombre.setTypeface(sf);
        competicion.setTypeface(sf);
        hora.setTypeface(sf2);
        final ImageView fondo = (ImageView) view.findViewById(R.id.imagenDeporte);
        final int imagenFondo = eventos.get(position).fondo;
        //fondo.setImageResource(imagenFondo);
        Glide.with(context)
                .load(imagenFondo)
                .into(fondo);
        view.setClickable(true);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, DetalleEvento.class);
                i.putExtra("index", position);
                i.putExtra("fondo", imagenFondo);
                i.putExtra("competicion",eventos.get(position).competicion.toUpperCase());
                i.putExtra("nombre",eventos.get(position).nombre.toUpperCase());
                i.putExtra("hora",eventos.get(position).directo?"AHORA":sdf.format(eventos.get(position).hora));
                Pair<View, String> p1 = Pair.create((View)competicion, "competiciont");
                Pair<View, String> p2 = Pair.create((View)hora, "horat");
                Pair<View, String> p3 = Pair.create((View)nombre, "nombret");
                Pair<View, String> p4 = Pair.create((View)fondo, "fondot");
                Pair<View, String> p5 = Pair.create((View)gradiente, "gradientet");
                Pair<View, String> p6 = Pair.create((View)global, "globalt");

                if (context.getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT){
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity)context, p4,p6);
                    context.startActivity(i, options.toBundle());
                }else{
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity)context, p4,p5);
                    context.startActivity(i, options.toBundle());
                }





            }
        });





        return view;
    }
}
