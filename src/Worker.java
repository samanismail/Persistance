import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.Hashtable;

public class Worker {
    static boolean arreter;
    static Hashtable<BigInteger, Integer> persistanceAdditive = new Hashtable<>();
    static Hashtable<BigInteger, Integer> persistanceMultiplicative = new Hashtable<>();
    static BufferedReader sisr;
    static PrintWriter sisw;
    static Socket socketObjets;
    static ObjectOutputStream oos;

    public static void main(String[] args) throws Exception {
        arreter=false;
        Socket socket = new Socket("10.192.34.181",8000);
        System.out.println("SOCKET = " + socket);
        Worker.sisr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Worker.sisw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
        GererSaisieWorker saisie=new GererSaisieWorker(sisw);
        saisie.start();
        String str;
        while(!arreter) {
            str = sisr.readLine();
            if(str.equals("END")) {
                arreter=true;
                sisw.println("END");
            }
            else if(str.split(" ")[0].equals("persistance")) {
                BigInteger debut = new BigInteger(str.split(" ")[1]);
                System.out.println("debut = " + debut + " fin = " + debut.add(new BigInteger("10000")));
                for (BigInteger i = debut; i.compareTo(debut.add(new BigInteger("10000"))) < 0; i = i.add(BigInteger.ONE)) {
                    Tache t = new Tache(i);
                    t.start();
                }
                envoyerPersistances(debut, debut.add(new BigInteger("10000")));
                //vider les hashmaps
                persistanceAdditive.clear();
                persistanceMultiplicative.clear();

            }
            else {
                System.out.println("Serveur=>"+str);
            }
        }
        sisr.close();
        sisw.close();
        socket.close();
        socketObjets.close();
        oos.close();
        System.exit(0);
    }

    public static void envoyerPersistances(BigInteger debut, BigInteger fin) {
        try {
            socketObjets = new Socket("10.192.34.181",10000);
            oos = new ObjectOutputStream(socketObjets.getOutputStream());
            Hachtable hm = new Hachtable(debut, fin,persistanceAdditive,persistanceMultiplicative);
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
            while(!(str = entreeClavier.readLine()).equals("END") && !Worker.arreter){
                pw.println(str);
            }
            pw.println("END");
            Worker.arreter=true;
        }catch(IOException e){e.printStackTrace();}

    }
}

class Tache extends Thread {
    private final BigInteger nombre;

    Tache(BigInteger nombre) {
        this.nombre = nombre;
    }
    public void run() {
        Worker.persistanceAdditive.put(nombre, Worker.persistanceAdditive(nombre));
        Worker.persistanceMultiplicative.put(nombre, Worker.persistanceMultiplicative(nombre));
    }

}
class Hachtable implements Serializable {
    private Hashtable<BigInteger, Integer> persistanceA;
    private Hashtable<BigInteger, Integer> persistanceM;
    private BigInteger debut;
    private BigInteger fin;

    public Hachtable(BigInteger debut,BigInteger fin,Hashtable<BigInteger, Integer> persistanceA, Hashtable<BigInteger, Integer> persistanceM) {
        this.persistanceA = persistanceA;
        this.persistanceM = persistanceM;
        this.debut = debut;
        this.fin = fin;

    }

    public Hashtable<BigInteger, Integer> getPersistanceA() {
        return persistanceA;
    }

    public Hashtable<BigInteger, Integer> getPersistanceM() {
        return persistanceM;
    }
    public BigInteger getDebut() {
        return debut;
    }
    public BigInteger getFin() {
        return fin;
    }
}


