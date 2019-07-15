
public class BoardThread extends Thread{
	private int upperLeftX;
	private int upperLeftY;
	private int lowerRightX;
	private int lowerRightY;
	private GameOfLife game;
	private int[][] nextBoard;
	private int[][] currentBoard;
	public BoardThread(GameOfLife game,int[][] currentBoard, int[][] nextBoard, int upperLeftX, int upperLeftY, int lowerRightX, int lowerRightY) {
		this.currentBoard = currentBoard;
		this.upperLeftX = upperLeftX;
		this.upperLeftY = upperLeftY;
		this.lowerRightX = lowerRightX;
		this.lowerRightY = lowerRightY;
		
		this.nextBoard = nextBoard;
		this.game = game;
	}
	public void processSection() {
		System.out.println("THREAD " + this.getName() + ", PROCESSING ("+upperLeftX+","+upperLeftY+")"+"TO ("+lowerRightX+","+lowerRightY+")");
		for(int i = upperLeftX; i < lowerRightX; i++) {
			for(int j = upperLeftY; j < lowerRightY; j++) {
				int nextState = game.getNextState(i, j);
				nextBoard[i][j] = nextState;
//				if(nextState == 1) {
//					TopLevelWindow.cells[i][j].setLive(TopLevelWindow.gridColor);
//				}else {
//					TopLevelWindow.cells[i][j].setDead();
//				}
			}
		}
	}
	@Override
	public void run() {
		processSection();
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
