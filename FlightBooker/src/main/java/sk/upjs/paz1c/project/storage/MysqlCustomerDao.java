package sk.upjs.paz1c.project.storage;


import java.sql.ResultSet;


import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import sk.upjs.paz1c.project.storage.EntityNotFoundException;
import sk.upjs.paz1c.project.storage.EntityUndeleteableException;
import sk.upjs.paz1c.project.storage.Customer;
import sk.upjs.paz1c.project.storage.Customer;

public class MysqlCustomerDao implements CustomerDao {

	private JdbcTemplate jdbcTemplate;

	public MysqlCustomerDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private class CustomerRowMapper implements RowMapper<Customer> {
		@Override
		public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
			long id = rs.getLong("id");
			String name = rs.getString("name");
			String surname = rs.getString("surname");
			LocalDate dateOfBirth = rs.getDate("date_Of_Birth").toLocalDate();
			String gender = rs.getString("gender");
			long phoneNumber = rs.getLong("phoneNumber");
			String adress = rs.getString("adress");
			return new Customer(id, name, surname, dateOfBirth, gender, phoneNumber, adress);
		}
	}

	@Override
	public List<Customer> getAll() {
		String sql = "SELECT id, name, surname, date_Of_Birth, gender, phoneNumber, adress FROM customer";
		return jdbcTemplate.query(sql, new CustomerRowMapper());
	}

	@Override
	public Customer getById(long id) {
		String sql = "SELECT id, name, surname, date_Of_Birth, gender, phoneNumber, adress FROM customer WHERE id = " + id;
		try {
			return jdbcTemplate.queryForObject(sql, new CustomerRowMapper());
		} catch (EmptyResultDataAccessException e) {
			throw new EntityNotFoundException("Customer with id " + id + " was not found in Database", e);
		}
	}

	@Override
	public List<Customer> getByFlightId(long flight_id) {
		String sql = "SELECT c.id, c.name, c.surname , c.date_Of_Birth, c.gender, c.phoneNumber, c.adress from flight_customer fc\n"
				+ "	JOIN customer c ON fc.customer_id = c.id\n"
				+ "		WHERE fc.flight_id = "
				+ flight_id;
		return jdbcTemplate.query(sql, new CustomerRowMapper());
	}

	@Override
	public Customer save(Customer customer) throws EntityNotFoundException, NullPointerException {
		if (customer.getId() == null) { // INSERT
			SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate);
			insert.withTableName("customer");
			insert.usingGeneratedKeyColumns("id");
			insert.usingColumns("name", "surname", "date_Of_Birth", "gender", "phoneNumber", "adress");

			Map<String, Object> values = new HashMap<>();
			values.put("name", customer.getName());
			values.put("surname", customer.getSurname());
			values.put("date_Of_Birth", customer.getDateOfBirth());
			values.put("gender", customer.getGender());
			values.put("phoneNumber", customer.getPhoneNumber());
			values.put("adress", customer.getAdress());

			return new Customer(insert.executeAndReturnKey(values).longValue(), customer.getName(),
					customer.getSurname(), customer.getDateOfBirth(), customer.getGender(),
					customer.getPhoneNumber(), customer.getAdress());
				
		} else { // UPDATE
			String sql = "UPDATE customer SET name = ?, "
					+ "surname = ?, date_Of_Birth = ?, gender = ?, phoneNumber = ?, adress = ? "
					+ "WHERE id = ?";
			int changed = jdbcTemplate.update(sql, customer.getName(), customer.getSurname(), customer.getDateOfBirth(),
					customer.getGender(), customer.getPhoneNumber(), customer.getAdress(),
					customer.getId());
			if (changed == 1) {
				return customer;
			} else {
				throw new EntityNotFoundException("Customer with id " + customer.getId() + " not found in DB!");
			}
		}
	}

	@Override
	public Customer delete(long idCustomer) {
		Customer customer = getById(idCustomer);
			jdbcTemplate.update("DELETE FROM customer WHERE id = " + idCustomer);
		return customer;
	}
	
}
