package dk.easv.tiffixexamweblager.DAL;

// Project imports
import dk.easv.tiffixexamweblager.BE.ScannedFile;

// Java imports
import java.util.List;

public interface IScannedFileDataAccess {

    List<ScannedFile> getFilesForDocument(int documentId) throws Exception;


}