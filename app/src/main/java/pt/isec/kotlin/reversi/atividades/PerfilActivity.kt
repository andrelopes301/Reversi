package pt.isec.kotlin.reversi.atividades


import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_perfil.*
import pt.isec.kotlin.reversi.R
import pt.isec.kotlin.reversi.Utils
import pt.isec.kotlin.reversi.databinding.ActivityPerfilBinding
import java.util.*
import kotlin.collections.ArrayList


class PerfilActivity : AppCompatActivity() {

    lateinit var b: ActivityPerfilBinding
    lateinit var gso: GoogleSignInOptions
    lateinit var auth: FirebaseAuth
    lateinit var googleSignInClient: GoogleSignInClient
    private val google_web_id =
        "555245515983-4043e977rrmj6derqgafa2d2pohumavs.apps.googleusercontent.com" //ir a configuracao sdk da web google
    var tema: Int = 1
    var imagemPerfilUri: Uri? = null
    var fotoTirada: Boolean = false

    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setFullscreen(this)
        b = ActivityPerfilBinding.inflate(layoutInflater)
        tema = Utils.setTema(this)


        Utils.mostrarImagemPerfil(this, R.id.Imagem_Perfil)

        auth = Firebase.auth
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(google_web_id).requestEmail().build()


        val navHostFragment = (supportFragmentManager.findFragmentById(R.id.fragment_Perfil) as NavHostFragment)
        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.navperfil)


        if (auth.currentUser != null) {
            graph.startDestination = R.id.visualizarPerfilFragment
        } else
            graph.startDestination = R.id.registarLoginPerfilFragment

        navHostFragment.navController.graph = graph

        checkTheme()
        setContentView(b.root)
        setThemeHelper()
    }

    /* Retirado e adaptado de https://memorynotfound.com/java-display-list-countries-using-locale-getisocountries/ */
    fun spinnerCountries(spinner: Spinner) {
        val countries = ArrayList<String>()
        val isoCountries = Locale.getISOCountries()
        for (country in isoCountries) {
            val locale = Locale("en", country)
            val iso = locale.isO3Country
            val code = locale.country
            val name = locale.displayCountry
            if ("" != iso && "" != code && "" != name) {
                countries.add(countryCodeToEmoji(code) + " " + name)
            }
        }
        countries.sort()
        countries.add(0, "Escolha o seu País: ")

        val countryAdapter = ArrayAdapter(this.applicationContext, android.R.layout.simple_spinner_item, countries)

        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = countryAdapter
    }

    /* Retirado e adaptado de https://attacomsian.com/blog/how-to-convert-country-code-to-emoji-in-java  */
    fun countryCodeToEmoji(code: String?): String {

        // offset between uppercase ascii and regional indicator symbols
        var code = code
        val OFFSET = 127397
        // validate code
        if (code == null || code.length != 2) {
            return ""
        }
        //fix for uk -> gb
        if (code.equals("uk", ignoreCase = true)) {
            code = "gb"
        }
        // convert code to uppercase
        code = code.uppercase(Locale.getDefault())
        val emojiStr = StringBuilder()
        //loop all characters
        for (element in code) {
            emojiStr.appendCodePoint(element.code + OFFSET)
        }
        // return emoji
        return emojiStr.toString()
    }


    fun onVoltarAtras(view: android.view.View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


    fun onTirarFotografia(view: android.view.View) {
        val intent = Intent(this, FotografiaActivity::class.java)
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {

            imagemPerfilUri = data.data
            println("IMAGEMPERFIL URI >> $imagemPerfilUri")

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imagemPerfilUri)
            val bitmapDrawable = BitmapDrawable(bitmap)
            b.ImagemPerfil.setImageDrawable(bitmapDrawable)

            fotoTirada = true
        }

    }

    fun inserirImagemFireBaseStorage(email: String) {
        FirebaseAuth.getInstance().signInAnonymously()
        val ref = FirebaseStorage.getInstance().getReference("/Imagens/$email")
        ref.putFile(imagemPerfilUri!!).addOnSuccessListener {
            Log.d("PerfilActivity",
                "Colocada com sucesso na FireBaseStorage a imagem: ${it.metadata?.path}")

            ref.downloadUrl.addOnSuccessListener { url ->
                Log.d("PerfilActivity", "Localizaçao da imagem: $url")

                val db = Firebase.firestore
                val jogador = db.collection("Jogadores").document(email)

                jogador.get(Source.SERVER)
                    .addOnSuccessListener {
                        jogador.update("ImagemPerfilUrl", url.toString())
                    }
            }
        }
            .addOnFailureListener {
                Log.d("PerfilActivity", "Erro a colocar a imagem na FireStoreDatabase!")
            }

    }


    fun checkTheme() {

        when (tema) {
            1 -> {
                b.voltarAtrasID?.setBackgroundColor(Color.parseColor("#7f5539"))
                b.voltarAtrasImgID?.setBackgroundColor(Color.parseColor("#7f5539"))
            }
            2 -> {
                b.voltarAtrasID?.setBackgroundColor(Color.parseColor("#133A49"))
                b.voltarAtrasImgID?.setBackgroundColor(Color.parseColor("#133A49"))
            }
            3 -> {
                b.voltarAtrasID?.setBackgroundColor(Color.parseColor("#1E1015"))
                b.voltarAtrasImgID?.setBackgroundColor(Color.parseColor("#1E1015"))
            }
        }
    }

    fun setThemeHelper(){
        var tema = Utils.setTema(this)
        var pfl : FrameLayout = findViewById(R.id.perfilflayout)
        when (tema){
            1-> {
                pfl.setBackgroundResource(R.color.fundoClassico)
                b.voltarAtrasTXTP?.setBackgroundResource(R.color.btnClassico)
                b.voltarAtrasIMGP?.setBackgroundResource(R.color.btnClassico)
            }
            2->{
                pfl.setBackgroundResource(R.color.fundoOceano)
                b.voltarAtrasTXTP?.setBackgroundResource(R.color.btnOceano)
                b.voltarAtrasIMGP?.setBackgroundResource(R.color.btnOceano)
            }
            3->{
                pfl.setBackgroundResource(R.color.fundoFrio)
                b.voltarAtrasTXTP?.setBackgroundResource(R.color.btnFrio)
                b.voltarAtrasIMGP?.setBackgroundResource(R.color.btnFrio)
            }
            else->{
                b.voltarAtrasTXTP?.setBackgroundResource(R.color.btnClassico)
                b.voltarAtrasIMGP?.setBackgroundResource(R.color.btnClassico)
            }
        }
    }

}