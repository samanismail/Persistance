/*
 * Nom de classe : GererSaisieClient
 *
 * Description   : Cette classe gère les requêtes envoyées par le Client au Serveur.
 *
 * Version       : 1.0
 *
 * Date          : 28/03/2023
 *
 * Copyright     : Saman ISMAIL, Tristan HOARAU, Delshad KADDO
 */
package ClientPackage;// Path: src\ClientPackage\GererSaisieClient.java

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;

class GererSaisieClient extends Thread
{
    private final BufferedReader entreeClavier;//pour lire les requetes du client
    private final PrintWriter pw;//pour envoyer les requetes au serveur
    private final BufferedReader sisr;//pour lire les reponses du serveur

    public GererSaisieClient(PrintWriter pw, BufferedReader sisr)
    {
        entreeClavier = new BufferedReader(new InputStreamReader(System.in));
        this.pw=pw;
        this.sisr=sisr;
    }

    public void run()
    {
        String requete = "";//requete envoyee au serveur
        try
        {
            while(!Client.arreter)//tant que le client n'a pas tape END
            {
                requete = MenuPrincipal();//on affiche le menu principal
                switch (requete) {
                    case ("END") ->
                    {
                        System.out.println("Fin du programme, deconnexion du client");
                        pw.println("END");
                        Client.arreter = true;
                    }
                    case ("mul") ->MenuPers(requete);//on affiche le menu de persistance multiplicative
                    case ("add") ->MenuPers(requete);//on affiche le menu de persistance additive
                    case ("comp") ->MenuPersComp(requete);//on affiche le menu de comparaison
                    case ("stat") -> StatGenServ();//on affiche les statistiques du serveur
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String MenuPrincipal() throws IOException
    {
        //cette methode affiche le menu principal et renvoie la requete du client
        System.out.println("\nTapez END pour quitter\n");
        System.out.println("Pour choisir une option dans un menu, veuillez taper la touche correspondante indiquee entre les parenthsese.");
        System.out.println("Veuillez choisir les donnees que vous souhaitez consulter : ");
        System.out.println("  - Persistance Multiplicative (mul)\n  - Persistance Additive (add)\n  - Comparaison des deux persistances (comp)\n  - Statistiques serveur (stat)");


        String requete = entreeClavier.readLine();//on lit la reponse du client
        //on verifie que la reponse est valide
        while( !requete.equals("mul") && !requete.equals("add") && !requete.equals("comp") &&
                !requete.equals("stat") && !requete.equals("END"))
        {
            System.out.println("Requete invalide, veuillez reessayer");
            requete = entreeClavier.readLine();
        }
        return requete;
    }

    public void MenuPers(String requete) throws IOException, InterruptedException {
        String type="";
        //on définit le type de persistance
        if(requete.equals("mul")) type = "multiplicative";
        else if(requete.equals("add")) type = "additive";
        System.out.println("\nTapez RETURN pour revenir en arriere\n");
        System.out.println("Persistance "+type+" -- Que souhaitez-vous consulter ? : ");
        System.out.println("""
                  - Persistance Maximale (pmax)
                  - Moyenne de la persistance (moy)
                  - Mediane de la persistance (med)
                  - Valeur de la persistance d'un nombre (pn)
                  - Valeur des persistances sur un intervalle (pi)
                  - Nombre d'occurences d'une persistance (op)
                  -Liste des nombres avec la persistance maximale (nbpmax)
                  \
                """);


        String choix = entreeClavier.readLine();//on lit la reponse du client
        //on verifie que la reponse est valide
        while( !choix.equals("RETURN") && !choix.equals("pmax") && !choix.equals("moy") && !choix.equals("med") && !choix.equals("pn")
                && !choix.equals("pi") && !choix.equals("op") && !choix.equals("nbpmax"))
        {
            System.out.println("Requete invalide, veuillez reessayer");
            choix = entreeClavier.readLine();
        }
        requete = requete + " " + choix;

        String choix2 = "";
        //on demande si le client souhaite le resultat sur un intervalle ou sur l'ensemble des nombres calcules
        switch(choix)
        {
            case("nbpmax"):// si le client souhaite la liste des nombres avec la persistance maximale
            case("pmax"):// si le client souhaite la persistance maximale
            case("op"):// si le client souhaite le nombre d'occurences d'une persistance
            case("moy"):// si le client souhaite la moyenne de la persistance
            case("med"):// si le client souhaite la mediane de la persistance
                System.out.println("\nSouhaitez-vous le resultat sur un intervalle (int) ou l'ensemble des nombres calcules (all) ?");
                choix2 = entreeClavier.readLine();
                while( !choix2.equals("int") && !choix2.equals("all") )
                {
                    System.out.println("Requete invalide, veuillez reessayer");
                    choix2 = entreeClavier.readLine();
                }
                if(choix2.equals("int"))//si le client souhaite le resultat sur un intervalle
                {
                    String min = "";
                    String max = "";
                    System.out.println("\nVeuillez choisir le debut de l'intervalle : ");
                    min = entreeClavier.readLine();//on lit le debut de l'intervalle
                    //on verifie que la reponse est valide
                    while( !min.matches("\\d+") && (new BigInteger(min).compareTo(new BigInteger("0")) < 0) )
                    {
                        System.out.println("Requete invalide, veuillez reessayer");
                        min = entreeClavier.readLine();
                    }
                    System.out.println("\nVeuillez choisir la fin de l'intervalle : ");
                    max = entreeClavier.readLine();//on lit la fin de l'intervalle
                    while( !max.matches("\\d+")  && (new BigInteger(max).compareTo(new BigInteger("0")) < 0)
                            && (new BigInteger(max).compareTo(new BigInteger(min)) <= 0) )
                    {
                        System.out.println("Requete invalide, veuillez reessayer");
                        max = entreeClavier.readLine();
                    }
                    choix2 = min + " " + max;
                }
                if(choix.equals("op"))//si le client souhaite le nombre d'occurences d'une persistance
                {
                    String pers = "";
                    System.out.println("\nVeuillez choisir la valeur de la persistance : ");
                    pers = entreeClavier.readLine();//on lit la persistance
                    while( !pers.matches("\\d+") && Integer.parseInt(pers) < 0)
                    {
                        System.out.println("Requete invalide, veuillez reessayer");
                        pers = entreeClavier.readLine();
                    }
                    choix2 = choix2 + " "+ pers;
                }
                RequestServPersSpec(requete + " " + choix2);
                break;
            case("pn")://si le client souhaite la persistance d'un nombre
                System.out.println("\nVeuillez choisir le nombre");
                String nb = "";
                nb = entreeClavier.readLine();//on lit le nombre
                while( !nb.matches("\\d+")  && (new BigInteger(nb).compareTo(new BigInteger("0")) < 0))
                {
                    System.out.println("Requete invalide, veuillez reessayer");
                    nb = entreeClavier.readLine();
                }
                choix2 = nb;
                RequestServPersSpec(requete + " " + choix2);
                break;
            case("pi")://si le client souhaite la persistance sur un intervalle
                String min = "";
                String max = "";
                System.out.println("\nVeuillez choisir le debut de l'intervalle : ");
                min = entreeClavier.readLine();
                while( !min.matches("\\d+") && (new BigInteger(min).compareTo(new BigInteger("0")) < 0))
                {
                    System.out.println("Requete invalide, veuillez reessayer");
                    min = entreeClavier.readLine();
                }
                System.out.println("\nVeuillez choisir la fin de l'intervalle : ");
                max = entreeClavier.readLine();
                while( !max.matches("\\d+") && (new BigInteger(max).compareTo(new BigInteger("0")) < 0)
                        && (new BigInteger(max).compareTo(new BigInteger(min)) <= 0))
                {
                    System.out.println("Requete invalide, veuillez reessayer");
                    max = entreeClavier.readLine();
                }
                choix2 = min + " " + max;
                RequestServPersSpec(requete + " " + choix2);
                break;
            case("RETURN"):break;//si le client souhaite revenir en arriere
        }
    }

    //cette méthode permet d'afficher le menu de comparaison des persistances
    public void MenuPersComp(String requete) throws IOException, InterruptedException {
        System.out.println("\nTapez RETURN pour revenir en arriere\n");
        System.out.println("Comparaisons des persistances -- Que souhaitez-vous consulter ? : ");
        System.out.println("  - Persistance Maximale (pmax)\n  - Moyenne de la persistance (moy)\n  - Mediane de la persistance (med)\n  - Persistance d'un nombre (pn)\n  - Nombre d'occurence d'une persistance (op)");


        String choix = entreeClavier.readLine();//on lit le choix du client
        //on verifie que le choix est valide
        while( !choix.equals("RETURN") && !choix.equals("pmax") && !choix.equals("moy") && !choix.equals("med") && !choix.equals("pn") && !choix.equals("op"))
        {
            System.out.println("Requete invalide, veuillez reessayer");
            choix = entreeClavier.readLine();
        }
        requete = requete + " " + choix;

        String choix2 = "";
        switch(choix){
            case("pmax"):
            case("op"):
            case("moy"):
            case("med"):
                System.out.println("\nSouhaitez-vous le resultat sur un intervalle (int) ou l'ensemble des nombres calcules (all) ?");
                choix2 = entreeClavier.readLine();
                while( !choix2.equals("int") && !choix2.equals("all") )
                {
                    System.out.println("Requete invalide, veuillez reessayer");
                    choix2 = entreeClavier.readLine();
                }
                if(choix2.equals("int"))
                {
                    String min = "";
                    String max = "";
                    System.out.println("\nVeuillez choisir le debut de l'intervalle : ");
                    min = entreeClavier.readLine();
                    while( !min.matches("\\d+") && (new BigInteger(min).compareTo(new BigInteger("0")) < 0))
                    {
                        System.out.println("Requete invalide, veuillez reessayer");
                        min = entreeClavier.readLine();
                    }
                    System.out.println("\nVeuillez choisir la fin de l'intervalle : ");
                    max = entreeClavier.readLine();
                    while( !max.matches("\\d+") && (new BigInteger(max).compareTo(new BigInteger("0")) < 0) && (new BigInteger(max).compareTo(new BigInteger(min)) <= 0))
                    {
                        System.out.println("Requete invalide, veuillez reessayer");
                        max = entreeClavier.readLine();
                    }
                    choix2 = min + " " + max;
                }
                if(choix.equals("op"))
                {
                    String pers = "";
                    System.out.println("\nVeuillez choisir la valeur de la persistance : ");
                    pers = entreeClavier.readLine();
                    while( !pers.matches("\\d+") && Integer.parseInt(pers) < 0)
                    {
                        System.out.println("Requete invalide, veuillez reessayer");
                        pers = entreeClavier.readLine();
                    }
                    choix2 = choix2 + " "+ pers;
                }
                RequestServPersSpec(requete + " " + choix2);
                break;
            case("pn"):
                System.out.println("\nVeuillez choisir le nombre");
                String nb = "";
                nb = entreeClavier.readLine();
                while( !nb.matches("\\d+") && (new BigInteger(nb).compareTo(new BigInteger("0")) < 0))
                {
                    System.out.println("Requete invalide, veuillez reessayer");
                    nb = entreeClavier.readLine();
                }
                choix2 = nb;
                RequestServPersSpec(requete + " " + choix2);
                break;
            case("pi"):
                String min = "";
                String max = "";
                System.out.println("\nVeuillez choisir le debut de l'intervalle : ");
                min = entreeClavier.readLine();
                while( !min.matches("\\d+") && (new BigInteger(min).compareTo(new BigInteger("0")) < 0))
                {
                    System.out.println("Requete invalide, veuillez reessayer");
                    min = entreeClavier.readLine();
                }
                System.out.println("\nVeuillez choisir la fin de l'intervalle : ");
                max = entreeClavier.readLine();
                while( !max.matches("\\d+") && (new BigInteger(max).compareTo(new BigInteger("0")) < 0) && (new BigInteger(max).compareTo(new BigInteger(min)) <= 0))
                {
                    System.out.println("Requete invalide, veuillez reessayer");
                    max = entreeClavier.readLine();
                }
                choix2 = min + " " + max;
                RequestServPersSpec(requete + " " + choix2);
                break;
            case("RETURN"):break;
        }
    }


    public void StatGenServ() throws IOException, InterruptedException
    {
        RequestServPersSpec("stat");//on envoie la requete au serveur pour obtenir les statistiques
    }

    //cette méthode permer d'envoyer une requete au serveur
    public void RequestServPersSpec(String request) throws IOException, InterruptedException
    {
        System.out.println("Resultat requete : " + request+"\n");
        pw.println(request);//on envoie la requete au serveur
        String rep ="";//on lit la reponse du serveur
        while(! (rep = sisr.readLine()).equals("finreponse"))
        {
            System.out.println(rep);//on affiche la reponse du serveur
        }
        Thread.sleep(500);
    }
}