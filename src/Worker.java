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
            if(str.split(" ")[0].equals("persistance")) {
                BigInteger debut = new BigInteger(str.split(" ")[1]);
                System.out.println("Début : " + debut);
                for (BigInteger i = debut; i.compareTo(debut.add(new BigInteger("10000"))) < 0; i = i.add(BigInteger.ONE)) {
                    Tache t = new Tache(i);
                    t.start();
                }
                envoyerPersistances(debut, debut.add(new BigInteger("10000")));

                System.gc();
            }


        }
        sisr.close();
        sisw.close();
        socket.close();
    }

    public static void envoyerPersistances(BigInteger debut, BigInteger fin) {
        try {
            Socket socketObjets= new Socket("10.192.34.181", 10000);
            Hachtable hm = new Hachtable(debut, fin,persistanceAdditive,persistanceMultiplicative);
            ObjectOutputStream oos = new ObjectOutputStream(socketObjets.getOutputStream());
            oos.writeObject(hm);
            oos.flush();
            oos.close();
            socketObjets.close();
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
            Worker.arreter=true;
            pw.println("END");
            System.exit(0);
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

