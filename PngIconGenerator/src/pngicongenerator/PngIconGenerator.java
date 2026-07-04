package pngicongenerator;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

/**
 * PngIconGenerator
 *
 * Takes a single source PNG (ideally 256x256 or larger, square) and produces a
 * full set of JFrame-ready icon sizes: icon-16.png, icon-32.png, icon-48.png,
 * icon-64.png, icon-256.png
 *
 * Output files land next to (or inside a folder you pick) so you can drop them
 * straight into /resources for setIconImages().
 *
 * Java 8, pure Swing + ImageIO, no external dependencies.
 */
public class PngIconGenerator extends JFrame {

    private static final int[] SIZES = {16, 32, 48, 64, 256};

    private final JTextField sourceField = new JTextField();
    private final JTextField outputField = new JTextField();
    private final JTextArea log = new JTextArea();
    private final JLabel previewLabel = new JLabel();
    private final Preferences prefs = Preferences.userNodeForPackage(PngIconGenerator.class);

    public PngIconGenerator() {
        super("PNG Icon Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        // restore last used folders
        sourceField.setText(prefs.get("lastSource", ""));
        outputField.setText(prefs.get("lastOutput", ""));

        setSize(560, 460);

        try {
            java.util.List<Image> icons = new java.util.ArrayList<>();
            for (String size : new String[]{"16", "32", "48", "64", "256"}) {
                java.net.URL url = getClass().getResource("/resources/icon-" + size + ".png");
                //System.out.println("icon-" + size + ".png -> " + (url != null ? "found" : "NOT FOUND"));
                if (url != null) {
                    icons.add(Toolkit.getDefaultToolkit().getImage(url));
                }
            }
            if (!icons.isEmpty()) {
                setIconImages(icons);
            } else {
                // none of the sized icons exist - fall back to the single icon that already worked
                Image fallback = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/icon.png"));
                setIconImage(fallback);
            }
        } catch (Exception e) {
            // missing icon shouldn't stop the app from launching
        }
        setLocationRelativeTo(null);
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        panel.add(new JLabel("Source PNG:"), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(sourceField, c);
        c.gridx = 2;
        c.weightx = 0;
        JButton browseSource = new JButton("Browse...");
        browseSource.addActionListener(this::onBrowseSource);
        AppTheme.styleButton(browseSource);
        panel.add(browseSource, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        panel.add(new JLabel("Output folder:"), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(outputField, c);
        c.gridx = 2;
        c.weightx = 0;
        JButton browseOutput = new JButton("Browse...");
        browseOutput.addActionListener(this::onBrowseOutput);
        AppTheme.styleButton(browseOutput);
        panel.add(browseOutput, c);

        return panel;
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        previewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        previewLabel.setBorder(BorderFactory.createTitledBorder("Preview"));
        previewLabel.setPreferredSize(new Dimension(160, 160));
        panel.add(previewLabel, BorderLayout.WEST);

        log.setEditable(false);
        log.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(log);
        scroll.setBorder(BorderFactory.createTitledBorder("Log"));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton generate = new JButton("Generate Icons");
        generate.addActionListener(this::onGenerate);
        AppTheme.styleButton(generate);
        panel.add(generate);
        return panel;
    }

    private void onBrowseSource(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG images", "png"));
        String last = prefs.get("lastSourceDir", null);
        if (last != null) {
            chooser.setCurrentDirectory(new File(last));
        }

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            sourceField.setText(f.getAbsolutePath());
            prefs.put("lastSource", f.getAbsolutePath());
            prefs.put("lastSourceDir", f.getParentFile().getAbsolutePath());
            updatePreview(f);

            // default output folder = same folder as source, unless already set
            if (outputField.getText().trim().isEmpty()) {
                outputField.setText(f.getParentFile().getAbsolutePath());
            }
        }
    }

    private void onBrowseOutput(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String last = prefs.get("lastOutput", null);
        if (last != null) {
            chooser.setCurrentDirectory(new File(last));
        }

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            outputField.setText(f.getAbsolutePath());
            prefs.put("lastOutput", f.getAbsolutePath());
        }
    }

    private void updatePreview(File f) {
        try {
            BufferedImage img = ImageIO.read(f);
            if (img != null) {
                Image scaled = highQualityScale(img, 150, 150);
                previewLabel.setIcon(new ImageIcon(scaled));
            }
        } catch (IOException ex) {
            previewLabel.setIcon(null);
        }
    }

    private void onGenerate(ActionEvent e) {
        String sourcePath = sourceField.getText().trim();
        String outputPath = outputField.getText().trim();

        if (sourcePath.isEmpty() || outputPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please choose both a source PNG and an output folder.",
                    "Missing info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File sourceFile = new File(sourcePath);
        File outputDir = new File(outputPath);

        if (!sourceFile.isFile()) {
            JOptionPane.showMessageDialog(this, "Source file not found:\n" + sourcePath,
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            JOptionPane.showMessageDialog(this, "Could not create output folder:\n" + outputPath,
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        log.setText("");
        BufferedImage source;
        try {
            source = ImageIO.read(sourceFile);
        } catch (IOException ex) {
            appendLog("ERROR reading source: " + ex.getMessage());
            return;
        }
        if (source == null) {
            appendLog("ERROR: file is not a readable image (is it really a PNG?)");
            return;
        }

        appendLog("Source: " + sourceFile.getName() + " (" + source.getWidth() + "x" + source.getHeight() + ")");
        if (source.getWidth() != source.getHeight()) {
            appendLog("WARNING: source is not square - icons will be stretched.");
        }
        appendLog("");

        int okCount = 0;
        for (int size : SIZES) {
            try {
                BufferedImage resized = highQualityScaleToImage(source, size, size);
                File outFile = new File(outputDir, "icon-" + size + ".png");
                ImageIO.write(resized, "png", outFile);
                appendLog("OK  -> " + outFile.getName() + "  (" + size + "x" + size + ")");
                okCount++;
            } catch (IOException ex) {
                appendLog("FAIL -> icon-" + size + ".png : " + ex.getMessage());
            }
        }

        appendLog("");
        appendLog("Done. " + okCount + "/" + SIZES.length + " icons written to:");
        appendLog(outputDir.getAbsolutePath());
    }

    private void appendLog(String line) {
        log.append(line + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    /**
     * High quality downscale. For large reduction ratios (e.g. 1024 -> 16) a
     * single bicubic pass can look muddy or aliased, so we step down in halves
     * ("multi-pass bicubic") until we're close to the target, then do one final
     * precise pass.
     */
    private static BufferedImage highQualityScaleToImage(BufferedImage src, int targetW, int targetH) {
        int curW = src.getWidth();
        int curH = src.getHeight();
        BufferedImage current = src;

        // Step down by halves while we're more than 2x the target size.
        while (curW / 2 > targetW && curH / 2 > targetH) {
            int nextW = Math.max(targetW, curW / 2);
            int nextH = Math.max(targetH, curH / 2);
            current = scaleStep(current, nextW, nextH);
            curW = nextW;
            curH = nextH;
        }

        return scaleStep(current, targetW, targetH);
    }

    private static Image highQualityScale(BufferedImage src, int w, int h) {
        return highQualityScaleToImage(src, w, h);
    }

    private static BufferedImage scaleStep(BufferedImage src, int w, int h) {
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dst.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose();
        return dst;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppTheme.apply();
            PngIconGenerator frame = new PngIconGenerator();
            AppTheme.styleFrame(frame);
            frame.setVisible(true);
        });
    }
}
