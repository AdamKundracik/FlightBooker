package sk.upjs.paz1c.project.storage;

import java.sql.ResultSet;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

public class MysqlFlightDao implements FlightDao {

	private JdbcTemplate jdbcTemplate;

	public MysqlFlightDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public List<Flight> getByAirport(Airport airport) {
		String sql = "select flight.id as flight_id, date_of_flight, airport_from, airport_where, company_name, flight_class, number_of_seats, departure, arrival, customer.id, customer.name, surname, date_Of_Birth, gender, phoneNumber, adress from flight\n"
				+ "left outer join airport on airport.id = flight.airport_from\n"
				+ "left outer join flight_customer on flight.id = flight_customer.flight_id\n"
				+ "left outer join customer on flight_customer.flight_id = customer.id\n" + "where airport_from = "
				+ airport.getId() + ";";
		return jdbcTemplate.query(sql, new ResultSetExtractor<List<Flight>>() {

			@Override
			public List<Flight> extractData(ResultSet rs) throws SQLException, DataAccessException {
				List<Flight> list = new ArrayList<Flight>();
				Flight flight = null;
				while (rs.next()) {
					Long id = rs.getLong("flight_id");
					if (flight == null || flight.getId() != id) {
						LocalDate dateOfFlight = rs.getDate("date_of_flight").toLocalDate();
						Long from = rs.getLong("airport_from");
						Long where = rs.getLong("airport_where");
						String companyName = rs.getString("company_name");
						String flightClass = rs.getString("flight_class");
						Integer numberOfSeats = rs.getInt("number_of_seats");
						LocalDateTime departure = rs.getTimestamp("departure").toLocalDateTime();
						LocalDateTime arrival = rs.getTimestamp("arrival").toLocalDateTime();
						list.add(flight = new Flight(id, dateOfFlight, from, where, companyName, flightClass,
								numberOfSeats, departure, arrival));
					}
					if (rs.getString("name") == null) {
						continue;
					}
					long id_user = rs.getLong("id");
					String name = rs.getString("name");
					String surname = rs.getString("surname");
					LocalDate dateOfBirth = rs.getDate("date_Of_Birth").toLocalDate();
					String gender = rs.getString("gender");
					long phoneNumber = rs.getLong("phoneNumber");
					String adress = rs.getString("adress");
					Customer cust = new Customer(id, name, surname, dateOfBirth, gender, phoneNumber, adress);
					List<Customer> customers = flight.getCustomers();
					customers.add(cust);
					flight.setCustomers(customers);
				}
				return list;
			}
		});
	}

	@Override
	public Flight save(Flight flight) throws EntityNotFoundException, NullPointerException {
		if (flight == null) {
			throw new NullPointerException("Flight cannot be null");
		}

		if (flight.getId() == null) { // INSERT
			SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate);
			insert.withTableName("flight");
			insert.usingGeneratedKeyColumns("id");
			insert.usingColumns("date_of_flight", "airport_from", "airport_where", "company_name", "flight_class",
					"number_of_seats", "departure", "arrival");

			Map<String, Object> values = new HashMap<>();
			values.put("date_of_flight", flight.getDateOfFlight());
			values.put("airport_from", flight.getFrom());
			values.put("airport_where", flight.getWhere());
			values.put("company_name", flight.getCompanyName());
			values.put("flight_class", flight.getFlightClass());
			values.put("number_of_seats", flight.getNumberOfSeats());
			values.put("departure", flight.getDeparture());
			values.put("arrival", flight.getArrival());

			try {
				Long idinsert = insert.executeAndReturnKey(values).longValue();
				flight.setId(idinsert);
				// insertCustomers(flight.getCustomers(), idinsert);
				return flight;
			} catch (DataIntegrityViolationException e) {
				throw new EntityNotFoundException(
						"Cannot insert flight, customer with id " + flight.getCustomers() + " not found", e);
			}
		} else {
			String sql = "UPDATE flight SET date_of_flight = ?, "
					+ "airport_from = ?, airport_where = ?, company_name = ?, flight_class = ?, number_of_seats = ?, departure = ?, "
					+ "arrival = ? " + "WHERE id = ?";
			int changed = jdbcTemplate.update(sql, flight.getDateOfFlight(), flight.getFrom(), flight.getWhere(),
					flight.getCompanyName(), flight.getFlightClass(), flight.getNumberOfSeats(), flight.getDeparture(),
					flight.getArrival(), flight.getId());
			if (changed == 0) {
				throw new EntityNotFoundException("Flight with id " + flight.getId() + " not found in DB!");
			}
			jdbcTemplate.update("DELETE FROM flight_customer WHERE flight_id = ?", flight.getId());
			List<Customer> customer = DaoFactory.INSTANCE.getCustomerDao().getByFlightId(flight.getId());
			List<Customer> customersNotInFlight = new ArrayList<>();
			for (int i = 0; i < flight.getCustomers().size(); i++) {
				Customer temporaryCustomer = flight.getCustomers().get(i);
				if (!customer.contains(temporaryCustomer)) {
					customersNotInFlight.add(temporaryCustomer);
				}
			}
			// insertCustomers(customersNotInFlight, flight.getId());
			return flight;

		}
	}

	@Override
	public void insertCustomers(List<Customer> customers, long flightId) {
		if (customers == null || customers.size() == 0) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO flight_customer (customer_id, flight_id) VALUES ");
		for (Customer customer : customers) {
			sb.append('(').append(customer.getId()).append(',').append(flightId).append("),");
		}
		String sql = sb.substring(0, sb.length() - 1);
		jdbcTemplate.update(sql);
	}

	@Override
	public boolean delete(long id) {
		jdbcTemplate.update("DELETE FROM flight_customer WHERE flight_id = ?", id);
		String sql = "DELETE FROM flight WHERE id = " + id;
		int deleted = jdbcTemplate.update(sql);
		return deleted == 1;
	}

	@Override
	public boolean isFull(Flight flight) {
		String sql = "select count(customer_id) as pocet from flight_customer\n"
				+ "				join flight on flight.id = flight_customer.flight_id where flight_id = " + flight.getId();
		int count = jdbcTemplate.queryForObject(sql, new RowMapper<Integer>() {

			@Override
			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				int volne = rs.getInt("pocet");
				return volne;
			}

		});
		if (count == flight.getNumberOfSeats()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public List<Flight> getAll() {
		String sql = "SELECT id, date_of_flight, airport_from, airport_where, company_name, flight_class,"
				+ "number_of_seats, departure, arrival FROM flight";
		return jdbcTemplate.query(sql, new RowMapper<Flight>() {

			@Override
			public Flight mapRow(ResultSet rs, int rowNum) throws SQLException {
				long id = rs.getLong("id");
				LocalDate dateOfFlight = rs.getDate("date_of_flight").toLocalDate();
				Long airportFrom = rs.getLong("airport_from");
				Long airportWhere = rs.getLong("airport_where");
				String companyName = rs.getString("company_name");
				String flightClass = rs.getString("flight_class");
				Integer numberOfSeats = rs.getInt("number_of_seats");
				LocalDateTime departure = rs.getTimestamp("departure").toLocalDateTime();
				LocalDateTime arrival = rs.getTimestamp("arrival").toLocalDateTime();

				return new Flight(id, dateOfFlight, airportFrom, airportWhere, companyName, flightClass, numberOfSeats,
						departure, arrival);
			}

		});

	}

	@Override
	public List<Flight> fromAtoB(String from, String where) {
		String sql = "Select flight.id,date_of_flight,airport_from,airport_where,company_name,flight_class,number_of_seats,departure,arrival from flight\r\n"
				+ "   join airport as a1 on airport_from = a1.id\r\n"
				+ "   join airport as a2 on airport_where = a2.id\r\n"
				+ "   where a1.airport_name = ? and a2.airport_name = ?";

		List<Flight> listFlight = new ArrayList<Flight>();
		listFlight = jdbcTemplate.query(sql, new ResultSetExtractor<List<Flight>>() {

			@Override
			public List<Flight> extractData(ResultSet rs) throws SQLException, DataAccessException {
				List<Flight> flights = new ArrayList<Flight>();
				while (rs.next()) {
					Long id = rs.getLong("id");
					LocalDate dateOfFlight = rs.getDate("date_of_flight").toLocalDate();
					Long from = rs.getLong("airport_from");
					Long where = rs.getLong("airport_where");
					String companyName = rs.getString("company_name");
					String flightClass = rs.getString("flight_class");
					Integer numberOfSeats = rs.getInt("number_of_seats");
					LocalDateTime departure = rs.getTimestamp("departure").toLocalDateTime();
					LocalDateTime arrival = rs.getTimestamp("arrival").toLocalDateTime();
					flights.add(new Flight(id, dateOfFlight, from, where, companyName, flightClass, numberOfSeats,
							departure, arrival));
				}
				return flights;
			}
		}, from, where);

		if (listFlight.size() > 0) {
			return listFlight;
		} else {
			List<Flight> gh = new ArrayList<>();
			String sql1 = "Select flight.id,date_of_flight,airport_from,airport_where,company_name,flight_class,number_of_seats,departure,arrival from flight\r\n"
					+ "   join airport as a1 on airport_from = a1.id\r\n"
					+ "   join airport as a2 on airport_where = a2.id\r\n"
					+ "   where a1.airport_name = ? or a2.airport_name = ?";
			System.out.println("SQL");
			return jdbcTemplate.query(sql1, new ResultSetExtractor<List<Flight>>() {

				@Override
				public List<Flight> extractData(ResultSet rs) throws SQLException, DataAccessException {
					List<Flight> from_list = new ArrayList<Flight>();
					List<Flight> where_list = new ArrayList<Flight>();
					while (rs.next()) {
						Long id = rs.getLong("id");
						LocalDate dateOfFlight = rs.getDate("date_of_flight").toLocalDate();
						Long from1 = rs.getLong("airport_from");
						Long where1 = rs.getLong("airport_where");
						String companyName = rs.getString("company_name");
						String flightClass = rs.getString("flight_class");
						Integer numberOfSeats = rs.getInt("number_of_seats");
						LocalDateTime departure = rs.getTimestamp("departure").toLocalDateTime();
						LocalDateTime arrival = rs.getTimestamp("arrival").toLocalDateTime();
						Airport a = DaoFactory.INSTANCE.getAirportDao().getById(from1);
						Airport a2 = DaoFactory.INSTANCE.getAirportDao().getById(where1);
						System.out.println("EXTRACTOR");
						if (a.getAirportName().equals(from)) {
							from_list.add(new Flight(id, dateOfFlight, from1, where1, companyName, flightClass,
									numberOfSeats, departure, arrival));
							System.out.println("PRVE IF");
							System.out.println(from_list);
							System.out.println();
						}

						if (a2.getAirportName().equals(where)) {
							where_list.add(new Flight(id, dateOfFlight, from1, where1, companyName, flightClass,
									numberOfSeats, departure, arrival));
							System.out.println("DRUHY IF");
							System.out.println(where_list);
							System.out.println();
						}
					}
					for (int i = 0; i < from_list.size(); i++) {
						for (int j = 0; j < where_list.size(); j++) {
							if (from_list.get(i).getWhere().equals(where_list.get(j).getFrom())
									&& from_list.get(i).getArrival().isBefore(where_list.get(j).getDeparture())
									&& from_list.get(i).getArrival().isAfter(LocalDateTime.now())) {
								gh.add(from_list.get(i));
								gh.add(where_list.get(j));
								System.out.println("=");
							}
						}
					}
					return gh;
				}
			}, from, where);
		}
	}

	@Override
	public boolean isCustomerInFlight(Customer customer, Flight flight) {
		   String sql = "SELECT customer_id FROM flight_customer WHERE customer_id = " + customer.getId() +" AND flight_id = " + flight.getId();
	        return  jdbcTemplate.query(sql, new ResultSetExtractor<Boolean>() {
	            
	            @Override
	            public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
	                if(rs.next()) {
	                    return true;
	                }
	                else {
	                    return false;
	                }
	            }

	        });
	}
}
