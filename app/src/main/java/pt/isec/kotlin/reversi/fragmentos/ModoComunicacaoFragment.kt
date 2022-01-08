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
import pt.isec.kotlin.reversi.atividades.ModoJogoActivity
import pt.isec.kotlin.reversi.databinding.FragmentModoComunicacaoBinding
import pt.isec.kotlin.reversi.modelo.Constantes.CLIENT_MODE
import pt.isec.kotlin.reversi.modelo.Constantes.SERVER_MODE

class ModoComunicacaoFragment : Fragment() {


    lateinit var b: FragmentModoComunicacaoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        b = FragmentModoComunicacaoBinding.inflate(inflater)

        b.voltarAtras.setOnClickListener {
            findNavController().navigate(R.id.action_modoComunicacao_to_modoJogoFragment)
        }

        b.BtnModoServidor.setOnClickListener {
            iniciarJogo(SERVER_MODE)
        }

        b.BtnModoCliente.setOnClickListener {
            iniciarJogo(CLIENT_MODE)
        }
        setThemeHelper()
        return b.root;

    }

    fun iniciarJogo(modoJogo: Int) {
        val intent = Intent(activity, JogoActivity::class.java).apply {
            putExtra("modoJogo", modoJogo)
        }
        startActivity(intent)
    }

    fun setThemeHelper(){
        val tema = Utils.setTema((activity as ModoJogoActivity))
        when (tema){
            1-> {
                b.voltarAtrasTXT.setBackgroundResource(R.color.btnClassico)
                b.voltarAtrasIMG.setBackgroundResource(R.color.btnClassico)
            }
            2->{
                b.voltarAtrasTXT.setBackgroundResource(R.color.btnOceano)
                b.voltarAtrasIMG.setBackgroundResource(R.color.btnOceano)
            }
            3->{
                b.voltarAtrasTXT.setBackgroundResource(R.color.btnFrio)
                b.voltarAtrasIMG.setBackgroundResource(R.color.btnFrio)
            }
            else->{
                b.voltarAtrasTXT.setBackgroundResource(R.color.btnClassico)
                b.voltarAtrasIMG.setBackgroundResource(R.color.btnClassico)
            }
        }
    }

}