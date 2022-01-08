package pt.isec.kotlin.reversi

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


class Utils {


    companion object {
        lateinit var sharedPreferences: SharedPreferences
        private lateinit var theme: String
        var tema : Int = 2

        fun setFullscreen(activity: AppCompatActivity) {
            activity.requestWindowFeature(Window.FEATURE_NO_TITLE) //will hide the title
            activity.supportActionBar?.hide() // hide the title bar
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN) //enable full screen

        }


        fun mostrarImagemPerfil(activity: AppCompatActivity, imageView: Int) {

            val db = Firebase.firestore

            println("email > ${FirebaseAuth.getInstance().currentUser?.email}")
            if (FirebaseAuth.getInstance().currentUser != null) {

                db.collection("Jogadores").document(FirebaseAuth.getInstance().currentUser?.email!!)
                    .addSnapshotListener { docSS, e ->

                        if (e != null) {
                            return@addSnapshotListener
                        }
                        if (docSS != null && docSS.exists()) {

                            val imagem = docSS.getString("ImagemPerfilUrl")
                                    //   println("IMAGEM : $imagem")
                            if (imagem == "")
                                return@addSnapshotListener


                            val imagemPerfil = activity.findViewById<ImageView>(imageView)
                                // Glide.with(activity).load(imagem).into(imagemPerfil)


                            Glide.with(activity.applicationContext).load(imagem).into(imagemPerfil)
                        }
                    }
            }
        }


        fun resetImagemPerfil(activity: AppCompatActivity, imageView: Int) {
            if (FirebaseAuth.getInstance().currentUser == null) {
                val imagemPerfil = activity.findViewById<ImageView>(imageView)
                imagemPerfil.setImageResource(R.drawable.avatar)
            }
        }


        fun setTema(activity: AppCompatActivity): Int {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            theme = sharedPreferences.getString("temas", "").toString()
            println(">>>>>>Tema: $theme")

            when {
                theme.equals("Classico") -> {
                    activity.setTheme(R.style.temaClassico);tema = 1;return 1
                }
                theme.equals("Oceano") -> {
                    activity.setTheme(R.style.temaOceano);tema=2;return 2
                }
                theme.equals("Frio") -> {
                    activity.setTheme(R.style.temaFrio);tema=3;return 3
                }
            }
            activity.setTheme(R.style.temaOceano)
            tema=2
            return 2
        }



        fun getTableColors(activity: AppCompatActivity): IntArray {
            var pecas = IntArray(5)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            theme = sharedPreferences.getString("temas", "").toString()
            when (tema){
                1 -> {
                    pecas[0] = R.drawable.circ1
                    pecas[1] = R.drawable.circ2
                    pecas[2] = R.drawable.circ3
                    pecas[3] = R.color.tabuleiroClassico
                    pecas[4] = R.color.selecionadoClassico
                    return pecas
                }
                2 -> {
                    pecas[0] = R.drawable.circ111
                    pecas[1] = R.drawable.circ222
                    pecas[2] = R.drawable.circ3
                    pecas[3] = R.color.tabuleiroTurquesa
                    pecas[4] = R.color.selecionadoTurquesa
                    return pecas
                }
                3 -> {
                    pecas[0] = R.drawable.circ11
                    pecas[1] = R.drawable.circ22
                    pecas[2] = R.drawable.circ3
                    pecas[3] = R.color.tabuleiroRoxoSujo
                    pecas[4] = R.color.selecionadoRoxoClaro
                    return pecas
                }
            }

            pecas[0] = R.drawable.circ1
            pecas[1] = R.drawable.circ2
            pecas[2] = R.drawable.circ3
            pecas[3] = R.color.tabuleiroClassico
            pecas[4] = R.color.selecionadoClassico

            return pecas
        }


        fun getMostrarJogadas(activity: AppCompatActivity): Boolean {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            val check = sharedPreferences.getBoolean("switchMJP", true)
            return check
        }

        fun setAppLocale(context: Context, language: String) {
            val locale = Locale(language)
            Locale.setDefault(locale)
            val config = context.resources.configuration
            config.setLocale(locale)
            context.createConfigurationContext(config)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }

        fun changeLanguage(activity: AppCompatActivity) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
            val check = sharedPreferences.getString("language", "").toString()
            when {
                check == "English" || check == "Inglês" || check == "Inglés"-> {
                    setAppLocale(activity, "en")
                }
                check == "Portuguese" || check == "Português" || check == "Portugués"-> {
                    setAppLocale(activity, "pt")
                }
                check == "Spanish" || check == "Espanhol" || check == "Español" -> {
                    setAppLocale(activity, "es")
                }
            }

        }


    }
}