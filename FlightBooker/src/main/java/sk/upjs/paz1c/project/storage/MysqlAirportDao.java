package sk.upjs.paz1c.project.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

public class MysqlAirportDao implements AirportDao {

	JdbcTemplate jdbcTemplate = new JdbcTemplate();

	public MysqlAirportDao(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Set<String> getAllCountries() {
		String sql = "SELECT country FROM airport";
		return jdbcTemplate.query(sql, new ResultSetExtractor<Set<String>>() {

			@Override
			public Set<String> extractData(ResultSet rs) throws SQLException, DataAccessException {
				Set<String> list = new HashSet<String>();
				Airport airport = null;
				while (rs.next()) {
					String countr = rs.getString("country");
					list.add(countr);
				}
				return list;
			}

		});
	}
	private class AirportRowMapper implements RowMapper<Airport> {
		@Override
		public Airport mapRow(ResultSet rs, int rowNum) throws SQLException {
			long id = rs.getLong("id");
			String country = rs.getString("country");
			String city = rs.getString("city");
			String airportName = rs.getString("airport_name");
			String airportAcronym = rs.getString("acronym_of_airport");
			return new Airport(id, country, city, airportName, airportAcronym);
		}
	}

	@Override
	public Airport getById(long id) throws EntityNotFoundException {
		String sql = "SELECT id, country, city, airport_name, acronym_of_airport FROM airport WHERE id = " + id;
		try {
			return jdbcTemplate.queryForObject(sql, new AirportRowMapper());
		} catch (EmptyResultDataAccessException e) {
			throw new EntityNotFoundException("Airport with id" + id + "was not found in Database", e);
		}
	}

	@Override
	public Airport save(Airport airport) throws EntityNotFoundException, NullPointerException {
		if (airport.getId() == null) { // INSERT
			SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate);
			insert.withTableName("airport");
			insert.usingGeneratedKeyColumns("id");
			insert.usingColumns("country", "city", "airport_name", "acronym_of_airport");
		
			Map<String, Object> values = new HashMap<>();
			values.put("country", airport.getCountry());
			values.put("city", airport.getCity());
			values.put("airport_name", airport.getAirportName());
			values.put("acronym_of_airport", airport.getAirportAcronym());
			
			try {
				return new Airport(insert.executeAndReturnKey(values).longValue(), airport.getCountry(),
						airport.getCity(), airport.getAirportName(), airport.getAirportAcronym());
			} catch (DataIntegrityViolationException e) {
				throw new EntityNotFoundException(
						"Cannot insert airport, airport with id " + airport.getId() + " not found", e);
			}
		}
			else { // UPDATE
				String sql = "UPDATE airport SET country = ?, "
						+ "city = ?, airport_name = ?, acronym_of_airport = ?"
						+ "WHERE id = ?";
				int changed = jdbcTemplate.update(sql, airport.getCountry(), airport.getCity(), 
						airport.getAirportName(), airport.getAirportAcronym(), airport.getId());
				if (changed == 1) {
					return airport;
				} else {
					throw new EntityNotFoundException("Airport with id " + airport.getId() + " not found in DB!");
				}
			}
	}

	@Override
	public Airport delete(long idAirport) throws EntityNotFoundException, NullPointerException {
		Airport airport = getById(idAirport);
		try {
			jdbcTemplate.update("DELETE FROM airport WHERE id = " + idAirport);
		} catch (DataIntegrityViolationException e) {
			throw new EntityUndeleteableException("Airport is a part of some attendance list, cannot be deleted", e);
		}
		return airport;
	}

	@Override
	public Set<String> getByCity(String city) {
		String sql = "SELECT * FROM airport WHERE city = " + " ' " + city + " ' ";
		return jdbcTemplate.query(sql, new ResultSetExtractor<Set<String>>() {

			public Set<String> extractData(ResultSet rs) throws SQLException, DataAccessException {
				Set<String> list = new HashSet<String>();
				Airport airport = null;
				while (rs.next()) {
					long id = rs.getLong("id");
					if (airport == null || airport.getId() != id) {
						String country = rs.getString("country");
						String city = rs.getString("city");
						String airportName = rs.getString("airport_name");
						String airportAcronym = rs.getString("acronym_of_airport");
						list.add(airportName);
					}
				}
				return list;
			}
		});
	}

	@Override
	public Set<String> getCityByCountry(String country) {
		return jdbcTemplate.query("SELECT city FROM airport WHERE country = " + "'" + country + "'",
				new ResultSetExtractor<Set<String>>() {

					@Override
					public Set<String> extractData(ResultSet rs) throws SQLException, DataAccessException {
						Set<String> list = new HashSet<String>();
						 while (rs.next()) {
			        			String city = rs.getString("city");
			                    list.add(city);
			                    }
			                return list;
					}
			
		});
	}

}
