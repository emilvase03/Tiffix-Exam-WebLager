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
    public void createMetadata(Metadata metadata) throws Exception {
        String sql = "INSERT INTO Metadata (BoxId, DocumentsAmount, FilesAmount, Notes) Values (?,?,?,?);";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, metadata.boxId());
            stmt.setInt(2, metadata.documentsAmount());
            stmt.setInt(3, metadata.filesAmount());
            stmt.setString(4, metadata.notes());

            stmt.executeUpdate();
        }
    }
}
