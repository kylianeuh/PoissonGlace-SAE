package com.sae402.poissonglobe;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.concurrent.Executors;

@Database(entities = {JoueurBD.class, PartieBD.class, JoueurPartieBD.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase bddInstance = null;

    public abstract JeuDAO getJeuDAO();

    public static AppDatabase getAppDatabase(Context context) {
        if (bddInstance == null) {
            synchronized (AppDatabase.class) {
                if (bddInstance == null) {
                    bddInstance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "Jeu-db")
                            .allowMainThreadQueries() // Gardé pour tes requêtes simples
                            .fallbackToDestructiveMigration()
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // Utilisation 'un exécuteur standard pour injecter au premier démarrage
                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        initialiserDonnees();
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return bddInstance;
    }

    private static void initialiserDonnees() {
        try {
            JeuDAO dao = bddInstance.getJeuDAO();
            if (dao != null && dao.getAllJoueurs().isEmpty()) {
                dao.insertJoueur(new JoueurBD("Kylian"));
                dao.insertJoueur(new JoueurBD("Lindsay"));
                dao.insertJoueur(new JoueurBD("Maxime"));
                dao.insertJoueur(new JoueurBD("Ludwig"));
                android.util.Log.d("BDD", "✓ Profils créés proprement !");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}