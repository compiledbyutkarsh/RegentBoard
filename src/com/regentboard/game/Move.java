package com.regentboard.game;

import java.util.ArrayList;
import java.util.List;

public class Move {
    public int fromRow, fromCol;
    public int toRow, toCol;
    public List<int[]> captured = new ArrayList<>();
    public List<int[]> path = new ArrayList<>();

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        path.add(new int[]{fromRow, fromCol});
        path.add(new int[]{toRow, toCol});
    }

    public boolean isJump() {
        return Math.abs(toRow - fromRow) == 2;
    }

    public boolean isMultiJump() {
        return captured.size() > 1;
    }

    public void addCapture(int row, int col) {
        captured.add(new int[]{row, col});
    }

    public void addPathPoint(int row, int col) {
        path.add(new int[]{row, col});
        toRow = row;
        toCol = col;
    }

    @Override
    public String toString() {
        return String.format("(%d,%d)->(%d,%d) captures:%d", fromRow, fromCol, toRow, toCol, captured.size());
    }
}
