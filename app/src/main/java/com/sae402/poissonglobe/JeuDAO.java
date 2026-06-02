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

    @androidx.room.Query("SELECT * FROM JoueurBD WHERE nom = :nomJoueur LIMIT 1")
    JoueurBD getJoueurParNom(String nomJoueur);

    @androidx.room.Insert
    long insertPartie(PartieBD partie);

    @androidx.room.Insert
    void insertJoueurPartie(JoueurPartieBD joueurPartie);


    // --- JOUEUR_PARTIE (Lien & Scores) ---


    @Query("SELECT * FROM JoueurPartieBD WHERE joueur_id = :jId")
    List<JoueurPartieBD> getScoresByJoueur(int jId);

    @Query("SELECT * FROM JoueurPartieBD ORDER BY score DESC LIMIT 10")
    List<JoueurPartieBD> getTopScores();

    // Met à jour les points totaux d'un joueur en lui ajoutant ses nouveaux buts
    @Query("UPDATE JoueurBD SET scoreGlobal = scoreGlobal + :nouveauxButs WHERE id = :joueurId")
    void ajouterPointsGlobaux(int joueurId, int nouveauxButs);

    // Compte le nombre de parties jouées par un joueur
    @Query("SELECT COUNT(*) FROM JoueurPartieBD WHERE joueur_id = :joueurId")
    int getNombrePartiesJouees(int joueurId);

    // Compte le nombre de victoires d'un joueur
    @Query("SELECT COUNT(*) FROM JoueurPartieBD WHERE joueur_id = :joueurId AND resultat = 'VICTOIRE'")
    int getNombreVictoires(int joueurId);



}