package com.regentboard.game;

import java.util.ArrayList;
import java.util.List;

public class Board {
    public static final int SIZE = 8;
    private PieceType[][] grid = new PieceType[SIZE][SIZE];

    public Board() {
        reset();
    }

    private Board(PieceType[][] grid) {
        for (int r = 0; r < SIZE; r++)
            this.grid[r] = grid[r].clone();
    }

    public void reset() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = PieceType.NONE;

        for (int r = 0; r < 3; r++)
            for (int c = 0; c < SIZE; c++)
                if ((r + c) % 2 == 1)
                    grid[r][c] = PieceType.BLACK;

        for (int r = 5; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if ((r + c) % 2 == 1)
                    grid[r][c] = PieceType.RED;
    }

    public PieceType get(int row, int col) {
        if (!inBounds(row, col)) return PieceType.NONE;
        return grid[row][col];
    }

    public void set(int row, int col, PieceType type) {
        if (inBounds(row, col)) grid[row][col] = type;
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    public Board copy() {
        return new Board(grid);
    }

    public void applyMove(Move move) {
        PieceType piece = get(move.fromRow, move.fromCol);
        set(move.fromRow, move.fromCol, PieceType.NONE);

        for (int[] cap : move.captured)
            set(cap[0], cap[1], PieceType.NONE);

        set(move.toRow, move.toCol, piece);

        if (piece == PieceType.RED && move.toRow == 0)
            set(move.toRow, move.toCol, PieceType.RED_KING);
        if (piece == PieceType.BLACK && move.toRow == SIZE - 1)
            set(move.toRow, move.toCol, PieceType.BLACK_KING);
    }

    public List<Move> getLegalMoves(boolean redTurn) {
        List<Move> jumps = new ArrayList<>();
        List<Move> normal = new ArrayList<>();

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                PieceType p = get(r, c);
                if (p.isEmpty()) continue;
                if (redTurn && !p.isRed()) continue;
                if (!redTurn && !p.isBlack()) continue;

                List<Move> pieceJumps = getJumps(r, c, p);
                jumps.addAll(pieceJumps);
                if (pieceJumps.isEmpty())
                    normal.addAll(getSimpleMoves(r, c, p));
            }
        }

        return jumps.isEmpty() ? normal : jumps;
    }

    private List<Move> getSimpleMoves(int r, int c, PieceType p) {
        List<Move> moves = new ArrayList<>();
        int[] dirs = p.isKing() ? new int[]{-1, 1} : new int[]{p.isRed() ? -1 : 1};

        for (int dr : dirs)
            for (int dc : new int[]{-1, 1}) {
                int nr = r + dr, nc = c + dc;
                if (inBounds(nr, nc) && get(nr, nc).isEmpty())
                    moves.add(new Move(r, c, nr, nc));
            }
        return moves;
    }

    private List<Move> getJumps(int r, int c, PieceType p) {
        List<Move> result = new ArrayList<>();
        boolean[][] jumped = new boolean[SIZE][SIZE];
        findJumps(r, c, r, c, p, jumped, null, result);
        return result;
    }

    private void findJumps(int startR, int startC, int r, int c,
                            PieceType p, boolean[][] jumped,
                            Move current, List<Move> result) {
        int[] dirs = p.isKing() ? new int[]{-1, 1} : new int[]{p.isRed() ? -1 : 1};
        boolean foundJump = false;

        for (int dr : dirs) {
            for (int dc : new int[]{-1, 1}) {
                int mr = r + dr, mc = c + dc;
                int lr = r + 2 * dr, lc = c + 2 * dc;

                if (!inBounds(lr, lc)) continue;
                if (jumped[mr][mc]) continue;

                PieceType mid = get(mr, mc);
                boolean enemyPresent = p.isRed() ? mid.isBlack() : mid.isRed();

                if (enemyPresent && get(lr, lc).isEmpty()) {
                    foundJump = true;
                    jumped[mr][mc] = true;

                    Move m = (current == null) ? new Move(startR, startC, lr, lc) : current;
                    if (current != null) m.addPathPoint(lr, lc);
                    m.addCapture(mr, mc);

                    PieceType promoted = (p == PieceType.RED && lr == 0) ? PieceType.RED_KING :
                                        (p == PieceType.BLACK && lr == SIZE - 1) ? PieceType.BLACK_KING : p;

                    findJumps(startR, startC, lr, lc, promoted, jumped, m, result);
                    jumped[mr][mc] = false;
                }
            }
        }

        if (!foundJump && current != null) result.add(current);
    }

    public int countPieces(boolean red) {
        int count = 0;
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++) {
                PieceType p = get(r, c);
                if (red && p.isRed()) count++;
                if (!red && p.isBlack()) count++;
            }
        return count;
    }

    public boolean hasWon(boolean red) {
        return countPieces(!red) == 0 || getLegalMoves(!red).isEmpty();
    }
}
