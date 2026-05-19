package dk.easv.tiffixexamweblager.DAL;

import dk.easv.tiffixexamweblager.BE.ScannedFile;

import java.util.List;

public interface IScannedFileDataAccess {

    List<ScannedFile> getFilesForDocument(int documentId) throws Exception;

    ScannedFile createFile(int documentId, ScannedFile file) throws Exception;

    void updateFileName(int fileId, String fileName) throws Exception;
}