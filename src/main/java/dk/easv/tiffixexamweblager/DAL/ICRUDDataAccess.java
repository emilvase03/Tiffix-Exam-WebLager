package dk.easv.tiffixexamweblager.DAL;

// Java imports
import java.util.List;

public interface ICRUDDataAccess<T> {
    public List<T> getAll() throws Exception;

    public T create(T entity) throws Exception;

    public void update(T entity) throws Exception;

    public void delete(T entity) throws Exception;
}
