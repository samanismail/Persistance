import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.Hashtable;
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
    static boolean[] WorkersDisponibles;
    static boolean arreter;


    public static void main(String[] args) throws IOException, InterruptedException {
        arreter=false;
        pwClient = new PrintWriter[maxClients];
        pwWorker = new PrintWriter[maxWorkers];
        ipClient = new String[maxClients];
        ipWorker = new String[maxWorkers];
        WorkersDisponibles = new boolean[maxWorkers];
        nombre = new BigInteger("0");
        EcouterObjets newEcouterObjets = new EcouterObjets();
        newEcouterObjets.start();
        ServerSocketHandler s1 = new ServerSocketHandler(8000, "worker");
        ServerSocketHandler s2 = new ServerSocketHandler(9000, "client");
        s1.start();
        s2.start();
        GererSaisieServeur saisie = new GererSaisieServeur();
        saisie.start();
        System.out.println("Serveur en ligne");
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

 
    public static BigInteger getNombre(){
        return nombre;
    }
    public static void MAJNombre(){
        nombre=nombre.add(new BigInteger("10000"));
    }
    public static synchronized void LancerWorkerCalculPersistance() throws InterruptedException {
        for(int i=0;i<maxWorkers;i++){
            if(WorkersDisponibles[i]){
                pwWorker[i].println("persistance "+getNombre());
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
                str = sisr.readLine();
                if (str.equals("END")) {
                    System.out.println("Client " + soc.getInetAddress() + " déconnecté");
                    for (int i = 0; i < Serveur.maxClients; i++) {
                        if (Objects.equals(Serveur.ipClient[i], soc.getInetAddress().toString())) {
                            Serveur.pwClient[i] = null;
                            Serveur.ipClient[i] = null;
                            Serveur.numClient--;
                            break;
                        }
                    }
                } else {
                    System.out.println("Client " + soc.getInetAddress() + " : " + str);
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
class ConnexionWorker extends Thread{
    private final BufferedReader sisr;
    private final Socket s;

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
    private final ServerSocket s;


    public EcouterObjets() throws IOException {
        s = new ServerSocket(10000);
    }
    public void run(){
        try {

            while(true){
                Socket soc = s.accept();
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
                this.oos = new ObjectOutputStream(new FileOutputStream("Multiplicative\\" + h.getDebut() + "-" + h.getFin() + ".ser"));
                this.oos.writeObject(persistanceM);
                this.oos = new ObjectOutputStream(new FileOutputStream("Additive\\" + h.getDebut() + "-" + h.getFin() + ".ser"));
                this.oos.writeObject(persistanceA);
                this.oos.flush();
                this.oos.close();
            }
        }catch (IOException | ClassNotFoundException e) {throw new RuntimeException(e);}
    }
}

//on crée une classe pour répondre aux requêtes des clients
class Reponse implements Serializable{
    Hashtable<BigInteger, Integer> reponse;

    public Reponse(Hashtable<BigInteger, Integer> reponse) {
        this.reponse = reponse;
    }
    public Hashtable<BigInteger, Integer> getReponse() {
        return reponse;
    }
}
