package com.sae402.poissonglobe;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {JoueurBD.class, PartieBD.class, JoueurPartieBD.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase bddInstance = null;

    public abstract JeuDAO getJeuDAO();

    public static AppDatabase getAppDatabase(Context context) {
        if (bddInstance == null) {
            synchronized (AppDatabase.class) {
                if (bddInstance == null) {

                    // --- RECONNAISSANCE ET CAPTURE DES CRASHS SYSTEME ---
                    Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
                        android.util.Log.e("POISSON_GLOBE_CRASH", "========================================");
                        android.util.Log.e("POISSON_GLOBE_CRASH", "LE CRASH S'EST PRODUIT ICI : " + thread.getName());
                        android.util.Log.e("POISSON_GLOBE_CRASH", "RAISON du crash : ", throwable);
                        android.util.Log.e("POISSON_GLOBE_CRASH", "========================================");

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
                        System.exit(1);
                    });

                    // --- PATCH DE SÉCURITÉ : Création manuelle du dossier databases ---
                    try {
                        java.io.File dbDir = new java.io.File(context.getApplicationInfo().dataDir + "/databases");
                        if (!dbDir.exists()) {
                            dbDir.mkdir();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // --- INITIALISATION SECURISEE DE ROOM ---
                    bddInstance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "Jeu-db")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            // CORRECTION : Utilisation de onCreate pour injecter les données au moment de la création du fichier
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // On insère immédiatement les personnages de base de façon stable
                                    initialiserDonneesParDefaut();
                                }
                            })
                            .build();
                }
            }
        }
        return bddInstance;
    }

    private static void initialiserDonneesParDefaut() {
        try {
            if (bddInstance != null && bddInstance.getJeuDAO() != null) {
                bddInstance.getJeuDAO().insertJoueur(new JoueurBD("Kylian"));
                bddInstance.getJeuDAO().insertJoueur(new JoueurBD("Lindsay"));
                bddInstance.getJeuDAO().insertJoueur(new JoueurBD("Maxime"));
                bddInstance.getJeuDAO().insertJoueur(new JoueurBD("Ludwig"));
                android.util.Log.d("BDD_SECURITE", "✓ Personnages de base créés avec succès !");
            }
        } catch (Exception e) {
            android.util.Log.e("BDD_SECURITE", "Erreur lors de l'initialisation des données", e);
        }
    }
}