package pet.eq;

/**
 * multiple equity - represents the only or high/low equity of a hand
 */
public class MEquity {
	
	/** equity instances */
	public final Equity[] eqs;
	/** is this equity exact or sampled */
	public final boolean exact;
	/** number of cards remaining in deck */
	public final int remCards;
	/** is hi/hilo-hi/hilo-lo combination */
	public final boolean hilo;
	
	/** percentage of hands with low possible */
	public float lowPossible;
	/** percentage total equity, including hi/lo and ties */
	public float totaleq;
	/** percentage times the player will win the entire pot */
	public float scoop;
	
	/** number of times won all pots, no ties */
	int scoopcount;
	
	/** create mequity - either hi/hi half/lo half or with given equity type, not both */
	MEquity(boolean hilo, int eqtype, int rem, boolean exact) {
		this.hilo = hilo;
		if (hilo) {
			this.eqs = new Equity[] { 
					new Equity(Equity.HI_ONLY), 
					new Equity(Equity.HILO_HI_HALF),
					new Equity(Equity.HILO_AFLO8_HALF) 
			};
		} else {
			this.eqs = new Equity[] { 
					new Equity(eqtype) 
			};
		}
		this.remCards = rem;
		this.exact = exact;
	}
	
	/** get the equity instance for the given equity type */
	public Equity getEq(int eqtype) {
		int i;
		switch (eqtype) {
			case Equity.DSLO_ONLY:
			case Equity.AFLO_ONLY:
			case Equity.AFLO8_ONLY:
			case Equity.HI_ONLY:
				i = 0;
				break;
			case Equity.HILO_HI_HALF:
				i = 1;
				break;
			case Equity.HILO_AFLO8_HALF:
				i = 2;
				break;
			default:
				throw new RuntimeException();
		}
		Equity e = eqs[i];
		if (e.eqtype != eqtype) {
			throw new RuntimeException("eq is type " + e.eqtype + " not " + eqtype);
		}
		return e;
	}
	
}
