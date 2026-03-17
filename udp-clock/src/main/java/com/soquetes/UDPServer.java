package com.soquetes;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servidor UDP do Sistema Distribuído de Relógio Mundial.
 *
 * Aguarda datagramas contendo um ID de região geográfica (ex: {@code America/Sao_Paulo}),
 * consulta a hora local daquela região via {@link ZonedDateTime} e devolve
 * a data/hora formatada ou uma mensagem de erro para regiões inválidas.
 *
 * @author Luis Henrique Rodrigues de Oliveira
 * @version 1.0
 * @since 2025
 */
public class UDPServer {

    // Porta em que o servidor ficará escutando.
    private static final int PORT = 9000;

    // Tamanho máximo do buffer de recepção (bytes).
    private static final int BUFFER_SIZE = 256;

    // Formato de saída da data e hora.
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss z");

    
    //Ponto de entrada do servidor UDP.
    public static void main(String[] args) {
        System.out.println("[UDP] Servidor iniciado na porta " + PORT);

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[BUFFER_SIZE];

            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                String regionId = new String(
                        request.getData(), 0, request.getLength(), StandardCharsets.UTF_8).trim();

                System.out.printf("[UDP] Requisição recebida de %s:%d -> região: %s%n",
                        request.getAddress().getHostAddress(),
                        request.getPort(),
                        regionId);

                String response = buildResponse(regionId);

                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                DatagramPacket reply = new DatagramPacket(
                        responseBytes,
                        responseBytes.length,
                        request.getAddress(),
                        request.getPort());

                socket.send(reply);
                System.out.println("[UDP] Resposta enviada: " + response);
            }

        } catch (Exception e) {
            System.err.println("[UDP] Erro no servidor: " + e.getMessage());
        }
    }

    
     //Constrói a resposta textual para o cliente a partir do ID de região.
    private static String buildResponse(String regionId) {
        try {
            ZoneId zone = ZoneId.of(regionId);
            ZonedDateTime now = ZonedDateTime.now(zone);
            return "Hora em " + regionId + ": " + now.format(FORMATTER);
        } catch (Exception e) {
            return "ERRO: Região inválida -> " + regionId;
        }
    }
}
