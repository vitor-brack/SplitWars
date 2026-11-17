package com.example.splitwars;

/**
 * Formato das mensagens no fio: uma linha de texto por mensagem, com os campos separados por um espaço.
 *
 * <pre>
 * PROJECTILE &lt;relativeX&gt; &lt;speedX&gt; &lt;speedY&gt;
 * SCREEN_SIZE &lt;largura&gt; &lt;altura&gt;
 * HIT
 * DISCONNECT
 * </pre>
 *
 * Números decimais sempre com ponto (Float.toString não depende do idioma do celular).
 * Linha fora desse formato vira {@code null} em {@link #decode}.
 */
final class MessageCodec {
    static final int MAX_SCREEN_DIMENSION = 16384;

    private static final String PROJECTILE = "PROJECTILE";
    private static final String SCREEN_SIZE = "SCREEN_SIZE";
    private static final String HIT = "HIT";
    private static final String DISCONNECT = "DISCONNECT";

    private MessageCodec() {
    }

    static String encode(NetworkMessage message) {
        switch (message.getType()) {
            case NetworkMessage.PROJECTILE:
                return PROJECTILE + " " + finite(message.getRelativeX())
                        + " " + finite(message.getSpeedX())
                        + " " + finite(message.getSpeedY());
            case NetworkMessage.SCREEN_SIZE:
                return SCREEN_SIZE + " " + message.getScreenWidth() + " " + message.getScreenHeight();
            case NetworkMessage.HIT:
                return HIT;
            case NetworkMessage.DISCONNECT:
                return DISCONNECT;
            default:
                throw new IllegalArgumentException("tipo de mensagem desconhecido: " + message.getType());
        }
    }

    static NetworkMessage decode(String line) {
        String[] parts = line.split(" ", -1);
        try {
            switch (parts[0]) {
                case PROJECTILE:
                    return parts.length == 4 ? decodeProjectile(parts) : null;
                case SCREEN_SIZE:
                    return parts.length == 3 ? decodeScreenSize(parts) : null;
                case HIT:
                    return parts.length == 1 ? new NetworkMessage(NetworkMessage.HIT) : null;
                case DISCONNECT:
                    return parts.length == 1 ? new NetworkMessage(NetworkMessage.DISCONNECT) : null;
                default:
                    return null;
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static NetworkMessage decodeProjectile(String[] parts) {
        float relativeX = Float.parseFloat(parts[1]);
        float speedX = Float.parseFloat(parts[2]);
        float speedY = Float.parseFloat(parts[3]);
        if (!isFinite(relativeX) || !isFinite(speedX) || !isFinite(speedY)) {
            return null;
        }
        NetworkMessage message = new NetworkMessage(NetworkMessage.PROJECTILE);
        message.setRelativeX(relativeX);
        message.setSpeedX(speedX);
        message.setSpeedY(speedY);
        return message;
    }

    private static NetworkMessage decodeScreenSize(String[] parts) {
        int width = Integer.parseInt(parts[1]);
        int height = Integer.parseInt(parts[2]);
        if (width < 1 || width > MAX_SCREEN_DIMENSION || height < 1 || height > MAX_SCREEN_DIMENSION) {
            return null;
        }
        NetworkMessage message = new NetworkMessage(NetworkMessage.SCREEN_SIZE);
        message.setScreenWidth(width);
        message.setScreenHeight(height);
        return message;
    }

    private static String finite(float value) {
        if (!isFinite(value)) {
            throw new IllegalArgumentException("valor que não é um número finito: " + value);
        }
        return Float.toString(value);
    }

    private static boolean isFinite(float value) {
        return !Float.isNaN(value) && !Float.isInfinite(value);
    }
}
