/*
 * Nom de classe : EcouterObjets
 *
 * Description   : Cette classe est un thread qui gère l'écoute des objets Hachtable envoyés par le Worker
 *                 Elle permet  de déserialiser les objets et de les stocker dans des fichiers séparés
 *                 pour chaque intervalle de nombres traités afin de pouvoir les réutiliser par la suite
 *                 Elle permet de mettre à jour le fichier maxCalcule.txt qui contient le nombre maximum
 *                 qui a été calculé jusqu'à présent
 *                 Elle permet également de mettre à jour le tableau WorkersDisponibles qui contient les adresses IP
 *                 des Workers disponibles
 *
 * Version       : 1.0
 *
 * Date          : 20/03/2023
 *
 * Copyright     : Saman ISMAIL, Tristan HOARAU, Delshad KADDO
 */
package ServeurPackage;

import Commun.Hachtable;//import Commun.Hachtable pour pouvoir utiliser la classe Hachtable et ses méthodes

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Objects;

class EcouterObjets extends Thread
{

    ObjectOutputStream oos;//Création d'un objet ObjectOutputStream pour créer un fichier sérialisé
    private Socket s;//Création d'un objet Socket pour pouvoir récupérer l'adresse IP du Worker qui a envoyé l'objet Hachtable



    public EcouterObjets()
    {

    }
    public void run()
    {
        try
        {
            while(!Serveur.arreter)
            {
                Socket soc = Serveur.serverSocketEcouteur.accept();//On attend qu'un Worker nous envoie un objet Hachtable
                ObjectInputStream ois = new ObjectInputStream(soc.getInputStream());//On récupère l'objet Hachtable envoyé par le Worker
                Hachtable h = (Hachtable) ois.readObject();//On déserialise l'objet Hachtable
                for(int i=0;i<Serveur.maxWorkers;i++)//On met à jour le tableau WorkersDisponibles
                {
                    //Si l'adresse IP du Worker qui a envoyé l'objet Hachtable est égale à l'adresse IP du Worker i
                    if(Objects.equals(Serveur.ipWorker[i], soc.getInetAddress().toString()))
                    {
                        Serveur.WorkersDisponibles[i]=true;
                    }
                }

                Hashtable<BigInteger, Integer> persistanceA = h.getPersistanceA();//On récupère la persistance additive de l'objet Hachtable
                Hashtable<BigInteger, Integer> persistanceM = h.getPersistanceM();//On récupère la persistance multiplicative de l'objet Hachtable


                // Créez un objet File pour représenter le dossier
                File folder = new File("Multiplicative");

                // Vérifiez si le dossier Multiplicative existe déjà
                if (!folder.exists())//Si le dossier n'existe pas
                {
                    // Créer le dossier en appelant la méthode mkdir() de l'objet File
                    folder.mkdir();
                }
                //pareil pour le dossier Additive
                folder = new File("Additive");
                if (!folder.exists())
                {
                    folder.mkdir();
                }
                //pareil pour le dossier Infos
                folder = new File("Infos");
                if (!folder.exists())
                {
                    folder.mkdir();
                }
                //pareil pour le fichier maxCalcule.txt
                folder = new File("Infos/maxCalcule.txt");
                if (!folder.exists())
                {
                    folder.createNewFile();//On crée le fichier maxCalcule.txt
                }
                //On crée un fichier sérialisé pour la persistance multiplicative
                this.oos = new ObjectOutputStream(new FileOutputStream("Multiplicative/" + h.getDebut() + "-" + h.getFin() + ".ser"));
                this.oos.writeObject(persistanceM);//On écrit la persistance multiplicative dans le fichier sérialisé
                this.oos.flush();//On vide le tampon
                //On crée un fichier sérialisé pour la persistance additive
                this.oos = new ObjectOutputStream(new FileOutputStream("Additive/" + h.getDebut() + "-" + h.getFin() + ".ser"));
                this.oos.writeObject(persistanceA);//On écrit la persistance additive dans le fichier sérialisé
                this.oos.flush();//On vide le tampon
                augmenterMaxCalcule();//On augmente le nombre maximum qui a été calculé jusqu'à présent
                Serveur.ecrireMaxCalculer();//On met à jour le fichier maxCalcule.txt
            }
        }catch (IOException | ClassNotFoundException e) {}
    }

    //Cette méthode permet d'augmenter le nombre maximum qui a été calculé jusqu'à présent
    public void augmenterMaxCalcule()
    {
        Serveur.maxcalcule = Serveur.maxcalcule.add(Serveur.intervalle);
    }

}