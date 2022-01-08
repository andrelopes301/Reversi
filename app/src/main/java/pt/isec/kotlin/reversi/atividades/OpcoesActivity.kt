package pt.isec.kotlin.reversi.atividades

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import pt.isec.kotlin.reversi.R
import pt.isec.kotlin.reversi.Utils
import pt.isec.kotlin.reversi.fragmentos.OpcoesFragment
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.View
import androidx.preference.Preference
import kotlinx.android.synthetic.main.dialog_about.view.*
import kotlinx.android.synthetic.main.dialog_fimjogo.view.*
import pt.isec.kotlin.reversi.Utils.Companion.sharedPreferences


class OpcoesActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(android.R.id.content, OpcoesFragment()).commit()
        //Utils.setFullscreen(this)
        Utils.setTema(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.opt)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)





    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
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

}