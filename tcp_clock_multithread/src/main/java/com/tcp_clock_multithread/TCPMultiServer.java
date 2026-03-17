package com.tcp_clock_multithread;

import java.io.*;
import java.net.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servidor TCP Multithread do Sistema Distribuído de Relógio Mundial.
 *
 * A cada nova conexão aceita via {@code accept()}, uma {@link Thread} dedicada é
 * disparada para processar aquela requisição de forma independente, liberando
 * o loop principal imediatamente para aceitar o próximo cliente. Isso garante
 * escalabilidade horizontal sem bloqueio entre atendimentos.
 *
 * @author Luis Henrique Rodrigues de Oliveira
 * @version 1.0
 * @since 2025
 */
public class TCPMultiServer {

    //Porta em que o servidor ficará escutando.
    private static final int PORT = 9002;

 
     //Ponto de entrada do servidor TCP Multithread.
    public static void main(String[] args) {
        System.out.println("[TCP-Multi] Servidor iniciado na porta " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Dispara uma nova Thread assim que o accept() retorna,
                // sem bloquear o loop principal.
                Thread worker = new Thread(new ClientHandler(clientSocket));
                worker.start();

                System.out.printf("[TCP-Multi] Nova thread iniciada: %s | Cliente: %s:%d%n",
                        worker.getName(),
                        clientSocket.getInetAddress().getHostAddress(),
                        clientSocket.getPort());
            }

        } catch (IOException e) {
            System.err.println("[TCP-Multi] Erro no servidor: " + e.getMessage());
        }
    }


    /**
     * Tarefa executada por cada Thread responsável por um único cliente.
     *
     * Implementa {@link Runnable} para manter o servidor desacoplado da lógica
     * de atendimento — padrão que facilita a substituição futura por um
     * {@link java.util.concurrent.ExecutorService} (pool de threads).
     */
    private static class ClientHandler implements Runnable {

        // Formato de saída da data e hora. 
        private static final DateTimeFormatter FORMATTER =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss z");

        private final Socket clientSocket;

        
        // Cria um handler para o socket de cliente informado. 
        ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        
        //Lê a região solicitada, consulta a hora e devolve a resposta ao cliente.
        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            String clientInfo = clientSocket.getInetAddress().getHostAddress()
                    + ":" + clientSocket.getPort();

            System.out.printf("[%s] Atendendo cliente %s%n", threadName, clientInfo);

            try (clientSocket;
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String regionId = in.readLine();
                System.out.printf("[%s] Região solicitada por %s: %s%n",
                        threadName, clientInfo, regionId);

                String response = buildResponse(regionId);
                out.println(response);

                System.out.printf("[%s] Resposta enviada para %s: %s%n",
                        threadName, clientInfo, response);

            } catch (IOException e) {
                System.err.printf("[%s] Erro ao atender %s: %s%n",
                        threadName, clientInfo, e.getMessage());
            }
        }
        
         // Constrói a resposta textual a partir do ID de região.
        private String buildResponse(String regionId) {
            if (regionId == null || regionId.isBlank()) {
                return "ERRO: Região não informada";
            }
            try {
                ZoneId zone = ZoneId.of(regionId.trim());
                ZonedDateTime now = ZonedDateTime.now(zone);
                return "Hora em " + regionId + ": " + now.format(FORMATTER);
            } catch (Exception e) {
                return "ERRO: Região inválida -> " + regionId;
            }
        }
    }
}
