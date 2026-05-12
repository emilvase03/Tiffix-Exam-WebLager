package dk.easv.tiffixexamweblager.BLL;

import dk.easv.tiffixexamweblager.BE.ScannedFile;
import dk.easv.tiffixexamweblager.DAL.DAO.ScannedFileDAO;
import dk.easv.tiffixexamweblager.DAL.IScannedFileDataAccess;

import java.util.List;

public class ScannedFileManager {

    private final IScannedFileDataAccess dataAccess;

    public ScannedFileManager() throws Exception {
        dataAccess = new ScannedFileDAO();
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public List<ScannedFile> getFilesForDocument(int documentId) throws Exception {
        return dataAccess.getFilesForDocument(documentId);
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Persists every unsaved file (id == 0) in {@code files} under {@code documentId}.
     * Each saved file has its generated id set in-place so the caller's list stays current.
     */
    public void saveFilesForDocument(int documentId, List<ScannedFile> files) throws Exception {
        for (ScannedFile file : files) {
            if (file.getId() == 0) {
                dataAccess.createFile(documentId, file);
            }
        }
    }
}