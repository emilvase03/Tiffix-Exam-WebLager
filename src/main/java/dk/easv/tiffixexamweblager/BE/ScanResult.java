package dk.easv.tiffixexamweblager.BE;

public class ScanResult {

    private final String fileName;
    private final byte[] fileBytes;
    private final boolean isCorrupted;

    public ScanResult(String fileName, byte[] fileBytes, boolean isCorrupted) {
        this.fileName = fileName;
        this.fileBytes = fileBytes;
        this.isCorrupted = isCorrupted;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public boolean isCorrupted() {
        return isCorrupted;
    }
}