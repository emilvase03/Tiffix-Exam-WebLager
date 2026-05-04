package dk.easv.tiffixexamweblager.DAL;

import dk.easv.tiffixexamweblager.BE.Box;

import java.util.List;

public interface IBoxDataAccess {
    List<Box> getAllBoxes() throws Exception;

}