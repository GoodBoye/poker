
package pet.hp.impl;

import java.util.*;

import pet.hp.*;

/**
 * common utilities for parsers
 */
public abstract class Parser2 extends Parser {
	
	// stuff for current hand, cleared on clear()
	// TODO should all be private...
	
	/** current hand */
	protected Hand hand;
	/** running pot - updated when pip() is called */
	private int pot;
	/** has live sb been posted (others are dead) */
	protected boolean sbposted;
	/** array of seat num to seat pip for this street. seat numbers are 1-10 */
	private final int[] seatPip = new int[11];
	/** map of player name to seat for current hand */
	protected final Map<String, Seat> seatsMap = new TreeMap<>();
	/** streets of action for current hand */
	protected final List<List<Action>> streets = new ArrayList<>();
	
	public Parser2() {
		//
	}
	
	/**
	 * throw exception if condition is false
	 */
	protected void assert_ (boolean cond, String condDesc) {
		if (!cond) {
			throw new RuntimeException("Assertion failed: " + condDesc);
		}
	}
	
	@Override
	public void clear () {
		super.clear();
		streets.clear();
		seatsMap.clear();
		Arrays.fill(seatPip, 0);
		pot = 0;
		hand = null;
		sbposted = false;
	}
	
	protected List<Action> currentStreet () {
		return streets.get(streets.size() - 1);
	}
	
	/**
	 * get the current street number, starting at 0
	 */
	protected int currentStreetIndex () {
		return streets.size() - 1;
	}
	
	/**
	 * throw runtime exception
	 */
	protected void fail (String desc) {
		throw new RuntimeException("Failure: " + desc);
	}
	
	/**
	 * validate and finalise hand
	 */
	protected void finish () {
		validateHand();
		validatePot();
		validateSeats();
		validateActions();
		
		// get seats
		hand.seats = seatsMap.values().toArray(new Seat[seatsMap.size()]);
		Arrays.sort(hand.seats, HandUtil.seatCmp);
		
		// get actions
		hand.streets = new Action[streets.size()][];
		for (int n = 0; n < streets.size(); n++) {
			List<Action> street = streets.get(n);
			hand.streets[n] = street.toArray(new Action[street.size()]);
		}
		
		getHistory().addHand(hand);
		if (hand.tourn != null) {
			getHistory().addTournPlayers(hand.tourn.id, seatsMap.keySet());
		}
		
		println("end of hand " + hand);
		println("seats: " + seatsMap);
		
		clear();
	}
	
	private void validateActions () {
		for (List<Action> actions : streets) {
			for (Action a : actions) {
				switch (a.type) {
					case BRINGSIN:
					case BET:
					case RAISE:
					case CALL:
					case ANTE:
					case POST:
						assert_(a.amount > 0, "am > 0");
						break;
					
					case COLLECT:
					case UNCALL:
						assert_(a.amount < 0, "am < 0");
						break;
					
					default:
						assert_(a.amount == 0, "am = 0");
				}
				
			}
		}
	}
	
	private void validateSeats () {
		int s = 0;
		for (Seat seat : seatsMap.values()) {
			assert_(seat.pip <= seat.chips, "chips");
			int c = 0;
			if (seat.finalHoleCards != null) {
				c += seat.finalHoleCards.length;
			}
			if (seat.finalUpCards != null) {
				c += seat.finalUpCards.length;
			}
			if (seat.showdown) {
				s++;
				assert_(c == GameUtil.getHoleCards(hand.game.type), "hole cards");
			} else {
				assert_(c <= GameUtil.getHoleCards(hand.game.type), "hole cards");
			}
		}
		if (hand.showdown) {
			assert_(s >= 2, "2 seat showdown");
		} else {
			assert_(s == 0, "no seats showdown");
		}
	}
	
	private void validateHand () {
		assert_(hand.date != 0, "has date");
		assert_(hand.button != 0, "has button");
		assert_(hand.game != null, "has game");
		assert_(hand.id != 0, "has id");
		assert_(hand.myseat != null, "has my seat");
		if (hand.showdown) {
			assert_(streets.size() == GameUtil.getMaxStreets(hand.game.type), "all streets");
		} else {
			assert_(streets.size() <= GameUtil.getMaxStreets(hand.game.type), "streets");
		}
		if (!GameUtil.isHilo(hand.game.type)) {
			assert_(!hand.showdownNoLow, "no low for non hilo");
		}
		if (GameUtil.isDraw(hand.game.type)) {
			int d = GameUtil.getHoleCards(hand.game.type);
			for (int n = 0; n < 3; n++) {
				String[] c = hand.myDrawCards(n);
				if (n == 0) {
					assert_(c.length == d, "draw: " + Arrays.toString(c));
				} else {
					assert_(c == null || c.length == d, "draw: " + Arrays.toString(c));
				}
			}
		}
	}
	
	/**
	 * add a new street
	 */
	protected List<Action> newStreet () {
		println("new street");
		streets.add(new ArrayList<Action>());
		return currentStreet();
	}
	
	/**
	 * put in pot - update running pot with seat pips
	 */
	protected void pip () {
		for (Seat seat : seatsMap.values()) {
			int pip = seatPip[seat.num];
			if (pip > 0) {
				println("seat " + seat + " pip " + pip);
				pot += pip;
				seat.pip += pip;
				seatPip[seat.num] = 0;
			}
		}
		// could check if pip amounts are same for those not all in
		println("pot now " + pot);
	}
	
	/**
	 * put an amount in the pot anonymously (dead blinds and antes). also
	 * updates hand.db
	 */
	protected void anonPip (int amount) {
		assert_(amount > 0, "am > 0");
		pot += amount;
		hand.db += amount;
		println("pot now " + pot);
	}
	
	protected int pot () {
		return pot;
	}
	
	/**
	 * get pip total for seat
	 */
	protected int seatPip (Seat seat) {
		return seatPip[seat.num];
	}
	
	/**
	 * seat puts in pot amount
	 */
	protected void seatPip (Seat seat, int amount) {
		seatPip[seat.num] += amount;
	}
	
	protected void validatePot () {
		println("hand pot=" + hand.pot + " rake=" + hand.rake + " antes=" + hand.db);
		// validate pot size
		assert_(hand.pot == pot, "total pot " + hand.pot + " equal to running pot " + pot);
		
		int won = 0;
		int pip = 0;
		for (Seat seat : seatsMap.values()) {
			won += seat.won;
			pip += seat.pip;
		}
		println("won=" + won + " pip=" + pip);
		
		assert_(won == (hand.pot - hand.rake), "won " + won + " equal to pot " + pot + " - rake " + hand.rake);
		assert_(won == (pip - hand.rake + hand.db), "won " + won + " equal to pip " + pip + " - rake " + hand.rake
				+ " + antes " + hand.db);
		
		int asum = -hand.rake;
		for (List<Action> str : streets) {
			for (Action ac : str) {
				if (ac.amount != 0) {
					println("actsum " + ac);
					asum += ac.amount;
				}
			}
		}
		assert_(asum == 0, "actsum " + asum + " = zero");
		
	}
	
}
