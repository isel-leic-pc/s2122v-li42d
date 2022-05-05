package isel.leic.pc.servers

import mu.KotlinLogging
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors
import kotlin.concurrent.thread

private val logger = KotlinLogging.logger {}

class EchoServerTP(private val port : Int) {

    //val pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    val pool = Executors.newCachedThreadPool()

    fun run() {
        val servSocket = ServerSocket()
        servSocket.bind(InetSocketAddress("0.0.0.0", port))
        while(true) {
            val clientSocket = servSocket.accept()
            logger.info("client ${clientSocket.remoteSocketAddress} connected")

            // Note this server creates an unbounded number of threads
            // Do not do this at home!
           pool.execute {
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

                writer.println(line)
                writer.flush()
            }
            while(true)
        }
        finally {
            writer.println("Bye")
            writer.flush()
            client.close()
            logger.info("client ${client.remoteSocketAddress} disconnected")
        }
    }
}

private fun main() {
    val server = EchoServerTP(8080)
    server.run()
}