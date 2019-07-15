import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

/**
 * The TopLevelWindow class handles representation of the GameOfLife class. The
 * application provides tools to change ticks, load and save grids, save desired
 * ranges, and calculates changes in living. This application also supports
 * color changes and output formatting.
 * 
 * @author chinm3
 *
 */
public class TopLevelWindow {
	static GameOfLife game;
	static JFrame frame;
	static JPanel board;
	protected static Cell[][] cells;
	static JPanel stats;
	static JLabel livetodead;
	static JLabel tick;
	static JLabel differences;
	static JPanel loadSave;
	protected static Color gridColor;
	static int threadCount;

	/**
	 * This constructs the main JFrame. Each component is initialized and added to
	 * the frame.
	 */
	public static void createWindow() {
		board = new JPanel();
		
		int rows = game.getRows();
		int cols = game.getCols();
		board.setLayout(new GridLayout(rows, cols));
		//board.setPreferredSize(new Dimension(400, 400));
		createBoard();

		stats = new JPanel();
		stats.setLayout(new BoxLayout(stats, BoxLayout.Y_AXIS));
		stats.setPreferredSize(new Dimension(100, 100));
		createStats();

		loadSave = new JPanel();
		loadSave.setPreferredSize(new Dimension(100, 100));
		createLoadSave();
		frame = new JFrame("Conway's Game of Life");
		frame.setExtendedState( frame.getExtendedState()|JFrame.MAXIMIZED_BOTH );
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel direct = createDirection();
		createMenu();
		frame.getContentPane().add(loadSave, BorderLayout.LINE_START);
		frame.getContentPane().add(board, BorderLayout.CENTER);
		frame.getContentPane().add(direct, BorderLayout.NORTH);
		frame.getContentPane().add(stats, BorderLayout.EAST);

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			public void run() {
				game.saveOnExit();
			}
		}));
		frame.validate();
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * This handles the Tools menu. It creates JMenuItems and adds them to a
	 * JMenuBar. The bar is then set to the JFrame.
	 */
	public static void createMenu() {
		JMenuBar menuBar;
		JMenu menu;
		JMenuItem menuItem;
		menuBar = new JMenuBar();

		// Build the first menu.
		menu = new JMenu("Tools");
		menu.setMnemonic(KeyEvent.VK_A);
		// menu.getAccessibleContext().setAccessibleDescription(
		// "The only menu in this program that has menu items");
		menuBar.add(menu);

		// a group of JMenuItems
		menuItem = new JMenuItem("Help", KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame,
						"<html>USAGE<br>---------------------------------------------------------------------------<br>"
								+ "Load a grid using the 'LOAD GRID' button on the GUI.  The grid must be in Kuzmin's specified format. <br>Hit 'NEXT' to advance the game a tick, and 'PREV' to go back a tick.  <br>Save the current tick using 'SAVE GRID'.  Save a range of ticks up to the current using Alt+2 (Tools->Save range).  <br>Set output pattern will save any new grid outputs named in the format 'inputstring'.txt</html>");
			}
		});

		menu.add(menuItem);

		menuItem = new JMenuItem("Set output pattern", null);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String out = (String) (JOptionPane.showInputDialog(frame, "Enter ouput pattern:",
							"Customized Dialog", JOptionPane.PLAIN_MESSAGE, null, null, "0"));
					game.setOutput(out);
				} catch (Exception q) {
					q.printStackTrace();
				}
			}
		});
		menu.add(menuItem);

		

		
		menuItem = new JMenuItem("Generate large grid", null);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String newRows = (String) (JOptionPane.showInputDialog(frame, "Enter rows:", "Customized Dialog",
							JOptionPane.PLAIN_MESSAGE, null, null, "0"));
					String newCols = (String) (JOptionPane.showInputDialog(frame, "Enter columns:", "Customized Dialog",
							JOptionPane.PLAIN_MESSAGE, null, null, "0"));
					if(newRows.matches("\\d+") && newCols.matches("\\d+")) {
						game.changeGridSize(Integer.parseInt(newRows), Integer.parseInt(newCols));
						frame.dispose();
						createWindow();
					}else {
						JOptionPane.showMessageDialog(frame, "Invalid dimension entered!");
						return;
					}
				} catch (Exception q) {
					q.printStackTrace();
				}
			}
		});
		menu.add(menuItem);
		
		
		menuItem = new JMenuItem("Set thread count", null);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.ALT_MASK));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Integer[] possibilities = new Integer[5];
				possibilities[0] = 1;
				possibilities[1] = 2;
				possibilities[2] = 4;
				possibilities[3] = 8;
				possibilities[4] = 16;
				
				
				try {
					int threads = (int) (JOptionPane.showInputDialog(frame, "Enter number of threads:", "Customized Dialog",
							JOptionPane.PLAIN_MESSAGE, null, possibilities, "1"));
					game.setThreads(threads);
					threadCount = threads;
				} catch (Exception q) {
					q.printStackTrace();
				}
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Save range", null);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.ALT_MASK));
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Integer[] possibilities = new Integer[game.getTick()];
				for (int i = 0; i < game.getTick(); i++) {
					possibilities[i] = new Integer(i);
				}
				try {
					int start = (int) (JOptionPane.showInputDialog(frame, "Enter start of range:", "Customized Dialog",
							JOptionPane.PLAIN_MESSAGE, null, possibilities, "0"));

					possibilities = new Integer[game.getTick() - start + 2];
					for (int i = start + 1; i <= game.getTick(); i++) {
						possibilities[i] = new Integer(i);
					}
					int end = (int) JOptionPane.showInputDialog(frame, "Enter end of range:", "Customized Dialog",
							JOptionPane.PLAIN_MESSAGE, null, possibilities, game.getTick());
					game.runSimulation(start, end);
				} catch (Exception q) {
					q.printStackTrace();
				}
			}
		});
		menu.add(menuItem);

		frame.setJMenuBar(menuBar);
	}

	/**
	 * Handles stat initialization. JLabels are created and added to the JPanel
	 */
	public static void createStats() {
		int live = game.getLive();
		int dead = game.getRows() * game.getCols() - live;
		livetodead = new JLabel("<html>LIVE: " + live + "<br>DEAD: " + dead + "<br>"
				+ Character.toString((char) '\u0394') + " LIVE: " + 0 + "<br>" + Character.toString((char) '\u0394')
				+ "% LIVE:<br>" + (double) Math.round(0 * 1000d) / 1000d);
		tick = new JLabel("Tick: " + game.getTick());

		stats.add(livetodead);
		stats.add(tick);
	}

	/**
	 * Handles LIVE, DEAD, change in LIVE, and change in LIVE percentage. Sets
	 * JLabels text to display stats.
	 */
	public static void updateStats() {
		String prevStat = livetodead.getText();
		Matcher matcher = Pattern.compile("\\d+").matcher(prevStat);
		matcher.find();
		int prevLive = Integer.valueOf(matcher.group());
		int total = game.getRows() * game.getCols();
		double prevLivePercentage = (double) prevLive / total;
		int live = game.getLive();

		double currLivePercentage = (double) live / total;
		double diff = currLivePercentage - prevLivePercentage;
		int dead = total - live;

		int liveDiff = live - prevLive;

		livetodead.setText("<html>LIVE: " + live + "<br>DEAD: " + dead + "<br>" + Character.toString((char) '\u0394')
				+ " LIVE: " + liveDiff + "<br>" + Character.toString((char) '\u0394') + "% LIVE:<br>"
				+ (double) Math.round(diff * 1000d) / 1000d);
		tick.setText("Tick: " + game.getTick());
	}

	/**
	 * This handles creation of the NEXT, PREV and CHANGE COLOR buttons. These are
	 * JButtons set with action listeners.
	 * 
	 * @return JPanel containing the buttons NEXT, PREV, CHANGE COLOR
	 */
	public static JPanel createDirection() {
		JPanel direction = new JPanel();
		JButton next = new JButton();
		JButton prev = new JButton();
		prev.setBackground(Color.red);
		next.setBackground(Color.green);
		next.setText("NEXT");
		prev.setText("PREV");
		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				game.advanceTick();
				//updateBoard();
				updateStats();

			}
		});
		prev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				game.previousTick();
				updateBoard();
				updateStats();
			}
		});

		JButton color = new JButton();
		color.setText("CHANGE COLOR");
		color.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeColor();
			}
		});

		direction.add(prev);
		direction.add(next);
		direction.add(color);
		direction.setPreferredSize(new Dimension(100, 100));
		return direction;
	}

	/**
	 * Handles initialization of LOAD and SAVE GRID functions.
	 * 
	 * @return JPanel component with load and save grid functions.
	 */
	public static JPanel createLoadSave() {

		JButton load = new JButton();
		load.setText("LOAD GRID");
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadFile();
			}
		});
		JButton save = new JButton();
		save.setText("SAVE GRID");
		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (game.getTick() == 0) {
					return;
				}
				saveGrid();
			}
		});

		loadSave.add(load);
		loadSave.add(save);
		return loadSave;
	}

	/**
	 * Save grid used to output a grid at the current tick.
	 */
	public static void saveGrid() {
		game.outputTickFile(game.getTick());
	}

	/**
	 * Updates board according to values in the GameOfLife instance.
	 */
	public static void updateBoard() {
		int i, j;
		for (i = 0; i < game.getRows(); i++) {
			for (j = 0; j < game.getCols(); j++) {
				if (game.getValueAt(i, j) == 1) {
					cells[i][j].setLive(gridColor);
				} else {
					cells[i][j].setDead();
				}
			}
		}
		//frame.revalidate();
		frame.repaint();
	}

	/**
	 * Handles creation of grid components. Initializes JButtons and colors then
	 * adds to the JPanel for the board.
	 */
	public static void createBoard() {
		gridColor = Color.black;
		int rows = game.getRows();
		int cols = game.getCols();
		cells = new Cell[rows][cols];
		int i, j;
		for (i = 0; i < rows; i++) {
			for (j = 0; j < cols; j++) {
				cells[i][j] = new Cell(Color.white);
				if (game.getValueAt(i, j) == 1) {
					cells[i][j].setLive(gridColor);
				} else {
					cells[i][j].setDead();
				}
				board.add(cells[i][j]);
			}
		}
	}

	/**
	 * Receives input of a JOptionPane and changes board color.
	 */
	public static void changeColor() {
		Object[] possibilities = { "Black", "Red", "Green", "Blue" };
		String s = (String) JOptionPane.showInputDialog(frame, "Select color", "Customized Dialog",
				JOptionPane.PLAIN_MESSAGE, null, possibilities, "Black");
		if (s.equals("Black")) {
			gridColor = Color.black;
			updateBoard();
		} else if (s.equals("Red")) {
			gridColor = Color.red;
			updateBoard();
		} else if (s.equals("Green")) {
			gridColor = Color.green;
			updateBoard();
		} else if (s.equals("Blue")) {
			gridColor = Color.blue;
			updateBoard();
		} else {
			return;
		}
	}

	/**
	 * Subroutine for LOAD GRID button. Allows user to input file path via
	 * JFileChooser.
	 */
	public static void loadFile() {
		JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		File curr = new File(System.getProperty("user.dir"));
		// jfc.setCurrentDirectory(curr);
		int returnValue = jfc.showOpenDialog(null);
		String path = "";
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = jfc.getSelectedFile();
			path = selectedFile.getAbsolutePath();
		}
		try {
			game.loadConfig(path);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(frame, "Invalid file format!", "Error", JOptionPane.WARNING_MESSAGE);
		}
		frame.dispose();
		createWindow();

	}

	public static void main(String[] args) {
		game = new GameOfLife();
		File file = new File("cfg.txt");
		if (!file.exists()) {
			System.out.println(
					"No cfg.txt found. Loading default 'glider' pattern. One will be created at the end of this session");
			game.loadConfig("glider");
		}else {
			try {
				game.loadConfig("cfg.txt");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		createWindow();

	}

}
