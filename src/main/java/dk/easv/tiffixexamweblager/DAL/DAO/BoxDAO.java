package dk.easv.tiffixexamweblager.DAL.DAO;

import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.DAL.DB.DBConnector;
import dk.easv.tiffixexamweblager.DAL.IBoxDataAccess;

import java.sql.*;
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
        String sql = "SELECT Id, Number FROM Box ORDER BY Id DESC";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                boxes.add(new Box(rs.getInt("Id"), rs.getInt("Number")));
            }
        }
        return boxes;
    }

}