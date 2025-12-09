package fr.insa.projetIntegrateur.UserService.model;

public class User {
	//-------------------------------- FIELDS --------------------------------//
	
	//id field
	private int idUser;
	
	//'classic' fields
	private String firstName;
	private String lastName;
	private String password;
	private String email;
	
	//profile related fields
	private int idProfileDefault; //id of the preferred/actual default user profile among the FIXED ones!!!
	//private ??? customProfile; //custom profile of the user that changes depending on feedback 
	
	//other fields
	
	//-------------------------------- CONSTRUCTORS --------------------------------//
	
	public User(String firstName, String lastName, String password, String email) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.password = password;
		this.email = email;
	}
	
	public User() {
	}
	
	//-------------------------------- GETTERS/SETTERS --------------------------------//
	
	public int getIdUser() {
		return idUser;
	}
	
	public void setIdUser(int idUser) {
		this.idUser = idUser;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getIdProfileDefault() {
		return idProfileDefault;
	}

	public void setIdProfileDefault(int idProfileDefault) {
		this.idProfileDefault = idProfileDefault;
	}
	
}
