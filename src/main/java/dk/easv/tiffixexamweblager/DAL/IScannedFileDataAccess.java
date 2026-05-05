package dk.easv.tiffixexamweblager.DAL;

import dk.easv.tiffixexamweblager.BE.ScannedFile;

import java.util.List;

public interface IScannedFileDataAccess {
    List<ScannedFile> getFilesForDocument(int documentId) throws Exception;
}