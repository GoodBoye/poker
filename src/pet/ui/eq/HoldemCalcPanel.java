package pet.ui.eq;

import java.util.*;

import javax.swing.*;

import pet.eq.*;

/**
 * displays equity calculation panel specific to holdem/omaha
 */
public class HoldemCalcPanel extends CalcPanel {
	
	private final CardPanel boardPanel;
	private final HandCardPanel[] handPanels = new HandCardPanel[10];
	private final JCheckBox randHandsBox = new JCheckBox("Hole Cards");
	private final JCheckBox randFlopBox = new JCheckBox("Flop");
	private final JCheckBox randTurnBox = new JCheckBox("Turn");
	private final JCheckBox randRiverBox = new JCheckBox("River");
	private final JComboBox pokerCombo = new JComboBox();
	private final int numHoleCards;

	public HoldemCalcPanel(boolean omaha) {
		this.numHoleCards = omaha ? 4 : 2;
		
		// create board and hands and collect card labels
		boardPanel = new CardPanel("Community Cards", 0, 5);
		
		pokerCombo.setModel(new DefaultComboBoxModel(new PokerItem[] {
				new PokerItem(PokerItem.HIGH, new HEPoker(omaha, false)),
				new PokerItem(PokerItem.HILO, new HEPoker(omaha, true))
		}));
		
		String name = omaha ? "Omaha" : "Hold'em";
		int min = omaha ? 2 : 1;
		int max = omaha ? 4 : 2;
		for (int n = 0; n < handPanels.length; n++) {
			handPanels[n] = new HandCardPanel(name + " hand " + (n+1), min, max, false);
		}
		
		// add to layout
		setBoard(boardPanel);
		setHandCardPanels(handPanels);
		
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
			addCalcOpt(pokerCombo);
		}
	}
	
	/**
	 * display the given hand
	 */
	@Override
	public void displayHand(String[] board, List<String[]> holeCards, String type) {
		displayHand(board, holeCards);
		PokerItem.select(pokerCombo, type);
	}

	@Override
	public void hideOpp(boolean hide) {
		for (int n = 1; n < handPanels.length; n++) {
			handPanels[n].setCardsHidden(hide);
		}
	}

	@Override
	public void random(int numhands) {
		System.out.println("holdem panel random hand");
		
		// clear
		for (HandCardPanel hp : handPanels) {
			if (randHandsBox.isSelected()) {
				hp.clearCards();
			}
			hp.setEquity(null);
		}
		if (randFlopBox.isSelected()) {
			boardPanel.setCard(null, 0);
			boardPanel.setCard(null, 1);
			boardPanel.setCard(null, 2);
		}
		if (randFlopBox.isSelected() || randTurnBox.isSelected()) {
			boardPanel.setCard(null, 3);
		}
		if (randFlopBox.isSelected() || randTurnBox.isSelected() || randRiverBox.isSelected()) {
			boardPanel.setCard(null, 4);
		}
		
		// update deck, get remaining cards
		updateDeck();
		List<String> deck = getDeck();
		Collections.shuffle(deck);
		
		int i = 0;
		if (randHandsBox.isSelected()) {
			for (int n = 0; n < numhands; n++) {
				handPanels[n].setCards(deck.subList(i, i + numHoleCards));
				i += numHoleCards;
			}
		}
		
		if (randFlopBox.isSelected()) {
			boardPanel.setCard(deck.get(i++), 0);
			boardPanel.setCard(deck.get(i++), 1);
			boardPanel.setCard(deck.get(i++), 2);
		}
		if (randTurnBox.isSelected()) {
			boardPanel.setCard(deck.get(i++), 3);
		}
		if (randRiverBox.isSelected()) {
			boardPanel.setCard(deck.get(i++), 4);
		}
		
		updateDeck();
		
		selectCard(5);
	}

	@Override
	public void calc() {
		for (HandCardPanel hp : handPanels) {
			hp.setEquity(null);
		}
		
		List<String> board = boardPanel.getCards();
		if (board.size() == 1 || board.size() == 2) {
			System.out.println("incomplete board");
			return;
		}
		
		if (board.size() == 0) {
			board = null;
		}
		
		List<HandCardPanel> cardPanels = new ArrayList<HandCardPanel>();
		List<String[]> cards = new ArrayList<String[]>();
		collectCards(cards, cardPanels);
		
		if (cards.size() == 0) {
			System.out.println("no hands");
			return;
		}
		
		final List<String> blockers = getBlockers();
		final PokerItem pokerItem = (PokerItem) pokerCombo.getSelectedItem();
		final MEquity[] meqs = pokerItem.poker.equity(board, cards, blockers, 0);
		
		for (int n = 0; n < meqs.length; n++) {
			cardPanels.get(n).setEquity(meqs[n]);
		}

	}

}
