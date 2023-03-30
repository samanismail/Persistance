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
         //création du fichier index
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
                pwWorker[i].println( getNombre() +" "+intervalle.add(getNombre()));
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
                if(sisr.ready()) {
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
                    } else if (str.split(" ").length==3 && str.split(" ")[0].equals("persistance") && str.split(" ")[1].equals("add"))// si le client demande une persistance
                    {
                        System.out.println("Calculons la persistance additive de " + str.split(" ")[2]);
                        int resultat = calculPersistanceAdditive(str.split(" ")[2]);
                        System.out.println("La persistance additive de " + str.split(" ")[2] + " est " + resultat);
                        sisw.println("persistance additive de " + str.split(" ")[2] + " " + resultat);

                    }
                    else if (str.split(" ").length==3 && str.split(" ")[0].equals("persistance") && str.split(" ")[1].equals("mul"))// si le client demande une persistance
                    {
                        int resultat = calculPersistanceMultiplicative(str.split(" ")[2]);
                        sisw.println("persistance add " + str.split(" ")[2] + " " + resultat);

                    }
                    else if(str.split(" ").length == 4 && str.split(" ")[0].equals("persistance") && str.split(" ")[1].equals("add") )
                    {
                        calculPersistanceAdditiveInterval(str.split(" ")[2],str.split(" ")[3]);
                    }
                    /*else if(str.split(" ").length == 4 && str.split(" ")[0].equals("persistance") && str.split(" ")[1].equals("mul") && str.split(" ")[2].equals("resultat"))
                    {
                        calculPersistanceMultiplicativeInterval(str.split(" ")[3],str.split(" ")[4]);
                    }*/

                    else
                    {
                        System.out.println("Client " + soc.getInetAddress() + " : " + str);
                    }
                }

            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void calculPersistanceAdditiveInterval(String d, String f) throws IOException, ClassNotFoundException {
        Hashtable<BigInteger,Integer> resultat = new Hashtable<>();
        BigInteger debut = new BigInteger(d);
        BigInteger fin = new BigInteger(f);
        BigInteger difference = fin.subtract(debut).add(BigInteger.valueOf(1));
        BigInteger i =   debut.divide(BigInteger.valueOf(10000)).multiply(BigInteger.valueOf(10000));

        //lire les nombres de debut à fin dans le dossier Additive du disque
        File repertoire = new File("Infos/index.txt");
        BufferedReader br = new BufferedReader(new FileReader(repertoire));
        String ligne;

        while(i.compareTo(fin)<0){
            ligne = br.readLine();
            if(ligne!=null ){
                {
                    // si i est dans l'intervalle
                    if(i.compareTo(fin)<0){
                        FileInputStream fileIn = new FileInputStream("Additive/"+ligne+".ser");    //ouverture du fichier
                        ObjectInputStream in = new ObjectInputStream(fileIn);
                        Hashtable<BigInteger, Integer> h = (Hashtable<BigInteger, Integer>) in.readObject();

                        //ajouter les valeurs de h dans l'intervale dans resultat
                        for (BigInteger key : h.keySet()) {
                            System.out.println("Debut : "+debut+" Fin : "+fin);
                            if(key.compareTo(debut)>=0 && key.compareTo(fin)<=0){
                                sisw.println("key : "+key+" value : "+h.get(key));
                                difference = difference.subtract(BigInteger.valueOf(1));
                            }
                            if(difference.compareTo(BigInteger.valueOf(0))==0){
                                break;
                            }
                        }


                    }
                    else {
                        break;
                    }

                }
            }
            i = i.add(BigInteger.valueOf(1));
        }





    }


    private int calculPersistanceMultiplicative(String s) {
        BigInteger nb = new BigInteger(s);
        int count = 0;

        while (nb.compareTo(BigInteger.TEN) >= 0) {
            BigInteger product = BigInteger.ONE;

            // Multiply all the digits of "nb"
            while (nb.compareTo(BigInteger.ZERO) > 0) {
                BigInteger[] divAndRem = nb.divideAndRemainder(BigInteger.TEN);
                product = product.multiply(divAndRem[1]);
                nb = divAndRem[0];
            }

            nb = product;
            count++;
        }

        return count;
    }

    private int calculPersistanceAdditive(String s) {
        BigInteger nb = new BigInteger(s);
        int count = 0;

        while (nb.compareTo(BigInteger.TEN) >= 0) {
            int sum = 0;

            // Add up all the digits of "nb"
            while (nb.compareTo(BigInteger.ZERO) > 0) {
                BigInteger[] divAndRem = nb.divideAndRemainder(BigInteger.TEN);
                sum += divAndRem[1].intValue();
                nb = divAndRem[0];
            }

            nb = BigInteger.valueOf(sum);
            count++;
        }
        return count;
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
                this.oos.flush();
                this.oos = new ObjectOutputStream(new FileOutputStream("Additive\\" + h.getDebut() + "-" + h.getFin() + ".ser"));
                this.oos.writeObject(persistanceA);
                this.oos.flush();
                augmenterMaxCalcule();
                Serveur.ecrireMaxCalculer();
                BufferedReader br = new BufferedReader(new FileReader("Infos\\maxCalcule.txt"));
                String line = br.readLine();
                System.out.println(line);
                br.close();
            }
        }catch (IOException | ClassNotFoundException e) {}
    }
    public void augmenterMaxCalcule(){
        Serveur.maxcalcule = Serveur.maxcalcule.add(Serveur.intervalle);
    }

}

