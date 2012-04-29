package pet.hp;

import java.util.*;

import pet.eq.Poker;

/**
 * stores hands, games and tournaments.
 * thread safe (all methods synchronized)
 */
public class History {

	/** string cache to avoid multiple instances of same string */
	private final HashMap<String,String> cache = new HashMap<String,String>(1000);
	/** game instances */
	private final ArrayList<Game> games = new ArrayList<Game>();
	/** tournament instances */
	private final TreeMap<Long,Tourn> tourns = new TreeMap<Long,Tourn>();
	/** hands seen so far */
	private final ArrayList<Hand> hands = new ArrayList<Hand>();
	/** hand ids seen so far */
	private final HashSet<Long> handIds = new HashSet<Long>();
	/** listeners for history changes */
	private final ArrayList<HistoryListener> listeners = new ArrayList<HistoryListener>();
	/** tournament players seen - XXX inefficient hack */
	private final TreeMap<Long,TreeSet<String>> tp = new TreeMap<Long,TreeSet<String>>();
	
	public History() {
		for (String c : Poker.FULL_DECK) {
			getString(c);
		}
	}
	
	/**
	 * add listener for new data
	 */
	public synchronized void addListener(HistoryListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * add new hand from parser
	 */
	public synchronized void addHand(Hand hand) {
		if (handIds.contains(hand.id)) {
			throw new RuntimeException("duplicate hand id");
		}
		hands.add(hand);
		for (HistoryListener l : listeners) {
			l.handAdded(hand);
		}
	}

	/**
	 * get cached string instance
	 */
	public synchronized String getString(String s) {
		if (s != null) {
			String s2 = cache.get(s);
			if (s2 != null) {
				return s2;
			}
			s = new String(s);
			cache.put(s, s);
		}
		return s;
	}
	
	/**
	 * Get game instance by game id string
	 */
	public synchronized Game getGame(String id) {
		for (Game game : games) {
			if (game.id.equals(id)) {
				return game;
			}
		}
		return null;
	}
	
	/**
	 * Get a list of all game ids.
	 * always returns a new list
	 */
	public synchronized List<String> getGames() {
		ArrayList<String> gameids = new ArrayList<String>(games.size());
		for (Game game : games) {
			gameids.add(game.id);
		}
		Collections.sort(gameids);
		return gameids;
	}

	/**
	 * get the game for the hand line and table details
	 */
	public synchronized Game getGame(char currency, char mix, char type, int subtype, char limit, int max, int sb, int bb) { 
		if (type == 0 || limit == 0 || max == 0 || currency == 0) {
			throw new RuntimeException("invalid game");
		}
		
		if (currency == Game.TOURN_CURRENCY) {
			// don't store blinds for tournament hands as they are variable
			sb = 0;
			bb = 0;
		}
		
		// find game, otherwise create it
		for (Game game : games) {
			if (game.currency == currency && game.type == type && game.limit == limit
					&& game.subtype == subtype && game.sb == sb && game.bb == bb && game.mix == mix
					&& game.max == max) {
				return game;
			}
		}

		Game game = new Game();
		game.currency = currency;
		game.type = type;
		game.limit = limit;
		game.subtype = subtype;
		game.sb = sb;
		game.bb = bb;
		game.max = max;
		game.mix = mix;
		game.id = GameUtil.getGameId(game);
		games.add(game);
		System.out.println("created game " + game);
		
		for (HistoryListener l : listeners) {
			l.gameAdded(game);
		}
		
		return game;
	}

	/**
	 * get tournament instance, possibly creating it
	 */
	public synchronized Tourn getTourn(long id, char cur, int buyin, int cost) {
		Tourn t = tourns.get(id);
		if (t == null) {
			tourns.put(id, t = new Tourn(id));
			t.currency = cur;
			t.buyin = buyin;
			t.cost = cost;
		} else {
			if (t.currency != cur || t.buyin != buyin || t.cost != cost) {
				throw new RuntimeException();
			}
		}
		return t;
	}
	
	/**
	 * Mark players as seen in tournament
	 */
	public synchronized void addTournPlayers(Long tournidobj, Collection<String> players) {
		TreeSet<String> tps = tp.get(tournidobj);
		if (tps == null) {
			tp.put(tournidobj, tps = new TreeSet<String>());
		}
		for (String p : players) {
			if (!tps.contains(p)) {
				tps.add(p);
			}
		}
		Tourn t = tourns.get(tournidobj);
		if (t.players < tps.size()) {
			t.players = tps.size();
		}
	}
	
	/**
	 * Get hands for the player.
	 * Always returns new list
	 */
	public synchronized List<Hand> getHands(String player, String gameid) {
		System.out.println("get hands for " + player + " gameid " + gameid);
		List<Hand> hands = new ArrayList<Hand>();

		for (Hand hand : this.hands) {
			if (hand.game.id.equals(gameid)) {
				for (Seat seat : hand.seats) {
					if (seat.name.equals(player)) {
						hands.add(hand);
						break;
					}
				}
			}
		}

		System.out.println("got " + hands.size() + " hands");
		return hands;
	}

	/**
	 * Get hands for the tournament.
	 * always returns new list
	 */
	public synchronized List<Hand> getHands(long tournid) {
		System.out.println("get hands for tourn " + tournid);
		List<Hand> hands = new ArrayList<Hand>();

		for (Hand hand : this.hands) {
			if (hand.tourn != null && hand.tourn.id == tournid) {
				hands.add(hand);
			}
		}

		System.out.println("got " + hands.size() + " hands");
		return hands;
	}

}
