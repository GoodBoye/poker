package pet.ui.eq;

import java.util.Arrays;

import pet.*;

public class DrawCalcPanel extends CalcPanel {
	private final HandCardPanel[] handPanels = new HandCardPanel[4];
	public DrawCalcPanel() {
		for (int n = 0; n < handPanels.length; n++) {
			handPanels[n] = new DrawHandPanel(cardLabels, n + 1);
		}
		initCardLabels();
		for (HandCardPanel hp : handPanels) {
			addgb(hp);
		}

		addgb(new ButtonPanel(this));
	}
	@Override
	protected void calc() {
		// XXX could be 0
		String[][] hands = HandCardPanel.getCards(handPanels);
		if (hands != null) {
			HandEq[] v = new DPoker().deqs(hands);
			for (int n = 0; n < v.length; n++) {
				handPanels[n].setHandEquity(v[n]);
			}
		} else {
			System.out.println("no hands");
		}

	}
	@Override
	protected void random(int num) {
		String[] deck = Poker.FULL_DECK.clone();
		Util.shuffle(deck);
		for (int n = 0; n < num; n++) {
			handPanels[n].setCards(Arrays.copyOfRange(deck, n * 5, n * 5 + 5));
		}
		deckPanel.selectCards(Arrays.copyOfRange(deck, 0, num * 5 + 5));
	}
}