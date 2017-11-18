package emperatriz.riverflood

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView


import com.bumptech.glide.Glide

import java.util.ArrayList

import emperatriz.riverflood.model.Link

import android.content.Context.CLIPBOARD_SERVICE

/**
 * Created by ramon on 01/05/2017.
 */

class LinkAdapter(private val links: ArrayList<Link>, private val context: Context) : BaseAdapter() {

    override fun getCount(): Int {
        return links.size
    }

    override fun getItem(position: Int): Any {
        return links[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }


    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view

        if (links[position].acestream != null && links[position].acestream!!.length > 10) {
            if (true || view == null) {
                val inflater = context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                view = inflater.inflate(R.layout.link, parent, false)
            }

            val kbps = view!!.findViewById(R.id.kbps) as TextView
            val flag = view.findViewById(R.id.flag) as ImageView
            val res = view.findViewById(R.id.resolution) as ImageView
            val fps = view.findViewById(R.id.fps) as ImageView
            val link = view.findViewById(R.id.link) as LinearLayout
            kbps.text = links[position].kbps.toString() + " kbps"
            val imagenFlag = Sys.init().getImagenFlag(links[position].idioma)
            //            Glide.with(context)
            //                    .load(imagenFlag)
            //                    .into(flag);
            val imagenRes = Sys.init().getRes(links[position].resolution)
            //            Glide.with(context)
            //                    .load(imagenRes)
            //                    .into(res);
            val imagenFps = Sys.init().getFps(links[position].fps)
            //            Glide.with(context)
            //                    .load(imagenFps)
            //                    .into(fps);
            flag.setImageResource(imagenFlag)
            res.setImageResource(imagenRes)
            fps.setImageResource(imagenFps)

            link.setOnClickListener {
                try {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(links[position].acestream))
                    context.startActivity(browserIntent)
                } catch (ex: ActivityNotFoundException) {
                    AlertDialog.Builder(context)
                            .setMessage("Para poder reproducir los enlaces necesitas tener instalado AceStream")
                            .setPositiveButton("Instalar") { dialog, which -> Sys.openAppInPlayStore("org.acestream.media", context) }.show()
                }
            }
            link.setOnLongClickListener {
                val ad = AlertDialog.Builder(ContextThemeWrapper(context, android.R.style.Theme_Material_Light_Dialog)).create()

                ad.setMessage(links[position].acestream)
                ad.setCancelable(true)
                ad.setButton("Copiar", DialogInterface.OnClickListener { dialog, which ->
                    val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("acestream", links[position].acestream)
                    clipboard.primaryClip = clip
                    Sys.toast(context, "Copiado en el portapapeles")


                    return@OnClickListener
                })
                ad.setButton2("Compartir", DialogInterface.OnClickListener { dialog, which ->
                    try {
                        val sendIntent = Intent()
                        sendIntent.action = Intent.ACTION_SEND
                        sendIntent.putExtra(Intent.EXTRA_TEXT, links[position].acestream)
                        sendIntent.type = "text/plain"
                        context.startActivity(sendIntent)
                    } catch (ex: Exception) {
                        Sys.toast(context, "No se ha podido compartir ¯\\_(ツ)_/¯")
                    }


                    return@OnClickListener
                })
                ad.setButton3("Reproducir", DialogInterface.OnClickListener { dialog, which ->
                    try {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(links[position].acestream))
                        context.startActivity(browserIntent)
                    } catch (ex: ActivityNotFoundException) {
                        AlertDialog.Builder(context)
                                .setMessage("Para poder reproducir los enlaces necesitas tener instalado AceStream")
                                .setPositiveButton("Instalar") { dialog, which -> Sys.openAppInPlayStore("org.acestream.media", context) }.show()
                    }


                    return@OnClickListener
                })
                ad.show()
                false
            }
        } else {
            if (view == null) {
                val inflater = context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                view = inflater.inflate(R.layout.linkwait, parent, false)
            }
        }







        return view!!
    }
}
