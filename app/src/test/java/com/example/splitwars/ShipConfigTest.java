package com.example.splitwars;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * Os números abaixo são os que estavam escritos, um a um, em ShipView (nave 1) e ShipView2 (nave 2)
 * antes de as duas views virarem uma só. Se um deles mudar sem querer, o jogo muda de jeito.
 */
public class ShipConfigTest {

    // Pega: trocar a regra de escolha (GameActivity e GameView sempre trataram 1 como nave 1).
    @Test
    public void typeOneSelectsShipOne() {
        assertSame(ShipConfig.SHIP_1, ShipConfig.forType(1));
    }

    // Pega: tratar só o tipo 2 como nave 2 (antes, qualquer tipo diferente de 1 usava a nave 2).
    @Test
    public void anyOtherTypeSelectsShipTwo() {
        for (int type : new int[] {2, 0, -1, 3, Integer.MAX_VALUE}) {
            assertSame("tipo " + type, ShipConfig.SHIP_2, ShipConfig.forType(type));
        }
    }

    // Pega: sprite da nave 1 apontando para o recurso errado, ou valor trocado ao copiar do ShipView.
    @Test
    public void shipOneKeepsTheValuesOfTheOldShipView() {
        ShipConfig c = ShipConfig.SHIP_1;
        assertEquals(R.drawable.spr_ship, c.shipSprite);
        assertEquals(R.drawable.spr_bullet, c.bulletSprite);
        assertEquals(R.drawable.spr_explosion, c.explosionSprite);
        assertEquals(R.drawable.spr_thruster, c.thrusterSprite);

        assertEquals(10.0f, c.bulletFrameRate, 0f);
        assertEquals(0.8f, c.bulletScale, 0f);

        assertEquals(12.0f, c.explosionFrameRate, 0f);
        assertEquals(10, c.explosionFrames);
        assertEquals(1.0f, c.explosionScale, 0f);

        assertEquals(12.0f, c.thrusterFrameRate, 0f);
        assertEquals(200, c.thrusterAlpha);
        assertEquals(0.8f, c.thrusterScale, 0f);
        assertEquals(30, c.thrusterOffsetY);
        assertEquals(95, c.thrusterDrawOffsetY);

        assertEquals(80, c.fallbackWidth);
        assertEquals(40, c.fallbackHeight);
        assertEquals(0.2f, c.hitboxFactor, 0f);
        assertEquals(10, c.bulletHitboxHalfSize);
    }

    // Pega: valor da nave 2 trocado ao copiar do ShipView2 (inclusive trocar com o da nave 1).
    @Test
    public void shipTwoKeepsTheValuesOfTheOldShipView2() {
        ShipConfig c = ShipConfig.SHIP_2;
        assertEquals(R.drawable.spr_ship2, c.shipSprite);
        assertEquals(R.drawable.spr_bullet2, c.bulletSprite);
        assertEquals(R.drawable.spr_explosion2, c.explosionSprite);
        assertEquals(R.drawable.spr_thruster2, c.thrusterSprite);

        assertEquals(12.0f, c.bulletFrameRate, 0f);
        assertEquals(0.9f, c.bulletScale, 0f);

        assertEquals(15.0f, c.explosionFrameRate, 0f);
        assertEquals(16, c.explosionFrames);
        assertEquals(1.2f, c.explosionScale, 0f);

        assertEquals(15.0f, c.thrusterFrameRate, 0f);
        assertEquals(225, c.thrusterAlpha);
        assertEquals(0.55f, c.thrusterScale, 0f);
        assertEquals(35, c.thrusterOffsetY);
        assertEquals(165, c.thrusterDrawOffsetY);

        assertEquals(85, c.fallbackWidth);
        assertEquals(45, c.fallbackHeight);
        assertEquals(0.225f, c.hitboxFactor, 0f);
        assertEquals(12, c.bulletHitboxHalfSize);
    }

    // Pega: as duas naves usando o mesmo sprite (a unificação perderia a aparência da nave 2).
    @Test
    public void theTwoShipsUseDifferentSprites() {
        assertNotEquals(ShipConfig.SHIP_1.shipSprite, ShipConfig.SHIP_2.shipSprite);
        assertNotEquals(ShipConfig.SHIP_1.bulletSprite, ShipConfig.SHIP_2.bulletSprite);
        assertNotEquals(ShipConfig.SHIP_1.explosionSprite, ShipConfig.SHIP_2.explosionSprite);
        assertNotEquals(ShipConfig.SHIP_1.thrusterSprite, ShipConfig.SHIP_2.thrusterSprite);
    }
}
