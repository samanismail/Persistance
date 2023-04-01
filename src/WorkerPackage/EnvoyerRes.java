/*
 * Nom de classe : EnvoyerRes
 *
 * Description   : Cette classe est un thread qui gère l'envoi de l'objet Hachtable lorsque les calculs sont terminés
 *
 * Version       : 1.0
 *
 * Date          : 28/03/2023
 * 
 * Copyright     : Saman ISMAIL, Tristan HOARAU, Delshad KADDO
 */
package WorkerPackage;

class EnvoyerRes extends Thread
{
    private Tache t;//

    // Constructeur
    public EnvoyerRes(Tache t)
    {
        this.t = t;
    }

    @Override
    public void run()
    {
        while(true)
        {
            t.EnvoyerPersistances();// Appel de la méthode EnvoyerPersistances de la classe Tache
            try{Thread.sleep(100);}catch(Exception e){e.printStackTrace();}
        }
    }
}
