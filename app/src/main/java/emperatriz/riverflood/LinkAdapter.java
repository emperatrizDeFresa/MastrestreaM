package emperatriz.riverflood;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import emperatriz.riverflood.model.Link;

import static android.content.Context.CLIPBOARD_SERVICE;

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
            ImageView res = (ImageView) view.findViewById(R.id.resolution);
            ImageView fps = (ImageView) view.findViewById(R.id.fps);
            LinearLayout link = (LinearLayout) view.findViewById(R.id.link);
            kbps.setText(links.get(position).kbps+" kbps");
            final int imagenFlag = Sys.init().getImagenFlag(links.get(position).idioma);
//            Glide.with(context)
//                    .load(imagenFlag)
//                    .into(flag);
            final int imagenRes = Sys.init().getRes(links.get(position).resolution);
//            Glide.with(context)
//                    .load(imagenRes)
//                    .into(res);
            final int imagenFps = Sys.init().getFps(links.get(position).fps);
//            Glide.with(context)
//                    .load(imagenFps)
//                    .into(fps);
            flag.setImageResource(imagenFlag);
            res.setImageResource(imagenRes);
            fps.setImageResource(imagenFps);

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
            link.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog ad = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Material_Light_Dialog)).create();

                    ad.setMessage(links.get(position).acestream);
                    ad.setCancelable(true);
                    ad.setButton("Copiar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("acestream", links.get(position).acestream);
                            clipboard.setPrimaryClip(clip);
                                Toast.makeText(context, "Copiado en el portapapeles", Toast.LENGTH_SHORT).show();


                            return;
                        }
                    });
                    ad.setButton2("Compartir", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            try{
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, links.get(position).acestream);
                                sendIntent.setType("text/plain");
                                context.startActivity(sendIntent);
                            }catch (Exception ex){
                                Toast.makeText(context, "No se ha podido compartir ¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show();
                            }


                            return;
                        }
                    });
                    ad.setButton3("Reproducir", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
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


                            return;
                        }
                    });
                    ad.show();
                    return false;
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
