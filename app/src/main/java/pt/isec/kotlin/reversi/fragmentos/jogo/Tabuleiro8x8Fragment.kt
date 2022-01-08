package pt.isec.kotlin.reversi.fragmentos.jogo

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.dialog_fimjogo.view.*
import kotlinx.android.synthetic.main.dialog_noplays.view.*
import kotlinx.android.synthetic.main.fragment_tabuleiro8x8.view.*
import pt.isec.kotlin.reversi.R
import pt.isec.kotlin.reversi.Utils
import pt.isec.kotlin.reversi.atividades.JogoActivity
import pt.isec.kotlin.reversi.atividades.MainActivity
import pt.isec.kotlin.reversi.modelo.Constantes
import pt.isec.kotlin.reversi.modelo.Constantes.MAXCOL
import pt.isec.kotlin.reversi.modelo.Constantes.MAXLIN
import pt.isec.kotlin.reversi.modelo.Constantes.MODOJOGO
import pt.isec.kotlin.reversi.modelo.Reversi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Tabuleiro8x8Fragment : Fragment(), View.OnClickListener {
    var cont: Int = 0
    var x: Int = 0
    var y: Int = 0
    var adv1 = 0
    var tabul = Array(MAXLIN) { arrayOfNulls<ImageButton>(MAXLIN) }
    var x1t: Int = 0
    var y1t: Int = 0
    private val reversi: Reversi by activityViewModels()
    var colorSceme = IntArray(5)



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {


        val view = inflater.inflate(R.layout.fragment_tabuleiro8x8, container, false)


        //Atribuir tags e onclicks a todos os imageButtons do tabuleiro
        for (i in 0 until MAXLIN) {
            var v: View
            val layout: LinearLayout = view.Tabuleiro8x8Fragment.getChildAt(i) as LinearLayout
            for (j in 0 until layout.childCount) {
                v = layout.getChildAt(j)
                v.tag = "$i$j"
                (v as? ImageButton)?.setOnClickListener(this)
                tabul[i][j] = view.findViewWithTag("$i$j")
            }
        }


        checkTheme()
        setInitialPieceColor()
        resetTabuleiro()


        reversi.state.observe(activity!!) {
            mostraPecasTabuleiro()
            resetTabuleiro()
            if(MODOJOGO==2){
                if(reversi.state.value==Reversi.State.JOGAR){
                    mostraJogadasPossiveis()
                } else
                    resetTabuleiro()
            }else{
                mostraJogadasPossiveis()
            }
        }

        return view
    }




    override fun onClick(v: View) {
        val id: String = v.tag as String
        x = id.toInt() / 10
        y = id.toInt() % 10
        println("x ::::::::::::::::::::   $x")
        println("y ::::::::::::::::::::   $y")

        if(MODOJOGO==1)
            desenrolarJogo(x,y)
        else if(MODOJOGO==2)
            desenrolarJogoOnline(x, y)
    }

    fun desenrolarJogo(x : Int, y: Int) {
        if (reversi.jogadaTroca) {
            if (troca()) {
                cont++
                resetTabuleiro()
                mostraTrocas()
                reversi.inicializaTroca(x, y)
                if (cont == 1) {
                    Toast.makeText(activity, getString(R.string.chose2piece), Toast.LENGTH_LONG)
                        .show()
                }
                if (cont == 2) {
                    Toast.makeText(activity, getString(R.string.advpiece), Toast.LENGTH_LONG).show()
                }
                if (cont == 3) {
                    reversi.advEscolhido = reversi.tabulInteiros[x][y]
                    reversi.trocarPeca()
                    Toast.makeText(activity, getString(R.string.changesucced), Toast.LENGTH_LONG)
                        .show()
                    cont = 0
                    mostraPecasTabuleiro()
                    reversi.trocarJogador()
                    resetTabuleiro()
                    mostraJogadasPossiveis()
                    reversi.somarPontuacao()
                    reversi.jogadaTroca = false
                    checkPlays()
                }
            } else {
                Toast.makeText(activity, getString(R.string.impossibleposition), Toast.LENGTH_SHORT)
                    .show()

            }
        } else if ((reversi.tabulInteiros[x][y]) == 4) {
            if (reversi.verificaVizinhanca(x, y, true)) {
                if (reversi.jogadaBomba) {
                    pecaBomba(x, y)
                    Toast.makeText(activity, getString(R.string.bombneighbors), Toast.LENGTH_LONG)
                        .show()
                    reversi.jogadaBomba = false
                    reversi.trocarJogador()
                } else {
                        reversi.trocarJogador()
                }
                mostraPecasTabuleiro()
                resetTabuleiro()
                mostraJogadasPossiveis()
                reversi.somarPontuacao()
                checkPlays()

            }
        } else {
            if (reversi.jogadaBomba) {
                Toast.makeText(activity, getString(R.string.impossiblebomb), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        checkEnd()
    }
    fun desenrolarJogoOnline(x : Int, y: Int){
       if(reversi.state.value==Reversi.State.JOGAR){

           if (reversi.jogadaTroca) {
               if (trocaOnline()) {
                   cont++
                   resetTabuleiro()
                   mostraTrocasOnline()
                   reversi.inicializaTroca(x, y)
                   if (cont == 1) {
                       Toast.makeText(activity, getString(R.string.chose2piece), Toast.LENGTH_LONG)
                           .show()
                   }
                   if (cont == 2) {
                       Toast.makeText(activity, getString(R.string.advpiece), Toast.LENGTH_LONG).show()
                   }
                   if (cont == 3) {
                       reversi.advEscolhido = reversi.tabulInteiros[x][y]
                       reversi.trocarPeca()
                       Toast.makeText(activity, getString(R.string.changesucced), Toast.LENGTH_LONG)
                           .show()
                       cont = 0
                       reversi.enviarJogada()
                       reversi.state.postValue(Reversi.State.ESPERA_JOGAR)
                       reversi.jogadaTroca = false
                       checkPlays()
                   }
               } else {
                   Toast.makeText(activity, getString(R.string.impossibleposition), Toast.LENGTH_SHORT)
                       .show()
               }
           } else if ((reversi.tabulInteiros[x][y]) == 4) {
               if (reversi.verificaVizinhanca(x, y, true)) {
                   if (reversi.jogadaBomba) {
                       pecaBomba(x, y)
                       Toast.makeText(
                           activity,
                           getString(R.string.bombneighbors),
                           Toast.LENGTH_LONG
                       )
                           .show()
                       reversi.jogadaBomba = false
                   }
                       reversi.enviarJogada()
                       reversi.state.postValue(Reversi.State.ESPERA_JOGAR)
                       checkPlays()


               }
           } else {
               if (reversi.jogadaBomba) {
                   Toast.makeText(activity, getString(R.string.impossiblebomb), Toast.LENGTH_SHORT)
                       .show()
               }
           }

           checkEnd()
       }
        reversi.somarPontuacao()


    }
    fun troca(): Boolean {
           if(reversi.tabulInteiros[x][y] == reversi.jogadorAtual && cont==0){
                    x1t = x
                    y1t= y
                    return true
           }else if( cont==1 && reversi.tabulInteiros[x][y]== reversi.jogadorAtual){
               if(x!=x1t || y!=y1t)
                    return true
          }else if (reversi.tabulInteiros[x][y] == adv1 && cont==2){
                    return true
          }
            return false
        }

    fun mostraTrocas() {
        if (reversi.jogadorAtual == 1) {
                    adv1 = 2
                } else if (reversi.jogadorAtual == 2) {
                    adv1 = 1
                }
                for (i in 0 until MAXLIN) {
                    for (j in 0 until MAXCOL) {
                        if (cont < 2) {
                            if (reversi.tabulInteiros[i][j] == reversi.jogadorAtual) {
                                tabul[i][j]!!.setBackgroundResource(R.color.beje)
                            }
                        } else if (cont == 2) {
                            if (reversi.tabulInteiros[i][j] == adv1) {
                            tabul[i][j]!!.setBackgroundResource(R.color.beje)
                            }
                        }
                    }
                }
                if(cont==1)
                   tabul[x1t][y1t]!!.setBackgroundResource(R.color.selecionado)
    }

    fun trocaOnline(): Boolean {
        if(reversi.tabulInteiros[x][y] == reversi.idJogador && cont==0){
            x1t = x
            y1t= y
            return true
        }else if( cont==1 && reversi.tabulInteiros[x][y]== reversi.idJogador){
            if(x!=x1t || y!=y1t)
                return true
        }else if (reversi.tabulInteiros[x][y] == adv1 && cont==2){
            return true
        }
        return false
    }

    fun mostraTrocasOnline() {
        if (reversi.idJogador == 1) {
            adv1 = 2
        } else if (reversi.idJogador == 2) {
            adv1 = 1
        }
        for (i in 0 until MAXLIN) {
            for (j in 0 until MAXCOL) {
                if (cont < 2) {
                    if (reversi.tabulInteiros[i][j] == reversi.idJogador) {
                        tabul[i][j]!!.setBackgroundResource(R.color.beje)
                    }
                } else if (cont == 2) {
                    if (reversi.tabulInteiros[i][j] == adv1) {
                        tabul[i][j]!!.setBackgroundResource(R.color.beje)
                    }
                }
            }
        }
        if(cont==1)
            tabul[x1t][y1t]!!.setBackgroundResource(R.color.selecionado)
    }


    fun mostraPecasTabuleiro() {
        for (i in 0 until MAXLIN) {
            for (j in 0 until MAXCOL) {
                when (reversi.tabulInteiros[i][j]) {
                    0 -> tabul[i][j]!!.setImageResource(android.R.color.transparent)
                    1 -> tabul[i][j]!!.setImageResource(Constantes.pecaJog1)
                    2 -> tabul[i][j]!!.setImageResource(Constantes.pecaJog2)
                    3 -> tabul[i][j]!!.setImageResource(Constantes.pecaJog3)
                }
            }
        }
    }

    fun pecaBomba(x: Int, y: Int) {
        reversi.pecaBomba(x, y)
        for (i in 0 until MAXLIN) {
            for (j in 0 until MAXCOL) {
                when (reversi.tabulInteiros[i][j]) {
                    0 -> tabul[i][j]!!.setImageResource(android.R.color.transparent)
                    1 -> tabul[i][j]!!.setImageResource(Constantes.pecaJog1)
                    2 -> tabul[i][j]!!.setImageResource(Constantes.pecaJog2)
                    3 -> tabul[i][j]!!.setImageResource(Constantes.pecaJog3)
                }
            }
        }
    }

    fun checkPlays() {
        if (!reversi.verificaFimJogo()) {

            var count = 0
            for (i in 0 until MAXLIN) {
                for (j in 0 until MAXCOL) {
                    if (reversi.tabulInteiros[i][j] == 4) {
                        count++
                    }
                }
            }
            if(count != 0)
                return

            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder((activity as JogoActivity))
            val dialogView: View = layoutInflater.inflate(R.layout.dialog_noplays, null)
            dialogBuilder.setView(dialogView)
            val alertDialog: AlertDialog = dialogBuilder.create()
            alertDialog.setTitle(getString(R.string.play))
            alertDialog.show()
            dialogView.Btn_ok.setOnClickListener() {
                reversi.trocarJogador()
                mostraJogadasPossiveis()
                alertDialog.dismiss()
            }
        }
    }

    fun checkTheme() {
        colorSceme = Utils.getTableColors(activity as JogoActivity)
        Constantes.pecaJog1 = colorSceme[0]
        Constantes.pecaJog2 = colorSceme[1]
        Constantes.pecaJog3 = colorSceme[2]
        Constantes.tabColor = colorSceme[3]
        Constantes.tabSubColor = colorSceme[4]
    }

    fun setInitialPieceColor() {
        tabul[3][3]!!.setImageResource(Constantes.pecaJog1)
        tabul[3][4]!!.setImageResource(Constantes.pecaJog2)
        tabul[4][3]!!.setImageResource(Constantes.pecaJog2)
        tabul[4][4]!!.setImageResource(Constantes.pecaJog1)
    }


    fun mostraJogadasPossiveis() {

        (activity as JogoActivity?)?.reversi?.mostrarJogadasPossiveis()
        for (i in 0 until MAXLIN) {
            for (j in 0 until MAXCOL) {
                if (reversi.tabulInteiros[i][j] == 4) {
                    if (Utils.getMostrarJogadas(activity as JogoActivity)) {
                        tabul[i][j]!!.setBackgroundResource(Constantes.tabSubColor)
                    }
                }
            }
        }
    }

    fun resetTabuleiro() {
        for (i in 0 until MAXLIN)
            for (j in 0 until MAXCOL) {
                tabul[i][j]!!.setBackgroundResource(Constantes.tabColor)
                if (reversi.tabulInteiros[i][j] == 4)
                    reversi.tabulInteiros[i][j] = 0
            }
    }


    fun checkEnd() {

        if ((activity as JogoActivity?)?.reversi?.verificaFimJogo() == true) {

            val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime.now()
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            val formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm")
            val formatted = current.format(formatter)
            if(MODOJOGO == 2) {
                //   if(reversi.pontJogador1 > reversi.pontJogador2) {
                val db = Firebase.firestore
                val email = FirebaseAuth.getInstance().currentUser?.email
                val score = hashMapOf(
                    "AdvsPieces" to reversi.pontJogador2,
                    "MyPieces" to reversi.pontJogador1,
                    "NomeAdvs" to context!!.getString(R.string.nomeJog2)
                )
                if (email != null) {
                    db.collection("Jogadores")
                        .document(email)
                        .collection("Scores")
                        .document("Score_$formatted")
                        .set(score)
                }
                println("Score Adicionado!")
                //  }
            }

            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder((activity as JogoActivity))
            val dialogView: View = layoutInflater.inflate(R.layout.dialog_fimjogo, null)
            dialogBuilder.setView(dialogView)


            val alertDialog: AlertDialog = dialogBuilder.create()
            alertDialog.setTitle(getString(R.string.endgame))
            alertDialog.show()

            dialogView.vencedor.setText(reversi.getWinner((activity as JogoActivity)))

            dialogView.Btn_sair2.setOnClickListener() {
                val intent = Intent((activity as JogoActivity), MainActivity::class.java)
                startActivity(intent)
                alertDialog.dismiss()
            }
            dialogView.Btn_jogarNovamente.setOnClickListener() {
                val intent = Intent((activity as JogoActivity), JogoActivity::class.java)
                startActivity(intent)
                alertDialog.dismiss()
            }
        }
    }


}