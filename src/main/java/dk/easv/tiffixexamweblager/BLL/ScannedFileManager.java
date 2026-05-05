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

    public List<ScannedFile> getFilesForDocument(int documentId) throws Exception {
        return dataAccess.getFilesForDocument(documentId);
    }
}
