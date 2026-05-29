package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.ScannedFile;
import dk.easv.tiffixexamweblager.DAL.DAO.ScannedFileDAO;
import dk.easv.tiffixexamweblager.DAL.IScannedFileDataAccess;

// Java imports
import java.util.List;

public class ScannedFileManager {

    private final IScannedFileDataAccess dataAccess;

    public ScannedFileManager() throws Exception {
        dataAccess = new ScannedFileDAO();
    }


    public List<ScannedFile> getFilesForDocument(int documentId) throws Exception {
        return dataAccess.getFilesForDocument(documentId);
    }

    public void saveFilesForDocument(int documentId, List<ScannedFile> files) throws Exception {
        for (ScannedFile file : files) {
            if (file.getId() == 0) {
                dataAccess.createFile(documentId, file);
            } else {
                dataAccess.updateFileName(file.getId(), file.getFileName());
            }
        }
    }
}