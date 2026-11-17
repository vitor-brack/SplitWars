package com.example.splitwars;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Lê as mensagens de {@link MessageCodec} de um fluxo de rede, uma linha por vez.
 * Guarda no máximo {@link #MAX_LINE_LENGTH} bytes de uma linha, para um oponente que nunca
 * manda o fim de linha não conseguir encher a memória do celular.
 */
final class MessageReader {
    static final int MAX_LINE_LENGTH = 128;

    private final InputStream in;
    private final byte[] line = new byte[MAX_LINE_LENGTH];

    MessageReader(InputStream in) {
        this.in = in;
    }

    /**
     * Devolve a próxima mensagem válida, ignorando linhas fora do formato, ou {@code null} quando o
     * outro lado fecha a conexão. Lança {@link IOException} se uma linha passar do limite.
     */
    NetworkMessage read() throws IOException {
        while (true) {
            String text = readLine();
            if (text == null) {
                return null;
            }
            NetworkMessage message = MessageCodec.decode(text);
            if (message != null) {
                return message;
            }
        }
    }

    private String readLine() throws IOException {
        int length = 0;
        while (true) {
            int b = in.read();
            if (b == -1) {
                return null;
            }
            if (b == '\n') {
                return new String(line, 0, length, StandardCharsets.ISO_8859_1);
            }
            if (length == MAX_LINE_LENGTH) {
                throw new IOException("linha maior que " + MAX_LINE_LENGTH + " bytes");
            }
            line[length++] = (byte) b;
        }
    }
}
