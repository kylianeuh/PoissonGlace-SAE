package com.sae402.poissonglobe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SelectionJoueursAdapter extends RecyclerView.Adapter<SelectionJoueursAdapter.ViewHolder> {

    private List<JoueurBD> listeJoueurs;
    private OnJoueurClickListener listener;

    public interface OnJoueurClickListener {
        void onJoueurClick(String nom);
    }

    public SelectionJoueursAdapter(List<JoueurBD> listeJoueurs, OnJoueurClickListener listener) {
        this.listeJoueurs = listeJoueurs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stats_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JoueurBD joueur = listeJoueurs.get(position);

        holder.tvPseudo.setText(joueur.nom);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onJoueurClick(joueur.nom);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listeJoueurs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPseudo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPseudo = itemView.findViewById(R.id.tvPseudoSelection);
        }
    }
}