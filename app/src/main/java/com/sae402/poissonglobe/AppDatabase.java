package com.sae402.poissonglobe;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Importezvos classes d'entités et DAO
import com.sae402.poissonglobe.JeuDAO;
import com.sae402.poissonglobe.JoueurBD;
import com.sae402.poissonglobe.JoueurPartieBD;
import com.sae402.poissonglobe.PartieBD;

@Database(entities = {JoueurBD.class, PartieBD.class, JoueurPartieBD.class}, version = 1, exportSchema = false)public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase bddInstance = null;

    public abstract JeuDAO getJeuDAO();public static AppDatabase getAppDatabase(Context context) {
        if (bddInstance == null) {synchronized (AppDatabase.class) {
            if (bddInstance == null) {
                bddInstance= Room.databaseBuilder(context.getApplicationContext(),
                                AppDatabase.class, "Jeu-db")
                        .allowMainThreadQueries()
                        .build();

                initialiserDonneesParDefaut();
            }
        }
        }
        return bddInstance;
    }

    private static void initialiserDonneesParDefaut() {
        if (bddInstance.getJeuDAO().getAllJoueurs().isEmpty()) {
            bddInstance.getJeuDAO().insertJoueur(new JoueurBD("Kylian"));
            bddInstance.getJeuDAO().insertJoueur(new JoueurBD("Adversaire"));
            bddInstance.getJeuDAO().insertJoueur(new JoueurBD("Adversaire2"));
            bddInstance.getJeuDAO().insertJoueur(new JoueurBD("Adversaire3"));
        }
    }
}