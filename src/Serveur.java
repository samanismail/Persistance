import java.io.*;
import java.net.*;

public class Serveur {
    static int port = 8080;
    static final int maxClients=10;
    static int numClient=0;
    static PrintWriter[] pwClient;

    public static void main(String[] args) throws Exception {
        pwClient=new PrintWriter[maxClients];
        ServerSocket s = new ServerSocket(8080);
        System.out.println("En attente de connexions...");

        while (numClient<maxClients){
            Socket soc = s.accept();
                ConnexionClient cc=new ConnexionClient(soc.getInetAddress().getHostName(),soc.getInetAddress().getHostAddress(),soc,numClient);
                System.out.println("Nouveau client => Pseudo: "+soc.getInetAddress().getHostName()+" IP :"+soc.getInetAddress().getHostAddress());
                numClient++;
                cc.start();


        }
    }

}

class ConnexionClient extends Thread{
    private String ip;
    private int id;
    private String pseudo;
    private boolean arret=false;
    private Socket s;
    private BufferedReader sisr;
    private PrintWriter sisw;

    public ConnexionClient(String pseudo,String ip,Socket s,int id){
        this.pseudo=pseudo;
        this.id=id;
        this.s=s;
        this.ip=ip;
        try{
            sisr = new BufferedReader(new InputStreamReader(s.getInputStream()));
            sisw = new PrintWriter( new BufferedWriter(new OutputStreamWriter(s.getOutputStream())),true);
            Serveur.pwClient[id]=sisw;
            GererSaisie saisie=new GererSaisie(sisw);
            saisie.start();
        }catch(IOException e){e.printStackTrace();}


    }
    public void run(){
        try{
            while (true) {

                String str = sisr.readLine();          		// lecture du message
                if (str.equals("END")){
                    Serveur.numClient--;
                    Serveur.pwClient[id]=null;
                    System.out.println("Client "+pseudo+" déconnecté");
                    break;
                }
                System.out.println(pseudo+"=>"+str);
            }
            sisr.close();
            sisw.close();
            s.close();
        }catch(IOException e){e.printStackTrace();}
    }
}