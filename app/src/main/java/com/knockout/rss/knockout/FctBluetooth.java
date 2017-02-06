package com.knockout.rss.knockout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class FctBluetooth extends Thread{

    private BluetoothDevice device = null;// le périphérique (le module bluetooth)
    private BluetoothSocket socket = null;
    private InputStream receiveStream = null;// Canal de réception
    private OutputStream sendStream = null;// Canal d'émission

    private ReceiverThread receiverThread;// On crée le thread de réception des données avec l'Handler venant du thread UI

    Handler handler;


    protected volatile boolean running = true;



    public void arret(){running=false;} // Méthode pour stopper le processus (pas forcément utile...)
    public void restart(){running=true;} // Méthode pour reprendre le processus (pas forcément utile... et pas sur que ça fonctionne)
    public boolean getRunningState(){return running;} //  Méthode pour renvoyer l'état de fonctionnement, sert aux méthodes .arret et .restart (on pourrait rassembler les 3 en une seule)

    // Constructeur
    public FctBluetooth(Handler hstatus, Handler h) {
        Log.i("FctBluetooth","Test1"); // Envoi message debbugage
        // On récupère la liste des périphériques associés
        Set<BluetoothDevice> setpairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        BluetoothDevice[] pairedDevices = (BluetoothDevice[]) setpairedDevices.toArray(new BluetoothDevice[setpairedDevices.size()]);
        Log.i("FctBluetooth","Test2, il y a "+pairedDevices.length+ " modules bluetooth pré enregistés"); // Envoi message debbugage
        // On vérifie que la liste n'est pas vide
        if (pairedDevices.length!=0){
            Log.i("FctBluetooth","On cherche le module"); // Envoi message debbugage
            // On parcourt la liste pour trouver notre module bluetooth
            for(int i=0;i<pairedDevices.length;i++) {
                // On teste si ce périphérique contient le nom du module bluetooth connecté au microcontrôleur
                if(pairedDevices[i].getName().contains("HC-06")) {
                    device = pairedDevices[i];
                    try {
                        // On récupère le socket de notre périphérique
                        socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                        receiveStream = socket.getInputStream();// Canal de réception (valide uniquement après la connexion)
                        sendStream = socket.getOutputStream();// Canal d'émission (valide uniquement après la connexion)
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                else
                    Log.i("FctBluetooth","il y a pas le bon module Bluetooth"); // Envoi message debbugage
            }
        }
        else
            Log.i("FctBluetooth","il y a aucun module Bluetooth"); // Envoi message debbugage

        handler = hstatus;

        receiverThread = new ReceiverThread(h); // Crée une classe "receiverThread" définie à la fin de ce script et qui se charge de la réception
    }

    // Méthode d'envoi de données qui fait référence à celle plus complète qui suit
    public void sendData(String data) {sendData(data, false);}

    // Méthode d'envoi de données
    public void sendData(String data, boolean deleteScheduledData) {
        Log.i("FctBluetooth","On envoie des infos"); // Envoi message debbugage
        try {
            // On écrit les données dans le buffer d'envoi
            sendStream.write(data.getBytes());
            // On s'assure qu'elles soient bien envoyées
            sendStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode de connexion
    public void connect() {
        Log.i("FctBluetooth","On se connecte"); // Envoi message debbugage
        new Thread() {
            @Override public void run() {
                try {
                    socket.connect();// Tentative de connexion
                    // Connexion réussie
                    Message msg = handler.obtainMessage();
                    msg.arg1 = 1;
                    handler.sendMessage(msg);

                    receiverThread.start(); // On lance le thread

                } catch (IOException e) {
                    // Echec de la connexion
                    Log.i("FctBluetooth", "Connection Failed : "+e.getMessage()); // Envoi message debbugage
                    e.printStackTrace();
                }
            }
        }.start();
    }

    // ???
    public void close() {
        Log.i("FctBluetooth","On ferme"); // Envoi message debbugage
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode de renvoi du composant bluetooth connecté
    public BluetoothDevice getDevice() {return device;}


    // classe privée qui est appelée par le constructeur. Elle se charge de la réception de données
    private class ReceiverThread extends Thread {
        Handler handler;

        //
        ReceiverThread(Handler h) {
            handler = h;
        }

        @Override
        public void run() {
            Log.i("FctBluetooth","On run"); // Envoi message debbugage
            while(running) {
                try {
                    // On teste si des données sont disponibles
                    if(receiveStream.available() > 0) {

                        byte buffer[] = new byte[1024];
                        // On lit les données, k représente le nombre de bytes lu
                        int k = receiveStream.read(buffer, 0, 1024);

                        if(k > 0) {
                            // On convertit les données en String
                            byte rawdata[] = new byte[k];
                            for(int i=0;i<k;i++)
                                rawdata[i] = buffer[i];

                            String data = new String(rawdata);
                            // On envoie les données dans le thread de l'UI pour les afficher

                            Message msg = handler.obtainMessage();
                            Bundle b = new Bundle();
                            b.putString("receivedData", data);
                            msg.setData(b);
                            handler.sendMessage(msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

