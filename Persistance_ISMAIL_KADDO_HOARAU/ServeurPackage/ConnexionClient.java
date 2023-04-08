/*
 * Nom de classe : ConnexionClient
 *
 * Description   : Cette classe est un thread qui gère la connexion d'un client au serveur
 *                 Elle permet de gérer les requêtes du client et de lui renvoyer les résultats
 *                 Elle gère aussi la déconnexion du client
 *
 * Version       : 1.0
 *
 * Date          : 28/03/2023
 *
 * Copyright     : Saman ISMAIL, Tristan HOARAU, Delshad KADDO
 */

package ServeurPackage;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Objects;

class ConnexionClient extends Thread
{
    private BufferedReader sisr;//BufferedReader pour lire les données envoyées par le client
    private PrintWriter sisw;//PrintWriter pour envoyer des données au client
    private Socket soc;//Socket pour la connexion

    //Constructeur
    public ConnexionClient(Socket s)
    {
        try
        {
            this.soc = s;//On initialise la socket
            sisr = new BufferedReader(new InputStreamReader(s.getInputStream()));//On initialise le BufferedReader
            sisw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);//On initialise le PrintWriter

            //à chaque nouvelle connexion d'un client, on l'ajoute à un emplacement libre dans la liste
            for (int i = 0; i < Serveur.maxClients; i++)
            {
                if (Serveur.pwClient[i] == null) {//Si l'emplacement est libre
                    Serveur.pwClient[i] = sisw;//On ajoute le PrintWriter du client à l'emplacement
                    Serveur.ipClient[i] = s.getInetAddress().toString();//On ajoute l'adresse IP du client à l'emplacement
                    Serveur.numClient++;//On incrémente le nombre de clients connectés
                    System.out.println("ClientPackage " + Serveur.ipClient[i] + " connecté");//On affiche un message de confirmation
                    break;
                }
            }
        }
        catch (IOException e)
        {}
    }

    //Méthode run du thread qui gère la connexion du client
    public void run()
    {
        try
        {
            String str;
            while (Serveur.numClient < Serveur.maxClients) //Tant que le nombre de clients connectés est inférieur au nombre maximum de clients
            {
                if (sisr.ready()) //Si le BufferedReader est prêt à lire
                {
                    str = sisr.readLine();//On lit la ligne envoyée par le client
                    if (str.equals("END")) //Si le client a envoyé "END"
                    {
                        for (int i = 0; i < Serveur.maxClients; i++) //On parcourt la liste des clients
                        {
                            if (Objects.equals(Serveur.ipClient[i], soc.getInetAddress().toString())) //Si l'adresse IP du client correspond à l'adresse IP du client qui a envoyé "END"
                            {
                                Serveur.pwClient[i] = null;//On supprime le PrintWriter du client de la liste
                                Serveur.ipClient[i] = null;//On supprime l'adresse IP du client de la liste
                                Serveur.numClient--;//On décrémente le nombre de clients connectés
                                System.out.println("ClientPackage " + soc.getInetAddress().toString() + " déconnecté");//On affiche un message de confirmation
                                System.out.println("Nombre de clients connectés :" + Serveur.numWorker);//On affiche le nombre de clients connectés
                                break;
                            }
                        }
                    }
                    else//Si le client a envoyé une requête différente de "END"
                    {
                        System.out.println("ClientPackage " + soc.getInetAddress().toString() + " a demandé : " + str);//On affiche la requête du client
                        String[] requete = str.split(" ");//On sépare la requête en plusieurs chaînes de caractères
                        //Si les calculs n'ont pas commencé
                        if(Serveur.maxcalcule.compareTo(new BigInteger("0")) == 0)
                        {
                            sisw.println("Impossible d'effectuer la requete, les calculs n'ont pas commences.");//On envoie un message d'erreur au client
                            Thread.sleep(5);//On attend 5ms
                            sisw.println("finreponse");//On envoie "finreponse" au client pour indiquer la fin de la réponse
                        }
                        else
                        {
                            //Si la requête est sur un intervalle non-calculé
                            if( requete.length == 4 && !requete[2].equals("all") && ( (new BigInteger(requete[2])).compareTo(Serveur.maxcalcule.subtract(BigInteger.ONE)) > 0
                                    || (new BigInteger(requete[3])).compareTo(Serveur.maxcalcule.subtract(BigInteger.ONE)) > 0) )
                            {
                                this.sisw.println("Impossible d'effectuer la requete, cet intervalle n'a pas encore ete calcule.  Nombre actuel max : "+Serveur.maxcalcule);
                                Thread.sleep(500);
                                sisw.println("finreponse");
                            }
                            else if( requete.length == 3 && !requete[2].equals("all") && ((new BigInteger(requete[2])).compareTo(Serveur.maxcalcule.subtract(BigInteger.ONE)) > 0) )
                            {
                                this.sisw.println("Impossible d'effectuer la requete, cet intervalle n'a pas encore ete calcule. Nombre actuel max : "+Serveur.maxcalcule);
                                Thread.sleep(500);
                                sisw.println("finreponse");
                            }
                            else
                            {
                                switch (requete[0]) //On regarde le premier mot de la requête
                                {
                                    case ("mul"):
                                    case ("add"):
                                        if (Objects.equals(requete[1], "pi"))
                                        {
                                            PersistancesIntervalle(requete);break;//Si le deuxième mot est "pi", on appelle la méthode PersistancesIntervalle
                                        }
                                        else if(Objects.equals(requete[1], "nbpmax"))
                                        {
                                            ListeNbPersistMax(requete);break;//Si le deuxième mot est "nbpmax", on appelle la méthode ListeNbPersistMax
                                        }
                                    case ("comp")://Si le premier mot est "comp", on regarde le deuxième mot
                                        switch (requete[1]) //On regarde le deuxième mot de la requête
                                        {
                                            case ("pn"): //Si le deuxième mot est "pn", on appelle la méthode PersistanceNb
                                            {
                                                PersistanceNb(requete[0], requete[2]);
                                                break;
                                            }
                                            case ("pmax") ://Si le deuxième mot est "pmax", on appelle la méthode PersistanceMax
                                            {
                                                PersistanceMax(requete);
                                                break;
                                            }

                                            case ("op")://Si le deuxième mot est "op", on appelle la méthode OccurencePers
                                            {
                                                OccurencePers(requete);
                                                break;
                                            }
                                            case ("moy")://Si le deuxième mot est "moy", on appelle la méthode MoyPersistance
                                            {
                                                MoyPersistance(requete);
                                                break;
                                            }
                                            case ("med") :
                                            {
                                                MedPersistance(requete);//Si le deuxième mot est "med", on appelle la méthode MedPersistance
                                                break;
                                            }
                                        }
                                        break;
                                    case("stat")://Si le premier mot est "stat", on appelle la méthode AfficherStatServ
                                        AfficherStatServ();
                                        break;
                                }
                                if(!requete[0].equals("stat")){
                                Thread.sleep(500);
                                } 
                                sisw.println("finreponse");//On envoie "finreponse" pour indiquer au client que la réponse est terminée dans tous les cas quand la requête est correcte
                            }
                        }
                    }
                }
            }
        }
        catch (IOException | InterruptedException | ClassNotFoundException e)
        {}


    }

    //Méthode qui calcule la persistance maximale d'un intervalle ou de tous les nombres en multiplicative ou additive
    private void ListeNbPersistMax(String[] requete) throws IOException, ClassNotFoundException, InterruptedException
    {
        String fichier = "";//Chaine de caractère qui contiendra le nom du fichier dans lequel on va écrire

        if(requete[0].equals("add"))//Si le premier mot de la requête est "add", on définit le type de persistance et le nom du fichier
        {
            fichier = "Additive/";
        }
        else if(requete[0].equals("mul"))//Si le premier mot de la requête est "mul", on définit le type de persistance et le nom du fichier
        {
            fichier = "Multiplicative/";
        }

        BigInteger debut = new BigInteger("0");//Définition du début de l'intervalle
        BigInteger fin = Serveur.maxcalcule.subtract(BigInteger.ONE);//Définition de la fin de l'intervalle

        if(!requete[2].equals("all"))//Si le troisième mot de la requête est différent de "all", on définit le début et la fin de l'intervalle à partir de la requête
        {
            debut = new BigInteger(requete[2]);
            fin = new BigInteger(requete[3]);
        }

        int persMax = PersistanceMax(requete);//On calcule la persistance maximale de l'intervalle
        sisw.println("Voici la liste des nombres ayant cette persistance : ");
        BigInteger i = debut.divide(Serveur.intervalle).multiply(Serveur.intervalle);//On définit le premier multiple de 100000 inférieurs ou égal à debut
        ArrayList<BigInteger> list = new ArrayList<>();//On crée une liste qui va contenir tous les multiples de 100000 inférieurs à fin

        //on ajoute tous les multiples de 100000 dans une liste inférieurs à fin
        while (i.compareTo(fin) <= 0)
        {
            list.add(i);
            i = i.add(Serveur.intervalle);
        }
        for (BigInteger b : list) //Pour chaque multiple de 100000, on regarde si le nombre est dans l'intervalle et si sa persistance est égale à la persistance maximale
        {
            FileInputStream fis = new FileInputStream(fichier+ b +"-"+b.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");//On ouvre le fichier correspondant
            ObjectInputStream ois = new ObjectInputStream(fis);//On crée un ObjectInputStream pour lire le fichier
            Hashtable<BigInteger, Integer> h = (Hashtable<BigInteger, Integer>) ois.readObject();//On lit le fichier et on le stocke dans une Hashtable

            //On regarde si chaque nombre de la Hashtable est dans l'intervalle et si sa persistance est égale à la persistance maximale
            for(BigInteger key : h.keySet())
            {
                if(key.compareTo(debut)>=0 && key.compareTo(fin)<=0)
                {
                    if(h.get(key) == persMax)
                    {
                        sisw.println(key);
                        Thread.sleep(5);
                    }
                }
            }
            ois.close();
            fis.close();
        }
    }

    //cette méthode calcule la persistance sur un intervalle donné
    private void PersistancesIntervalle(String[] requete) throws IOException, ClassNotFoundException, InterruptedException
    {

        String fichier = "";
        String type = "";

        //on définit le type de persistance et le nom du dossier en fonction du premier mot de la requête
        if(requete[0].equals("add"))
        {
            fichier = "Additive/";
            type = "additive";
        }
        else if(requete[0].equals("mul"))
        {
            fichier = "Multiplicative/";
            type = "multiplicative";
        }

        BigInteger debut = new BigInteger(requete[2]);//debut de l'intervalle
        BigInteger fin = new BigInteger(requete[3]);//fin de l'intervalle
        BigInteger i = debut.divide(Serveur.intervalle).multiply(Serveur.intervalle);//on définit le premier multiple de 100000 inférieurs ou égal à debut
        ArrayList<BigInteger> list = new ArrayList<>();//on crée une liste qui va contenir tous les multiples de 100000 inférieurs à fin

        //on ajoute tous les multiples de 100000 dans une liste inférieurs à fin
        while (i.compareTo(fin) <= 0)
        {
            list.add(i);
            i = i.add(Serveur.intervalle);
        }

        //on ouvre chque fichier dans la liste et on ajoute les résultats dans la table de hachage
        for (BigInteger b : list)
        {
            FileInputStream fis = new FileInputStream(fichier+ b +"-"+b.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            Hashtable<BigInteger, Integer> h = (Hashtable<BigInteger, Integer>) ois.readObject();
            for(BigInteger key =b;key.compareTo(b.add(Serveur.intervalle).subtract(BigInteger.ONE))<=0;key=key.add(BigInteger.ONE))
            {
                if(key.compareTo(debut)>=0 && key.compareTo(fin)<=0)
                {
                    sisw.println(type+" ( "+key+" ) = "+h.get(key));
                    Thread.sleep(1);
                }

            }
            ois.close();
            fis.close();
        }
    }

    //cette méthode calcule mediane de persistance sur un intervalle donné ou sur tout l'intervalle en persistence multiplicative ou additive
    private BigInteger MedPersistance(String[] requete) throws IOException, ClassNotFoundException, InterruptedException {
        String fichier = "";
        String type = "";
        //on définit le type de persistance et le nom du dossier en fonction du premier mot de la requête
        if(requete[0].equals("add"))
        {
            fichier = "Additive/";
            type = "additive";
        }
        else if(requete[0].equals("mul"))
        {
            fichier = "Multiplicative/";
            type = "multiplicative";
        }

        if(!type.equals(""))//si le type est défini, on calcule la médiane
        {
            BigInteger debut = new BigInteger("0");//définition du début de l'intervalle
            BigInteger fin = Serveur.maxcalcule.subtract(BigInteger.ONE);//définition de la fin de l'intervalle

            if(!requete[2].equals("all"))//si l'intervalle n'est pas tout l'intervalle
            {
                debut = new BigInteger(requete[2]);//on définit le début de l'intervalle en fonction de la requête
                fin = new BigInteger(requete[3]);//on définit la fin de l'intervalle en fonction de la requête
            }
            BigInteger med;//on crée une variable qui va contenir la médiane

            int[] ValPersistances = new int[12];//on crée un tableau qui va contenir le nombre de nombres ayant une persistance égale à l'indice du tableau

            //on initialise le tableau des persistances à 0
            for(int i = 0; i<12;i++)
            {
                ValPersistances[i] = 0;
            }

            BigInteger i = debut.divide(Serveur.intervalle).multiply(Serveur.intervalle);//on définit le premier multiple de 100000 inférieurs ou égal à debut
            ArrayList<BigInteger> list = new ArrayList<>();//on crée une liste qui va contenir tous les multiples de 100000 inférieurs à fin

            //on ajoute tous les multiples de 100000 dans une liste inférieurs à fin
            while (i.compareTo(fin) <= 0)
            {
                list.add(i);
                i = i.add(Serveur.intervalle);
            }

            //on ouvre chque fichier dans la liste et on incrémente le nombre de nombres ayant une persistance égale à l'indice du tableau
            for (BigInteger b : list)
            {
                FileInputStream fis = new FileInputStream(fichier+ b +"-"+b.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");
                ObjectInputStream ois = new ObjectInputStream(fis);
                Hashtable<BigInteger, Integer> h = (Hashtable<BigInteger, Integer>) ois.readObject();
                for (BigInteger key : h.keySet())
                {
                    if(key.compareTo(debut)>=0 && key.compareTo(fin)<=0)
                    {
                        ValPersistances[h.get(key)] ++;
                    }
                }
                ois.close();
                fis.close();
            }

            BigInteger total = (fin.subtract(debut)).add(BigInteger.ONE); /*Nombre total d'éléments calculés*/
            BigInteger pariteTot = total.mod(BigInteger.TWO);   /*Parité du total*/
            BigInteger indice = new BigInteger("0");
            int nb_val = 0;                                     /*Nb d'élts parcourus dans la case d'indice indiceTab dans le tableau des valeurs de persistances*/
            int indiceTab = 0;                                  /*Indice du tableau courant*/

            //Si le total est impair, la médiane des persistances est la valeur au milieu
            if( (pariteTot).compareTo(BigInteger.ZERO) != 0)
            {
                //Tant que je ne suis pas à la valeur du milieu du nombre d'éléments
                while( indice.compareTo(total.divide(BigInteger.TWO).add(BigInteger.ONE)) < 0)
                {
                    //Si le nombre dépasse le nombre d'élements égal à la première valeur de persistance
                    while(nb_val >= ValPersistances[indiceTab])
                    {
                        indiceTab ++;
                        nb_val = 0;
                    }
                    nb_val ++;                                  /*J'incrémente le nombre d'éléments parcourus pour la première valeur de persistance*/
                    indice = indice.add(BigInteger.ONE);        /*J'incrémente aussi le nombre d'éléments parcourus dans le nombre total d'éléments calculés*/
                }
                med = new BigInteger(indiceTab+"");             /*La médiane correspond à l'indice du tableau pour lequel la moitié du nombre d'éléments total calculés a été atteint*/
            }
            else
            {
                //Tant que je ne suis pas à la valeur du milieu du nombre d'éléments
                while( indice.compareTo( total.divide(BigInteger.TWO)) < 0)
                {
                    //Si le nombre dépasse le nombre d'élements égal à la première valeur de persistance
                    while(nb_val >= ValPersistances[indiceTab])
                    {
                        indiceTab ++;
                        nb_val = 0;
                    }
                    nb_val ++;                                  /*J'incrémente le nombre d'éléments parcourus pour la première valeur de persistance*/
                    indice = indice.add(BigInteger.ONE);        /*J'incrémente aussi le nombre d'éléments parcourus dans le nombre total d'éléments calculés*/

                }
                int val1 = indiceTab;                           /*(n/2) ème élément*/
                int val2 = indiceTab;                           /*(n/2)+1 ème élément*/
                if( (nb_val+1) > ValPersistances[indiceTab] ) /*Si le (n/2)+1e élément est sur la valeur de persistance suivante */
                {
                    val2 = indiceTab + 1;
                }
                med = ( new BigInteger(val1+"").add(new BigInteger(val2+""))).divide(BigInteger.TWO);//on calcule la médiane
            }
            sisw.println("La médiane de la persistance "+type+" sur l'intervalle " + debut + "-" + fin+ " est "+med);//on envoie la médiane au client
            return med;
        }
        else if (requete[0].equals("comp"))//si la requête est une comparaison
        {

            String[] requetecomp = new String[4];//on crée une requête de comparaison

            //on initialise la requête de comparaison
            System.arraycopy(requete, 0, requetecomp, 0, requete.length);

            //si l'intervalle est all on définit le début à 0 et fin au nombre maximum calculé par les Workers
            if(requetecomp[2].equals("all"))
            {
                requetecomp[2] = "0";
                requetecomp[3] = Serveur.maxcalcule.toString();
            }

            //on calcule la médiane de la persistance multiplicative sur l'intervalle
            requetecomp[0] = "mul";
            BigInteger PersistanceMoyMul = MedPersistance(requetecomp);
            Thread.sleep(200);

            //on calcule la médiane de la persistance additive sur l'intervalle
            requetecomp[0] = "add";
            BigInteger PersistanceMoyAdd = MedPersistance(requetecomp);
            Thread.sleep(200);

            //on compare les médianes et on envoie le résultat au client
            if(PersistanceMoyAdd.compareTo(PersistanceMoyMul) > 0)
            {
                sisw.println("La médiane de la persistance additive sur cette intervalle est superieure à celle de la persistance multiplicative sur l'intervalle " + requetecomp[2] + "-" + requetecomp[3]);
            }
            else if(PersistanceMoyMul.compareTo(PersistanceMoyAdd) > 0)
            {
                sisw.println("La médiane de la persistance multiplicative sur cette intervalle est superieure à celle de la persistance additive sur l'intervalle " + requetecomp[2] + "-" + requetecomp[3]);
            }
            else
            {
                sisw.println("Les médianes des persistances additives et multiplicatives sont égales sur l'intervalle " + requetecomp[2] + "-" + requetecomp[3]);
            }
            return new BigInteger("1");
        }
        return new BigInteger("0");
    }

    //méthode qui calcule la persistance d'un nombre
    private void PersistanceNb(String type, String nombre) throws IOException, ClassNotFoundException, InterruptedException
    {
        //chercher le fichier qui contient le nombre
        BigInteger nb = new BigInteger(nombre);
        BigInteger i = nb.divide(Serveur.intervalle).multiply(Serveur.intervalle);

        String fichier1 ;
        String fichier2 ;

        Hashtable<BigInteger, Integer> h;//on crée une hashtable pour stocker les nombres et leur persistance

        if(type.equals("add"))//si la persistance est additive
        {
            fichier1  ="Additive/";//on définit le chemin du fichier
            FileInputStream fis = new FileInputStream(fichier1+i+"-"+i.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");//on ouvre le fichier
            ObjectInputStream ois = new ObjectInputStream(fis);//on crée un objet pour lire le fichier
            h = (Hashtable<BigInteger, Integer>) ois.readObject();//on lit le fichier et on le stocke dans la hashtable
            sisw.println("La persistance additive de " + nb + " est " + h.get(nb));//on envoie la persistance additive du nombre au client
        }
        else if(type.equals("mul"))//si la persistance est multiplicative
        {
            fichier1 = "Multiplicative/";//on définit le chemin du fichier
            FileInputStream fis = new FileInputStream(fichier1+i+"-"+i.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");//on ouvre le fichier
            ObjectInputStream ois = new ObjectInputStream(fis);//on crée un objet pour lire le fichier
            h = (Hashtable<BigInteger, Integer>) ois.readObject();//on lit le fichier et on le stocke dans la hashtable
            sisw.println("La persistance multiplicative de " + nb + " est " + h.get(nb));//on envoie la persistance multiplicative du nombre au client
        }
        else if(type.equals("comp"))//si la persistance est à comparer
        {
            //on définit le chemin des deux fichiers à aller chercher
            fichier1  ="Additive/";
            fichier2 = "Multiplicative/";

            //on ouvre les deux fichiers, on les lit et on les stocke dans deux hashtables
            FileInputStream fis = new FileInputStream(fichier1+i+"-"+i.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            Hashtable<BigInteger, Integer> h1 = (Hashtable<BigInteger, Integer>) ois.readObject();
            fis = new FileInputStream(fichier2+i+"-"+i.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");
            ois = new ObjectInputStream(fis);
            Hashtable<BigInteger, Integer> h2 = (Hashtable<BigInteger, Integer>) ois.readObject();

            //on envoie le resultat de chaque persistance
            sisw.println("La persistance additive de " + nb + " est " + h1.get(nb));
            Thread.sleep(50);
            sisw.println("La persistance multiplicative de " + nb + " est " + h2.get(nb));
            Thread.sleep(50);

            //on compare les deux persistances et on envoie le résultat au client
            if(h1.get(nb) > h2.get(nb))
            {
                sisw.println("La persistance additive de " + nb + " est plus grande que sa persistance multiplicative");
            }
            else if(h1.get(nb) < h2.get(nb))
            {

                sisw.println("La persistance multiplicative de " + nb + " est plus grande que sa persistance additive");
            }
            else
            {
                sisw.println("La persistance additive de " + nb + " est égale à sa persistance multiplicative");
            }
        }
    }

    //Méthode qui calcule le nombre d'occurence d'un nombre
    private void OccurencePers(String[] requette) throws IOException, ClassNotFoundException, InterruptedException
    {
        String fichier1 = "Additive/";//on définit le chemin du fichier
        String fichier2 = "Multiplicative/";//on définit le chemin du fichier
        String type = "";

        if(requette[0].equals("add"))//si la persistance est additive
        {
            fichier1 = "Additive/";//on définit le chemin du fichier
            type = "additive";//on définit le type de persistance
        }
        else if(requette[0].equals("mul"))//si la persistance est multiplicative
        {
            fichier1 = "Multiplicative/";//on définit le chemin du fichier
            type = "multiplicative";//on définit le type de persistance
        }

        BigInteger maxCalcule = new BigInteger(Serveur.maxcalcule.toString());//on crée un BigInteger pour le maxCalcule
        BigInteger occurence = new BigInteger("0");//on crée un BigInteger pour le nombre occurrence
        BigInteger debut;//on crée un BigInteger pour le début de l'intervalle
        BigInteger fin;//on crée un BigInteger pour la fin de l'intervalle
        BigInteger i;
        FileInputStream fis = null;
        ObjectInputStream ois=null;
        Hashtable<BigInteger, Integer> h;//on crée une hashtable pour stocker les nombres et leur persistance
        ArrayList<BigInteger> list = new ArrayList<>();//on crée une liste pour stocker les fichiers à lire

        //on ajoute tous les multiples de 100000 dans une liste inférieurs à fin
        if(requette[0].equals("add") || requette[0].equals("mul"))
        {
            if(requette[2].equals("all"))//si on veut tout calculer
            {
                //on définit le début et la fin de l'intervalle
                debut = new BigInteger("0");
                i = new BigInteger("0");
                fin = maxCalcule;
            }
            else//si on veut calculer un intervalle
            {
                debut = new BigInteger(requette[2]);//on définit le début de l'intervalle
                i = debut.divide(Serveur.intervalle).multiply(Serveur.intervalle);//on définit le début du fichier à lire
                fin = new BigInteger(requette[3]);//on définit la fin de l'intervalle
            }
            //on ajoute tous les multiples de 100000 dans une liste inférieurs à fin
            while (i.compareTo(fin) <= 0)
            {
                list.add(i);
                i = i.add(Serveur.intervalle);
            }
            //on parcourt la liste des fichiers à lire
            for (BigInteger b : list)
            {
                fis = new FileInputStream(fichier1+ b +"-"+b.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");//on ouvre le fichier
                ois = new ObjectInputStream(fis);//on crée un objet pour lire le fichier
                h = (Hashtable<BigInteger, Integer>) ois.readObject();//on lit le fichier et on le stocke dans la hashtable
                for(BigInteger key : h.keySet())//on parcourt la hashtable
                {
                    if(requette[2].equals("all"))//si on veut tout calculer
                    {
                        if(key.compareTo(debut)>=0 && key.compareTo(fin)<=0)//si le nombre est dans l'intervalle
                        {
                            if(h.get(key) == Integer.parseInt(requette[3]))//si la persistance est égale à la persistance demandée
                            {
                                occurence = occurence.add(BigInteger.ONE);//on incrémente le nombre d'occurence
                            }

                        }
                    }
                    else
                    {//si on veut calculer un intervalle
                        if(key.compareTo(debut)>=0 && key.compareTo(fin)<=0)//si le nombre est dans l'intervalle
                        {
                            if(h.get(key) == Integer.parseInt(requette[4])) //si la persistance est égale à la persistance demandée
                            {
                                occurence = occurence.add(BigInteger.ONE);//on incrémente le nombre d'occurence
                            }
                        }
                    }

                }
                ois.close();
                fis.close();
            }
            if(requette[2].equals("all"))//si on veut tout calculer
            {
                sisw.println("Il y a " + occurence + " nombres entre "+ debut + " et "+fin + " dont la persistance "+type+ " est égale à " + requette[3]);//on envoie le résultat au client
            }
            else//si on veut calculer un intervalle
            {
                sisw.println("Il y a " + occurence + " nombres entre "+ debut + " et "+fin + " dont la persistance "+type+ " est égale à " + requette[4]);//on envoie le résultat au client
            }

        }
        else//si on veut comparer les persistance additive et multiplicative
        {
            if(requette[2].equals("all"))//si on veut tout calculer
            {
                i = new BigInteger("0");//on définit le début du fichier à lire
                debut = new BigInteger("0");//on définit le début de l'intervalle
                fin = maxCalcule;//on définit la fin de l'intervalle
                //on ajoute tous les multiples de 100000 dans une liste inférieurs à fin
                while (i.compareTo(fin) <= 0)
                {
                    list.add(i);
                    i = i.add(Serveur.intervalle);
                }

                BigInteger occurenceMul = new BigInteger("0");//on crée un BigInteger pour le nombre d'occurence multiplicative
                BigInteger occurenceAdd = new BigInteger("0");//on crée un BigInteger pour le nombre d'occurence additive

                //on parcourt la liste des fichiers à lire pour la persistance additive
                for (BigInteger b : list)
                {
                    fis = new FileInputStream(fichier1+ b +"-"+b.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");//on ouvre le fichier
                    ois = new ObjectInputStream(fis);//on crée un objet pour lire le fichier
                    h = (Hashtable<BigInteger, Integer>) ois.readObject();//on lit le fichier et on le stocke dans la hashtable
                    for(BigInteger key : h.keySet())//on parcourt la hashtable
                    {
                        if(key.compareTo(debut)>=0 && key.compareTo(fin)<=0)//si le nombre est dans l'intervalle
                        {
                            if (h.get(key) == Integer.parseInt(requette[3]))//si la persistance est égale à la persistance demandée
                            {
                                occurenceAdd = occurenceAdd.add(BigInteger.ONE);//on incrémente le nombre d'occurence additive
                            }

                        }

                    }
                }
                sisw.println("Il y a " + occurenceAdd + " nombres entre "+ debut + " et "+fin + " dont la persistance additive est égale à " + requette[3]);//on envoie le résultat au client
                Thread.sleep(50);

                //on parcourt la liste des fichiers à lire pour la persistance multiplicative
                for (BigInteger b : list)
                {
                    fis = new FileInputStream(fichier2+ b +"-"+b.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");//on ouvre le fichier
                    ois = new ObjectInputStream(fis);//on crée un objet pour lire le fichier
                    h = (Hashtable<BigInteger, Integer>) ois.readObject();//on lit le fichier et on le stocke dans la hashtable
                    for(BigInteger key : h.keySet())//on parcourt la hashtable
                    {
                        if(key.compareTo(debut)>=0 && key.compareTo(fin)<=0)//si le nombre est dans l'intervalle
                        {
                            if(h.get(key) == Integer.parseInt(requette[3]))//si la persistance est égale à la persistance demandée
                            {
                                occurenceMul = occurenceMul.add(BigInteger.ONE);//on incrémente le nombre d'occurence multiplicative
                            }
                        }

                    }
                }
                sisw.println("Il y a " + occurenceMul + " nombres entre "+ debut + " et "+fin + " dont la persistance multiplicative est égale à " + requette[3]);//on envoie le résultat au client
                Thread.sleep(50);

                //on compare les deux persistances pour savoir laquelle est la plus fréquente
                if(occurenceAdd.compareTo(occurenceMul) > 0)
                {
                    sisw.println("La persistance additive est la plus fréquente");
                }

                else if(occurenceAdd.compareTo(occurenceMul) < 0)
                {
                    sisw.println("La persistance multiplicative est la plus fréquente");
                }

                else
                {
                    sisw.println("Les persistance additive et multiplicative sont égales en fréquence");
                }
                Thread.sleep(50);
            }
            else//si on veut calculer un intervalle
            {
                i=new BigInteger(requette[2]).divide(Serveur.intervalle).multiply(Serveur.intervalle);//on définit le début du fichier à lire
                fin = new BigInteger(requette[3]);//on définit la fin de l'intervalle
                debut = new BigInteger(requette[2]);//on définit le début de l'intervalle

                //on ajoute tous les multiples de 100000 dans une liste inférieurs à fin
                while (i.compareTo(fin) <= 0)
                {
                    list.add(i);
                    i = i.add(Serveur.intervalle);
                }

                BigInteger occurenceMul = new BigInteger("0");//on crée un BigInteger pour le nombre d'occurence multiplicative
                BigInteger occurenceAdd = new BigInteger("0");//on crée un BigInteger pour le nombre d'occurence additive

                //on parcourt la liste des fichiers à lire pour la persistance additive
                for (BigInteger b : list)
                {
                    fis = new FileInputStream(fichier1+ b +"-"+b.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");//on ouvre le fichier
                    ois = new ObjectInputStream(fis);//on crée un objet pour lire le fichier
                    h = (Hashtable<BigInteger, Integer>) ois.readObject();//on lit le fichier et on le stocke dans la hashtable
                    for(BigInteger key : h.keySet())//on parcourt la hashtable
                    {
                        if(key.compareTo(debut)>=0 && key.compareTo(fin)<=0)//si le nombre est dans l'intervalle
                        {
                            if(h.get(key) == Integer.parseInt(requette[4]))//si la persistance est égale à la persistance demandée
                            {
                                occurenceAdd = occurenceAdd.add(BigInteger.ONE);//on incrémente le nombre d'occurence additive
                            }
                        }
                    }
                }
                sisw.println("Occurence de la persistance additive de "+requette[4]+" entre "+debut+" et "+fin+" : "+occurenceAdd);//on envoie le résultat au client
                Thread.sleep(50);
                //on parcourt la liste des fichiers à lire pour la persistance multiplicative
                for (BigInteger b : list)
                {
                    fis = new FileInputStream(fichier2+ b +"-"+b.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");//on ouvre le fichier
                    ois = new ObjectInputStream(fis);//on crée un objet pour lire le fichier
                    h = (Hashtable<BigInteger, Integer>) ois.readObject();//on lit le fichier et on le stocke dans la hashtable
                    for(BigInteger key : h.keySet())//on parcourt la hashtable
                    {
                        if(key.compareTo(debut)>=0 && key.compareTo(fin)<=0)//si le nombre est dans l'intervalle
                        {
                            if(h.get(key) == Integer.parseInt(requette[4]))//si la persistance est égale à la persistance demandée
                            {
                                occurenceMul = occurenceMul.add(BigInteger.ONE);//on incrémente le nombre d'occurence multiplicative
                            }
                        }
                    }
                }
                sisw.println("Occurence de la persistance multiplicative de "+requette[4]+" entre " +debut+" et "+fin+" : "+occurenceMul);//on envoie le résultat au client
                Thread.sleep(50);

                //on compare les deux persistances pour savoir laquelle est la plus fréquente
                if(occurenceAdd.compareTo(occurenceMul) > 0)
                {
                    sisw.println("La persistance additive est la plus fréquente");
                }

                else if(occurenceAdd.compareTo(occurenceMul) < 0)
                {
                    sisw.println("La persistance multiplicative est la plus fréquente");
                }

                else
                {
                    sisw.println("Les persistance additive et multiplicative sont égales en fréquence");
                }
                Thread.sleep(50);
            }


        }


    }

    //méthode qui calcule la persistance maximum d'un intervalle
    private int PersistanceMax(String [] requete) throws IOException, ClassNotFoundException, InterruptedException {
        String fichier = "";//on crée une chaine de caractère pour le nom du fichier
        String type = "";//on crée une chaine de caractère pour le type de persistance

        //on définit le type de persistance et le nom du fichier en fonction de la requête
        if(requete[0].equals("add"))
        {
            fichier = "Additive/";
            type = "additive";
        }
        else if(requete[0].equals("mul"))
        {
            fichier = "Multiplicative/";
            type = "multiplicative";
        }

        if(!type.equals(""))//si le type de persistance est défini
        {
            BigInteger debut = new BigInteger("0");//on définit le début de l'intervalle
            BigInteger fin = Serveur.maxcalcule.subtract(BigInteger.ONE);//on définit la fin de l'intervalle

            if(!requete[2].equals("all"))//si on veut calculer un intervalle
            {
                debut = new BigInteger(requete[2]);//on définit le début de l'intervalle en fonction de la requête
                fin = new BigInteger(requete[3]);//on définit la fin de l'intervalle en fonction de la requête
            }

            int PersistanceMax = 0;//on crée un entier pour la persistance maximum
            BigInteger i = debut.divide(Serveur.intervalle).multiply(Serveur.intervalle);//on crée un BigInteger pour le début de l'intervalle
            ArrayList<BigInteger> list = new ArrayList<>();//on crée une liste pour stocker les multiples de 100000 inférieurs à fin

            //on ajoute tous les multiples de 100000 dans une liste inférieurs à fin
            while (i.compareTo(fin) <= 0)
            {
                list.add(i);
                i = i.add(Serveur.intervalle);
            }

            //on parcourt la liste des fichiers à lire
            for (BigInteger b : list)
            {
                FileInputStream fis = new FileInputStream(fichier+ b +"-"+b.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");//on ouvre le fichier
                ObjectInputStream ois = new ObjectInputStream(fis);//on crée un objet pour lire le fichier
                Hashtable<BigInteger, Integer> h = (Hashtable<BigInteger, Integer>) ois.readObject();//on lit le fichier et on le stocke dans la hashtable

                int val;//on crée un entier pour stocker la persistance d'un nombre

                if(list.indexOf(b) != (list.size() - 1))//si on n'est pas sur le dernier fichier
                {
                    for(BigInteger key = b; key.compareTo(b.add(Serveur.intervalle).subtract(BigInteger.ONE)) <= 0; key = key.add(BigInteger.ONE)){//on parcourt la hashtable
                        {
                            if ((val = h.get(key)) > PersistanceMax)//si la persistance est supérieure à la persistance maximum
                            {
                                PersistanceMax = val;//on met à jour la persistance maximum
                            }
                        }
                    }
                }
                else{//si on est sur le dernier fichier
                    for(BigInteger key = b; key.compareTo(fin) <= 0; key = key.add(BigInteger.ONE)){//on parcourt la hashtable
                        if( (val = h.get(key)) > PersistanceMax){//si la persistance est supérieure à la persistance maximum
                            PersistanceMax = val;//on met à jour la persistance maximum
                        }
                    }
                }
                ois.close();
                fis.close();
            }
            sisw.println("La persistance "+type+" maximale sur l'intervalle " + debut + "-" + fin+ " est "+PersistanceMax);//on envoie le résultat au client

            return PersistanceMax;
        }
        else if (requete[0].equals("comp"))//si on veut comparer les persistance additive et multiplicative
        {
            String[] requetecomp = new String[4];//on crée un tableau de chaine de caractère pour stocker la requête

            //on copie la requête dans le tableau
            System.arraycopy(requete, 0, requetecomp, 0, requete.length);

            //on définit le début et la fin de l'intervalle si on veut calculer sur tout l'intervalle déja calculé
            if(requetecomp[2].equals("all"))
            {
                requetecomp[2] = "0";
                requetecomp[3] = Serveur.maxcalcule.toString();
            }

            //on calcule la persistance maximum pour la persistance multiplicative et la persistance additive
            requetecomp[0] = "mul";
            int PersistanceMaxMul = PersistanceMax(requetecomp);
            requetecomp[0] = "add";
            int PersistanceMaxAdd = PersistanceMax(requetecomp);

            Thread.sleep(200);//on attend 200ms pour laisser le temps au client de recevoir les résultats

            //on compare les deux persistances et on envoie le résultat au client
            if(PersistanceMaxAdd > PersistanceMaxMul)
            {
                sisw.println("La persistance additive maximale sur cette intervalle est superieure a la persistance mutliplicative maximale");
            }
            else if(PersistanceMaxAdd < PersistanceMaxMul)
            {
                sisw.println("La persistance multiplicative sur cette intervalle est superieure a la persistance additive maximale");
            }
            else
            {
                sisw.println("Les deux persistances sont egales");
            }
            return 1;
        }
        return 0;
    }

    //méthode pour calculer la moyenne de persistance
    private BigInteger MoyPersistance(String[] requete) throws IOException, ClassNotFoundException, InterruptedException
    {
        String fichier = "";//on crée une chaine de caractère pour stocker le nom du fichier
        String type = "";//on crée une chaine de caractère pour stocker le type de persistance

        //on définit le nom du fichier et le type de persistance
        if(requete[0].equals("add"))
        {
            fichier = "Additive/";
            type = "additive";
        }
        else if(requete[0].equals("mul"))
        {
            fichier = "Multiplicative/";
            type = "multiplicative";
        }

        //on vérifie que le type de persistance est bien défini
        if(!type.equals(""))
        {
            BigInteger debut = new BigInteger("0");//on définit le début de l'intervalle en fonction de la requête
            BigInteger fin = Serveur.maxcalcule.subtract(BigInteger.ONE);//on définit la fin de l'intervalle en fonction de la requête

            //on définit le début et la fin de l'intervalle si on veut calculer sur un intervalle défini
            if(!requete[2].equals("all"))
            {
                debut = new BigInteger(requete[2]);//on définit le début de l'intervalle en fonction de la requête
                fin = new BigInteger(requete[3]);//on définit la fin de l'intervalle en fonction de la requête
            }

            BigInteger moy = new BigInteger("0");//on crée un entier pour stocker la moyenne

            int[] ValPersistances = new int[12];//on crée un tableau d'entier pour stocker les valeurs de persistance

            BigInteger i = debut.divide(Serveur.intervalle).multiply(Serveur.intervalle);//on crée un entier pour stocker le début de l'intervalle

            ArrayList<BigInteger> list = new ArrayList<>();//on crée une liste pour stocker les multiples de 100000

            //on ajoute tous les multiples de 100000 dans une liste inférieurs à fin
            while (i.compareTo(fin) <= 0)
            {
                list.add(i);
                i = i.add(Serveur.intervalle);
            }

            //on parcourt la liste des noms de fichier
            for (BigInteger b : list)
            {
                FileInputStream fis = new FileInputStream(fichier+ b +"-"+b.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");//on ouvre le fichier
                ObjectInputStream ois = new ObjectInputStream(fis);//on crée un objet pour lire le fichier
                Hashtable<BigInteger, Integer> h = (Hashtable<BigInteger, Integer>) ois.readObject();//on crée une hashtable pour stocker les valeurs du fichier


                for(BigInteger key : h.keySet())//on parcourt la hashtable
                {
                    if(key.compareTo(debut) >= 0 && key.compareTo(fin) <= 0)//si la clé est dans l'intervalle
                    {
                        ValPersistances[h.get(key)] ++;//on incrémente la valeur de persistance
                    }
                }
                ois.close();
                fis.close();
            }

            //on calcule la moyenne de chaque persistance
            for(int a=0; a<ValPersistances.length;a++)
            {
                moy = moy.add( (new BigInteger(a+"")).multiply(new BigInteger(ValPersistances[a]+"")) );
            }
            moy = moy.divide(fin.subtract(debut).add(BigInteger.ONE));//on divise par le nombre de valeurs
            sisw.println("La persistance "+type+" moyenne sur l'intervalle " + debut + "-" + fin+ " est "+moy);//on envoie le résultat au client
            return moy;//on retourne la moyenne
        }
        else if (requete[0].equals("comp")){//si on veut comparer les deux persistances
            String[] requetecomp = new String[4];//on crée un tableau de chaine de caractère pour stocker la requête

            //on copie la requête dans le nouveau tableau
            System.arraycopy(requete, 0, requetecomp, 0, requete.length);

            //on vérifie si on veut calculer sur tout l'intervalle
            if(requetecomp[2].equals("all"))
            {
                requetecomp[2] = "0";
                requetecomp[3] = Serveur.maxcalcule.toString();
            }
            //on calcule la persistance moyenne mutliplicative
            requetecomp[0] = "mul";
            BigInteger PersistanceMoyMul = MoyPersistance(requetecomp);
            Thread.sleep(200);
            //on calcule la persistance moyenne additive
            requetecomp[0] = "add";
            BigInteger PersistanceMoyAdd = MoyPersistance(requetecomp);
            Thread.sleep(200);
            //on compare la persistance moyenne mutliplicative et additive et on envoie le résultat au client
            if(PersistanceMoyAdd.compareTo(PersistanceMoyMul) > 0)
            {
                sisw.println("La persistance additive moyenne sur cette intervalle est superieure à la persistance multiplicative moyenne");
            }
            else if(PersistanceMoyMul.compareTo(PersistanceMoyAdd) > 0)
            {
                sisw.println("La persistance multiplicative moyenne sur cette intervalle est superieure à la persistance additive moyenne");
            }
            else{
                sisw.println("Les persistances moyennes additives et multiplicatives sont égales");
            }
            return new BigInteger("1");
        }
        return new BigInteger("0");
    }

    //méthode pour afficher les statistiques du serveur
    public void AfficherStatServ() throws InterruptedException {
        String str = "Il y a actuellement "+Serveur.numWorker+" workers connectés.\n";//on crée une chaine de caractère pour stocker le nombre de workers
        sisw.println(str);//on envoie la chaine de caractère au client

        Thread.sleep(20);//on attend 20ms
        str = "Il y a actuellement "+Serveur.numClient+" clients connectés.\n";//on crée une chaine de caractère pour stocker le nombre de clients
        sisw.println(str);//on envoie la chaine de caractère au client
        Thread.sleep(20);//on attend 20ms

        str = "Le nombre maximum ayant été calculé est "+Serveur.maxcalcule.toString()+".\n";//on crée une chaine de caractère pour stocker le nombre maximum ayant été calculé
        sisw.println(str);//on envoie la chaine de caractère au client
        Thread.sleep(20);//on attend 20ms

        LocalDateTime t1 = Serveur.tps;//on crée un objet LocalDateTime pour stocker l'heure de lancement du serveur
        LocalDateTime t2 = LocalDateTime.now();//on crée un objet LocalDateTime pour stocker l'heure actuelle
        Duration duree = Duration.between(t1, t2);//on crée un objet Duration pour stocker la durée entre l'heure de lancement et l'heure actuelle

        //on crée une chaine de caractère pour stocker la durée entre l'heure de lancement et l'heure actuelle
        str = "Le serveur est lancé depuis : "+ duree.toHours() +" heures, "+ duree.toMinutes() +" minutes et "+ duree.toSeconds()+ " secondes ( Heure de lancement : "+t1.getHour()+":"+t1.getMinute()+":"+t1.getSecond()+" "+t1.getDayOfMonth()+"/"+t1.getMonthValue()+"/"+t1.getYear()+" ).\n";
        sisw.println(str);//on envoie la chaine de caractère au client
        Thread.sleep(20);//On attend 20ms
        sisw.println("");
    }
}