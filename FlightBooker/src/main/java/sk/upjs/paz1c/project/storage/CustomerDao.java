package sk.upjs.paz1c.project.storage;

import java.util.List;

public interface CustomerDao {
	
	List<Customer> getAll();
	
	Customer getById(long id);
	
	List<Customer> getByFlightId(long flight_id);
	
	Customer save(Customer customer) throws EntityNotFoundException, NullPointerException;
	
	Customer delete(long idCustomer) throws EntityNotFoundException;

}
