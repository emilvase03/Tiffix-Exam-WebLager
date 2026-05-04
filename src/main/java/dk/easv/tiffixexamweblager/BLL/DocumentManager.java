package dk.easv.tiffixexamweblager.BLL;

import dk.easv.tiffixexamweblager.BE.Document;
import dk.easv.tiffixexamweblager.DAL.DAO.DocumentDAO;
import dk.easv.tiffixexamweblager.DAL.IDocumentDataAccess;

import java.util.List;

public class DocumentManager {
    private final IDocumentDataAccess dataAccess;

    public DocumentManager() throws Exception {
        dataAccess = new DocumentDAO();
    }

    public List<Document> getDocumentsForBox(int boxId) throws Exception {
        return dataAccess.getDocumentsForBox(boxId);
    }

    public Document createDocument(int boxId, int sortOrder) throws Exception {
        return dataAccess.createDocument(boxId, sortOrder);
    }

    public void deleteDocument(Document document) throws Exception {
        dataAccess.deleteDocument(document);
    }
}
