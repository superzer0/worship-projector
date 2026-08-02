package sk.calvary.worship.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public final class OperatorPreviewPanel extends JPanel {
    private static final int GAP = 12;

    private final JLabel preparedLabel;
    private final JLabel liveLabel;
    private final JPanel preparedCard;
    private final JPanel liveCard;
    private final JButton goLiveButton;

    public OperatorPreviewPanel(
            Component preparedView,
            Component liveView,
            JButton goLiveButton,
            String preparedText,
            String liveText
    ) {
        super(new GridBagLayout());
        this.goLiveButton = goLiveButton;
        this.preparedLabel = createHeader(preparedText);
        this.liveLabel = createHeader(liveText);
        this.preparedCard = createPreviewCard("preparedPreview", preparedLabel, preparedView);
        this.liveCard = createPreviewCard("livePreview", liveLabel, liveView);

        setBorder(new EmptyBorder(GAP, GAP, GAP, GAP));
        setMinimumSize(new Dimension(320, 420));
        setPreferredSize(new Dimension(380, 680));

        configureGoLiveButton();

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.BOTH;

        constraints.gridy = 0;
        constraints.weighty = 1;
        add(preparedCard, constraints);

        constraints.gridy = 1;
        constraints.weighty = 0;
        constraints.insets = new Insets(GAP, 0, GAP, 0);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(goLiveButton, constraints);

        constraints.gridy = 2;
        constraints.weighty = 1;
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.fill = GridBagConstraints.BOTH;
        add(liveCard, constraints);

        applyThemeColors();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (goLiveButton != null)
            applyThemeColors();
    }

    private JLabel createHeader(String text) {
        JLabel label = new JLabel(text);
        label.setBorder(new EmptyBorder(0, 2, 8, 2));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        return label;
    }

    private JPanel createPreviewCard(String name, JLabel header, Component view) {
        JPanel card = new JPanel(new BorderLayout());
        card.setName(name);
        card.setBorder(new EmptyBorder(GAP, GAP, GAP, GAP));
        card.add(header, BorderLayout.NORTH);
        card.add(view, BorderLayout.CENTER);
        return card;
    }

    private void configureGoLiveButton() {
        goLiveButton.setName("goLiveButton");
        goLiveButton.setPreferredSize(new Dimension(240, 52));
        goLiveButton.setMinimumSize(new Dimension(160, 44));
        goLiveButton.setFont(goLiveButton.getFont().deriveFont(Font.BOLD, 15f));
        goLiveButton.setToolTipText("Send the prepared content to the live output (F5)");
        goLiveButton.putClientProperty("JButton.buttonType", "roundRect");
        goLiveButton.getAccessibleContext().setAccessibleDescription(
                "Send the prepared lyrics and background to the live output"
        );
    }

    private void applyThemeColors() {
        Color preparedAccent = color("jWorship.preparedAccent", new Color(0x2563EB));
        Color liveAccent = color("jWorship.liveAccent", new Color(0xDC2626));
        Color panelBackground = color("Panel.background", getBackground());
        Color cardBackground = color("TextField.background", panelBackground);

        setBackground(panelBackground);
        preparedLabel.setForeground(preparedAccent);
        liveLabel.setForeground(liveAccent);
        preparedCard.setBackground(cardBackground);
        liveCard.setBackground(cardBackground);
        preparedCard.setBorder(new CompoundBorder(
                new LineBorder(preparedAccent, 2, true),
                new EmptyBorder(GAP, GAP, GAP, GAP)
        ));
        liveCard.setBorder(new CompoundBorder(
                new LineBorder(liveAccent, 2, true),
                new EmptyBorder(GAP, GAP, GAP, GAP)
        ));
        Color liveAction = UIManager.getColor("jWorship.liveActionBackground");
        if (liveAction == null) {
            liveAction = new Color(0xDC2626);
        }
        goLiveButton.setBackground(liveAction);
        goLiveButton.setForeground(Color.WHITE);
    }

    private Color color(String key, Color fallback) {
        Color value = UIManager.getColor(key);
        return value == null ? fallback : value;
    }
}
