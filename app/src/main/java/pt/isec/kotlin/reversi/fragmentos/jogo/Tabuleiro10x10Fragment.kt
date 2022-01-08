package pt.isec.kotlin.reversi.fragmentos.jogo

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.dialog_fimjogo.view.*
import kotlinx.android.synthetic.main.dialog_noplays.view.*
import kotlinx.android.synthetic.main.fragment_tabuleiro10x10.view.*
import pt.isec.kotlin.reversi.R
import pt.isec.kotlin.reversi.Utils
import pt.isec.kotlin.reversi.atividades.JogoActivity
import pt.isec.kotlin.reversi.modelo.Constantes
import pt.isec.kotlin.reversi.modelo.Constantes.MAXCOL
import pt.isec.kotlin.reversi.modelo.Constantes.MAXLIN
import pt.isec.kotlin.reversi.modelo.Reversi

class Tabuleiro10x10Fragment : Fragment(), View.OnClickListener {

    var x: Int = 0
    var y: Int = 0
    var cont: Int = 0
    var adv1 = 0
    var adv2 = 0
    var tabul = Array(MAXLIN) { arrayOfNulls<ImageButton>(MAXLIN) }
    private val reversi: Reversi by activityViewModels()
    var colorSceme = IntArray(5)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {



        val view = inflater.inflate(R.layout.fragment_tabuleiro10x10, container, false)
        //Atribuir tags e onclicks a todos os imageButtons do tabuleiro
        for (i in 0 until MAXLIN) {
            var v: View
            val layout: LinearLayout = view.Tabuleiro10x10Fragment.getChildAt(i) as LinearLayout
            for (j in 0 until layout.childCount) {
                v = layout.getChildAt(j)
                v.tag = "$i$j"
                (v as? ImageButton)?.setOnClickListener(this)
                tabul[i][j] = view.findViewWithTag("$i$j")
            }
        }



        checkTheme()
        resetTabuleiro()
        setInitialPieceColor()


        reversi.state.observe(activity!!) {
            mostraPecasTabuleiro()
            resetTabuleiro()
            mostraJogadasPossiveis()
        }

        return view
    }

    override fun onClick(v: View) {
        val id: String = v.tag as String
        x = id.toInt() / 10
        y = id.toInt() % 10
        println("x ::::::::::::::::::::   $x")
        println("y ::::::::::::::::::::   $y")

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
            }
        } else {
            if (reversi.jogadaBomba) {
                Toast.makeText(activity, getString(R.string.impossiblebomb), Toast.LENGTH_SHORT)
                    .show()
            }
        }
        reversi.mostraTabuleiro()
        checkPlays()
        checkEnd()
    }

    fun troca(): Boolean {
        if (cont < 2) {
            if ((reversi.tabulInteiros[x][y]) == reversi.jogadorAtual)
                return true
        } else {
            if ((reversi.tabulInteiros[x][y]) == adv1 || (reversi.tabulInteiros[x][y]) == adv2)
                return true
        }
        return false
    }

    fun mostraTrocas(){

        if(reversi.jogadorAtual==1){
            adv1=2
            adv2=3
        }else if(reversi.jogadorAtual==2){
            adv1=1
            adv2=3
        }else if(reversi.jogadorAtual==3){
            adv1=1
            adv2=2
        }
        for(i in 0 until MAXLIN){
            for(j in 0 until MAXLIN){
                if(cont==0 || cont==1){
                    if(reversi.tabulInteiros[i][j] == reversi.jogadorAtual) {
                        tabul[i][j]!!.setBackgroundResource(R.color.beje)
                    }
                }else if(cont==2){
                    if(reversi.tabulInteiros[i][j] == adv1 || reversi.tabulInteiros[i][j] == adv2) {
                        tabul[i][j]!!.setBackgroundResource(R.color.beje)
                    }
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

    fun checkTheme() {
        colorSceme = Utils.getTableColors(activity as JogoActivity)
        Constantes.pecaJog1 = colorSceme[0]
        Constantes.pecaJog2 = colorSceme[1]
        Constantes.pecaJog3 = colorSceme[2]
        Constantes.tabColor = colorSceme[3]
        Constantes.tabSubColor = colorSceme[4]
    }

    fun setInitialPieceColor() {

        println("MAXLIN >>>> "+ MAXLIN)
        println("MAXLIN >>>> "+ MAXCOL)


        tabul[2][4]!!.setImageResource(Constantes.pecaJog1)
        tabul[3][5]!!.setImageResource(Constantes.pecaJog1)
        tabul[2][5]!!.setImageResource(Constantes.pecaJog2)
        tabul[3][4]!!.setImageResource(Constantes.pecaJog2)

        tabul[6][2]!!.setImageResource(Constantes.pecaJog3)
        tabul[7][3]!!.setImageResource(Constantes.pecaJog3)
        tabul[6][3]!!.setImageResource(Constantes.pecaJog1)
        tabul[7][2]!!.setImageResource(Constantes.pecaJog1)

        tabul[6][6]!!.setImageResource(Constantes.pecaJog2)
        tabul[7][7]!!.setImageResource(Constantes.pecaJog2)
        tabul[6][7]!!.setImageResource(Constantes.pecaJog3)
        tabul[7][6]!!.setImageResource(Constantes.pecaJog3)
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

    fun checkEnd() {

        if ((activity as JogoActivity?)?.reversi?.verificaFimJogo() == true) {
            println("FIMFIMFIMFIM")
            //TODO - verifica fim de jogo
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder((activity as JogoActivity))
            val dialogView: View = layoutInflater.inflate(R.layout.dialog_fimjogo, null)
            dialogBuilder.setView(dialogView)


            val alertDialog: AlertDialog = dialogBuilder.create()
            alertDialog.setTitle(getString(R.string.endgame))
            alertDialog.show()

            //   dialogView.vencedor.setText(reversi.getWinner((activity as JogoActivity)))

            dialogView.Btn_sair2.setOnClickListener() {
                (activity as JogoActivity).finish()
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