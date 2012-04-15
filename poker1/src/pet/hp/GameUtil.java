package pet.hp;

import java.text.NumberFormat;

import pet.eq.*;

/**
 * Utility methods for game objects, most methods are just interested in game type
 */
public class GameUtil {

	/** poker equity functions */
	private static final Poker drawPoker = new DrawPoker();
	private static final Poker holdemPoker = new HEPoker(false);
	private static final Poker omahaPoker = new HEPoker(true);
	private static final String[] hestreetnames = { "Pre flop", "Flop", "Turn", "River" };
	private static final String[] drawstreetnames = { "Pre draw", "Post draw" };

	/** get full name of currency */
	public static String getCurrencyName(char currency) {
		switch (currency) {
			case '$': 
				return "USD";
			case '€': 
				return "EUR";
			case Game.PLAY_CURRENCY: 
				return "Play";
			default: 
				throw new RuntimeException("no such currency " + currency);
		}
	}

	/** get full name of limit type */
	public static String getLimitName(char limittype) {
		switch (limittype) {
			case Game.POT_LIMIT: 
				return "PL"; //"Pot Limit";
			case Game.NO_LIMIT: 
				return "NL"; //"No Limit";
			case Game.FIXED_LIMIT: 
				return "FL"; //"Fixed Limit";
			default: 
				throw new RuntimeException("no such limit " + limittype);
		}
	}

	/** get full name of game */
	public static String getGameTypeName(char gametype) {
		switch (gametype) {
			case Game.OM_TYPE: 
				return "Omaha";
			case Game.HE_TYPE: 
				return "Hold'em";
			case Game.FCD_TYPE: 
				return "5 Card Draw";
			default: 
				throw new RuntimeException("no such game type " + gametype);
		}
	}

	/** get full name of mixed game type */
	public static String getMixTypeName(char mixtype) {
		switch (mixtype) {
			case Game.NLHE_PLO_MIX: 
				return "Mixed NLH/PLO";
			default: 
				throw new RuntimeException("unknown mix type " + mixtype);
		}
	}

	/** get full name of game variant */
	public static String getSubTypeName(char subtype) {
		switch (subtype) {
			case Game.ZOOM_SUBTYPE: 
				return "Zoom";
			default: 
				throw new RuntimeException("unknown subtype type " + subtype);
		}
	}

	/**
	 * return string description of game
	 */
	public static String getGameId(Game game) {
		// Play 5 Card Draw NL 6max Zoom 0.01/0.02
		// (curr) [mix] (type) (lim) (max) [zoom] (blinds)
		StringBuilder sb = new StringBuilder();
		sb.append(getCurrencyName(game.currency)).append(" ");
		if (game.mix != 0) {
			sb.append(getMixTypeName(game.mix)).append(": ");
		}
		sb.append(getGameTypeName(game.type)).append(" ");
		sb.append(getLimitName(game.limit)).append(" ");
		sb.append(game.max).append("-max ");
		if (game.subtype != 0) {
			sb.append(getSubTypeName(game.subtype)).append(" ");
		}
		sb.append(formatMoney(game.currency, game.sb)).append("/");
		sb.append(formatMoney(game.currency, game.bb));
		return sb.toString();
	}

	/** return the number of hole cards for this game */
	public static int getHoleCards(char gametype) {
		switch (gametype) {
			case Game.FCD_TYPE:
				return 5;
			case Game.HE_TYPE:
				return 2;
			case Game.OM_TYPE:
				return 4;
			default: 
				throw new RuntimeException("unknown game type " + gametype);
		}
	}

	/** return the minimum number of hole cards required for an equity calculation for this game */
	public static int getMinHoleCards(char gametype) {
		// should probably get this from poker instance
		switch (gametype) {
			case Game.FCD_TYPE:
				return 1;
			case Game.HE_TYPE:
				return 1;
			case Game.OM_TYPE:
				return 2;
			default: 
				throw new RuntimeException("unknown game type " + gametype);
		}
	}

	/** return a string representing unknown hole cards for this game */
	public static String unknownCardsString(char gametype) {
		switch (gametype) {
			case Game.FCD_TYPE:
				return "[ ][ ][ ][ ][ ]";
			case Game.HE_TYPE:
				return "[ ][ ]";
			case Game.OM_TYPE:
				return "[ ][ ][ ][ ]";
		}
		throw new RuntimeException("unknown game type " + gametype);
	}

	/** return true if this street is the showdown street for the given game type */
	public static boolean isShowdown (char gametype, int street) {
		switch (gametype) {
			case Game.FCD_TYPE:
				return street == drawstreetnames.length - 1;
			case Game.HE_TYPE:
			case Game.OM_TYPE:
				return street == hestreetnames.length - 1;
		}
		throw new RuntimeException("unknown game type " + gametype);
	}

	/** return the maximum number of streets in this game type */
	public static int getMaxStreets (char gametype) {
		switch (gametype) {
			case Game.FCD_TYPE:
				return drawstreetnames.length;
			case Game.HE_TYPE:
			case Game.OM_TYPE:
				return hestreetnames.length;
		}
		throw new RuntimeException("unknown game type " + gametype);
	}

	/** get the name of the street for this game type */
	public static String getStreetName (char gametype, int street) {
		switch (gametype) {
			case Game.FCD_TYPE:
				return drawstreetnames[street];
			case Game.HE_TYPE:
			case Game.OM_TYPE:
				return hestreetnames[street];
		}
		throw new RuntimeException("unknown game type " + gametype);
	}

	public static String formatMoney(char currency, int amount) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		switch (currency) {
			case '$':
			case '€':
				// TODO $2 instead of $2.00
				return String.format("%c%.2f", currency, amount / 100f);
			case Game.PLAY_CURRENCY:
				return nf.format(amount);
			default: throw new RuntimeException("unknown currency " + currency);
		}
	}

	/**
	 * Get poker equity function for game type
	 */
	public static Poker getPoker(char gametype) {
		switch (gametype) {
			case Game.FCD_TYPE:
				return drawPoker;
			case Game.HE_TYPE:
				return holdemPoker;
			case Game.OM_TYPE:
				return omahaPoker;
		}
		throw new RuntimeException("no poker for type " + gametype);
	}

}
