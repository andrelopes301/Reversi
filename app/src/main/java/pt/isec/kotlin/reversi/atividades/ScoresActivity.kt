package pt.isec.kotlin.reversi.atividades

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_scores.*
import pt.isec.kotlin.reversi.R
import pt.isec.kotlin.reversi.Utils


class ScoresActivity : AppCompatActivity() {

    data class Scores(val estado: String, var MyPieces: Long?, var AdvsPieces: Long?, var nomeAdvs: String?)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setTema(this)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_scores)


        val db = Firebase.firestore
        db.collection("Jogadores")
            .document(FirebaseAuth.getInstance().currentUser!!.email!!)
            .addSnapshotListener { docSS, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (docSS != null && docSS.exists()) {
                    val username = docSS.getString("Nome")
                    val usernameTv : TextView = findViewById(R.id.nomeJogadorSCORE)
                    usernameTv.text = username

                    val imagem = docSS.getString("ImagemPerfilUrl")
                    println("IMAGEM : $imagem")
                    if (imagem == "")
                        return@addSnapshotListener
                    val imagemPerfil = findViewById<ImageView>(R.id.Imagem_PerfilSCORE)
                    Glide.with(this).load(imagem).into(imagemPerfil)

                }
            }

        val data = arrayListOf<Scores>()

        val email = FirebaseAuth.getInstance().currentUser?.email
        var count = 0
        if (email != null) {
            db.collection("Jogadores").document(email).collection("Scores")
                .get()
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        for (document in task.result) {
                            count++
                            println("cont++")
                          //  if (count == 6)
                             //  break

                            val myPieces = document.getLong("MyPieces")
                            println("Numero de minhas pecas > "+  myPieces)
                            val advsPieces = document.getLong("AdvsPieces")
                            println("Numero de pecas adversarias> "+  advsPieces)
                            val nomeAdvs = document.getString("NomeAdvs")
                            println("Nome do advs> "+  nomeAdvs)
                            var estado = ""

                            if (myPieces != null) {
                                if (myPieces > advsPieces!!) {
                                    estado = getString(R.string.victory)
                                } else  if (myPieces < advsPieces)
                                  estado = getString(R.string.defeat)
                                else
                                    estado = getString(R.string.tiee)
                            }
                            val item = Scores(estado, myPieces, advsPieces, nomeAdvs)
                            data.add(item)

                        }
                    } else {
                        Log.d("SCORE", "Erro a receber documentos: ", task.exception)
                    }

                    val adapter = MyAdapter(data)
                    listaScores.adapter = adapter
                }
        }


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.bestScores)



    }

    class MyAdapter(val data : ArrayList<Scores>) : BaseAdapter() {

        override fun getCount(): Int = data.size

        override fun getItem(position: Int): Any {
            return data[position]
        }

        override fun getItemId(position: Int): Long = position.toLong()

        @RequiresApi(Build.VERSION_CODES.M)
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = LayoutInflater.from(parent!!.context).inflate(R.layout.listview_item,parent,false)


            view.findViewById<TextView>(R.id.vitoriaDerrotaTextView).apply {
                text = data[position].estado
                println("texto >"+text)
                if (text == "Derrota" || text == "Defeat" || text == "Fracaso" ) {
                    setBackgroundColor(Color.rgb(200, 100, 100))
                    view.findViewById<TextView>(R.id.AdvsScoreTextView)
                        .setTypeface(null, Typeface.BOLD)
                } else if (text == "Vitória" || text == "Victory" || text =="Victoria") {
                    view.findViewById<TextView>(R.id.MyScoreTextView)
                        .setTypeface(null, Typeface.BOLD)
                } else {
                    setBackgroundColor(Color.rgb(255, 253, 152))
                }
            }

            view.findViewById<TextView>(R.id.MyScoreTextView).text = data[position].MyPieces.toString()
            view.findViewById<TextView>(R.id.AdvsScoreTextView).text = data[position].AdvsPieces.toString()
            view.findViewById<TextView>(R.id.nomeAdvsTextView).text = data[position].nomeAdvs



            return view
        }

    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }


fun fireBase(){
    val db = Firebase.firestore

    val email = FirebaseAuth.getInstance().currentUser?.email

    if (email != null) {
        db.collection("Jogadores").document(email).collection("Scores").document()
            .addSnapshotListener { docSS, e ->

                if (e != null) {
                    return@addSnapshotListener
                }
                if (docSS != null && docSS.exists()) {



                }
            }
    }
}}