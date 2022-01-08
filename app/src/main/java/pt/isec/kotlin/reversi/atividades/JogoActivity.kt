package pt.isec.kotlin.reversi.atividades

import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import pt.isec.kotlin.reversi.R
import pt.isec.kotlin.reversi.Utils
import pt.isec.kotlin.reversi.databinding.ActivityJogoBinding
import pt.isec.kotlin.reversi.fragmentos.jogo.Score2playersFragment
import pt.isec.kotlin.reversi.fragmentos.jogo.Score3playersFragment
import pt.isec.kotlin.reversi.fragmentos.jogo.Tabuleiro10x10Fragment
import pt.isec.kotlin.reversi.fragmentos.jogo.Tabuleiro8x8Fragment
import pt.isec.kotlin.reversi.modelo.Constantes.CLIENT_MODE
import pt.isec.kotlin.reversi.modelo.Constantes.MODOJOGO
import pt.isec.kotlin.reversi.modelo.Constantes.SERVER_MODE
import pt.isec.kotlin.reversi.modelo.Constantes.SERVER_PORT
import pt.isec.kotlin.reversi.modelo.Reversi


class JogoActivity : AppCompatActivity() {

    val reversi: Reversi by viewModels()
    lateinit var b: ActivityJogoBinding
    private var dlg: AlertDialog? = null
    var tema: Int = 1
    var pressedTime: Long = 0
    lateinit var fragment: Fragment
    lateinit var btnBomba: LinearLayout
    lateinit var btnTroca: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        tema = Utils.setTema(this)
        super.onCreate(savedInstanceState)
        Utils.setFullscreen(this)


        b = ActivityJogoBinding.inflate(layoutInflater)

        setContentView(b.root)
        setThemeHelper(tema)




        if (MODOJOGO == 1 || MODOJOGO == 2) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.scoreFragmentFrameLayout, Score2playersFragment()).addToBackStack(null).commit()
            supportFragmentManager.beginTransaction()
                .replace(R.id.tabuleiroFragmentFrameLayout, Tabuleiro8x8Fragment()).addToBackStack(null).commit()

        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.scoreFragmentFrameLayout, Score3playersFragment()).addToBackStack(null).commit()
            supportFragmentManager.beginTransaction()
                .replace(R.id.tabuleiroFragmentFrameLayout, Tabuleiro10x10Fragment()).addToBackStack(null).commit()

        }


        btnBomba = findViewById(R.id.jogadaBomba)
        btnTroca = findViewById(R.id.jogadaTroca)


        reversi.state.observe(this) { state ->
            btnBomba.isEnabled =
                reversi.pecaBombaJog1 && reversi.jogadorAtual == 1 || reversi.pecaBombaJog2 && reversi.jogadorAtual == 2 || reversi.pecaBombaJog3 && reversi.jogadorAtual == 3
            btnBomba.isInvisible = !btnBomba.isEnabled
            btnTroca.isEnabled =
                reversi.pecaTrocaJog1 && reversi.jogadorAtual == 1 || reversi.pecaTrocaJog2 && reversi.jogadorAtual == 2 || reversi.pecaTrocaJog3 && reversi.jogadorAtual == 3
            btnTroca.isInvisible = !btnTroca.isEnabled
            if(MODOJOGO==2) {
                if (state == Reversi.State.ESPERA_JOGAR) {
                    btnBomba.isEnabled = false
                    btnTroca.isEnabled = false
                } else if (state == Reversi.State.JOGAR) {
                    btnBomba.isEnabled = true
                    btnTroca.isEnabled = true
                }
            }
        }



        if(MODOJOGO == 2) {

            reversi.connectionState.observe(this) { state ->

                if (state != Reversi.ConnectionState.SETTING_PARAMETERS &&
                    state != Reversi.ConnectionState.SERVER_CONNECTING
                ) {
                    if(dlg?.isShowing == true) {
                        dlg?.dismiss()
                        dlg = null
                    }
                }

                if (state == Reversi.ConnectionState.CONNECTION_ERROR) {
                    finish()
                }
                if (state == Reversi.ConnectionState.CONNECTION_ENDED)
                    finish()

                if (reversi.connectionState.value == Reversi.ConnectionState.SETTING_PARAMETERS ) {
                    when (intent.getIntExtra("modoJogo", SERVER_MODE)) {
                        SERVER_MODE -> startAsServer()
                        CLIENT_MODE -> startAsClient()
                    }
                }

            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }


    override fun onBackPressed() {

        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            reversi.stopGame()

        } else {
            Toast.makeText(baseContext, getString(R.string.backMSG), Toast.LENGTH_SHORT).show()
        }
        pressedTime = System.currentTimeMillis()

    }


    fun onTrocaPecas(view: View) {
        if(!reversi.jogadaBomba){
            if(reversi.pontJogador1>=4 && reversi.pontJogador2>=4 && (MODOJOGO==1||MODOJOGO==2) || reversi.pontJogador1>=4 && reversi.pontJogador2>=4 && reversi.pontJogador3>=4 && MODOJOGO==3){
                reversi.jogadaTroca = true
                Toast.makeText(this,
                    getString(R.string.choose2pieces),
                    Toast.LENGTH_LONG).show()

                if (MODOJOGO == 1 || MODOJOGO == 2) {

                    fragment = supportFragmentManager.findFragmentById(R.id.tabuleiroFragmentFrameLayout) as Tabuleiro8x8Fragment

                    (fragment as Tabuleiro8x8Fragment).resetTabuleiro()
                    (fragment as Tabuleiro8x8Fragment).mostraTrocas()
                } else {
                    fragment = supportFragmentManager.findFragmentById(R.id.tabuleiroFragmentFrameLayout) as Tabuleiro10x10Fragment
                    (fragment as Tabuleiro10x10Fragment).resetTabuleiro()
                    (fragment as Tabuleiro10x10Fragment).mostraTrocas()
                }

                if (reversi.jogadorAtual == 1) {
                    reversi.pecaTrocaJog1 = false
                } else if (reversi.jogadorAtual == 2) {
                    reversi.pecaTrocaJog2 = false
                } else if (reversi.jogadorAtual == 3) {
                    reversi.pecaTrocaJog3 = false
                }
            }else if(reversi.pontJogador1<4 || reversi.pontJogador2<4 || reversi.pontJogador3<4){
                Toast.makeText(this,getString(R.string.tradeunavailable),Toast.LENGTH_LONG).show()
            }
        }else if(reversi.jogadaBomba){
            Toast.makeText(this,getString(R.string.bombpieceactive),Toast.LENGTH_LONG).show()
        }
    }

    fun onPecaBomba(view: View) {

        if(!reversi.jogadaTroca){
            if(reversi.pontJogador1>=4 && reversi.pontJogador2>=4 && (MODOJOGO==1||MODOJOGO==2) || reversi.pontJogador1>=4 && reversi.pontJogador2>=4 && reversi.pontJogador3>=4 && MODOJOGO==3){
                reversi.jogadaBomba = true
                Toast.makeText(this, getString(R.string.chooseBombLocation), Toast.LENGTH_SHORT).show()
                if (reversi.jogadorAtual == 1) {
                    reversi.pecaBombaJog1 = false
                } else if (reversi.jogadorAtual == 2) {
                    reversi.pecaBombaJog2 = false
                } else if (reversi.jogadorAtual == 3) {
                    reversi.pecaBombaJog3 = false
                }
            }
            else if (reversi.pontJogador1<4 || reversi.pontJogador2<4 || reversi.pontJogador3<4){
                Toast.makeText(this,getString(R.string.bombnotpossible),Toast.LENGTH_LONG).show()
            }
        }else if(reversi.jogadaTroca){
            Toast.makeText(this,getString(R.string.tradePieceActive),Toast.LENGTH_LONG).show()
        }

    }


    private fun startAsServer() {
        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip =
            wifiManager.connectionInfo.ipAddress // Deprecated in API Level 31. Suggestion NetworkCallback
        val strIPAddress = String.format("%d.%d.%d.%d",
            ip and 0xff,
            (ip shr 8) and 0xff,
            (ip shr 16) and 0xff,
            (ip shr 24) and 0xff
        )

        val ll = LinearLayout(this).apply {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            this.setPadding(50, 50, 50, 50)
            layoutParams = params

            when(tema){
                1->setBackgroundResource(R.color.fundoClassico)
                2->setBackgroundResource(R.color.fundoOceano)
                3->setBackgroundResource(R.color.fundoFrio)

            }

            orientation = LinearLayout.HORIZONTAL
            addView(ProgressBar(context).apply {
                isIndeterminate = true
                val paramsPB = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
                paramsPB.gravity = Gravity.CENTER_VERTICAL
                layoutParams = paramsPB
                indeterminateTintList = ColorStateList.valueOf(Color.rgb(96, 96, 32))
            })
            addView(TextView(context).apply {
                val paramsTV = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams = paramsTV
                text = String.format(getString(R.string.msg_ip_address), strIPAddress)
                textSize = 20f
                setTextColor(Color.rgb(96, 96, 32))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            })
        }

        dlg = AlertDialog.Builder(this).run {
            setTitle(getString(R.string.servermode))
            setView(ll)
            setOnCancelListener {
                reversi.stopServer()
                finish()
            }
            create()
        }
        reversi.startServer()
        dlg?.show()
    }


    private fun startAsClient() {
        val edtBox = EditText(this).apply {
            maxLines = 1
            filters = arrayOf(object : InputFilter {
                override fun filter(
                    source: CharSequence?,
                    start: Int,
                    end: Int,
                    dest: Spanned?,
                    dstart: Int,
                    dend: Int,
                ): CharSequence? {
                    source?.run {
                        var ret = ""
                        forEach {
                            if (it.isDigit() || it.equals('.'))
                                ret += it
                        }
                        return ret
                    }
                    return null
                }

            })
        }
        val dlg = AlertDialog.Builder(this).run {
            setTitle(getString(R.string.clientmode))
            setMessage(getString(R.string.ipadress))
            setPositiveButton(getString(R.string.connect)) { _: DialogInterface, _: Int ->
                val strIP = edtBox.text.toString()
                if (strIP.isEmpty() || !Patterns.IP_ADDRESS.matcher(strIP).matches()) {
                    Toast.makeText(this@JogoActivity, getString(R.string.adressincorrect), Toast.LENGTH_LONG)
                        .show()
                    finish()
                } else {
                    reversi.startClient(edtBox.text.toString())
                }
            }
            setNeutralButton(getString(R.string.connectEmulator)) { _: DialogInterface, _: Int ->
                reversi.startClient("10.0.2.2", SERVER_PORT - 1)
                // Configure port redirect on the Server Emulator:
                // telnet localhost <5554|5556|5558|...>
                // auth <key>
                // redir add tcp:9998:9999
            }
            setNegativeButton(getString(R.string.cancel1)) { _: DialogInterface, _: Int ->
                finish()
            }
            setCancelable(false)
            setView(edtBox)
            create()
        }
        println("qqqq")
        dlg.show()
    }


    fun setThemeHelper(tema: Int) {
        when (tema) {
            1 -> {
                b.jogadaBomba.setBackgroundResource(R.color.btnClassico)
                b.bombaIMG.setImageResource(R.drawable.bomba_classico)
                b.bombaTXT.setTextColor(Color.parseColor("#CFCBC9"))
                b.bombaTXT.setBackgroundResource(R.color.btnClassico)
                b.jogadaTroca.setBackgroundResource(R.color.btnClassico)
                b.trocaIMG.setImageResource(R.drawable.change_classico)
                b.trocaTXT.setTextColor(Color.parseColor("#CFCBC9"))
                b.trocaTXT.setBackgroundResource(R.color.btnClassico)
            }
            2 -> {
                b.jogadaBomba.setBackgroundResource(R.color.btnOceano)
                b.bombaIMG.setImageResource(R.drawable.bomba_oceano)
                b.bombaTXT.setTextColor(Color.parseColor("#C7E6E0"))
                b.bombaTXT.setBackgroundResource(R.color.btnOceano)
                b.jogadaTroca.setBackgroundResource(R.color.btnOceano)
                b.trocaIMG.setImageResource(R.drawable.change_oceano)
                b.trocaTXT.setTextColor(Color.parseColor("#C7E6E0"))
                b.trocaTXT.setBackgroundResource(R.color.btnOceano)
            }
            3 -> {
                b.jogadaBomba.setBackgroundResource(R.color.btnFrio)
                b.bombaIMG.setImageResource(R.drawable.bomba_frio)
                b.bombaTXT.setTextColor(Color.parseColor("#AEAEAE"))
                b.bombaTXT.setBackgroundResource(R.color.btnFrio)
                b.jogadaTroca.setBackgroundResource(R.color.btnFrio)
                b.trocaIMG.setImageResource(R.drawable.change_frio)
                b.trocaTXT.setTextColor(Color.parseColor("#AEAEAE"))
                b.trocaTXT.setBackgroundResource(R.color.btnFrio)
            }
        }
    }


}
