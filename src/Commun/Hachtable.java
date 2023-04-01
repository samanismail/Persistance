/*
 * Nom de classe : Hachtable
 *
 * Description   :Cette classe représente l'objet Hachtable qui contient les persistances additive et multiplicative
 *                qui est envoyée comme objet sérialisé depuis le Worker au Serveur
 *                le Serveur et le Worker utilisent cette mêmes classe pour envoyer et recevoir des objets sérialisés
 *
 * Version       : 1.0
 *
 * Date          : 28/03/2023
 *
 * Copyright     : Saman ISMAIL, Tristan HOARAU, Delshad KADDO
 */

package Commun;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Hashtable;

public class Hachtable implements Serializable
{
    private Hashtable<BigInteger, Integer> persistanceA;//la hashtable contenant les persistance additive
    private Hashtable<BigInteger, Integer> persistanceM;//la hashtable contenant les persistance multiplicative
    private BigInteger debut;//le debut de l'intervalle
    private BigInteger fin;//la fin de l'intervalle

    //constructeur
    public Hachtable(BigInteger debut, BigInteger fin, Hashtable<BigInteger,
            Integer> persistanceA, Hashtable<BigInteger, Integer> persistanceM)
    {
        this.persistanceA = persistanceA;
        this.persistanceM = persistanceM;
        this.debut = debut;
        this.fin = fin;
    }

    //getters
    public Hashtable<BigInteger, Integer> getPersistanceA()
    {
        return persistanceA;
    }

    public Hashtable<BigInteger, Integer> getPersistanceM()
    {
        return persistanceM;
    }

    public BigInteger getDebut()
    {
        return debut;
    }

    public BigInteger getFin()
    {
        return fin;
    }
}

