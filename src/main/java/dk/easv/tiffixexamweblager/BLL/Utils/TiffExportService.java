package dk.easv.tiffixexamweblager.BLL.Utils;

// Project imports
import dk.easv.tiffixexamweblager.BE.ScannedFile;

// Java imports
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Exports ScannedFile collections to disk as TIFF files.
 *
 * <p><b>Single-page</b> — writes each page's raw bytes directly to its own
 * {@code .tiff} file (lossless, no decode/re-encode).
 *
 * <p><b>Multi-page</b> — decodes every page and writes them into one {@code .tiff}
 * using the standard {@code prepareWriteSequence} / {@code writeToSequence} /
 * {@code endWriteSequence} sequence with an explicit {@code ImageWriteParam}.
 * Returns the number of pages actually written so the caller can verify.
 */
public class TiffExportService {

    // ── Single-page ───────────────────────────────────────────────────────────

    /**
     * Writes each page as a separate TIFF in {@code outputDir}.
     * File names: {@code {docLabel}_page_{sortOrder}.tiff}
     */
    public void exportSinglePage(List<ScannedFile> files, Path outputDir, String docLabel)
            throws IOException {

        String safeLabel = sanitize(docLabel);

        for (ScannedFile sf : files) {
            byte[] bytes = sf.getTiffFile();
            if (bytes == null || bytes.length == 0) continue;

            String filename    = safeLabel + "_page_" + sf.getSortOrder() + ".tiff";
            Path   destination = resolveUnique(outputDir, filename);
            Files.write(destination, bytes);
        }
    }

    // ── Multi-page ────────────────────────────────────────────────────────────

    /**
     * Combines all pages into a single multi-page TIFF at {@code outputFile}.
     * Pages appear in the same order as {@code files} (by sort order).
     *
     * <p>Uses {@code prepareWriteSequence} → {@code writeToSequence} → {@code endWriteSequence}
     * with an explicit (default) {@link ImageWriteParam} — passing {@code null} for the param
     * causes some JDK TIFF writers to only commit the first page.
     *
     * @return the number of pages written into the file
     */
    public int exportMultiPage(List<ScannedFile> files, Path outputFile) throws Exception {

        // Decode all pages up front; skip any that are unreadable
        List<BufferedImage> pages = new ArrayList<>();
        for (ScannedFile sf : files) {
            byte[] bytes = sf.getTiffFile();
            if (bytes == null || bytes.length == 0) continue;

            BufferedImage page = ImageIO.read(new ByteArrayInputStream(bytes));
            if (page != null) {
                pages.add(page);
            }
        }

        if (pages.isEmpty()) return 0;

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("TIFF");
        if (!writers.hasNext()) {
            throw new Exception(
                    "No TIFF ImageWriter found. Requires Java 9+ (javax.imageio TIFF support).");
        }

        ImageWriter    writer = writers.next();
        ImageWriteParam param  = writer.getDefaultWriteParam(); // explicit param — not null

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