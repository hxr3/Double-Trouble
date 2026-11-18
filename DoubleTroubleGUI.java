import java.awt.*;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;

public class DoubleTroubleGUI extends JFrame {

    private int[] piles = {3, 7, 5};
    private boolean playerTurn = true;
    private Random rand = new Random();

    private int playerWins = 0;
    private int computerWins = 0;
    private int roundsToWin = 2;

    private JPanel mainContainer;
    private CardLayout cardLayout;

    private String selectedDifficulty = "Unbeatable";

    private JLabel scoreLabel;
    private JLabel statusLabel;
    private JPanel boardPanel;
    private JButton actionButton;
    private JPanel[] difficultyCardPanels = new JPanel[4];

    private int selectedPile = -1;
    private int selectedAmount = 1;

    private boolean isAnimating = false;
    private int animatingPile = -1;
    private float animationScale = 1.0f;

    // Sleek modern dark indie theme palette
    private final Color bgDark = new Color(15, 17, 23);
    private final Color panelBg = new Color(24, 28, 38);
    private final Color accentRed = new Color(255, 70, 85);
    private final Color accentGreen = new Color(46, 204, 113);
    private final Color accentYellow = new Color(241, 196, 15);
    private final Color textWhite = new Color(245, 245, 245);
    private final Color textMuted = new Color(130, 135, 150);

    public DoubleTroubleGUI() {
        setTitle("Double Trouble");
        setSize(1000, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Apply a custom dark UI manager look and feel defaults to eliminate old Swing window defaults
        try {
            UIManager.put("Panel.background", bgDark);
            UIManager.put("OptionPane.background", panelBg);
            UIManager.put("OptionPane.messageForeground", textWhite);
            UIManager.put("Button.background", panelBg);
            UIManager.put("Button.foreground", textWhite);
        } catch (Exception ignored) {}

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(bgDark);

        mainContainer.add(createSetupPanel(), "SETUP");
        mainContainer.add(createLoadingPanel(), "LOADING");
        mainContainer.add(createGamePanel(), "GAME");

        add(mainContainer);
        cardLayout.show(mainContainer, "SETUP");
        setVisible(true);
    }

    // Modern clean main menu setup screen with interactive difficulty cards
    private JPanel createSetupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(bgDark);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel title = new JLabel("DOUBLE TROUBLE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(accentRed);
        panel.add(title, gbc);

        gbc.gridy++;
        JLabel subtitle = new JLabel("SELECT TACTICAL DIFFICULTY");
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        subtitle.setForeground(textMuted);
        panel.add(subtitle, gbc);

        gbc.gridy++;
        JPanel cardsGrid = new JPanel(new GridLayout(2, 2, 18, 18));
        cardsGrid.setBackground(bgDark);

        String[] diffNames = {"Easy", "Medium", "Hard", "Unbeatable"};
        String[] diffDescs = {
            "A relaxed skirmish.",
            "Balanced AI logic.",
            "High strategic pressure.",
            "Flawless Nim calculation."
        };

        for (int i = 0; i < 4; i++) {
            final String diff = diffNames[i];
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(panelBg);
            card.setPreferredSize(new Dimension(260, 120));
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));

            boolean isSelected = diff.equals(selectedDifficulty);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(isSelected ? accentRed : new Color(45, 50, 65), isSelected ? 2 : 1),
                    BorderFactory.createEmptyBorder(18, 18, 18, 18)
            ));

            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setBackground(panelBg);

            JLabel nameLbl = new JLabel(diff);
            nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
            nameLbl.setForeground(isSelected ? accentRed : textWhite);
            topRow.add(nameLbl, BorderLayout.WEST);

            if (isSelected) {
                JLabel badge = new JLabel("ACTIVE");
                badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
                badge.setForeground(accentRed);
                topRow.add(badge, BorderLayout.EAST);
            }
            card.add(topRow);

            card.add(Box.createVerticalStrut(10));

            JLabel descLbl = new JLabel(diffDescs[i]);
            descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            descLbl.setForeground(textMuted);
            card.add(descLbl);

            card.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    selectedDifficulty = diff;
                    refreshSetupCards(cardsGrid, diffNames, diffDescs);
                }
            });

            difficultyCardPanels[i] = card;
            cardsGrid.add(card);
        }

        panel.add(cardsGrid, gbc);

        gbc.gridy++;
        JButton launchBtn = createStyledButton("INITIALIZE MATCH", accentRed);
        launchBtn.setPreferredSize(new Dimension(538, 48));
        launchBtn.addActionListener(e -> startLoadingSequence());
        panel.add(launchBtn, gbc);

        return panel;
    }

    private void refreshSetupCards(JPanel grid, String[] names, String[] descs) {
        grid.removeAll();
        for (int i = 0; i < 4; i++) {
            final String diff = names[i];
            JPanel card = difficultyCardPanels[i];
            card.removeAll();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(panelBg);

            boolean isSelected = diff.equals(selectedDifficulty);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(isSelected ? accentRed : new Color(45, 50, 65), isSelected ? 2 : 1),
                    BorderFactory.createEmptyBorder(18, 18, 18, 18)
            ));

            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setBackground(panelBg);

            JLabel nameLbl = new JLabel(diff);
            nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 17));
            nameLbl.setForeground(isSelected ? accentRed : textWhite);
            topRow.add(nameLbl, BorderLayout.WEST);

            if (isSelected) {
                JLabel badge = new JLabel("ACTIVE");
                badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
                badge.setForeground(accentRed);
                topRow.add(badge, BorderLayout.EAST);
            }
            card.add(topRow);

            card.add(Box.createVerticalStrut(10));

            JLabel descLbl = new JLabel(descs[i]);
            descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            descLbl.setForeground(textMuted);
            card.add(descLbl);

            grid.add(card);
        }
        grid.revalidate();
        grid.repaint();
    }

    private JPanel createLoadingPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(bgDark);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel loadTitle = new JLabel("ESTABLISHING MATCHROOM...");
        loadTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        loadTitle.setForeground(textWhite);
        panel.add(loadTitle, gbc);

        gbc.gridy++;
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(320, 24));
        progressBar.setForeground(accentRed);
        progressBar.setBackground(panelBg);
        progressBar.setBorderPainted(false);
        panel.add(progressBar, gbc);

        Timer timer = new Timer(25, new ActionListener() {
            int progress = 0;
            public void actionPerformed(ActionEvent e) {
                progress += 4;
                progressBar.setValue(progress);
                if (progress >= 100) {
                    ((Timer) e.getSource()).stop();
                    cardLayout.show(mainContainer, "GAME");
                    initNewMatch();
                }
            }
        });

        panel.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                progressBar.setValue(0);
                timer.start();
            }
        });

        return panel;
    }

    private JPanel createGamePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(bgDark);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(panelBg);
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel gameTitle = new JLabel("Double Trouble");
        gameTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gameTitle.setForeground(accentRed);
        topBar.add(gameTitle, BorderLayout.WEST);

        scoreLabel = new JLabel("Series: You 0 - 0 Opponent");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        scoreLabel.setForeground(textWhite);
        topBar.add(scoreLabel, BorderLayout.EAST);
        panel.add(topBar, BorderLayout.NORTH);

        JPanel centerArea = new JPanel(new BorderLayout(10, 10));
        centerArea.setBackground(bgDark);

        boardPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        boardPanel.setBackground(bgDark);
        centerArea.add(boardPanel, BorderLayout.CENTER);

        JPanel actionArea = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actionArea.setBackground(bgDark);
        actionButton = createStyledButton("Confirm Move", accentGreen);
        actionButton.setEnabled(false);
        actionButton.setPreferredSize(new Dimension(200, 44));
        actionButton.addActionListener(e -> handleActionButtonClick());
        actionArea.add(actionButton);
        centerArea.add(actionArea, BorderLayout.SOUTH);

        panel.add(centerArea, BorderLayout.CENTER);

        statusLabel = new JLabel("Your turn. Click a pile card to select, then click balls.");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusLabel.setForeground(textMuted);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton btn = new JButton(text);
        btn.setBackground(baseColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void startLoadingSequence() {
        cardLayout.show(mainContainer, "LOADING");
    }

    private void initNewMatch() {
        playerWins = 0;
        computerWins = 0;
        updateScoreDisplay();
        startNewRound();
    }

    private void startNewRound() {
        piles = new int[]{3, 7, 5};
        selectedPile = -1;
        selectedAmount = 1;
        isAnimating = false;
        playerTurn = true;

        statusLabel.setText("Your turn. Click a pile card to select, then click balls.");
        actionButton.setText("Confirm Move");
        actionButton.setEnabled(false);

        refreshBoard();
    }

    // Redraws board with 3D glossy triangle layout and glowing selection highlight rings
    private void refreshBoard() {
        boardPanel.removeAll();

        String[] sectorNames = {"Green Pile", "Yellow Pile", "Red Pile"};
        Color[] sectorColors = {accentGreen, accentYellow, accentRed};

        for (int i = 0; i < 3; i++) {
            final int pileIndex = i;
            JPanel card = new JPanel();
            card.setBackground(panelBg);
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

            Color borderColor = (selectedPile == i) ? accentRed : new Color(45, 50, 65);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor, selectedPile == i ? 2 : 1),
                    BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));

            card.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (playerTurn && !isAnimating && piles[pileIndex] > 0) {
                        selectedPile = pileIndex;
                        selectedAmount = 1;
                        actionButton.setEnabled(true);
                        statusLabel.setText("Selected " + sectorNames[pileIndex] + ". Click balls to adjust selection.");
                        playPopSound();
                        refreshBoard();
                    }
                }
            });

            JLabel nameLabel = new JLabel(sectorNames[i]);
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            nameLabel.setForeground(sectorColors[i]);
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(nameLabel);

            card.add(Box.createVerticalStrut(6));

            JLabel countLabel = new JLabel(piles[i] + " items left");
            countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            countLabel.setForeground(textMuted);
            countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(countLabel);

            card.add(Box.createVerticalStrut(25));

            JPanel itemsContainer = new JPanel();
            itemsContainer.setLayout(new BoxLayout(itemsContainer, BoxLayout.Y_AXIS));
            itemsContainer.setBackground(panelBg);
            itemsContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

            int totalItems = piles[i];
            int currentBallIndex = 0;
            int rowCapacity = 1;

            while (currentBallIndex < totalItems) {
                int itemsInThisRow = Math.min(rowCapacity, totalItems - currentBallIndex);
                JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
                rowPanel.setBackground(panelBg);

                for (int r = 0; r < itemsInThisRow; r++) {
                    final int ballIndex = currentBallIndex++;
                    JPanel ball = new JPanel() {
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            Graphics2D g2 = (Graphics2D) g;
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                            int diameter = 36;
                            if (isAnimating && pileIndex == animatingPile && ballIndex >= piles[pileIndex] - selectedAmount) {
                                diameter = (int) (36 * animationScale);
                            }

                            int offset = (36 - diameter) / 2;
                            Color base = sectorColors[pileIndex];
                            Color bright = base.brighter().brighter();
                            Color dark = base.darker().darker().darker();

                            // Soft drop shadow
                            g2.setColor(new Color(0, 0, 0, 90));
                            g2.fill(new Ellipse2D.Double(offset + 2, offset + 4, diameter, diameter));

                            // 3D glossy gradient sphere
                            RadialGradientPaint rgp = new RadialGradientPaint(
                                    new Point.Float(offset + diameter * 0.35f, offset + diameter * 0.35f),
                                    diameter * 0.7f,
                                    new float[]{0.0f, 0.7f, 1.0f},
                                    new Color[]{bright, base, dark},
                                    CycleMethod.NO_CYCLE
                            );
                            g2.setPaint(rgp);
                            g2.fill(new Ellipse2D.Double(offset, offset, diameter, diameter));

                            // Sleek glowing neon accent ring instead of flat white outline
                            boolean isSelectedBall = (selectedPile == pileIndex && ballIndex >= piles[pileIndex] - selectedAmount);
                            if (isSelectedBall && !isAnimating) {
                                g2.setColor(new Color(255, 255, 255, 220));
                                g2.setStroke(new BasicStroke(2.5f));
                                g2.draw(new Ellipse2D.Double(offset - 2, offset - 2, diameter + 4, diameter + 4));
                            }
                        }
                    };

                    ball.setOpaque(true);
                    ball.setBackground(panelBg);
                    ball.setPreferredSize(new Dimension(36, 36));

                    if (playerTurn && !isAnimating) {
                        ball.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        ball.addMouseListener(new MouseAdapter() {
                            public void mouseClicked(MouseEvent e) {
                                selectedPile = pileIndex;
                                selectedAmount = piles[pileIndex] - ballIndex;
                                actionButton.setEnabled(true);
                                statusLabel.setText("Selected " + selectedAmount + " balls. Confirm your move.");
                                playPopSound();
                                refreshBoard();
                            }
                        });
                    }

                    rowPanel.add(ball);
                }

                itemsContainer.add(rowPanel);
                rowCapacity++;
            }

            card.add(itemsContainer);
            boardPanel.add(card);
        }

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private void handleActionButtonClick() {
        if (playerTurn && !isAnimating) {
            executePlayerTurn();
        } else if (!playerTurn && !isAnimating) {
            executeComputerTurnStep();
        }
    }

    private void executePlayerTurn() {
        if (selectedPile == -1 || piles[selectedPile] == 0) return;

        if (selectedAmount <= 0 || selectedAmount > piles[selectedPile]) {
            return;
        }

        playPopSound();
        animatingPile = selectedPile;
        isAnimating = true;
        actionButton.setEnabled(false);

        Timer animTimer = new Timer(15, new ActionListener() {
            float scale = 1.0f;
            public void actionPerformed(ActionEvent e) {
                scale -= 0.1f;
                animationScale = Math.max(0.0f, scale);
                refreshBoard();
                if (scale <= 0.0f) {
                    ((Timer) e.getSource()).stop();
                    piles[animatingPile] -= selectedAmount;
                    selectedPile = -1;
                    selectedAmount = 1;
                    isAnimating = false;
                    animationScale = 1.0f;
                    refreshBoard();

                    if (checkRoundOver(true)) return;

                    playerTurn = false;
                    statusLabel.setText("Opponent is thinking...");
                    actionButton.setText("Opponent Turn");
                    actionButton.setEnabled(true);
                    refreshBoard();
                }
            }
        });
        animTimer.start();
    }

    private void executeComputerTurnStep() {
        int targetPile = 0;
        int removeAmount = 1;

        for (int i = 0; i < 3; i++) {
            if (piles[i] > 0) {
                targetPile = i;
                break;
            }
        }

        if (selectedDifficulty.equals("Easy")) {
            do {
                targetPile = rand.nextInt(3);
            } while (piles[targetPile] == 0);
            removeAmount = rand.nextInt(piles[targetPile]) + 1;
        } else if (selectedDifficulty.equals("Medium")) {
            if (rand.nextDouble() < 0.4) {
                targetPile = getNimSumPile();
                removeAmount = calculateNimSumRemoval(targetPile);
            } else {
                do {
                    targetPile = rand.nextInt(3);
                } while (piles[targetPile] == 0);
                removeAmount = rand.nextInt(piles[targetPile]) + 1;
            }
        } else if (selectedDifficulty.equals("Hard")) {
            if (rand.nextDouble() < 0.8) {
                targetPile = getNimSumPile();
                removeAmount = calculateNimSumRemoval(targetPile);
            } else {
                do {
                    targetPile = rand.nextInt(3);
                } while (piles[targetPile] == 0);
                removeAmount = rand.nextInt(piles[targetPile]) + 1;
            }
        } else {
            targetPile = getNimSumPile();
            removeAmount = calculateNimSumRemoval(targetPile);
        }

        playPopSound();
        animatingPile = targetPile;
        selectedAmount = removeAmount;
        selectedPile = targetPile;
        isAnimating = true;
        actionButton.setEnabled(false);

        final int finalTargetPile = targetPile;
        final int finalRemoveAmount = removeAmount;

        Timer animTimer = new Timer(15, new ActionListener() {
            float scale = 1.0f;
            public void actionPerformed(ActionEvent e) {
                scale -= 0.1f;
                animationScale = Math.max(0.0f, scale);
                refreshBoard();
                if (scale <= 0.0f) {
                    ((Timer) e.getSource()).stop();
                    piles[finalTargetPile] -= finalRemoveAmount;
                    selectedPile = -1;
                    selectedAmount = 1;
                    isAnimating = false;
                    animationScale = 1.0f;
                    refreshBoard();

                    if (checkRoundOver(false)) return;

                    playerTurn = true;
                    statusLabel.setText("Your turn. Click a pile card to select, then click balls.");
                    actionButton.setText("Confirm Move");
                    actionButton.setEnabled(false);
                    refreshBoard();
                }
            }
        });
        animTimer.start();
    }

    private void playPopSound() {
        new Thread(() -> {
            try {
                float sampleRate = 8000f;
                byte[] buf = new byte[600];
                for (int i = 0; i < buf.length; i++) {
                    double angle = i / (sampleRate / 350) * 2.0 * Math.PI;
                    buf[i] = (byte)(Math.sin(angle) * 127.0 * (1.0 - (double)i / buf.length));
                }
                AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                sdl.write(buf, 0, buf.length);
                sdl.drain();
                sdl.close();
            } catch (Exception e) {
            }
        }).start();
    }

    private int getNimSumPile() {
        int nimSum = piles[0] ^ piles[1] ^ piles[2];
        if (nimSum != 0) {
            for (int i = 0; i < 3; i++) {
                int target = piles[i] ^ nimSum;
                if (target < piles[i]) return i;
            }
        }
        for (int i = 0; i < i; i++) { // fallback
            if (piles[i] > 0) return i;
        }
        for (int i = 0; i < 3; i++) {
            if (piles[i] > 0) return i;
        }
        return 0;
    }

    private int calculateNimSumRemoval(int pileIndex) {
        int nimSum = piles[0] ^ piles[1] ^ piles[2];
        if (nimSum != 0) {
            int target = piles[pileIndex] ^ nimSum;
            if (target < piles[pileIndex]) return piles[pileIndex] - target;
        }
        return 1;
    }

    // Custom modern dark styled dialog window replacing the default 1980s OS popup dialogs
    private boolean checkRoundOver(boolean playerJustMoved) {
        if (piles[0] == 0 && piles[1] == 0 && piles[2] == 0) {
            if (playerJustMoved) playerWins++;
            else computerWins++;

            updateScoreDisplay();

            if (playerWins == roundsToWin || computerWins == roundsToWin) {
                String winnerMsg = (playerWins > computerWins) ? "MATCH WON" : "MATCH LOST";
                showCustomDialog(winnerMsg, "Final Score: You " + playerWins + " - " + computerWins + " Opponent", true);
                cardLayout.show(mainContainer, "SETUP");
            } else {
                String roundMsg = playerJustMoved ? "ROUND SECURED" : "ROUND LOST";
                showCustomDialog(roundMsg, "Advancing to next round.", false);
                startNewRound();
            }
            return true;
        }
        return false;
    }

    // Custom dark themed modal dialog window matching indie game aesthetic
    private void showCustomDialog(String titleText, String bodyText, boolean isMatchEnd) {
        JDialog dialog = new JDialog(this, titleText, true);
        dialog.setSize(380, 200);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(panelBg);
        dialog.setLayout(new BorderLayout(15, 15));
        ((JComponent) dialog.getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLbl = new JLabel(titleText);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(accentRed);
        titleLbl.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.add(titleLbl, BorderLayout.NORTH);

        JLabel bodyLbl = new JLabel("<html><div style='text-align: center;'>" + bodyText + "</div></html>");
        bodyLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bodyLbl.setForeground(textWhite);
        bodyLbl.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.add(bodyLbl, BorderLayout.CENTER);

        JButton okBtn = createStyledButton("CONTINUE", accentGreen);
        okBtn.setPreferredSize(new Dimension(140, 36));
        okBtn.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setBackground(panelBg);
        btnPanel.add(okBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void updateScoreDisplay() {
        scoreLabel.setText("Series: You " + playerWins + " - " + computerWins + " Opponent");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DoubleTroubleGUI::new);
    }
}