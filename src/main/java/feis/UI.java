package feis;

import org.kordamp.ikonli.swing.FontIcon;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Reusable Swing factory methods used across all panels.
 */
public final class UI {

    private UI() {}

    // ═══════════════════════════════════════════════════════════════════
    //  CUSTOM BUTTON — rounded gradient, hover/press, Ikonli icon support
    // ═══════════════════════════════════════════════════════════════════
    public static class FeisButton extends JButton {

        private final Color baseColor;
        private boolean hovered = false;
        private boolean pressed = false;
        private static final int RADIUS = 10;

        public FeisButton(String text, Color baseColor) {
            super(text);
            this.baseColor = baseColor;
            setFont(Theme.FONT_HEADING);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 20, 11, 20));
            setIconTextGap(8);
            setHorizontalAlignment(SwingConstants.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e)  { hovered = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)   { hovered = false; pressed = false; repaint(); }
                @Override public void mousePressed(MouseEvent e)  { pressed = true;  repaint(); }
                @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int shadow = pressed ? 1 : 3;
            int by     = pressed ? 2 : 0;
            int bh     = h - shadow - by;

            // Drop shadow
            if (!pressed) {
                g2.setColor(new Color(0, 0, 0, 55));
                g2.fill(new RoundRectangle2D.Float(2, shadow + by, w - 4, bh, RADIUS * 2, RADIUS * 2));
            }

            // Gradient fill
            Color top, bot;
            if (!isEnabled()) {
                top = new Color(90, 95, 105); bot = new Color(70, 75, 85);
            } else if (pressed) {
                top = baseColor.darker().darker(); bot = baseColor.darker();
            } else if (hovered) {
                top = brighten(baseColor, 30); bot = brighten(baseColor, 10);
            } else {
                top = brighten(baseColor, 18); bot = baseColor;
            }

            g2.setPaint(new GradientPaint(0, by, top, 0, by + bh, bot));
            g2.fill(new RoundRectangle2D.Float(1, by, w - 2, bh, RADIUS * 2, RADIUS * 2));

            // Top shine
            if (!pressed) {
                g2.setColor(new Color(255, 255, 255, 38));
                g2.fill(new RoundRectangle2D.Float(2, by + 1, w - 4, bh / 2, RADIUS * 2, RADIUS * 2));
            }

            // Border outline
            g2.setColor(new Color(0, 0, 0, 45));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(1, by, w - 2, bh, RADIUS * 2, RADIUS * 2));

            g2.dispose();
            // Let Swing paint icon + text on top
            super.paintComponent(g);
        }

        @Override
        protected void paintBorder(Graphics g) { /* handled inside paintComponent */ }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width + 4, d.height + 4);
        }

        static Color brighten(Color c, int a) {
            return new Color(Math.min(255, c.getRed() + a),
                             Math.min(255, c.getGreen() + a),
                             Math.min(255, c.getBlue() + a));
        }
    }

    // ── Compact QR scan button (inline next to text fields) ──────────────
    private static class QRButton extends JButton {
        private boolean hovered = false;
        private boolean pressed = false;

        QRButton() {
            super();
            setIcon(Icons.qrScan(13));
            setText(" QR");
            setFont(new Font("Segoe UI", Font.BOLD, 10));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(4, 10, 5, 10));
            setToolTipText("Scan QR code to fill this field");
            setIconTextGap(4);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e)  { hovered = true;  repaint(); }
                @Override public void mouseExited(MouseEvent e)   { hovered = false; pressed = false; repaint(); }
                @Override public void mousePressed(MouseEvent e)  { pressed = true;  repaint(); }
                @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            Color base = pressed ? Theme.PRIMARY.darker()
                       : hovered ? FeisButton.brighten(Theme.PRIMARY_LIGHT, 20)
                       : Theme.PRIMARY_LIGHT;
            g2.setPaint(new GradientPaint(0, 0, FeisButton.brighten(base, 20), 0, h, base));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, 8, 8));
            g2.setColor(new Color(0, 0, 0, 40));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 8, 8));
            g2.dispose();
            super.paintComponent(g);
        }
        @Override protected void paintBorder(Graphics g) {}
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PUBLIC BUTTON FACTORIES  (all have Ikonli icons)
    // ═══════════════════════════════════════════════════════════════════

    public static JButton primaryBtn(String text) {
        return withIcon(new FeisButton(text, Theme.PRIMARY), Icons.shield(MD));
    }

    public static JButton saveBtn(String text) {
        return withIcon(new FeisButton(text, Theme.PRIMARY), Icons.save(MD));
    }

    public static JButton dangerBtn(String text) {
        return withIcon(new FeisButton(text, Theme.DANGER), Icons.trash(MD));
    }

    public static JButton successBtn(String text) {
        return withIcon(new FeisButton(text, Theme.SUCCESS), Icons.checkCircle(MD));
    }

    public static JButton exportBtn(String text) {
        return withIcon(new FeisButton(text, Theme.SUCCESS), Icons.export(MD));
    }

    public static JButton printBtn(String text) {
        return withIcon(new FeisButton(text, new Color(140, 100, 0)), Icons.print(MD));
    }

    public static JButton accentBtn(String text) {
        return withIcon(new FeisButton(text, new Color(160, 100, 0)), Icons.generate(MD));
    }

    public static JButton refreshBtn(String text) {
        return withIcon(new FeisButton(text, Theme.PRIMARY_LIGHT), Icons.refresh(MD));
    }

    public static JButton cancelBtn(String text) {
        return withIcon(new FeisButton(text, Theme.DANGER), Icons.cancel(MD));
    }

    public static JButton warningBtn(String text) {
        return withIcon(new FeisButton(text, Theme.WARNING), Icons.warning(MD));
    }

    /** Generic styled button — caller supplies the icon */
    public static JButton makeStyledBtn(String text, Color bg) {
        return new FeisButton(text, bg);
    }

    /** Generic styled button with explicit icon */
    public static JButton makeStyledBtn(String text, Color bg, FontIcon icon) {
        return withIcon(new FeisButton(text, bg), icon);
    }

    private static JButton withIcon(JButton btn, FontIcon icon) {
        btn.setIcon(icon);
        return btn;
    }

    private static final int MD = Icons.MD;

    // ═══════════════════════════════════════════════════════════════════
    //  FORM HELPERS
    // ═══════════════════════════════════════════════════════════════════

    public static JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_TITLE);
        l.setForeground(Theme.PRIMARY);
        l.setBorder(new EmptyBorder(0, 0, 8, 0));
        return l;
    }

    public static JLabel subheading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_HEADING);
        l.setForeground(Theme.PRIMARY_LIGHT);
        return l;
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text + ":");
        l.setFont(Theme.FONT_BODY);
        l.setForeground(Theme.TEXT_PRIMARY);
        return l;
    }

    public static JTextField field(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(Theme.FONT_BODY);
        return f;
    }

    public static JTextField dateField() {
        return field(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 12);
    }

    public static JTextField field(String defaultText, int cols) {
        JTextField f = new JTextField(defaultText, cols);
        f.setFont(Theme.FONT_BODY);
        return f;
    }

    public static JComboBox<String> yesNoBox() {
        JComboBox<String> cb = new JComboBox<>(new String[]{"Yes", "No", "N/A"});
        cb.setFont(Theme.FONT_BODY);
        return cb;
    }

    public static JComboBox<String> combo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(Theme.FONT_BODY);
        return cb;
    }

    public static void addRow(JPanel panel, GridBagConstraints g, int row,
                               String labelText, JComponent comp) {
        g.gridx = 0; g.gridy = row; g.weightx = 0.28; g.gridwidth = 1;
        panel.add(label(labelText), g);
        g.gridx = 1; g.weightx = 0.72;
        panel.add(comp, g);
    }

    public static void addRowWithScan(JPanel panel, GridBagConstraints g, int row,
                                       String labelText, JTextField field,
                                       Runnable onScanClick) {
        g.gridx = 0; g.gridy = row; g.weightx = 0.28; g.fill = GridBagConstraints.HORIZONTAL;
        panel.add(label(labelText), g);

        JPanel combo = new JPanel(new BorderLayout(4, 0));
        combo.setOpaque(false);
        combo.add(field, BorderLayout.CENTER);

        QRButton scanBtn = new QRButton();
        scanBtn.addActionListener(e -> onScanClick.run());
        combo.add(scanBtn, BorderLayout.EAST);

        g.gridx = 1; g.weightx = 0.72;
        panel.add(combo, g);
    }

    public static JPanel buttonBar(JButton... btns) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        p.setOpaque(false);
        for (JButton b : btns) p.add(b);
        return p;
    }

    public static JPanel formCard() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.SURFACE);
        p.setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER),
            new EmptyBorder(16, 18, 16, 18)));
        return p;
    }

    public static GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill   = GridBagConstraints.HORIZONTAL;
        return g;
    }

    public static DefaultTableCellRenderer altRowRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) setBackground(row % 2 == 0 ? Theme.ROW_EVEN : Theme.ROW_ODD);
                setBorder(new EmptyBorder(3, 8, 3, 8));
                setFont(Theme.FONT_BODY);
                if (val != null) {
                    String s = val.toString();
                    if (s.equalsIgnoreCase("No") || s.startsWith("⚠"))
                        setForeground(Theme.DANGER);
                    else if (s.equalsIgnoreCase("Yes") || s.startsWith("✅"))
                        setForeground(Theme.SUCCESS);
                    else
                        setForeground(Theme.TEXT_PRIMARY);
                }
                return this;
            }
        };
    }
}
