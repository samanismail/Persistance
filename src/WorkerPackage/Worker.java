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

public class Worker{
    static ObjectOutputStream oos;
    static Socket socketObjets;
    static int coeurs = Runtime.getRuntime().availableProcessors(); 
    static String adresse;
    static boolean arreter;
    public static void main(String[] args) throws Exception {

        adresse = "10.192.34.181";//"10.192.34.181"
        if(args.length > 0){
            adresse = args[0];
        }
        Socket socket = new Socket(adresse,8000);
        System.out.println("SOCKET = " + socket);
        BufferedReader sisr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter sisw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
        GererSaisieWorker saisie=new GererSaisieWorker(sisw);
        saisie.start();

        Tache t = new Tache(); /*Permet de gérer l'intervalle entre les différents threads du Worker*/
        
        
        ProcCalcul[] thread_calc;
        thread_calc = new ProcCalcul[coeurs];
        for(int i=0; i<coeurs; i++)
        {
            thread_calc[i] = new ProcCalcul(t,i);
        }
        for(int i=0; i<coeurs; i++)
        {
            thread_calc[i].start();
        }
        EnvoyerRes sendfile = new EnvoyerRes(t);
        sendfile.start();

        String str;
        boolean arreter=false;
        while(!arreter) {
            if(sisr.ready())
            {
                str = sisr.readLine();
                //On vérifie si le serveur ne s'est pas déconnecté
                if(str.equals("END")) {
                    arreter=true;
                    sisw.println("END");
                    try{Thread.sleep(500);}catch(Exception e){}
                    System.out.println("Le serveur a demandé la fin du programme.");
                    System.exit(0);
                }
                //Réception et calcul de l'intervalle puis envoi
                else if(str.split(" ").length == 2) {
                    BigInteger debut = new BigInteger(str.split(" ")[0]);
                    BigInteger fin = new BigInteger(str.split(" ")[1]);
                    t.LancerTache(debut, fin);
                    try{Thread.sleep(500);}catch(Exception e){}
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
