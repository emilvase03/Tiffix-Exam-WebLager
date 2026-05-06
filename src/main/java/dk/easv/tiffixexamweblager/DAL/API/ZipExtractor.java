package dk.easv.tiffixexamweblager.DAL.API;

import dk.easv.tiffixexamweblager.BE.ExtractedFile;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Unpacks a ZIP delivered as raw bytes and returns every TIFF entry inside.
 * Ignores directories and any non-TIFF entries (e.g. __MACOSX metadata).
 */
public class ZipExtractor {

    /**
     * @param zipBytes raw bytes of a ZIP archive
     * @return list of ExtractedFile, one per .tif / .tiff entry found
     * @throws Exception if the bytes are not a valid ZIP or reading fails
     */
    public List<ExtractedFile> extractTiffs(byte[] zipBytes) throws Exception {
        List<ExtractedFile> results = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory() || !isTiff(entry.getName())) {
                    zis.closeEntry();
                    continue;
                }

                byte[] fileBytes = zis.readAllBytes();
                // Use only the plain filename, strip any path inside the ZIP
                String fileName = extractFileName(entry.getName());
                results.add(new ExtractedFile(fileName, fileBytes));

                zis.closeEntry();
            }
        }

        if (results.isEmpty()) {
            throw new Exception("ZIP contained no TIFF files.");
        }

        return results;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isTiff(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".tif") || lower.endsWith(".tiff");
    }

    /** Strips any folder path that may be present inside the ZIP entry name. */
    private String extractFileName(String entryName) {
        int slash = Math.max(entryName.lastIndexOf('/'), entryName.lastIndexOf('\\'));
        return slash >= 0 ? entryName.substring(slash + 1) : entryName;
    }
}