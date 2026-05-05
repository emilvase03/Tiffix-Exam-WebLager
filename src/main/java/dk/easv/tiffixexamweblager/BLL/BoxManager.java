package dk.easv.tiffixexamweblager.BLL;

import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.DAL.DAO.BoxDAO;
import dk.easv.tiffixexamweblager.DAL.IBoxDataAccess;

import java.util.List;

public class BoxManager {

    private final IBoxDataAccess dataAccess;

    public BoxManager() throws Exception {
        dataAccess = new BoxDAO();
    }

    public List<Box> getAllBoxes() throws Exception {
        return dataAccess.getAllBoxes();
    }

    public Box createBox(Box box) throws Exception {
        return dataAccess.createBox(box);
    }

    public void deleteBox(Box box) throws Exception {
        dataAccess.deleteBox(box);
    }
}