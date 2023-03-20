import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Worker implements Runnable, Serializable{
    private InetAddress adresseIP;
    private String nom;
    private int coeurs;
    private final int port = 8080;
    private Socket socket;

    public Worker(InetAddress adresseIP, String nom, int coeurs) throws IOException {
        this.adresseIP = adresseIP;
        this.nom = nom;
        this.coeurs = coeurs;


    }

    public void run(){
        String requette;
        try {
            Socket socket = new Socket("10.192.34.181", port);
            System.out.println("SOCKET = " + socket);
            // illustration des capacites bidirectionnelles du flux
            BufferedReader sisr = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            PrintWriter sisw = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream())),true);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String getNom(){
        return this.nom;
    }
    public int getCoeurs(){
        return this.coeurs;
    }
    public InetAddress getAdresseIP(){
        return this.adresseIP;
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        Worker worker = new Worker(InetAddress.getLocalHost(),InetAddress.getLocalHost().getHostName(),Runtime.getRuntime().availableProcessors());//on crée un worker
        Thread thread = new Thread(worker);//on lance le thread du worker
        thread.start();

    }
}
