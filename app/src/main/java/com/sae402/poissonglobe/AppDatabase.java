package com.sae402.poissonglobe;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {JoueurBD.class, PartieBD.class, JoueurPartieBD.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase bddInstance = null;

    public abstract JeuDAO getJeuDAO();

// Dans AppDatabase.java, remplacer la méthode getAppDatabase par celle-ci :

    public static AppDatabase getAppDatabase(Context context) {
        if (bddInstance == null) {
            synchronized (AppDatabase.class) {
                if (bddInstance == null) {

                    // --- RECONNAISSANCE ET CAPTURE DES CRASHS SYSTEME ---
                    Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                        // 1. On génère un énorme tag dans les logs faciles à trouver
                        android.util.Log.e("POISSON_GLOBE_CRASH", "========================================");
                        android.util.Log.e("POISSON_GLOBE_CRASH", "LE CRASH S'EST PRODUIT ICI : " + thread.getName());
                        android.util.Log.e("POISSON_GLOBE_CRASH", "RAISON du crash : ", throwable);
                        android.util.Log.e("POISSON_GLOBE_CRASH", "========================================");

                        // 2. Optionnel : Écrire le crash dans un fichier local de l'application
                        try {
                            java.io.File dossier = context.getExternalFilesDir(null);
                            java.io.File fichierLog = new java.io.File(dossier, "crash_log.txt");
                            java.io.FileWriter writer = new java.io.FileWriter(fichierLog, true);
                            java.io.PrintWriter printWriter = new java.io.PrintWriter(writer);
                            printWriter.println("\n--- CRASH DU " + new java.util.Date() + " ---");
                            throwable.printStackTrace(printWriter);
                            printWriter.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // On laisse le système fermer proprement l'app après la sauvegarde du log
                        System.exit(1);
                    });
                    // -----------------------------------------------------

                    bddInstance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "Jeu-db")
                            .allowMainThreadQueries()
                            .build();

                    new Thread(() -> {
                        initialiserDonneesParDefaut();
                    }).start();
                }
            }
        }
        return bddInstance;
    }

    private static void initialiserDonneesParDefaut() {
        try {
            if (bddInstance.getJeuDAO().getAllJoueurs().isEmpty()) {
                bddInstance.getJeuDAO().insertJoueur(new JoueurBD("Kylian"));
                bddInstance.getJeuDAO().insertJoueur(new JoueurBD("Lindsay"));
                bddInstance.getJeuDAO().insertJoueur(new JoueurBD("Maxime"));
                bddInstance.getJeuDAO().insertJoueur(new JoueurBD("Ludwig"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}