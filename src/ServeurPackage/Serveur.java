package ServeurPackage;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.time.LocalDateTime;

public class Serveur {
    static final int maxClients=5;
    static int numClient=0;
    static PrintWriter[] pwClient;
    static String[] ipClient;

    static int maxWorkers=5;
    static int numWorker=0;
    static PrintWriter[] pwWorker;
    static String[] ipWorker;

    static BigInteger nombre;
    static BigInteger maxcalcule = new BigInteger("0");
    static boolean[] WorkersDisponibles;
    static boolean arreter;
    static EcouterObjets[] ecouterObjets;
    static ServerSocket serverSocketEcouteur;
    static BigInteger intervalle = new BigInteger("100000");
    static LocalDateTime tps;

    public static void main(String[] args) throws IOException, InterruptedException {
        //vérifier si le fichier maxCalcule.txt existe
        File f = new File("Infos/maxCalcule.txt");
        if(f.exists()){
            //lire le fichier maxCalcule.txt
            BufferedReader br = new BufferedReader(new FileReader("Infos/maxCalcule.txt"));
            maxcalcule = new BigInteger(br.readLine()).add(intervalle);
            nombre = maxcalcule;
        }
        else{
            maxcalcule = new BigInteger("0");
            nombre = new BigInteger("0");
        }
        arreter=false;
        pwClient = new PrintWriter[maxClients];
        pwWorker = new PrintWriter[maxWorkers];
        ipClient = new String[maxClients];
        ipWorker = new String[maxWorkers];
        ecouterObjets = new EcouterObjets[maxWorkers];
        WorkersDisponibles = new boolean[maxWorkers];
        tps = LocalDateTime.now();
        serverSocketEcouteur = new ServerSocket(10000);
        ServerSocketHandler s1 = new ServerSocketHandler(8000, "worker");
        ServerSocketHandler s2 = new ServerSocketHandler(9000, "client");
        s1.start();
        s2.start();
        GererSaisieServeur saisie = new GererSaisieServeur();
        saisie.start();
        System.out.println("Serveur en ligne. Adresse IP : "+InetAddress.getLocalHost());
        while(!arreter){
            LancerWorkerCalculPersistance();
        }
        for(int i=0;i<Serveur.maxWorkers;i++){
            if(Serveur.pwWorker[i]!=null)
                Serveur.pwWorker[i].println("END");
        }
        for(int i=0;i<Serveur.maxClients;i++){
            if(Serveur.pwClient[i]!=null)
                Serveur.pwClient[i].println("END");
        }
        Thread.sleep(1000);
        System.exit(0);

    }
    static synchronized void ecrireMaxCalculer() throws IOException {
        System.out.println("Max calcule : "+maxcalcule);
        BufferedWriter bw = new BufferedWriter(new FileWriter("Infos/maxCalcule.txt"));
        bw.write(maxcalcule.toString());
        bw.close();
    }


    public static BigInteger getNombre(){
        return nombre;
    }
    public static void MAJNombre(){
        nombre=nombre.add(intervalle);
    }
    public static void LancerWorkerCalculPersistance() throws InterruptedException {
        for(int i=0;i<maxWorkers;i++){
            if(WorkersDisponibles[i]){
                pwWorker[i].println( getNombre() +" "+intervalle.add(getNombre()).subtract(new BigInteger("1")));
                WorkersDisponibles[i]=false;
                MAJNombre();
            }
        }
    }
}

