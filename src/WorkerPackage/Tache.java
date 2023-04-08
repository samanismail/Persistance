/*
 * Nom de classe : Tache
 *
 * Description   : Cette classe gère la répartition de l'intervalle reçu par le Worker entre ses différents threads, ainsi
 *                 que l'écriture des résultats dans les Hashtable et l'envoi du fichier. Elle fait le lien entre l'ensemble 
 *                 des threads de calcul et d'envoi du Worker.
 *
 * Version       : 1.0
 *
 * Date          : 28/03/2023
 * 
 * Copyright     : Saman ISMAIL, Tristan HOARAU, Delshad KADDO
 */
package WorkerPackage;

import Commun.Hachtable;

import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.math.BigInteger;


class Tache
{
    private BigInteger min;//borne inférieure de l'intervalle
    private BigInteger max;//borne supérieure de l'intervalle
    private BigInteger debut;//borne inférieure de l'intervalle pour chaque thread
    private BigInteger fin;//borne supérieure de l'intervalle pour chaque thread
    private Hashtable<BigInteger, Integer> persistanceAdditive ;//Hashtable contenant les persistances additive
    private Hashtable<BigInteger, Integer> persistanceMultiplicative;//Hashtable contenant les persistances multiplicative
    private boolean end_calc;//indique si les calculs sont terminés
    private boolean[] threads_finis;//indique si les threads de calcul sont terminés
    private Socket socketObjets;//pour la connexion avec le serveur
    private int coeurs;//nombre de coeurs du processeur
    private String adresse;

    public Tache(String adresse,int coeurs) throws IOException {
        this.coeurs = coeurs;//on initialise le nombre de coeurs
        this.adresse = adresse;
        this.min = new BigInteger("0");//on initialise la borne inférieure à 0
        this.debut = min;//on initialise la borne inférieure de l'intervalle pour chaque thread à 0
        this.max = new BigInteger("-1");//on initialise la borne supérieure à -1
        this.fin = max;//on initialise la borne supérieure de l'intervalle pour chaque thread à -1
        this.persistanceAdditive = new Hashtable<>();//on initialise la Hashtable des persistances additive
        this.persistanceMultiplicative = new Hashtable<>();//on initialise la Hashtable des persistances multiplicative
        this.end_calc = true;//on initialise end_calc à true au lancement de la tâche
        this.threads_finis = new boolean[this.coeurs];//on déclare le tableau des threads
        //on initialise le tableau des threads à true
        for( int i = 0; i < this.coeurs; i++)
        {
            this.threads_finis[i] = true;
        }
    }

    //Cette méthode permet de récupérer la borne inférieure de l'intervalle
    public Hashtable<BigInteger, Integer> getHashtableMult()
    {
        return this.persistanceMultiplicative;
    }

    //Cette méthode permet de récupérer la borne supérieure de l'intervalle
    public Hashtable<BigInteger, Integer> getHashtableAdd()
    {
        return this.persistanceAdditive;
    }

    //Cette méthode permet de connaître l'état des threads
    public boolean getStatutThreads()
    {
        boolean ok = false;//on initialise ok à false
        for(boolean statut : this.threads_finis)//on parcourt le tableau des threads
        {
            ok = statut;//on met ok à true si tous les threads sont terminés
        }
        return ok;//on retourne ok
    }

    //Cette méthode permet de mettre à jour l'état d'un thread avec son indice
    public void setStatutThreads(int i, boolean bool)
    {
        this.threads_finis[i] = bool;
    }


    //Cette méthode permet de réinitialiser les attributs de la classe
    public synchronized void LancerTache(BigInteger min, BigInteger max)
    {
        this.min = min;
        this.max = max;

        this.debut = min;
        this.fin = max;

        this.end_calc = false;

        System.out.println("Tache "+debut+ "-"+fin+" lancee");
        notify();//on notifie les threads en attente
    }

    //Cette méthode permer de récuperer le nombre à calculer
    public synchronized BigInteger getNumber()
    {
        while(end_calc)//si les calculs sont terminés, le thread est mis en attente
        {
            try{this.wait();}catch(Exception e){};
        }
        //on met end_calc à false si le nombre à calculer est inférieur à la borne supérieure
        end_calc = !(this.min.compareTo(this.max) < 0);
        BigInteger res = this.min;//on récupère le nombre à calculer
        if(!end_calc)//si les calculs ne sont pas terminés
        {
            this.min = this.min.add(BigInteger.ONE);//on incrémente le nombre à calculer
            notify();//on notifie les threads en attente
        }
        return res;//on retourne le nombre à calculer
    }

    //Cette méthode permet d'envoyer les résultats de la tache au serveur
    public synchronized void EnvoyerPersistances() {
        if (!(this.max.compareTo(new BigInteger("-1")) == 0))//si l'intervalle n'est pas vide
        {
            if (!getStatutThreads() || !end_calc)//si les threads ne sont pas terminés ou si les calculs ne sont pas terminés
            {
                try
                {
                    this.wait();//on met le thread en attente
                }
                catch (Exception e)
                {}
            }
            else//si les threads sont terminés et que les calculs sont terminés
            {
                try
                {
                    this.reset();//on réinitialise les attributs de la classe
                    //on se connecte au serveur
                    this.socketObjets = new Socket(adresse, 10000);//on initialise la connexion avec le serveur (tests
                    ObjectOutputStream oos= new ObjectOutputStream(socketObjets.getOutputStream());
                    Hachtable hm = new Hachtable(this.debut, this.fin, getHashtableAdd(), getHashtableMult());
                    //on envoie les résultats au serveur
                    oos.writeObject(hm);
                    oos.flush();//on vide le buffer
                    //on réinitialise les Hashtables
                    this.persistanceMultiplicative.clear();
                    this.persistanceAdditive.clear();
                    //on affiche le résultat
                    System.out.println(debut + "-" + fin + " envoye");
                    oos.close();//on ferme la connexion
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    //Cette méthode permet d'ajouter une persistance additive à la Hashtable
    public synchronized void ajouteAdd(BigInteger nb, int pers)
    {
        this.persistanceAdditive.put(nb, pers);//on ajoute la persistance additive à la Hashtable
        notify();//on notifie les threads en attente
    }

    //Cette méthode permet d'ajouter une persistance multiplicative à la Hashtable
    public synchronized void ajouteMult(BigInteger nb, int pers)
    {
        this.persistanceMultiplicative.put(nb, pers);//on ajoute la persistance multiplicative à la Hashtable
        notify();//on notifie les threads en attente
    }

    //Cette méthode permet de réinitialiser les attributs de la classe
    public void reset()
    {
        this.min = new BigInteger("0");
        this.max = new BigInteger("-1");
    }
}