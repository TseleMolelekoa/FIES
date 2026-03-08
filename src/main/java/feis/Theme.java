package feis;

import java.awt.*;

/**
 * Centralised design tokens for the Fire Equipment Inspection System.
 */
public final class Theme {

    private Theme() {}

    // ── Palette ──────────────────────────────────────────────────────────
    public static final Color PRIMARY        = new Color(10,  44,  94);   // deep navy
    public static final Color PRIMARY_LIGHT  = new Color(29, 100, 160);
    public static final Color ACCENT         = new Color(212, 160,  20);  // gold
    public static final Color ACCENT_LIGHT   = new Color(255, 210,  60);
    public static final Color SUCCESS        = new Color(22, 140,  60);
    public static final Color DANGER         = new Color(190,  30,  30);
    public static final Color WARNING        = new Color(220, 140,   0);
    public static final Color BG             = new Color(242, 244, 248);
    public static final Color SURFACE        = Color.WHITE;
    public static final Color BORDER         = new Color(205, 210, 220);
    public static final Color TEXT_PRIMARY   = new Color(50,  70,  90);
    public static final Color TEXT_SECONDARY = new Color(90, 100, 120);
    public static final Color ROW_EVEN       = Color.GREEN;
    public static final Color ROW_ODD        = new Color(237, 243, 252);

    // ── Typography ────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  18);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas",  Font.PLAIN, 11);

    // ── Spacing ───────────────────────────────────────────────────────────
    public static final int GAP    = 8;
    public static final int RADIUS = 8;
}
