
package pet.ui.eq;

import pet.eq.*;

/**
 * calc panel for draw hands
 */
public class BadugiCalcPanel extends CalcPanel {
	
	public BadugiCalcPanel() {
		super(true);
		HandCardPanel[] handPanels = new HandCardPanel[6];
		for (int n = 0; n < handPanels.length; n++) {
			handPanels[n] = new HandCardPanel("Badugi hand " + (n + 1), 1, 4, false);
		}
		setHandCardPanels(handPanels);
		
		initCardLabels();
		
		PokerItem[] items = new PokerItem[] {
			new PokerItem(PokerItem.BADUGI, new DrawPoker(Value.badugiValue))
		};
		setPokerItems(items);
		
		selectCard(0);
	}
	
}
