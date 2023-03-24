import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.Hashtable;

public class Worker {
    static int port = 8000;
    static InetAddress ip ;
    static String pseudo;
    static boolean arreter=false;
    static Hashtable<BigInteger, Integer> persistanceAdditive = new Hashtable<>();
    static Hashtable<BigInteger, Integer> persistanceMultiplicative = new Hashtable<>();
    static BufferedReader sisr;
    static PrintWriter sisw;

    public static void main(String[] args) throws Exception {
        ip= InetAddress.getLocalHost();
        pseudo="Worker:"+InetAddress.getLocalHost().getHostName();
        Socket socket = new Socket("10.192.34.181",port);
        System.out.println("SOCKET = " + socket);
        Worker.sisr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Worker.sisw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);

        GererSaisieWorker saisie=new GererSaisieWorker(sisw);
        saisie.start();

        String str;
        while(!arreter) {
            str = sisr.readLine();

            if(str.split(" ")[0].equals("persistance")){
                BigInteger debut = new BigInteger(str.split(" ")[1]);
               Tache t = new Tache(debut,debut.add(new BigInteger("1000")));
               t.start();
            }
            else
                System.out.println("Serveur=>"+str);
        }
        System.out.println("END");
        sisr.close();
        sisw.close();
        socket.close();
    }

    public static void envoyerPersistances(BigInteger debut, BigInteger fin,Socket socket){
        Hachtable hm = new Hachtable(debut, fin,persistanceAdditive,persistanceMultiplicative);
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(hm);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static int persistanceMultiplicative(BigInteger nb) {
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
    public static int persistanceAdditive(BigInteger nb) {
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

class GererSaisieWorker extends Thread{
    private final BufferedReader entreeClavier;
    private final PrintWriter pw;

    public GererSaisieWorker(PrintWriter pw){
        entreeClavier = new BufferedReader(new InputStreamReader(System.in));
        this.pw=pw;
    }

    public void run(){
        String str;
        try{
            while(!(str=entreeClavier.readLine()).equals("END")){
                pw.println(str);
            }
            //si on tape END
            pw.println("END");
        }catch(IOException e){e.printStackTrace();}
        Worker.arreter=true;
        System.exit(0);
    }
}

class Tache extends Thread {
    private final BigInteger debut;
    private final BigInteger fin;

    Tache(BigInteger debut, BigInteger fin) {
        this.debut = debut;
        this.fin = fin;
    }
    public void run() {
        try {
            Socket socket = new Socket("10.192.34.181", 10000);
            for (BigInteger i = debut; i.compareTo(fin) <= 0; i = i.add(BigInteger.ONE)) {
                if (!Worker.persistanceAdditive.containsKey(i)) {
                    Worker.persistanceAdditive.put(i, Worker.persistanceAdditive(i));
                }
                if (!Worker.persistanceMultiplicative.containsKey(i)) {
                    Worker.persistanceMultiplicative.put(i, Worker.persistanceMultiplicative(i));
                }
            }
            Worker.envoyerPersistances(debut,fin,socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

