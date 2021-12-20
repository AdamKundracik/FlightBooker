package sk.upjs.paz1c.project.storage;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class MysqlFlightDaoTest {

	private FlightDao flightDao;
	private CustomerDao customerDao;
	private String country;
	private Flight savedFlight;
	private Airport airport;
	private AirportDao airportDao;
	private List<Customer> savedCustomer;

	public MysqlFlightDaoTest() {
		DaoFactory.INSTANCE.testing();
		flightDao = DaoFactory.INSTANCE.getFlightDao();
		customerDao = DaoFactory.INSTANCE.getCustomerDao();
		airportDao = DaoFactory.INSTANCE.getAirportDao();
	}

	@BeforeEach
	void setUp() throws Exception {
		List<Customer> customers = customerDao.getAll();
		LocalDate date = LocalDate.of(2021, 12, 21);
		savedCustomer = new ArrayList<Customer>();
		savedCustomer
				.add(customerDao.save(new Customer(null, "Flight", "INSERT", date, "Male", 24L, "Martinska ulica")));
		savedCustomer
				.add(customerDao.save(new Customer(null, "Flight", "INSERT", date, "Male", 24L, "Martinska ulica")));

		LocalDateTime departure = LocalDateTime.of(2021, 12, 5, 12, 34, 45);
		LocalDateTime arrival = LocalDateTime.of(2021, 12, 5, 12, 50, 45);
		Flight testerFlight = new Flight(null, date, 1L, 2L, "Slovak Airlines", "Bussines", 40, departure, arrival,
				customers);
		savedFlight = flightDao.save(testerFlight);

	}

	@AfterEach
	void tearDown() throws Exception {
		flightDao.delete(savedFlight.getId());
		for (Customer c : savedCustomer) {
			customerDao.delete(c.getId());
		}

	}
	
	@Test
	void testTest() {
		System.out.println(flightDao.fromAtoB("Bratislava Airport", "Trnava Airport"));

		
	}

	@Test
	void testInsert() {

		Airport airport1 = new Airport("England", "London", "London airport", "LND");
		Airport airportSaved = airportDao.save(airport1);
		LocalDateTime localDate = LocalDateTime.of(2021, 12, 21, 12, 12, 10);
		LocalDateTime localDate1 = LocalDateTime.of(2021, 12, 21, 21, 15, 38);
		LocalDate date1 = LocalDate.now();
		List<Customer> listCustomers = new ArrayList<>();
		Flight flight1 = new Flight(null, date1, airportSaved.getId(), airportSaved.getId(), "Slovak Airlines",
				"Bussines", 1, localDate, localDate1);
		LocalDate date = LocalDate.of(2021, 12, 21);
		Customer customer = new Customer("Tester", "Of delete", date, "Male", 24L, "Trencinska ulica");
		Customer savedCustomer = customerDao.save(customer);

		// int numberOfFlights = flightDao.getByAirport(airportSaved).size();

		listCustomers.add(savedCustomer);
		listCustomers.add(customerDao.save(new Customer("Tester2", "Of insert", date, "Female", 27L, "Popradska")));
		flight1.setCustomers(listCustomers);
		Flight saved = flightDao.save(flight1);
		assertEquals(saved, flight1);
		assertEquals(listCustomers.size(), saved.getCustomers().size());
		assertNotNull(saved.getId());
	}

	@Test
	void testDelete() {
		List<Customer> customers = customerDao.getAll();
		Airport airport1 = new Airport("England", "London", "London airport", "LND");
		Airport airportSaved = airportDao.save(airport1);
		LocalDateTime localDate = LocalDateTime.of(2021, 12, 21, 12, 12, 10);
		LocalDateTime localDate1 = LocalDateTime.of(2021, 12, 21, 21, 15, 38);
		Flight flightToDelete = new Flight(null, LocalDate.now(), airportSaved.getId(), airportSaved.getId(),
				"Slovak Airlines", "Bussines", 40, localDate, localDate1, customers);
		Flight saved = flightDao.save(flightToDelete);
		List<Flight> all = flightDao.getByAirport(airportSaved);
		boolean success = flightDao.delete(saved.getId());
		assertTrue(success);
		assertEquals(all.size() - 1, flightDao.getByAirport(airportSaved).size());
		assertFalse(flightDao.delete(saved.getId()));
		assertFalse(flightDao.delete(-1L));

	}
	
	@Test
    void testIsFull() {
         Airport airport1 = new Airport("England", "London", "London airport", "LND");
         Airport airportSaved = airportDao.save(airport1); 
         LocalDateTime localDate = LocalDateTime.of(2021, 12, 21, 12, 12, 10); 
         LocalDateTime localDate1 = LocalDateTime.of(2021, 12, 21, 21, 15, 38);
         Flight flight = new Flight(null,
         LocalDate.now(), airportSaved.getId(), airportSaved.getId(), "Slovak Airlines", "Bussines", 3, localDate, localDate1); 
         LocalDate date = LocalDate.of(2021, 12, 21); 
         Customer customer = new Customer("Tester","Of delete", date, "Male", 24L, "Trencinska ulica"); 
         Customer savedCustomer = customerDao.save(customer); 
         List<Customer> customers = new ArrayList<>();
         customers.add(savedCustomer); 
         flight.setCustomers(customers);
         Flight saved = flightDao.save(flight); 
         System.out.println(saved); 
         boolean trueOrFalse = flightDao.isFull(saved); 
         System.out.println(trueOrFalse);
         if(saved.getCustomers().size() == saved.getNumberOfSeats()) {
             assertTrue(trueOrFalse);
         }
         else {
             assertFalse(trueOrFalse);
         }
         
    }
//
//	@Test
//	void testUpdate() {
//		Airport airport1 = new Airport("England1", "London", "London airport", "LND");
//		Airport airportSaved = airportDao.save(airport1);	
//		//System.out.println(airportSaved);
//		List<Customer> customers = customerDao.getAll();
//		
//		LocalDateTime localDate = LocalDateTime.of(2021, 12, 21, 12, 12, 10);
//		LocalDateTime localDate1 = LocalDateTime.of(2021, 12, 21, 21, 15, 38);
//		LocalDate date1 = LocalDate.now();
//		Flight flight1 = new Flight(savedFlight.getId(), date1, airportSaved.getId(), airportSaved.getId(), "Slovak Airlines", "Bussines", 1, localDate, localDate1);
//		
//		LocalDate date = LocalDate.of(2003, 9, 21);
//		Customer customer = new Customer("Tester", "Of update", date, "Male", 24L, "Presovska ulica");
//		Customer savedCustomer1 = customerDao.save(customer);
//		
//		List<Customer> list = new ArrayList<>();
//		list.add(savedCustomer.get(0));
//		list.add(savedCustomer.get(1));
//		list.add(savedCustomer1);
//		//System.out.println(list);
//		flight1.setCustomers(list);	
//		//System.out.println(flight1);
//		Flight saved = flightDao.save(flight1);
//		//System.out.println("controla "+saved);
//		//System.out.println("controla "+flight1);
//		assertEquals(saved, flight1);
//		List<Flight> list1 = flightDao.getByAirport(airportSaved);
//		//System.out.println(list1);
//		boolean found = false;
//		
//		for (Flight flight23 : list1) {
//			if (flight23.getId().equals(saved.getId())) {
//				found = true;
//                assertEquals(saved, flight23);
//				break;
//			}
//		}
//		assertTrue(found);		
//		
//		flightDao.save(savedFlight); // nahradme tudentov nasp na pvodnch
//		flight1.setId(-1L);
//		assertThrows(EntityNotFoundException.class, new Executable() {
//			@Override
//			public void execute() throws Throwable {
//				flightDao.save(flight1);
//			}
//		});
//		assertThrows(NullPointerException.class, new Executable() {
//			@Override
//			public void execute() throws Throwable {
//				flightDao.save(null);
//			}
//		});

		
		//Flight saved = flightDao.save(flight1);
		
		
		
		
		
//		saved.getCustomers().add(savedCustomer1);
//		System.out.println(saved);
//		flightDao.save(saved);
//		List<Customer> list = customerDao.getByFlightId(saved.getId());
//		System.out.println(list.size());
//		
//
//		
//		saved.getCustomers().add(customers.get(0));
//		System.out.println(saved);
//		flightDao.save(saved);
//		System.out.println(saved);
//		assertEquals(saved, flight1);
//		assertEquals(list.size() + 1, customerDao.getByFlightId(saved.getId()).size());
		
	
//	
//	@Test
//	void testGetByAirport() {
//		Airport airport1 = new Airport("Chech Republik", "Praga", "Pragues airport", "PRG");
//		Airport airportSaved = airportDao.save(airport1);
//		LocalDateTime localDate = LocalDateTime.of(2021, 12, 21, 12, 12, 10);
//		LocalDateTime localDate1 = LocalDateTime.of(2021, 12, 21, 21, 15, 38);
//		LocalDate date1 = LocalDate.now();
//		List<Customer> customers = customerDao.getAll();
//		Flight flight1 = new Flight(savedFlight.getId(), date1, airportSaved.getId(), airportSaved.getId(), "Slovak Airlines", "Bussines", 1, localDate, localDate1);
//		LocalDate date = LocalDate.of(2003, 9, 21);
//		Customer customer = new Customer("Tester", "Of airport", date, "Male", 24L, "Warsavska ulica");
//		Customer savedCustomer1 = customerDao.save(customer);
//		
//		List<Customer> list = new ArrayList<>();
//		list.add(savedCustomer.get(0));
//		list.add(savedCustomer1);
//		
//		flight1.setCustomers(list);	
//		Flight saved = flightDao.save(flight1);
//		List<Flight> list1 = flightDao.getByAirport(airportSaved);
//		assertTrue(list1.size() > 0);
//		int count = 0;
//		Flight fromDb = null;
//		for (Flight a: list1) {
//			if (a.getId().equals(saved.getId())) {
//				count++;
//				fromDb = a;
//			}
//		}
//		assertEquals(1, count);
//		for (Customer savedCust: list) {
//			boolean found = false;
//			for (Customer customerInDb: fromDb.getCustomers()) {
//				if (savedCust.equals(customerInDb)) {
//					found = true;
//				}
//			}
//			assertTrue(found);
//		}
//		assertThrows(NullPointerException.class, new Executable() {
//			@Override
//			public void execute() throws Throwable {
//				flightDao.getByAirport(null);
//			}
//		});
//	}

}
