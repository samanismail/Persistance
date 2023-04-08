/*
 * Nom de classe : ServerSocketHandler
 *
 * Description   : Cette classe permet de créer un thread qui gère les connexions entrantes
 *                Elle permet de créer un thread pour les ConnexionsClient et un autre pour les ConnexionsWorker
 *
 * Version       : 1.0
 *
 * Date          : 25/03/2023
 *
 * Copyright     : Saman ISMAIL, Tristan HOARAU, Delshad KADDO
 */
package ServeurPackage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class ServerSocketHandler extends Thread
{
    private int port;
    private String type;
    private ServerSocket serverSocket;


    public ServerSocketHandler(int port,String type) throws IOException {
            this.port = port;
            this.type = type;
            this.serverSocket = new ServerSocket(port);
    }
    public void run()
    {
        try
        {
            //tant que le nombre maximal de clients et de workers n'est pas atteint
            while(Serveur.numClient < Serveur.maxClients && Serveur.numWorker < Serveur.maxWorkers)
            {
                Socket soc = serverSocket.accept();//on accepte la connexion
                if(this.type.equals("client"))//si c'est un client
                {
                    ConnexionClient cc = new ConnexionClient(soc);//on crée un thread pour gérer la connexion du client
                    cc.start();//on lance le thread
                }
                if(this.type.equals("worker"))//si c'est un worker
                {
                    ConnexionWorker cw = new ConnexionWorker(soc);//on crée un thread pour gérer la connexion du worker
                    cw.start();//on lance le thread
                }

            }
        }catch(IOException e){}
    }
}


