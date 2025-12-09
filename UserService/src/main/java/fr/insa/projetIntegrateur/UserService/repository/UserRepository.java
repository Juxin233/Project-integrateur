package fr.insa.projetIntegrateur.UserService.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import fr.insa.projetIntegrateur.UserService.model.User;

@Component
public class UserRepository {
	
	//-------------------------------- FIELDS & CONSTRUCTORS --------------------------------//

	private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
	    
	//-------------------------------- GET METHODS --------------------------------//

    public List<User> getAllUsers() {
        return jdbcTemplate.query("SELECT * FROM User", new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                User e = new User();
                e.setIdUser(rs.getInt("idUser"));
                e.setFirstName(rs.getString("firstName"));
                e.setLastName(rs.getString("lastName"));
                e.setPassword(rs.getString("password"));
                e.setEmail(rs.getString("email"));

                //fixed profile
                e.setIdProfileDefault(rs.getInt("idProfileDefault"));
                
                //custom profile
                
                return e;
            }
        });
    }
    
    public User getUserById(int id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM User WHERE idUser = ?",
                new Object[]{id},
                (rs, rowNum) -> {
                    User e = new User();
                    e.setIdUser(rs.getInt("idUser"));
                    e.setFirstName(rs.getString("firstName"));
                    e.setLastName(rs.getString("lastName"));
                    e.setPassword(rs.getString("password"));
                    e.setEmail(rs.getString("email"));

                    //fixed profile
                    e.setIdProfileDefault(rs.getInt("idProfileDefault"));
                    
                    //custom profile
                    
                    return e;
                }
        );
    }
    
    public List<User> getUserByLastName(String lastName) {
        return jdbcTemplate.query(
                "SELECT * FROM User WHERE lastName = ?",
                new Object[]{lastName},
                (rs, rowNum) -> {
                    User e = new User();
                    e.setIdUser(rs.getInt("idUser"));
                    e.setFirstName(rs.getString("firstName"));
                    e.setLastName(rs.getString("lastName"));
                    e.setPassword(rs.getString("password"));
                    e.setEmail(rs.getString("email"));
                    
                    //fixed profile
                    e.setIdProfileDefault(rs.getInt("idProfileDefault"));
                    
                    //custom profile
                    
                    return e;
                }
        );
    }
	    
	//-------------------------------- POST METHODS --------------------------------//
    
    public User createUser(User user) {
        String sql = "INSERT INTO User (firstName, lastName, password, email) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        //we do not define the profiles at this stage since this is a brand new user
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] {"idUser"});
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(4, user.getPassword());
            ps.setString(4, user.getEmail());
            return ps;
        }, keyHolder);

        // Set the generated ID in the User object
        user.setIdUser(keyHolder.getKey().intValue());

        return user;
    }
	    
	//-------------------------------- PUT METHODS --------------------------------//
	    
	
	    
	//-------------------------------- DELETE METHODS --------------------------------//
	    
    public int deleteUser(int idUser) {
        String sqlDeleteUser = "DELETE FROM User WHERE idUser = ?";
        return jdbcTemplate.update(sqlDeleteUser, idUser);
    }
    
}
