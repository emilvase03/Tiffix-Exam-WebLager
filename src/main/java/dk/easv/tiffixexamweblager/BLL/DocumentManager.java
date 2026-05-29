package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Document;
import dk.easv.tiffixexamweblager.BE.User;
import dk.easv.tiffixexamweblager.BLL.Utils.LogAction;
import dk.easv.tiffixexamweblager.BLL.Utils.UserSession;
import dk.easv.tiffixexamweblager.DAL.DAO.DocumentDAO;
import dk.easv.tiffixexamweblager.DAL.IDocumentDataAccess;

// Java imports
import java.util.List;

public class DocumentManager {

    private final IDocumentDataAccess dataAccess;
    private final LogManager          logManager;

    public DocumentManager() throws Exception {
        dataAccess = new DocumentDAO();
        logManager = new LogManager();
    }

    public List<Document> getDocumentsForBox(int boxId) throws Exception {
        return dataAccess.getDocumentsForBox(boxId);
    }

    public Document createDocument(int boxId, int sortOrder) throws Exception {
        Document created = dataAccess.createDocument(boxId, sortOrder);
        log(LogAction.CREATE, "Created document in box ID: " + boxId + " (sort order: " + sortOrder + ")");
        return created;
    }

    public void deleteDocument(Document document) throws Exception {
        dataAccess.deleteDocument(document);
        log(LogAction.DELETE, "Deleted document ID: " + document.getId());
    }

    private void log(LogAction action, String message) {
        try {
            User current = UserSession.getInstance().getCurrentUser();
            if (current != null) {
                logManager.log(
                        current.getId(),
                        action,
                        current.getUsername() + ": " + message
                );
            }
        } catch (Exception ignored) {}
    }
}