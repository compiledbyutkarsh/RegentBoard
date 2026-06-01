package com.regentboard.ui;

import com.regentboard.ai.CheckersAI;
import com.regentboard.game.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class BoardPanel extends JPanel {

    private static final int CELL = 80;
    private static final int BOARD_SIZE = Board.SIZE * CELL;
    private static final int PIECE_PAD = 10;

    private final Color BG          = new Color(10, 10, 18);
    private final Color DARK_SQUARE = new Color(28, 30, 48);
    private final Color LIGHT_SQUARE= new Color(18, 20, 34);
    private final Color RED_BASE    = new Color(220, 50, 50);
    private final Color RED_LIGHT   = new Color(255, 100, 100);
    private final Color BLACK_BASE  = new Color(30, 30, 50);
    private final Color BLACK_LIGHT = new Color(80, 80, 120);
    private final Color CROWN_COLOR = new Color(255, 215, 0);
    private final Color HINT_COLOR  = new Color(100, 200, 255, 90);
    private final Color SELECT_GLOW = new Color(100, 200, 255, 160);
    private final Color MOVABLE_RING= new Color(255, 220, 60, 140);

    private GameState state = new GameState();
    private CheckersAI ai = new CheckersAI(6);
    private boolean vsAI = true;

    // Animation state
    private float selPulse = 0;
    private float selDir = 1;
    private List<FadePiece> fadePieces = new ArrayList<>();
    private MovingPiece movingPiece = null;
    private Timer animTimer;

    // Particle system
    private List<Particle> particles = new ArrayList<>();

    private JLabel statusLabel;
    private JLabel scoreLabel;

    public BoardPanel(JLabel statusLabel, JLabel scoreLabel) {
        this.statusLabel = statusLabel;
        this.scoreLabel = scoreLabel;
        setPreferredSize(new Dimension(BOARD_SIZE, BOARD_SIZE));
        setBackground(BG);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { handleClick(e.getX(), e.getY()); }
        });

        animTimer = new Timer(16, e -> tick());
        animTimer.start();
        updateLabels();
    }

    private void tick() {
        selPulse += 0.07f * selDir;
        if (selPulse > 1f) { selPulse = 1f; selDir = -1; }
        if (selPulse < 0f) { selPulse = 0f; selDir = 1; }

        fadePieces.removeIf(fp -> { fp.alpha -= 0.05f; return fp.alpha <= 0; });

        if (movingPiece != null) {
            movingPiece.progress += 0.08f;
            if (movingPiece.progress >= 1f) {
                movingPiece = null;
                if (vsAI && !state.isRedTurn() && !state.isGameOver())
                    SwingUtilities.invokeLater(this::doAIMove);
            }
        }

        particles.removeIf(p -> { p.update(); return p.life <= 0; });
        repaint();
    }

    private void handleClick(int px, int py) {
        if (movingPiece != null) return;
        if (state.isGameOver()) return;
        if (vsAI && !state.isRedTurn()) return;

        int col = px / CELL;
        int row = py / CELL;
        if (!state.getBoard().inBounds(row, col)) return;

        if (state.getSelectedRow() >= 0) {
            boolean moved = state.tryMove(row, col);
            if (moved) {
                spawnParticles(row, col);
                updateLabels();
                if (vsAI && !state.isRedTurn() && !state.isGameOver())
                    SwingUtilities.invokeLater(this::doAIMove);
                return;
            }
        }
        state.selectCell(row, col);
    }

    private void doAIMove() {
        Move m = ai.getBestMove(state.getBoard(), false);
        if (m == null) return;

        float startX = m.fromCol * CELL + CELL / 2f;
        float startY = m.fromRow * CELL + CELL / 2f;
        float endX   = m.toCol * CELL + CELL / 2f;
        float endY   = m.toRow * CELL + CELL / 2f;
        PieceType piece = state.getBoard().get(m.fromRow, m.fromCol);
        movingPiece = new MovingPiece(startX, startY, endX, endY, piece);

        for (int[] cap : m.captured)
            fadePieces.add(new FadePiece(cap[0], cap[1], state.getBoard().get(cap[0], cap[1])));

        state.tryMove(m.toRow, m.toCol);
        spawnParticles(m.toRow, m.toCol);
        updateLabels();
    }

    public void undo() {
        state.undo();
        if (vsAI) state.undo();
        updateLabels();
    }

    public void newGame() {
        state = new GameState();
        fadePieces.clear();
        movingPiece = null;
        particles.clear();
        updateLabels();
    }

    public void toggleAI() {
        vsAI = !vsAI;
    }

    private void updateLabels() {
        if (state.isGameOver()) {
            statusLabel.setText(state.isRedWon() ? "🏆 Red Wins!" : "🏆 Black Wins!");
        } else {
            statusLabel.setText(state.isRedTurn() ? "🔴 Red's Turn" : "⚫ Black's Turn");
        }
        scoreLabel.setText("🔴 " + state.getRedCount() + "   ⚫ " + state.getBlackCount());
    }

    private void spawnParticles(int row, int col) {
        float cx = col * CELL + CELL / 2f;
        float cy = row * CELL + CELL / 2f;
        for (int i = 0; i < 12; i++)
            particles.add(new Particle(cx, cy));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawBoard(g2);
        drawHighlights(g2);
        drawPieces(g2);
        drawMovingPiece(g2);
        drawParticles(g2);
        if (state.isGameOver()) drawWinOverlay(g2);
    }

    private void drawBoard(Graphics2D g2) {
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                Color base = (r + c) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE;
                g2.setColor(base);
                g2.fillRect(c * CELL, r * CELL, CELL, CELL);

                // Subtle grid lines
                g2.setColor(new Color(255, 255, 255, 8));
                g2.drawRect(c * CELL, r * CELL, CELL, CELL);
            }
        }

        // Board border glow
        g2.setStroke(new BasicStroke(3f));
        g2.setColor(new Color(100, 120, 200, 60));
        g2.drawRect(1, 1, BOARD_SIZE - 2, BOARD_SIZE - 2);
    }

    private void drawHighlights(Graphics2D g2) {
        List<int[]> movable = state.getSelectablePieces();
        for (int[] pos : movable) {
            float alpha = 0.4f + 0.3f * selPulse;
            g2.setColor(new Color(
                MOVABLE_RING.getRed(), MOVABLE_RING.getGreen(),
                MOVABLE_RING.getBlue(), (int)(alpha * 255)));
            g2.setStroke(new BasicStroke(2.5f));
            int x = pos[1] * CELL + 4, y = pos[0] * CELL + 4;
            g2.drawOval(x, y, CELL - 8, CELL - 8);
        }

        int sr = state.getSelectedRow(), sc = state.getSelectedCol();
        if (sr >= 0) {
            float glowAlpha = 0.5f + 0.5f * selPulse;
            for (int i = 4; i >= 0; i--) {
                float a = glowAlpha * (1 - i * 0.18f);
                g2.setColor(new Color(100, 200, 255, (int)(a * 180)));
                g2.setStroke(new BasicStroke(i * 1.2f + 1f));
                g2.drawOval(sc * CELL + PIECE_PAD - i, sr * CELL + PIECE_PAD - i,
                    CELL - PIECE_PAD * 2 + i * 2, CELL - PIECE_PAD * 2 + i * 2);
            }
        }

        List<int[]> dests = state.getValidDestinations();
        for (int[] d : dests) {
            int x = d[1] * CELL, y = d[0] * CELL;
            g2.setColor(HINT_COLOR);
            g2.fillRoundRect(x + 2, y + 2, CELL - 4, CELL - 4, 8, 8);
            g2.setColor(new Color(100, 200, 255, 200));
            g2.setStroke(new BasicStroke(1.5f));
            int dotSize = 14;
            g2.fillOval(x + CELL / 2 - dotSize / 2, y + CELL / 2 - dotSize / 2, dotSize, dotSize);
        }
    }

    private void drawPieces(Graphics2D g2) {
        Board board = state.getBoard();
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                PieceType p = board.get(r, c);
                if (p.isEmpty()) continue;
                if (movingPiece != null && r == state.getSelectedRow() && c == state.getSelectedCol()) continue;

                final int fr = r, fc = c;
                boolean fading = fadePieces.stream().anyMatch(fp -> fp.row == fr && fp.col == fc);
                if (fading) continue;

                drawPiece(g2, p, c * CELL + CELL / 2, r * CELL + CELL / 2, 1f);
            }
        }

        for (FadePiece fp : fadePieces)
            drawPiece(g2, fp.type, fp.col * CELL + CELL / 2, fp.row * CELL + CELL / 2, fp.alpha);
    }

    private void drawPiece(Graphics2D g2, PieceType type, int cx, int cy, float alpha) {
        int r = CELL / 2 - PIECE_PAD;
        boolean red = type.isRed();

        Color base  = red ? RED_BASE  : BLACK_BASE;
        Color light = red ? RED_LIGHT : BLACK_LIGHT;
        Color shadow = new Color(0, 0, 0, (int)(180 * alpha));

        // Shadow
        g2.setColor(shadow);
        g2.fillOval(cx - r + 4, cy - r + 6, r * 2, r * 2);

        // Main piece gradient
        RadialGradientPaint gradient = new RadialGradientPaint(
            new Point2D.Float(cx - r * 0.3f, cy - r * 0.3f),
            r * 1.2f,
            new float[]{0f, 1f},
            new Color[]{
                new Color(light.getRed(), light.getGreen(), light.getBlue(), (int)(255 * alpha)),
                new Color(base.getRed(), base.getGreen(), base.getBlue(), (int)(255 * alpha))
            }
        );
        g2.setPaint(gradient);
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);

        // Rim
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(255, 255, 255, (int)(40 * alpha)));
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);

        // Top shine
        g2.setColor(new Color(255, 255, 255, (int)(60 * alpha)));
        g2.fillOval(cx - r / 2, cy - r / 2 - 4, r / 2, r / 3);

        // Crown
        if (type.isKing()) {
            g2.setColor(new Color(CROWN_COLOR.getRed(), CROWN_COLOR.getGreen(), CROWN_COLOR.getBlue(), (int)(255 * alpha)));
            g2.setFont(new Font("Serif", Font.BOLD, r));
            FontMetrics fm = g2.getFontMetrics();
            String crown = "♛";
            int tw = fm.stringWidth(crown);
            g2.drawString(crown, cx - tw / 2, cy + fm.getAscent() / 2 - 2);
        }
    }

    private void drawMovingPiece(Graphics2D g2) {
        if (movingPiece == null) return;
        float t = easeInOut(movingPiece.progress);
        float x = movingPiece.startX + (movingPiece.endX - movingPiece.startX) * t;
        float y = movingPiece.startY + (movingPiece.endY - movingPiece.startY) * t;

        float bounce = (float) Math.sin(movingPiece.progress * Math.PI) * 8;
        drawPiece(g2, movingPiece.type, (int) x, (int) (y - bounce), 1f);
    }

    private void drawParticles(Graphics2D g2) {
        for (Particle p : particles) {
            float alpha = p.life / p.maxLife;
            g2.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), (int)(alpha * 220)));
            g2.fillOval((int)(p.x - p.size / 2), (int)(p.y - p.size / 2), (int)p.size, (int)p.size);
        }
    }

    private void drawWinOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, BOARD_SIZE, BOARD_SIZE);

        String msg = state.isRedWon() ? "RED WINS" : "BLACK WINS";
        g2.setFont(new Font("Georgia", Font.BOLD, 52));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(msg);

        // Glow
        for (int i = 8; i >= 0; i--) {
            float a = 0.06f * (8 - i);
            g2.setColor(new Color(255, 215, 0, (int)(a * 255)));
            g2.drawString(msg, BOARD_SIZE / 2 - tw / 2 - i, BOARD_SIZE / 2 + i);
            g2.drawString(msg, BOARD_SIZE / 2 - tw / 2 + i, BOARD_SIZE / 2 - i);
        }

        g2.setColor(new Color(255, 215, 0));
        g2.drawString(msg, BOARD_SIZE / 2 - tw / 2, BOARD_SIZE / 2);

        g2.setFont(new Font("Dialog", Font.PLAIN, 16));
        String sub = "Press New Game to play again";
        int sw = g2.getFontMetrics().stringWidth(sub);
        g2.setColor(new Color(200, 200, 200, 180));
        g2.drawString(sub, BOARD_SIZE / 2 - sw / 2, BOARD_SIZE / 2 + 50);
    }

    private float easeInOut(float t) {
        return t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }

    // Inner classes
    static class FadePiece {
        int row, col; PieceType type; float alpha = 1f;
        FadePiece(int r, int c, PieceType t) { row = r; col = c; type = t; }
    }

    static class MovingPiece {
        float startX, startY, endX, endY, progress = 0;
        PieceType type;
        MovingPiece(float sx, float sy, float ex, float ey, PieceType t) {
            startX = sx; startY = sy; endX = ex; endY = ey; type = t;
        }
    }

    static class Particle {
        float x, y, vx, vy, size, life, maxLife;
        Color color;
        static final Color[] COLORS = {
            new Color(255, 200, 50), new Color(100, 200, 255),
            new Color(255, 100, 100), new Color(150, 255, 150)
        };
        Particle(float x, float y) {
            this.x = x; this.y = y;
            double angle = Math.random() * 2 * Math.PI;
            float speed = (float)(Math.random() * 4 + 1);
            vx = (float)(Math.cos(angle) * speed);
            vy = (float)(Math.sin(angle) * speed);
            size = (float)(Math.random() * 6 + 2);
            maxLife = life = (float)(Math.random() * 20 + 10);
            color = COLORS[(int)(Math.random() * COLORS.length)];
        }
        void update() { x += vx; y += vy; vy += 0.2f; life--; size *= 0.96f; }
    }
}
