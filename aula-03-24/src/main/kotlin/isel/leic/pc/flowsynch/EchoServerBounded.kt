package isel.leic.pc.flowsynch

import mu.KotlinLogging
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread

private val logger = KotlinLogging.logger {}

class EchoServerBounded(private val port : Int) {
    val MAX_CLIENTS = 2

    val avaiableConnections = Semaphore(MAX_CLIENTS)

    private inline fun sendResponse(writer: PrintWriter, resp : String) {
        writer.println(resp)
        writer.flush()
    }

    fun run() {
        val servSocket = ServerSocket()
        servSocket.bind(InetSocketAddress("0.0.0.0", port))
        while(true) {
            avaiableConnections.acquire()
            val clientSocket = servSocket.accept()
            logger.info("client ${clientSocket.remoteSocketAddress} connected")
            // Note this server creates an unbounded number of threads
            // Do not do this at home!
            thread {
                processClient(clientSocket)

            }
        }
    }

    fun processClient(client: Socket) {
        val reader =
            BufferedReader(InputStreamReader(client.getInputStream()))
        val writer =
            PrintWriter( client.getOutputStream())
        try {
            do {
                val line = reader.readLine()
                if (line == null || line.equals("exit"))
                    break;
                sendResponse(writer,line)
            }
            while(true)
        }
        finally {
            sendResponse(writer,"Bye")
            client.close()
            avaiableConnections.release()
            logger.info("client ${client.remoteSocketAddress} disconnected")
        }
    }
}

private fun main() {
    val server = EchoServerBounded(8080)
    server.run()
}