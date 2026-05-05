package dk.easv.tiffixexamweblager.DAL.DAO;

import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.DAL.DB.DBConnector;
import dk.easv.tiffixexamweblager.DAL.IBoxDataAccess;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BoxDAO implements IBoxDataAccess {

    private final DBConnector dbConnector;

    public BoxDAO() throws Exception {
        dbConnector = new DBConnector();
    }

    @Override
    public List<Box> getAllBoxes() throws Exception {
        List<Box> boxes = new ArrayList<>();
        String sql = """
                SELECT b.Id, b.Number, b.Title, b.CreatedAt,
                       u.Username AS CreatedByUsername,
                       b.DocumentsAmount, b.PagesAmount
                FROM Box b
                LEFT JOIN [User] u ON u.Id = b.CreatedByUserId
                WHERE b.IsDeleted = 0
                ORDER BY b.Number ASC
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                boxes.add(new Box(
                        rs.getInt("Id"),
                        rs.getInt("Number"),
                        rs.getString("Title"),
                        rs.getObject("CreatedAt", LocalDateTime.class),
                        rs.getString("CreatedByUsername"),   // read constructor
                        rs.getInt("DocumentsAmount"),
                        rs.getInt("PagesAmount")
                ));
            }
        }
        return boxes;
    }

    @Override
    public Box createBox(Box box) throws Exception {
        String sql = """
                INSERT INTO Box (Number, Title, CreatedAt, CreatedByUserId,
                                 DocumentsAmount, PagesAmount, IsDeleted)
                VALUES (?, ?, ?, ?, ?, ?, 0)
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, box.getNumber());
            stmt.setString(2, box.getTitle());
            stmt.setObject(3, box.getCreatedAt());
            stmt.setInt(4, box.getCreatedByUserId());
            stmt.setInt(5, box.getDocumentsAmount());
            stmt.setInt(6, box.getPagesAmount());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    box.setId(keys.getInt(1));
                }
            }
        }
        return box;
    }

    @Override
    public void deleteBox(Box box) throws Exception {
        String sql = "UPDATE Box SET IsDeleted = 1 WHERE Id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, box.getId());
            stmt.executeUpdate();
        }
    }
}