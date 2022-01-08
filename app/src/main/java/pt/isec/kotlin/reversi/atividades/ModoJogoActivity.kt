package pt.isec.kotlin.reversi.atividades

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.isec.kotlin.reversi.R
import pt.isec.kotlin.reversi.Utils
import android.widget.LinearLayout

class ModoJogoActivity : AppCompatActivity() {


    var tema : Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setFullscreen(this)
        tema = Utils.setTema(this)
        setContentView(R.layout.activity_modo_jogo)
        setBackgroundTheme()
    }
    fun setBackgroundTheme(){
        val mjl : LinearLayout = findViewById(R.id.modojogolayout)
        when (tema) {
                    1 -> {
                        mjl.setBackgroundResource(R.color.fundoClassico)
                    }
                    2 -> {
                        mjl.setBackgroundResource(R.color.fundoOceano)
                    }
                    3 -> {
                        mjl.setBackgroundResource(R.color.fundoFrio)
                    }
                }
    }



}