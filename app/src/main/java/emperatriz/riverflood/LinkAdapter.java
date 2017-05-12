package emperatriz.riverflood;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import emperatriz.riverflood.model.Link;

/**
 * Created by ramon on 01/05/2017.
 */

public class LinkAdapter extends BaseAdapter {

    private ArrayList<Link> links;
    private Context context;

    public LinkAdapter(ArrayList<Link> links, Context context){
        this.links=links;
        this.context=context;
    }

    @Override
    public int getCount() {
        return links.size();
    }

    @Override
    public Object getItem(int position) {
        return links.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        if (links.get(position).acestream!=null && links.get(position).acestream.length()>0){
            if (true||view == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.link, parent, false);
            }

            TextView kbps = (TextView) view.findViewById(R.id.kbps);
            ImageView flag = (ImageView) view.findViewById(R.id.flag);
            LinearLayout link = (LinearLayout) view.findViewById(R.id.link);

            kbps.setText(links.get(position).kbps+" kbps");
            final int imagenFlag = Sys.init().getImagenFlag(links.get(position).idioma);
            //fondo.setImageResource(imagenFondo);
            Glide.with(context)
                    .load(imagenFlag)
                    .into(flag);

            link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(links.get(position).acestream));
                        context.startActivity(browserIntent);
                    }catch (ActivityNotFoundException ex){
                        new AlertDialog.Builder(context)
                                .setMessage("Para poder reproducir los enlaces necesitas tener instalado AceStream")
                                .setPositiveButton("Instalar", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Sys.openAppInPlayStore("org.acestream.media",context);
                                    }
                                }).show();
                    }
                }
            });
        }else{
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.linkwait, parent, false);
            }
        }







        return view;
    }
}
