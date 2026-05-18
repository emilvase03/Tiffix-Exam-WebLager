package dk.easv.tiffixexamweblager.BLL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Metadata;
import dk.easv.tiffixexamweblager.DAL.DAO.MetadataDAO;
import dk.easv.tiffixexamweblager.DAL.IMetadataDataAccess;

public class MetadataManager {
    private IMetadataDataAccess dataAccess;

    public MetadataManager() throws Exception{
        dataAccess = new MetadataDAO();
    }

    public void createMetadata(Metadata metadata) throws Exception{
        dataAccess.createMetadata(metadata);
    }
}
