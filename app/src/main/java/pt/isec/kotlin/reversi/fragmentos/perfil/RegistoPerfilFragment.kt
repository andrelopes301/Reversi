package pt.isec.kotlin.reversi.fragmentos.perfil

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_registo_perfil.*
import pt.isec.kotlin.reversi.R
import pt.isec.kotlin.reversi.atividades.MainActivity
import pt.isec.kotlin.reversi.atividades.PerfilActivity
import pt.isec.kotlin.reversi.databinding.FragmentRegistoPerfilBinding
import java.util.*


class RegistoPerfilFragment : Fragment() {

    lateinit var b: FragmentRegistoPerfilBinding
    lateinit var nomeJogador: String
    lateinit var email: String
    var password: String = "1234567890"

    lateinit var nomeEditText: EditText
    lateinit var emailEditText: EditText
    lateinit var passwordEditText: EditText

    lateinit var termos: CheckBox
    lateinit var spinner: Spinner
    lateinit var tipoRegisto: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = FragmentRegistoPerfilBinding.inflate(layoutInflater)

        val bundle = arguments
        if (bundle != null)
            tipoRegisto = bundle.getString("tipoRegisto")!!


        b.BtnConfirmarPerfil.setOnClickListener {
            nomeJogador = b.textIdNomeJogador.text.toString()
            termos = b.checkboxIdTermos

            if (TextUtils.isEmpty(nomeJogador)) {
                b.textIdNomeJogador.requestFocus()
                b.textIdNomeJogador.error = getString(R.string.name_required)
                return@setOnClickListener
            }

            if (nomeJogador.length > 15) {
                b.textIdNomeJogador.requestFocus()
                b.textIdNomeJogador.error = getString(R.string.username_incorrect)
                return@setOnClickListener
            }


            if (tipoRegisto != "google") {

                email = b.textIdEmail.text.toString()
                if (TextUtils.isEmpty(email)) {
                    b.textIdEmail.requestFocus()
                    b.textIdEmail.error = getString(R.string.emailrequired)
                    return@setOnClickListener
                }


                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    b.textIdEmail.requestFocus()
                    b.textIdEmail.error = getString(R.string.email_invalid)
                    return@setOnClickListener
                }


                password = b.textIdPassword.text.toString()
                if (TextUtils.isEmpty(password)) {
                    b.textIdPassword.requestFocus()
                    b.textIdPassword.error = getString(R.string.passwordrequired)
                    return@setOnClickListener
                }

                if (password.length < 6) {
                    b.textIdPassword.requestFocus()
                    b.textIdPassword.error = getString(R.string.password_invalid)
                    return@setOnClickListener
                }
            }

            if (spinner.selectedItemPosition == 0) {
                Toast.makeText(activity, getString(R.string.select_country), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (!termos.isChecked) {
                Toast.makeText(activity, getString(R.string.terms_required), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if(tipoRegisto != "alterardados"){
                if (!(activity as PerfilActivity).fotoTirada) {
                    Toast.makeText(
                        activity,
                        getString(R.string.photoRequired),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return@setOnClickListener
                }
        }

            val db = Firebase.firestore
            var imagem = ""

            //Perfil Criado

            email = b.textIdEmail.text.toString()
            println("EMAIL A11111 >>$email")

            if (!email.contains("@gmail.com") && tipoRegisto != "alterardados") {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(activity as PerfilActivity) { result ->
                        Log.i("REGISTO_EMAIL", "createUser: success")

                    }
                    .addOnFailureListener(activity as PerfilActivity) { e ->
                        Log.i("REGISTO_EMAIL", "createUser: failure ${e.message}")
                    }
            }



            if (FirebaseAuth.getInstance().currentUser != null) {
                db.collection("Jogadores").document(email)
                    .addSnapshotListener { docSS, e ->

                        if (e != null) {
                            return@addSnapshotListener
                        }
                        if (docSS != null && docSS.exists()) {
                            imagem = docSS.getString("ImagemPerfilUrl").toString()
                        }
                    }
            }



            if (tipoRegisto == "alterardados") {
                atualizarDados(
                    email,
                    nomeJogador,
                    nacionalidadesSpinner.selectedItemPosition,
                    password
                )
            } else
                guardarJogadorFirebase(
                    nomeJogador,
                    email,
                    password,
                    imagem,
                    b.nacionalidadesSpinner.selectedItemPosition
                )


            if ((activity as PerfilActivity).fotoTirada)
                (activity as PerfilActivity).inserirImagemFireBaseStorage(email)


            (activity as PerfilActivity).fotoTirada = false

            Toast.makeText(activity, getString(R.string.profileSucess), Toast.LENGTH_SHORT).show()
            val intent = Intent(activity, MainActivity::class.java)
            startActivity(intent)
        }


        b.BtnApagarDados.setOnClickListener {

            val nomeJogador = b.textIdNomeJogador.text.toString()
            if (!TextUtils.isEmpty(nomeJogador))
                b.textIdNomeJogador.text.clear()



            if (tipoRegisto == "alterardados") {

                b.textIdEmail.isEnabled = false


                if (!b.textIdEmail.text.contains("@gmail.com")) {

                    val password = b.textIdPassword.text.toString()
                    if (!TextUtils.isEmpty(password))
                        b.textIdPassword.text.clear()
                }

            }

            if (termos.isChecked)
                b.checkboxIdTermos.isChecked = false

            if (spinner.selectedItemPosition != 0)
                spinner.setSelection(0)

            b.BtnApagarDados.isEnabled = false

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {


        nomeEditText = b.textIdNomeJogador
        emailEditText = b.textIdEmail
        passwordEditText = b.textIdPassword

        termos = b.checkboxIdTermos
        spinner = b.nacionalidadesSpinner
        (activity as PerfilActivity).spinnerCountries(spinner)

        if (FirebaseAuth.getInstance().currentUser != null) {

            val email = FirebaseAuth.getInstance().currentUser?.email
            emailEditText.setText(email)
            emailEditText.isEnabled = false

            println("EMAIL >>> $email")
            if (email!!.contains("@gmail.com")) {
                passwordEditText.setText("0000000000")
                passwordEditText.isEnabled = false
            }

            val db = Firebase.firestore
            db.collection("Jogadores").document(FirebaseAuth.getInstance().currentUser?.email!!)
                .addSnapshotListener { docSS, e ->

                    if (e != null) {
                        return@addSnapshotListener
                    }
                    if (docSS != null && docSS.exists()) {
                        val nome = docSS.getString("Nome")
                        nomeEditText.setText(nome)

                        val password = docSS.getString("Password")
                        if (!email.contains("@gmail.com"))
                            passwordEditText.setText(password)


                        val nacionalidade = docSS.getLong("Nacionalidade")
                        if (nacionalidade != null)
                            spinner.setSelection(nacionalidade.toInt())

                    }
                }
        }


        return b.root
    }

    override fun onResume() {
        super.onResume()

        nomeEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {
                val nomeJogador = b.textIdNomeJogador.text.toString()
                b.BtnApagarDados.isEnabled = !TextUtils.isEmpty(nomeJogador)
            }
        })

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int,
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                val textEmail = b.textIdEmail.text.toString()
                b.BtnApagarDados.isEnabled = !TextUtils.isEmpty(textEmail)
            }
        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int,
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                val textPassword = b.textIdPassword.text.toString()
                b.BtnApagarDados.isEnabled = !TextUtils.isEmpty(textPassword)
            }
        })




        if (spinner.selectedItemPosition != 0) {
            b.BtnApagarDados.isEnabled = true
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                if (spinner.selectedItemPosition != 0) {
                    b.BtnApagarDados.isEnabled = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        termos.setOnCheckedChangeListener { _, _ ->
            if (termos.isChecked)
                b.BtnApagarDados.isEnabled = true
        }


    }


    fun guardarJogadorFirebase(
        nomeJogador: String,
        email: String,
        password: String,
        imagemPerfilUrl: String,
        nacionalidade: Int,
    ) {

        val db = Firebase.firestore
        val jogador = hashMapOf(
            "Nome" to nomeJogador,
            "E-mail" to email,
            "Password" to password,
            "ImagemPerfilUrl" to imagemPerfilUrl,
            "Nacionalidade" to nacionalidade
        )

        db.collection("Jogadores").document(email).set(jogador)
    }


    fun atualizarDados(email: String, nomeJogador: String, nacionalidade: Int, password: String) {

        val db = Firebase.firestore
        val jogador = db.collection("Jogadores").document(email)

        jogador.get(Source.SERVER)
            .addOnSuccessListener {
                jogador.update("Nome", nomeJogador)

                jogador.update("Nome", nomeJogador)
                if (email.contains("@gmail.com"))
                    jogador.update("Password", password)

                jogador.update("Nacionalidade", nacionalidade)
            }

    }


}




