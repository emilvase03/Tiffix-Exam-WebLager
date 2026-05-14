package dk.easv.tiffixexamweblager.BLL.Utils;

// Project imports
import dk.easv.tiffixexamweblager.BE.Rule;
import dk.easv.tiffixexamweblager.BE.ScannedFile;

// Java imports
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Exports ScannedFile collections to disk as TIFF files with ALL active
 * transformations baked in.
 *
 * <p><b>Single-page</b> — one {@code .tiff} per page, named
 * {@code {docLabel}_page_{sortOrder}.tiff}.
 *
 * <p><b>Multi-page</b> — all pages of a document in one {@code .tiff}, using
 * {@code prepareWriteSequence} / {@code writeToSequence} / {@code endWriteSequence}
 * with an explicit {@link ImageWriteParam} (passing {@code null} causes some
 * JDK TIFF writers to commit only the first page).
 * Returns the number of pages written so the caller can verify completeness.
 */
public class TiffExportService {

    // ── Single-page export ────────────────────────────────────────────────────

    /**
     *   Writes each page as a separate TIFF in {@code outputDir}.
     *   File names: {@code {docLabel}_page_{sortOrder}.tiff}
     */
    public void exportSinglePage(List<ScannedFile> files,
                                 Path outputDir,
                                 String docLabel,
                                 List<Rule> rules) throws IOException {

        String safeLabel = sanitize(docLabel);

        for (ScannedFile sf : files) {

            BufferedImage final_ = resolveFullyTransformed(sf, rules);
            if (final_ == null) continue;   // unreadable page — skip silently

            String filename    = safeLabel + "_page_" + sf.getSortOrder() + ".tiff";
            Path   destination = resolveUnique(outputDir, filename);

            ImageIO.write(final_, "TIFF", destination.toFile());
        }
    }

    // ── Multi-page export ─────────────────────────────────────────────────────

    /**
     * Combines all pages into a single multi-page TIFF at {@code outputFile}.
     * Pages appear in {@code files} order (by sort order).
     *
     * @param files      pages to export
     * @param outputFile destination file path (created or overwritten)
     * @param rules      active profile rules — applied to any page whose
     *                   {@code processedImage} is null (lazy load path)
     * @return number of pages written
     */
    public int exportMultiPage(List<ScannedFile> files,
                               Path outputFile,
                               List<Rule> rules) throws Exception {

        // Resolve and transform every page first; skip unreadable ones
        List<BufferedImage> pages = new ArrayList<>();
        for (ScannedFile sf : files) {
            BufferedImage img = resolveFullyTransformed(sf, rules);
            if (img != null) pages.add(img);
        }

        if (pages.isEmpty()) return 0;

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("TIFF");
        if (!writers.hasNext()) {
            throw new Exception(
                    "No TIFF ImageWriter found. Requires Java 9+ (javax.imageio TIFF support).");
        }

        ImageWriter     writer = writers.next();
        ImageWriteParam param  = writer.getDefaultWriteParam();   // must NOT be null

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile.toFile())) {
            writer.setOutput(ios);
            writer.prepareWriteSequence(null);

            for (BufferedImage page : pages) {
                writer.writeToSequence(new IIOImage(page, null, null), param);
            }

            writer.endWriteSequence();
            ios.flush();
        } finally {
            writer.dispose();
        }

        return pages.size();
    }


    /**
     * Returns the fully-transformed image for {@code sf}, applying:
     * <ol>
     *   <li>Profile rules (ROTATE + BRIGHTNESS from the selected profile)
     *   <li>User rotation accumulated during the session
     *   <li>User brightness accumulated during the session
     * </ol>
     *
     * <p>Step 1 is already done if {@code sf.getProcessedImage()} is non-null —
     * that happens at fetch time via {@code ImageTransformations.applyRules()}.
     * If {@code processedImage} is null (e.g. a DB-loaded file that was never
     * previewed), this method decodes the raw bytes and applies the rules now
     * so the exported result is identical to what the user would have seen in preview.
     *
     * @return transformed image, or {@code null} if the page cannot be decoded
     */
    private BufferedImage resolveFullyTransformed(ScannedFile sf, List<Rule> rules) {
        BufferedImage base = sf.getProcessedImage();

        if (base == null) {
            byte[] bytes = sf.getTiffFile();
            if (bytes != null && bytes.length > 0) {
                try {
                    BufferedImage raw = ImageIO.read(new ByteArrayInputStream(bytes));
                    if (raw != null) {
                        base = ImageTransformations.applyRules(raw, rules);
                        sf.setProcessedImage(base);   // cache so preview is also consistent
                    }
                } catch (IOException ignored) { }
            }

            if (base == null && sf.getFilePath() != null && !sf.getFilePath().isBlank()) {
                try {
                    BufferedImage raw = ImageIO.read(new File(sf.getFilePath()));
                    if (raw != null) {
                        base = ImageTransformations.applyRules(raw, rules);
                        sf.setProcessedImage(base);
                    }
                } catch (IOException ignored) { }
            }
        }

        if (base == null) return null;

        return ImageTransformations.applyAll(base, sf.getUserRotation(), sf.getUserBrightness());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String sanitize(String label) {
        return label.replaceAll("[\\s/\\\\:*?\"<>|]", "_");
    }

    private Path resolveUnique(Path dir, String filename) {
        Path candidate = dir.resolve(filename);
        if (!Files.exists(candidate)) return candidate;

        int    dot    = filename.lastIndexOf('.');
        String base   = dot > 0 ? filename.substring(0, dot) : filename;
        String ext    = dot > 0 ? filename.substring(dot)    : "";
        int    suffix = 2;

        do {
            candidate = dir.resolve(base + "_" + suffix + ext);
            suffix++;
        } while (Files.exists(candidate));

        return candidate;
    }
}