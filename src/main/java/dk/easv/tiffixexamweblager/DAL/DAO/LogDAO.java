package dk.easv.tiffixexamweblager.DAL.DAO;

import dk.easv.tiffixexamweblager.BE.LogEntry;
import dk.easv.tiffixexamweblager.DAL.ILogDataAccess;
import dk.easv.tiffixexamweblager.DAL.Utils.DBConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogDAO implements ILogDataAccess {

    private final DBConnector dbConnector;

    public LogDAO() throws Exception {
        dbConnector = new DBConnector();
    }

    @Override
    public void insertLog(int userId, int typeId, String message) throws Exception {
        String sql = "INSERT INTO Log (UserId, TypeId, Message) VALUES (?, ?, ?)";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, typeId);
            ps.setString(3, message);
            ps.executeUpdate();
        }
    }

    @Override
    public List<LogEntry> getAllLogEntries() throws Exception {
        List<LogEntry> entries = new ArrayList<>();
        String sql = """
                SELECT l.Id, u.Username, lt.Type, l.Message, l.CreatedAt
                FROM Log l
                JOIN [User]  u  ON l.UserId = u.Id
                JOIN LogType lt ON l.TypeId = lt.Id
                WHERE l.IsDeleted = 0
                ORDER BY l.CreatedAt DESC
                """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                entries.add(new LogEntry(
                        rs.getInt("Id"),
                        rs.getString("Username"),
                        rs.getString("Type"),
                        rs.getString("Message"),
                        rs.getTimestamp("CreatedAt").toLocalDateTime()
                ));
            }
        }
        return entries;
    }
}