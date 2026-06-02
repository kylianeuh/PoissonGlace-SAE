package com.sae402.poissonglobe;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import androidx.annotation.Nullable;

public class MusiqueService extends Service {

    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. On liste tes 3 musiques de jazz disponibles (bien renommées avec des _)
        int[] listeMusiques = {
                R.raw.waveloom_no_copyright_jazz_cafe,
                R.raw.waveloom_no_copyright_jazz_elegant,
                R.raw.waveloom_no_copyright_piano
        };

        // 2. On choisit un index au hasard entre 0, 1 et 2
        int indexAleatoire = (int) (Math.random() * listeMusiques.length);
        int musiqueChoisie = listeMusiques[indexAleatoire];

        // 3. On initialise le lecteur avec la piste tirée au sort
        mediaPlayer = MediaPlayer.create(this, musiqueChoisie);

        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true); // Celle qui a été choisie bouclera à l'infini
            mediaPlayer.setVolume(0.7f, 0.7f); // Volume à 70% pour une ambiance posée
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // Coupe proprement la musique si l'utilisateur kill l'application
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }
}