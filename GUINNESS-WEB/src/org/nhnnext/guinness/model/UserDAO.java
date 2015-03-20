package org.nhnnext.guinness.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;


public class UserDAO {
	public Connection getConnection(){
		String url = "jdbc:mysql://localhost:3306/GUINNESS";
		String id = "link413";
		String pw = "link413";
		
		try {
		Class.forName("com.mysql.jdbc.Driver");
		return DriverManager.getConnection(url,id,pw);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	public void createUser(User user) throws SQLException{
		String sql = "insert into USERS values(?,?,?,?,default)";
		
		String userId = user.getUserId();
		UserDAO userDao = new UserDAO();

		if (userDao.readUser(userId) != null) {
			System.out.println("존재하는 userId 입니다!");
		}
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		try { 
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user.getUserId());
			pstmt.setString(2, user.getUserName());
			pstmt.setString(3, user.getUserPassword());
			pstmt.setString(4, null);
			
			pstmt.executeUpdate();
			
		} finally {
			if(pstmt != null) {
				pstmt.close();
			}
			if(conn != null){
				conn.close();
			}
		}
		
	}
	
	public User readUser(String userId) throws SQLException{
		String sql = "select * from USERS where userId=?";
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try { 
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			
			rs = pstmt.executeQuery();

			if (rs.next()) {
				User user = new User(rs.getString("userId"), rs.getString("userPassword"), rs.getString("userName"));
				return user;
			}
		} finally {
			if(pstmt != null) {
				pstmt.close();
			}
			if(conn != null){
				conn.close();
			}
			if(rs != null){
				rs.close();
			}
		}
		
		return null;
	}
	
	public void updateUser(){}
	
	public void deleteUser(){}
}