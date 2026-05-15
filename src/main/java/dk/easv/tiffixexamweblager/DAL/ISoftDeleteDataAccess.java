package dk.easv.tiffixexamweblager.DAL;

// Java imports
import java.util.List;

public interface ISoftDeleteDataAccess<T> {

    public List<T> getAllIncludingSoftDeleted() throws Exception;
}
