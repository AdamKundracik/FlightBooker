package sk.upjs.paz1c.project.storage;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import junit.framework.TestSuite;

import org.junit.jupiter.api.function.Executable;
import org.springframework.dao.DataIntegrityViolationException;

class MysqlCustomerDaoTest {

	private FlightDao flightDao;
	private CustomerDao customerDao;
	private Customer savedCustomer;
	private Flight savedFlight;
	private List<Customer> savedCustomers;
	private Airport airport;
	private AirportDao airportDao;

	public MysqlCustomerDaoTest() {
		DaoFactory.INSTANCE.testing();
		customerDao = DaoFactory.INSTANCE.getCustomerDao();
		flightDao = DaoFactory.INSTANCE.getFlightDao();
		airportDao = DaoFactory.INSTANCE.getAirportDao();
	}

	@BeforeEach
	void setUp() throws Exception {
		LocalDate date = LocalDate.of(2021, 12, 21);
		Customer testerCustomer = new Customer("Test", "Customer", date, "Male", 948438721L, "Košická 2");
		savedCustomer = customerDao.save(testerCustomer);
	}

	@AfterEach
	void tearDown() throws Exception {
		customerDao.delete(savedCustomer.getId());
	}

	@Test
	void testGetAll() {
		List<Customer> customers = customerDao.getAll();
		assertNotNull(customers);
		assertTrue(customers.size() > 0);
		// System.out.println(customers);
	}

	@Test
	void testGetById() {
		Customer byIdCustomer = customerDao.getById(savedCustomer.getId());
		assertEquals(savedCustomer, byIdCustomer);
		assertThrows(EntityNotFoundException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				customerDao.getById(-1L);
			}
		});

	}

	@Test
	void testInsert() {
		LocalDate date = LocalDate.of(2021, 12, 21);
		int initialSize = customerDao.getAll().size();
		Customer newCustomer = new Customer("Testovač", "Insertu", date, "nema gender", 23L, "Presovska ulica");
		Customer savedNewCustomer = customerDao.save(newCustomer);
		newCustomer.setId(savedNewCustomer.getId());
		// System.out.println(savedNewCustomer);
		// System.out.println(newCustomer);
		assertEquals(savedNewCustomer, newCustomer);
		assertNotNull(savedNewCustomer.getId());
		List<Customer> all = customerDao.getAll();
		assertEquals(initialSize + 1, all.size());

		boolean found = false;
		for (Customer customer : all) {
			if (customer.getId().equals(savedNewCustomer.getId())) {
				found = true;
				assertEquals(savedNewCustomer, customer);
				break;
			}
		}
		assertTrue(found);
		customerDao.delete(savedNewCustomer.getId());
	}

	@Test
	void testUpdate() {
		LocalDate date = LocalDate.of(2021, 12, 21);
		Customer changedCustomer = new Customer(savedCustomer.getId(), "Test", "Update", date, "Male", 24L,
				"Bratislavska ulica");
		Customer savedChangedCustomer = customerDao.save(changedCustomer);
		assertEquals(changedCustomer, savedChangedCustomer);

		List<Customer> all = customerDao.getAll();
		boolean found = false;
		for (Customer customer : all) {
			if (customer.getId().equals(changedCustomer.getId())) {
				found = true;
				assertEquals(savedChangedCustomer, customer);
				break;
			}
		}
		assertTrue(found);
		changedCustomer.setId(-1L);
		assertThrows(EntityNotFoundException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				customerDao.save(changedCustomer);
			}
		});

		changedCustomer.setId(savedChangedCustomer.getId());
		changedCustomer.setPhoneNumber(-1L);
		assertThrows(EntityNotFoundException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				customerDao.save(changedCustomer);
			}
		});

		assertThrows(NullPointerException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				customerDao.save(null);
			}
		});
	}

	@Test
	void testDelete() {
		LocalDate date = LocalDate.of(2021, 12, 21);
		Customer customerToDelete = new Customer("Tester", "Of delete", date, "Male", 24L, "Trencinska ulica");
		Customer saved = customerDao.save(customerToDelete);
		String nameSaved = saved.getName();
		Customer saved2 = customerDao.delete(saved.getId());
		String nameSaved2 = saved2.getName();
		assertEquals(nameSaved, nameSaved2);

		LocalDateTime departure = LocalDateTime.of(2021, 12, 5, 12, 34, 45);
		LocalDateTime arrival = LocalDateTime.of(2021, 12, 5, 12, 50, 45);
		Airport airport = new Airport("England", "London", "London airport", "LND");
		Airport airportSaved = airportDao.save(airport);
		List<Customer> customers = DaoFactory.INSTANCE.getCustomerDao().getAll();
		Flight flight = new Flight(date, airportSaved.getId(), airportSaved.getId(), "Slovak Airlines", "Bussines", 40,
				departure, arrival, customers);
		flight.getCustomers().add(customers.get(0));
		flight = flightDao.save(flight);
		//System.out.println(flight);

		assertThrows(EntityNotFoundException.class, new Executable() {
			@Override
			public void execute() throws Throwable {
				customerDao.getById(saved.getId());
			}
		});

	}

//	@Test
//	void testGetByFlightId() {
//		LocalDate date = LocalDate.of(2021, 12, 21);
//		LocalDateTime departure = LocalDateTime.of(2021, 12, 5, 12, 34, 45);
//		LocalDateTime arrival = LocalDateTime.of(2021, 12, 5, 12, 50, 45);
//		Airport airport = new Airport("England", "London", "London airport", "LND");
//		Airport airportSaved = airportDao.save(airport);
//
//		Customer customer1 = new Customer("Tester", "Of bySubjectId", date, "Male", 24L, "Martinska ulica");
//		Customer customer2 = customerDao.save(customer1);
//		List<Customer> customers = customerDao.getAll();
//		Flight flight = new Flight(date, airportSaved.getId(), airportSaved.getId(), "Slovak Airlines", "Bussines", 45,
//				departure, arrival, customers);
//
//		Flight saved = flightDao.save(flight);
//
//		List<Customer> listCustomers = new ArrayList<Customer>();
//        listCustomers.add(customer2);
//        saved.setCustomers(listCustomers);
//		System.out.println(saved);
//		flightDao.save(saved);
//		List<Customer> list = customerDao.getByFlightId(saved.getId());
//		System.out.println(list.size());
//
//		saved.getCustomers().add(customers.get(0));
//		System.out.println(saved);
//		flightDao.save(saved);
//		System.out.println(saved);
//		assertEquals(list.size() + 1, customerDao.getByFlightId(saved.getId()).size());
//		assertThrows(EntityUndeleteableException.class, new Executable() {
//			@Override
//			public void execute() throws Throwable {
//				customerDao.delete(customer2.getId());
//			}
//		});

//		List<Customer> list2 = customerDao.getByFlightId(-1); 
//		assertEquals(0,list2.size());

}
