package com.example.splitwars;

/**
 * A parte da tela em que a nave pode andar. Quando as duas telas têm formatos diferentes, os dois
 * jogadores precisam jogar na mesma proporção, então a tela mais larga (ou mais alta) ganha faixas
 * sobrando nas laterais (ou em cima e embaixo).
 */
final class PlayableArea {
    final int left;
    final int top;
    final int right;
    final int bottom;

    private PlayableArea(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    /**
     * Devolve a área, ou {@code null} enquanto o tamanho de alguma das telas não é conhecido.
     * Uma área vazia nunca deve ser usada como limite: a nave ficaria presa fora dela.
     */
    static PlayableArea compute(int screenWidth, int screenHeight, int opponentWidth, int opponentHeight) {
        if (screenWidth <= 0 || screenHeight <= 0 || opponentWidth <= 0 || opponentHeight <= 0) {
            return null;
        }

        if (screenWidth == opponentWidth && screenHeight == opponentHeight) {
            return new PlayableArea(0, 0, screenWidth, screenHeight);
        }

        int playableWidth;
        int playableHeight;
        int offsetX;
        int offsetY;

        float myAspectRatio = (float) screenWidth / screenHeight;
        float opponentAspectRatio = (float) opponentWidth / opponentHeight;

        if (myAspectRatio > opponentAspectRatio) {
            float scaleRatio = (float) screenHeight / opponentHeight;
            playableWidth = (int) (opponentWidth * scaleRatio);
            playableHeight = screenHeight;
            offsetX = (screenWidth - playableWidth) / 2;
            offsetY = 0;
        } else {
            float scaleRatio = (float) screenWidth / opponentWidth;
            playableWidth = screenWidth;
            playableHeight = (int) (opponentHeight * scaleRatio);
            offsetX = 0;
            offsetY = (screenHeight - playableHeight) / 2;
        }

        playableWidth = Math.min(playableWidth, screenWidth);
        playableHeight = Math.min(playableHeight, screenHeight);
        return new PlayableArea(offsetX, offsetY, offsetX + playableWidth, offsetY + playableHeight);
    }
}
