import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.lang.management.*;

public class Calibrate {
	private static final String OUTPUTFILENAME = "calibration.txt";
	private static int n;
	private static int count = 0;

	public static void main(String[] args) {
		try {
			String[] grid1 = new String[] { "0123456789", "9876543210", "0123456789", "9876543210", "0123456789",
					"9876543210", "0123456789", "9876543210", "0123456789", "9876543210" };
			char[][] inGrid = new char[10][10];
			for (int i = 0; i < 10; i++) {
				inGrid[i] = grid1[i].toCharArray();
			}
			double startUserTime = getUserTime();
			n = 10;
			state initState = new state(inGrid, new position(-1, -1), 0, 0, true);
			state r = minimax(0, initState, 5, Integer.MIN_VALUE, Integer.MAX_VALUE);
			Double now = (getUserTime() - startUserTime) / Math.pow(10, 9);
			writeOutput(now / count);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static state minimax(int depth, state s, int maxDepth, int alpha, int beta) {
		Map<Integer, List<position>> conComp = new HashMap<Integer, List<position>>();
		TreeMap<Integer, List<Integer>> moveTree;
		moveTree = new TreeMap<Integer, List<Integer>>(Collections.reverseOrder());
		findAllMoves(s.board, conComp, moveTree);
		state result = null;
		if (conComp.isEmpty() || depth == maxDepth) {
			result = new state(s);
			result.score = evaluate(s);
			return result;
		} else {
			Iterator<Integer> i = moveTree.keySet().iterator();
			while (i.hasNext()) {
				List<Integer> moves = moveTree.get(i.next());
				for (int move : moves) {
					state newState = applyMove(s, conComp.get(move));
					count++;
					if (s.myTurn) {
						state resState = minimax(depth + 1, newState, maxDepth, alpha, beta);
						if (s.score < resState.score) {
							result = new state(newState);
							result.score = resState.score;
							result.myScore = resState.myScore;
							result.oppScore = resState.oppScore;
							s.score = result.score;
							if (result.score >= beta)
								return result;
							alpha = Math.max(alpha, s.score);
						}
					} else {
						state resState = minimax(depth + 1, newState, maxDepth, alpha, beta);
						if (s.score > resState.score) {
							result = new state(newState);
							result.score = resState.score;
							result.myScore = resState.myScore;
							result.oppScore = resState.oppScore;
							s.score = result.score;
							if (result.score <= alpha)
								return result;
							beta = Math.min(beta, s.score);
						}
					}
				}
			}
			return result;
		}
	}

	private static state applyMove(state s, List<position> move) {
		boolean turn = s.myTurn == true ? false : true;
		state newState = new state(null, move.get(0), 0, 0, turn);
		if (s.myTurn) {
			newState.myScore = s.myScore + (move.size() * move.size());
			newState.oppScore = s.oppScore;
		} else {
			newState.myScore = s.myScore;
			newState.oppScore = s.oppScore + (move.size() * move.size());
		}
		newState.board = rearrangeboard(move, s.board);
		return newState;
	}

	private static int evaluate(state s) {
		return s.myScore - s.oppScore;
	}

	private static void findAllMoves(char[][] board, Map<Integer, List<position>> conComp,
			TreeMap<Integer, List<Integer>> moveTree) {
		boolean visited[][] = new boolean[n][n];
		boolean empty[][] = new boolean[n][n];
		int count = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (board[i][j] == '*') {
					empty[i][j] = true;
				} else if (!visited[i][j]) {
					conComp.put(count, new ArrayList<>());
					conComp.get(count).add(new position(i, j));
					DFS(visited, empty, i, j, board, conComp);
					int compSize = conComp.get(count).size();
					if (!moveTree.containsKey(compSize)) {
						moveTree.put(compSize, new ArrayList<>());
					}
					moveTree.get(compSize).add(count);
					count++;
				}
			}
		}
	}

	private static char[][] rearrangeboard(List<position> move, char[][] board) {
		char[][] outGrid = new char[n][n];
		int emptySpaces = 0;
		for (int j = 0; j < n; j++) {
			for (int i = 0; i < n; i++) {
				if (move.contains(new position(i, j))) {
					emptySpaces++;
					if (i == emptySpaces - 1) {
						outGrid[i][j] = '*';
					} else {
						for (int k = i; k >= emptySpaces; k--) {
							outGrid[k][j] = outGrid[k - 1][j];
						}
						outGrid[emptySpaces - 1][j] = '*';
					}
				} else {
					outGrid[i][j] = board[i][j];
				}
			}
			emptySpaces = 0;
		}
		return outGrid;
	}

	public static void writeOutput(Double t) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUTFILENAME))) {
			bw.write(t.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void DFS(boolean visited[][], boolean empty[][], int i, int j, char[][] board,
			Map<Integer, List<position>> conComp) {
		int adjRows[] = new int[] { 0, -1, 1, 0 };
		int adjCols[] = new int[] { -1, 0, 0, 1 };
		int size = conComp.size() - 1;
		char cur = board[i][j];
		visited[i][j] = true;
		for (int k = 0; k < 4; k++) {
			int row = i + adjRows[k];
			int col = j + adjCols[k];
			if (row >= 0 && row < n && col >= 0 && col < n) {
				if (!visited[row][col] && board[row][col] == cur) {
					conComp.get(size).add(new position(row, col));
					DFS(visited, empty, row, col, board, conComp);
				} else if (board[row][col] == '*') {
					empty[row][col] = true;
				}
			}
		}
	}

	private static double getUserTime() {
		ThreadMXBean b = ManagementFactory.getThreadMXBean();
		return b.isThreadCpuTimeSupported() ? b.getCurrentThreadUserTime() : 0L;
	}
}

class state {
	public char[][] board;
	public int myScore;
	public int oppScore;
	public position move;
	public boolean myTurn;
	public int score;

	public state(char[][] grid, position p, int my, int opp, Boolean turn) {
		this.board = grid;
		this.move = p;
		this.myScore = my;
		this.oppScore = opp;
		this.myTurn = turn;
		this.score = this.myTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;
	}

	state(state s) {
		this.board = s.board;
		this.move = s.move;
		this.myScore = s.myScore;
		this.oppScore = s.oppScore;
		this.myTurn = s.myTurn;
	}
}

class position {
	public int row;
	public int col;

	public position(int i, int j) {
		this.row = i;
		this.col = j;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof position))
			return false;
		position pos = (position) o;
		return ((this.row == pos.row) && (this.col == pos.col));
	}
}

