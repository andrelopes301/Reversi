package pt.isec.kotlin.reversi.fragmentos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import pt.isec.kotlin.reversi.R
import pt.isec.kotlin.reversi.Utils
import pt.isec.kotlin.reversi.atividades.JogoActivity
import pt.isec.kotlin.reversi.atividades.MainActivity
import pt.isec.kotlin.reversi.atividades.ModoJogoActivity
import pt.isec.kotlin.reversi.databinding.FragmentModoJogoBinding
import pt.isec.kotlin.reversi.modelo.Constantes


class ModoJogoFragment : Fragment() {


    lateinit var b: FragmentModoJogoBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        b = FragmentModoJogoBinding.inflate(inflater)


        b.voltarAtras.setOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }

        b.BtnModo1.setOnClickListener {
            Constantes.MAXLIN = 8; Constantes.MAXCOL = 8
            Constantes.MODOJOGO = 1

            val intent = Intent(activity, JogoActivity::class.java)
            startActivity(intent)
        }

        b.BtnModo2.setOnClickListener {
            Constantes.MAXLIN = 8; Constantes.MAXCOL = 8
            Constantes.MODOJOGO = 2
            findNavController().navigate(R.id.action_modoJogoFragment_to_modoComunicacao)
        }
        b.BtnModo3.setOnClickListener {
            Constantes.MAXLIN = 10; Constantes.MAXCOL = 10
            Constantes.MODOJOGO = 3

            //MODO DE JOGO 3 NAO FUNCIONA PARA ONLINE
            //findNavController().navigate(R.id.action_modoJogoFragment_to_modoComunicacao)
            val intent = Intent(activity, JogoActivity::class.java)
            startActivity(intent)
        }


        setThemeHelper()
        return b.root
    }

    fun setThemeHelper(){
            val tema = Utils.setTema((activity as ModoJogoActivity))
            when (tema){
                1-> {
                    b.voltarAtrasText.setBackgroundResource(R.color.btnClassico)
                    b.voltarAtrasIMG.setBackgroundResource(R.color.btnClassico)
                }
                2->{
                    b.voltarAtrasText.setBackgroundResource(R.color.btnOceano)
                    b.voltarAtrasIMG.setBackgroundResource(R.color.btnOceano)
                }
                3->{
                    b.voltarAtrasText.setBackgroundResource(R.color.btnFrio)
                    b.voltarAtrasIMG.setBackgroundResource(R.color.btnFrio)
                }
                else->{
                    b.voltarAtrasText.setBackgroundResource(R.color.btnClassico)
                    b.voltarAtrasIMG.setBackgroundResource(R.color.btnClassico)
                }
            }
        }




}