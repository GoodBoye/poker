package pet.eq;

import java.util.Arrays;

/**
 * Poker hand valuation.
 */
public abstract class Poker {
	
	/**
	 * Rank masks (allowing 20 bits for hand value, i.e. 4 bits per card)
	 */
	private static final int H_RANK = 0;
	private static final int P_RANK = 1 << 20;
	private static final int TP_RANK = 2 << 20;
	private static final int TK_RANK = 3 << 20;
	private static final int ST_RANK = 4 << 20;
	private static final int FL_RANK = 5 << 20;
	private static final int FH_RANK = 6 << 20;
	private static final int FK_RANK = 7 << 20;
	private static final int SF_RANK = 8 << 20;
	private static final int LOW_RANK = 9 << 20;
	/**
	 * short rank names (value >> 20)
	 */
	public static final String[] ranknames = { "H", "P", "2P", "3K", "S", "F", "FH", "4K", "SF" };
	/** card suit representations */
	public static final char H_SUIT = 'h', C_SUIT = 'c', S_SUIT = 's', D_SUIT = 'd';
	/** complete deck */
	public static final String[] FULL_DECK = new String[] { 
		"2h", "2s", "2c", "2d",
		"3h", "3s", "3c", "3d", "4h", "4s", "4c", "4d", "5h", "5s", "5c",
		"5d", "6h", "6s", "6c", "6d", "7h", "7s", "7c", "7d", "8h", "8s",
		"8c", "8d", "9h", "9s", "9c", "9d", "Th", "Ts", "Tc", "Td", "Jh",
		"Js", "Jc", "Jd", "Qh", "Qs", "Qc", "Qd", "Kh", "Ks", "Kc", "Kd",
		"Ah", "As", "Ac", "Ad" 
	};
	/** complete suits */
	public static final char[] suits = { S_SUIT, H_SUIT, C_SUIT, D_SUIT };

	private final String[] hand = new String[5];
	public boolean debug;
	
	public abstract HandEq[] equity(String[] board, String[][] holes);

	protected void println(String s) {
		if (debug) {
			System.out.println(s);
		}
	}

	private static boolean isLow(String[] hand) {
		for (int n = 0; n < hand.length; n++) {
			if (faceValue(hand[n], false) > 8) {
				return false;
			}
		}
		return true;
	}

	public int lowValue(String[] hand) {
		if (isLow(hand)) {
			int p = isPair(hand, false);
			if ((p & 0xf00000) == H_RANK) {
				// no pairs
				// invert value
				return (LOW_RANK | 0xfffff) - (p & 0xfffff);
			}
		}
		return 0;
	}
	
	private void validate(String[] h) {
		for (int n = 0; n < h.length; n++) {
			String c = h[n];
			if ("23456789TJQKA".indexOf(face(c)) == -1 || "hdsc".indexOf(suit(c)) == -1) {
				throw new RuntimeException("invalid hand " + Arrays.toString(h));
			}
			for (int m = n + 1; m < h.length; m++) {
				if (c.equals(h[m])) {
					throw new RuntimeException("invalid hand " + Arrays.toString(h));
				}
			}
		}
	}

	/**
	 * Get high value of 5 card hand
	 */
	public int value(String[] handp) {
		// copy so we can sort
		ArrayUtil.copy(handp, hand);
		ArrayUtil.sort(hand, Cmp.faceCmp);
		//validate(hand);
		int f = isFlush(hand);
		int s = isStraight(hand);
		if (f != 0) {
			if (s != 0) {
				return SF_RANK | (s & 0xfffff);
			} else {
				return f;
			}
		} else if (s != 0) {
			return s;
		}
		return isPair(hand, true);
	}

	private static int isFlush(String[] hand) {
		char s = suit(hand[0]);
		for (int n = 1; n < 5; n++) {
			if (suit(hand[n]) != s) {
				return 0;
			}
		}
		// requires sorted hand
		return FL_RANK | (faceValue(hand[0]) << 16) + (faceValue(hand[1]) << 12) + (faceValue(hand[2]) << 8) + (faceValue(hand[3]) << 4) + faceValue(hand[4]);
	}

	private static int isStraight(String[] hand) {
		// requires sorted hand
		// max str is AKJQT
		// min str is A5432
		int v0 = faceValue(hand[0]);
		int v = faceValue(hand[1]);
		int hc = 0;
		if (v0 == 14 && v == 5) {
			hc = 5;
		} else if (v0 == v + 1) {
			hc = v0;
		}
		if (hc != 0) {
			for (int n = 2; n < 5; n++) {
				int vn = faceValue(hand[n]);
				if (v != vn + 1) {
					return 0;
				}
				v = vn;
			}
			return ST_RANK | hc;
		}
		return 0;
	}

	/**
	 * Return pair value or high cards.
	 * Does not require sorted hand
	 */
	private static int isPair(String[] hand, boolean acehigh) {
		// count card face frequencies (3 bits each) -- 0, 1, 2, 3, 4
		long v = 0;
		for (int n = 0; n < hand.length; n++) {
			v += (1L << ((14 - faceValue(hand[n], acehigh)) * 3));
		}
		// get the card faces for each frequency
		int fk = 0, tk = 0, pa = 0, hc = 0;
		for (int f = 14; v != 0; v >>= 3, f--) {
			int i = (int) (v & 7);
			if (i == 0) {
				continue;
			} else if (i == 1) {
				hc = (hc << 4) | f;
			} else if (i == 2) {
				pa = (pa << 4) | f;
			} else if (i == 3) {
				tk = f;
			} else if (i == 4) {
				fk = f;
			}
		}

		if (fk != 0) {
			return FK_RANK | (fk << 4) | hc;
		} else if (tk != 0) {
			if (pa != 0) {
				return FH_RANK | (tk << 4) | pa;
			} else {
				return TK_RANK | (tk << 8) | hc;
			}
		} else if (pa >= 16) {
			return TP_RANK | (pa << 4) | hc;
		} else if (pa != 0) {
			return P_RANK | (pa << 12) | hc;
		} else {
			return H_RANK | hc;
		}
	}

	/**
	 * Return integer value of card face, ace high (from A = 14 to deuce = 2)
	 */
	static int faceValue(String card) {
		int i = "23456789TJQKA".indexOf(face(card));
		if (i >= 0) {
			return i + 2;
		}
		throw new RuntimeException("unknown face " + card);
	}

	/**
	 * Return integer value of card face, ace high or low (from A = 14 to 2 = 2 or K = 13 to A = 1)
	 */
	private static int faceValue(String card, boolean acehigh) {
		int v = faceValue(card);
		if (v == 14 && !acehigh) {
			v = 1;
		}
		return v;
	}

	/**
	 * Returns lowercase character representing suit, i.e. s, d, h or c
	 */
	public static final char suit(String card) {
		return card.charAt(1);
	}

	/**
	 * Return character symbol of face value
	 */
	private static char valueFace(int x) {
		int v = x & 0xf;
		// allow 0 index
		return "**23456789TJQKA".charAt(v);
	}

	/**
	 * Return string representation of hand value
	 */
	public static String valueString(int value) {
		if (value == 0) {
			return "nil";
		}
		if ((value & 0xf00000) == LOW_RANK) {
			value = (LOW_RANK | 0xfffff) - value;
		}
		char c1 = valueFace(value);
		char c2 = valueFace(value >> 4);
		char c3 = valueFace(value >> 8);
		char c4 = valueFace(value >> 12);
		char c5 = valueFace(value >> 16);
		switch (value & 0xf00000) {
		case LOW_RANK: return c1 + " " + c2 + " " + c3 + " " + c4 + " " + c5 + " high";
		case SF_RANK: return "Straight Flush " + c1;
		case FK_RANK: return "Four of a Kind " + c2 + " - " + c1;
		case FH_RANK: return "Full House " + c2 + " full of " + c1;
		case FL_RANK: return "Flush - " + c5 + " " + c4 + " " + c3 + " " + c2 + " " + c1 + " high";
		case ST_RANK: return "Straight - " + c1 + " high";
		case TK_RANK: return "Three of a Kind " + c3 + " - " + c2 + " " + c1;
		case TP_RANK: return "Two Pair " + c3 + " and " + c2 + " - " + c1;
		case P_RANK: return "Pair " + c4 + " - " + c3 + " " + c2 + " " + c1;
		case H_RANK: return c5 + " " + c4 + " " + c3 + " " + c2 + " " + c1 + " high";
		default: return "Unknown";
		}
	}

	public static char face(String card) {
		return card.charAt(0);
	}
	
}
