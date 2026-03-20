package com.tcpsoquete;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Cliente TCP do Sistema Distribuído de Relógio Mundial.
 *
 * Abre uma conexão com o servidor TCP, envia o ID de uma região geográfica
 * e exibe a data/hora retornada ou a mensagem de erro emitida pelo servidor.
 *
 * @author Luis Henrique Rodrigues de Oliveira
 * @version 1.0
 * @since 2025
 */
public class TCPClient {

    //Endereço do servidor.
    private static final String HOST = "localhost";

    //Porta do servidor.
    private static final int PORT = 9001;

    
    //Ponto de entrada do cliente TCP.
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("[TCP] Digite o ID da região (ex: Europe/London): ");
        String regionId = scanner.nextLine().trim();

        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()))) {

            out.println(regionId);
            System.out.println("[TCP] Requisição enviada. Aguardando resposta...");

            String response = in.readLine();
            System.out.println("[TCP] Resposta: " + response);

        } catch (IOException e) {
            System.err.println("[TCP] Erro ao conectar ao servidor: " + e.getMessage());
        }

        scanner.close();
    }
}
