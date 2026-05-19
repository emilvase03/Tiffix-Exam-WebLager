package dk.easv.tiffixexamweblager.DAL.DAO;

//Project imports
import dk.easv.tiffixexamweblager.BE.ScannedFile;
import dk.easv.tiffixexamweblager.DAL.IScannedFileDataAccess;
import dk.easv.tiffixexamweblager.DAL.Utils.DBConnector;

//Java imports
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ScannedFileDAO implements IScannedFileDataAccess {

    private final DBConnector connector;

    private Boolean fileNameColumnExists = null;

    public ScannedFileDAO() throws IOException {
        connector = new DBConnector();
    }

    private boolean hasFileNameColumn() {
        if (fileNameColumnExists != null) return fileNameColumnExists;
        try (Connection conn = connector.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet cols = meta.getColumns(conn.getCatalog(), null, "ScannedFile", "FileName")) {
                fileNameColumnExists = cols.next();
            }
        } catch (Exception e) {
            fileNameColumnExists = false;
        }
        return fileNameColumnExists;
    }

    @Override
    public List<ScannedFile> getFilesForDocument(int documentId) throws Exception {

        String baseSql = """
                SELECT Id, DocumentId, TiffFile, ScanOrder, SortOrder
                FROM ScannedFile
                WHERE DocumentId = ? AND IsDeleted = 0
                ORDER BY SortOrder
                """;

        String withFileName = """
                SELECT Id, DocumentId, TiffFile, ScanOrder, SortOrder, FileName
                FROM ScannedFile
                WHERE DocumentId = ? AND IsDeleted = 0
                ORDER BY SortOrder
                """;

        boolean useFileName = hasFileNameColumn();
        String sql = useFileName ? withFileName : baseSql;

        List<ScannedFile> files = new ArrayList<>();

        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, documentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ScannedFile file = new ScannedFile(
                            rs.getInt("Id"),
                            rs.getInt("DocumentId"),
                            rs.getBytes("TiffFile"),
                            rs.getInt("ScanOrder"),
                            rs.getInt("SortOrder")
                    );

                    if (useFileName) {
                        String storedName = rs.getString("FileName");
                        if (storedName != null && !storedName.isBlank()) {
                            file.setFileName(storedName);
                        }
                    }

                    files.add(file);
                }
            }
        }

        return files;
    }

    @Override
    public ScannedFile createFile(int documentId, ScannedFile file) throws Exception {

        String sqlWithName = """
                INSERT INTO ScannedFile (DocumentId, TiffFile, ScanOrder, SortOrder, FileName)
                VALUES (?, ?, ?, ?, ?)
                """;

        String sqlWithout = """
                INSERT INTO ScannedFile (DocumentId, TiffFile, ScanOrder, SortOrder)
                VALUES (?, ?, ?, ?)
                """;

        boolean useFileName = hasFileNameColumn();
        String sql = useFileName ? sqlWithName : sqlWithout;

        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, documentId);
            stmt.setBytes(2, file.getTiffFile());
            stmt.setInt(3, file.getScanOrder());
            stmt.setInt(4, file.getSortOrder());
            if (useFileName) {
                stmt.setString(5, file.getFileName());
            }
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) file.setId(keys.getInt(1));
            }
        }

        return file;
    }

    @Override
    public void updateFileName(int fileId, String fileName) throws Exception {
        if (!hasFileNameColumn()) return;

        String sql = "UPDATE ScannedFile SET FileName = ? WHERE Id = ?";

        try (Connection conn = connector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fileName);
            stmt.setInt(2, fileId);
            stmt.executeUpdate();
        }
    }
}