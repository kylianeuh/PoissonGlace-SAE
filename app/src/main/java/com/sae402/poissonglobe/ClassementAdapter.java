package com.sae402.poissonglobe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ClassementAdapter extends RecyclerView.Adapter<ClassementAdapter.ViewHolder> {

    private List<Joueur> listeJoueurs;

    public ClassementAdapter(List<Joueur> listeJoueurs) {
        this.listeJoueurs = listeJoueurs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_classement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Joueur joueur = listeJoueurs.get(position);

        holder.tvPosition.setText(String.valueOf(position + 1));
        holder.tvPseudo.setText(joueur.getPseudo());
        holder.tvScore.setText(String.valueOf(joueur.getScore()));
    }

    @Override
    public int getItemCount() {
        return listeJoueurs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosition, tvPseudo, tvScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvPseudo = itemView.findViewById(R.id.tvPseudo);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}