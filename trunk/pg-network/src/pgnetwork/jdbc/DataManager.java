package pgnetwork.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataManager {

	private Connection con;
	private int currentSweepEntry = 0;
	private int currentSweep = 0;
	
	private static final DataManager self = new DataManager();
	
	private DataManager(){
	
	}
	
	public static DataManager getInstance(){
		return self;
	}
	
	public void openConnection(){
		
		try {
			
			Class.forName( "com.mysql.jdbc.Driver" ).newInstance(); 
			
			con = DriverManager.getConnection("jdbc:mysql://localhost/pgnetwork", "pgnetwork", "pgnetwork");
			
			Statement stmt = con.createStatement();
			String query = "select max(id) from sweep;";
			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			currentSweep = rs.getInt(1);
			query = "select max(id) from sweep_entry;";
			rs = stmt.executeQuery(query);
			rs.next();
			currentSweepEntry = rs.getInt(1);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void newSweep(boolean isRational, double cost2, double initDensity, double initSegregation, double initCoopRate, double learning1, double learning2){
		currentSweep++;
		
		try {
			Statement stmt = con.createStatement();
			
			String query = "INSERT INTO ";
			query += "sweep (id, rational, cost_2, init_density, init_segregation, init_coop_rate, learning_1, learning_2)";
			query += " VALUES ('" + currentSweep + "', '" + (isRational? 1 : 0) + "', '" + cost2 + "', '" + initDensity + "', '" + initSegregation + "', '" + initCoopRate + "', '" + learning1 + "', '" + learning2 + "')";
//			System.out.println( "Query: " + query );
			stmt.executeUpdate( query );
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void save(double startDensity, double startSegregation, double startCoopRate, double coopRate, double density, double segregation, int[] strategyChanges){
		
		currentSweepEntry++;
		
		try {
		
			Statement stmt = con.createStatement();
			
			String query = "INSERT INTO ";
			query += "sweep_entry (id, sweep_id, start_density, start_segregation, start_coop_rate, density, segregation, coop_rate, ch_coop_coop, ch_coop_def, ch_def_coop, ch_def_def)";
			query += " VALUES ('" + currentSweepEntry + "', '" + currentSweep + "', '" + startDensity + "', '" + startSegregation + "', '" + startCoopRate + "', '" + density + "', '" + segregation + "', '" + coopRate + "', '" + strategyChanges[0] + "', '" + strategyChanges[1] + "', '" + strategyChanges[2] + "', '" + strategyChanges[3] + "')";
//			System.out.println( "Query: " + query );
			stmt.executeUpdate( query );
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void closeConnection(){
		try {
			if (con != null)
				con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
