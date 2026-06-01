# ♟ RegentBoard — Premium Checkers

A high-end checkers game built with Java Swing featuring smooth animations, particle effects, 3D glossy pieces, and a Minimax AI opponent with Alpha-Beta pruning.

## ✨ Features
- 3D glossy pieces with gradient rendering and shine effects
- Smooth piece movement animation with arc bounce
- Particle burst effects on captures
- Pulsing gold ring on selectable pieces
- Blue dot highlights for valid moves
- 👑 King pieces with crown symbol
- Win overlay with glow effect
- 🤖 Minimax AI with Alpha-Beta pruning (depth 6)
- ↩️ Undo move support
- Two player and vs AI mode

## 🛠️ Tech Stack
| Layer | Technology |
|---|---|
| Language | Java 17 |
| UI | Java Swing & AWT |
| Graphics | Custom 2D Graphics (Graphics2D) |
| AI | Minimax + Alpha-Beta Pruning |
| Game Loop | Timer-based 60fps |

## 🚀 How to Run

### Compile
```bash
cd RegentBoard
find src -name "*.java" > sources.txt
javac -d out @sources.txt
```

### Run
```bash
java -cp out com.regentboard.Main
```

## 🎮 How to Play
- Click a piece to select it (gold ring appears)
- Blue dots show valid moves
- Click a blue dot to move
- Mandatory jumps are enforced
- Multi-jump chains supported
- Reach the opposite end to become a King ♛

## 📁 Structure
```
RegentBoard/
├── src/com/regentboard/
│   ├── Main.java
│   ├── game/
│   │   ├── Board.java
│   │   ├── GameState.java
│   │   ├── Move.java
│   │   └── PieceType.java
│   ├── ai/
│   │   └── CheckersAI.java
│   └── ui/
│       ├── BoardPanel.java
│       └── GameFrame.java
```

---
Built by [compiledbyutkarsh](https://github.com/compiledbyutkarsh)
