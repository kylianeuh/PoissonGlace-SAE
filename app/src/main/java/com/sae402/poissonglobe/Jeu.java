package com.sae402.poissonglobe;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class Jeu  extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // On charge le layout XML qui superpose les assets et le calque Java
        setContentView(R.layout.activity_jeu);
    }
}
