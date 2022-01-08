package pt.isec.kotlin.reversi.fragmentos.perfil

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.isec.kotlin.reversi.R
import pt.isec.kotlin.reversi.Utils
import pt.isec.kotlin.reversi.atividades.MainActivity
import pt.isec.kotlin.reversi.atividades.PerfilActivity
import pt.isec.kotlin.reversi.databinding.FragmentRegistarLoginBinding
import pt.isec.kotlin.reversi.modelo.Constantes.TAG


class RegistarLoginFragment : Fragment() {

    lateinit var b: FragmentRegistarLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setTema(activity as PerfilActivity)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {


        b = FragmentRegistarLoginBinding.inflate(inflater)
        b.BtnRegistarEmail.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("tipoRegisto", "email")
            findNavController().navigate(
                R.id.action_registarLoginFragment_to_registoPerfilFragment,
                bundle
            )
        }

        b.BtnLoginEmail.setOnClickListener {

            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity)
            val dialogView: View = layoutInflater.inflate(R.layout.dialog_login, null)
            dialogBuilder.setView(dialogView)


            val emailEditText = dialogView.findViewById<EditText>(R.id.emailEditTextDialog)
            val passwordEditText = dialogView.findViewById<EditText>(R.id.passwordEditTextDialog)


            val cancelarBtn =
                dialogView.findViewById<Button>(R.id.Btn_cancelar) as Button

            val iniciarSessaoBtn =
                dialogView.findViewById<Button>(R.id.Btn_iniciarSessao) as Button

            val alertDialog: AlertDialog = dialogBuilder.create()
            alertDialog.setTitle("Dados de Login")
            alertDialog.show()


            iniciarSessaoBtn.setOnClickListener {

                val email = emailEditText.text
                val password = passwordEditText.text

                if (!TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                        .matches() && !TextUtils.isEmpty(password)
                ) {


                    //Verificar a conta
                    (activity as PerfilActivity).auth.signInWithEmailAndPassword(
                        email.toString(),
                        password.toString()
                    )
                        .addOnSuccessListener(activity as PerfilActivity) { result ->
                            Toast.makeText(
                                activity,
                                getString(R.string.loginSucess),
                                Toast.LENGTH_SHORT
                            ).show()

                            alertDialog.dismiss()
                            findNavController().navigate(R.id.action_registarLoginFragment_to_visualizarPerfilFragment)
                        }
                        .addOnFailureListener(activity as PerfilActivity) { e ->
                            Toast.makeText(
                                activity,
                                getString(R.string.email_incorrect),
                                Toast.LENGTH_LONG
                            ).show()
                        }


                } else {

                    if (TextUtils.isEmpty(emailEditText.text)) {
                        emailEditText.requestFocus()
                        emailEditText.error = getString(R.string.emailrequired)
                        return@setOnClickListener
                    }

                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailEditText.text)
                            .matches()
                    ) {
                        emailEditText.requestFocus()
                        emailEditText.error = getString(R.string.email_invalid)
                        return@setOnClickListener
                    }


                    if (TextUtils.isEmpty(passwordEditText.text)) {
                        passwordEditText.requestFocus()
                        passwordEditText.error = getString(R.string.passwordrequired)
                        return@setOnClickListener
                    }

                }
            }

            cancelarBtn.setOnClickListener {
                alertDialog.dismiss()
            }
        }

        b.BtnLoginContaGoogle.setOnClickListener {
            (activity as PerfilActivity).googleSignInClient =
                GoogleSignIn.getClient(
                    (activity as PerfilActivity),
                    (activity as PerfilActivity).gso
                )
            (activity as PerfilActivity).googleSignInClient.signOut()
            signInWithGoogle.launch((activity as PerfilActivity).googleSignInClient.signInIntent)
        }

        return b.root;

    }


    private val signInWithGoogle = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)


        } catch (e: ApiException) {
            Log.i(TAG, "onActivityResult - Google authentication: failure")
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        (activity as PerfilActivity).auth.signInWithCredential(credential)
            .addOnSuccessListener(activity as PerfilActivity) { result ->
                Log.d(TAG, "signInWithCredential:success")

                val db = Firebase.firestore

                db.collection("Jogadores")
                    .document((activity as PerfilActivity).auth.currentUser?.email!!)
                    .addSnapshotListener { docSS, e ->
                        if (e != null) {
                            return@addSnapshotListener
                        }
                        if (docSS != null && docSS.exists()) {

                            Toast.makeText(
                                activity,
                                getString(R.string.login_sucess),
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(activity, MainActivity::class.java)
                            startActivity(intent)

                        } else {
                            val bundle = Bundle()
                            bundle.putString("tipoRegisto", "google")
                            findNavController().navigate(
                                R.id.action_registarLoginFragment_to_registoPerfilFragment,
                                bundle
                            )

                        }
                    }


            }
            .addOnFailureListener((activity as PerfilActivity)) { e ->
                Log.d(TAG, "signInWithCredential:failure ${e.message}")
            }
    }


}