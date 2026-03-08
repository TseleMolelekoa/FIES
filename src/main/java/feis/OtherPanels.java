package feis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// ══════════════════════════════════════════════════════════════════════════
//  FIRE HOSE REEL PANEL
// ══════════════════════════════════════════════════════════════════════════
class HoseReelPanel extends JPanel {

    private final List<Map<String,String>> store;
    private final JTextField    dateField    = UI.dateField();
    private final JComboBox<String> siteBox  = UI.combo("Parsons","Bruce","King","Load Out");
    private final JTextField    areaField    = UI.field(22);
    private final JTextField    deptField    = UI.field(22);
    private final JTextField    eqNoField    = UI.field(14);
    private final JTextField    locationField= UI.field(22);
    private final JTextField    commentsField= UI.field(30);
    private final JTextField    inspectedBy  = UI.field(22);
    private final JTextField    areaResp     = UI.field(22);
    private final JTextField    emsSupervisor= UI.field(22);

    private static final String[] ITEMS = {
        "Hose reel drum intact","Hose intact","Nozzle intact",
        "Seal not broken","Valve complete","Accessibility",
        "Visibility","Signage in place","Signage and Hose Reel clean"
    };
    private final Map<String,JComboBox<String>> checks = new LinkedHashMap<>();

    HoseReelPanel(List<Map<String,String>> store, Runnable refreshRecords) {
        this.store = store;
        setLayout(new BorderLayout(8,8));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(12,14,12,14));
        add(UI.sectionTitle("Fire Hose Reel Checklist  —  Fire Equipment Inspection System"), BorderLayout.NORTH);

        JPanel card = UI.formCard();
        GridBagConstraints g = UI.gbc();
        int row = 0;

        UI.addRow(card, g, row++, "Date *", dateField);
        UI.addRow(card, g, row++, "Site *", siteBox);

        UI.addRowWithScan(card, g, row++, "Equipment No", eqNoField, () ->
            new QRScannerDialog(SwingUtilities.getWindowAncestor(this),
                "Scan Hose Reel Location Tag", result -> {
                    eqNoField.setText(result.startsWith("FEIS-EQ|") ?
                        extractField(result,"No") : result);
                    String loc = extractField(result,"Area");
                    if (!loc.isEmpty()) locationField.setText(loc);
                }).setVisible(true));

        UI.addRowWithScan(card, g, row++, "Location (QR)", locationField, () ->
            new QRScannerDialog(SwingUtilities.getWindowAncestor(this),
                "Scan Location QR", result -> locationField.setText(result)).setVisible(true));

        UI.addRow(card, g, row++, "Area",       areaField);
        UI.addRow(card, g, row++, "Department", deptField);

        g.gridx=0; g.gridy=row++; g.gridwidth=2;
        JLabel div = new JLabel("Checklist Items");
        div.setFont(new Font("Segoe UI",Font.BOLD,30)); div.setForeground(Theme.TEXT_SECONDARY);
        card.add(div,g); g.gridwidth=1;

        for (String item : ITEMS) {
            JComboBox<String> cb = UI.yesNoBox();
            checks.put(item,cb);
            UI.addRow(card,g,row++,item,cb);
        }

        g.gridx=0; g.gridy=row++; g.gridwidth=2;
        card.add(buildCondPanel(), g); g.gridwidth=1;

        UI.addRow(card,g,row++,"Comments",        commentsField);
        UI.addRow(card,g,row++,"Inspected By",    inspectedBy);
        UI.addRow(card,g,row++,"Area Responsible",areaResp);
        UI.addRow(card,g,row,  "EMS Supervisor",  emsSupervisor);

        JScrollPane scroll = new JScrollPane(card);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        JButton saveBtn  = UI.saveBtn("Save Record");
        JButton clearBtn = UI.dangerBtn("Clear");
        saveBtn.addActionListener(e  -> save(refreshRecords));
        clearBtn.addActionListener(e -> clear());
        add(UI.buttonBar(clearBtn,saveBtn), BorderLayout.SOUTH);
    }

    private JPanel buildCondPanel() {
        JPanel p = new JPanel(new BorderLayout(8,0));
        p.setBackground(new Color(240,250,245));
        p.setBorder(new EmptyBorder(8,10,8,10));
        JLabel title = new JLabel("Scan Hose Reel Condition Tag");
        title.setFont(Theme.FONT_HEADING); title.setForeground(Theme.PRIMARY);
        JLabel hint  = new JLabel("Scan QR on hose reel to verify drum/hose/nozzle status");
        hint.setFont(Theme.FONT_SMALL);  hint.setForeground(Theme.TEXT_SECONDARY);
        JButton btn  = new JButton("Scan Condition");
        btn.setBackground(Theme.SUCCESS); btn.setForeground(Color.WHITE);
        btn.setFont(Theme.FONT_HEADING); btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6,14,6,14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e ->
            new QRScannerDialog(SwingUtilities.getWindowAncestor(this),
                "Scan Hose Reel Condition QR", result -> {
                    hint.setText("✅ Scanned: " + result);
                    hint.setForeground(Theme.SUCCESS);
                    commentsField.setText(commentsField.getText() +
                        (commentsField.getText().isEmpty()?"":"|") + "CondQR:" + result);
                    if (result.contains("ALL_OK"))
                        checks.values().forEach(cb -> cb.setSelectedItem("Yes"));
                }).setVisible(true));
        JPanel left = new JPanel(new GridLayout(2,1)); left.setOpaque(false);
        left.add(title); left.add(hint);
        p.add(left,BorderLayout.CENTER); p.add(btn,BorderLayout.EAST);
        return p;
    }

    private String extractField(String raw, String key) {
        for (String part : raw.split("\\|"))
            if (part.startsWith(key+":")) return part.substring(key.length()+1);
        return "";
    }

    private void save(Runnable refresh) {
        Map<String,String> rec = new LinkedHashMap<>();
        rec.put("Type","Fire Hose Reel");
        rec.put("Date",       dateField.getText().trim());
        rec.put("Site",       siteBox.getSelectedItem().toString());
        rec.put("Equipment No", eqNoField.getText().trim());
        rec.put("Location",   locationField.getText().trim());
        rec.put("Area",       areaField.getText().trim());
        rec.put("Department", deptField.getText().trim());
        checks.forEach((k,v) -> rec.put(k, v.getSelectedItem().toString()));
        rec.put("Comments",     commentsField.getText().trim());
        rec.put("Inspected By", inspectedBy.getText().trim());
        store.add(rec);
        refresh.run();
        JOptionPane.showMessageDialog(this,"✅  Hose Reel record saved!","Saved",JOptionPane.INFORMATION_MESSAGE);
    }

    private void clear() {
        dateField.setText(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        areaField.setText(""); deptField.setText(""); eqNoField.setText(""); locationField.setText("");
        commentsField.setText(""); inspectedBy.setText(""); areaResp.setText(""); emsSupervisor.setText("");
        siteBox.setSelectedIndex(0);
        checks.values().forEach(cb -> cb.setSelectedIndex(0));
    }
}


