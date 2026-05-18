package dk.easv.tiffixexamweblager.DAL.DAO;

// Project imports
import dk.easv.tiffixexamweblager.BE.Metadata;
import dk.easv.tiffixexamweblager.DAL.IMetadataDataAccess;
import dk.easv.tiffixexamweblager.DAL.Utils.DBConnector;

// Java imports
import java.sql.Connection;
import java.sql.PreparedStatement;

public class MetadataDAO implements IMetadataDataAccess {
    private DBConnector dbConnector;

    public MetadataDAO() throws Exception {
        dbConnector = new DBConnector();
    }

    @Override
    public void saveMetadata(Metadata metadata) throws Exception {
        String sql = """
            IF EXISTS (SELECT 1 FROM Metadata WHERE BoxId = ?)
                UPDATE Metadata SET DocumentsAmount = ?, FilesAmount = ?, Notes = ? WHERE BoxId = ?
            ELSE
                INSERT INTO Metadata (BoxId, DocumentsAmount, FilesAmount, Notes) VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, metadata.boxId());
            stmt.setInt(2, metadata.documentsAmount());
            stmt.setInt(3, metadata.filesAmount());
            stmt.setString(4, metadata.notes());
            stmt.setInt(5, metadata.boxId());
            stmt.setInt(6, metadata.boxId());
            stmt.setInt(7, metadata.documentsAmount());
            stmt.setInt(8, metadata.filesAmount());
            stmt.setString(9, metadata.notes());

            stmt.executeUpdate();
        }
    }
}
