package feis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * The main application window containing all checklist tabs.
 */
public class MainFrame extends JFrame {

    // In-memory record stores (shared across panels and records tab)
    private final List<Map<String,String>> extRecords    = new ArrayList<>();
    private final List<Map<String,String>> delugeRecords = new ArrayList<>();
    private final List<Map<String,String>> hoseRecords   = new ArrayList<>();
    private final List<Map<String,String>> firstAidRecs  = new ArrayList<>();

    private RecordsPanel recordsPanel;

    public MainFrame() {
        super("Fire Equipment Inspection System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1120, 820);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        setIconImage(createIcon());
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.add(buildBanner(), BorderLayout.NORTH);
        root.add(buildTabs(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    // ── Banner ──────────────────────────────────────────────────────────────
    private JPanel buildBanner() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.PRIMARY);
        p.setBorder(new EmptyBorder(10, 18, 10, 18));

        // Left: logo + subtitle
        JPanel left = new JPanel(new GridLayout(3, 1, 0, 2));
        left.setOpaque(false);

        JLabel logo = new JLabel("Fire Equipment Inspection System");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(Color.WHITE);

        JLabel sub1 = new JLabel("Fire Equipment Inspection System  |  Emergency Services Department");
        sub1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub1.setForeground(new Color(170, 200, 245));

        JLabel sub2 = new JLabel("Fire Equipment Inspection System  |  Rev 1.0  |  POPI Act Compliant");
        sub2.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        sub2.setForeground(new Color(130, 165, 210));

        left.add(logo); left.add(sub1); left.add(sub2);

        // Right: date + QR legend
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 2));
        right.setOpaque(false);

        JLabel dateLabel = new JLabel(
            new SimpleDateFormat("EEEE, dd MMMM yyyy").format(new Date()),
            SwingConstants.RIGHT);
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dateLabel.setForeground(Theme.ACCENT_LIGHT);

        JLabel qrLegend = new JLabel("QR scan enabled on all location + condition fields",
            SwingConstants.RIGHT);
        qrLegend.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        qrLegend.setForeground(new Color(150, 195, 255));

        right.add(dateLabel); right.add(qrLegend);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    // ── Tabs ─────────────────────────────────────────────────────────────────
    private JTabbedPane buildTabs() {
        recordsPanel = new RecordsPanel(extRecords, delugeRecords, hoseRecords, firstAidRecs);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(Theme.FONT_HEADING);
        tabs.setBackground(Theme.BG);

        tabs.addTab("Fire Extinguishers",
            new ExtinguisherPanel(extRecords,    recordsPanel::refresh));

        tabs.addTab("Fire Deluge System",
            new DelugePanel(delugeRecords,        recordsPanel::refresh));

        tabs.addTab("Fire Hose Reel",
            new HoseReelPanel(hoseRecords,        recordsPanel::refresh));

        tabs.addTab("First-Aid Box",
            new FirstAidPanel(firstAidRecs,       recordsPanel::refresh));

        tabs.addTab("Records / Export",   recordsPanel);

        // Refresh records tab when selected
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 4) recordsPanel.refresh();
        });

        return tabs;
    }

    // ── Footer ────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 3));
        p.setBackground(new Color(228, 232, 238));
        JLabel l = new JLabel(
            "All confidential information shall only be used for the purpose of collection " +
            "and shall be handled according to the POPI Act.");
        l.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        l.setForeground(new Color(120, 125, 140));
        p.add(l);
        return p;
    }

    // ── App icon (programmatic) ───────────────────────────────────────────────
    private Image createIcon() {
        BufferedImage img = new java.awt.image.BufferedImage(32, 32,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Theme.PRIMARY);
        g.fillRoundRect(0, 0, 32, 32, 8, 8);
        g.setColor(Theme.ACCENT);
        g.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g.drawString("K", 9, 23);
        g.dispose();
        return img;
    }
}
