package com.sae402.poissonglobe;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface JeuDAO {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertJoueur(JoueurBD joueur);

    @Query("SELECT * FROM JoueurBD")
    List<JoueurBD> getAllJoueurs();

    @Query("SELECT * FROM JoueurBD WHERE nom = :nomJoueur LIMIT 1")
    JoueurBD getJoueurParNom(String nomJoueur);

    @Query("SELECT * FROM JoueurBD WHERE nom = :nomJoueur LIMIT 1")
    JoueurBD getJoueurByNom(String nomJoueur);


    @Insert
    long insertPartie(PartieBD partie);

    @Insert
    long insertJoueurPartie(JoueurPartieBD joueurPartie);


    @Query("SELECT * FROM JoueurPartieBD WHERE joueur_id = :jId")
    List<JoueurPartieBD> getScoresByJoueur(int jId);

    @Query("SELECT * FROM JoueurPartieBD ORDER BY score DESC LIMIT 10")
    List<JoueurPartieBD> getTopScores();

    @Query("UPDATE JoueurBD SET scoreGlobal = scoreGlobal + :nouveauxButs WHERE id = :joueurId")
    void ajouterPointsGlobaux(int joueurId, int nouveauxButs);

    @Query("SELECT COUNT(*) FROM JoueurPartieBD WHERE joueur_id = :joueurId")
    int getNombrePartiesJouees(int joueurId);

    @Query("SELECT COUNT(*) FROM JoueurPartieBD WHERE joueur_id = :joueurId AND resultat = 'VICTOIRE'")
    int getNombreVictoires(int joueurId);
}