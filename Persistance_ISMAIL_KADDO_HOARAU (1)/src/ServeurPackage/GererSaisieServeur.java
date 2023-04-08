/*
 * Nom de classe : GererSaisieServeur
 *
 * Description   :Cette classe permet de gérer les commandes envoyées par le Serveur
 *                Elle permet d'envoyer un message à tous les Clients et Workers
 *                Elle permet d'afficher la liste des Clients et Workers connectés
 *                Elle permet d'arrêter le Serveur
 *
 * Version       : 1.0
 *
 * Date          : 25/03/2023
 *
 * Copyright     : Saman ISMAIL, Tristan HOARAU, Delshad KADDO
 */
package ServeurPackage;

import java.io.*;

class GererSaisieServeur extends Thread
{
    private BufferedReader entreeClavier;//pour lire les commandes envoyées par le serveur

    public GererSaisieServeur(){
        entreeClavier = new BufferedReader(new InputStreamReader(System.in));
    }

    public void run()
    {
        String str;//pour stocker les commandes envoyées par le serveur
        try
        {
            while(!Serveur.arreter)//tant que le serveur n'est pas arrêté
            {
                str = entreeClavier.readLine();//on lit la commande envoyée par le serveur
                switch (str) {
                    case "workers" -> //si la commande est "workers"
                    {

                        for (int i = 0; i < Serveur.maxWorkers; i++)//on affiche la liste des workers
                        {
                            System.out.println("WorkerPackage " + i + " : " + Serveur.ipWorker[i]);
                        }
                    }
                    case "clients" -> //si la commande est "clients"
                    {
                        for (int i = 0; i < Serveur.maxClients; i++) //on affiche la liste des clients
                        {
                            System.out.println("ClientPackage " + i + " : " + Serveur.ipClient[i]);
                        }
                    }
                    case "END" -> Serveur.arreter = true;//si la commande est "END" on arrête le serveur
                    default -> //si la commande est autre
                    {
                        //envoi du message à tous les clients et workers
                        for (int i = 0; i < Serveur.maxClients; i++)
                        {
                            if (Serveur.pwClient[i] != null)
                                Serveur.pwClient[i].println(str);
                        }
                        for (int i = 0; i < Serveur.maxWorkers; i++)
                        {
                            if (Serveur.pwWorker[i] != null)
                                Serveur.pwWorker[i].println(str);
                        }
                    }
                }


            }

        }catch(IOException e){}
    }
}
