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

      String query;
      PreparedStatement pStatement;
      int maxBillings = -1;
      int reqId = -1;
      int clientId = -1;
      PGpoint location = NW;

   try{

      // get all requests that have not yet been picked up
      query = "create view queuedRequests as select request_id from ((select request_id from Pickup) " +
      "EXCEPT (select request_id from Request)) as t1 ";
   pStatement = connection.prepareStatement(query);
   ResultSet queuedRequests = pStatement.executeQuery();

   // get client with total billings
   query = "create view totalBillings as select client_id, sum(amount) as billings from Request r, " +
      "Billed b where r.request_id = b.request_id group by client_id";
   pStatement = connection.prepareStatement(query);
   ResultSet totalBillings = pStatement.executeQuery();

   // join queuedRequests and totalBillings
   query = "create view requestWithBillings as select request_id, client_id, billings, location from " + 
      "queuedRequests q, Requests r, totalBillings t, Place p where q.request_id = r.request_id and " + 
      "r.client_id = t.client_id and r.source = p.name";
   pStatement = connection.prepareStatement(query);
   ResultSet requestWithBillings = pStatement.executeQuery();

   // set up the boundary
   double min_x;
   double max_x;
   double min_y;
   double max_y;
   if (NW.x <= SE.x) {
      min_x = NW.x;
      max_x = SE.x;
   } else { 
      min_x = SE.x;
      max_x = NW.x;
   }

   if (NW.y <= SE.y) {
      min_y = NW.y;
      max_y = SE.y;
   } else { 
      min_y = SE.y;
      max_y = NW.y;
   }

   // find the highest priority client within the boundary
   while (requestWithBillings.next()) {
      int currentBillings = requestWithBillings.getInt("billings");
      int currentReqId = requestWithBillings.getInt("request_id");
      int currentClient = requestWithBillings.getInt("client_id");
      PGpoint currentLocation = (PGpoint) requestWithBillings.getObject("location");
      if ((min_x <= currentLocation.x && currentLocation.x <= max_x) && 
         (min_y <= currentLocation.y && currentLocation.y <= max_y)) {
         if (currentBillings > maxBillings) {
            maxBillings = currentBillings;
            reqId = currentReqId;
            location = currentLocation;
            clientId = currentClient;
         }
      }
   }

   // Add the client we found into the table
   query = "create table priorityClient (request_id integer, client_id integer, location point, total_billings real)";
   pStatement = connection.prepareStatement(query);
   ResultSet priorityClient = pStatement.executeQuery();

   pStatement = connection.prepareStatement("INSERT INTO priorityclient (request_id, client_id, location, total_billings) " +
      "VALUES (?, ?, ?, ?)");
   pStatement.setInt(1, reqId);
   pStatement.setInt(2, clientId);
   pStatement.setObject(3, location);
   pStatement.setObject(4, maxBillings);
   pStatement.executeUpdate();


   // get the latest availability declaration time of all drivers get the non latest availabilities of all drivers 
   query = "create view notLatest as select a.driver_id as driver_id, a.datetime as datetime, " + 
      "a.location as location from Available a1, Available a2 where a1.driver_id = a2.driver_id and a1.datetime < a2.datetime";
   pStatement = connection.prepareStatement(query);
   ResultSet notLatest = pStatement.executeQuery();

   // get the latest availabilities of all drivers
   query = "create view Latest as select driver_id, datetime, location from ((select driver_id, datetime, location " +
      "from Available) EXCEPT (select driver_id, datetime, location from notLatest)) as t1";
   pStatement = connection.prepareStatement(query);
   ResultSet latest = pStatement.executeQuery();

   // check if the latest availability declaration time is a time later than all times the driver is dispatched, put into a list
   query = "create view hasBeenDispatched as select d.driver_id, d.datetime, d.location from Latest l, " + 
      "Dispatch d where l.driver_id = d.driver_id and (d.datetime - l.datetime) > INTERVAL '0'";
   pStatement = connection.prepareStatement(query);
   ResultSet hasBeenDispatched = pStatement.executeQuery();

   // get the remaining non dispatched drivers that are available
   query = "create view hasNotBeenDispatched as select driver_id, datetime, location from " + 
      "((select driver_id, datetime, location from latest) EXCEPT (select driver_id, datetime, location " + 
      "from hasBeenDispatched)) as t1";
   pStatement = connection.prepareStatement(query);
   ResultSet hasNotBeenDispatched = pStatement.executeQuery();

   // set up available drivers within boundary table
   query = "create table driversAvailable (driver_id integer, location point)";
   pStatement = connection.prepareStatement(query);
   ResultSet driversAvailable = pStatement.executeQuery();

   // check if the latest availability declaration time is a time later than any time 
   // the driver is dispatched, put into a list if within bounds
   while (hasNotBeenDispatched.next()) {
      int driver_id = hasNotBeenDispatched.getInt("driver_id");
      PGpoint currentLocation = (PGpoint) hasNotBeenDispatched.getObject("location");
      if ((min_x <= currentLocation.x && currentLocation.x <= max_x) && 
         (min_y <= currentLocation.y && currentLocation.y <= max_y)) {
            pStatement = connection.prepareStatement("INSERT INTO driversAvailable (driver_id, location) " +
               "VALUES (?, ?)");
            pStatement.setInt(1, driver_id);
            pStatement.setObject(2, currentLocation);
            pStatement.executeUpdate();
         }
      }

   // set up minimum distance table
   query = "create table minDistance (driver_id integer, distance integer)";
   pStatement = connection.prepareStatement(query);
   ResultSet minDistance = pStatement.executeQuery();

   query = "create view distances as select request_id, client_id, driver_id, p.location <@> d.location as distance ," + 
      "d.location as car_location from priorityclient p, driversAvailable d";
   pStatement = connection.prepareStatement(query);
   ResultSet distances = pStatement.executeQuery();

   query = "create view withoutClosestDistance as select d1.request_id as request_id, d1.client_id as client_id, " + 
      "d1.driver_id as driver_id, d1.distance as distance, d1.car_location as car_location from distances d1, " + 
      "distances d2 where d1.driver_id != d2.driver_id and d1.distance > d2.distance";
   pStatement = connection.prepareStatement(query);
   ResultSet withoutClosestDistance = pStatement.executeQuery();

   query = "create view closestDistance as select request_id, client_id, driver_id, distance " +
      "from ((select request_id, client_id, driver_id, distance, car_location from distances) EXCEPT " + 
      "(select request_id, client_id, driver_id, distance, car_location from withoutClosestDistance)) as t1";
   pStatement = connection.prepareStatement(query);
   ResultSet closestDistance = pStatement.executeQuery();

   query = "select request_id, client_id, driver_id, car_location from closestDistance";
   pStatement = connection.prepareStatement(query);
   ResultSet toAdd = pStatement.executeQuery();

   if (toAdd.next()) {
      int req_id = toAdd.getInt("request_id");
      int driver_id = toAdd.getInt("driver_id");
      PGpoint car_loc = (PGpoint) toAdd.getObject("car_location");
      // insert driver of min distance into table
      pStatement = connection.prepareStatement("INSERT INTO Dispatch (request_id, driver_id, car_location, datetime) " +
         "VALUES (?, ?, ?, ?)");
      pStatement.setInt(1, req_id);
      pStatement.setInt(2, driver_id);
      pStatement.setObject(3, car_loc);
      pStatement.setTimestamp(4, when);
      pStatement.executeUpdate();
   }

      // while (avl.next()) {
      //    PreparedStatement ps = connection.prepareStatement("INSERT INTO Dispatch (request_id, driver_id, car_location, datetime) VALUES (?, ?, ?, ?)");
      //    ps.setInt(1, request_ID);
      //    ps.setTimestamp(2, driver_ID);
      //    ps.setObject(3, car_loc); //from available
      //    ps.setObject(4, when);
      //    ps.executeUpdate();
      // }

   } catch (SQLException e) {
      System.err.println("Got an exception!");
      System.err.println(e.getMessage());
      e.printStackTrace();
   }
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
      //   System.out.println(timestamp);
      //   Timestamp ts = new Timestamp(time);
      //   Timestamp ts = new Timestamp(System.currentTimeMillis());
        //Test available
        boolean result = a2.available(12345,timestamp , new PGpoint(1, 2.5));
        if (result == true){System.out.println("Finish available!");}

        //Test pick_up
        System.out.println("Test Pick_up:");
        boolean result2 =a2.picked_up(12345, 99, timestamp);
        if (result2 == true){System.out.println("Finish Pick Up!");}
      
         // Test dispatch
        System.out.println("Test dispatch:");
        boolean result3 = a2.dispatch(new PGpoint(1, 100), new PGpoint(100, 1), new Timestamp(new Date().getTime()));
        if (result2 == true){System.out.println("Finish Dispatch!");}

        boolean discon =a2.disconnectDB();
        if (discon == true){System.out.println("Disconnected!");}
      

      } catch(Exception e){   
        System.out.println("Test failed");
        e.printStackTrace();
      }
      System.out.println("Boo!");
   }

}
