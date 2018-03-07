import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FruitGame {
	private static final String INPUTFILENAME = "input.txt";
	private static final String OUTPUTFILENAME = "output.txt";
	private static final String CALIBRATE = "calibration.txt";
	private static int n;
	private static double perNodeTime;

	public static void main(String[] args) {
		try (BufferedReader br = new BufferedReader(new FileReader(CALIBRATE))) {
			perNodeTime = Double.parseDouble(br.readLine());
		} catch (IOException e) {
			perNodeTime = 8.846533871751641E-6;
		}
		try (BufferedReader br = new BufferedReader(new FileReader(INPUTFILENAME))) {
			n = Integer.parseInt(br.readLine());
			int p = Integer.parseInt(br.readLine());
			double timeLeft = Double.parseDouble(br.readLine());
			int maxNodes = (int) Math.floor(timeLeft / (2 * perNodeTime));
			char[][] inGrid = new char[n][n];
			int k = 0;
			String curLine;
			while ((curLine = br.readLine()) != null) {
				inGrid[k] = curLine.toCharArray();
				k++;
			}
			state initState = new state(inGrid, new position(-1, -1), 0, 0, true);
			int maxDepth = (int) Math.ceil(Math.log(maxNodes) / (2 * Math.log(p)));
			state r = minimax(0, initState, maxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);
			writeOutput(r);
		} catch (IOException e) {
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

	public static void writeOutput(state r) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUTFILENAME))) {
			bw.write(Character.toString((char) (r.move.col + 65)) + Integer.toString(r.move.row + 1));
			for (int i = 0; i < n; i++) {
				bw.write("\n" + new String(r.board[i]));
			}
			bw.write("\n" + r.score);
			bw.write("\n" + r.myScore);
			bw.write("\n" + r.oppScore);
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
