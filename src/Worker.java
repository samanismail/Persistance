import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.*;

public class Worker {
    static boolean arreter;
    static Hashtable<BigInteger, Integer> persistanceAdditive = new Hashtable<>();
    static Hashtable<BigInteger, Integer> persistanceMultiplicative = new Hashtable<>();
    static BufferedReader sisr;
    static PrintWriter sisw;
    static Socket socketObjets;
    static ObjectOutputStream oos;
    static int coeurs = Runtime.getRuntime().availableProcessors();
    static ProcCalcul thread_calc[];

    public static void main(String[] args) throws Exception {
        arreter=false;
        Socket socket = new Socket("10.192.34.181",8000);
        System.out.println("SOCKET = " + socket);
        Worker.sisr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Worker.sisw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
        GererSaisieWorker saisie=new GererSaisieWorker(sisw);
        saisie.start();
        String str;


        thread_calc = new ProcCalcul[coeurs];
        Tache t = new Tache();
        boolean proc_fini = false;
        for(int i=0; i<coeurs; i++)
        {
            thread_calc[i] = new ProcCalcul(t,i);
        }
        for(int i=0; i<coeurs; i++)
        {
            thread_calc[i].join();
        }
        for(int i=0; i<coeurs; i++)
        {
            thread_calc[i].start();
        }

        while(!arreter) {
            str = sisr.readLine();
            if(str.equals("END")) {
                arreter=true;
                sisw.println("END");
            }
            else if(str.split(" ")[0].equals("persistance")) {
                BigInteger debut = new BigInteger(str.split(" ")[1]);
                System.out.println("debut = " + debut + " fin = " + debut.add(new BigInteger("10000")));
                boolean pret = true;
                if(pret){
                    pret = false;
                    t.LancerTache(debut);
                    afficherHashmap(persistanceAdditive,debut);
                    /*if(debut.compareTo(new BigInteger("20000")) == 0)
                        Thread.sleep(100000);*/
                    envoyerPersistances(debut, debut.add(new BigInteger("10000")));
                    //vider les hashmaps
                    persistanceAdditive.clear();
                    persistanceMultiplicative.clear();
                    t.reset();
                    try{Thread.sleep(500);}catch(Exception e){}
                    pret = true;
                }
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

    public static void afficherHashmap(Hashtable<BigInteger, Integer> hm, BigInteger debut) {
        for(BigInteger key = debut; key.compareTo(debut.add(new BigInteger("10000"))) < 0; key = key.add(BigInteger.ONE)) {
            System.out.println("key = " + key + " value = " + hm.get(key));
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

    public static synchronized void ajouteAdd(BigInteger nb, int pers)
    {
        Worker.persistanceAdditive.put(nb, pers);

    }

    public static synchronized void ajouteMult(BigInteger nb, int pers)
    {
        Worker.persistanceMultiplicative.put(nb, pers); //System.out.println("Résultat écrit par Thread n°"+i);
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

class Tache{
    private BigInteger min;
    private BigInteger max;
    public boolean end_calc;

    public Tache()
    {
        this.min = new BigInteger("0");
        this.max = new BigInteger("-1");
        this.end_calc = true;
    }

    public boolean getStatut(){
        return this.end_calc;
    }

    public synchronized void LancerTache(BigInteger min)
    {
        this.min = min;
        this.max = min.add(new BigInteger("10000"));
        this.end_calc = false;
        notifyAll();
    }

    public synchronized BigInteger getNumber()
    {
        while(end_calc){
            //System.out.println("Thread à l'arrêt");
            try{this.wait();}catch(Exception e){};
        }

        return giveNumber();
    }

    public synchronized BigInteger giveNumber()
    {
        end_calc = !(this.min.compareTo(this.max) <= 0);
        BigInteger res = this.min;
        if(!end_calc){
            this.min = this.min.add(BigInteger.ONE);
            notifyAll();
        }
        //System.out.println("Nombre récupéré");
        return res;
    }

    public void reset()
    {
        this.min = new BigInteger("0");
        this.max = new BigInteger("-1");
        end_calc = true;
    }
}

class ProcCalcul extends Thread{

    private Tache t;
    private int indice;

    public ProcCalcul (Tache t, int i){
        this.t = t;
        this.indice = i;
    }

    public int getIndice()
    {
        return this.indice;
    }

    @Override
    public void run(){
        BigInteger nb;
        int res1, res2;
        BigInteger fini = new BigInteger("-1");
        while(true)
        {
            nb = t.getNumber();
            //System.out.println("Thread n° "+indice+" & chiffre = "+nb);
            res1 = Worker.persistanceMultiplicative(nb);
            res2 = Worker.persistanceAdditive(nb);
            Worker.ajouteMult(nb, res1);
            Worker.ajouteAdd(nb, res2);
        }
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

