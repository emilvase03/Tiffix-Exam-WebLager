package dk.easv.tiffixexamweblager.BLL.Utils;

// Project imports
import dk.easv.tiffixexamweblager.BE.ScannedFile;

// Java imports
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

/**
 * Exports ScannedFile collections to disk as TIFF files.
 *
 * <p><b>Single-page</b> — writes each page's raw bytes directly to its own
 * {@code .tiff} file (completely lossless, no decode/re-encode).
 *
 * <p><b>Multi-page</b> — decodes every page and writes them as a sequence
 * into one {@code .tiff} file using Java's built-in TIFF ImageWriter (Java 9+).
 */
public class TiffExportService {

    // ── Single-page ───────────────────────────────────────────────────────────

    /**
     * Writes each page as a separate TIFF in {@code outputDir}.
     *
     * <p>File names: {@code {docLabel}_page_{sortOrder}.tiff}
     *
     * @param files     ordered pages to export
     * @param outputDir directory that will receive the files (must already exist)
     * @param docLabel  human-readable document name (used in the file name)
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
     * @param files      ordered pages to combine
     * @param outputFile full path of the file to create (parent dir must exist)
     * @throws Exception if no TIFF writer is available or a page cannot be decoded
     */
    public void exportMultiPage(List<ScannedFile> files, Path outputFile) throws Exception {

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("TIFF");
        if (!writers.hasNext()) {
            throw new Exception(
                    "No TIFF ImageWriter found. Requires Java 9+ with javax.imageio TIFF support.");
        }

        ImageWriter writer = writers.next();

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile.toFile())) {
            writer.setOutput(ios);
            writer.prepareWriteSequence(null);

            for (ScannedFile sf : files) {
                byte[] bytes = sf.getTiffFile();
                if (bytes == null || bytes.length == 0) continue;

                BufferedImage page = ImageIO.read(new ByteArrayInputStream(bytes));
                if (page == null) continue;

                writer.writeToSequence(new IIOImage(page, null, null), null);
            }

            writer.endWriteSequence();
        } finally {
            writer.dispose();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Replaces characters that are unsafe in file-system names. */
    private String sanitize(String label) {
        return label.replaceAll("[\\s/\\\\:*?\"<>|]", "_");
    }

    /**
     * Returns {@code dir/filename}; if that already exists appends {@code _2}, {@code _3}, …
     * so earlier exports are never silently overwritten.
     */
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