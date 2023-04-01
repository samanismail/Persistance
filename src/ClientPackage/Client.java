/*
 * Nom de classe : Client
 *
 * Description   : Cette classe représente le Client, elle permet de connecter le Client au serveur
 *
 * Version       : 1.0
 *
 * Date          : 28/03/2023
 *
 * Copyright     : Saman ISMAIL, Tristan HOARAU, Delshad KADDO
 */

package ClientPackage;

import java.io.*;
import java.net.*;
public class Client
{

    static boolean arreter=false;//variable qui permet de savoir si le client doit s'arreter

    public static void main(String[] args) throws Exception
    {
        String adresse = "10.192.34.181";//adresse du serveur (tests)
        if(args.length > 0)
        {
            adresse = args[0];//adresse du serveur si on passe une adresse en parametre
        }
        Socket socket = new Socket(adresse,9000);//connexion au serveur
        System.out.println("Vous etes connecte au serveur");
        BufferedReader sisr = new BufferedReader(new InputStreamReader(socket.getInputStream()));//pour lire les messages du serveur

        PrintWriter sisw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);//pour envoyer des messages au serveur

        GererSaisieClient saisie=new GererSaisieClient(sisw, sisr);//thread qui permet de gerer la saisie du client
        saisie.start();//lancement du thread

        String str;
        while(!arreter)//tant que le client n'a pas recu le message "END" du serveur
        {
            if(sisr.ready())//si le serveur a envoye un message
            {
                str = sisr.readLine();//on recupere le message
                if(str.equals("END"))//si le message est "END"
                {
                    arreter = true;//on arrete le client
                    System.out.println(str);//on affiche le message
                }
            }
        }
        sisw.println("END");//dans le cas ou le client s'arrete, on envoie le message "END" au serveur
        sisr.close();//fermeture des flux
        sisw.close();//fermeture des flux
        socket.close();//fermeture de la connexion
        System.exit(0);//arret du client
    }
}