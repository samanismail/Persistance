/*
 * Nom de classe : ConnexionWorker
 *
 * Description   : Cette classe gére la connexion d'un worker au serveur et l'ajoute à la liste des workers
 *                connectés, elle gère aussi la déconnexion d'un worker du serveur en le retirant de la liste
 *
 *
 * Version       : 1.0
 *
 * Date          : 28/03/2023
 *
 * Copyright     : Saman ISMAIL, Tristan HOARAU, Delshad KADDO
 */
package ServeurPackage;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

class ConnexionWorker extends Thread
{
    private BufferedReader sisr;//pour lire les données envoyées par le client
    private Socket s;//pour envoyer des données au client
    private EcouterObjets eo;//pour écouter les objets envoyés par le client

    public ConnexionWorker(Socket s) throws IOException
    {
        this.s=s;
        sisr = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintWriter sisw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);

        //on ajoute le worker à un emplacement libre dans la liste
        for(int i=0;i<Serveur.maxWorkers;i++)
        {
            if(Serveur.pwWorker[i]==null)//si l'emplacement est libre
            {
                Serveur.pwWorker[i]=sisw;//on ajoute le worker à l'emplacement
                Serveur.ipWorker[i]=s.getInetAddress().toString();//on ajoute l'adresse IP du worker à l'emplacement
                Serveur.WorkersDisponibles[i]=true;//on indique que le worker est disponible
                System.out.println("WorkerPackage " + Serveur.ipWorker[i] +" connecté");//on affiche un message de confirmation
                Serveur.numWorker++;//on incrémente le nombre de workers connectés

                eo=new EcouterObjets();//on crée un nouveau thread pour écouter les objets envoyés par le worker
                eo.start();//on démarre le thread
                break;
            }
        }
    }
    public void run()
    {

        try
        {
            while (Serveur.numWorker < Serveur.maxWorkers)
            {
                String str=sisr.readLine();
                if (str!= null && str.equals("END"))//si le worker envoie "END"
                {
                    //retrait du worker de la liste en fonction de son adresse IP
                    for(int i=0;i<Serveur.maxWorkers;i++)
                    {
                        if(Objects.equals(Serveur.ipWorker[i], s.getInetAddress().toString()))
                        {
                            Serveur.pwWorker[i]=null;
                            Serveur.ipWorker[i]=null;
                            Serveur.WorkersDisponibles[i]=false;
                            Serveur.numWorker--;
                            System.out.println("WorkerPackage " +s.getInetAddress()+" déconnecté");
                            System.out.println("Nombre de workers connectés : "+Serveur.numWorker);
                        }
                    }
                }
            }
        }catch (IOException e) {}
    }
}