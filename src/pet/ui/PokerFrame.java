package pet.ui;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.*;

import pet.eq.DrawPoker;
import pet.eq.DrawPoker2;
import pet.eq.PokerUtil;
import pet.hp.*;
import pet.hp.impl.PSParser;
import pet.hp.info.*;
import pet.ui.eq.*;
import pet.ui.gr.GraphData;
import pet.ui.hud.HUDManager;
import pet.ui.rep.ReplayPanel;

/**
 * Poker equity GUI tool.
 */
public class PokerFrame extends JFrame {
	
	// left triangle 25c0, right triangle 25b6 
	public static final String LEFT_TRI = "\u25c0";
	public static final String RIGHT_TRI = "\u25b6";
	public static final Font boldfont = new Font("SansSerif", Font.BOLD, 12);
	public static final Font bigfont = new Font("SansSerif", Font.BOLD, 24);
	
	private static PokerFrame instance;
	
	public static PokerFrame getInstance() {
		return instance;
	}
	
	public static void main(String[] args) {
		// os x assumes US locale if system language is english...
		// user needs to add british english to list of languages in system preferences/language and text
		System.out.println("locale " + Locale.getDefault());
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		ToolTipManager.sharedInstance().setDismissDelay(5000);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// need to create and pack in awt thread otherwise it can deadlock
				// due to the java console panel
				instance = new PokerFrame();
				System.out.println("Poker Equity Tool - https://github.com/alexyz");
				instance.start();
				instance.setVisible(true);
			}
		});
		
	}
	
	/** all the parsed data */
	private final History history = new History();
	/** thread that feeds the parser */
	private final FollowThread followThread = new FollowThread(new PSParser(history));
	/** data analysis */
	private final Info info = new Info();
	private final JTabbedPane tabs = new JTabbedPane();
	private final JTabbedPane eqTabs = new JTabbedPane();
	private final JTabbedPane hisTabs = new JTabbedPane();
	private final ReplayPanel replayPanel = new ReplayPanel();
	private final BankrollPanel bankrollPanel = new BankrollPanel();
	private final LastHandPanel lastHandPanel = new LastHandPanel();
	private final HistoryPanel historyPanel = new HistoryPanel();
	private final HandsPanel handsPanel = new HandsPanel();
	private final HoldemCalcPanel holdemPanel = new HoldemCalcPanel(false);
	private final HoldemCalcPanel omahaPanel = new HoldemCalcPanel(true);
	private final DrawCalcPanel drawPanel = new DrawCalcPanel();
	private final GamesPanel gamesPanel = new GamesPanel();
	private final PlayerPanel playerPanel = new PlayerPanel();
	private final HUDManager hudManager = new HUDManager();
	
	public PokerFrame() {
		super("Poker Equity Tool");
		try {
			InputStream iconIs = getClass().getResourceAsStream("/pet32.png");
			BufferedImage icon = ImageIO.read(iconIs);
			iconIs.close();
		    setIconImage(icon);
		    //com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
		    //app.setDockIconImage (icon);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		tabs.addTab("Equity", eqTabs);
		tabs.addTab("History", hisTabs);
		
		eqTabs.addTab("Hold'em", holdemPanel);
		eqTabs.addTab("Omaha", omahaPanel);
		eqTabs.addTab("Draw", drawPanel);
		
		hisTabs.addTab("History", historyPanel);
		hisTabs.addTab("Players", playerPanel);
		hisTabs.addTab("Games", gamesPanel);
		hisTabs.addTab("Tournaments", new TournPanel());
		hisTabs.addTab("Graph", bankrollPanel);
		hisTabs.addTab("Hands", handsPanel);
		hisTabs.addTab("Replay", replayPanel);
		hisTabs.addTab("Last Hand", lastHandPanel);
		
		history.addListener(lastHandPanel);
		history.addListener(gamesPanel);
		history.addListener(info);
		history.addListener(hudManager);
		
		followThread.addListener(historyPanel);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(tabs);
		pack();
	}
	
	public void start() {
		followThread.start();
	}
	
	public Info getInfo() {
		return info;
	}
	
	public History getHistory() {
		return history;
	}
	
	/** display hand in replayer */
	public void replayHand(Hand hand) {
		replayPanel.setHand(hand);
		hisTabs.setSelectedComponent(replayPanel);
		tabs.setSelectedComponent(hisTabs);
	}
	
	/** display hand in hand panel */
	public void displayHand(Hand hand) {
		lastHandPanel.showHand(hand);
		hisTabs.setSelectedComponent(lastHandPanel);
		tabs.setSelectedComponent(hisTabs);
	}
	
	public void displayBankRoll(GraphData bankRoll) {
		bankrollPanel.setData(bankRoll);
		hisTabs.setSelectedComponent(bankrollPanel);
		tabs.setSelectedComponent(hisTabs);
	}
	
	public FollowThread getFollow() {
		return followThread;
	}
	
	/** display hands in hands tab */
	public void displayHands(String name, String gameid) {
		handsPanel.displayHands(name, gameid);
		hisTabs.setSelectedComponent(handsPanel);
		tabs.setSelectedComponent(hisTabs);
	}

	public void displayHands(long tournid) {
		handsPanel.displayHands(tournid);
		hisTabs.setSelectedComponent(handsPanel);
		tabs.setSelectedComponent(hisTabs);
	}
	
	public void displayHoldemEquity(String[] board, String[][] holes, boolean omaha, boolean hilo) {
		HoldemCalcPanel panel = omaha ? omahaPanel : holdemPanel;
		panel.displayHand(board, holes, hilo);
		eqTabs.setSelectedComponent(panel);
		tabs.setSelectedComponent(eqTabs);
	}
	
	public void displayDrawEquity(String[][] holes, String type) {
		drawPanel.displayHand(holes, type);
		eqTabs.setSelectedComponent(drawPanel);
		tabs.setSelectedComponent(eqTabs);
	}

	public void displayPlayer(String player) {
		playerPanel.displayPlayer(player);
		hisTabs.setSelectedComponent(playerPanel);
		tabs.setSelectedComponent(hisTabs);
	}

	public HUDManager getHudManager() {
		return hudManager;
	}
	

	public void f() {
		// get all draw hands with hole cards
		// compare predicted draw with actual draw
		for (String gid : history.getGames()) {
			if (gid.contains(GameUtil.getGameTypeName(Game.FCD_TYPE))) {
				List<Hand> hands = history.getHands("tawvx", gid);
				for (Hand hand : hands) {
					// get pre-draw and post draw hands
					String[] h1 = hand.myHoleCards0;
					String[] h2 = hand.myseat.holeCards;
					int d = hand.myseat.drawn0;
					if (h1 != null && h2 != null) {
						String[] pre = DrawPoker2.getDrawingHand(h1, d, true);
						for (String c1 : pre) {
							find: {
								for (String c2 : h2) {
									if (c1.equals(c2)) {
										break find;
									}
								}
								System.out.println();
								System.out.println("hand " + hand);
								System.out.println("hole " + Arrays.toString(h1) + " drawn " + d);
								System.out.println("predicted " + Arrays.toString(pre));
								System.out.println("actual " + Arrays.toString(h2));
								break;
							}
						}
					}
				}
			}
		}
		
	}
	

}
