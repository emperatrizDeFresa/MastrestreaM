package emperatriz.riverflood

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast


import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList

import emperatriz.riverflood.model.Evento

class Panel : Activity() {

    internal var w: WebView?=null
    internal var dialog: ProgressDialog? = null
    internal var animacion: LinearLayout?=null
    internal var noEvents: LinearLayout?=null
    internal var noEvents1: TextView?=null
    internal var noEvents2: TextView?=null
    internal var grid: GridView?=null

    internal var pd: ProgressDialog?=null
    internal var webs:Spinner? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panel)
        Sys.init().updateGestorUrl(this@Panel)
        w = findViewById(R.id.web) as WebView
        w!!.settings.javaScriptEnabled = true
        Sys.init().w = w
        Sys.init().panel = this
        actionBar!!.title = null
        actionBar!!.setDisplayShowCustomEnabled(true)
        actionBar!!.setBackgroundDrawable(ColorDrawable(-0x1000000))
        webs = Spinner(this)
//        val spinnerArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, Sys.init().getGestores())
//        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        webs!!.adapter = spinnerArrayAdapter
//        try{
//            webs.setSelection(Sys.init().getSelectedGestor(this@Panel).id)
//        }catch (ex:Exception){
//            webs.setSelection(0)
//            Sys.init().selectGestorId(0,this@Panel)
//        }

        actionBar!!.customView = webs



        noEvents = findViewById(R.id.noEvents) as LinearLayout
        noEvents1 = findViewById(R.id.noEvents1) as TextView
        noEvents2 = findViewById(R.id.noEvents2) as TextView

        val nombre = findViewById(R.id.nombre) as TextView
        val sf = Typeface.createFromAsset(assets, "SF Movie Poster Condensed Bold.ttf")
        nombre.typeface = sf

        grid = findViewById(R.id.grid) as GridView
        grid!!.numColumns = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 1 else 2
        grid!!.emptyView = noEvents



        animacion = findViewById(R.id.waitAnimation) as LinearLayout

        //Sys.init().cargando(this@Panel)
        //Sys.init().getSelectedGestor(this@Panel).parseData()
        val cv = CheckVersion()
        cv.execute()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater

        inflater.inflate(if (Sys.isRooted) R.menu.panel else R.menu.panel, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> {
                val ad = AlertDialog.Builder(ContextThemeWrapper(this@Panel, android.R.style.Theme_Material_Light_Dialog)).create()

                ad.setMessage("¿Cómo quieres compartir la app?")
                ad.setCancelable(true)
                ad.setButton("Enviar archivo", DialogInterface.OnClickListener { dialog, which ->
                    try {
                        Sys.sendAppItself(this@Panel)
                    } catch (ex: Exception) {
                        Sys.toast(this@Panel, "No se ha podido compartir ¯\\_(ツ)_/¯")
                    }

                    return@OnClickListener
                })
                ad.setButton2("Enviar enlace", DialogInterface.OnClickListener { dialog, which ->
                    try {
                        Sys.sendAppItselfText(this@Panel)
                    } catch (ex: Exception) {
                        Sys.toast(this@Panel, "No se ha podido compartir ¯\\_(ツ)_/¯")
                    }

                    return@OnClickListener
                })
                ad.setButton3("Usando telepatía", DialogInterface.OnClickListener { dialog, which ->
                    Sys.toast(this@Panel, "( ͡° ͜ʖ ͡°)")
                    return@OnClickListener
                })
                ad.show()
            }
            R.id.action_refresh -> {

                //grid!!.adapter = null

                Sys.init().cargando(this@Panel)
                Sys.init().getSelectedGestor(this@Panel).parseDataRefresh()
            }
            R.id.action_kill -> {
                val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                activityManager.killBackgroundProcesses("org.acestream.media")
            }
        }
        return true
    }


    fun populateGrid(eventos: ArrayList<Evento>) {

        val anim = TranslateAnimation(0f, 0f, 1000f, 0f)
        anim.duration = 400
        anim.interpolator = DecelerateInterpolator()
        anim.setAnimationListener(object : Animation.AnimationListener {

            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationRepeat(animation: Animation) {
                // TODO Auto-generated method stub

            }

            override fun onAnimationEnd(animation: Animation) {

                noEvents1!!.setTextColor(if (eventos.size > 0) -0x1000000 else -0x1)
                noEvents2!!.setTextColor(if (eventos.size > 0) -0x1000000 else -0x1)

            }
        })




        grid!!.adapter = EventoAdapter(eventos, this@Panel)
        grid!!.startAnimation(anim)
        Sys.init().cargado()
        Sys.verifyStoragePermissions(this@Panel)
//        val cv = CheckVersion()
//        cv.execute()

    }

    private inner class CheckVersion : AsyncTask<String, Float, Boolean>() {


        override fun doInBackground(vararg urls: String): Boolean? {

            return Sys.init().checkNewVersion(this@Panel)

        }


        override fun onPostExecute(result: Boolean?) {
            if (result!!) {
                val ad = AlertDialog.Builder(ContextThemeWrapper(this@Panel, android.R.style.Theme_Material_Light_Dialog)).create()
                ad.setTitle("Actualización disponible")
                ad.setMessage("Hay una nueva versión que introduce nuevos fallos aún más catastróficos y desesperantes.\n\n ¿Quieres actualizar?")
                ad.setCancelable(true)
                ad.setButton("Por supuesto", DialogInterface.OnClickListener { dialog, which ->
                    dialogActualizar()

                    return@OnClickListener
                })
                ad.setButton2("Hemos venido a jugar", DialogInterface.OnClickListener { dialog, which ->
                    dialogActualizar()

                    return@OnClickListener
                })
                ad.setButton3("Malo será", DialogInterface.OnClickListener { dialog, which ->
                    dialogActualizar()

                    return@OnClickListener
                })
                ad.show()

            }

            webs!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    val gestor = Sys.init().getSelectedGestor(this@Panel)
                    val gid = gestor.id
                    //gestor.updateUrl(Sys.init().getUrl(position,this@Panel))
                    Sys.init().selectGestorId(position, this@Panel)

                    if (true||gid != position) {
                        //grid!!.adapter = null
                        Sys.init().cargando(this@Panel)
                        Sys.init().getSelectedGestor(this@Panel).parseData()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }

            try{
                val spinnerArrayAdapter = ArrayAdapter(this@Panel, android.R.layout.simple_spinner_item, Sys.init().getGestores())
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                if (Sys.init().getGestores().size==0){
                    finish()
                }
                webs!!.adapter = spinnerArrayAdapter
                webs!!.setSelection(Sys.init().getSelectedGestor(this@Panel).id)
            }catch (ex:Exception){
                webs!!.setSelection(0)
                Sys.init().selectGestorId(0,this@Panel)
            }

        }
    }

    private fun dialogActualizar() {
        pd = ProgressDialog(ContextThemeWrapper(this@Panel, android.R.style.Theme_Material_Light_Dialog))
        pd!!.isIndeterminate = true
        pd!!.setProgressNumberFormat(null)
        pd!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pd!!.setCancelable(false)
        //pd.setMax(100);
        pd!!.setMessage("Descargando actualización")
        pd!!.show()

        val down = Download()
        down.execute("https://drive.google.com/uc?export=download&id=0BxODOfYE_PuDaFJidXlpMWpLOVE")
    }


    private inner class Download : AsyncTask<String, Int, String>() {

        override fun doInBackground(vararg params: String): String {
            if (downloadApk(params[0])) {
                val intent = Intent(Intent.ACTION_VIEW)
                val PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
                // intent.setDataAndType(Uri.fromFile(new
                // File(Sys.init().ctx.getFilesDir() + "/", "app.apk")),
                // "application/vnd.android.package-archive");
                intent.setDataAndType(Uri.fromFile(File(PATH, "masterstream.apk")), "application/vnd.android.package-archive")
                startActivity(intent)
                return "OK"

            } else {
                return "NO"

            }

        }

        override fun onPostExecute(ok: String) {

            pd!!.dismiss()

            if (ok == "NO") {
                Sys.toast(this@Panel, "No se ha descargado la actualización")
            } else {
                //                System.exit(0);
                //                finish();
            }


        }

        private fun downloadApk(path: String): Boolean {
            var ret = false
            val outputFileName = "masterstream.apk"
            try {
                // connecting to url
                val u = URL(path)
                val c = u.openConnection() as HttpURLConnection
                //c.setRequestMethod("GET");
                // c.setDoOutput(true);
                c.connect()
                val fileLength = c.contentLength
                val PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
                val file = File(PATH)
                val creado = file.mkdirs()
                val outputFile = File(file, outputFileName)


                // file input is from the url
                val ins = c.inputStream
                val fos = FileOutputStream(outputFile)


                // here's the download code
                val buffer = ByteArray(1024)
                var len1 = 0
                var total: Long = 0
                val count: Int
                len1 = ins.read(buffer)
                while (len1 != -1) {
                    fos.write(buffer, 0, len1)
                    try {
                        total += len1.toLong()

                        len1 = ins.read(buffer)
                    } catch (ex: Exception) {
                    }

                }
                fos.close()
                ins.close()
                ret = true
            } catch (ex: Exception) {
                ret = false
            }

            return ret
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            Sys.REQUEST_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                } else {
                    Sys.toast(this@Panel, "Sin los permisos necesarios la app no puede funcionar")
                    finish()
                }
                return
            }
        }
    }

}
