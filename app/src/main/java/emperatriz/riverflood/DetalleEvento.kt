package emperatriz.riverflood

import android.app.Activity
import android.app.ProgressDialog
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Window
import android.view.WindowManager
import android.webkit.WebView
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.crystal.crystalpreloaders.widgets.CrystalPreloader

import emperatriz.riverflood.model.Evento

class DetalleEvento : Activity() {

    internal var w: WebView?=null
    internal var dialog: ProgressDialog? = null
    internal var index: Int = 0
    internal var empty: LinearLayout?=null
    internal var grid: GridView?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_evento)

        w = findViewById(R.id.web) as WebView
        w!!.settings.javaScriptEnabled = true
        Sys.init().w = w
        Sys.init().evento = this

        index = intent.getIntExtra("index", 0)
        val imagenFondo = intent.getIntExtra("fondo", R.drawable.des1)
        var competicionText = intent.getStringExtra("competicion")
        val horaText = intent.getStringExtra("hora")
        var nombreText = intent.getStringExtra("nombre")

        if (nombreText.length == 0) {
            nombreText = competicionText
            competicionText = ""
        }

        val window = window
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)


        window.statusBarColor = -0x1000000


        val competicion = findViewById(R.id.competicion) as TextView
        val nombre = findViewById(R.id.nombre) as TextView
        val hora = findViewById(R.id.hora) as TextView
        competicion.text = competicionText
        hora.text = horaText
        nombre.text = nombreText
        val sf = Typeface.createFromAsset(assets, "SF Movie Poster Condensed Bold.ttf")
        val sf2 = Typeface.createFromAsset(assets, "SF Movie Poster.ttf")
        nombre.typeface = sf
        competicion.typeface = sf
        hora.typeface = sf2
        nombre.setShadowLayer(1f, Sys.getDp(2f, this), Sys.getDp(2f, this), -0x1000000)
        competicion.setShadowLayer(1f, Sys.getDp(1.4f, this), Sys.getDp(1.4f, this), -0x1000000)
        hora.setShadowLayer(1f, Sys.getDp(1.4f, this), Sys.getDp(1.4f, this), -0x1000000)
        val fondo = findViewById(R.id.imagenDeporte) as ImageView
        fondo.setImageResource(imagenFondo)


        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val screenDensity = metrics.densityDpi

        empty = findViewById(R.id.empty) as LinearLayout
        grid = findViewById(R.id.grid) as GridView
        grid!!.numColumns = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 2 else if (screenDensity > 350) 3 else 4
        grid!!.emptyView = empty

        Sys.init().getSelectedGestor(this).getLinks(index)

    }

    fun populateLinks(evento: Evento) {
        if (evento.links!!.size == 0) {
            Sys.toast(this, "No hay enlaces disponibles (ಠ_ಠ)")
            finish()
        } else {
            if (grid!!.adapter == null) {
                grid!!.adapter = LinkAdapter(evento.links!!, this@DetalleEvento)
            } else {
                (grid!!.adapter as LinkAdapter).notifyDataSetChanged()
            }
        }


    }
}
