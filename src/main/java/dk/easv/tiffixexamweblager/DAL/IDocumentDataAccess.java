package dk.easv.tiffixexamweblager.DAL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Document;

// Java imports
import java.util.List;

public interface IDocumentDataAccess {
    public List<Document> getDocumentsForBox(int boxId) throws Exception;

    public Document createDocument(int boxId, int sortOrder) throws Exception;

    public void deleteDocument(Document document) throws Exception;
}