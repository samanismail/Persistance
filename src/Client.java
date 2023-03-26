import java.io.*;
import java.net.*;
public class Client {
    static int port = 9000;
    static boolean arreter=false;

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("10.192.34.181",9000);
        System.out.println("SOCKET = " + socket);
        BufferedReader sisr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        PrintWriter sisw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);

        GererSaisieClient saisie=new GererSaisieClient(sisw);
        saisie.start();

        String str;
        while(arreter!=true) {
            str = sisr.readLine();
            System.out.println("Serveur=>"+str);
        }

        System.out.println("END");
        //sisw.println("END") ;
        sisr.close();
        sisw.close();
        socket.close();
    }
}

class GererSaisieClient extends Thread{
    private BufferedReader entreeClavier;
    private PrintWriter pw;

    public GererSaisieClient(PrintWriter pw){
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
        Client.arreter=true;
        System.exit(0);
    }
}