/*
 * Nom de classe : Worker
 *
 * Description   : Cette classe représente le Worker, elle gère les calculs des persistances additives et multiplicatives sur un intervalle
 *
 * Version       : 1.0
 *
 * Date          : 30/03/2023
 * 
 * Copyright     : Saman ISMAIL, Tristan HOARAU, Delshad KADDO
 */
package WorkerPackage;

import java.io.*;
import java.math.BigInteger;
import java.net.*;

public class Worker
{
    static ObjectOutputStream oos;//Pour envoyer les objets
    static Socket socketObjets;//pour la connexion avec le serveur
    static int coeurs = Runtime.getRuntime().availableProcessors();//Nombre de coeurs du processeur
    static String adresse;//Adresse du serveur
    static boolean arreter;//Pour arrêter le programme
    public static void main(String[] args) throws Exception
    {

        adresse = "10.192.34.181";//adresse du serveur (tests)

        if(args.length > 0){
            adresse = args[0];//adresse du serveur si on passe une adresse en parametre
        }
        Socket socket = new Socket(adresse,8000);//Connexion au serveur
        System.out.println("SOCKET = " + socket);//Affichage de la connexion
        BufferedReader sisr = new BufferedReader(new InputStreamReader(socket.getInputStream()));//Pour lire les messages du serveur
        PrintWriter sisw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);//Pour envoyer des messages au serveur

        GererSaisieWorker saisie=new GererSaisieWorker(sisw);//thread qui permet de gerer la saisie du worker
        saisie.start();//lancement du thread

        Tache t = new Tache(); /*Permet de gérer l'intervalle entre les différents threads du Worker*/
        
        
        ProcCalcul[] thread_calc;//Tableau de threads
        thread_calc = new ProcCalcul[coeurs];//Declaration du tableau de threads

        //Création des threads
        for(int i=0; i<coeurs; i++)
        {
            thread_calc[i] = new ProcCalcul(t,i);
        }
        //Lancement des threads
        for(int i=0; i<coeurs; i++)
        {
            thread_calc[i].start();
        }

        EnvoyerRes sendfile = new EnvoyerRes(t);//thread qui permet d'envoyer les résultats au serveur
        sendfile.start();//lancement du thread

        String str;//Pour stocker les messages du serveur
        boolean arreter=false;//on initialise la variable d'arret a false

        while(!arreter)//tant que le worker n'a pas reçu de message END du serveur
        {
            if(sisr.ready())//si le serveur a envoyé un message
            {
                str = sisr.readLine();//on stocke le message dans str
                //On vérifie si le serveur ne s'est pas déconnecté
                if(str.equals("END")) {//si le serveur a envoyé un message END
                    arreter=true;//on met la variable d'arret a true
                    sisw.println("END");//on envoie un message END au serveur
                    try{Thread.sleep(500);}catch(Exception e){}//on attend 500ms
                    System.out.println("Le serveur a demandé la fin du programme.");//on affiche un message
                    System.exit(0);//on quitte le programme
                }
                //Réception et calcul de l'intervalle puis envoi
                else if(str.split(" ").length == 2)//si le message contient 2 nombres séparés par un espace
                {
                    BigInteger debut = new BigInteger(str.split(" ")[0]);//on stocke le premier nombre dans debut
                    BigInteger fin = new BigInteger(str.split(" ")[1]);//on stocke le deuxième nombre dans fin
                    t.LancerTache(debut, fin);//on lance la tache et on lui donne debut et fin en parametre
                    Thread.sleep(500);//on attend 500ms
                }
            }
        }

        oos.close();
        sisr.close();
        sisw.close();
        socket.close();
        System.exit(0);
    }

}
