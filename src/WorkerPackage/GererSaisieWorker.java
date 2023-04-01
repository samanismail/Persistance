/*
 * Nom de classe : GererSaisieWorker
 *
 * Description   : Cette classe gère les instructions envoyées par le Worker au Serveur, notamment si nous souhaitons déconnecter le Worker du Serveur.
 *
 * Version       : 1.0
 *
 * Date          : 25/03/2023
 * 
 * Copyright     : Saman ISMAIL, Tristan HOARAU, Delshad KADDO
 */
package WorkerPackage;

import java.io.*;

class GererSaisieWorker extends Thread
{
    private final BufferedReader entreeClavier;
    private final PrintWriter pw;

    public GererSaisieWorker(PrintWriter pw)
    {
        entreeClavier = new BufferedReader(new InputStreamReader(System.in));
        this.pw=pw;
    }

    public void run()
    {
        String str;
        try
        {
            //tant que l'on n'a pas reçu le mot END ou que le worker n'a pas reçu le mot END
            while(!(str = entreeClavier.readLine()).equals("END"))
            {
                pw.println(str);//on envoie la commande au serveur
            }
            pw.println("END");//on envoie le mot END au serveur si le worker a tapé END
            WorkerPackage.Worker.arreter=true;//on arrête le worker
        }catch(IOException e){e.printStackTrace();}

    }
}