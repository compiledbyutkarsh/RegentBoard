package com.regentboard.ai;

import com.regentboard.game.Board;
import com.regentboard.game.Move;
import com.regentboard.game.PieceType;

import java.util.List;

public class CheckersAI {
    private int depth;

    public CheckersAI(int depth) {
        this.depth = depth;
    }

    public Move getBestMove(Board board, boolean redTurn) {
        List<Move> moves = board.getLegalMoves(redTurn);
        if (moves.isEmpty()) return null;

        Move best = null;
        int bestScore = redTurn ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Move m : moves) {
            Board copy = board.copy();
            copy.applyMove(m);
            int score = minimax(copy, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, !redTurn);
            if (redTurn && score > bestScore) { bestScore = score; best = m; }
            if (!redTurn && score < bestScore) { bestScore = score; best = m; }
        }

        return best;
    }

    private int minimax(Board board, int depth, int alpha, int beta, boolean redTurn) {
        if (depth == 0 || board.hasWon(true) || board.hasWon(false))
            return evaluate(board);

        List<Move> moves = board.getLegalMoves(redTurn);
        if (moves.isEmpty()) return redTurn ? -1000 : 1000;

        if (redTurn) {
            int max = Integer.MIN_VALUE;
            for (Move m : moves) {
                Board copy = board.copy();
                copy.applyMove(m);
                max = Math.max(max, minimax(copy, depth - 1, alpha, beta, false));
                alpha = Math.max(alpha, max);
                if (beta <= alpha) break;
            }
            return max;
        } else {
            int min = Integer.MAX_VALUE;
            for (Move m : moves) {
                Board copy = board.copy();
                copy.applyMove(m);
                min = Math.min(min, minimax(copy, depth - 1, alpha, beta, true));
                beta = Math.min(beta, min);
                if (beta <= alpha) break;
            }
            return min;
        }
    }

    private int evaluate(Board board) {
        int score = 0;
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                PieceType p = board.get(r, c);
                if (p == PieceType.RED)       score += 10;
                else if (p == PieceType.RED_KING)   score += 16;
                else if (p == PieceType.BLACK)      score -= 10;
                else if (p == PieceType.BLACK_KING) score -= 16;

                // Positional bonus
                if (p.isRed()) {
                    score += (Board.SIZE - r);
                    if (c == 0 || c == Board.SIZE - 1) score += 2;
                }
                if (p.isBlack()) {
                    score -= r;
                    if (c == 0 || c == Board.SIZE - 1) score -= 2;
                }
            }
        }
        return score;
    }
}
