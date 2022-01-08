package pt.isec.kotlin.reversi.fragmentos.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.isec.kotlin.reversi.R
import pt.isec.kotlin.reversi.Utils
import pt.isec.kotlin.reversi.atividades.PerfilActivity
import pt.isec.kotlin.reversi.databinding.FragmentVisualizarPerfilBinding

class VisualizarPerfilFragment : Fragment() {


    lateinit var b: FragmentVisualizarPerfilBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Utils.setTema(activity as PerfilActivity)
        b = FragmentVisualizarPerfilBinding.inflate(layoutInflater)


        b.BtnAlterarDados.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("tipoRegisto", "alterardados")

            findNavController().navigate(R.id.action_visualizarPerfilFragment_to_registoPerfilFragment,
                bundle)
        }


        b.BtnTerminarSessao.setOnClickListener {

            if ((activity as PerfilActivity).auth.currentUser != null) {
                (activity as PerfilActivity).auth.signOut()
            }

            Utils.resetImagemPerfil((activity as PerfilActivity), R.id.Imagem_Perfil)

            println("Autenticacao : ${(activity as PerfilActivity).auth}")
            findNavController().navigate(R.id.action_visualizarPerfilFragment_to_registarLogin)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        val db = Firebase.firestore
        (activity as PerfilActivity).spinnerCountries(b.nacionalidadesSpinner)

        val email = (activity as PerfilActivity).auth.currentUser?.email

        if (email != null) {
            db.collection("Jogadores").document(email)
                .addSnapshotListener { docSS, e ->

                    if (e != null) {
                        return@addSnapshotListener
                    }
                    if (docSS != null && docSS.exists()) {
                        val username = docSS.getString("Nome")
                        b.textIdNomeJogador.setText(username)

                        val eemail = docSS.getString("E-mail")
                        b.textIdEmail.setText(eemail)


                        val nacionalidade = docSS.getLong("Nacionalidade")
                        if (nacionalidade != null) {
                            b.nacionalidadesSpinner.setSelection(nacionalidade.toInt())
                            b.nacionalidadesSpinner.isEnabled = false
                        }

                        val imagem = docSS.getString("ImagemPerfilUrl")
                        println("IMAGEM : $imagem")
                        if (imagem == "")
                            return@addSnapshotListener

                        val imagemPerfil =
                            (activity as PerfilActivity).findViewById<ImageView>(R.id.Imagem_Perfil)

                        Glide.with((activity as PerfilActivity)).load(imagem).into(imagemPerfil)


                    }
                }
        }
        return b.root
    }

}