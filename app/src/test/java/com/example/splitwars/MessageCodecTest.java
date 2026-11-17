package com.example.splitwars;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Locale;
import org.junit.After;
import org.junit.Test;

public class MessageCodecTest {

    private final Locale originalLocale = Locale.getDefault();

    @After
    public void restoreLocale() {
        Locale.setDefault(originalLocale);
    }

    private static NetworkMessage projectile(float relativeX, float speedX, float speedY) {
        NetworkMessage message = new NetworkMessage(NetworkMessage.PROJECTILE);
        message.setRelativeX(relativeX);
        message.setSpeedX(speedX);
        message.setSpeedY(speedY);
        return message;
    }

    private static NetworkMessage screenSize(int width, int height) {
        NetworkMessage message = new NetworkMessage(NetworkMessage.SCREEN_SIZE);
        message.setScreenWidth(width);
        message.setScreenHeight(height);
        return message;
    }

    // Pega: nome do tipo trocado no fio, que o outro celular não reconheceria.
    @Test
    public void encodesHitAndDisconnectAsTheirBareNames() {
        assertEquals("HIT", MessageCodec.encode(new NetworkMessage(NetworkMessage.HIT)));
        assertEquals("DISCONNECT", MessageCodec.encode(new NetworkMessage(NetworkMessage.DISCONNECT)));
    }

    // Pega: campos trocados de ordem (relativeX, speedX, speedY).
    @Test
    public void encodesProjectileWithThreeFloatsInOrder() {
        assertEquals("PROJECTILE 0.5 2.0 -3.0", MessageCodec.encode(projectile(0.5f, 2.0f, -3.0f)));
    }

    // Pega: largura e altura trocadas.
    @Test
    public void encodesScreenSizeWidthThenHeight() {
        assertEquals("SCREEN_SIZE 1920 1080", MessageCodec.encode(screenSize(1920, 1080)));
    }

    // Pega: formatar com a vírgula do celular em português ("0,25"), que o outro lado leria errado.
    @Test
    public void encodesFloatsWithADotEvenWhenThePhoneLocaleUsesAComma() {
        Locale.setDefault(Locale.forLanguageTag("pt-BR"));
        assertEquals("PROJECTILE 0.25 1.5 -2.0", MessageCodec.encode(projectile(0.25f, 1.5f, -2.0f)));
    }

    // Pega: NaN ou infinito indo para o fio e chegando como posição do tiro.
    @Test
    public void refusesToEncodeValuesThatAreNotFinite() {
        float[] bad = {Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY};
        for (float value : bad) {
            try {
                MessageCodec.encode(projectile(value, 0f, 0f));
                fail("deveria recusar relativeX=" + value);
            } catch (IllegalArgumentException expected) {
                // ok
            }
            try {
                MessageCodec.encode(projectile(0f, 0f, value));
                fail("deveria recusar speedY=" + value);
            } catch (IllegalArgumentException expected) {
                // ok
            }
        }
    }

    // Pega: leitura que descarta ou troca algum campo de cada tipo.
    @Test
    public void decodesEachMessageType() {
        NetworkMessage shot = MessageCodec.decode("PROJECTILE 0.5 2.0 -3.0");
        assertNotNull(shot);
        assertEquals(NetworkMessage.PROJECTILE, shot.getType());
        assertEquals(0.5f, shot.getRelativeX(), 0f);
        assertEquals(2.0f, shot.getSpeedX(), 0f);
        assertEquals(-3.0f, shot.getSpeedY(), 0f);

        NetworkMessage size = MessageCodec.decode("SCREEN_SIZE 2400 1080");
        assertNotNull(size);
        assertEquals(NetworkMessage.SCREEN_SIZE, size.getType());
        assertEquals(2400, size.getScreenWidth());
        assertEquals(1080, size.getScreenHeight());

        assertEquals(NetworkMessage.HIT, MessageCodec.decode("HIT").getType());
        assertEquals(NetworkMessage.DISCONNECT, MessageCodec.decode("DISCONNECT").getType());
    }

    // Pega: perda de precisão (por exemplo, arredondar para 2 casas) ou notação científica quebrada.
    @Test
    public void floatsSurviveTheTripleEncodeDecodeExactly() {
        float[] values = {0.1f, 1.0E-5f, 123456.79f, -0.3f, Float.MAX_VALUE};
        for (float value : values) {
            NetworkMessage back = MessageCodec.decode(MessageCodec.encode(projectile(value, value, value)));
            assertNotNull("valor " + value, back);
            assertEquals(value, back.getRelativeX(), 0f);
            assertEquals(value, back.getSpeedX(), 0f);
            assertEquals(value, back.getSpeedY(), 0f);
        }
    }

    // Pega: o limite aceitando valor ruim ou recusando valor bom (a GameView divide pela largura e pela altura).
    @Test
    public void acceptsScreenSizesFromOneUpToTheLimit() {
        int max = MessageCodec.MAX_SCREEN_DIMENSION;
        NetworkMessage smallest = MessageCodec.decode("SCREEN_SIZE 1 1");
        assertNotNull(smallest);
        assertEquals(1, smallest.getScreenWidth());
        NetworkMessage largest = MessageCodec.decode("SCREEN_SIZE " + max + " " + max);
        assertNotNull(largest);
        assertEquals(max, largest.getScreenHeight());
        assertNull(MessageCodec.decode("SCREEN_SIZE " + (max + 1) + " 100"));
        assertNull(MessageCodec.decode("SCREEN_SIZE 100 " + (max + 1)));
    }

    // Pega: linha de lixo virando mensagem (e derrubando o jogo com divisão por zero ou posição inválida).
    @Test
    public void decodeReturnsNullForEveryMalformedLine() {
        String[] malformed = {
                "", " ", "HELLO", "hit", "HIT ", " HIT", "HIT 1", "DISCONNECT 1",
                "PROJECTILE", "PROJECTILE 0.5 1.0", "PROJECTILE 0.5 1.0 2.0 3.0",
                "PROJECTILE a b c", "PROJECTILE 0.5  1.0 2.0",
                "PROJECTILE NaN 1.0 2.0", "PROJECTILE 0.5 Infinity 2.0", "PROJECTILE 0.5 1.0 -Infinity",
                "SCREEN_SIZE", "SCREEN_SIZE 1920",
                "SCREEN_SIZE 0 1080", "SCREEN_SIZE 1920 0", "SCREEN_SIZE -1 1080", "SCREEN_SIZE 1920 -1",
                "SCREEN_SIZE 1.5 1080", "SCREEN_SIZE 99999999999 1080"
        };
        for (String line : malformed) {
            assertNull("deveria recusar: [" + line + "]", MessageCodec.decode(line));
        }
    }
}
