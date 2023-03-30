import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.util.*;

public class Worker{
    static boolean arreter;
    static BufferedReader sisr;
    static PrintWriter sisw;
    static Socket socketObjets;
    static ObjectOutputStream oos;

    static int coeurs = Runtime.getRuntime().availableProcessors()-2;
    static ProcCalcul[] thread_calc;
    static String adresse;

    public static void main(String[] args) throws Exception {

        arreter=false;
        adresse = "10.192.34.181";//"10.192.34.181"
        if(args.length > 0){
            adresse = args[0];
        }
        Socket socket = new Socket(adresse,8000);
        System.out.println("SOCKET = " + socket);
        Worker.sisr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Worker.sisw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
        GererSaisieWorker saisie=new GererSaisieWorker(sisw);
        saisie.start();
        String str;

        thread_calc = new ProcCalcul[coeurs];
        Tache t = new Tache();
        EnvoyerRes sendfile = new EnvoyerRes(t);
        for(int i=0; i<coeurs; i++)
        {
            thread_calc[i] = new ProcCalcul(t,i);
        }
        for(int i=0; i<coeurs; i++)
        {
            thread_calc[i].start();
        }
        sendfile.start();


        while(!arreter) {
            if(sisr.ready())
            {
                str = sisr.readLine();
                if(str.equals("END")) {
                    arreter=true;
                    sisw.println("END");
                    try{Thread.sleep(500);}catch(Exception e){}
                    System.out.println("Le serveur a demandé la fin du programme");
                    System.exit(0);
                }
                else if(str.split(" ").length == 2) {
                    BigInteger debut = new BigInteger(str.split(" ")[0]);
                    BigInteger fin = new BigInteger(str.split(" ")[1]);
                    t.LancerTache(debut, fin);

                    try{Thread.sleep(500);}catch(Exception e){}
                }
            }
        }
        oos.close();
        System.exit(0);
    }

    public static void envoyerPersistances(BigInteger debut, BigInteger fin, Tache t) {

        try {
            socketObjets = new Socket(adresse,10000);
            oos = new ObjectOutputStream(socketObjets.getOutputStream());
            Hachtable hm = new Hachtable(debut, fin,t.getHashtableAdd(),t.getHashtableMult());
            oos.writeObject(hm);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        t.afficherHashmap("mult", debut, fin);
        t.reset();
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
    private BigInteger debut;
    private BigInteger fin;
    private Hashtable<BigInteger, Integer> persistanceAdditive ;
    private Hashtable<BigInteger, Integer> persistanceMultiplicative;
    public boolean end_calc;
    public boolean[] threads_finis;

    public Tache()
    {
        this.min = new BigInteger("0");
        this.debut = min;
        this.max = new BigInteger("-1");
        this.fin = max;
        this.persistanceAdditive = new Hashtable<>();
        this.persistanceMultiplicative = new Hashtable<>();
        this.end_calc = true;
        this.threads_finis = new boolean[Worker.coeurs];
        for( boolean i : threads_finis){
            i = true;
        }
    }

    public Hashtable<BigInteger, Integer> getHashtableMult(){
        return this.persistanceMultiplicative;
    }

    public Hashtable<BigInteger, Integer> getHashtableAdd(){
        return this.persistanceAdditive;
    }

    public boolean getStatutThreads(){
        boolean ok = false;
        for(boolean statut : this.threads_finis){
            ok = statut;
        }
        return ok;
    }

    public void setStatutThreads(int i, boolean bool){
        this.threads_finis[i] = bool;
    }


    public synchronized void LancerTache(BigInteger min, BigInteger max)
    {
        this.min = min;
        this.debut = min;
        this.max = max;
        this.fin = max;
        this.end_calc = false;
        System.out.println("Tache "+debut+ "-"+fin+" lancee");
        notify();
    }

    public synchronized BigInteger getNumber()
    {
        while(end_calc){
            try{this.wait();}catch(Exception e){e.printStackTrace();};
        }
        end_calc = !(this.min.compareTo(this.max) <= 0);
        BigInteger res = this.min;
        if(!end_calc){
            this.min = this.min.add(BigInteger.ONE);
            notify();
        }
        return res;
    }

    public synchronized void EnvoyerPersistances(){
        if( !(this.max.compareTo(new BigInteger("-1")) == 0) ){
            if( !getStatutThreads() || !end_calc){
                try{this.wait();}catch(Exception e){e.printStackTrace();}
                System.out.println("Coince");
            }
            else{
                try {
                    this.reset();
                    System.out.println("Fichier en cours d'envoi");
                    Worker.socketObjets = new Socket(Worker.adresse,10000);
                    Worker.oos = new ObjectOutputStream(Worker.socketObjets.getOutputStream());
                    Hachtable hm = new Hachtable(this.debut, this.fin,getHashtableAdd(),getHashtableMult());
                    Worker.oos.writeObject(hm);
                    Worker.oos.flush();
                    afficherHashmap("mult", debut, this.fin);
                    afficherHashmap("add", debut, this.fin);
                    this.persistanceMultiplicative.clear();
                    this.persistanceAdditive.clear();
                    System.out.println(debut + "-" + fin + " envoye" );
                } catch (IOException e) {e.printStackTrace();}
            }
        }
    }

    public void afficherHashmap(String type, BigInteger debut, BigInteger fin) {
        if(type == "mult"){
            for(BigInteger key = debut; key.compareTo(fin) < 0; key = key.add(BigInteger.ONE)) {
                System.out.println("key = " + key + " value = " + persistanceMultiplicative.get(key));
            }
        }
        else if (type == "add"){
            for(BigInteger key = debut; key.compareTo(fin) < 0; key = key.add(BigInteger.ONE)) {
                System.out.println("key = " + key + " value = " + persistanceAdditive.get(key));
            }
        }
        else{
            System.out.println("Mauvais type");
        }
    }


    public synchronized void ajouteAdd(BigInteger nb, int pers)
    {
        this.persistanceAdditive.put(nb, pers);
        notify();
    }

    public synchronized void ajouteMult(BigInteger nb, int pers)
    {
        this.persistanceMultiplicative.put(nb, pers);
        notify();
    }

    public void reset()
    {
        System.out.println("RESET POUR "+debut+"-"+fin);
        this.min = new BigInteger("0");
        this.max = new BigInteger("-1");
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

    public int persistanceMultiplicative(BigInteger nb) {
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
    public int persistanceAdditive(BigInteger nb) {
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
    @Override
    public void run(){
        BigInteger nb;
        int res1, res2;
        while(true)
        {
            //System.out.println("Thread n°" + indice + " commence");
            nb = t.getNumber();
            t.setStatutThreads(this.indice, false);
            res1 = persistanceMultiplicative(nb);
            res2 = persistanceAdditive(nb);
            t.ajouteMult(nb, res1);
            t.ajouteAdd(nb, res2);
            t.setStatutThreads(this.indice, true);
            //System.out.println("Thread n°" + indice + " fini");
        }
    }
}

class EnvoyerRes extends Thread{
    private Tache t;

    public EnvoyerRes(Tache t){
        this.t = t;
    }

    @Override
    public void run(){
        while(true){
            t.EnvoyerPersistances();
            try{Thread.sleep(200);}catch(Exception e){e.printStackTrace();}
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

