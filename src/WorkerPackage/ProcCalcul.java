/*
 * Nom de classe : ProcCalcul
 *
 * Description   : Cette classe correspond à chaque thread du Worker qui s'occupe des calculs. Ce thread réalise le calcul 
 *                 des persistances additive et multiplicative d'un nombre puis écrit ce résultat dans les Hashtable
 *                 correspondates.
 *
 * Version       : 1.0
 *
 * Date          : 28/03/2023
 * 
 * Copyright     : Saman ISMAIL, Tristan HOARAU, Delshad KADDO
 */
package WorkerPackage;

import java.math.BigInteger;

class ProcCalcul extends Thread
{

    private Tache t;
    private int indice;

    public ProcCalcul (Tache t, int i)
    {
        this.t = t;
        this.indice = i;
    }

    //Cette méthode calcule la persistance multiplicative d'un nombre
    public int persistanceMultiplicative(BigInteger nb)
    {
        int res = 0;//résultat de la persistance

        while (nb.compareTo(BigInteger.TEN) >= 0)//tant que le nombre est supérieur ou égal à 10
        {
            BigInteger produit = BigInteger.ONE;//on initialise le produit à 1

            while (nb.compareTo(BigInteger.ZERO) > 0)//tant que le nombre est supérieur à 0
            {
                //divAndRem[0] = quotient, divAndRem[1] = reste
                BigInteger[] divAndRem = nb.divideAndRemainder(BigInteger.TEN);//on divise le nombre par 10
                produit = produit.multiply(divAndRem[1]);//on multiplie le produit par le reste
                nb = divAndRem[0];//on affecte le quotient à nb
            }

            nb = produit;//on affecte le produit à nb
            res++;
        }
        return res;
    }

    //Cette méthode calcule la persistance additive d'un nombre
    public int persistanceAdditive(BigInteger nb)
    {
        int res = 0;

        while (nb.compareTo(BigInteger.TEN) >= 0)//tant que le nombre est supérieur à 0
        {
            int somme = 0;//on initialise la somme à 0

            while (nb.compareTo(BigInteger.ZERO) > 0)//tant que le nombre est supérieur à 0
            {
                //divAndRem[0] = quotient, divAndRem[1] = reste
                BigInteger[] divAndRem = nb.divideAndRemainder(BigInteger.TEN);//on divise le nombre par 10
                somme += divAndRem[1].intValue();//on ajoute le reste à la somme
                nb = divAndRem[0];//on affecte le quotient à nb
            }

            nb = BigInteger.valueOf(somme);//on affecte la somme à nb
            res++;
        }
        return res;
    }
    @Override
    public void run()
    {
        BigInteger nb;//nombre à traiter
        int res1;//résultat de la persistance additive
        int res2;//résultat de la persistance multiplicative

        while(true)
        {
            nb = t.getNumber();//on récupère le nombre à traiter
            t.setStatutThreads(this.indice, false);//on met le statut du thread à false
            res1 = persistanceMultiplicative(nb);//on calcule la persistance additive
            res2 = persistanceAdditive(nb);//on calcule la persistance multiplicative
            t.ajouteMult(nb, res1);//on ajoute le résultat dans la Hashtable Multiplica
            t.ajouteAdd(nb, res2);//on ajoute le résultat dans la Hashtable Additive
            t.setStatutThreads(this.indice, true);//on met le statut du thread à true
        }
    }
}
