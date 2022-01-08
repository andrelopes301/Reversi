package pt.isec.kotlin.reversi.modelo


import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONObject
import pt.isec.kotlin.reversi.R
import pt.isec.kotlin.reversi.modelo.Constantes.MAXCOL
import pt.isec.kotlin.reversi.modelo.Constantes.MAXLIN
import pt.isec.kotlin.reversi.modelo.Constantes.MODOJOGO
import pt.isec.kotlin.reversi.modelo.Constantes.SERVER_PORT
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread


class Reversi : ViewModel() {


    enum class State {
        INICIO, JOGAR, ESPERA_JOGAR, JOGADOR_3, GAME_OVER
    }

    enum class ConnectionState {
        SETTING_PARAMETERS, SERVER_CONNECTING, CLIENT_CONNECTING, CONNECTION_ESTABLISHED,
        CONNECTION_ERROR, CONNECTION_ENDED
    }

    private var socket: Socket? = null
    private val socketI: InputStream?
        get() = socket?.getInputStream()
    private val socketO: OutputStream?
        get() = socket?.getOutputStream()

    private var serverSocket: ServerSocket? = null
    private var threadComm: Thread? = null

    var idJogador : Int = 0
    var jogadorAtual: Int = 0
    var pontJogador1: Int
    var pontJogador2: Int
    var pontJogador3: Int
    var pecaBombaJog1: Boolean
    var pecaBombaJog2: Boolean
    var pecaBombaJog3: Boolean
    var jogadaBomba: Boolean
    var pecaTrocaJog1: Boolean
    var pecaTrocaJog2: Boolean
    var pecaTrocaJog3: Boolean
    var jogadaTroca: Boolean
    var advEscolhido: Int
    var x: Int
    var y: Int
    var x1: Int
    var y1: Int
    var x2: Int
    var y2: Int
    var x3: Int
    var y3: Int
    var tabulInteiros = Array(0) { IntArray(0) }
    val state = MutableLiveData(State.INICIO)
    val connectionState = MutableLiveData(ConnectionState.SETTING_PARAMETERS)
    var jogadorSorteado : Int = 0
    lateinit var imagemJogador1 : String
    lateinit var nomeJogador1 : String
    var imagemJogador2 : String = ""
    var nomeJogador2 : String = ""

    //Inicialização dos Dados do Jogo/Jogadores
    init {
        x = 0; y = 0;x1 = 0;y1 = 0;x2 = 0;y2 = 0;x3 = 0;y3 = 0
        pontJogador1 = 2
        pontJogador2 = 2
        pontJogador3 = 2
        advEscolhido = 0
        jogadaBomba = false
        pecaBombaJog1 = true
        pecaBombaJog2 = true
        pecaBombaJog3 = true
        jogadaTroca = false
        pecaTrocaJog1 = true
        pecaTrocaJog2 = true
        pecaTrocaJog3 = true
        tabulInteiros = Array(MAXLIN) { IntArray(MAXCOL) }

        inicializarTabuleiro()

        sorteioJogador()
    }

    //Inicialização do Tabuleiro
    fun inicializarTabuleiro() {

        for (i in 0 until MAXLIN) {
            for (j in 0 until MAXCOL) {
                tabulInteiros[i][j] = 0
            }
        }

        if (MODOJOGO == 1 || MODOJOGO == 2) {
            tabulInteiros[3][3] = 1
            tabulInteiros[3][4] = 2
            tabulInteiros[4][3] = 2
            tabulInteiros[4][4] = 1

        } else if (MODOJOGO == 3) {
            tabulInteiros[2][4] = 1
            tabulInteiros[3][5] = 1
            tabulInteiros[2][5] = 2
            tabulInteiros[3][4] = 2

            tabulInteiros[6][2] = 3
            tabulInteiros[7][3] = 3
            tabulInteiros[6][3] = 1
            tabulInteiros[7][2] = 1

            tabulInteiros[6][6] = 2
            tabulInteiros[7][7] = 2
            tabulInteiros[6][7] = 3
            tabulInteiros[7][6] = 3
        }
    }

    fun trocarPeca() {
        tabulInteiros[x1][y1] = advEscolhido
        tabulInteiros[x2][y2] = advEscolhido
        tabulInteiros[x3][y3] = jogadorAtual
        x1 = 0;y1 = 0;x2 = 0;y2 = 0;x3 = 0;y3 = 0
        jogadaTroca = false
    }

    fun pecaBomba(x: Int, y: Int) {
        for (i in -1..1) {
            for (j in -1..1) {
                if (!limite(x + i, y + j))
                    tabulInteiros[x + i][y + j] = 0
            }
        }
    }

    fun inicializaTroca(x: Int, y: Int) {
        if (x1 == 0) {
            x1 = x;y1 = y
            return
        } else if (x2 == 0) {
            x2 = x;y2 = y
            return
        } else if (x3 == 0) {
            x3 = x;y3 = y
            return
        }
    }


    fun verificaVizinhanca(x: Int, y: Int, jogada: Boolean): Boolean {
        var valida = false

        for (i in -1..1) {
            for (j in -1..1) {
                if (limite(x + j, y + i)) {
                    continue
                }

                if (espacoVazio(x + j, y + i)) {
                    continue
                }
                if (pecaAdversaria(x + j, y + i)) {
                    if (verificaLinha(x, y, j, i, jogada)) {
                        valida = true
                        if(MODOJOGO==1 || MODOJOGO==3)
                            tabulInteiros[x][y] = jogadorAtual
                        else
                            tabulInteiros[x][y] = idJogador
                    }
                }
            }
        }

        return valida
    }


    fun verificaLinha(x: Int, y: Int, vx: Int, vy: Int, jogada: Boolean): Boolean {
        var valida = false
        var dx = x
        var dy = y

        while (true) {
            dx += vx
            dy += vy

            if (limite(dx, dy)) {
                break
            }
            if (espacoVazio(dx, dy)) {
                break
            }
            if (pecaAtual(dx, dy)) {
                troca(x, y, vx, vy, dx, dy, jogada)
                valida = true
                break
            }
        }
        return valida
    }


    fun troca(x: Int, y: Int, vx: Int, vy: Int, endx: Int, endy: Int, jogada: Boolean) {
        var dx = x
        var dy = y

        while (true) {
            dx += vx
            dy += vy
            if (dx == endx && dy == endy) {
                break
            }
            if (jogada){
                if(MODOJOGO==1 || MODOJOGO==3)
                    tabulInteiros[dx][dy] = jogadorAtual
                else
                    tabulInteiros[dx][dy] = idJogador
            }
        }
    }


    fun limite(x: Int, y: Int): Boolean {
        return x < 0 || x >= MAXLIN || y < 0 || y >= MAXCOL
    }

    fun pecaAtual(x: Int, y: Int): Boolean {
        if(MODOJOGO==1 || MODOJOGO==3)
            return tabulInteiros[x][y] == jogadorAtual
        else
            return tabulInteiros[x][y] == idJogador
    }

    fun espacoVazio(x: Int, y: Int): Boolean {
        return tabulInteiros[x][y] == 0
    }


    fun pecaAdversaria(x: Int, y: Int): Boolean {

        var adversario: Int = 0
        var adversario1: Int = 0

        println("MODO JOGO : $MODOJOGO")

        when (MODOJOGO) {
            1  -> {

                if (jogadorAtual == 1)
                    adversario = 2
                else
                    adversario = 1

                return tabulInteiros[x][y] == adversario
            }
            2 -> {
                if(idJogador==1)
                    adversario=2
                else
                    adversario=1
                return tabulInteiros[x][y]==adversario
            }
            3 -> {
                if (jogadorAtual == 1) {
                    adversario = 2
                    adversario1 = 3
                } else if (jogadorAtual == 2) {
                    adversario = 3
                    adversario1 = 1
                } else if (jogadorAtual == 3) {
                    adversario = 1
                    adversario1 = 2
                }

                return tabulInteiros[x][y] == adversario || tabulInteiros[x][y] == adversario1
            }

        }
        return false
    }


    fun mostrarJogadasPossiveis() {

        for (i in 0 until MAXLIN) {
            for (j in 0 until MAXCOL) {
                if (tabulInteiros[i][j] == 0) {
                    if (verificaVizinhanca(i, j, false)) {
                        tabulInteiros[i][j] = 4
                    }
                }
            }
        }
    }

    fun trocarJogador() {

        when (MODOJOGO) {
            1, 2 -> {
                when (state.value) {
                    State.JOGAR -> {
                        state.postValue(State.ESPERA_JOGAR); jogadorAtual = 2
                    }
                    State.ESPERA_JOGAR -> {
                        state.postValue(State.JOGAR) ; jogadorAtual = 1
                    }
                    else -> System.err.println("\n<ERRO a trocar estado de jogador>")
                }

            }

            3 -> {
                when (state.value) {
                    State.JOGAR -> {
                        state.postValue(State.ESPERA_JOGAR); jogadorAtual = 2
                    }
                    State.ESPERA_JOGAR -> {
                        state.postValue(State.JOGADOR_3); jogadorAtual = 3
                    }
                    State.JOGADOR_3 -> {
                        state.postValue(State.JOGAR); jogadorAtual = 1
                    }
                    else -> System.err.println("\n<ERRO A trocar estado de jogador>")
                }
            }
        }
    }

    fun mostraTabuleiro() {
        println("TABULEIRO//////////////////////////////////////////")
        for (i in 0 until MAXLIN) {
            for (j in 0 until MAXCOL) {
                print(" " + tabulInteiros[i][j] + " ")
            }
            println()
        }
        println("/////////////////////////////////////////////////")
    }


    fun verificaFimJogo(): Boolean {
        when (MODOJOGO) {
            1, 2 -> if ((pontJogador1 + pontJogador2) == 64 || pontJogador1 == 0 || pontJogador2 == 0) {
                println("fim de jogo!!!!!!!");return true
            }
            3 -> {
                if ((pontJogador1 + pontJogador2 + pontJogador3) == 100) {
                    println("fim de jogo!!!!!!!");return true
                }
                if (pontJogador1 == 0 && pontJogador2 == 0) {
                    println("fim de jogo!!!!!!!");return true
                }
                if (pontJogador1 == 0 && pontJogador3 == 0) {
                    println("fim de jogo!!!!!!!");return true
                }
                if (pontJogador2 == 0 && pontJogador3 == 0) {
                    println("fim de jogo!!!!!!!");return true
                }
            }
        }
        return false
    }


    fun getWinner(context: Context):String{
        when(MODOJOGO){
            1,2->{
                if(pontJogador1==pontJogador2)
                    return context.getString(R.string.tie)
                if(pontJogador1>pontJogador2) {
                    return context.getString(R.string.win1)+nomeJogador1
                }else{
                    return context.getString(R.string.win1)+nomeJogador2

                }
            }
            3->{
                if(pontJogador1==pontJogador2 && pontJogador1==pontJogador3) {
                    return context.getString(R.string.tie)
                }else if(pontJogador1==pontJogador2 && pontJogador1>pontJogador3){
                    return context.getString(R.string.win1)+nomeJogador1+context.getString(R.string.win1and2)
                }else if(pontJogador2==pontJogador3 && pontJogador2>pontJogador1){
                    return context.getString(R.string.win2and3)
                }else if (pontJogador1==pontJogador3 && pontJogador1>pontJogador2){
                    return context.getString(R.string.win1)+nomeJogador1+context.getString(R.string.win1and3)
                }else if(pontJogador1>pontJogador2 && pontJogador1>pontJogador3) {
                    return context.getString(R.string.win1)+nomeJogador1
                }else if(pontJogador2>pontJogador3){
                    return context.getString(R.string.win2)
                }else{
                    return context.getString(R.string.win3)
                }
            }
        }
        pontJogador1=2
        pontJogador2=2
        pontJogador3=2
        return "err"

    }

    fun somarPontuacao() {

        pontJogador1 = 0
        pontJogador2 = 0
        pontJogador3 = 0


        for (i in 0 until MAXLIN) {
            for (j in 0 until MAXCOL) {
                if (tabulInteiros[i][j] == 1)
                    pontJogador1++

                if (tabulInteiros[i][j] == 2)
                    pontJogador2++

                if (tabulInteiros[i][j] == 3)
                    pontJogador3++
            }
        }


    }

    fun sorteioJogador() {

        when (MODOJOGO) {
            1 -> {
                jogadorAtual = (1..2).random()
                when (jogadorAtual) {
                    1 -> state.postValue(State.JOGAR)
                    2 -> state.postValue(State.ESPERA_JOGAR)
                }
            }
            2 -> {
                jogadorAtual = (1..2).random()
                when (jogadorAtual) {
                    1 -> {jogadorSorteado=1}
                    2 -> {jogadorSorteado=2}
                }
            }
            3 -> {
                jogadorAtual = (1..3).random()
                when (jogadorAtual) {
                    1 -> state.postValue(State.JOGAR)
                    2 -> state.postValue(State.ESPERA_JOGAR)
                    3 -> state.postValue(State.JOGADOR_3)
                }
            }
        }
        println("\nJogador sorteado a começar: $jogadorAtual\n")
    }



    fun startServer() {
        idJogador = 1 // identificar o servidor
        if (serverSocket != null ||
            socket != null ||
            connectionState.value != ConnectionState.SETTING_PARAMETERS
        )
            return

        connectionState.postValue(ConnectionState.SERVER_CONNECTING)
        thread {
            serverSocket = ServerSocket(SERVER_PORT)
            serverSocket?.apply {  //apply?? run??

                try {
                    println("Conection state:  ${connectionState.value}")

                    startComm(serverSocket!!.accept())
                } catch (_: Exception) {
                    connectionState.postValue(ConnectionState.CONNECTION_ERROR)
                } finally {
                    serverSocket?.close()
                    serverSocket = null
                }
            }
        }
    }

    fun stopServer() {
        serverSocket?.close()
        connectionState.postValue(ConnectionState.CONNECTION_ENDED)
        serverSocket = null
    }

    fun enviarDados(){

        socketO?.run {
            thread {
                try {
                    val json = JSONObject()
                    json.put("imgJogador2", imagemJogador1)
                    json.put("nomeJogador2", nomeJogador1)
                    if(idJogador==1)
                        json.put("jogadorSorteado",jogadorSorteado)
                    println("---------------------Done--------------------------")


                    val msg = json.toString()
                    val printStream = PrintStream(this)
                    printStream.println(msg)
                    printStream.flush()

                } catch (_: Exception) {
                    stopGame()
                }
            }
        }

    }
    fun enviarJogada(){
        socketO?.run {
            thread {
                try {
                    val json = JSONObject()
                    for (i in 0 until 8) {
                        for (j in 0 until 8) {
                            json.put("tabul$i$j",tabulInteiros[i][j])
                            print(json)
                        }
                    }

                    val msg = json.toString()
                    val printStream  = PrintStream(this)
                    printStream.println(msg)
                    printStream.flush()

                } catch (_: Exception) {
                    stopGame()
                }
            }
        }

    }


    private fun startComm(newSocket: Socket) {
        if (threadComm != null)
            return
        socket = newSocket
        threadComm = thread {
            try {
                if (socketI == null)
                    return@thread
                connectionState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
                //state.postValue(State.AGUARDA_JOGADORES)
                enviarDados()
                println("Conection state dps:  ${connectionState.value}")
                val bufI = socketI!!.bufferedReader()



                if (state.value != State.JOGAR || state.value != State.ESPERA_JOGAR) {
                    val message = bufI.readLine()
                    val json = JSONObject(message)

                    imagemJogador2 = json.getString("imgJogador2")
                    nomeJogador2 = json.getString("nomeJogador2")
                    println("Dados recebidos >>>>>>>>: $imagemJogador2, $nomeJogador2, $jogadorSorteado")
                    if (idJogador == 2) {
                        jogadorSorteado = json.getString("jogadorSorteado").toInt()
                        println("sorteio:v$jogadorSorteado")
                    }

                    println("!!!!!!!!!!!!!!!!!!sorteio: $jogadorSorteado!!!!!!!!!!!!!!!!!!!!!!")
                    if (jogadorSorteado == idJogador)
                        state.postValue(State.JOGAR)
                    else
                        state.postValue(State.ESPERA_JOGAR)

                }

                while (state.value != State.GAME_OVER) {

                    if(state.value == State.JOGAR || state.value == State.ESPERA_JOGAR){
                        val message = bufI.readLine()
                        val json = JSONObject(message)

                        for (i in 0 until 8) {
                            for (j in 0 until 8) {
                                tabulInteiros[i][j] = json.getInt("tabul$i$j")
                            }
                        }

                        state.postValue(State.JOGAR)
                    }
                }

            } catch (e: Exception) {
                println(e)
            }finally{
                stopGame()
            }
        }
    }





    fun startClient(serverIP: String, serverPort: Int = SERVER_PORT) {
        idJogador=2
        if (socket != null || connectionState.value != ConnectionState.SETTING_PARAMETERS)
            return

        thread {
            connectionState.postValue(ConnectionState.CLIENT_CONNECTING)
            try {
                val newsocket = Socket()
                newsocket.connect(InetSocketAddress(serverIP, serverPort), 5000)
                println("AQUIIIIIIIIIIIIIII no cliente")

                println("Conection state:  ${connectionState.value}")

                startComm(newsocket)
            } catch (_: Exception) {
                connectionState.postValue(ConnectionState.CONNECTION_ERROR)
                stopGame()
            }
        }
    }


    fun stopGame() {
        try {
            state.postValue(State.GAME_OVER)
            connectionState.postValue(ConnectionState.CONNECTION_ERROR)
            socket?.close()
            socket = null
            threadComm?.interrupt()
            threadComm = null
        } catch (_: Exception) {
        }
    }

    fun setImagem(img : String){
        imagemJogador1 = img
    }
    fun setNome(nome : String){
        nomeJogador1 = nome
    }


}