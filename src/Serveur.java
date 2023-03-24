import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.Hashtable;

public class Serveur {
    static final int maxClients=10;
    static int numClient=0;
    static PrintWriter[] pwClient;
    static String[] pseudoClient;
    static String[] ipClient;

    static int maxWorkers=10;
    static int numWorker=0;
    static PrintWriter[] pwWorker;
    static String[] pseudoWorker;
    static String[] ipWorker;
    static BigInteger nombre;


    public static void main(String[] args) {
        pwClient=new PrintWriter[maxClients];
        pwWorker=new PrintWriter[maxWorkers];
        pseudoClient=new String[maxClients];
        pseudoWorker=new String[maxWorkers];
        ipClient=new String[maxClients];
        ipWorker=new String[maxWorkers];
        nombre=new BigInteger("1");

        ConnexionClient cc=new ConnexionClient();
        ConnexionWorker cw=new ConnexionWorker();
        cw.start();
        cc.start();


    }
    public static void afficherClients(){
        for(int i=0;i<numClient;i++){
            System.out.println("Client "+i+" : "+Serveur.pseudoClient[i]+" "+Serveur.ipClient[i]);
        }
    }
    public static void afficherWorkers(){
        for(int i=0;i<numClient;i++){
            System.out.println("Worker "+i+" : "+Serveur.pseudoWorker[i]+" "+Serveur.ipWorker[i]);
        }
    }
    public static void LancerWorkerCalculPersistance(){
        for(int i=0;i<=numWorker;i++){
            if(pwWorker[i]!=null){
                pwWorker[i].println("persistance 777");
                nombre=nombre.add(BigInteger.ONE);
                break;
            }
        }
    }
}

class ConnexionClient extends Thread{
    private BufferedReader sisr;
    private PrintWriter sisw;
    static int port = 9000;

    public ConnexionClient(){
    }
    public void run(){
        try{
            ServerSocket s = new ServerSocket(port);
            while (Serveur.numClient<Serveur.maxClients){
                if(Serveur.numClient==0){
                    System.out.println("En attente de connexions Client...");
                }
                Socket soc = s.accept();
                sisr = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                sisw = new PrintWriter( new BufferedWriter(new OutputStreamWriter(soc.getOutputStream())),true);
                Serveur.pwClient[Serveur.numClient]=sisw;
                Serveur.pseudoClient[Serveur.numClient]=soc.getInetAddress().getHostName();
                Serveur.ipClient[Serveur.numClient]=soc.getInetAddress().getHostAddress();
                GererSaisieServeur saisie=new GererSaisieServeur(sisw);
                saisie.start();
                String pseudo = soc.getInetAddress().getHostName();
                System.out.println("Nouveau client => Pseudo: "+soc.getInetAddress().getHostName()+" IP :"+soc.getInetAddress().getHostAddress());
                Serveur.numClient++;
                String str = sisr.readLine();          		// lecture du message
                if (str.equals("END")){
                    Serveur.numClient--;
                    Serveur.pwClient[Serveur.numClient]=null;
                    Serveur.pseudoClient[Serveur.numClient]=null;
                    Serveur.ipClient[Serveur.numClient]=null;
                    System.out.println("Client "+ pseudo +" déconnecté");
                }
                else{
                    System.out.println(pseudo +"=>"+str);
                }

            }
            sisr.close();
            sisw.close();
            s.close();
        }catch(IOException e){e.printStackTrace();}
    }
}
class ConnexionWorker extends Thread{
    private BufferedReader sisr;
    private PrintWriter sisw;
    static int port = 8000;

    public ConnexionWorker(){
    }
    public void run(){
        try{
            ServerSocket s = new ServerSocket(port);
            while (Serveur.numWorker<Serveur.maxWorkers){
                if(Serveur.numWorker==0){
                    System.out.println("En attente de connexions Worker...");
                }
                Socket soc = s.accept();
                sisr = new BufferedReader(new InputStreamReader(soc.getInputStream()));
                sisw = new PrintWriter( new BufferedWriter(new OutputStreamWriter(soc.getOutputStream())),true);

                Serveur.pwWorker[Serveur.numWorker]=sisw;
                Serveur.pseudoWorker[Serveur.numWorker]=soc.getInetAddress().getHostName();
                Serveur.ipWorker[Serveur.numWorker]=soc.getInetAddress().getHostAddress();
                GererSaisieServeur saisie=new GererSaisieServeur(sisw);
                saisie.start();

                String pseudo = soc.getInetAddress().getHostName();
                System.out.println("Nouveau Worker => Pseudo: "+soc.getInetAddress().getHostName()+" IP :"+soc.getInetAddress().getHostAddress());
                EcouterObjets ecouterObjets=new EcouterObjets(soc);
                ecouterObjets.start();
                Serveur.numWorker++;
                String str = sisr.readLine();
                if (str.equals("END")){
                    Serveur.numWorker--;
                    Serveur.pwWorker[Serveur.numWorker]=null;
                    Serveur.pseudoWorker[Serveur.numWorker]=null;
                    Serveur.ipWorker[Serveur.numWorker]=null;
                    System.out.println("Worker "+ pseudo +" déconnecté");
                }
                else{
                    System.out.println(pseudo +"=>"+str);
                }

            }
            sisr.close();
            sisw.close();
            s.close();
        }catch(IOException e){e.printStackTrace();}
    }
}
class GererSaisieServeur extends Thread{
    private final BufferedReader entreeClavier;
    private final PrintWriter pw;

    public GererSaisieServeur(PrintWriter pw){
        entreeClavier = new BufferedReader(new InputStreamReader(System.in));
        this.pw=pw;
    }

    public void run(){
        String str;
        try{
            while(!(str=entreeClavier.readLine()).equals("END")){
                if(str.equals("clients")){
                    Serveur.afficherClients();
                }
                if(str.equals("workers")){
                    Serveur.afficherWorkers();
                }
                else{
                    pw.println(str);
                }
            }
            //si on tape END
            pw.println("END");
        }catch(IOException e){e.printStackTrace();}
        Client.arreter=true;
    }
}
//ecouter objets avec ObjectOutputStream
class EcouterObjets extends Thread{
    private final Socket soc;
    private final String pseudo;
    static int port = 10000;

    public EcouterObjets(Socket soc){
        this.soc=soc;
        this.pseudo=soc.getInetAddress().getHostName();
    }
    public void run(){
        try{
            ServerSocket s = new ServerSocket(port);
            while (true) {
                System.out.println("En attente d'objets Worker...");
                Socket soc = s.accept();
                //créer un fichier sur le Desktop et mettre la Hachtable rçue dedans
                ObjectInputStream ois = new ObjectInputStream(soc.getInputStream());
                Hachtable h = (Hachtable) ois.readObject();
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("C:\\Users\\mymar\\Desktop\\"+h.getDebut()+"-"+h.getFin()+".txt"));
                File f = new File("C:\\Users\\mymar\\Desktop\\test.txt");
                oos.writeObject(h);
                oos.flush();
                oos.close();
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
