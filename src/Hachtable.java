import java.io.Serializable;
import java.math.BigInteger;
import java.util.Hashtable;

public class Hachtable implements Serializable {
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
