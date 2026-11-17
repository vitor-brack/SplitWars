package com.example.splitwars;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Escreve as mensagens de {@link MessageCodec} num fluxo de rede, uma linha por mensagem.
 * O envio é sincronizado: o NetworkManager manda de várias threads, e duas linhas gravadas ao
 * mesmo tempo se misturariam no fio.
 */
final class MessageWriter {
    private final OutputStream out;

    MessageWriter(OutputStream out) {
        this.out = out;
    }

    synchronized void write(NetworkMessage message) throws IOException {
        byte[] bytes = (MessageCodec.encode(message) + "\n").getBytes(StandardCharsets.ISO_8859_1);
        out.write(bytes);
        out.flush();
    }
}
