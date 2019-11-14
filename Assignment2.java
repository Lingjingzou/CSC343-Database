import java.sql.*;
import java. util. Date;
import java. sql. Timestamp;
// You should use this class so that you can represent SQL points as
// Java PGpoint objects.
import org.postgresql.geometric.PGpoint;

public class Assignment2 {

   // A connection to the database
   Connection connection;

   Assignment2() throws SQLException {
      try {
         Class.forName("org.postgresql.Driver");
      } catch (ClassNotFoundException e) {
         e.printStackTrace();
      }
   }

  /**
   * Connects and sets the search path.
   *
   * Establishes a connection to be used for this session, assigning it to
   * the instance variable 'connection'.  In addition, sets the search
   * path to uber, public.
   *
   * @param  url       the url for the database
   * @param  username  the username to connect to the database
   * @param  password  the password to connect to the database
   * @return           true if connecting is successful, false otherwise
   */
   public boolean connectDB(String URL, String username, String password) {
      // Implement this method!
      try {
         connection = DriverManager.getConnection(URL , username, password);
         PreparedStatement stat = connection.prepareStatement("SET SEARCH_PATH TO uber, pubic");
         stat.execute();
     } catch(SQLException se) {
         return false;
     }
   //   System.out.println("Connected to database");
     return true;
   }

  /**
   * Closes the database connection.
   *
   * @return true if the closing was successful, false otherwise
   */
   public boolean disconnectDB() {
      // Implement this method!
      if(connection != null) {
         try {
             connection.close();
         } catch (SQLException se) {
             return false;
         }
     }
     return true;
   }
   
   /* ======================= Driver-related methods ======================= */

   /**
    * Records the fact that a driver has declared that he or she is available 
    * to pick up a client.  
    *
    * Does so by inserting a row into the Available table.
    * 
    * @param  driverID  id of the driver
    * @param  when      the date and time when the driver became available
    * @param  location  the coordinates of the driver at the time when 
    *                   the driver became available
    * @return           true if the insertion was successful, false otherwise. 
    */
   public boolean available(int driverID, Timestamp when, PGpoint location) {
      // Implement this method!
      try {
         String queryString = "INSERT INTO Available (driver_id, datetime, location)" 
                              + " VALUES (?, ?, ?)";
         
         PreparedStatement st = connection.prepareStatement(queryString);
         st.setInt(1, driverID);
         // System.err.println("Set driver ID!");
         st.setTimestamp(2, when);
         // System.err.println("Set datetime!");
         st.setObject(3, location);
         // System.err.println("Set location!");
         st.executeUpdate();

         // PreparedStatement fis = connection.prepareStatement("SELECT * FROM Available");
         // ResultSet fi = fis.executeQuery();

         // while (fi.next()) {
         //    System.out.println("have: " + fi.getInt("driver_id") + fi.getTimestamp("datetime") + fi.getObject("location"));
         //    }

         return true;

      } catch (SQLException e) {
         System.err.println("Got an exception!");
         System.err.println(e.getMessage());
         e.printStackTrace();
      }
      return false;
   }

   /**
    * Records the fact that a driver has picked up a client.
    *
    * If the driver was dispatched to pick up the client and the corresponding
    * pick-up has not been recorded, records it by adding a row to the
    * Pickup table, and returns true.  Otherwise, returns false.
    * 
    * @param  driverID  id of the driver
    * @param  clientID  id of the client
    * @param  when      the date and time when the pick-up occurred
    * @return           true if the operation was successful, false otherwise
    */
   public boolean picked_up(int driverID, int clientID, Timestamp when) {
      // Implement this method!

      int request_ID = 0;
      try {

         String que = "SELECT * FROM Dispatch";
         PreparedStatement Statement = connection.prepareStatement(que);
         ResultSet re = Statement.executeQuery();

         while (re.next()) {
            if (re.getInt("driver_id") == driverID ){
                  request_ID = re.getInt("request_id");
                  // System.out.println("drive in dis found!");
                  break;
               }
            return false;
         }
         // System.out.println("Request : " + request_ID + "is found !");

         String query = "SELECT * FROM Pickup";

         PreparedStatement pStatement = connection.prepareStatement(query);
         ResultSet result = pStatement.executeQuery();

         while (result.next()) {
            if (result.getInt("request_id") == request_ID){
                  // System.out.println("pickup exist");
                  return false;
               }
         }

         PreparedStatement ps = connection.prepareStatement("INSERT INTO Pickup (request_id, datetime) VALUES (?, ?)");
         ps.setInt(1, request_ID);
         ps.setTimestamp(2, when);
         ps.executeUpdate();

         // PreparedStatement fis = connection.prepareStatement("SELECT * FROM Pickup");
         // ResultSet fi = fis.executeQuery();

         // while (fi.next()) {
         //    System.out.println("pick : r_id : " + fi.getInt("request_id") + fi.getTimestamp("datetime"));
         //    }
         
         return true;

      } catch (SQLException e) {
         System.err.println("Got an exception!");
         System.err.println(e.getMessage());
         // e.printStackTrace();
      }
      return false;
   }
   
   /* ===================== Dispatcher-related methods ===================== */

   /**
    * Dispatches drivers to the clients who've requested rides in the area
    * bounded by NW and SE.
    * 
    * For all clients who have requested rides in this area (i.e., whose 
    * request has a source location in this area), dispatches drivers to them
    * one at a time, from the client with the highest total billings down
    * to the client with the lowest total billings, or until there are no
    * more drivers available.
    *
    * Only drivers who (a) have declared that they are available and have 
    * not since then been dispatched, and (b) whose location is in the area
    * bounded by NW and SE, are dispatched.  If there are several to choose
    * from, the one closest to the client's source location is chosen.
    * In the case of ties, any one of the tied drivers may be dispatched.
    *
    * Area boundaries are inclusive.  For example, the point (4.0, 10.0) 
    * is considered within the area defined by 
    *         NW = (1.0, 10.0) and SE = (25.0, 2.0) 
    * even though it is right at the upper boundary of the area.
    *
    * Dispatching a driver is accomplished by adding a row to the
    * Dispatch table.  All dispatching that results from a call to this
    * method is recorded to have happened at the same time, which is
    * passed through parameter 'when'.
    * 
    * @param  NW    x, y coordinates in the northwest corner of this area.
    * @param  SE    x, y coordinates in the southeast corner of this area.
    * @param  when  the date and time when the dispatching occurred
    */
   public void dispatch(PGpoint NW, PGpoint SE, Timestamp when) {
      // Implement this method!

   }

   public static void main(String[] args) {
      // You can put testing code in here. It will not affect our autotester.
      System.out.println("Hello!");

      Assignment2 a2;
      try {
        a2 = new Assignment2();
        String url = "jdbc:postgresql://localhost:5432/csc343h-zoulingj";
        boolean conn =a2.connectDB(url, "zoulingj", "");
        if (conn == true){System.out.println("Connected!");}
        
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println(timestamp);
      //   Timestamp ts = new Timestamp(time);
      //   Timestamp ts = new Timestamp(System.currentTimeMillis());
        //Test available
        boolean result = a2.available(12345,timestamp , new PGpoint(1, 2.5));
        if (result == true){System.out.println("Finish available!");}

        //Test pick_up
        System.out.println("Test Pick_up:");
        boolean result2 =a2.picked_up(12345, 99, timestamp);
        if (result2 == true){System.out.println("Finish Pick Up!");}
      
       //   // Test dispatch
      //   System.out.println("Test dispatch:");
      //   a2.dispatch(new PGpoint(2, 10), new PGpoint(100, 100), new Timestamp(new Date().getTime()));

        boolean discon =a2.disconnectDB();
        if (discon == true){System.out.println("Disconnected!");}
      

      } catch(Exception e){   
        System.out.println("Test failed");
        e.printStackTrace();
      }
      System.out.println("Boo!");
   }

}
