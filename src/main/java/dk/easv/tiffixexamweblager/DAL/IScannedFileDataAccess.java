package dk.easv.tiffixexamweblager.DAL;

import dk.easv.tiffixexamweblager.BE.ScannedFile;

import java.util.List;

public interface IScannedFileDataAccess {

    List<ScannedFile> getFilesForDocument(int documentId) throws Exception;

    /**
     * Inserts one ScannedFile row and sets its generated id on the returned object.
     *
     * @param documentId the owning document's DB id
     * @param file       unsaved ScannedFile (id == 0)
     * @return the same file with {@code id} updated to the generated key
     */
    ScannedFile createFile(int documentId, ScannedFile file) throws Exception;
}