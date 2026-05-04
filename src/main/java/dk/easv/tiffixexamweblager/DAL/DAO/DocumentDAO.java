package dk.easv.tiffixexamweblager.DAL.DAO;

import dk.easv.tiffixexamweblager.BE.Document;
import dk.easv.tiffixexamweblager.DAL.DB.DBConnector;
import dk.easv.tiffixexamweblager.DAL.IDocumentDataAccess;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO implements IDocumentDataAccess {

    private final DBConnector dbConnector;

    public DocumentDAO() throws Exception {
        dbConnector = new DBConnector();
    }

    @Override
    public List<Document> getDocumentsForBox(int boxId) throws Exception {
        List<Document> documents = new ArrayList<>();

        String docSql = "SELECT Id, BoxId, SortOrder FROM Document WHERE BoxId = ? ORDER BY SortOrder";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(docSql)) {

            stmt.setInt(1, boxId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                documents.add(new Document(
                        rs.getInt("Id"),
                        rs.getInt("BoxId"),
                        rs.getInt("SortOrder")
                ));
            }
        }

        return documents;
    }

    @Override
    public Document createDocument(int boxId, int sortOrder) throws Exception {
        String sql = "INSERT INTO Document (BoxId, SortOrder) VALUES (?, ?)";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, boxId);
            stmt.setInt(2, sortOrder);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return new Document(rs.getInt(1), boxId, sortOrder);
            }
            throw new Exception("Document creation failed.");
        }
    }

    @Override
    public void deleteDocument(Document document) throws Exception {
        String sql = "DELETE FROM Document WHERE Id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, document.getId());
            stmt.executeUpdate();
        }
    }
}