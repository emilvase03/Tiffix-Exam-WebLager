package dk.easv.tiffixexamweblager.DAL.DAO;

// Project imports
import dk.easv.tiffixexamweblager.BE.ScannedFile;
import dk.easv.tiffixexamweblager.DAL.Utils.DBConnector;
import dk.easv.tiffixexamweblager.DAL.IScannedFileDataAccess;

// Java imports
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class ScannedFileDAO implements IScannedFileDataAccess {

    private final DBConnector connector = new DBConnector();

    public ScannedFileDAO() throws IOException {
    }

    @Override
    public List<ScannedFile> getFilesForDocument(int documentId) throws Exception {

        String sql = """
            SELECT Id, DocumentId, ScanOrder, SortOrder, FilePath, RotationAngle
            FROM ScannedFile
            WHERE DocumentId = ?
            ORDER BY SortOrder
            """;

        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, documentId);

            ResultSet rs = stmt.executeQuery();
            List<ScannedFile> files = new ArrayList<>();

            while (rs.next()) {
                files.add(new ScannedFile(
                        rs.getInt("Id"),
                        rs.getInt("DocumentId"),
                        rs.getInt("ScanOrder"),
                        rs.getInt("SortOrder"),
                        rs.getString("FilePath"),
                        rs.getDouble("RotationAngle")
                ));
            }
            return files;
        }
    }
}