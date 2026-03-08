package feis;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.swing.FontIcon;

import java.awt.*;

/**
 * Centralised icon factory using Ikonli + FontAwesome 5 Solid.
 *
 * Usage:
 *   button.setIcon(Icons.save(16));
 *   label.setIcon(Icons.fire(20));
 */
public final class Icons {

    private Icons() {}

    // ── Sizes ─────────────────────────────────────────────────────────────
    public static final int SM  = 14;
    public static final int MD  = 16;
    public static final int LG  = 20;
    public static final int XL  = 24;
    public static final int XXL = 32;

    // ═════════════════════════════════════════════════════════════════════
    //  BUTTON ICONS  (white, for use on coloured buttons)
    // ═════════════════════════════════════════════════════════════════════

    public static FontIcon save(int size) {
        return icon(FontAwesomeSolid.SAVE, size, Color.WHITE);
    }

    public static FontIcon trash(int size) {
        return icon(FontAwesomeSolid.TRASH, size, Color.WHITE);
    }

    public static FontIcon refresh(int size) {
        return icon(FontAwesomeSolid.SYNC_ALT, size, Color.WHITE);
    }

    public static FontIcon export(int size) {
        return icon(FontAwesomeSolid.FILE_EXPORT, size, Color.WHITE);
    }

    public static FontIcon print(int size) {
        return icon(FontAwesomeSolid.PRINT, size, Color.WHITE);
    }

    public static FontIcon qrScan(int size) {
        return icon(FontAwesomeSolid.QRCODE, size, Color.WHITE);
    }

    public static FontIcon camera(int size) {
        return icon(FontAwesomeSolid.CAMERA, size, Color.WHITE);
    }

    public static FontIcon decode(int size) {
        return icon(FontAwesomeSolid.SEARCH, size, Color.WHITE);
    }

    public static FontIcon folderOpen(int size) {
        return icon(FontAwesomeSolid.FOLDER_OPEN, size, Color.WHITE);
    }

    public static FontIcon keyboard(int size) {
        return icon(FontAwesomeSolid.KEYBOARD, size, Color.WHITE);
    }

    public static FontIcon cancel(int size) {
        return icon(FontAwesomeSolid.TIMES, size, Color.WHITE);
    }

    public static FontIcon tag(int size) {
        return icon(FontAwesomeSolid.TAG, size, Color.WHITE);
    }

    public static FontIcon generate(int size) {
        return icon(FontAwesomeSolid.BARCODE, size, Color.WHITE);
    }

    // ═════════════════════════════════════════════════════════════════════
    //  TAB / SECTION ICONS  (coloured)
    // ═════════════════════════════════════════════════════════════════════

    public static FontIcon fire(int size) {
        return icon(FontAwesomeSolid.FIRE, size, new Color(220, 80, 30));
    }

    public static FontIcon droplet(int size) {
        return icon(FontAwesomeSolid.TINT, size, new Color(30, 130, 210));
    }

    public static FontIcon hose(int size) {
        return icon(FontAwesomeSolid.FAUCET, size, new Color(30, 160, 100));
    }

    public static FontIcon medkit(int size) {
        return icon(FontAwesomeSolid.FIRST_AID, size, new Color(200, 50, 50));
    }

    public static FontIcon clipboard(int size) {
        return icon(FontAwesomeSolid.CLIPBOARD_LIST, size, Theme.ACCENT);
    }

    // ═════════════════════════════════════════════════════════════════════
    //  STATUS / INDICATOR ICONS
    // ═════════════════════════════════════════════════════════════════════

    public static FontIcon checkCircle(int size) {
        return icon(FontAwesomeSolid.CHECK_CIRCLE, size, Theme.SUCCESS);
    }

    public static FontIcon checkCircleWhite(int size) {
        return icon(FontAwesomeSolid.CHECK_CIRCLE, size, Color.WHITE);
    }

    public static FontIcon warning(int size) {
        return icon(FontAwesomeSolid.EXCLAMATION_TRIANGLE, size, Theme.WARNING);
    }

    public static FontIcon info(int size) {
        return icon(FontAwesomeSolid.INFO_CIRCLE, size, Theme.PRIMARY_LIGHT);
    }

    public static FontIcon shield(int size) {
        return icon(FontAwesomeSolid.SHIELD_ALT, size, Color.WHITE);
    }

    // ═════════════════════════════════════════════════════════════════════
    //  FORM FIELD ICONS
    // ═════════════════════════════════════════════════════════════════════

    public static FontIcon calendar(int size) {
        return icon(FontAwesomeSolid.CALENDAR_ALT, size, Theme.TEXT_SECONDARY);
    }

    public static FontIcon locationPin(int size) {
        return icon(FontAwesomeSolid.MAP_MARKER_ALT, size, Theme.TEXT_SECONDARY);
    }

    public static FontIcon user(int size) {
        return icon(FontAwesomeSolid.USER, size, Theme.TEXT_SECONDARY);
    }

    public static FontIcon wrench(int size) {
        return icon(FontAwesomeSolid.WRENCH, size, Theme.TEXT_SECONDARY);
    }

    public static FontIcon building(int size) {
        return icon(FontAwesomeSolid.BUILDING, size, Theme.TEXT_SECONDARY);
    }

    // ═════════════════════════════════════════════════════════════════════
    //  CORE FACTORY
    // ═════════════════════════════════════════════════════════════════════

    public static FontIcon icon(org.kordamp.ikonli.Ikon ikon, int size, Color color) {
        return FontIcon.of(ikon, size, color);
    }
}
