package pet.ui.eq;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.*;

import pet.eq.*;

/**
 * calc panel for draw hands
 */
public class DrawCalcPanel extends CalcPanel {
	
	private final JComboBox pokerCombo = new JComboBox();
	private final JSpinner drawsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 3, 1));
	
	private final HandCardPanel[] handPanels = new HandCardPanel[6];
	
	public DrawCalcPanel() {
		for (int n = 0; n < handPanels.length; n++) {
			handPanels[n] = new HandCardPanel("Draw hand " + (n + 1), 1, 5, false);
		}
		
		setCardPanels(handPanels);
		
		initCardLabels();
		
		PokerItem[] items = new PokerItem[] {
				new PokerItem(PokerItem.HIGH, new DrawPoker(true)),
				new PokerItem(PokerItem.DSLOW, new DrawPoker(false)),
		};
		
		pokerCombo.setModel(new DefaultComboBoxModel(items));
		addCalcOpt(pokerCombo);
		addCalcOpt(new JLabel("Draws"));
		addCalcOpt(drawsSpinner);
		
		selectCard(0);
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
	protected void hideOpp(boolean hide) {
		for (int n = 1; n < handPanels.length; n++) {
			handPanels[n].setCardsHidden(hide);
		}
	}
	
	@Override
	protected void calc() {
		List<String[]> hands = HandCardPanel.getCards(handPanels);
		if (hands != null) {
			PokerItem item = (PokerItem) pokerCombo.getSelectedItem();
			List<String> blockers = getBlockers();
			int draws = ((SpinnerNumberModel) drawsSpinner.getModel()).getNumber().intValue();
			MEquity[] meqs = item.poker.equity(null, hands, blockers, draws);
			for (int n = 0; n < meqs.length; n++) {
				handPanels[n].setEquity(meqs[n]);
			}
		} else {
			System.out.println("no hands");
		}
	}
	
	@Override
	protected void random(int num) {
		String[] deck = Poker.deck();
		ArrayUtil.shuffle(deck, new Random());
		for (int n = 0; n < num; n++) {
			handPanels[n].setCards(Arrays.asList(Arrays.copyOfRange(deck, n * 5, n * 5 + 5)));
		}
		updateDeck();
	}
	
}
