package com.mbcc.templeapp.dao;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mbcc.templeapp.dto.Visitor;
import com.mbcc.templeapp.dto.VisitorLog;
import com.newrelic.agent.deps.ch.qos.logback.core.net.SyslogOutputStream;

public class VisitorMySqlDao {

	private static String DATABASE_URL="//localhost3306:root:sesame";
	
	
	

	public static Connection getConnection() throws URISyntaxException, SQLException {
		//URI dbUri = new URI(System.getenv("DATABASE_URL"));
		URI dbUri = new URI(DATABASE_URL);
		
		//String username = dbUri.getUserInfo().split(":")[0];
		//String password = dbUri.getUserInfo().split(":")[1];
		//String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
		String dbUrl = "jdbc:mysql://localhost:3306/visitordb"; 
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return DriverManager.getConnection(dbUrl, "root", "sesame");
	}

	public static List<Visitor> retrieveVisitorsDataIfExists(String phoneNumber)
			throws SQLException, URISyntaxException { System.out.println(phoneNumber);
		PreparedStatement retrieveVisitorsStatement = getConnection().prepareStatement("SELECT * FROM Visitor where phone=?");
		retrieveVisitorsStatement.setString(1, phoneNumber);
		ArrayList<Visitor> visitors = getVisitors(retrieveVisitorsStatement);
		return visitors;
		
		
	}
	
	public static List<VisitorLog> retrieveVisitorsDataForDateRange(Date from, Date to)
			throws SQLException, URISyntaxException {
		
		
		PreparedStatement retrieveVisitorsStatement = getConnection().prepareStatement("SELECT * FROM VisitorLog where dateofvisit BETWEEN ? AND ?");
		retrieveVisitorsStatement.setTimestamp(1, new Timestamp(from.getTime()));
		retrieveVisitorsStatement.setTimestamp(2, new Timestamp(to.getTime()));
		
		ArrayList<VisitorLog> visitors = getVisitorsLog(retrieveVisitorsStatement);
		return visitors;
	}
	
	public static List<Visitor> retrieveAllVisitors()
			throws SQLException, URISyntaxException {
		
		
		PreparedStatement retrieveVisitorsStatement = getConnection().prepareStatement("SELECT * FROM Visitor");
		
		ArrayList<Visitor> visitors = getVisitors(retrieveVisitorsStatement);
		return visitors;
	}
	
	public static List<VisitorLog> retrieveAllVisitorsLog( String startDate, String endDate)
			throws SQLException, URISyntaxException {

		//TODO if startDate and endDate are not null then construct the below select statement with between clause
		PreparedStatement retrieveVisitorsStatement = getConnection().prepareStatement("SELECT * FROM VisitorLog");
		ArrayList<VisitorLog> visitorsLog = getVisitorsLog(retrieveVisitorsStatement);
		return visitorsLog;
	}

	private static ArrayList<VisitorLog> getVisitorsLog(PreparedStatement retrieveVisitorsStatement) throws SQLException {
		ResultSet resultSet = retrieveVisitorsStatement.executeQuery();
		ArrayList<VisitorLog> visitorsLog = new ArrayList<VisitorLog>();

		while (resultSet.next()) {
			VisitorLog visitorLog = new VisitorLog();
			visitorLog.setFirstName(resultSet.getString(2));
			visitorLog.setLastName(resultSet.getString(3));
			visitorLog.setDateofvisit(resultSet.getDate(4));
			visitorsLog.add(visitorLog);
		}
		return visitorsLog;
	}

	private static ArrayList<Visitor> getVisitors(PreparedStatement retrieveVisitorsStatement) throws SQLException {
		ResultSet resultSet = retrieveVisitorsStatement.executeQuery();
		ArrayList<Visitor> visitors = new ArrayList<Visitor>();

		while (resultSet.next()) {
			Visitor visitor = new Visitor();
			visitor.setPhoneNumber(resultSet.getString(2));
			visitor.setFirstName(resultSet.getString(3));
			visitor.setLastName(resultSet.getString(4));
			visitors.add(visitor);
		}
		return visitors;
	}

	public static void initializeDatabase() throws SQLException, URISyntaxException {
		Statement stmt = getConnection().createStatement();

		String dropVisitorLog = "DROP TABLE IF EXISTS VisitorLog";
		stmt.executeUpdate(dropVisitorLog);
		
		String visitorLog = "CREATE TABLE VisitorLog " + "(id int(11) AUTO_INCREMENT, " + " first VARCHAR(255), "
				+ " last VARCHAR(255), " + " dateofvisit timestamp, " + " PRIMARY KEY ( id ))";

		stmt.executeUpdate(visitorLog);
		
		String dropVisitor = "DROP TABLE IF EXISTS Visitor";
		stmt.executeUpdate(dropVisitor);

		String visitors = "CREATE TABLE Visitor " + "(id int(11) AUTO_INCREMENT, " + " phone VARCHAR(8),"
				+ " first VARCHAR(255), " + " last VARCHAR(255), " + " member boolean, " + " PRIMARY KEY ( id ))";

		stmt.executeUpdate(visitors);
	}

	public static void insertVisitor(Visitor visitor) throws SQLException, URISyntaxException {
		PreparedStatement insertStatement = getConnection().prepareStatement("INSERT INTO Visitor(phone,first,last,member) values(?,?,?,?)");
		insertStatement.setString(1, visitor.getPhoneNumber());
		insertStatement.setString(2, visitor.getFirstName());
		insertStatement.setString(3, visitor.getLastName());
		insertStatement.setBoolean(4, visitor.isMember());
		insertStatement.executeUpdate();
		
		insertVisitorLog(visitor);	
	}
	
	public static void insertVisitorLog(Visitor visitor) throws SQLException, URISyntaxException {
		PreparedStatement insertStatement = getConnection().prepareStatement("INSERT INTO VisitorLog(first,last,dateofvisit) values(?,?,?)");
		insertStatement.setString(1, visitor.getFirstName());
		insertStatement.setString(2, visitor.getLastName());
		insertStatement.setTimestamp(3, new Timestamp(new Date().getTime()));
		insertStatement.executeUpdate();
	}

}
