package dk.easv.tiffixexamweblager.GUI.Models;

import dk.easv.tiffixexamweblager.BE.Box;
import dk.easv.tiffixexamweblager.BLL.BoxManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class BoxModel {

    private final BoxManager manager;
    private final ObservableList<Box> allBoxes = FXCollections.observableArrayList();

    public BoxModel() throws Exception {
        manager = new BoxManager();
    }

    public ObservableList<Box> getAllBoxes() throws Exception {
        allBoxes.setAll(manager.getAllBoxes());
        return allBoxes;
    }

    public Box createBox(Box box) throws Exception {
        Box created = manager.createBox(box);
        int i = 0;
        while (i < allBoxes.size() && allBoxes.get(i).getNumber() < created.getNumber()) {
            i++;
        }
        allBoxes.add(i, created);
        return created;
    }

    public void deleteBox(Box box) throws Exception {
        manager.deleteBox(box);
        allBoxes.remove(box);
    }
}