package com.example.splitwars;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class MessageWriterTest {

    // Os testes aqui leem de volta com o MessageReader: se ele girar para sempre, o teste falha em vez de travar.
    @Rule
    public final Timeout timeout = Timeout.seconds(20);

    private static final class RecordingSink extends OutputStream {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int flushes = 0;

        @Override
        public void write(int b) {
            bytes.write(b);
        }

        @Override
        public void flush() {
            flushes++;
        }

        String text() {
            return new String(bytes.toByteArray(), StandardCharsets.ISO_8859_1);
        }
    }

    // Escreve byte a byte cedendo a vez, como uma rede lenta: sem exclusão mútua, linhas de threads diferentes se misturam.
    private static final class SlowSink extends OutputStream {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        @Override
        public void write(int b) {
            bytes.write(b);
            Thread.yield();
        }
    }

    private static NetworkMessage projectile(float relativeX, float speedX, float speedY) {
        NetworkMessage message = new NetworkMessage(NetworkMessage.PROJECTILE);
        message.setRelativeX(relativeX);
        message.setSpeedX(speedX);
        message.setSpeedY(speedY);
        return message;
    }

    // Pega: esquecer o flush (a mensagem fica no buffer e o jogo do outro celular não a recebe) ou o '\n' final.
    @Test
    public void writesOneLinePerMessageAndFlushesEachOne() throws IOException {
        RecordingSink sink = new RecordingSink();
        MessageWriter writer = new MessageWriter(sink);

        writer.write(new NetworkMessage(NetworkMessage.HIT));
        assertEquals("HIT\n", sink.text());
        assertEquals(1, sink.flushes);

        writer.write(projectile(0.5f, 2.0f, 3.0f));
        assertEquals("HIT\nPROJECTILE 0.5 2.0 3.0\n", sink.text());
        assertEquals(2, sink.flushes);
    }

    // Pega: um valor inválido escrevendo meia linha no fio antes de falhar.
    @Test
    public void writesNothingWhenTheMessageCannotBeEncoded() throws IOException {
        RecordingSink sink = new RecordingSink();
        MessageWriter writer = new MessageWriter(sink);

        try {
            writer.write(projectile(Float.NaN, 0f, 0f));
            fail("deveria recusar NaN");
        } catch (IllegalArgumentException expected) {
            // ok
        }
        assertEquals("", sink.text());
        assertEquals(0, sink.flushes);
    }

    // Pega: remover a sincronização do envio (os envios rodam em várias threads do pool).
    @Test(timeout = 20000)
    public void concurrentSendersNeverInterleaveTheirLines() throws Exception {
        final SlowSink sink = new SlowSink();
        final MessageWriter writer = new MessageWriter(sink);
        final int threads = 8;
        final int perThread = 100;

        List<Thread> workers = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            final float marker = t + 1;
            Thread worker = new Thread(() -> {
                try {
                    for (int i = 0; i < perThread; i++) {
                        writer.write(projectile(0.5f, marker, 3.0f));
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });
            workers.add(worker);
        }
        for (Thread worker : workers) {
            worker.start();
        }
        for (Thread worker : workers) {
            worker.join();
        }

        MessageReader reader = new MessageReader(new ByteArrayInputStream(sink.bytes.toByteArray()));
        int[] perMarker = new int[threads + 1];
        int total = 0;
        NetworkMessage message;
        while ((message = reader.read()) != null) {
            assertEquals(NetworkMessage.PROJECTILE, message.getType());
            assertEquals(0.5f, message.getRelativeX(), 0f);
            assertEquals(3.0f, message.getSpeedY(), 0f);
            perMarker[(int) message.getSpeedX()]++;
            total++;
        }
        assertEquals(threads * perThread, total);
        for (int t = 1; t <= threads; t++) {
            assertEquals("mensagens da thread " + t, perThread, perMarker[t]);
        }
    }

    // Pega: o que o escritor grava não ser o que o leitor entende (os dois lados do fio juntos, sem dublê).
    @Test
    public void whatTheWriterSendsTheReaderUnderstands() throws IOException {
        ByteArrayOutputStream wire = new ByteArrayOutputStream();
        MessageWriter writer = new MessageWriter(wire);

        NetworkMessage size = new NetworkMessage(NetworkMessage.SCREEN_SIZE);
        size.setScreenWidth(2400);
        size.setScreenHeight(1080);
        writer.write(size);
        writer.write(projectile(0.75f, -1.5f, 4.0f));
        writer.write(new NetworkMessage(NetworkMessage.HIT));
        writer.write(new NetworkMessage(NetworkMessage.DISCONNECT));

        MessageReader reader = new MessageReader(new ByteArrayInputStream(wire.toByteArray()));

        NetworkMessage a = reader.read();
        assertNotNull(a);
        assertEquals(NetworkMessage.SCREEN_SIZE, a.getType());
        assertEquals(2400, a.getScreenWidth());
        assertEquals(1080, a.getScreenHeight());

        NetworkMessage b = reader.read();
        assertNotNull(b);
        assertEquals(NetworkMessage.PROJECTILE, b.getType());
        assertEquals(0.75f, b.getRelativeX(), 0f);
        assertEquals(-1.5f, b.getSpeedX(), 0f);
        assertEquals(4.0f, b.getSpeedY(), 0f);

        assertEquals(NetworkMessage.HIT, reader.read().getType());
        assertEquals(NetworkMessage.DISCONNECT, reader.read().getType());
        assertNull(reader.read());
    }
}
