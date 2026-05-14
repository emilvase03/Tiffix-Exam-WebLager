package dk.easv.tiffixexamweblager.DAL;

// Project imports
import dk.easv.tiffixexamweblager.BE.Customer;

public interface ICustomerDataAccess extends ICRUDDataAccess<Customer>, ISoftDeleteDataAccess<Customer>{
}
