/*
 * Nom de classe : Serveur
 *
 * Description   : Cette classe représente le Serveur, elle permet de connecter les Clients et les Workers au serveur
 *                 et de gérer les calculs
 *                 Elle permet de mettre à jour le le fichier maxcalcule.txt de manière synchrone
 *                 Elle permet de créer les tableaux pour les Clients et les Workers
 *                 Elle envoi des requêtes aux Workers pour qu'ils effectuent les calculs
 *                 Elle contient la variable intervalle qui permet de définir l'intervalle de recherche pour chaque Worker
 *
 * Version       : 1.0
 *
 * Date          : 21/03/2023
 *
 * Copyright     : Saman ISMAIL, Tristan HOARAU, Delshad KADDO
 */
package ServeurPackage;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.time.LocalDateTime;

public class Serveur
{
    static final int maxClients=5;//nombre max de clients
    static int numClient=0;//nombre de clients connectés
    static PrintWriter[] pwClient;//tableau des PrintWriter des clients
    static String[] ipClient;//tableau des adresses IP des clients

    static int maxWorkers=5;//nombre max de workers
    static int numWorker=0;//nombre de workers connectés
    static PrintWriter[] pwWorker;//tableau des PrintWriter des workers
    static String[] ipWorker;//tableau des adresses IP des workers

    private static BigInteger nombre;//nombre à tester
    static BigInteger maxcalcule;//dernier nombre testé
    static boolean[] WorkersDisponibles;//tableau des workers disponibles
    static boolean arreter;//variable pour arrêter le serveur
    static ServerSocket serverSocketEcouteur;//socket d'écoute
    static BigInteger intervalle = new BigInteger("100000");//intervalle de recherche pour chaque worker
    static LocalDateTime tps;//temps de début du lancement du serveur

    public static void main(String[] args) throws IOException, InterruptedException
    {
        //vérifier si le fichier maxCalcule.txt existe
        File f = new File("Infos/maxCalcule.txt");
        if(f.exists())
        {
            //lire le fichier maxCalcule.txt
            BufferedReader br = new BufferedReader(new FileReader("Infos/maxCalcule.txt"));
            maxcalcule = new BigInteger(br.readLine());
            nombre = maxcalcule;//on initialise le nombre à tester au dernier nombre testé + l'intervalle
        }
        else//si le fichier maxCalcule.txt n'existe pas
        {
            maxcalcule = new BigInteger("0");//on initialise le dernier nombre testé à 0
            nombre = new BigInteger("0");//on initialise le nombre à tester à 0
        }

        arreter=false;//on initialise la variable arreter à false


        ipClient = new String[maxClients];//on initialise le tableau des adresses IP des clients
        pwClient = new PrintWriter[maxClients];//on initialise le tableau des PrintWriter des clients

        ipWorker = new String[maxWorkers];//on initialise le tableau des adresses IP des workers
        pwWorker = new PrintWriter[maxWorkers];//on initialise le tableau des PrintWriter des workers
        WorkersDisponibles = new boolean[maxWorkers];//on initialise le tableau des workers disponibles


        tps = LocalDateTime.now();//on initialise le temps de début du lancement du serveur à maintenant

        serverSocketEcouteur = new ServerSocket(10000);//on initialise la socket serveur d'écoute
        ServerSocketHandler s2 = new ServerSocketHandler(9000, "client");//on initialise le thread qui va écouter les clients
        ServerSocketHandler s1 = new ServerSocketHandler(8000, "worker");//on initialise le thread qui va écouter les workers

        s1.start();//on lance le thread qui va écouter les workers
        s2.start();//on lance le thread qui va écouter les clients

        GererSaisieServeur saisie = new GererSaisieServeur();//on initialise le thread qui va gérer la saisie du serveur
        saisie.start();//on lance le thread qui va gérer la saisie du serveur

        System.out.println("Serveur en ligne. Adresse IP : "+InetAddress.getLocalHost());//on affiche l'adresse IP du serveur

        while(!arreter)//tant que la variable arreter est à false
        {
            LancerWorkerCalculPersistance();
        }

        //on arrête le serveur en envoyant "END" à tous les clients et tous les workers
        for(int i=0;i<Serveur.maxWorkers;i++)
        {
            if(Serveur.pwWorker[i]!=null)
            {
                Serveur.pwWorker[i].println("END");
            }
        }
        for(int i=0;i<Serveur.maxClients;i++)
        {
            if(Serveur.pwClient[i]!=null)
            {
                Serveur.pwClient[i].println("END");
            }
        }
        Thread.sleep(1000);//on attend 1 seconde
        System.exit(0);//on arrête le serveur

    }

    //méthode pour mettre à jour le fichier maxCalcule.txt de manière synchrone
    static synchronized void ecrireMaxCalculer() throws IOException
    {
        System.out.println("Max calcule : "+maxcalcule);
        BufferedWriter bw = new BufferedWriter(new FileWriter("Infos/maxCalcule.txt"));
        bw.write(maxcalcule.toString());
        bw.close();
    }


    public static BigInteger getNombre()
    {
        return nombre;
    }
    public static void MAJNombre()//méthode pour mettre à jour le nombre à tester
    {
        nombre=nombre.add(intervalle);//on ajoute l'intervalle au nombre à tester
    }

    //méthode pour lancer les workers pour qu'ils effectuent les calculs
    public static void LancerWorkerCalculPersistance()
    {
        for(int i=0;i<maxWorkers;i++)//pour chaque worker
        {
            if(WorkersDisponibles[i])//si le worker est disponible
            {
                //on envoi une requête au worker pour qu'il effectue les calculs
                pwWorker[i].println( getNombre() +" "+intervalle.add(getNombre()).subtract(new BigInteger("1")));
                WorkersDisponibles[i]=false;//on met le worker à indisponible
                MAJNombre();//on met à jour le nombre à tester
            }
        }
    }
}