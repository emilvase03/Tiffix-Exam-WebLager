package dk.easv.tiffixexamweblager.DAL;

import dk.easv.tiffixexamweblager.BE.Document;

import java.util.List;

public interface IDocumentDataAccess {
    List<Document> getDocumentsForBox(int boxId) throws Exception;
    Document createDocument(int boxId, int sortOrder) throws Exception;
    void deleteDocument(Document document) throws Exception;
}