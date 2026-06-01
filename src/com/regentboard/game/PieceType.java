package com.regentboard.game;

public enum PieceType {
    NONE, RED, RED_KING, BLACK, BLACK_KING;

    public boolean isRed() { return this == RED || this == RED_KING; }
    public boolean isBlack() { return this == BLACK || this == BLACK_KING; }
    public boolean isKing() { return this == RED_KING || this == BLACK_KING; }
    public boolean isEmpty() { return this == NONE; }

    public PieceType promote() {
        if (this == RED) return RED_KING;
        if (this == BLACK) return BLACK_KING;
        return this;
    }
}
