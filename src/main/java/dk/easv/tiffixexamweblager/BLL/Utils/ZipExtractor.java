package dk.easv.tiffixexamweblager.BLL.Utils;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipExtractor {

    public static class ExtractedFile {
        private final String fileName;
        private final byte[] fileBytes;


        /* Creates a new extracted file container.
         *
         * @param fileName  the name of the extracted file
         * @param fileBytes the binary content of the file
         */

    public ExtractedFile(String fileName, byte[] fileBytes) {
            this.fileName = fileName;
            this.fileBytes = fileBytes;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getFileBytes() {
            return fileBytes;
        }
    }


   /**
     * Extracts all TIFF files from a ZIP archive provided as a byte array.
     *
     * <p>The method iterates through all entries in the ZIP file and
     * extracts only files with the extensions {@code .tif} or {@code .tiff}.
     * Other file types are ignored.</p>
     *
     * @param zipBytes the raw ZIP file data received from the API
     * @return a list of extracted TIFF files
     * @throws IOException if the ZIP input cannot be read
    */

public List<ExtractedFile> extractTiffs(byte[] zipBytes) throws IOException {
        List<ExtractedFile> files = new ArrayList<>();

        try (ZipInputStream zis =
                     new ZipInputStream(new ByteArrayInputStream(zipBytes))) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                if (entry.getName().toLowerCase().endsWith(".tif")
                        || entry.getName().toLowerCase().endsWith(".tiff")) {

                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    zis.transferTo(buffer);

                    files.add(new ExtractedFile(
                            entry.getName(),
                            buffer.toByteArray()
                    ));
                }
            }
        }
        return files;
    }
}