package com.sae402.poissonglobe;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface JeuDAO {

    // --- JOUEURS ---

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertJoueur(JoueurBD joueur);

    @Query("SELECT * FROM JoueurBD")
    List<JoueurBD> getAllJoueurs();

    @Query("SELECT * FROM JoueurBD WHERE nom = :nom LIMIT 1")
    JoueurBD getJoueurByNom(String nom);


    // --- PARTIES ---

    @Insert
    long insertPartie(PartieBD partie);


    // --- JOUEUR_PARTIE (Lien & Scores) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertJoueurPartie(JoueurPartieBD joueurPartie);

    @Query("SELECT * FROM JoueurPartieBD WHERE joueur_id = :jId")
    List<JoueurPartieBD> getScoresByJoueur(int jId);

    @Query("SELECT * FROM JoueurPartieBD ORDER BY score DESC LIMIT 10")
    List<JoueurPartieBD> getTopScores();
}