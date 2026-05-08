package dk.easv.tiffixexamweblager.BE;

public class ScanResult {

    private final String fileName;
    private final byte[] fileBytes;

    public ScanResult(String fileName, byte[] fileBytes) {
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