package pet.ui.eq;

import java.awt.*;

import javax.swing.*;

import pet.eq.*;

/** show high hand ranks */
public class RanksPanel extends JPanel {
	
	private final Font font = UIManager.getFont("Label.font");
	private final Font fontbold = font.deriveFont(Font.BOLD);
	
	private final JLabel[] rankLabs;
	
	public RanksPanel() {
		setLayout(new GridLayout(1, Poker.RANKS));
		
		// high value rank labels
		rankLabs = new JLabel[Poker.RANKS];
		for (int n = 0; n < rankLabs.length; n++) {
			JLabel l = new JLabel();
			l.setVerticalAlignment(SwingConstants.CENTER);
			//l.setPreferredSize(new Dimension(boldfont.getSize() * 4, boldfont.getSize() + 4));
			//l.setMinimumSize(l.getPreferredSize());
			rankLabs[n] = l;
			add(rankLabs[n]);
		}
		
	}
	
	public void clearHandEquity() {
		for (JLabel rl : rankLabs) {
			rl.setFont(font);
			rl.setText("");
		}
	}
	
	/** populate the rank names and win percentages */
	public void setHandEquity(Equity e) {
		String[] names = Equity.getRankNames(e.eqtype);
		for (int n = 0; n < rankLabs.length; n++) {
			JLabel rl = rankLabs[n];
			if (names != null && names.length > n) {
				rl.setForeground(e.wonrank[n] > 0 ? Color.black : Color.darkGray);
				rl.setFont(e.wonrank[n] > 0 ? fontbold : font);
				rl.setText(String.format("%s: %.0f", names[n], e.wonrank[n]));
			} else {
				rl.setText("");
			}
		}
	}
	
}
