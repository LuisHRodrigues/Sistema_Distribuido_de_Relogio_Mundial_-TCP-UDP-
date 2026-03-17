package com.soquetes;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Cliente UDP do Sistema Distribuído de Relógio Mundial.
 *
 * Envia um ID de região geográfica ao {@link UDPServer} e aguarda a resposta
 * por até 5 segundos. Se o servidor não responder no prazo, exibe uma mensagem
 * de erro em vez de travar indefinidamente.
 *
 * @author Luis Henrique Rodrigues de Oliveira
 * @version 1.0
 * @since 2025
 */
public class UDPClient {

    //Endereço do servidor.
    private static final String HOST = "localhost";

    //Porta do servidor.
    private static final int PORT = 9000;

    //Timeout de espera pela resposta, em milissegundos.
    private static final int TIMEOUT_MS = 5000;

    // Tamanho máximo do buffer de recepção (bytes).
    private static final int BUFFER_SIZE = 512;

    
    //Ponto de entrada do cliente UDP. 
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("[UDP] Digite o ID da região (ex: America/Sao_Paulo): ");
        String regionId = scanner.nextLine().trim();

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(TIMEOUT_MS);

            InetAddress address = InetAddress.getByName(HOST);
            byte[] data = regionId.getBytes(StandardCharsets.UTF_8);

            DatagramPacket request = new DatagramPacket(data, data.length, address, PORT);
            socket.send(request);
            System.out.println("[UDP] Requisição enviada. Aguardando resposta...");

            byte[] buffer = new byte[BUFFER_SIZE];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);

            try {
                socket.receive(response);
                String result = new String(
                        response.getData(), 0, response.getLength(), StandardCharsets.UTF_8);
                System.out.println("[UDP] Resposta: " + result);
            } catch (SocketTimeoutException e) {
                System.err.println("[UDP] Servidor ocupado ou offline");
            }

        } catch (Exception e) {
            System.err.println("[UDP] Erro no cliente: " + e.getMessage());
        }

        scanner.close();
    }
}
