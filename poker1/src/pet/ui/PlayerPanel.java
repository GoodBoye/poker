package pet.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pet.hp.util.PlayerGameInfo;
import pet.hp.util.PlayerInfo;
import pet.ui.gr.GraphData;

/**
 * TODO send to bankroll button
 * update table model
 * add all time win/loss column
 */
public class PlayerPanel extends JPanel {
	// [name]
	// [table-name,games,hands,value]
	// [pinfo]
	private final JTextField nameField = new JTextField();
	private final JTable playersTable = new JTable();
	private final JTable gamesTable = new JTable();
	private final JTextArea gameTextArea = new JTextArea();
	private final JButton bankrollButton = new JButton("Bankroll");
	private final JButton sessionButton = new JButton("Session");
	
	public PlayerPanel() {
		super(new BorderLayout());
		
		nameField.setColumns(10);
		nameField.setBorder(BorderFactory.createTitledBorder("Player Name"));
		nameField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				find();
			}
		});
		
		playersTable.setAutoCreateRowSorter(true);
		playersTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		playersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int r = playersTable.getSelectionModel().getMinSelectionIndex();
					if (r >= 0) {
						int sr = playersTable.convertRowIndexToModel(r);
						PlayerInfo pi = ((PlayerTableModel)playersTable.getModel()).getRow(sr);
						System.out.println("selected " + r + " => " + sr + " => " + pi);
						gamesTable.setModel(new GameTableModel(pi.games));
						revalidate();
					}
				}
			}
		});
		
		gamesTable.setAutoCreateRowSorter(true);
		gamesTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		gamesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int r = gamesTable.getSelectionModel().getMinSelectionIndex();
					if (r >= 0) {
						int sr = gamesTable.convertRowIndexToModel(r);
						PlayerGameInfo gi = ((GameTableModel) gamesTable.getModel()).getRow(sr);
						System.out.println("selected " + r + " => " + sr + " => " + gi);
						gameTextArea.setText(gi.toLongString());
						revalidate();
					}
				}
			}
		});
		
		gameTextArea.setRows(5);
		gameTextArea.setLineWrap(true);
		
		bankrollButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO get list of hands from history
				int r = gamesTable.getSelectionModel().getMinSelectionIndex();
				if (r >= 0) {
					int sr = gamesTable.convertRowIndexToModel(r);
					PlayerGameInfo gi = ((GameTableModel) gamesTable.getModel()).getRow(sr);
					System.out.println("selected " + r + " => " + sr + " => " + gi);
					PokerFrame pf = PokerFrame.getInstance();
					GraphData bankRoll = pf.getHistory().getBankRoll(gi.player.name, gi.gameName);
					pf.displayBankRoll(bankRoll);
				}
			}
		});
		
		JScrollPane gamesTableScroller = new JScrollPane(gamesTable);
		gamesTableScroller.setBorder(BorderFactory.createTitledBorder("Player Games"));
		
		JScrollPane gameTextAreaScroller = new JScrollPane(gameTextArea);
		gameTextAreaScroller.setBorder(BorderFactory.createTitledBorder("Player Game Info"));
		
		JScrollPane playersTableScroller = new JScrollPane(playersTable);
		playersTableScroller.setBorder(BorderFactory.createTitledBorder("Players"));
		
		JPanel mainPanel = new JPanel(new GridLayout(3, 1));
		mainPanel.add(playersTableScroller);
		mainPanel.add(gamesTableScroller);
		mainPanel.add(gameTextAreaScroller);
		
		JPanel topPanel = new JPanel();
		topPanel.add(nameField);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(sessionButton);
		bottomPanel.add(bankrollButton);
				
		add(topPanel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
	}
	
	private void find() {
		String pattern = nameField.getText();
		PokerFrame pf = PokerFrame.getInstance();
		playersTable.setModel(new PlayerTableModel(pf.getHistory().getPlayers(pattern)));
	}
}


