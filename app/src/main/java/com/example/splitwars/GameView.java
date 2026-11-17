package com.example.splitwars;

import android.content.Context;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class GameView extends View {
    // Fica null até chegar o tamanho da tela do oponente. Sem limite, a nave anda pela tela inteira;
    // uma área vazia aplicada antes disso empurrava a nave para o canto (ver PlayableArea).
    private RectF playableRect = null;
    private NetworkManager networkManager;
    private ShipView shipView;
    private int screenWidth;
    private int screenHeight;
    private boolean pausarJogo = false;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    public GameView(Context context) {
        super(context);
    }

    public GameView(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
    }

    public void initialize(NetworkManager networkManager, boolean isHost, ShipView shipView) {
        this.networkManager = networkManager;
        this.shipView = shipView;

        post(() -> {
            if (getWidth() > 0 && getHeight() > 0) {
                shipView.centralizarNave();
                updatePlayableRect();
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;

        post(() -> {
            shipView.centralizarNave();
            updatePlayableRect();
        });
    }

    public void resume() {
        pausarJogo = false;
        new Thread(() -> {
            while (!pausarJogo) {
                if (networkManager != null) {
                    processNetworkMessages();
                }
                try {
                    Thread.sleep(17);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    public void pause() {
        pausarJogo = true;
    }

    public void reiniciarJogo() {
        pausarJogo = false;
        shipView.resetarNave();
        shipView.iniciarAnimacao();
        resume(); // CORREÇÃO: Reativar o processamento de mensagens de rede
    }

    public void onShipHit() {
        uiHandler.postDelayed(() -> {
            pausarJogo = true;
            shipView.pararAnimacao();

            if (getContext() instanceof GameActivity) {
                ((GameActivity) getContext()).mostrarGameOver("VOCÊ PERDEU!", false);
            }
        }, 2500);

        if (networkManager != null && networkManager.isConnected()) {
            networkManager.sendHit();
        }
    }

    public void onProjectileFired(float x, float y, float speedX, float speedY) {
        // Método mantido para compatibilidade com ShipView listeners
    }

    public void onProjectileHitBorder(float x, float y, float speedX, float speedY) {
        if (networkManager != null && networkManager.isConnected()) {
            float relativeX = x / screenWidth;
            networkManager.sendProjectile(relativeX, speedX, Math.abs(speedY));
        }
    }

    private void processNetworkMessages() {
        NetworkMessage message = networkManager.receiveMessage();
        if (message == null) return;

        switch (message.getType()) {
            case NetworkMessage.PROJECTILE:
                shipView.receberTiroInimigo(message.getRelativeX(), message.getSpeedX(), message.getSpeedY());
                break;

            case NetworkMessage.HIT:
                uiHandler.postDelayed(() -> {
                    pausarJogo = true;
                    shipView.pararAnimacao();

                    if (getContext() instanceof GameActivity) {
                        ((GameActivity) getContext()).mostrarGameOver("VOCÊ GANHOU!", true);
                    }
                }, 2000);
                break;

            case NetworkMessage.DISCONNECT:
                uiHandler.post(() ->
                        Toast.makeText(getContext(), "Oponente desconectou!", Toast.LENGTH_LONG).show()
                );
                break;

            case NetworkMessage.SCREEN_SIZE:
                adjustPlayableArea(message.getScreenWidth(), message.getScreenHeight());
                break;
        }
    }

    private void adjustPlayableArea(int opponentWidth, int opponentHeight) {
        PlayableArea area = PlayableArea.compute(screenWidth, screenHeight, opponentWidth, opponentHeight);
        if (area == null) {
            return;
        }

        playableRect = new RectF(area.left, area.top, area.right, area.bottom);
        updatePlayableRect();
    }

    private void updatePlayableRect() {
        shipView.setLimitesNave(playableRect);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            shipView.disparar();
            return true;
        }
        return super.onTouchEvent(event);
    }
}