package emperatriz.riverflood


import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.util.TypedValue
import android.view.Window
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import com.crystal.crystalpreloaders.widgets.CrystalPreloader
import emperatriz.riverflood.model.*


import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.ArrayList
import java.util.Arrays
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.TimeZone

class Sys private constructor() {

    var w: WebView? = null
    var panel: Panel? = null
    var evento: DetalleEvento? = null

    private val gestores = ArrayList<GestorPagina>()

    private val futbol: ArrayList<Int>
    private val baloncesto: ArrayList<Int>
    private val ciclismo: ArrayList<Int>
    private val motogp: ArrayList<Int>
    private val f1: ArrayList<Int>
    private val tenis: ArrayList<Int>
    private val boxeo: ArrayList<Int>
    private val atletismo: ArrayList<Int>
    private val desconocido: ArrayList<Int>

    private var indexF: Int = 0
    private var indexB: Int = 0
    private var indexC: Int = 0
    private var indexM: Int = 0
    private var indexF2: Int = 0
    private var indexB2: Int = 0
    private var indexD: Int = 0
    private var indexA: Int = 0
    private var indexT: Int = 0

    private var dialog: Dialog? = null

    init {
        futbol = ArrayList(Arrays.asList(R.drawable.fut1, R.drawable.fut2, R.drawable.fut3, R.drawable.fut4, R.drawable.fut5, R.drawable.fut6, R.drawable.fut7))
        baloncesto = ArrayList(Arrays.asList(R.drawable.bal1, R.drawable.bal2, R.drawable.bal3))
        ciclismo = ArrayList(Arrays.asList(R.drawable.cic1, R.drawable.cic2, R.drawable.cic3))
        motogp = ArrayList(Arrays.asList(R.drawable.mot1, R.drawable.mot2, R.drawable.mot3))
        f1 = ArrayList(Arrays.asList(R.drawable.for1, R.drawable.for2, R.drawable.for3))
        tenis = ArrayList(Arrays.asList(R.drawable.ten1, R.drawable.ten2, R.drawable.ten3))
        boxeo = ArrayList(Arrays.asList(R.drawable.box1, R.drawable.box2))
        atletismo = ArrayList(Arrays.asList(R.drawable.atl1, R.drawable.atl2, R.drawable.atl3))
        desconocido = ArrayList(Arrays.asList(R.drawable.des1, R.drawable.des2))
        indexF = 0
        indexB = 0
        indexC = 0
        indexM = 0
        indexF2 = 0
        indexB2 = 0
        indexD = 0
        indexA = 0
        indexT = 0
        Collections.shuffle(futbol)
        Collections.shuffle(baloncesto)
        Collections.shuffle(ciclismo)
        Collections.shuffle(motogp)
        Collections.shuffle(f1)
        Collections.shuffle(tenis)
        Collections.shuffle(boxeo)
        Collections.shuffle(atletismo)
        Collections.shuffle(desconocido)

        gestores.add(Linkotes.init())
        gestores.add(LivesportWs.init())
        gestores.add(LFootballWs.init())

    }

    fun updateGestorUrl(context:Context){
        var i=0
        for (gestor in gestores){
            gestor.updateUrl(getUrl(i++,context))
        }
    }


    fun getUrl(pos:Int, context: Context): String {
        return getPreferencia("urlGestor"+pos, "", context)
    }

    fun saveUrl(urls:ArrayList<String>, context: Context) {
        var pos=0
        for (url in urls){
            guardaPreferencia("urlGestor"+pos++, url, context)
        }
    }


    fun getSelectedGestor(context: Context): GestorPagina {
        val indexGestores = getPreferencia("indexGestores", 0, context)
        return gestores[indexGestores]
    }


    fun getGestores(): ArrayList<String> {
        val ret = ArrayList<String>()
        for (g in gestores) {
            ret.add(g.nombre)
        }
        return ret
    }


    fun selectGestorId(id: Int, context: Context) {
        guardaPreferencia("indexGestores", id, context)
    }

    fun horaValidaEvento(d: Date?): Boolean {
        val limite = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"))
        limite.add(Calendar.HOUR, 6)
        val lim = limite.timeInMillis
        limite.add(Calendar.HOUR, -10)
        val lim2 = limite.timeInMillis
        val dl = d!!.time
        return lim >= dl// && lim2<dl;
    }

    fun getImagenDeporte(tipo: Int): Int {
        when (tipo) {
            Evento.FUTBOL -> {
                indexF++
                if (indexF >= futbol.size) indexF = 0
                return futbol[indexF]
            }
            Evento.BALONCESTO -> {
                indexB++
                if (indexB >= baloncesto.size) indexB = 0
                return baloncesto[indexB]
            }
            Evento.BOXEO -> {
                indexB2++
                if (indexB2 >= boxeo.size) indexB2 = 0
                return boxeo[indexB2]
            }
            Evento.CICLISMO -> {
                indexC++
                if (indexC >= ciclismo.size) indexC = 0
                return ciclismo[indexC]
            }
            Evento.DESCONOCIDO -> {
                indexD++
                if (indexD >= desconocido.size) indexD = 0
                return desconocido[indexD]
            }
            Evento.F1 -> {
                indexF2++
                if (indexF2 >= f1.size) indexF2 = 0
                return f1[indexF2]
            }
            Evento.MOTOS -> {
                indexM++
                if (indexM >= motogp.size) indexM = 0
                return motogp[indexM]
            }
            Evento.ATLETISMO -> {
                indexA++
                if (indexA >= atletismo.size) indexA = 0
                return atletismo[indexA]
            }
            Evento.TENIS -> {
                indexT++
                if (indexT >= tenis.size) indexT = 0
                return tenis[indexT]
            }
        }
        return desconocido[0]
    }

    fun getImagenFlag(tipo: Int): Int {
        when (tipo) {
            Link.ENG -> {
                return R.drawable.ing
            }
            Link.ESP -> {
                return R.drawable.esp
            }
            Link.GER -> {
                return R.drawable.ale
            }
            Link.ITA -> {
                return R.drawable.ita
            }
            Link.POR -> {
                return R.drawable.por
            }
            Link.RU -> {
                return R.drawable.rus
            }
            Link.CRO -> {
                return R.drawable.cro
            }
            Link.SUE -> {
                return R.drawable.sue
            }
            Link.LET -> {
                return R.drawable.let
            }
            Link.UCR -> {
                return R.drawable.ucr
            }
            Link.FRA -> {
                return R.drawable.fra
            }
        }
        return R.drawable.des
    }

    fun getRes(tipo: Int): Int {
        when (tipo) {
            Link.HD1080 -> {
                return R.drawable.hd1080
            }
            Link.HD720 -> {
                return R.drawable.hd720
            }
            Link.SD576 -> {
                return R.drawable.sd576
            }
        }
        return R.drawable.no
    }

    fun getFps(tipo: Int): Int {
        when (tipo) {
            Link.FPS25 -> {
                return R.drawable.fps25
            }
            Link.FPS50 -> {
                return R.drawable.fps50
            }
        }
        return R.drawable.no
    }

    fun cargando(context: Context) {
        if (!estaCargando()){
            dialog = Dialog(context, R.style.CustomDialog)
            dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog!!.setContentView(R.layout.waitdialog)
            val nombre = dialog!!.findViewById(R.id.nombre) as TextView
            val sf = Typeface.createFromAsset(context.assets, "SF Movie Poster Condensed Bold.ttf")
            nombre.typeface = sf
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            if ( Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==Calendar.TUESDAY){
                nombre.text = "MartesstreaM"
            }

            val colors = ArrayList<Int>()
            colors.add(0xff00ff)
            colors.add(0xffff00)
            colors.add(0x00ffff)
            var i=0

            dialog!!.show()
        }


//        var revolvingCircle: CrystalPreloader = dialog!!.findViewById(R.id.revolvingCircle) as CrystalPreloader
//
//        val handlerAnim = Handler()
//        val runnableCode = runnable {
//            if (estaCargando()){
//                revolvingCircle.foreground.
//                handlerAnim.postDelayed(this, 300)
//            }
//        }
//        handlerAnim.postDelayed(runnableCode , 300)
    }

    fun runnable(body: Runnable.(Runnable)->Unit) = object: Runnable {
        override fun run() {
            this.body(this)
        }
    }

    fun estaCargando(): Boolean {
        return dialog != null && dialog!!.isShowing
    }

    fun cargado() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
    }

    fun guardaPreferencia(key: String, value: Int, context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getPreferencia(key: String, defValue: Int, context: Context): Int {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getInt(key, defValue)
    }

    fun guardaPreferencia(key: String, value: String, context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getPreferencia(key: String, defValue: String, context: Context): String {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        return preferences.getString(key, defValue)
    }

    fun checkNewVersion(context: Context): Boolean {
        try {
            // Create a URL for the desired page
            val url = URL("https://drive.google.com/uc?export=download&id=0BxODOfYE_PuDWnhkRHRaTWN3T2M")

            // Read all the text returned by the server
            val ins = BufferedReader(InputStreamReader(url.openStream()))
            val v = ins.readLine()
            var i=0
            var pagina = ins.readLine()
            var paginas:ArrayList<String> = ArrayList()
            while (pagina!=null){

                if (pagina!=null){
                    paginas.add(pagina)
                    gestores[i++].updateUrl(pagina)
                }
                pagina = ins.readLine()
            }
            ins.close()
            saveUrl(paginas,context)
            val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            val server = Integer.parseInt(v.replace(".", ""))
            val app = Integer.parseInt(versionName.replace(".", ""))
            return server > app
        } catch (e: Exception) {
            return false
        }

    }

    companion object {
        private var instance: Sys? = null

        fun init(): Sys {
            if (instance == null)
                instance = Sys()
            return instance!!
        }

        fun limpia(st: String): String {
            return st.replace("&nbsp;", "").replace("<br />", "<br>").replace("<br/>", "<br>").replace("\\n", "").replace("\\t", "").replace("<br>", " ").replace("&Ntilde;", "ñ").replace("&acute;", "\'")
        }

        fun openAppInPlayStore(packageName: String, context: Context) {
            val rateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName))
            var marketFound = false

            // find all applications able to handle our rateIntent
            val otherApps = context.packageManager.queryIntentActivities(rateIntent, 0)
            for (otherApp in otherApps) {
                // look for Google Play application
                if (otherApp.activityInfo.applicationInfo.packageName == "com.android.vending") {

                    val otherAppActivity = otherApp.activityInfo
                    val componentName = ComponentName(
                            otherAppActivity.applicationInfo.packageName,
                            otherAppActivity.name
                    )
                    rateIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    rateIntent.component = componentName
                    context.startActivity(rateIntent)
                    marketFound = true
                    break

                }
            }

            // Si no está instalado el play store, se ejecuta desde el navegador
            if (!marketFound) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName))
                context.startActivity(webIntent)
            }
        }

        // Storage Permissions
        val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        /**
         * Checks if the app has permission to write to device storage
         *
         *
         * If the app does not has permission then the user will be prompted to grant permissions
         *
         * @param activity
         */
        fun verifyStoragePermissions(activity: Activity) {
            // Check if we have write permission
            val permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                        activity,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                )
            }
        }

        @Throws(IOException::class)
        fun sendAppItself(paramActivity: Activity) {
            val pm = paramActivity.packageManager
            val appInfo: ApplicationInfo
            try {
                appInfo = pm.getApplicationInfo(paramActivity.packageName,
                        PackageManager.GET_META_DATA)
                val sendBt = Intent(Intent.ACTION_SEND)
                sendBt.type = "*/*"
                sendBt.putExtra(Intent.EXTRA_STREAM,
                        Uri.parse("file://" + appInfo.publicSourceDir))

                paramActivity.startActivity(Intent.createChooser(sendBt,
                        "Comparte MasterstreaM enviándola a través de:"))
            } catch (e1: PackageManager.NameNotFoundException) {
                e1.printStackTrace()
            }

        }

        @Throws(IOException::class)
        fun sendAppItselfText(paramActivity: Activity) {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Descarga MasterstreaM desde aquí: http://goo.gl/59McZu")
            sendIntent.type = "text/plain"
            paramActivity.startActivity(sendIntent)
        }

        fun getDp(pixels: Float, context: Context): Float {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, context.resources.displayMetrics)
        }

        // get from build info
        // check if /system/app/Superuser.apk is present
        // ignore
        // try executing commands
        val isRooted: Boolean
            get() {
                val buildTags = android.os.Build.TAGS
                if (buildTags != null && buildTags.contains("test-keys")) {
                    return true
                }
                try {
                    val file = File("/system/app/Superuser.apk")
                    if (file.exists()) {
                        return true
                    }
                } catch (e1: Exception) {
                }

                return (canExecuteCommand("/system/xbin/which su")
                        || canExecuteCommand("/system/bin/which su") || canExecuteCommand("which su"))
            }

        // executes a command on the system
        private fun canExecuteCommand(command: String): Boolean {
            var executedSuccesfully: Boolean
            try {
                Runtime.getRuntime().exec(command)
                executedSuccesfully = true
            } catch (e: Exception) {
                executedSuccesfully = false
            }

            return executedSuccesfully
        }

        fun toast(ctx: Context, text: String) {
            Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show()
            //Snackbar.make(((Activity)ctx).findViewById(android.R.id.content),text,Snackbar.LENGTH_SHORT).show();
        }
    }
}
