package pet.ui;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import pet.hp.*;
import pet.hp.info.*;
import pet.ui.ta.*;

public class GamesPanel extends JPanel implements HistoryListener {
	
	private final JComboBox gameCombo = new JComboBox();
	private final MyJTable gamesTable = new MyJTable();
	private final JTextArea textArea = new JTextArea();
	
	public GamesPanel() {
		super(new BorderLayout());
		
		gamesTable.setModel(new GameInfoTableModel(GameInfoTableModel.gameCols));
		gamesTable.setAutoCreateRowSorter(true);
		gamesTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		gamesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int r = gamesTable.getSelectionModel().getMinSelectionIndex();
					if (r >= 0) {
						int sr = gamesTable.convertRowIndexToModel(r);
						GameInfoTableModel gamesModel = (GameInfoTableModel) gamesTable.getModel();
						PlayerGameInfo gi = gamesModel.getRow(sr);
						System.out.println("selected " + r + " => " + sr + " => " + gi);
						textArea.setText(gi.toLongString());
						revalidate();
					}
				}
			}
		});
		
		gameCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					updateGame();
				}
			}
		});
		
		JPanel topPanel = new JPanel();
		topPanel.add(gameCombo);
		add(topPanel, BorderLayout.NORTH);
		
		JScrollPane tableScroller = new JScrollPane(gamesTable);
		tableScroller.setBorder(BorderFactory.createTitledBorder("Player Game Infos"));
		
		JScrollPane textAreaScroller = new JScrollPane(textArea);
		textAreaScroller.setBorder(BorderFactory.createTitledBorder("Selected Player Game Info"));
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroller, textAreaScroller);
		splitPane.setResizeWeight(0.5);
		add(splitPane, BorderLayout.CENTER);
	}
	
	private void updateGame() {
		System.out.println("update game");
		String selectedGameId = (String) gameCombo.getSelectedItem();
		Info info = PokerFrame.getInstance().getInfo();
		List<PlayerGameInfo> gameInfos = info.getGameInfos(selectedGameId);
		GameInfoTableModel gamesModel = (GameInfoTableModel) gamesTable.getModel();
		gamesModel.setRows(gameInfos);
		gamesModel.setPopulation(info.getPopulation());
		repaint();
	}

	@Override
	public void handAdded(Hand hand) {
		// could update table...
	}

	@Override
	public void gameAdded(Game game) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// update the game combo
				List<String> games = PokerFrame.getInstance().getHistory().getGames();
				gameCombo.setModel(new DefaultComboBoxModel(games.toArray(new String[games.size()])));
			}
		});
	}
	
}
