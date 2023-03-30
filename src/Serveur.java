import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

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


    public static void main(String[] args) throws IOException, InterruptedException {
        arreter=false;
        pwClient = new PrintWriter[maxClients];
        pwWorker = new PrintWriter[maxWorkers];
        ipClient = new String[maxClients];
        ipWorker = new String[maxWorkers];
        ecouterObjets = new EcouterObjets[maxWorkers];
        WorkersDisponibles = new boolean[maxWorkers];
        nombre = new BigInteger("0");
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
        System.out.println("Max calculé : "+maxcalcule);
        BufferedWriter bw = new BufferedWriter(new FileWriter("Infos\\maxCalcule.txt"));
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

class ServerSocketHandler extends Thread{
    int port;
    private String type;
    private ServerSocket serverSocket;


    public ServerSocketHandler(int port,String type) {
        try{
            this.port = port;
            this.type = type;
            this.serverSocket = new ServerSocket(port);
        }catch(IOException e){e.printStackTrace();}
    }
    public void run(){
        try{
            while(Serveur.numClient < Serveur.maxClients && Serveur.numWorker < Serveur.maxWorkers){
                Socket soc = serverSocket.accept();
                if(this.type.equals("client")){
                    ConnexionClient cc = new ConnexionClient(soc);
                    cc.start();
                }
                if(this.type.equals("worker")){
                    ConnexionWorker cw = new ConnexionWorker(soc);
                    cw.start();
                }

            }
        }catch(IOException e){e.printStackTrace();}
    }
}
class ConnexionClient extends Thread {
    private BufferedReader sisr;
    private PrintWriter sisw;
    private Socket soc;


    public ConnexionClient(Socket s) {
        try {
            this.soc = s;
            sisr = new BufferedReader(new InputStreamReader(s.getInputStream()));
            sisw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
            //on ajoute le client à un emplacement libre dans la liste
            for (int i = 0; i < Serveur.maxClients; i++) {
                if (Serveur.pwClient[i] == null) {
                    Serveur.pwClient[i] = sisw;
                    Serveur.ipClient[i] = s.getInetAddress().toString();
                    Serveur.numClient++;
                    System.out.println("Client " + Serveur.ipClient[i] + " connecté");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            String str;
            while (Serveur.numClient < Serveur.maxClients) {
                if (sisr.ready()) {
                    str = sisr.readLine();

                    if (str.equals("END")) {

                        for (int i = 0; i < Serveur.maxClients; i++) {
                            if (Objects.equals(Serveur.ipClient[i], soc.getInetAddress().toString())) {
                                Serveur.pwClient[i] = null;
                                Serveur.ipClient[i] = null;
                                Serveur.numClient--;
                                System.out.println("Client " + soc.getInetAddress().toString() + " déconnecté");
                                System.out.println("Nombre de clients connectés :" + Serveur.numWorker);
                                break;
                            }
                        }
                    }
                    else {

                        String[] requete = str.split(" ");
                        //ajouter if pour les requettes qui dépassent la taille maxCalcule
                        if(true){
                            System.out.println("Client " + soc.getInetAddress().toString() + " : " + str);
                            switch (requete[0]) {
                                case ("mul"):
                                case ("add"):

                                    //Bien vérifier si requete[2] == all sinon c'est une demande d'intervalle, requete [2] = debut et requete[3] = fin
                                    if (Objects.equals(requete[1], "pi")) {PersistancesIntervalle(requete);
                                        break;
                                    }

                                case ("comp"):
                                    switch (requete[1]) {
                                        case ("pn"): PersistanceNb(requete[0],requete[2]);
                                            break;
                                        //Bien vérifier si requete[2] == all sinon c'est une demande d'intervalle, requete [2] = debut et requete[3] = fin
                                        case ("pmax"): /*PersistanceMax(requete)*/
                                            break;
                                        case ("op"): /*OccurencePers(requete)*/
                                            break;
                                        case ("moy"): /*MoyPers(requete)*/
                                            break;
                                        case ("med"): /*MedPers(requete)*/
                                            break;

                                    }
                            }
                        }
                    }
                }

            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    private void PersistancesIntervalle(String[] requete) throws IOException, ClassNotFoundException {

        String fichier = "";
        String type = "";
        if(requete[0].equals("add")){
            fichier = "Additive\\";
            type = "additive";
        }
        else if(requete[0].equals("mul")){
            fichier = "Multiplicative\\";
            type = "multiplicative";
        }
        BigInteger debut = new BigInteger(requete[2]);//debut de l'intervalle
        BigInteger fin = new BigInteger(requete[3]);//fin de l'intervalle
        BigInteger i = debut.divide(Serveur.intervalle).multiply(Serveur.intervalle);
        ArrayList<BigInteger> list = new ArrayList<>();
        //on ajoute tous les multiples de 100000 dans une liste inférieurs à fin
        while (i.compareTo(fin) <= 0) {
            list.add(i);
            i = i.add(Serveur.intervalle);
        }
        //on ouvre chque fichier dans la liste et on ajoute les résultats dans la table de hachage
        for (BigInteger b : list) {
            FileInputStream fis = new FileInputStream(fichier+ b +"-"+b.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            Hashtable<BigInteger, Integer> h = (Hashtable<BigInteger, Integer>) ois.readObject();
            for(BigInteger key : h.keySet())
            {
                if(key.compareTo(debut)>=0 && key.compareTo(fin)<=0)
                    sisw.println("persistance "+type+" de " + key + " " + h.get(key));
            }
            ois.close();
            fis.close();
        }
    }

    private void PersistanceNb(String type, String nombre) throws IOException, ClassNotFoundException {
        //chercher le fichier qui contient le nombre
        BigInteger nb = new BigInteger(nombre);
        BigInteger i = nb.divide(Serveur.intervalle).multiply(Serveur.intervalle);

        String fichier1 = "";
        String fichier2 = "";
        Hashtable<BigInteger, Integer> h=null;
        if(type.equals("add")){
            fichier1  ="Additive\\";
            FileInputStream fis = new FileInputStream(fichier1+i+"-"+i.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            h = (Hashtable<BigInteger, Integer>) ois.readObject();
            sisw.println("persistance additive de " + nb + " " + h.get(nb));
        }
        else if(type.equals("mul")){
            fichier1 = "Multiplicative\\";
            FileInputStream fis = new FileInputStream(fichier1+i+"-"+i.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            h = (Hashtable<BigInteger, Integer>) ois.readObject();
            sisw.println("persistance multiplicative de " + nb + " " + h.get(nb));
        }
        else if(type.equals("comp")){
            fichier1  ="Additive\\";
            fichier2 = "Multiplicative\\";
            FileInputStream fis = new FileInputStream(fichier1+i+"-"+i.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            Hashtable<BigInteger, Integer> h1 = (Hashtable<BigInteger, Integer>) ois.readObject();
            fis = new FileInputStream(fichier2+i+"-"+i.add(Serveur.intervalle).subtract(BigInteger.ONE)+".ser");
            ois = new ObjectInputStream(fis);
            Hashtable<BigInteger, Integer> h2 = (Hashtable<BigInteger, Integer>) ois.readObject();
            if(h1.get(nb) > h2.get(nb)){
                sisw.println(nombre + ": additive("+h1.get(nb)+") > multiplicative("+h2.get(nb)+")");
            }
            else if(h1.get(nb) < h2.get(nb)){

                sisw.println(nombre + ": additive("+h1.get(nb)+") < multiplicative("+h2.get(nb)+")");
            }
            else{
                sisw.println(nombre + ": additive("+h1.get(nb)+") = multiplicative("+h2.get(nb)+")");
            }
        }



    }
}
class ConnexionWorker extends Thread{
    private final BufferedReader sisr;
    private final Socket s;
    private EcouterObjets eo;

    public ConnexionWorker(Socket s) throws IOException {
        this.s=s;
        sisr = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintWriter sisw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
        //on ajoute le worker à un emplacement libre dans la liste
        for(int i=0;i<Serveur.maxWorkers;i++){
            if(Serveur.pwWorker[i]==null){
                Serveur.pwWorker[i]=sisw;
                Serveur.ipWorker[i]=s.getInetAddress().toString();
                Serveur.WorkersDisponibles[i]=true;
                System.out.println("Worker "+ Serveur.ipWorker[i] +" connecté");
                Serveur.numWorker++;

                eo=new EcouterObjets();
                eo.start();
                Serveur.ecouterObjets[i]=eo;

                break;
            }
        }
    }
    public void run() {

        try {
            while (Serveur.numWorker < Serveur.maxWorkers) {
                String str=sisr.readLine();
                if (str!= null && str.equals("END")){
                    //retrait du worker de la liste en fonction de son adresse IP
                    for(int i=0;i<Serveur.maxWorkers;i++){
                        if(Objects.equals(Serveur.ipWorker[i], s.getInetAddress().toString())){
                            Serveur.pwWorker[i]=null;
                            Serveur.ipWorker[i]=null;
                            Serveur.WorkersDisponibles[i]=false;
                            Serveur.numWorker--;
                            System.out.println("Worker "+s.getInetAddress()+" déconnecté");
                            System.out.println("Nombre de workers connectés : "+Serveur.numWorker);
                        }
                    }
                }
            }
        }catch (IOException e) {
            e.printStackTrace();}
    }
}
class GererSaisieServeur extends Thread{
    private final BufferedReader entreeClavier;
    private final PrintWriter pw;

    public GererSaisieServeur(){
        entreeClavier = new BufferedReader(new InputStreamReader(System.in));
        pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)),true);
    }

    public void run(){
        String str;
        try{
            while(!Serveur.arreter){
                str = entreeClavier.readLine();
                if(str.equals("workers")){
                    for(int i=0;i<Serveur.maxWorkers;i++){
                        System.out.println("Worker "+i+" : "+Serveur.ipWorker[i]);
                    }
                }
                else if(str.equals("clients")){
                    for(int i=0;i<Serveur.maxClients;i++){
                        System.out.println("Client "+i+" : "+Serveur.ipClient[i]);
                    }
                }
                else if(str.equals("END")){
                    Serveur.arreter=true;
                }
                else{
                    //envoi du message à tous les clients et aux workers
                    for(int i=0;i<Serveur.maxClients;i++){
                        if(Serveur.pwClient[i]!=null)
                            Serveur.pwClient[i].println(str);
                    }
                    for(int i=0;i<Serveur.maxWorkers;i++){
                        if(Serveur.pwWorker[i]!=null)
                            Serveur.pwWorker[i].println(str);
                    }
                }


            }

        }catch(IOException e){e.printStackTrace();}
    }
}
class EcouterObjets extends Thread{

    ObjectOutputStream oos;
    private Socket s;



    public EcouterObjets() throws IOException {

    }
    public void run(){
        try {
            while(true){
                Socket soc = Serveur.serverSocketEcouteur.accept();
                System.out.println("Rentre dans boucle");
                ObjectInputStream ois = new ObjectInputStream(soc.getInputStream());
                Hachtable h = (Hachtable) ois.readObject();
                for(int i=0;i<Serveur.maxWorkers;i++){
                    if(Objects.equals(Serveur.ipWorker[i], soc.getInetAddress().toString())){
                        Serveur.WorkersDisponibles[i]=true;
                    }
                }
                Hashtable<BigInteger, Integer> persistanceA = h.getPersistanceA();
                Hashtable<BigInteger, Integer> persistanceM = h.getPersistanceM();


                // Créez un objet File pour représenter le dossier
                File folder = new File("Multiplicative");

                // Vérifiez si le dossier existe déjà
                if (!folder.exists()) {
                    // Créez le dossier en appelant la méthode mkdir() de l'objet File
                    folder.mkdir();
                }
                folder = new File("Additive");
                if (!folder.exists()) {
                    folder.mkdir();
                }
                folder = new File("Infos");
                if (!folder.exists()) {
                    folder.mkdir();
                }
                folder = new File("Infos\\maxCalcule.txt");
                if (!folder.exists()) {
                    folder.createNewFile();
                }
                this.oos = new ObjectOutputStream(new FileOutputStream("Multiplicative\\" + h.getDebut() + "-" + h.getFin() + ".ser"));
                this.oos.writeObject(persistanceM);
                this.oos.flush();
                this.oos = new ObjectOutputStream(new FileOutputStream("Additive\\" + h.getDebut() + "-" + h.getFin() + ".ser"));
                this.oos.writeObject(persistanceA);
                this.oos.flush();
                augmenterMaxCalcule();
                Serveur.ecrireMaxCalculer();
            }
        }catch (IOException | ClassNotFoundException e) {}
    }
    public void augmenterMaxCalcule(){
        Serveur.maxcalcule = Serveur.maxcalcule.add(Serveur.intervalle);
    }

}

