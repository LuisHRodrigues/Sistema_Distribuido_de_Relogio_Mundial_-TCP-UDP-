package com.tcpsoquete;

import java.io.*;
import java.net.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servidor TCP Single-Thread do Sistema Distribuído de Relógio Mundial.
 *
 * Aceita conexões sequencialmente: cada cliente é atendido até o fim antes
 * que o próximo seja aceito. Clientes adicionais ficam na fila de backlog
 * gerenciada pelo sistema operacional enquanto aguardam sua vez.
 *
 * @author Luis Henrique Rodrigues de Oliveira
 * @version 1.0
 * @since 2025
 */
public class TCPServer {

    /** Porta em que o servidor ficará escutando. */
    private static final int PORT = 9001;

    /** Formato de saída da data e hora. */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss z");

    /*
     * Ponto de entrada do servidor TCP Single-Thread.
     */
    public static void main(String[] args) {
        System.out.println("[TCP-Single] Servidor iniciado na porta " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.printf("[TCP-Single] Cliente conectado: %s:%d%n",
                        clientSocket.getInetAddress().getHostAddress(),
                        clientSocket.getPort());

                handleClient(clientSocket);
            }

        } catch (IOException e) {
            System.err.println("[TCP-Single] Erro no servidor: " + e.getMessage());
        }
    }

    /*
     * Lê a requisição do cliente, processa a região e envia a resposta.
     *
     * O socket do cliente é fechado ao término do atendimento.
     */
    private static void handleClient(Socket clientSocket) {
        try (clientSocket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String regionId = in.readLine();
            System.out.println("[TCP-Single] Região solicitada: " + regionId);

            String response = buildResponse(regionId);
            out.println(response);
            System.out.println("[TCP-Single] Resposta enviada: " + response);

        } catch (IOException e) {
            System.err.println("[TCP-Single] Erro ao atender cliente: " + e.getMessage());
        }
    }

    /*
     * Constrói a resposta textual para o cliente a partir do ID de região.
     */
    private static String buildResponse(String regionId) {
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
