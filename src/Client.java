import java.io.*;
import java.math.BigInteger;
import java.net.*;
public class Client {

    static boolean arreter=false;

    public static void main(String[] args) throws Exception {
        String adresse = "0.0.0.0";
        if(args.length > 0){
            adresse = args[0];
        }
        Socket socket = new Socket(adresse,9000);
        System.out.println("Vous etes connecte au serveur");
        BufferedReader sisr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        PrintWriter sisw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);

        GererSaisieClient saisie=new GererSaisieClient(sisw);
        saisie.start();

        String str;
        while(!arreter) {
            if(sisr.ready()){
                str = sisr.readLine();
                System.out.println("Serveur=>"+str);
                if(str.equals("END")) arreter = true; System.out.println("END");
            }
            
        }
        sisr.close();
        sisw.close();
        socket.close();
    }
}

class GererSaisieClient extends Thread{
    private BufferedReader entreeClavier;
    private PrintWriter pw;

    public GererSaisieClient(PrintWriter pw){
        entreeClavier = new BufferedReader(new InputStreamReader(System.in));
        this.pw=pw;
    }

    public void run(){
        String requete = "";
        try{
            while(!Client.arreter){
                requete = MenuPrincipal();
                switch(requete){
                    case("END") : System.out.println("Fin du programme, deconnexion du client"); pw.println("END") ; Client.arreter = true; break;
                    case("mul"):
                    case("add"):
                        MenuPers(requete);break;
                    case("comp"):
                        MenuPersComp(requete);
                    //case("s"){StatGenServ();}
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String MenuPrincipal() throws IOException{
        System.out.println("\nTapez END pour quitter\n");
        System.out.println("Pour choisir une option dans un menu, veuillez taper la touche correspondante indiquee entre les parenthsese.");
        System.out.println("Veuillez choisir les donnees que vous souhaitez consulter : ");
        System.out.println("  - Persistance Multiplicative (mul)\n  - Persistance Additive (add)\n  - Comparaison des deux persistances (comp)\n  - Statistiques serveur (stat)");
        
        
        String requete = entreeClavier.readLine();
        while( !requete.equals("mul") && !requete.equals("add") && !requete.equals("comp") && !requete.equals("stat") && !requete.equals("END")){
            System.out.println("Requete invalide, veuillez reessayer");
            requete = entreeClavier.readLine();
        }
        return requete;
    }

    public void MenuPers(String requete)throws IOException{
        String type="";
        if(requete.equals("mul")) type = "multiplicative";
        if(requete.equals("add")) type = "additive";
        System.out.println("\nTapez RETURN pour revenir en arriere\n");
        System.out.println("Persistance "+type+" -- Que souhaitez-vous consulter ? : ");
        System.out.println("  - Persistance Maximale (pmax)\n  - Moyenne de la persistance (moy)\n  - Mediane de la persistance (med)\n  - Valeur de la persistance d'un nombre (pn)\n  - Valeur des persistances sur un intervalle (pi)\n  - Nombre d'occurences d'une persistance (op)\n");
        

        String choix = entreeClavier.readLine();
        while( !choix.equals("RETURN") && !choix.equals("pmax") && !choix.equals("moy") && !choix.equals("med") && !choix.equals("pn") && !choix.equals("pi") && !choix.equals("op")){
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
                while( !choix2.equals("int") && !choix2.equals("all") ){
                    System.out.println("Requete invalide, veuillez reessayer");
                    choix2 = entreeClavier.readLine();
                }
                if(choix2.equals("int")){
                    String min = ""; 
                    String max = "";
                    System.out.println("\nVeuillez choisir le debut de l'intervalle : ");
                    min = entreeClavier.readLine();
                    while( !min.matches("\\d+") ){
                        System.out.println("Requete invalide, veuillez reessayer");
                        min = entreeClavier.readLine();
                    }
                    System.out.println("\nVeuillez choisir la fin de l'intervalle : ");
                    max = entreeClavier.readLine();
                    while( !max.matches("\\d+") ){
                        System.out.println("Requete invalide, veuillez reessayer");
                        max = entreeClavier.readLine();
                    }
                    choix2 = min + " " + max;
                }
                if(choix.equals("op")){
                    String pers = "";
                    System.out.println("\nVeuillez choisir la valeur de la persistance : ");
                    pers = entreeClavier.readLine();
                    while( !pers.matches("\\d+") ){
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
                while( !nb.matches("\\d+") ){
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
                while( !min.matches("\\d+") ){
                    System.out.println("Requete invalide, veuillez reessayer");
                    min = entreeClavier.readLine();
                }
                System.out.println("\nVeuillez choisir la fin de l'intervalle : ");
                max = entreeClavier.readLine();
                while( !max.matches("\\d+") ){
                    System.out.println("Requete invalide, veuillez reessayer");
                    max = entreeClavier.readLine();
                }
                choix2 = min + " " + max;
                RequestServPersSpec(requete + " " + choix2);
                break;
            case("RETURN"):break;
        }   
    }

    public void MenuPersComp(String requete) throws IOException{
        System.out.println("\nTapez RETURN pour revenir en arriere\n");
        System.out.println("Comparaisons des persistances -- Que souhaitez-vous consulter ? : ");
        System.out.println("  - Persistance Maximale (pmax)\n  - Moyenne de la persistance (moy)\n  - Mediane de la persistance (med)\n  - Persistance d'un nombre (pn)\n  - Nombre d'occurence d'une persistance (op)");
        

        String choix = entreeClavier.readLine();
        while( !choix.equals("RETURN") && !choix.equals("pmax") && !choix.equals("moy") && !choix.equals("med") && !choix.equals("pn") && !choix.equals("op")){
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
                while( !choix2.equals("int") && !choix2.equals("all") ){
                    System.out.println("Requete invalide, veuillez reessayer");
                    choix2 = entreeClavier.readLine();
                }
                if(choix2.equals("int")){
                    String min = ""; 
                    String max = "";
                    System.out.println("\nVeuillez choisir le debut de l'intervalle : ");
                    min = entreeClavier.readLine();
                    while( !min.matches("\\d+") ){
                        System.out.println("Requete invalide, veuillez reessayer");
                        min = entreeClavier.readLine();
                    }
                    System.out.println("\nVeuillez choisir la fin de l'intervalle : ");
                    max = entreeClavier.readLine();
                    while( !max.matches("\\d+") ){
                        System.out.println("Requete invalide, veuillez reessayer");
                        max = entreeClavier.readLine();
                    }
                    choix2 = min + " " + max;
                }
                if(choix.equals("op")){
                    String pers = "";
                    System.out.println("\nVeuillez choisir la valeur de la persistance : ");
                    pers = entreeClavier.readLine();
                    while( !pers.matches("\\d+") ){
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
                while( !nb.matches("\\d+") ){
                    System.out.println("Requete invalide, veuillez reessayer");
                    nb = entreeClavier.readLine();
                }
                choix2 = nb;
                break;
            case("pi"):
                String min = ""; 
                String max = "";
                System.out.println("\nVeuillez choisir le debut de l'intervalle : ");
                min = entreeClavier.readLine();
                while( !min.matches("\\d+") ){
                    System.out.println("Requete invalide, veuillez reessayer");
                    min = entreeClavier.readLine();
                }
                System.out.println("\nVeuillez choisir la fin de l'intervalle : ");
                max = entreeClavier.readLine();
                while( !max.matches("\\d+") ){
                    System.out.println("Requete invalide, veuillez reessayer");
                    max = entreeClavier.readLine();
                }
                choix2 = min + " " + max;
                RequestServPersSpec(requete + " " + choix2);
                break;
            case("RETURN"):break;
        }   
    }

    public void RequestServPersSpec(String request){
        System.out.println("Resultat requete : " + request);
        pw.println(request);
    }
}