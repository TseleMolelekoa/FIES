package feis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Records tab: shows all saved inspections in a colour-coded table.
 * Supports CSV export and record deletion.
 */
public class RecordsPanel extends JPanel {

    private final List<Map<String,String>> extRecords;
    private final List<Map<String,String>> delugeRecords;
    private final List<Map<String,String>> hoseRecords;
    private final List<Map<String,String>> firstAidRecords;

    private final DefaultTableModel model;
    private final JTable            table;
    private final JLabel            countLabel = new JLabel("No records yet.");
    private final JLabel            issueLabel = new JLabel();

    public RecordsPanel(List<Map<String,String>> ext,
                        List<Map<String,String>> deluge,
                        List<Map<String,String>> hose,
                        List<Map<String,String>> firstAid) {
        this.extRecords     = ext;
        this.delugeRecords  = deluge;
        this.hoseRecords    = hose;
        this.firstAidRecords= firstAid;

        setLayout(new BorderLayout(8,8));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(12,14,12,14));

        // ── Title ─────────────────────────────────────────────────────────
        add(UI.sectionTitle("All Inspection Records"), BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────
        String[] cols = {"#","Type","Date","Site / Location","Equipment No",
                         "Issues Found","Status","Inspected By"};
        model = new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setFont(Theme.FONT_BODY);
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0,1));
        table.setSelectionBackground(new Color(180,215,255));
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.getTableHeader().setFont(Theme.FONT_HEADING);
        table.getTableHeader().setBackground(Theme.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0,34));
        table.setDefaultRenderer(Object.class, UI.altRowRenderer());

        // Column widths
        int[] widths = {30,160,90,160,120,110,100,140};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(0,0,0,0));
        add(scroll, BorderLayout.CENTER);

        // ── Stats bar ──────────────────────────────────────────────────────
        JPanel stats = new JPanel(new BorderLayout(8,0));
        stats.setBackground(new Color(235,240,248));
        stats.setBorder(new EmptyBorder(6,10,6,10));

        countLabel.setFont(Theme.FONT_SMALL);
        countLabel.setForeground(Theme.TEXT_SECONDARY);

        issueLabel.setFont(new Font("Segoe UI",Font.BOLD,11));
        issueLabel.setForeground(Theme.DANGER);

        JPanel statsLeft = new JPanel(new FlowLayout(FlowLayout.LEFT,12,0));
        statsLeft.setOpaque(false);
        statsLeft.add(countLabel);
        statsLeft.add(issueLabel);
        stats.add(statsLeft, BorderLayout.CENTER);

        // ── Buttons ───────────────────────────────────────────────────────
        JButton refreshBtn = UI.refreshBtn("Refresh");
        JButton exportBtn  = UI.exportBtn("Export CSV");
        JButton deleteBtn  = UI.dangerBtn("Delete");
        JButton printBtn   = UI.printBtn("Print Summary");

        refreshBtn.addActionListener(e -> refresh());
        exportBtn.addActionListener(e  -> exportCSV());
        deleteBtn.addActionListener(e  -> deleteSelected());
        printBtn.addActionListener(e   -> printSummary());

        JPanel btns = UI.buttonBar(deleteBtn, printBtn, exportBtn, refreshBtn);
        btns.setBackground(new Color(235,240,248));

        stats.add(btns, BorderLayout.EAST);
        add(stats, BorderLayout.SOUTH);
    }

    // ═════════════════════════════════════════════════════════════════════
    public void refresh() {
        model.setRowCount(0);
        List<Map<String,String>> all = getAllRecords();
        long totalIssues = 0;
        int i = 1;
        for (Map<String,String> rec : all) {
            long noCount = rec.values().stream().filter("No"::equals).count();
            totalIssues += noCount;
            String issueStr = noCount > 0 ? "⚠ " + noCount + " item(s)" : "—";
            String status   = rec.getOrDefault("Status",
                              noCount > 0 ? "Action Required" : "OK");
            String loc  = rec.getOrDefault("Location","");
            String site = rec.getOrDefault("Site","");
            String area = rec.getOrDefault("Area","");
            String location;
            if (!site.isBlank())      location = site;
            else if (!area.isBlank()) location = area;
            else if (!loc.isBlank())  location = loc;
            else                      location = "—";

            model.addRow(new Object[]{
                i++,
                rec.getOrDefault("Type","—"),
                rec.getOrDefault("Date","—"),
                location,
                rec.getOrDefault("No", rec.getOrDefault("Equipment No","—")),
                issueStr,
                status,
                rec.getOrDefault("Inspected By","—")
            });
        }
        int total = model.getRowCount();
        countLabel.setText(total + " record(s)  |  Total inspections this session");
        if (totalIssues > 0)
            issueLabel.setText("⚠  " + totalIssues + " checklist issue(s) require attention");
        else if (total > 0)
            issueLabel.setText("✅  All inspections passed");
        else
            issueLabel.setText("");
    }

    private List<Map<String,String>> getAllRecords() {
        List<Map<String,String>> all = new ArrayList<>();
        all.addAll(extRecords); all.addAll(delugeRecords);
        all.addAll(hoseRecords); all.addAll(firstAidRecords);
        return all;
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this,"Select a row first."); return; }
        if (JOptionPane.showConfirmDialog(this,"Delete this record?","Confirm",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        String type = table.getValueAt(row,1).toString();
        List<Map<String,String>> target;
        if ("Fire Extinguisher".equals(type))       target = extRecords;
        else if ("Fire Deluge System".equals(type)) target = delugeRecords;
        else if ("Fire Hose Reel".equals(type))     target = hoseRecords;
        else                                         target = firstAidRecords;
        if (!target.isEmpty()) target.remove(target.size()-1);
        refresh();
    }

    private void exportCSV() {
        List<Map<String,String>> all = getAllRecords();
        if (all.isEmpty()) { JOptionPane.showMessageDialog(this,"No records to export."); return; }
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("FireEquipment_Inspection_" +
            new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(fc.getSelectedFile()))) {
            Set<String> keys = new LinkedHashSet<>();
            all.forEach(r -> keys.addAll(r.keySet()));
            pw.println(String.join(",", keys));
            for (Map<String,String> rec : all) {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (String k : keys) {
                    if (!first) sb.append(",");
                    String v = rec.getOrDefault(k,"");
                    if (v.contains(",") || v.contains("\""))
                        v = "\"" + v.replace("\"","\"\"") + "\"";
                    sb.append(v); first = false;
                }
                pw.println(sb);
            }
            JOptionPane.showMessageDialog(this,
                "✅  Exported:\n" + fc.getSelectedFile().getAbsolutePath(),
                "Exported", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,"Export failed:\n"+ex.getMessage(),
                "Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("FIRE EQUIPMENT INSPECTION SUMMARY\n");
        sb.append("Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())).append("\n");
        sb.append("=".repeat(60)).append("\n\n");
        for (int r = 0; r < model.getRowCount(); r++) {
            sb.append(String.format("%-4s %-22s %-12s %-18s %-12s %-14s\n",
                model.getValueAt(r,0), model.getValueAt(r,1),
                model.getValueAt(r,2), model.getValueAt(r,3),
                model.getValueAt(r,4), model.getValueAt(r,5)));
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setFont(Theme.FONT_MONO);
        ta.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(ta),
            "Print Preview", JOptionPane.PLAIN_MESSAGE);
    }

}
