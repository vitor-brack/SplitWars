package com.example.splitwars;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class MessageReaderTest {

    // Um leitor com defeito pode girar para sempre; sem limite de tempo o teste travaria a suíte inteira.
    @Rule
    public final Timeout timeout = Timeout.seconds(20);

    private static MessageReader readerOf(String text) {
        return new MessageReader(new ByteArrayInputStream(text.getBytes(StandardCharsets.ISO_8859_1)));
    }

    private static String repeat(char c, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(c);
        }
        return builder.toString();
    }

    // Entrega no máximo 1 byte por chamada, como uma rede lenta que fatia a linha.
    private static final class OneByteAtATime extends FilterInputStream {
        OneByteAtATime(InputStream in) {
            super(in);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return super.read(b, off, Math.min(len, 1));
        }
    }

    // Fluxo de 'A' que conta quantos bytes foram consumidos e termina depois de 'total'.
    private static final class CountingAs extends InputStream {
        private final long total;
        long consumed = 0;

        CountingAs(long total) {
            this.total = total;
        }

        @Override
        public int read() {
            if (consumed >= total) {
                return -1;
            }
            consumed++;
            return 'A';
        }
    }

    // Pega: ler só uma linha, ou trocar a ordem das mensagens.
    @Test
    public void readsMessagesInTheOrderTheyArrive() throws IOException {
        MessageReader reader = readerOf("SCREEN_SIZE 1920 1080\nPROJECTILE 0.5 2.0 3.0\nHIT\n");

        NetworkMessage first = reader.read();
        assertNotNull(first);
        assertEquals(NetworkMessage.SCREEN_SIZE, first.getType());
        assertEquals(1920, first.getScreenWidth());

        NetworkMessage second = reader.read();
        assertNotNull(second);
        assertEquals(NetworkMessage.PROJECTILE, second.getType());
        assertEquals(3.0f, second.getSpeedY(), 0f);

        NetworkMessage third = reader.read();
        assertNotNull(third);
        assertEquals(NetworkMessage.HIT, third.getType());
    }

    // Pega: devolver uma mensagem vazia no fim da conexão (o loop de leitura giraria sem parar).
    @Test(timeout = 5000)
    public void returnsNullWhenTheOtherSideClosesTheConnection() throws IOException {
        assertNull(readerOf("").read());

        MessageReader reader = readerOf("HIT\n");
        assertNotNull(reader.read());
        assertNull(reader.read());
    }

    // Pega: linha inválida derrubando a conexão ou travando a leitura.
    @Test
    public void skipsInvalidLinesAndKeepsReading() throws IOException {
        MessageReader reader = readerOf("lixo\nPROJECTILE a b c\n\nHIT\n");

        NetworkMessage message = reader.read();
        assertNotNull(message);
        assertEquals(NetworkMessage.HIT, message.getType());
        assertNull(reader.read());
    }

    // Pega: bytes que não são texto válido causando exceção em vez de serem ignorados.
    @Test
    public void ignoresLinesWithNonTextBytes() throws IOException {
        MessageReader reader = readerOf("ÿþ\u0000\nHIT\n");

        NetworkMessage message = reader.read();
        assertNotNull(message);
        assertEquals(NetworkMessage.HIT, message.getType());
    }

    // Pega: supor que a linha chega inteira numa leitura só.
    @Test
    public void putsALineTogetherWhenItArrivesOneByteAtATime() throws IOException {
        byte[] bytes = "SCREEN_SIZE 800 600\nHIT\n".getBytes(StandardCharsets.ISO_8859_1);
        MessageReader reader = new MessageReader(new OneByteAtATime(new ByteArrayInputStream(bytes)));

        NetworkMessage size = reader.read();
        assertNotNull(size);
        assertEquals(800, size.getScreenWidth());
        assertEquals(600, size.getScreenHeight());
        assertEquals(NetworkMessage.HIT, reader.read().getType());
    }

    // Pega: o limite errado por um (linha no limite recusada, ou linha acima do limite aceita).
    @Test
    public void acceptsALineAtTheLimitButRejectsOneByteMore() throws IOException {
        int max = MessageReader.MAX_LINE_LENGTH;

        MessageReader atLimit = readerOf(repeat('A', max) + "\nHIT\n");
        NetworkMessage message = atLimit.read();
        assertNotNull(message);
        assertEquals(NetworkMessage.HIT, message.getType());

        MessageReader overLimit = readerOf(repeat('A', max + 1) + "\nHIT\n");
        try {
            overLimit.read();
            fail("deveria recusar a linha maior que o limite");
        } catch (IOException expected) {
            // ok
        }
    }

    // Pega: ler uma "linha" sem fim para a memória (um oponente malicioso travando o celular).
    @Test(timeout = 5000)
    public void stopsEarlyOnAHugeLineWithoutBuffering() {
        CountingAs endless = new CountingAs(10_000_000L);
        MessageReader reader = new MessageReader(endless);
        try {
            reader.read();
            fail("deveria recusar a linha gigante");
        } catch (IOException expected) {
            // ok
        }
        assertTrue("leu bytes demais antes de desistir: " + endless.consumed, endless.consumed < 1024);
    }
}
