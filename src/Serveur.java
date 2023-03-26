import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.Hashtable;

public class Serveur {
    static final int maxClients=10;
    static int numClient=0;
    static PrintWriter[] pwClient;
    static String[] ipClient;

    static int maxWorkers=10;
    static int numWorker=0;
    static PrintWriter[] pwWorker;
    static String[] ipWorker;
    static BigInteger nombre;
    static boolean[] WorkersDisponibles;


    public static void main(String[] args) throws IOException, InterruptedException {
        pwClient = new PrintWriter[maxClients];
        pwWorker = new PrintWriter[maxWorkers];
        ipClient = new String[maxClients];
        ipWorker = new String[maxWorkers];
        WorkersDisponibles = new boolean[maxWorkers];
        for(int i=0;i<maxWorkers;i++){
            WorkersDisponibles[i]=false;
        }
        nombre = new BigInteger("0");
        EcouterObjets newEcouterObjets = new EcouterObjets();
        newEcouterObjets.start();
        ServerSocketHandler s1 = new ServerSocketHandler(8000, "worker");
        ServerSocketHandler s2 = new ServerSocketHandler(9000, "client");
        s1.start();
        s2.start();
        GererSaisieServeur saisie = new GererSaisieServeur();
        saisie.start();
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
                MAJNombre();
                WorkersDisponibles[i]=false;
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
            System.out.println("Serveur en ligne, socket d'ecoute cree => "+serverSocket);
        }catch(IOException e){e.printStackTrace();}
    }
    public void run(){
        try{
            while(Serveur.numClient < Serveur.maxClients && Serveur.numWorker < Serveur.maxWorkers){

                Socket soc = serverSocket.accept();
                if(this.type.equals("client")){
                    ConnexionClient cc = new ConnexionClient(soc);
                    System.out.println("Nouvelle connexion client - SOCKET => "+soc);
                    cc.start();
                }
                if(this.type.equals("worker")){
                    ConnexionWorker cw = new ConnexionWorker(soc);
                    System.out.println("Nouvelle connexion worker - SOCKET => "+soc);
                    cw.start();
                }

            }
        }catch(IOException e){e.printStackTrace();}
    }
}
class ConnexionClient extends Thread{
    private BufferedReader sisr;
    private PrintWriter sisw;
    private  Socket soc;

    public ConnexionClient(Socket s){
        try{
            this.soc = s;
            sisr = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            sisw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(soc.getOutputStream())),true);
            Serveur.pwClient[Serveur.numClient]=sisw;
            Serveur.ipClient[Serveur.numClient]=soc.getInetAddress().toString();
            System.out.println("Client "+ Serveur.ipClient[Serveur.numClient] +" connecté");
            Serveur.numClient++;
        }catch(IOException e){e.printStackTrace();}
    }
    public void run(){
        try{
            while(Serveur.numClient < Serveur.maxClients){
                String str;
                if((str=sisr.readLine()).equals("END")){
                    //retrait du client de la liste en fonction de son adresse IP
                    for(int i=0;i<Serveur.numClient;i++){
                        if(Serveur.ipClient[i].equals(soc.getInetAddress().getHostAddress())){
                            Serveur.pwClient[i]=null;
                            Serveur.ipClient[i]=null;
                            Serveur.numClient--;
                            System.out.println("Client "+soc.getInetAddress()+" déconnecté");
                        }
                    }
                }
                else if(str.equals("salut")){

                    FileInputStream fileIn = new FileInputStream("D:\\L2_S4\\Info4B\\Persistance\\Persistance\\Additive\\20000-30000.ser");
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    Hashtable<BigInteger, Integer> h = (Hashtable<BigInteger, Integer>) in.readObject();
                    in.close();
                    fileIn.close();
                    System.out.println("Deserialized Hashtable.");
                    //afficher toutes les valeurs de la hachtable
                    for (BigInteger key : h.keySet()) {
                        System.out.println("key: " + key + " value: " + h.get(key));
                    }

                }
                else{
                    System.out.println("Client "+soc.getInetAddress()+" : "+str);
                }
            }
        }catch(IOException e){e.printStackTrace();} catch (ClassNotFoundException e) {
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
        Serveur.pwWorker[Serveur.numWorker]= sisw;
        Serveur.ipWorker[Serveur.numWorker]=s.getInetAddress().getHostAddress();
        Serveur.WorkersDisponibles[Serveur.numWorker]=true;
        Serveur.numWorker++;
    }
    public void run() {

        try {
            while (Serveur.numWorker < Serveur.maxWorkers) {
                Serveur.LancerWorkerCalculPersistance();
                String str;
                if ((str = sisr.readLine()).equals("END")) {
                    //retrait du worker de la liste en fonction de son adresse IP
                    for (int i = 0; i < Serveur.numWorker; i++) {
                        if (Serveur.ipWorker[i].equals(s.getInetAddress().getHostAddress())) {
                            Serveur.pwWorker[i] = null;
                            Serveur.ipWorker[i] = null;
                            Serveur.WorkersDisponibles[i] = false;
                            Serveur.numWorker--;
                            System.out.println("Worker " + s.getInetAddress() + " déconnecté");
                        }
                    }
                }
            }
        }catch (IOException | InterruptedException e) {
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
            while(!(str=entreeClavier.readLine()).equals("END")){
               if(str.equals("workers")){
                   for(int i=0;i<Serveur.numWorker;i++){
                       System.out.println("Worker "+i+" : "+Serveur.ipWorker[i]);
                   }
               }
               else if(str.equals("clients")){
                   for(int i=0;i<Serveur.numClient;i++){
                       System.out.println("Client "+i+" : "+Serveur.ipClient[i]);
                   }
               }
               else{
                     System.out.println(str);
               }


            }
            //si on tape END
            pw.println("END");
        }catch(IOException e){e.printStackTrace();}
        Client.arreter=true;
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
                for(int i=0;i<Serveur.numWorker;i++){
                    if(Serveur.ipWorker[i].equals(soc.getInetAddress().getHostAddress())){
                        Serveur.WorkersDisponibles[i]=true;
                    }
                }
                Serveur.LancerWorkerCalculPersistance();
                Hashtable<BigInteger, Integer> persistanceA = h.getPersistanceA();
                Hashtable<BigInteger, Integer> persistanceM = h.getPersistanceM();

                this.oos = new ObjectOutputStream(new FileOutputStream("D:\\L2_S4\\Info4B\\Persistance\\Persistance\\Multiplicative\\" + h.getDebut() + "-" + h.getFin() + ".ser"));
                this.oos = new ObjectOutputStream(new FileOutputStream("D:\\L2_S4\\Info4B\\Persistance\\Persistance\\Additive\\" + h.getDebut() + "-" + h.getFin() + ".ser"));
                this.oos.writeObject(persistanceM);
                this.oos.writeObject(persistanceA);
                this.oos.flush();
                this.oos.close();
                System.gc();


            }
        }catch (IOException | ClassNotFoundException | InterruptedException e) {throw new RuntimeException(e);}
    }
}
