package com.knockout.rss.knockout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.Button;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;


/**
 * Created by delachevasnerie on 12/01/2017.
 *
 * MainActivity: activité principale appelée au démarrage de l'application. Elle appelle le layout
 * activity_main, qui contient l'interface pour appeler les autres activités.
 *
 * Fichier dans Git
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) { // On réécrit la méthode de création de l'activité
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // On appelle le layout activity_main

        // Création Bouton "butKnock" qui appellera l'activité Knock
        Button butKnock = (Button) findViewById(R.id.buttonknock);
        butKnock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent1 = new Intent(MainActivity.this, Knock.class);
                startActivity(myIntent1);
            }
        });

        // Création Bouton "butHistorique" qui appellera l'activité Historique
        Button butHistorique = (Button) findViewById(R.id.buttonhistorique);
        butHistorique.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent3 = new Intent(MainActivity.this, Historique.class);
                startActivity(myIntent3);
            }
        });

        // Création Bouton "butParametre" qui appellera l'activité Parametre
        Button butParametre = (Button) findViewById(R.id.buttonparametre);
        butParametre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent4 = new Intent(MainActivity.this, Parametre.class);
                startActivity(myIntent4);
            }
        });

        // Création Bouton "butConnecter" qui appellera l'activité Connecter
        Button butConnecter = (Button) findViewById(R.id.buttonconnecter);
        butConnecter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent5 = new Intent(MainActivity.this, Connecter.class);
                startActivity(myIntent5);
            }
        });

        // TEST pour afficher du texte html
        final TextView textview = (TextView) findViewById(R.id.textview);
        final InputStream stream = getResources().openRawResource(R.raw.content);
        final String text;
        try {
            text = IOUtils.toString(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        textview.setText(Html.fromHtml(text));
    }


    // TEST Création du Menu qui dépend des ressources du dossier "res/menu"
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // TEST Création des actions qui seront effectuées sur le menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
            /* DO EDIT */
                return true;
            case R.id.action_add:
            /* DO ADD */
                return true;
            case R.id.action_delete:
            /* DO DELETE */
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}