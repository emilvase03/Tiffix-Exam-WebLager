package dk.easv.tiffixexamweblager.DAL.DAO;

import dk.easv.tiffixexamweblager.BE.ScannedFile;
import dk.easv.tiffixexamweblager.DAL.DB.DBConnector;
import dk.easv.tiffixexamweblager.DAL.IScannedFileDataAccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ScannedFileDAO implements IScannedFileDataAccess {

    private final DBConnector dbConnector;

    public ScannedFileDAO() throws Exception {
        this.dbConnector = new DBConnector();
    }

    @Override
    public List<ScannedFile> getFilesForDocument(int documentId) throws Exception {
        List<ScannedFile> files = new ArrayList<>();

        String sql = """
            SELECT  Id,     DocumentId, ScanOrder, SortOrder,  FilePath,  RotationAngle,  CreatedAt
            FROM ScannedFile
            WHERE DocumentId = ?
            ORDER BY SortOrder
            """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, documentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    files.add(new ScannedFile(
                            rs.getInt("Id"),
                            rs.getInt("DocumentId"),
                            rs.getInt("ScanOrder"),
                            rs.getInt("SortOrder"),
                            rs.getString("FilePath"),
                            rs.getInt("RotationAngle"),
                            rs.getTimestamp("CreatedAt").toLocalDateTime()
                    ));
                }
            }
        }

        return files;
    }
}