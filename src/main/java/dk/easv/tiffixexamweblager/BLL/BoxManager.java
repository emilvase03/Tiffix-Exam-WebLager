package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.DAL.DAO.BoxDAO;
import dk.easv.tiffixexamweblager.DAL.ICRUDDataAccess;

// Java imports
import java.util.List;

public class BoxManager {

    private final ICRUDDataAccess<Box> dataAccess;

    public BoxManager() throws Exception {
        dataAccess = new BoxDAO();
    }

    public List<Box> getAllBoxes() throws Exception {
        return dataAccess.getAll();
    }

    public Box createBox(Box box) throws Exception {
        return dataAccess.create(box);
    }

    public void deleteBox(Box box) throws Exception {
        dataAccess.delete(box);
    }
}