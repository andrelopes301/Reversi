package pt.isec.kotlin.reversi.fragmentos.jogo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.isec.kotlin.reversi.R
import pt.isec.kotlin.reversi.Utils
import pt.isec.kotlin.reversi.atividades.JogoActivity
import pt.isec.kotlin.reversi.modelo.Reversi

class Score3playersFragment : Fragment() {

    lateinit var nomeJog1: TextView
    lateinit var nomeJog2: TextView
    lateinit var nomeJog3: TextView
    lateinit var pontuacaoJog1: TextView
    lateinit var pontuacaoJog2: TextView
    lateinit var pontuacaoJog3: TextView
    lateinit var peca1: ImageView
    lateinit var peca2: ImageView
    lateinit var peca3: ImageView
    lateinit var img1: ImageView
    lateinit var img2: ImageView
    lateinit var img3: ImageView
    var tema: Int = 1
    var backgroundBorder: Int = 0


    private val reversi: Reversi by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        val view = inflater.inflate(R.layout.fragment_score3players, container, false)

        pontuacaoJog1 = view.findViewById(R.id.pontuacaoJog11)
        pontuacaoJog2 = view.findViewById(R.id.pontuacaoJog22)
        pontuacaoJog3 = view.findViewById(R.id.pontuacaoJog33)
        nomeJog1 = view.findViewById(R.id.nomeJog11)
        nomeJog2 = view.findViewById(R.id.nomeJog22)
        nomeJog3 = view.findViewById(R.id.nomeJog33)
        pontuacaoJog1 = view.findViewById(R.id.pontuacaoJog11)
        pontuacaoJog2 = view.findViewById(R.id.pontuacaoJog22)
        pontuacaoJog3 = view.findViewById(R.id.pontuacaoJog33)
        peca1 = view.findViewById(R.id.pecaJog11)
        peca2 = view.findViewById(R.id.pecaJog22)
        peca3 = view.findViewById(R.id.pecaJog33)
        img1 = view.findViewById(R.id.imgPerfilJog11)
        img2 = view.findViewById(R.id.imgPerfilJog22)
        img3 = view.findViewById(R.id.imgPerfilJog33)

        checkTheme()



        activity?.run {
            reversi.state.observe(this) {


                if (FirebaseAuth.getInstance().currentUser != null) {
                    val db = Firebase.firestore
                    db.collection("Jogadores")
                        .document(FirebaseAuth.getInstance().currentUser!!.email!!)
                        .addSnapshotListener { docSS, e ->
                            if (e != null) {
                                return@addSnapshotListener
                            }
                            if (docSS != null && docSS.exists()) {
                                val username = docSS.getString("Nome")
                                nomeJog1.text = username
                                if(username!=null)
                                    reversi.setNome(username)
                                println(">>>>>>>>>>>>>>>$username<<<<<<<<<<<<<<<<<<<")

                                val imagem = docSS.getString("ImagemPerfilUrl")
                                println("IMAGEM : $imagem")
                                if (imagem == "")
                                    return@addSnapshotListener
                                if(imagem!=null)
                                    reversi.setImagem(imagem)


                                Glide.with((activity as JogoActivity)).load(imagem)
                                    .into(img1)
                            }
                        }
                }





                if (reversi.jogadorAtual == 1) {
                    img1.setBackgroundResource(backgroundBorder)
                    img2.setBackgroundResource(R.drawable.border)
                    img3.setBackgroundResource(R.drawable.border)
                }
                if (reversi.jogadorAtual == 2) {
                    img1.setBackgroundResource(R.drawable.border)
                    img2.setBackgroundResource(backgroundBorder)
                    img3.setBackgroundResource(R.drawable.border)
                }
                if (reversi.jogadorAtual == 3) {
                    img1.setBackgroundResource(R.drawable.border)
                    img2.setBackgroundResource(R.drawable.border)
                    img3.setBackgroundResource(backgroundBorder)
                }



                pontuacaoJog1.text = reversi.pontJogador1.toString()
                pontuacaoJog2.text = reversi.pontJogador2.toString()
                pontuacaoJog3.text = reversi.pontJogador2.toString()
            }
        }






        return view
    }


    fun checkTheme() {
        tema = Utils.setTema(activity as JogoActivity)
        when {
            tema == 1 -> {
                peca1.setImageResource(R.drawable.circ1)
                peca2.setImageResource(R.drawable.circ2)
                nomeJog1.setTextColor(Color.parseColor("#7f5539"))
                nomeJog2.setTextColor(Color.parseColor("#7f5539"))
                backgroundBorder = R.drawable.border_realce_classico
            }
            tema == 2 -> {
                peca1.setImageResource(R.drawable.circ111)
                peca2.setImageResource(R.drawable.circ222)
                nomeJog1.setTextColor(Color.parseColor("#133A49"))
                nomeJog2.setTextColor(Color.parseColor("#133A49"))
                backgroundBorder = R.drawable.border_realce_oceano
            }
            tema == 3 -> {
                peca1.setImageResource(R.drawable.circ11)
                peca2.setImageResource(R.drawable.circ22)
                nomeJog1.setTextColor(Color.parseColor("#1E1015"))
                nomeJog2.setTextColor(Color.parseColor("#1E1015"))
                backgroundBorder = R.drawable.border_realce_frio
            }
        }
    }


}