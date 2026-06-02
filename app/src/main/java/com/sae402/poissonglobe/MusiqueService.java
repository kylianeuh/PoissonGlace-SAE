package com.sae402.poissonglobe;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
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

        int[] listeMusiques = {
                R.raw.waveloom_no_copyright_jazz_cafe,
                R.raw.waveloom_no_copyright_jazz_elegant,
                R.raw.waveloom_no_copyright_piano
        };

        int indexAleatoire = (int) (Math.random() * listeMusiques.length);
        int musiqueChoisie = listeMusiques[indexAleatoire];

        // CORRECTION CRITIQUE ANR : On initialise manuellement le MediaPlayer sans bloquer le thread
        mediaPlayer = new MediaPlayer();

        // Configuration des attributs audio (indispensable pour les bonnes pratiques Android)
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build());

        try {
            // Liaison sécurisée avec le fichier brut du dossier res/raw
            android.content.res.AssetFileDescriptor afd = getResources().openRawResourceFd(musiqueChoisie);
            if (afd != null) {
                mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();

                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(0.7f, 0.7f);

                // Écouteur : se déclenche dès que le décodage asynchrone en tâche de fond est fini
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        // Le thread principal est libre, la musique démarre sans aucun à-coup !
                        mediaPlayer.start();
                        Log.d("MUSIQUE_SERVICE", "La piste audio asynchrone est prête et démarre.");
                    }
                });

                // On lance la préparation en tâche de fond (Asynchrone = ZÉRO FREEZE)
                mediaPlayer.prepareAsync();
            }
        } catch (Exception e) {
            Log.e("MUSIQUE_SERVICE", "Erreur lors du chargement asynchrone du fichier audio", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // La lecture est maintenant gérée par le OnPreparedListener
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
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }
}