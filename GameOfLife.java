import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 * The GameOfLife class handles all properties
 * of Conway's Game of Life simulation and provides
 * methods to output information to create a GUI.
 * @author chinm3
 *
 */
public class GameOfLife {
	
	private int[][] currentGrid;
	private int[][] nextGrid;
	private int rows;
	private int cols;
	private String outputPattern;
	private int currentTick = 0;
	private int threadCount = 1;
	private List<int[][]> grids;
	/**
	 * Constructor for an instance of GameOfLife
	 * @param initState 2D array containing the initial seed
	 * @param ticks Number of iterations in simulation
	 * @param output Output pattern of each tick
	 */
	public GameOfLife() {	
		grids = new ArrayList<int[][]>();
		currentGrid = new int[10][10];
		for(int i = 0; i < 10; i++) {
			for(int j = 0; j < 10; j++) {
				currentGrid[i][j] = 0;
			}
		}
		rows = 10;
		cols = 10;
		outputPattern = "defaultPattern";
	}
	
	/**Return current tick of game board.
	 * @return int Current tick
	 */
	public int getTick() {
		return currentTick;
	}
	
	/**
	 * Set the output pattern to string s
	 * @param s output pattern to be set
	 */
	public void setOutput(String s) {
		outputPattern = s;
	}
	
	public void setThreads(int threads) {
		threadCount = threads;
	}
	/**
	 * Reverts board to previous tick.  This copies the previous tick board to the current game board.
	 */
	public void previousTick() {
		if(currentTick == 0) {
			return;
		}
		int[][] prev = grids.get(grids.size()-1);
		for(int i = 0; i < rows; i++) {
			currentGrid[i] = prev[i].clone();
		}
		
		grids.remove(grids.size()-1);
		//System.out.println(grids);
		currentTick--;
		
	}
	public void changeGridSize(int rows, int cols) {
		int[][] generateGrid = new int[rows][cols];
		Random rand = new Random();
		for(int i=  0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				int n = rand.nextInt(100);
				if(n > 49) {
					generateGrid[i][j] = 1;
				}else {
					generateGrid[i][j] = 0;
				}
			}
		}
		currentGrid = generateGrid;
		this.rows = rows;
		this.cols = cols;
		currentTick = 0;
		grids.clear();
		grids.add(currentGrid);
	}
	/**
	 * Advances board a tick.  Next grid is calculated and then stored in currentGrid.
	 */
	public void advanceTick() {
//		if(currentTick >= 0) {
//			int[][] currCopy = new int[rows][cols];
//			for(int i = 0; i < rows; i++) {
//				currCopy[i] = currentGrid[i].clone();
//			}
//			grids.add(currCopy);
//		}
		nextGrid = new int[rows][cols];
		int x = (int) Math.sqrt(threadCount);
		int threadWidth = cols;
		int threadHeight = rows;
		if(Math.pow(x,2) == threadCount) {
			threadWidth = cols/x;
			threadHeight = rows/x;
		}else {
			threadWidth = cols/2;
			int temp = threadCount/2;
			threadHeight = rows/temp;
		}
		//write for loop to properly section each part
		ArrayList<BoardThread> threads = new ArrayList<BoardThread>();
		for(int i = 0; i < rows; i+=threadHeight) {
			for(int j = 0; j < cols; j+= threadWidth) {
				System.out.println("Section: (" + i + " , " + j + ") TO ("+(i+threadHeight)+" , "+(j+threadWidth)+ ")");
				BoardThread t = new BoardThread(this, this.currentGrid, this.nextGrid,i,j,i+threadHeight, j+threadWidth);
				t.start();
				threads.add(t);
				
			}
		}
		for(BoardThread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(int i = 0; i < nextGrid.length; i++) {
			currentGrid[i] = nextGrid[i].clone();
		}
		currentTick++;
	}
	
	/**
	 * @param row Row index
	 * @param col Column index
	 * @return int State of cell at row,col in previous tick. 
	 */
//	public int getPreviousState(int row, int col) {
//		return(grids.get(currentTick)[row][col]);
//	}
//	
	/**
	 * Handles loading a file's path.
	 * @param inputFile path to file to be loaded
	 */
	public void loadConfig(String inputFile) {
		BufferedReader reader;
		int[][] initialState = null;
		int i = 0;
		int j = 0;
		try {
			reader = new BufferedReader(new FileReader(inputFile));
			String line = reader.readLine();
			String[] dimensions = line.split(", ");
			int rows = Integer.parseInt(dimensions[0]);
			int cols = Integer.parseInt(dimensions[1]);
			if(rows < 3 || cols < 3) {
				printError("Invalid dimensions");
			}
			initialState = new int[rows][cols];
			line = reader.readLine();
			while(line != null) {
				String[] currRow = line.split(",");
				for(String x : currRow) {
					if(x.isEmpty()) {
						printError("Invalid grid input");
					}
					initialState[i][j] = Integer.parseInt(x);
					if(initialState[i][j] != 0 && initialState[i][j] != 1) {
						printError("Invalid grid input");
					}
					j++;
				}
				i++;
				if(j != cols) {
					printError("Invalid grid input");
				}
				j=0;
				line = reader.readLine();
			}
			if(i != rows) {
				printError("Invalid grid input");
			}
			currentGrid = initialState;
			this.rows = initialState.length;
			this.cols = initialState[0].length;
			currentTick = 0;
			grids.clear();
			grids.add(currentGrid);
		}catch(IOException e) {
			System.out.println("Invalid filename");
			
		}
	}
	
	/**
	 * Grabs value at row,col in current grid.
	 * @param row row of value
	 * @param col column of value
	 * @return int value at row,col in the current grid
	 */
	public int getValueAt(int row, int col) {
		return currentGrid[row][col];
	}
	/**
	 * This method outputs the next tick of the simulation to a file named by the output pattern with the tick
	 * number appended to it.
	 * @param tick Current tick to append to output pattern file name.
	 */
	public void outputTickFile(int tick) {
		String fileName = outputPattern + Integer.toString(tick)+ ".txt";
		try {
			System.out.println(grids+ " :: " + tick);
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			for(int[] row : currentGrid) {
				for(int col : row) {
					writer.print(col);
				}
				writer.println();
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * This method runs the simulation and outputs each tick to a file named by the designated output pattern.
	 */
	public void runSimulation(int start, int end) {
		currentGrid = grids.get(0);
		nextGrid = new int[rows][cols];
		while(this.currentTick > start) {
			previousTick();
		}
		while(currentTick <= end) {
			outputTickFile(currentTick);
			advanceTick();
		}

	}
	
	/**
	 * Get number of rows
	 * @return Rows in current board
	 */
	public int getRows() {
		return rows;
	}
	
	/**
	 * Get number of columns
	 * @return Columns in current board
	 */
	public int getCols() {
		return cols;
	}
	
	/**
	 * Get living cells on board
	 * @return Living cell count in current board
	 */
	public int getLive() {
		int sum = 0; 
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				if(currentGrid[i][j] == 1) sum++;
			}
		}
		return sum;
	}

	/**
	 * This method counts the number of live neighbors around an index, supporting wrapping around edges
	 * so that at each index, there will always be 8 neighbors.
	 * @param row Row index of grid
	 * @param col Column index of grid
	 * @return int Number of live neighbors around grid[row][col]
	 */
	public int getLiveNeighborCount(int row, int col) {
		int liveNeighbors = 0;
		int leftBound = col-1;
		int rightBound = col+1;
		int upperBound = row-1;
		int lowerBound = row+1;
		if(leftBound < 0) {
			leftBound += cols;
		}
		if(rightBound > cols-1) {
			rightBound -= cols;
		}
		if(upperBound < 0) {
			upperBound += rows;
		}
		if(lowerBound > rows-1) {
			lowerBound -= rows;
		}
		if(currentGrid[row][leftBound] == 1) liveNeighbors++;
		if(currentGrid[row][rightBound] == 1) liveNeighbors++;
		if(currentGrid[upperBound][col] == 1) liveNeighbors++;
		if(currentGrid[lowerBound][col] == 1) liveNeighbors++;
		if(currentGrid[upperBound][leftBound] == 1) liveNeighbors++;
		if(currentGrid[upperBound][rightBound] == 1) liveNeighbors++;
		if(currentGrid[lowerBound][leftBound] == 1) liveNeighbors++;
		if(currentGrid[lowerBound][rightBound] == 1) liveNeighbors++;
		return liveNeighbors;
	}
	
	/**
	 * This method takes an index in the grid and returns the next state based on how many live neighbors are 
	 * surrounding the index.
	 * @param row Row index in grid
	 * @param col Column index in grid
	 * @return int The next state of the current grid index at [i][j], following Conway's rules.
	 */
	public int getNextState(int row, int col) {
		int neighbors = getLiveNeighborCount(row, col);
		if(currentGrid[row][col] == 1 && neighbors < 2) return 0;
		if(currentGrid[row][col] == 1 && neighbors < 4) return 1;
		if(currentGrid[row][col] == 1 && neighbors > 3) return 0;
		if(currentGrid[row][col] == 0 && neighbors == 3) return 1;
		return 0;
	}
	/**
	 * This method prints the current state of the board to the terminal.
	 */
	public void printBoard() {
		for(int[] row : currentGrid) {
			for(int col : row) {
				System.out.print(col);
			}
			System.out.println();
		}
	}
	
	/**
	 * Saves a cfg.txt file on exit with load path and output pattern. 
	 */
	public void saveOnExit() {
		String fileName = "cfg.txt";
		try {
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			int[][] base = grids.get(0);
			writer.println(rows + ", "+cols);
			for(int i=0; i < base.length; i++) {
				for(int j = 0; j < base[i].length;j++) {
					writer.print(base[i][j]);
					if(j < base[i].length) {
						writer.print(",");
					}
				}
				writer.println();
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public void printError(String s) {
		System.out.println("ERROR occurred: " + s);
		System.exit(1);
	}
}
