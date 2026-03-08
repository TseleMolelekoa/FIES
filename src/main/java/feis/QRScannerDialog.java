package feis;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * QR Scanner Dialog — three tabs:
 *   📷 Camera  : live webcam feed with continuous QR detection
 *   🖼 Upload  : drag-and-drop / file picker image → ZXing decode
 *   ⌨ Manual   : free-text entry with format examples
 */
public class QRScannerDialog extends JDialog {

    // ── Colours ───────────────────────────────────────────────────────────
    private static final Color BG_DARK    = new Color(18, 32, 58);
    private static final Color BG_CARD    = new Color(26, 46, 82);
    private static final Color BORDER_CLR = new Color(55, 95, 160);
    private static final Color TEXT_LIGHT = new Color(190, 215, 255);
    private static final Color TEXT_DIM   = new Color(110, 145, 200);

    // ── Constants ─────────────────────────────────────────────────────────
    private static final String MANUAL_PLACEHOLDER = "Type or paste value here…";

    // ── State ─────────────────────────────────────────────────────────────
    private final Consumer<String> onScanned;
    private final String           purpose;

    // Camera
    private Webcam        activeWebcam    = null;
    private WebcamPanel   webcamPanel     = null;
    private Thread        scanThread      = null;
    private final AtomicBoolean scanning  = new AtomicBoolean(false);
    private final AtomicBoolean resultFound = new AtomicBoolean(false);

    // Upload
    private BufferedImage loadedImage     = null;
    private boolean       dropHovered     = false;
    private boolean       previewSwapped  = false;

    // Widgets shared across tabs
    private final JLabel  camStatusLbl   = centreLabel(" ", TEXT_DIM, 11);
    private final JLabel  uploadStatusLbl = centreLabel("Drop or upload a QR image to begin", TEXT_DIM, 11);
    private final JButton decodeBtn      = styledBtn("  Decode QR",        Theme.SUCCESS,       Icons.decode(Icons.MD));
    private final JButton uploadBtn      = styledBtn("  Upload Image",     Theme.PRIMARY_LIGHT, Icons.folderOpen(Icons.MD));
    private final JLabel  previewHolder  = new JLabel();
    private final JTextField manualField = new JTextField();

    // ═════════════════════════════════════════════════════════════════════
    public QRScannerDialog(Window owner, String purpose, Consumer<String> onScanned) {
        super(owner, "QR Scanner  —  " + purpose, ModalityType.APPLICATION_MODAL);
        this.purpose   = purpose;
        this.onScanned = onScanned;
        buildUI();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { stopCamera(); }
        });
        pack();
        setMinimumSize(new Dimension(760, 500));
        setLocationRelativeTo(owner);
    }

    // ═════════════════════════════════════════════════════════════════════
    //  ROOT
    // ═════════════════════════════════════════════════════════════════════
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DARK);
        root.add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(BG_DARK);
        tabs.setForeground(TEXT_LIGHT);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabs.setBorder(new EmptyBorder(4, 8, 0, 8));

        tabs.addTab(null, Icons.camera(16), buildCameraTab(),  "Scan using your webcam");
        tabs.addTab(null, Icons.folderOpen(16), buildUploadTab(), "Upload or drag a QR image");
        tabs.addTab(null, Icons.keyboard(16), buildManualTab(), "Enter the value manually");

        // Tab titles with icons
        tabs.setTitleAt(0, "  Camera  ");
        tabs.setTitleAt(1, "  Upload  ");
        tabs.setTitleAt(2, "  Manual  ");

        // Start/stop camera when switching tabs
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 0) startCamera();
            else stopCamera();
        });

        root.add(tabs, BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);
        setContentPane(root);

        // Auto-start camera on first open
        SwingUtilities.invokeLater(this::startCamera);
    }

    // ── Header ─────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(new Color(12, 28, 60));
        h.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 2, 0, BORDER_CLR),
                new EmptyBorder(12, 18, 12, 18)));
        JLabel title = new JLabel("  " + purpose);
        title.setIcon(Icons.qrScan(18));
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Theme.ACCENT_LIGHT);
        JLabel sub = new JLabel("Camera · Upload · Manual");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(TEXT_DIM);
        h.add(title, BorderLayout.WEST);
        h.add(sub,   BorderLayout.EAST);
        return h;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  TAB 1 — CAMERA
    // ═════════════════════════════════════════════════════════════════════
    private JPanel buildCameraTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        // Camera viewport placeholder
        JPanel viewport = new JPanel(new BorderLayout());
        viewport.setBackground(new Color(8, 18, 38));
        viewport.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CLR, 2, true),
                new EmptyBorder(0, 0, 0, 0)));
        viewport.setPreferredSize(new Dimension(460, 310));

        // Default state label
        JLabel noCamera = new JLabel("<html><center>"
                + "<b style='color:#aad4ff'>Initialising camera…</b><br><br>"
                + "<span style='color:#7799bb'>Point camera at QR code — auto-detects</span>"
                + "</center></html>", Icons.camera(40), SwingConstants.CENTER);
        noCamera.setHorizontalTextPosition(SwingConstants.CENTER);
        noCamera.setVerticalTextPosition(SwingConstants.BOTTOM);
        noCamera.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noCamera.setName("no-camera-label");
        viewport.add(noCamera, BorderLayout.CENTER);
        viewport.setName("camera-viewport");

        // Camera selector row
        List<Webcam> cams = new ArrayList<>();
        try { cams = Webcam.getWebcams(2000); } catch (Exception ignored) {}

        JPanel controlRow = new JPanel(new BorderLayout(8, 0));
        controlRow.setOpaque(false);

        if (!cams.isEmpty()) {
            String[] camNames = cams.stream().map(Webcam::getName).toArray(String[]::new);
            JComboBox<String> camBox = new JComboBox<>(camNames);
            camBox.setFont(Theme.FONT_BODY);
            camBox.setBackground(BG_CARD);
            camBox.setForeground(TEXT_LIGHT);
            List<Webcam> finalCams = cams;
            camBox.addActionListener(e -> {
                stopCamera();
                int idx = camBox.getSelectedIndex();
                if (idx >= 0 && idx < finalCams.size()) {
                    activeWebcam = finalCams.get(idx);
                    startCameraInViewport(viewport, activeWebcam);
                }
            });
            JLabel camLbl = new JLabel("Camera: ");
            camLbl.setFont(Theme.FONT_BODY);
            camLbl.setForeground(TEXT_DIM);
            controlRow.add(camLbl, BorderLayout.WEST);
            controlRow.add(camBox, BorderLayout.CENTER);
        }

        JButton torchBtn = styledBtn("  Torch", new Color(100, 80, 10), Icons.wrench(Icons.MD));
        torchBtn.setToolTipText("Toggle torch / flash (if supported)");
        torchBtn.addActionListener(e -> toggleTorch());
        controlRow.add(torchBtn, BorderLayout.EAST);

        // Status
        camStatusLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));

        panel.add(viewport,    BorderLayout.CENTER);
        panel.add(controlRow,  BorderLayout.NORTH);
        panel.add(camStatusLbl, BorderLayout.SOUTH);

        // Store viewport ref for later use
        panel.setName("camera-tab-panel");

        return panel;
    }

    // ── Start camera & scanning loop ──────────────────────────────────────
    private void startCamera() {
        SwingUtilities.invokeLater(() -> setCamStatus("🔍  Searching for cameras…", TEXT_DIM));
        new Thread(() -> {
            try {
                List<Webcam> cams = Webcam.getWebcams(3000);
                if (cams.isEmpty()) {
                    SwingUtilities.invokeLater(() ->
                            setCamStatus("⚠  No camera found. Use Upload or Manual tab.", Theme.WARNING));
                    return;
                }
                activeWebcam = cams.get(0);
                SwingUtilities.invokeLater(() -> {
                    // Find the viewport and inject webcam panel
                    JPanel foundViewport = findNamedPanel(getContentPane(), "camera-viewport");
                    if (foundViewport != null) startCameraInViewport(foundViewport, activeWebcam);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        setCamStatus("⚠  Camera init failed: " + ex.getMessage(), Theme.DANGER));
            }
        }, "cam-init").start();
    }

    private void startCameraInViewport(JPanel viewport, Webcam cam) {
        stopCamera();
        try {
            cam.setViewSize(WebcamResolution.VGA.getSize());
            cam.open();
            webcamPanel = new WebcamPanel(cam, false);
            webcamPanel.setFPSDisplayed(false);
            webcamPanel.setDisplayDebugInfo(false);
            webcamPanel.setImageSizeDisplayed(false);
            webcamPanel.setMirrored(true);
            webcamPanel.setBackground(new Color(8, 18, 38));

            viewport.removeAll();
            viewport.add(webcamPanel, BorderLayout.CENTER);
            viewport.revalidate();
            viewport.repaint();

            setCamStatus("📷  Camera active — point at QR code to scan automatically", Theme.SUCCESS);
            startScanLoop(cam);
        } catch (Exception ex) {
            setCamStatus("⚠  Cannot open camera: " + ex.getMessage(), Theme.DANGER);
        }
    }

    private void startScanLoop(Webcam cam) {
        scanning.set(true);
        resultFound.set(false);
        scanThread = new Thread(() -> {
            while (scanning.get() && !resultFound.get()) {
                try {
                    Thread.sleep(200); // ~5 fps decode attempts
                    if (!cam.isOpen()) break;
                    BufferedImage frame = cam.getImage();
                    if (frame == null) continue;
                    String result = tryDecode(frame);
                    if (result == null) result = tryDecode(toGrayscale(frame));
                    if (result != null && !result.isEmpty()) {
                        resultFound.set(true);
                        scanning.set(false);
                        final String found = result;
                        SwingUtilities.invokeLater(() -> onQRFound(found));
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ignored) {}
            }
        }, "qr-scan-loop");
        scanThread.setDaemon(true);
        scanThread.start();
    }

    private void onQRFound(String result) {
        setCamStatus("✅  QR detected: " + result, Theme.SUCCESS);
        stopCamera();
        // Flash green overlay briefly then accept
        Timer timer = new Timer(600, e -> {
            onScanned.accept(result);
            dispose();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void stopCamera() {
        scanning.set(false);
        if (scanThread != null) { scanThread.interrupt(); scanThread = null; }
        if (webcamPanel != null) { webcamPanel.stop(); webcamPanel = null; }
        if (activeWebcam != null && activeWebcam.isOpen()) {
            try { activeWebcam.close(); } catch (Exception ignored) {}
        }
    }

    private void toggleTorch() {
        // Torch not universally supported — show info
        JOptionPane.showMessageDialog(this,
                "Torch/flash control is not supported on all cameras.\n"
                        + "If your camera supports it, try adjusting brightness in your OS camera settings.",
                "Torch", JOptionPane.INFORMATION_MESSAGE);
    }

    // ═════════════════════════════════════════════════════════════════════
    //  TAB 2 — UPLOAD
    // ═════════════════════════════════════════════════════════════════════
    private JPanel buildUploadTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(12, 14, 12, 14));

        JPanel dropZone = buildDropZone();

        uploadStatusLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));

        decodeBtn.setEnabled(false);
        uploadBtn.addActionListener(e -> pickImageFile());
        decodeBtn.addActionListener(e -> decodeLoadedImage());

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(uploadBtn);
        btnRow.add(decodeBtn);

        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setOpaque(false);
        bottom.add(uploadStatusLbl, BorderLayout.NORTH);
        bottom.add(btnRow,          BorderLayout.SOUTH);

        panel.add(dropZone, BorderLayout.CENTER);
        panel.add(bottom,   BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildDropZone() {
        JPanel zone = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(dropHovered ? new Color(30, 70, 140) : BG_CARD);
                g2.fill(new RoundRectangle2D.Float(2, 2, w-4, h-4, 16, 16));
                float[] dash = {8f, 5f};
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND, 1f, dash, 0f));
                g2.setColor(dropHovered ? Theme.ACCENT_LIGHT : BORDER_CLR);
                g2.draw(new RoundRectangle2D.Float(2, 2, w-4, h-4, 16, 16));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        zone.setOpaque(false);
        zone.setPreferredSize(new Dimension(460, 290));
        zone.setBorder(new EmptyBorder(10, 10, 10, 10));
        zone.setName("drop-zone");

        // Placeholder
        JPanel placeholder = new JPanel(new BorderLayout(0, 10));
        placeholder.setOpaque(false);
        placeholder.setName("drop-placeholder");
        JLabel bigIcon = new JLabel(Icons.qrScan(52));
        bigIcon.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel h1 = centreLabel("Drag & Drop QR image here", TEXT_LIGHT, 13);
        h1.setFont(h1.getFont().deriveFont(Font.BOLD));
        JLabel h2 = centreLabel("or click Upload Image below", TEXT_DIM, 11);
        JLabel h3 = centreLabel("JPG · PNG · BMP · GIF", new Color(80, 115, 170), 10);
        JPanel hints = new JPanel(new GridLayout(3, 1, 0, 4));
        hints.setOpaque(false);
        hints.add(h1); hints.add(h2); hints.add(h3);
        placeholder.add(bigIcon, BorderLayout.CENTER);
        placeholder.add(hints,   BorderLayout.SOUTH);
        zone.add(placeholder, BorderLayout.CENTER);

        // Preview label (swapped in after load)
        previewHolder.setHorizontalAlignment(SwingConstants.CENTER);
        previewHolder.setVerticalAlignment(SwingConstants.CENTER);

        // Drag and drop
        new DropTarget(zone, new DropTargetAdapter() {
            @Override public void dragEnter(DropTargetDragEvent e) { dropHovered = true;  zone.repaint(); }
            @Override public void dragExit(DropTargetEvent e)      { dropHovered = false; zone.repaint(); }
            @Override public void drop(DropTargetDropEvent e) {
                dropHovered = false; zone.repaint();
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY);
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>)
                            e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) loadImageFile(files.get(0), zone);
                    e.dropComplete(true);
                } catch (Exception ex) {
                    e.dropComplete(false);
                    setUploadStatus("⚠  Drop failed: " + ex.getMessage(), Theme.DANGER);
                }
            }
        });

        zone.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        zone.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (loadedImage == null) pickImageFile();
            }
        });
        return zone;
    }

    private void pickImageFile() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select QR Code Image");
        fc.setFileFilter(new FileNameExtensionFilter(
                "Images (JPG, PNG, BMP, GIF)", "jpg","jpeg","png","bmp","gif"));
        fc.setAcceptAllFileFilterUsed(false);
        for (String p : new String[]{
                System.getProperty("user.home") + "\\Downloads",
                System.getProperty("user.home") + "\\Desktop",
                System.getProperty("user.home")}) {
            File d = new File(p);
            if (d.exists()) { fc.setCurrentDirectory(d); break; }
        }
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            loadImageFile(fc.getSelectedFile(), findNamedPanel(getContentPane(), "drop-zone"));
    }

    private void loadImageFile(File file, JPanel zone) {
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) { setUploadStatus("⚠  Cannot read that image.", Theme.DANGER); return; }
            loadedImage = img;
            Image scaled = img.getScaledInstance(440, 270, Image.SCALE_SMOOTH);
            previewHolder.setIcon(new ImageIcon(scaled));
            // Swap placeholder with preview in drop zone
            if (zone != null && !previewSwapped) {
                JPanel ph = findNamedPanel(zone, "drop-placeholder");
                if (ph != null) zone.remove(ph);
                zone.add(previewHolder, BorderLayout.CENTER);
                zone.revalidate(); zone.repaint();
                previewSwapped = true;
            }
            decodeBtn.setEnabled(true);
            setUploadStatus("✅  " + file.getName() + " loaded — click Decode QR", Theme.SUCCESS);
        } catch (Exception ex) {
            setUploadStatus("⚠  " + ex.getMessage(), Theme.DANGER);
        }
    }

    private void decodeLoadedImage() {
        if (loadedImage == null) { setUploadStatus("⚠  Load an image first.", Theme.WARNING); return; }
        setUploadStatus("🔍  Scanning for QR code…", new Color(120, 180, 255));
        decodeBtn.setEnabled(false);
        SwingWorker<String, Void> w = new SwingWorker<>() {
            @Override protected String doInBackground() {
                String r = tryDecode(loadedImage);
                if (r == null) r = tryDecode(toGrayscale(loadedImage));
                if (r == null) r = tryDecode(toHighContrast(loadedImage));
                return r;
            }
            @Override protected void done() {
                try {
                    String result = get();
                    if (result != null) {
                        setUploadStatus("✅  Decoded: " + result, Theme.SUCCESS);
                        final String finalResult = result;
                        Timer timer = new Timer(600, e -> {
                            onScanned.accept(finalResult);
                            dispose();
                        });
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        setUploadStatus("❌  No QR found. Try a clearer photo.", Theme.DANGER);
                        decodeBtn.setEnabled(true);
                    }
                } catch (Exception ex) {
                    setUploadStatus("⚠  " + ex.getMessage(), Theme.DANGER);
                    decodeBtn.setEnabled(true);
                }
            }
        };
        w.execute();
    }

    // ═════════════════════════════════════════════════════════════════════
    //  TAB 3 — MANUAL
    // ═════════════════════════════════════════════════════════════════════
    private JPanel buildManualTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel heading = new JLabel("  Type or paste the equipment value");
        heading.setIcon(Icons.keyboard(16));
        heading.setFont(new Font("Segoe UI", Font.BOLD, 13));
        heading.setForeground(TEXT_LIGHT);

        // Examples card
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(16, 16, 16, 16)));

        JLabel examples = new JLabel("<html>"
                + "<span style='color:#aaccff'><b>Accepted formats:</b></span><br><br>"
                + "<span style='color:#8899bb'>Simple ID:</span><br>"
                + "<tt style='color:#ffdd88; font-size:11'>  KM-SE094</tt><br><br>"
                + "<span style='color:#8899bb'>Equipment tag:</span><br>"
                + "<tt style='color:#ffdd88; font-size:11'>  FEIS-EQ|No:KM-SE094|Area:Admin Building|Type:9kg DCP</tt><br><br>"
                + "<span style='color:#8899bb'>Free text location:</span><br>"
                + "<tt style='color:#ffdd88; font-size:11'>  Parsons Conveyor East - Bay 3</tt>"
                + "</html>");
        examples.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Input
        manualField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        manualField.setBackground(new Color(14, 28, 55));
        manualField.setForeground(Color.WHITE);
        manualField.setCaretColor(Theme.ACCENT_LIGHT);
        manualField.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CLR, 1, true),
                new EmptyBorder(10, 12, 10, 12)));

        // Placeholder handling
        manualField.setForeground(TEXT_DIM);
        manualField.setText(MANUAL_PLACEHOLDER);
        manualField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (manualField.getText().equals(MANUAL_PLACEHOLDER)) {
                    manualField.setText("");
                    manualField.setForeground(Color.WHITE);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (manualField.getText().isEmpty()) {
                    manualField.setForeground(TEXT_DIM);
                    manualField.setText(MANUAL_PLACEHOLDER);
                }
            }
        });
        manualField.addActionListener(e -> submitManual());

        JButton submitBtn = styledBtn("  Use This Value", Theme.SUCCESS, Icons.checkCircleWhite(Icons.MD));
        submitBtn.addActionListener(e -> submitManual());

        JLabel hint = centreLabel("↵  Press Enter or click the button", TEXT_DIM, 10);

        card.add(examples,  BorderLayout.CENTER);
        card.add(manualField, BorderLayout.SOUTH);

        JPanel btnArea = new JPanel(new BorderLayout(0, 6));
        btnArea.setOpaque(false);
        btnArea.add(hint,      BorderLayout.NORTH);
        btnArea.add(submitBtn, BorderLayout.SOUTH);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(card,    BorderLayout.CENTER);
        panel.add(btnArea, BorderLayout.SOUTH);
        return panel;
    }

    private void submitManual() {
        String val = manualField.getText().trim();
        if (val.isEmpty() || val.equals(MANUAL_PLACEHOLDER)) {
            manualField.setBorder(new CompoundBorder(
                    new LineBorder(Theme.DANGER, 2, true),
                    new EmptyBorder(10, 12, 10, 12)));
            manualField.requestFocus();
            return;
        }
        onScanned.accept(val);
        dispose();
    }

    // ═════════════════════════════════════════════════════════════════════
    //  FOOTER
    // ═════════════════════════════════════════════════════════════════════
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setBackground(new Color(12, 28, 60));
        footer.setBorder(new CompoundBorder(
                new MatteBorder(2, 0, 0, 0, BORDER_CLR),
                new EmptyBorder(10, 16, 10, 16)));
        JLabel tip = new JLabel("💡  Camera tab auto-detects QR codes — no button needed. Upload tab supports drag-and-drop.");
        tip.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        tip.setForeground(TEXT_DIM);
        JButton cancelBtn = styledBtn("  Close", Theme.DANGER, Icons.cancel(Icons.MD));
        cancelBtn.addActionListener(e -> { stopCamera(); dispose(); });
        footer.add(tip,       BorderLayout.CENTER);
        footer.add(cancelBtn, BorderLayout.EAST);
        return footer;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  ZXing DECODE HELPERS
    // ═════════════════════════════════════════════════════════════════════
    private String tryDecode(BufferedImage img) {
        try {
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.ALSO_INVERTED, Boolean.TRUE);
            LuminanceSource src = new BufferedImageLuminanceSource(img);
            BinaryBitmap bmp   = new BinaryBitmap(new HybridBinarizer(src));
            return new MultiFormatReader().decode(bmp, hints).getText();
        } catch (NotFoundException e) { return null; }
    }

    private BufferedImage toGrayscale(BufferedImage src) {
        BufferedImage gs = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gs.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return gs;
    }

    private BufferedImage toHighContrast(BufferedImage src) {
        BufferedImage gs = toGrayscale(src);
        for (int y = 0; y < gs.getHeight(); y++)
            for (int x = 0; x < gs.getWidth(); x++)
                gs.setRGB(x, y, (gs.getRGB(x, y) & 0xFF) < 128 ? 0xFF000000 : 0xFFFFFFFF);
        return gs;
    }

    // ═════════════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ═════════════════════════════════════════════════════════════════════
    private void setCamStatus(String msg, Color color) {
        SwingUtilities.invokeLater(() -> {
            camStatusLbl.setText(msg);
            camStatusLbl.setForeground(color);
        });
    }

    private void setUploadStatus(String msg, Color color) {
        SwingUtilities.invokeLater(() -> {
            uploadStatusLbl.setText(msg);
            uploadStatusLbl.setForeground(color);
        });
    }

    private static JLabel centreLabel(String text, Color color, int size) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.PLAIN, size));
        l.setForeground(color);
        return l;
    }

    private static JButton styledBtn(String text, Color bg, org.kordamp.ikonli.swing.FontIcon icon) {
        return UI.makeStyledBtn(text, bg, icon);
    }

    /** Walk the container tree and find a panel by its name */
    private static JPanel findNamedPanel(Container root, String name) {
        for (Component c : root.getComponents()) {
            if (c instanceof JPanel p) {
                if (name.equals(p.getName())) return p;
                JPanel found = findNamedPanel(p, name);
                if (found != null) return found;
            } else if (c instanceof Container ct) {
                JPanel found = findNamedPanel(ct, name);
                if (found != null) return found;
            }
        }
        return null;
    }
}