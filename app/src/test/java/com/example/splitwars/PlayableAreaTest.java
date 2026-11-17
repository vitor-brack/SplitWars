package com.example.splitwars;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * A área em que a nave anda, calculada a partir do tamanho da própria tela e do da tela do oponente.
 * Os valores esperados são os que a conta antiga do GameView dava para as mesmas telas.
 */
public class PlayableAreaTest {

    private static void assertArea(PlayableArea area, int left, int top, int right, int bottom) {
        assertNotNull(area);
        assertEquals("left", left, area.left);
        assertEquals("top", top, area.top);
        assertEquals("right", right, area.right);
        assertEquals("bottom", bottom, area.bottom);
    }

    // Pega: errar o caso mais comum, duas telas iguais, que tem de usar a tela inteira.
    @Test
    public void equalScreensUseTheWholeScreen() {
        assertArea(PlayableArea.compute(2400, 1080, 2400, 1080), 0, 0, 2400, 1080);
    }

    // Pega: trocar o lado da sobra (a tela mais larga ganha faixas dos lados, não de cima e de baixo).
    @Test
    public void aWiderScreenLeavesBarsOnTheSides() {
        assertArea(PlayableArea.compute(2400, 1080, 1920, 1080), 240, 0, 2160, 1080);
    }

    // Pega: o mesmo erro no outro sentido (a tela mais alta ganha faixas em cima e embaixo).
    @Test
    public void aTallerScreenLeavesBarsAboveAndBelow() {
        assertArea(PlayableArea.compute(1920, 1080, 2400, 1080), 0, 108, 1920, 972);
    }

    // Pega: tratar tamanhos diferentes como formatos diferentes (mesma proporção usa a tela inteira).
    @Test
    public void sameShapeAtADifferentSizeUsesTheWholeScreen() {
        assertArea(PlayableArea.compute(1920, 1080, 3840, 2160), 0, 0, 1920, 1080);
    }

    // Pega: errar a escala quando um dos celulares está em pé e o outro deitado.
    @Test
    public void aLandscapeScreenAgainstAPortraitOne() {
        assertArea(PlayableArea.compute(2400, 1080, 1080, 2400), 957, 0, 1443, 1080);
        assertArea(PlayableArea.compute(1080, 2400, 2400, 1080), 0, 957, 1080, 1443);
    }

    // Pega: devolver uma área vazia (0,0,0,0) enquanto não se sabe o tamanho de alguma das telas.
    // Era isso que prendia a nave no canto: a área vazia era aplicada como limite antes do SCREEN_SIZE.
    @Test
    public void thereIsNoAreaWhileAScreenSizeIsUnknown() {
        assertNull(PlayableArea.compute(0, 0, 2400, 1080));
        assertNull(PlayableArea.compute(2400, 1080, 0, 0));
        assertNull(PlayableArea.compute(2400, 0, 2400, 1080));
        assertNull(PlayableArea.compute(0, 1080, 2400, 1080));
        assertNull(PlayableArea.compute(2400, 1080, 2400, 0));
        assertNull(PlayableArea.compute(2400, 1080, 0, 1080));
        assertNull(PlayableArea.compute(-1, -1, -1, -1));
    }
}
