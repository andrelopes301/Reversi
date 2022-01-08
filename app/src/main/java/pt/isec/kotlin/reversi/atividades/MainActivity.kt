package pt.isec.kotlin.reversi.atividades

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.isec.kotlin.reversi.R
import pt.isec.kotlin.reversi.Utils
import pt.isec.kotlin.reversi.Utils.Companion.mostrarImagemPerfil
import pt.isec.kotlin.reversi.Utils.Companion.setFullscreen
import kotlin.system.exitProcess
import com.google.firebase.firestore.DocumentSnapshot

import com.google.firebase.firestore.QuerySnapshot

import androidx.annotation.NonNull

import com.google.android.gms.tasks.OnCompleteListener


//gradle signingReport - comando
// 90:7A:DF:5C:4D:2C:78:0F:11:1B:65:05:DF:4B:4B:84:96:C9:02:AA - Chave gerada


class MainActivity : AppCompatActivity() {

    var tema: Int = 1
    lateinit var btnOPT: ImageButton
    lateinit var ml: FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullscreen(this)

        mostrarImagemPerfil(this, R.id.perfilJogador)

        tema = Utils.setTema(this)
        Utils.changeLanguage(this)
        setContentView(R.layout.activity_main)
        btnOPT = findViewById(R.id.Btn_Opcoes)
        ml = findViewById(R.id.mainlayout)
        setBtnTheme(tema)

    }

    fun onNovoJogo(view: android.view.View) {

        if (FirebaseAuth.getInstance().currentUser == null) {
            Toast.makeText(this, getString(R.string.iniciarSessaoRequired), Toast.LENGTH_SHORT)
                .show()

            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)

        } else {

            val intent = Intent(this, ModoJogoActivity::class.java)
            startActivity(intent)
        }
    }

    fun onOpcoes(view: android.view.View) {
        val intent = Intent(this, OpcoesActivity::class.java)
        startActivity(intent)
    }

    fun onPontuacoes(view: android.view.View) {


        if (FirebaseAuth.getInstance().currentUser == null) {
            Toast.makeText(this, getString(R.string.iniciarSessaoRequired), Toast.LENGTH_SHORT)
                .show()

            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)

        } else {
            val db = Firebase.firestore
            val email = FirebaseAuth.getInstance().currentUser?.email
            var count = 0
            if (email != null) {
                db.collection("Jogadores").document(email).collection("Scores")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            for (document in task.result) {
                                count++
                            }
                            if (count == 0) {
                                Toast.makeText(
                                    this,
                                    getString(R.string.noScores),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val intent = Intent(this, ScoresActivity::class.java)
                                startActivity(intent)
                            }

                        } else {
                            Log.d("SCORE", "Erro a receber documentos: ", task.exception)
                        }
                    }
            }
        }


    }

    fun onPerfil(view: android.view.View) {
        val intent = Intent(this, PerfilActivity::class.java)
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity(); // Close all activites
        exitProcess(0);  // Releasing resources
    }

    fun onSair(view: android.view.View) {
        finishAffinity(); // Close all activites
        exitProcess(0);  // Releasing resources
    }


    fun setBtnTheme(tema: Int) {
        when (tema) {
            1 -> {
                btnOPT.setImageResource(R.drawable.opcoes_icon_classico)
                ml.setBackgroundResource(R.color.fundoClassico)
            }
            2 -> {
                btnOPT.setImageResource(R.drawable.opcoes_icon_oceano)
                ml.setBackgroundResource(R.color.fundoOceano)
            }
            3 -> {
                btnOPT.setImageResource(R.drawable.opcoes_icon_frio)
                ml.setBackgroundResource(R.color.fundoFrio)
            }
        }
    }
}
