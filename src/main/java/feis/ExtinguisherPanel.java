package feis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tab panel for the Fire Extinguishers Monthly Checklist (Fire Equipment Inspection System).
 * Includes QR scanning for Equipment Location and Condition check.
 */
public class ExtinguisherPanel extends JPanel {

    private final List<Map<String,String>> store;

    // ── Form fields ──────────────────────────────────────────────────────
    private final JTextField    dateField    = UI.dateField();
    private final JTextField    noField      = UI.field(10);
    private final JComboBox<String> areaBox  = UI.combo(
            "Admin Building","Parsons","Bruce","King","Load Out",
            "Workshop","Crusher","Stockpile","Conveyor","Other");
    private final JTextField    locationField= UI.field(22);   // QR-filled
    private final JTextField    eqTypeField  = UI.field("9kg DCP Fire Extinguisher", 22);
    private final JTextField    expiryField  = UI.field(12);
    private final JTextField    commentsField= UI.field(30);
    private final JTextField    actionsField = UI.field(30);
    private final JComboBox<String> statusBox= UI.combo("Inspected","Not Inspected","Defective","Replaced");
    private final JTextField    inspectedBy  = UI.field(22);

    // ── Checklist ─────────────────────────────────────────────────────────
    private static final String[] ITEMS = {
        "Visibility", "Accessibility", "Signage in place",
        "Bracket intact", "Carry handle intact",
        "Safety pin intact and sealed",
        "Pressure gauge in the green",
        "Discharge hose not cracked/damaged",
        "Information sticker available",
        "Inspect condition of fire extinguisher",
        "Service provider sticker available",
        "Monthly inspection sticker present"
    };
    private final Map<String, JComboBox<String>> checks = new LinkedHashMap<>();

    // ── Condition scan result label ──────────────────────────────────────
    private final JLabel condScanLabel = new JLabel("Not yet scanned");

    public ExtinguisherPanel(List<Map<String,String>> store, Runnable refreshRecords) {
        this.store = store;
        setLayout(new BorderLayout(8, 8));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(12, 14, 12, 14));

        add(UI.sectionTitle("Fire Extinguishers Monthly Checklist  —  Fire Equipment Inspection System"), BorderLayout.NORTH);

        JPanel formCard = UI.formCard();
        GridBagConstraints g = UI.gbc();

        int row = 0;

        // ── Header info ───────────────────────────────────────────────────
        UI.addRow(formCard, g, row++, "Date *",         dateField);

        // Equipment No with QR scan for Location
        UI.addRowWithScan(formCard, g, row++, "Equipment No *", noField, () ->
            new QRScannerDialog(SwingUtilities.getWindowAncestor(this),
                "Scan Equipment No / Location Tag", result -> {
                    // Parse FEIS-EQ|No:XXX|Area:YYY|... format
                    if (result.startsWith("FEIS-EQ|")) {
                        parseAndFillEquipmentTag(result);
                    } else {
                        noField.setText(result);
                    }
                }).setVisible(true));

        UI.addRow(formCard, g, row++, "Area *",         areaBox);

        // Location QR field
        UI.addRowWithScan(formCard, g, row++, "Location (QR)", locationField, () ->
            new QRScannerDialog(SwingUtilities.getWindowAncestor(this),
                "Scan Location QR Code", result -> {
                    locationField.setText(result);
                    JOptionPane.showMessageDialog(
                        SwingUtilities.getWindowAncestor(this),
                        "📍  Location set to:\n" + result, "Location Scanned",
                        JOptionPane.INFORMATION_MESSAGE);
                }).setVisible(true));

        UI.addRow(formCard, g, row++, "Equipment Type", eqTypeField);
        UI.addRow(formCard, g, row++, "Expiry Date",    expiryField);

        // ── Divider ───────────────────────────────────────────────────────
        g.gridx = 0; g.gridy = row++; g.gridwidth = 2;
        JLabel divider = new JLabel("Checklist Items");
        divider.setFont(new Font("Segoe UI", Font.BOLD, 30));
        divider.setForeground(Theme.TEXT_SECONDARY);
        formCard.add(divider, g);
        g.gridwidth = 1;

        // ── Checklist items ───────────────────────────────────────────────
        for (String item : ITEMS) {
            JComboBox<String> cb = UI.yesNoBox();
            checks.put(item, cb);
            UI.addRow(formCard, g, row++, item, cb);
        }

        // ── Condition scan row ─────────────────────────────────────────────
        g.gridx = 0; g.gridy = row++; g.gridwidth = 2;
        JPanel condPanel = buildConditionScanPanel();
        formCard.add(condPanel, g);
        g.gridwidth = 1;

        // ── Footer fields ─────────────────────────────────────────────────
        UI.addRow(formCard, g, row++, "Comments",          commentsField);
        UI.addRow(formCard, g, row++, "Actions Required",  actionsField);
        UI.addRow(formCard, g, row++, "Status",            statusBox);
        UI.addRow(formCard, g, row++, "Inspected By *",    inspectedBy);

        // ── QR Tag generator button ───────────────────────────────────────
        g.gridx = 0; g.gridy = row; g.gridwidth = 2;
        JButton tagBtn = UI.accentBtn("Generate & Print QR Tag");
        tagBtn.addActionListener(e -> generateTag());
        formCard.add(tagBtn, g);

        JScrollPane scroll = new JScrollPane(formCard);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // ── Action buttons ────────────────────────────────────────────────
        JButton saveBtn  = UI.saveBtn("Save Record");
        JButton clearBtn = UI.dangerBtn("Clear Form");

        saveBtn.addActionListener(e  -> save(refreshRecords));
        clearBtn.addActionListener(e -> clear());

        add(UI.buttonBar(clearBtn, saveBtn), BorderLayout.SOUTH);
    }

    // ── Condition scan mini-panel ─────────────────────────────────────────
    private JPanel buildConditionScanPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setBackground(new Color(240, 247, 255));
        p.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel title = new JLabel("Condition QR Scan");
        title.setFont(Theme.FONT_HEADING);
        title.setForeground(Theme.PRIMARY);

        condScanLabel.setFont(Theme.FONT_SMALL);
        condScanLabel.setForeground(Theme.TEXT_SECONDARY);

        JButton scanCondBtn = new JButton("Scan Condition Tag");
        scanCondBtn.setBackground(Theme.PRIMARY_LIGHT);
        scanCondBtn.setForeground(Color.WHITE);
        scanCondBtn.setFont(Theme.FONT_HEADING);
        scanCondBtn.setFocusPainted(false);
        scanCondBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
        scanCondBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        scanCondBtn.addActionListener(e -> scanCondition());

        JPanel left  = new JPanel(new GridLayout(2, 1)); left.setOpaque(false);
        left.add(title); left.add(condScanLabel);

        p.add(left, BorderLayout.CENTER);
        p.add(scanCondBtn, BorderLayout.EAST);
        return p;
    }

    // ── Scan condition QR ─────────────────────────────────────────────────
    private void scanCondition() {
        new QRScannerDialog(SwingUtilities.getWindowAncestor(this),
            "Scan Condition / Inspection QR", result -> {
                condScanLabel.setText("Scanned: " + result);
                condScanLabel.setForeground(Theme.SUCCESS);
                // Parse FEIS-COND|No:XXX|CHECK or custom condition codes
                if (result.contains("|")) {
                    String[] parts = result.split("\\|");
                    for (String part : parts) {
                        if (part.startsWith("No:"))
                            noField.setText(part.substring(3));
                        if (part.startsWith("COND:")) {
                            String cond = part.substring(5);
                            // Auto-fill Yes/No based on encoded condition
                            autoFillCondition(cond);
                        }
                    }
                }
                commentsField.setText(commentsField.getText() +
                    (commentsField.getText().isEmpty()?"":"  |  ") +
                    "Condition QR scanned: " + result);
            }).setVisible(true);
    }

    /** Auto-populates checklist from a condition code like "ALL_OK" or "HOSE_DAMAGED" */
    private void autoFillCondition(String code) {
        String upper = code.toUpperCase();
        if (upper.equals("ALL_OK")) {
            checks.values().forEach(cb -> cb.setSelectedItem("Yes"));
        } else if (upper.equals("HOSE_DAMAGED")) {
            checks.values().forEach(cb -> cb.setSelectedItem("Yes"));
            checks.get("Discharge hose not cracked/damaged").setSelectedItem("No");
            actionsField.setText("Replace discharge hose immediately.");
        } else if (upper.equals("PIN_MISSING")) {
            checks.values().forEach(cb -> cb.setSelectedItem("Yes"));
            checks.get("Safety pin intact and sealed").setSelectedItem("No");
            actionsField.setText("Replace safety pin and seal.");
        } else if (upper.equals("LOW_PRESSURE")) {
            checks.values().forEach(cb -> cb.setSelectedItem("Yes"));
            checks.get("Pressure gauge in the green").setSelectedItem("No");
            actionsField.setText("Service required – pressure below spec.");
        } else {
            commentsField.setText("Condition code: " + code);
        }
    }

    /** Parse equipment tag QR and auto-fill fields */
    private void parseAndFillEquipmentTag(String raw) {
        for (String part : raw.split("\\|")) {
            if (part.startsWith("No:"))   noField.setText(part.substring(3));
            if (part.startsWith("Area:")) locationField.setText(part.substring(5));
            if (part.startsWith("Type:")) eqTypeField.setText(part.substring(5));
            if (part.startsWith("Site:")) {
                String site = part.substring(5);
                for (int i = 0; i < areaBox.getItemCount(); i++)
                    if (areaBox.getItemAt(i).equalsIgnoreCase(site)) {
                        areaBox.setSelectedIndex(i); break; }
            }
        }
    }

    private void generateTag() {
        if (noField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,"Enter an Equipment No first.","Required",JOptionPane.WARNING_MESSAGE);
            return;
        }
        QRCodeGenerator.showTagDialog(
            SwingUtilities.getWindowAncestor(this),
            noField.getText().trim(),
            areaBox.getSelectedItem().toString() + " " + locationField.getText().trim(),
            eqTypeField.getText().trim(),
            "Fire Equipment Inspection System",
            "Exp: " + expiryField.getText().trim());
    }

    private void save(Runnable refresh) {
        if (dateField.getText().trim().isEmpty() || noField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,"Date and Equipment No are required.","Validation",JOptionPane.WARNING_MESSAGE);
            return;
        }
        Map<String,String> rec = new LinkedHashMap<>();
        rec.put("Type","Fire Extinguisher");
        rec.put("Date",          dateField.getText().trim());
        rec.put("No",            noField.getText().trim());
        rec.put("Area",          areaBox.getSelectedItem().toString());
        rec.put("Location",      locationField.getText().trim());
        rec.put("Equipment Type",eqTypeField.getText().trim());
        checks.forEach((k,v) -> rec.put(k, v.getSelectedItem().toString()));
        rec.put("Expiry Date",      expiryField.getText().trim());
        rec.put("Comments",         commentsField.getText().trim());
        rec.put("Actions Required", actionsField.getText().trim());
        rec.put("Status",           statusBox.getSelectedItem().toString());
        rec.put("Inspected By",     inspectedBy.getText().trim());
        rec.put("Condition QR",     condScanLabel.getText());
        store.add(rec);
        refresh.run();
        JOptionPane.showMessageDialog(this,"✅  Fire Extinguisher record saved!","Saved",JOptionPane.INFORMATION_MESSAGE);
    }

    private void clear() {
        dateField.setText(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        noField.setText(""); locationField.setText(""); expiryField.setText("");
        commentsField.setText(""); actionsField.setText(""); inspectedBy.setText("");
        eqTypeField.setText("9kg DCP Fire Extinguisher");
        areaBox.setSelectedIndex(0); statusBox.setSelectedIndex(0);
        checks.values().forEach(cb -> cb.setSelectedIndex(0));
        condScanLabel.setText("Not yet scanned");
        condScanLabel.setForeground(Theme.TEXT_SECONDARY);
    }
}
