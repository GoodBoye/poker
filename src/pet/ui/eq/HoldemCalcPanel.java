package pet.ui.eq;

import java.util.*;
import javax.swing.*;

import pet.eq.*;

public class HoldemCalcPanel extends CalcPanel {
	
	private final CardPanel boardPanel;
	private final HandCardPanel[] handPanels = new HandCardPanel[10];
	private final JCheckBox randHandsBox = new JCheckBox("Hands");
	private final JCheckBox randFlopBox = new JCheckBox("Flop");
	private final JCheckBox randTurnBox = new JCheckBox("Turn");
	private final JCheckBox randRiverBox = new JCheckBox("River");
	private final JCheckBox hiloBox = new JCheckBox("Hi/Lo");
	private final boolean omaha;
	private final int numHoleCards;

	public HoldemCalcPanel(boolean omaha) {
		this.omaha = omaha;
		this.numHoleCards = omaha ? 4 : 2;
		
		// create board and hands and collect card labels
		boardPanel = new CardPanel("Community Cards", 0, 5);
		boardPanel.collectCardLabels(cardLabels);
		
		String name = omaha ? "Omaha" : "Hold'em";
		int min = omaha ? 2 : 1;
		int max = omaha ? 4 : 2;
		for (int n = 0; n < handPanels.length; n++) {
			HandCardPanel cp = new HandCardPanel(name + " hand " + (n+1), min, max);
			cp.collectCardLabels(cardLabels);
			handPanels[n] = cp;
		}
		
		// add to layout
		setBoard(boardPanel);
		setHands(handPanels);
		
		initCardLabels();
		
		// select first hole card
		selectCard(5);

		randHandsBox.setSelected(true);
		randFlopBox.setSelected(true);
		
		addRandOpt(randHandsBox);
		addRandOpt(randFlopBox);
		addRandOpt(randTurnBox);
		addRandOpt(randRiverBox);
		if (omaha) {
			addCalcOpt(hiloBox);
		}
	}
	
	/**
	 * display the given hand
	 */
	public void displayHand(String[] board, String[][] holes, boolean hilo) {
		clear();
		boardPanel.setCards(board);
		for (int n = 0; n < holes.length; n++) {
			handPanels[n].setCards(holes[n]);
		}
		hiloBox.setSelected(hilo);
		updateDeck();
	}

	@Override
	public void hideOpp(boolean hide) {
		super.hideOpp(hide);
		for (int n = 1; n < handPanels.length; n++) {
			handPanels[n].setCardsHidden(hide);
		}
	}

	@Override
	public void random(int numhands) {
		for (HandCardPanel hp : handPanels) {
			if (randHandsBox.isSelected()) {
				hp.clearCards();
			}
			hp.setHandEquity(null);
		}
		if (randFlopBox.isSelected()) {
			boardPanel.clearCards(0, 3);
		}
		if (randFlopBox.isSelected() || randTurnBox.isSelected()) {
			boardPanel.clearCards(3, 4);
		}
		if (randFlopBox.isSelected() || randTurnBox.isSelected() || randRiverBox.isSelected()) {
			boardPanel.clearCards(4, 5);
		}
		
		// update deck, get remaining cards
		updateDeck();
		String[] deck = getDeck();
		RandomUtil.shuffle(deck);
		
		int i = 0;
		if (randHandsBox.isSelected()) {
			for (int n = 0; n < numhands; n++) {
				handPanels[n].setCards(Arrays.copyOfRange(deck, i, i + numHoleCards));
				i += numHoleCards;
			}
		}
		if (randFlopBox.isSelected()) {
			boardPanel.setCard(deck[i++], 0);
			boardPanel.setCard(deck[i++], 1);
			boardPanel.setCard(deck[i++], 2);
		}
		if (randTurnBox.isSelected()) {
			boardPanel.setCard(deck[i++], 3);
		}
		if (randRiverBox.isSelected()) {
			boardPanel.setCard(deck[i++], 4);
		}
		
		updateDeck();
		selectCard(5);
	}

	@Override
	public void calc() {
		List<String[]> hs = new ArrayList<String[]>();
		for (HandCardPanel hp : handPanels) {
			hp.setHandEquity(null);
		}
		
		String[] board = boardPanel.getCards();
		if (board.length == 1 || board.length == 2) {
			System.out.println("incomplete board");
			return;
		}
		if (board.length == 0) {
			board = null;
		}
		
		List<HandCardPanel> hps = new ArrayList<HandCardPanel>();
		for (HandCardPanel hp : handPanels) {
			String[] hand = hp.getCards();
			if (hand.length > 0) {
				if (hand.length < hp.getMinCards()) {
					System.out.println("incomplete hand");
					return;
					
				} else {
					hps.add(hp);
					hs.add(hand);
				}
			}
		}

		if (hs.size() == 0) {
			System.out.println("no hands");
			return;
		}

		String[][] hands = hs.toArray(new String[hs.size()][]);
		MEquity[] eqs = new HEPoker(omaha, hiloBox.isSelected()).equity(board, hands, null);
		for (int n = 0; n < eqs.length; n++) {
			//HandEq e = eqs[n];
			//pl.get(n).setHandEquity(e);
			hps.get(n).setHandEquity(eqs[n]);
		}

	}

	/**
	 * clear the deck, the board and the hand panels and select first hole card
	 */
	@Override
	public void clear() {
		super.clear();
		boardPanel.clearCards();
		for (HandCardPanel hp : handPanels) {
			hp.clearCards();
		}
		selectCard(5);
	}

}
