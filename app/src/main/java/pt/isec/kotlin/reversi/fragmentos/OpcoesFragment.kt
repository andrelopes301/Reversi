package pt.isec.kotlin.reversi.fragmentos

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.LinearLayout
import androidx.preference.DialogPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.android.synthetic.main.dialog_about.view.*
import pt.isec.kotlin.reversi.R
import pt.isec.kotlin.reversi.Utils
import pt.isec.kotlin.reversi.atividades.JogoActivity
import pt.isec.kotlin.reversi.atividades.OpcoesActivity

class OpcoesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.opcoes, rootKey)


        val myPref = findPreference("about") as Preference?

        myPref!!.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {

            override fun onPreferenceClick(preference: Preference?): Boolean {

                val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder((activity as OpcoesActivity))
                val dialogView: View = layoutInflater.inflate(R.layout.dialog_about, null)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
               // dialogView.setPadding(50, 50, 50, 50)
                dialogView.layoutParams = params

                dialogBuilder.setView(dialogView)
                val alertDialog: AlertDialog = dialogBuilder.create()

              //  alertDialog.setTitle("About")


                alertDialog.show()



                return true
            }
        }
    }


}