package feis;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

/**
 * Utility class to generate QR code images and a printable tag dialog
 * for fire equipment labelling.
 */
public class QRCodeGenerator {

    private QRCodeGenerator() {}

    // ── Core QR generator ────────────────────────────────────────────────
    /**
     * Generates a QR code as a BufferedImage.
     * @param content  text/URL to encode
     * @param size     pixel size (square)
     * @return         BufferedImage of the QR code
     */
    public static BufferedImage generateQR(String content, int size) throws WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < size; x++)
            for (int y = 0; y < size; y++)
                img.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
        return img;
    }

    /**
     * Build a branded equipment tag image ready for printing.
     * Contains company name, equipment info, QR code, and a border.
     */
    public static BufferedImage buildTag(String equipmentNo, String area, String type,
                                         String site, String extraInfo) throws WriterException {
        int W = 400, H = 580;
        BufferedImage tag = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = tag.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, W, H);

        // Top bar
        g.setColor(Theme.PRIMARY);
        g.fillRoundRect(0, 0, W, 70, 0, 0);

        // Logo area
        g.setColor(Theme.ACCENT);
        g.fillRect(0, 0, 8, 70);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g.drawString("FIRE EQUIPMENT INSPECTION SYSTEM", 20, 26);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g.setColor(new Color(190, 220, 255));
        g.drawString("Fire Equipment Inspection Tag", 20, 46);
        g.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g.setColor(Theme.ACCENT_LIGHT);
        g.drawString("Fire Equipment Inspection System", 20, 62);

        // QR code content string
        String qrContent = String.format(
            "FEIS-EQ|No:%s|Area:%s|Type:%s|Site:%s|%s",
            equipmentNo, area, type, site, extraInfo
        );

        // Generate QR
        BufferedImage qr = generateQR(qrContent, 240);
        g.drawImage(qr, (W - 240) / 2, 90, null);

        // Equipment details
        int y = 350;
        g.setColor(Theme.PRIMARY);
        g.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g.drawString(equipmentNo, 20, y);

        y += 26;
        g.setColor(Theme.TEXT_PRIMARY);
        g.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g.drawString("Type:   " + type, 20, y);

        y += 22;
        g.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        g.setColor(Theme.TEXT_SECONDARY);
        g.drawString("Area:   " + area, 20, y);

        y += 22;
        g.drawString("Site:    " + site, 20, y);

        if (extraInfo != null && !extraInfo.isBlank()) {
            y += 22;
            g.drawString("Info:    " + extraInfo, 20, y);
        }

        // Condition scan section
        y += 40;
        g.setColor(Theme.PRIMARY);
        g.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g.drawString("MONTHLY INSPECTION SCAN", 20, y);

        // Small QR for condition check
        String condQR = "FEIS-COND|No:" + equipmentNo + "|CHECK";
        BufferedImage condQrImg = generateQR(condQR, 100);
        g.drawImage(condQrImg, W - 120, y - 10, null);

        y += 16;
        g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g.setColor(Theme.TEXT_SECONDARY);
        g.drawString("Scan above QR to open monthly checklist", 20, y);
        g.drawString("for this equipment", 20, y + 14);

        // Border
        g.setColor(Theme.PRIMARY);
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(3, 3, W - 6, H - 6, 12, 12);

        // Bottom strip
        g.setColor(new Color(240, 243, 248));
        g.fillRect(0, H - 32, W, 32);
        g.setColor(Theme.TEXT_SECONDARY);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        g.drawString("Fire Equipment Inspection System  |  POPI Act Compliant  |  Scan QR for inspection data", 14, H - 10);

        g.dispose();
        return tag;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  TAG VIEWER DIALOG
    // ═════════════════════════════════════════════════════════════════════
    public static void showTagDialog(Window owner, String equipmentNo, String area,
                                      String type, String site, String extra) {
        try {
            BufferedImage tag = buildTag(equipmentNo, area, type, site, extra);

            JDialog dlg = new JDialog(owner, "Equipment QR Tag — " + equipmentNo,
                    Dialog.ModalityType.APPLICATION_MODAL);
            dlg.setSize(460, 660);
            dlg.setLocationRelativeTo(owner);
            dlg.setResizable(false);

            JPanel root = new JPanel(new BorderLayout(8, 8));
            root.setBackground(Theme.BG);
            root.setBorder(new EmptyBorder(12, 12, 12, 12));

            JLabel imgLabel = new JLabel(new ImageIcon(tag));
            imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
            JScrollPane scroll = new JScrollPane(imgLabel);
            scroll.setBorder(null);
            root.add(scroll, BorderLayout.CENTER);

            // Buttons
            JButton printBtn = styledBtn("Print Tag", Theme.PRIMARY);
            JButton saveBtn  = styledBtn("Save PNG",  Theme.SUCCESS);
            JButton closeBtn = styledBtn("Close",      Theme.DANGER);

            printBtn.addActionListener(e -> printTag(tag, dlg));
            saveBtn.addActionListener(e  -> saveTagPNG(tag, equipmentNo, dlg));
            closeBtn.addActionListener(e -> dlg.dispose());

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            btns.setOpaque(false);
            btns.add(printBtn); btns.add(saveBtn); btns.add(closeBtn);
            root.add(btns, BorderLayout.SOUTH);

            dlg.setContentPane(root);
            dlg.setVisible(true);

        } catch (WriterException ex) {
            JOptionPane.showMessageDialog(owner, "Failed to generate QR tag:\n" + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void printTag(BufferedImage img, JDialog parent) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) graphics;
            double scaleX = pageFormat.getImageableWidth()  / img.getWidth();
            double scaleY = pageFormat.getImageableHeight() / img.getHeight();
            double scale  = Math.min(scaleX, scaleY);
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2.scale(scale, scale);
            g2.drawImage(img, 0, 0, null);
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try { job.print(); }
            catch (PrinterException ex) {
                JOptionPane.showMessageDialog(parent, "Print failed: " + ex.getMessage());
            }
        }
    }

    private static void saveTagPNG(BufferedImage img, String eqNo, JDialog parent) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("FireEq_Tag_" + eqNo.replaceAll("[^a-zA-Z0-9]","_") + ".png"));
        if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
            try {
                ImageIO.write(img, "PNG", fc.getSelectedFile());
                JOptionPane.showMessageDialog(parent,
                    "✅  Tag saved to:\n" + fc.getSelectedFile().getAbsolutePath(),
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent, "Save failed: " + ex.getMessage());
            }
        }
    }

    private static JButton styledBtn(String text, Color bg) {
        if (text.contains("Print")) return UI.makeStyledBtn(text, bg, Icons.print(Icons.MD));
        if (text.contains("Save"))  return UI.makeStyledBtn(text, bg, Icons.save(Icons.MD));
        if (text.contains("Close")) return UI.makeStyledBtn(text, bg, Icons.cancel(Icons.MD));
        return UI.makeStyledBtn(text, bg);
    }
}
