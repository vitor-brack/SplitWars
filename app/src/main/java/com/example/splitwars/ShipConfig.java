package com.example.splitwars;

/**
 * Tudo o que muda de uma nave para a outra: sprites, velocidade e tamanho das animações,
 * posição da propulsão e áreas de colisão. O ShipView lê estes valores em vez de ter um
 * código para cada nave.
 */
final class ShipConfig {
    final int shipSprite;
    final int bulletSprite;
    final int explosionSprite;
    final int thrusterSprite;

    // tiro: quadros por segundo da animação e escala do desenho
    final float bulletFrameRate;
    final float bulletScale;

    // explosão: quadros por segundo, quantidade de quadros no sprite e escala do desenho
    final float explosionFrameRate;
    final int explosionFrames;
    final float explosionScale;

    // propulsão: quadros por segundo, transparência (0 a 255), escala, deslocamento vertical do
    // desenho e quanto acima da nave ela é posicionada
    final float thrusterFrameRate;
    final int thrusterAlpha;
    final float thrusterScale;
    final int thrusterOffsetY;
    final int thrusterDrawOffsetY;

    // tamanho usado quando o sprite da nave não carrega
    final int fallbackWidth;
    final int fallbackHeight;

    // colisão: fração do sprite da nave que conta como alvo e meia-largura do tiro
    final float hitboxFactor;
    final int bulletHitboxHalfSize;

    private ShipConfig(
            int shipSprite, int bulletSprite, int explosionSprite, int thrusterSprite,
            float bulletFrameRate, float bulletScale,
            float explosionFrameRate, int explosionFrames, float explosionScale,
            float thrusterFrameRate, int thrusterAlpha, float thrusterScale,
            int thrusterOffsetY, int thrusterDrawOffsetY,
            int fallbackWidth, int fallbackHeight,
            float hitboxFactor, int bulletHitboxHalfSize) {
        this.shipSprite = shipSprite;
        this.bulletSprite = bulletSprite;
        this.explosionSprite = explosionSprite;
        this.thrusterSprite = thrusterSprite;
        this.bulletFrameRate = bulletFrameRate;
        this.bulletScale = bulletScale;
        this.explosionFrameRate = explosionFrameRate;
        this.explosionFrames = explosionFrames;
        this.explosionScale = explosionScale;
        this.thrusterFrameRate = thrusterFrameRate;
        this.thrusterAlpha = thrusterAlpha;
        this.thrusterScale = thrusterScale;
        this.thrusterOffsetY = thrusterOffsetY;
        this.thrusterDrawOffsetY = thrusterDrawOffsetY;
        this.fallbackWidth = fallbackWidth;
        this.fallbackHeight = fallbackHeight;
        this.hitboxFactor = hitboxFactor;
        this.bulletHitboxHalfSize = bulletHitboxHalfSize;
    }

    static final ShipConfig SHIP_1 = new ShipConfig(
            R.drawable.spr_ship, R.drawable.spr_bullet, R.drawable.spr_explosion, R.drawable.spr_thruster,
            10.0f, 0.8f,
            12.0f, 10, 1.0f,
            12.0f, 200, 0.8f,
            30, 95,
            80, 40,
            0.2f, 10);

    static final ShipConfig SHIP_2 = new ShipConfig(
            R.drawable.spr_ship2, R.drawable.spr_bullet2, R.drawable.spr_explosion2, R.drawable.spr_thruster2,
            12.0f, 0.9f,
            15.0f, 16, 1.2f,
            15.0f, 225, 0.55f,
            35, 165,
            85, 45,
            0.225f, 12);

    /** Tipo 1 é a nave 1; qualquer outro tipo usa a nave 2 (a regra que o jogo sempre teve). */
    static ShipConfig forType(int type) {
        return type == 1 ? SHIP_1 : SHIP_2;
    }
}
