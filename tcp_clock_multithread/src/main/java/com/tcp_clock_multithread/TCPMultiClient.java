package com.tcp_clock_multithread;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Cliente TCP para o Servidor Multithread do Relógio Mundial.
 *
 * Idêntico ao {@code TCPClient} da Parte 2 porém aponta para a porta
 * {@code 9002} do servidor multithread. Pode ser executado em múltiplos
 * terminais simultaneamente para demonstrar o atendimento paralelo.
 *
 * @author Luis Henrique Rodrigues de Oliveira
 * @version 1.0
 * @since 2025
 */
public class TCPMultiClient {

    // Endereço do servidor. 
    private static final String HOST = "localhost";

    //Porta do servidor multithread.
    private static final int PORT = 9002;

    //Ponto de entrada do cliente TCP multithread.
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("[TCP-Multi] Digite o ID da região (ex: Asia/Tokyo): ");
        String regionId = scanner.nextLine().trim();

        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()))) {

            out.println(regionId);
            System.out.println("[TCP-Multi] Requisição enviada. Aguardando resposta...");

            String response = in.readLine();
            System.out.println("[TCP-Multi] Resposta: " + response);

        } catch (IOException e) {
            System.err.println("[TCP-Multi] Erro ao conectar ao servidor: " + e.getMessage());
        }

        scanner.close();
    }
}
