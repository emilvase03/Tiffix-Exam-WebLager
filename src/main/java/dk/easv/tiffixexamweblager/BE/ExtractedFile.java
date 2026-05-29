package dk.easv.tiffixexamweblager.BE;

public class ExtractedFile {

    private final String fileName;
    private final byte[] fileBytes;

    public ExtractedFile(String fileName, byte[] fileBytes) {
        this.fileName  = fileName;
        this.fileBytes = fileBytes;
    }

    /** Original file name as it appeared inside the ZIP (e.g. "scan_001.tiff"). */
    public String getFileName() { return fileName; }

    /** Raw bytes of the TIFF file. */
    public byte[] getFileBytes() { return fileBytes; }

    /** Convenience: size in bytes, useful for display. */
    public long getSizeBytes() { return fileBytes.length; }

    @Override
    public String toString() {
        return "ExtractedFile{fileName='" + fileName + "', size=" + fileBytes.length + "B}";
    }
}