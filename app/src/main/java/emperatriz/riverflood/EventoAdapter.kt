package emperatriz.riverflood

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.bumptech.glide.Glide

import java.text.SimpleDateFormat
import java.util.ArrayList

import emperatriz.riverflood.model.Evento

/**
 * Created by ramon on 01/05/2017.
 */

class EventoAdapter(private val eventos: ArrayList<Evento>, private val context: Context) : BaseAdapter() {

    internal var sdf = SimpleDateFormat("HH:mm")

    override fun getCount(): Int {
        return eventos.size
    }

    override fun getItem(position: Int): Any {
        return eventos[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }


    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view
        if (view == null) {
            val inflater = context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.evento, parent, false)
        }

        var nombreS = eventos[position].nombre!!.toUpperCase()
        var competicionS = eventos[position].competicion!!.toUpperCase()

        if (nombreS.length == 0) {
            nombreS = competicionS
            competicionS = ""
        }

        val competicion = view!!.findViewById(R.id.competicion) as TextView
        val nombre = view.findViewById(R.id.nombre) as TextView
        val hora = view.findViewById(R.id.hora) as TextView
        val gradiente = view.findViewById(R.id.gradiente) as LinearLayout
        val global = view.findViewById(R.id.global) as LinearLayout
        val eventocompleto = view.findViewById(R.id.eventocompleto) as LinearLayout

        competicion.setText(competicionS)
        hora.text = if (eventos[position].directo) "AHORA" else sdf.format(eventos[position].hora)
        nombre.setText(nombreS)
        val sf = Typeface.createFromAsset(context.assets, "SF Movie Poster Condensed Bold.ttf")
        val sf2 = Typeface.createFromAsset(context.assets, "SF Movie Poster.ttf")
        nombre.typeface = sf
        nombre.setShadowLayer(1f, Sys.getDp(2f, context), Sys.getDp(2f, context), -0x1000000)
        competicion.typeface = sf
        competicion.setShadowLayer(1f, Sys.getDp(1.4f, context), Sys.getDp(1.4f, context), -0x1000000)
        hora.typeface = sf2
        hora.setShadowLayer(1f, Sys.getDp(1.4f, context), Sys.getDp(1.4f, context), -0x1000000)
        val fondo = view.findViewById(R.id.imagenDeporte) as ImageView
        val imagenFondo = eventos[position].fondo
        //fondo.setImageResource(imagenFondo);
        Glide.with(context)
                .load(imagenFondo)
                .into(fondo)
        view.isClickable = true
        view.setOnClickListener {
            val i = Intent(context, DetalleEvento::class.java)
            i.putExtra("index", position)
            i.putExtra("fondo", imagenFondo)
            i.putExtra("competicion", eventos[position].competicion!!.toUpperCase())
            i.putExtra("nombre", eventos[position].nombre!!.toUpperCase())
            i.putExtra("hora", if (eventos[position].directo) "AHORA" else sdf.format(eventos[position].hora))
            val p1 = Pair.create(competicion as View, "competiciont")
            val p2 = Pair.create(hora as View, "horat")
            val p3 = Pair.create(nombre as View, "nombret")
            val p4 = Pair.create(fondo as View, "fondot")
            val p5 = Pair.create(gradiente as View, "gradientet")
            val p6 = Pair.create(global as View, "globalt")

            if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity, p4, p6)
                context.startActivity(i, options.toBundle())
            } else {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity, p4, p5)
                context.startActivity(i, options.toBundle())
            }
        }

        view.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                global.setBackgroundColor(0x22ffffff)
                val anim = ScaleAnimation(0.98f, 1f, 0.97f, 1f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f)
                anim.duration = 70
                anim.isFillEnabled = true
                anim.fillAfter = true

                anim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        global.setBackgroundColor(0x22000000)
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
                eventocompleto.startAnimation(anim)
            }


            false
        }





        return view
    }
}
