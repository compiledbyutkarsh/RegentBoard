package com.regentboard.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GameFrame extends JFrame {

    private BoardPanel boardPanel;
    private JLabel statusLabel;
    private JLabel scoreLabel;

    public GameFrame() {
        setTitle("RegentBoard — Premium Checkers");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        buildUI();
        pack();
        setLocationRelativeTo(null);
    }

    private void buildUI() {
        Color BG      = new Color(8, 8, 16);
        Color PANEL   = new Color(14, 14, 24);
        Color BORDER  = new Color(40, 40, 70);
        Color TEXT    = new Color(220, 220, 240);
        Color SUBTEXT = new Color(120, 120, 160);
        Color ACCENT  = new Color(100, 120, 220);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG);
        setContentPane(root);

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(PANEL);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(14, 20, 14, 20)
        ));

        JLabel title = new JLabel("♟ RegentBoard");
        title.setFont(new Font("Georgia", Font.BOLD, 20));
        title.setForeground(new Color(200, 180, 255));
        topBar.add(title, BorderLayout.WEST);

        scoreLabel = new JLabel("🔴 12   ⚫ 12");
        scoreLabel.setFont(new Font("Dialog", Font.BOLD, 15));
        scoreLabel.setForeground(TEXT);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topBar.add(scoreLabel, BorderLayout.CENTER);

        statusLabel = new JLabel("🔴 Red's Turn");
        statusLabel.setFont(new Font("Dialog", Font.PLAIN, 13));
        statusLabel.setForeground(SUBTEXT);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        topBar.add(statusLabel, BorderLayout.EAST);

        root.add(topBar, BorderLayout.NORTH);

        // Board
        boardPanel = new BoardPanel(statusLabel, scoreLabel);
        JPanel boardWrap = new JPanel(new GridBagLayout());
        boardWrap.setBackground(BG);
        boardWrap.setBorder(new EmptyBorder(20, 20, 20, 20));
        boardWrap.add(boardPanel);
        root.add(boardWrap, BorderLayout.CENTER);

        // Bottom controls
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 14));
        bottom.setBackground(PANEL);
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JButton newGame = styledBtn("New Game", new Color(60, 180, 100));
        JButton undo    = styledBtn("Undo", new Color(100, 120, 220));
        JButton aiToggle = styledBtn("vs AI: ON", new Color(180, 100, 60));

        newGame.addActionListener(e -> boardPanel.newGame());
        undo.addActionListener(e -> boardPanel.undo());
        aiToggle.addActionListener(e -> {
            boardPanel.toggleAI();
            aiToggle.setText(aiToggle.getText().contains("ON") ? "vs AI: OFF" : "vs AI: ON");
        });

        bottom.add(newGame);
        bottom.add(undo);
        bottom.add(aiToggle);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        legend.setBackground(PANEL);
        legend.add(legendItem("Highlighted ring = selectable piece", SUBTEXT));
        legend.add(legendItem("Blue dot = valid move", SUBTEXT));
        bottom.add(legend);

        root.add(bottom, BorderLayout.SOUTH);
    }

    private JButton styledBtn(String text, Color accent) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Dialog", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(accent.darker());
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accent, 1),
            new EmptyBorder(8, 18, 8, 18)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(accent); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(accent.darker()); }
        });
        return btn;
    }

    private JLabel legendItem(String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Dialog", Font.PLAIN, 11));
        lbl.setForeground(color);
        return lbl;
    }
}
