package com.regentboard.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GameState {
    private Board board;
    private boolean redTurn;
    private List<Move> legalMoves;
    private Move selectedMove;
    private int selectedRow = -1, selectedCol = -1;
    private Stack<Board> history = new Stack<>();
    private Stack<Boolean> turnHistory = new Stack<>();
    private boolean gameOver = false;
    private boolean redWon = false;

    public GameState() {
        board = new Board();
        redTurn = true;
        refreshMoves();
    }

    private void refreshMoves() {
        legalMoves = board.getLegalMoves(redTurn);
        if (legalMoves.isEmpty()) {
            gameOver = true;
            redWon = !redTurn;
        }
    }

    public boolean selectCell(int row, int col) {
        PieceType piece = board.get(row, col);
        boolean ownPiece = redTurn ? piece.isRed() : piece.isBlack();

        if (ownPiece) {
            boolean hasMoves = legalMoves.stream()
                .anyMatch(m -> m.fromRow == row && m.fromCol == col);
            if (hasMoves) {
                selectedRow = row;
                selectedCol = col;
                return true;
            }
        }
        return false;
    }

    public boolean tryMove(int row, int col) {
        if (selectedRow < 0) return false;
        for (Move m : legalMoves) {
            if (m.fromRow == selectedRow && m.fromCol == selectedCol
                && m.toRow == row && m.toCol == col) {
                applyMove(m);
                return true;
            }
        }
        return false;
    }

    private void applyMove(Move m) {
        history.push(board.copy());
        turnHistory.push(redTurn);
        board.applyMove(m);
        selectedRow = -1;
        selectedCol = -1;
        redTurn = !redTurn;
        refreshMoves();
    }

    public boolean undo() {
        if (history.isEmpty()) return false;
        board = history.pop();
        redTurn = turnHistory.pop();
        gameOver = false;
        refreshMoves();
        selectedRow = -1;
        selectedCol = -1;
        return true;
    }

    public List<int[]> getValidDestinations() {
        List<int[]> dests = new ArrayList<>();
        if (selectedRow < 0) return dests;
        for (Move m : legalMoves)
            if (m.fromRow == selectedRow && m.fromCol == selectedCol)
                dests.add(new int[]{m.toRow, m.toCol});
        return dests;
    }

    public List<int[]> getSelectablePieces() {
        List<int[]> pieces = new ArrayList<>();
        for (Move m : legalMoves) {
            boolean already = pieces.stream()
                .anyMatch(p -> p[0] == m.fromRow && p[1] == m.fromCol);
            if (!already) pieces.add(new int[]{m.fromRow, m.fromCol});
        }
        return pieces;
    }

    public Board getBoard() { return board; }
    public boolean isRedTurn() { return redTurn; }
    public boolean isGameOver() { return gameOver; }
    public boolean isRedWon() { return redWon; }
    public int getSelectedRow() { return selectedRow; }
    public int getSelectedCol() { return selectedCol; }
    public List<Move> getLegalMoves() { return legalMoves; }
    public int getRedCount() { return board.countPieces(true); }
    public int getBlackCount() { return board.countPieces(false); }
    public void clearSelection() { selectedRow = -1; selectedCol = -1; }
}
