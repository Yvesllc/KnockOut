package com.knockout.rss.knockout;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import android.app.Activity;
import android.graphics.*;
import android.os.Bundle;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.*;

import java.util.Arrays;

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by delachevasnerie on 25/01/2017.
 *
 * Activité chargée de la communication Bluetooth. Elle gère à la fois l'interface et pilote le
 * Bluetooth au moyen de l'appel de la classe FctBluetooth.
 */

// Création de la classe
public class Connecter extends Activity implements OnClickListener {
    private TextView logview; // Zone de texte pour afficher les données reçu par Bluetooth
    private EditText sendtext; // Zone de texte à remplir pour être ensuite envoyé
    private Button connect, send, butStopnRestart, butKnock; // On initialise les boutons

    private FctBluetooth bt = null; // On crée l'objet de connexion Bluetooth

    private long lastTime = 0; // Temporisation
    int tempsRafraichissement = 200; // Temps de refraichissement (Il n'est pas encore établi si c'est un temps correspondant à l'affichage ou à la réception des données)


    // On crée un handler pour gérer du travail parallèle.
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String data = msg.getData().getString("receivedData"); // On reçoit les "data" données envoyées par bluetooth
            String texttemp = "";
            // On extraie les 4 chiffres : data="984,983,1003,43" devient dataOK = [984 983 1003 43]
            // Cette partie plante, apparemment à cause de la forme des données "data" qui peuvent être coupées
            String[] dataTemp = data.split(","); // On découpe le string en fction de la ","
            if (dataTemp[0] != null && dataTemp[0].matches("^\\p{Digit}+$") && dataTemp.length==4) { // On contrôle la présence de qqch, la taille, le fait que ce soit des chiffres
                int[] dataOK = new int[dataTemp.length];
                for (int i = 0; i < 4; i++) { // On remplie le vecteur
                    if (dataTemp[i].matches("^\\p{Digit}+$")) {
                        dataOK[i] = Integer.parseInt(dataTemp[i]);
                        texttemp = (texttemp + dataOK[i]+ ",");
                    }
                    else {texttemp = texttemp + "_DP,_rang_"+i+"_on_a_" + dataTemp[i] + "_FP_";}
                }
            }

            // On met à jour le Logview pour afficher les "data"
            long t = System.currentTimeMillis();
            if (t - lastTime > tempsRafraichissement) {// Pour eviter que les messages soient coupes (??)

                logview.append("Separateur" + "\n");
                logview.append("\n");
                lastTime = System.currentTimeMillis();
            }
            if (logview.getLineCount() < 50) logview.append(texttemp); // On n'ajoute que 10x les data et après on écrase logview
            else {
                logview.setText(texttemp);
                Log.i("Connecter", "Test data" + texttemp);
            }

            //if (logview.getLineCount() < 10) logview.append(data); // On n'ajoute que 10x les data et après on écrase logview
            //else {
            //    logview.setText(data);
            //    Log.i("Connecter", "Test data" + data);
            //}
        }

    };

    //
    final Handler handlerStatus = new Handler() {
        public void handleMessage(Message msg) {
            int co = msg.arg1;
            if(co == 1) {
                logview.append("Connected\n");
            } else if(co == 2) {
                logview.append("Disconnected\n");
            }
        }
    };

    // Méthode de création de l'objet "Connecter" (Vérifier s'il s'agit d'un constructeur ou autre...)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect); // On relie l'interface "connect" à l'activité

        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();

        // Demande d'activation de la fonction Bluetooth (si inactive)
        if (!blueAdapter.isEnabled()) { // i.e. "si le bluetooth est inactif..."
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBlueTooth, RESULT_OK);
        } else { // i.e. "si le bluetooth est actif..."
            Toast.makeText(Connecter.this, "Bluetooth actif", LENGTH_SHORT).show();
        }

        // Création de l'objet fonction Bluetooth
        bt = new FctBluetooth(handlerStatus, handler);

        // Création des liens avec les interfaces du layout "connect"
        logview = (TextView)findViewById(R.id.logview); // Affichage résultat
        sendtext = (EditText)findViewById(R.id.sendtxt); // Affichage texte à envoyer

        connect = (Button)findViewById(R.id.connect); // Bouton pour se connecter
        connect.setOnClickListener(this);

        send = (Button)findViewById(R.id.send); // Bouton pour envoyer un message
        send.setOnClickListener(this);

        // Pour arrêter le processus de chargement des données et le relancer
        butStopnRestart = (Button) findViewById(R.id.butStopnRestart);
        butStopnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bt.getRunningState()){bt.arret();}
                else{bt.restart();}
            }
        });

        // Pour aller sur la page Knock
        butKnock = (Button) findViewById(R.id.butKnock);
        butKnock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent1 = new Intent(Connecter.this, Knock.class);
                startActivity(myIntent1);
            }
        });

    }

    // Pour lancer la connexion au clic et l'envoi des données
    @Override
    public void onClick(View v) {
        if(v == connect) {
            bt.connect(); // !!! C'est ici qu'on appelle l'ojet "bt" de type "FctBluetooth" qui va faire la connexion avec la méthode .connect!!!
            Log.i("KnockOut","A essaye de se connecter");
        } else if(v == send) {
            bt.sendData(sendtext.getText().toString()); // !!! C'est ici qu'on appelle la méthode .sendData pour envoyer via bluetooth des données !!!
        }
    }





}