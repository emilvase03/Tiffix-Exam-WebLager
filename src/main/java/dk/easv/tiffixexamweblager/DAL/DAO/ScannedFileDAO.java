package dk.easv.tiffixexamweblager.DAL.DAO;

// Project imports
import dk.easv.tiffixexamweblager.BE.ScannedFile;
import dk.easv.tiffixexamweblager.DAL.IScannedFileDataAccess;
import dk.easv.tiffixexamweblager.DAL.Utils.DBConnector;

// Java imports
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScannedFileDAO implements IScannedFileDataAccess {

    private final DBConnector connector;

    public ScannedFileDAO() throws IOException {
        connector = new DBConnector();
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    public List<ScannedFile> getFilesForDocument(int documentId) throws Exception {

        String sql = """
                SELECT Id, DocumentId, TiffFile, ScanOrder, SortOrder
                FROM ScannedFile
                WHERE DocumentId = ? AND IsDeleted = 0
                ORDER BY SortOrder
                """;

        List<ScannedFile> files = new ArrayList<>();

        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, documentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                files.add(new ScannedFile(
                        rs.getInt("Id"),
                        rs.getInt("DocumentId"),
                        rs.getBytes("TiffFile"),
                        rs.getInt("ScanOrder"),
                        rs.getInt("SortOrder")
                ));
            }
        }

        return files;
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    @Override
    public ScannedFile createFile(int documentId, ScannedFile file) throws Exception {

        String sql = """
                INSERT INTO ScannedFile (DocumentId, TiffFile, ScanOrder, SortOrder)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, documentId);
            stmt.setBytes(2, file.getTiffFile());
            stmt.setInt(3, file.getScanOrder());
            stmt.setInt(4, file.getSortOrder());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    file.setId(keys.getInt(1));
                }
            }
        }

        return file;
    }
}