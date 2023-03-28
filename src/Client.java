import java.io.*;
import java.net.*;
public class Client {

    static boolean arreter=false;

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("10.192.34.181",9000);
        System.out.println("Vous êtes connecté au serveur");
        BufferedReader sisr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        PrintWriter sisw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);

        GererSaisieClient saisie=new GererSaisieClient(sisw);
        saisie.start();

        String str;
        while(!arreter) {
            str = sisr.readLine();
            System.out.println("Serveur=>"+str);
        }

        System.out.println("END");
        //sisw.println("END") ;
        sisr.close();
        sisw.close();
        socket.close();
    }
}

class GererSaisieClient extends Thread{
    private final BufferedReader entreeClavier;
    private final PrintWriter pw;

    public GererSaisieClient(PrintWriter pw){
        entreeClavier = new BufferedReader(new InputStreamReader(System.in));
        this.pw=pw;
    }

    public void run(){
        try{
            System.out.println("Tapez END pour quitter");
            System.out.println("Tapez persistance pour lancer le calcul de la persistance");
            while(true){
                String requette = entreeClavier.readLine();
                pw.println(requette);
            }
               /* if(str.equals("persistance"))
                    requete = "persistance ";
                    System.out.println("Additive(a) ou Multiplicative (m) ?");
                    if((str=entreeClavier.readLine()).equals("a"))
                        requete += "add ";
                    else if(str.equals("m"))
                        requete += "mul ";
                    else{
                        System.out.println("Erreur de saisie");
                        saisieOK = false;
                    }
                    System.out.println("Valeur minimale ?");
                    min = new BigInteger(entreeClavier.readLine());
                    requete += min + " ";
                    System.out.println("Valeur maximale ?");
                    max = new BigInteger(entreeClavier.readLine());
                    requete += max;
                    if(saisieOK){
                        System.out.println(requete);
                        pw.println(requete);
                        requete = "";

                    }

                    else
                        saisieOK = true;
            }
            //si on tape END
            pw.println("END");*/
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}