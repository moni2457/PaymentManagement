import java.sql.*;
import java.util.*;

	public class PaymentManagement {

		public static void main(String[] args) {
			try{  
				Class.forName("com.mysql.cj.jdbc.Driver");  
				Connection database =DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306?serverTimezone=UTC", "mshah", "B00830791");
				reconcilePayments(database);
				unpaidOrders(database);
				unknownPayments(database);
				database.close(); 
			}
			catch(Exception e)
			{
				System.out.println(e);
			} 
		}
		
		//mapping the orders table with payment table
		public static void reconcilePayments(Connection database) {
			try{
				Statement stmt=database.createStatement();
				stmt.execute("use mshah;");
				ResultSet rs = stmt.executeQuery("alter table orders add column checkNumber varchar(100);"); 
				ResultSet rs1 = stmt.executeQuery("alter table orders add column orderPaymentCompleted varchar(100);");
				ResultSet rs2 = stmt.executeQuery("alter table orders add column totalAmount float4;");
				ResultSet rs3 = stmt.executeQuery("update orders o join (select orderNumber,sum(orderdetails.quantityOrdered*orderdetails.priceEach) as totalSales from orderdetails group by orderNumber) j on  o.orderNumber = j.orderNumber set o.totalAmount = j.totalSales;");
				ResultSet rs4 = stmt.executeQuery("update orders o join payments p on o.customerNumber = p.customerNumber set o.checkNumber = p.checkNumber where floor(o.totalAmount) = floor(p.amount);");
				ResultSet rs5 = stmt.executeQuery("update orders o  cross join payments p set o.orderPaymentCompleted = 'YES' where floor(o.totalAmount) = floor(p.amount) and o.checkNumber = p.checkNumber;");
				rs.close();
				rs1.close();
				rs2.close();
				rs3.close();
				rs4.close();
				rs5.close();
				stmt.close();
			}
			catch(Exception e) {
				System.out.println(e);
			}
			
		}
		
		public boolean payOrder(Connection database, float amount, String cheque_number, ArrayList<Integer> orders) {
		try {
			Statement stmt=database.createStatement();
			stmt.execute("use mshah;");
			ResultSet rs = stmt.executeQuery("alter table payments add column validPayment varchar(100);");
			rs.close();
			stmt.close();
		}
		catch(Exception e) {
			System.out.println(e);
		}
			return true;	
		}
		
		//ArrayList of orderNumber which have no record of payment
		public static ArrayList<Integer> unpaidOrders(Connection database) {
			try {
				ArrayList<Integer> orderNumbers = new ArrayList<Integer>();
					Statement stmt=database.createStatement();
					stmt.execute("use mshah;");
					ResultSet rs = stmt.executeQuery("select orderNumber from orders where orderPaymentCompleted is not null and status !='Cancelled' and status!='Disputed';"); 
					while(rs.next()) {
						orderNumbers.add(new Integer(rs.getString("orderNumber")));
					}
					stmt.close();
					rs.close();
					return orderNumbers;
			}
			catch(Exception e) {
				System.out.println(e);
			}
			return null;
			
		}
		
		//Arraylist of checknumber which are not managed in orders table
		public static ArrayList<String> unknownPayments(Connection database){
			try {
				 ArrayList<String> checkNumbers = new ArrayList<String>();
					Statement stmt=database.createStatement();
					stmt.execute("use mshah;");
					ResultSet rs = stmt.executeQuery("SELECT p.checkNumber FROM payments p left outer join orders o on o.checkNumber = p.checkNumber where o.checkNumber is null;"); 
					while(rs.next()) {
						checkNumbers.add(rs.getString("checkNumber"));
					}
					stmt.close();
					rs.close();
					return checkNumbers;
			}
			catch(Exception e) {
				System.out.println(e);
			}
			return null;	
			}
	}