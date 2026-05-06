package dk.easv.tiffixexamweblager.BE;

/**
 * Transfer object representing a single TIFF pulled out of the API's ZIP response.
 * This is NOT a persisted entity — it is a staging object that lives only until
 * whoever owns the File BE/DAO layer saves it to the database/disk.
 */
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