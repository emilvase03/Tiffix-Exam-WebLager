package dk.easv.tiffixexamweblager.DAL;

// Prject imports
import dk.easv.tiffixexamweblager.BE.Metadata;

public interface IMetadataDataAccess {
    public void createMetadata(Metadata metadata) throws Exception;
}
