import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import java.util.*;
import java.lang.*;
import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.LinkedHashMap;
import java.time.LocalDate;

//CLASSPATH=$CLASSPATH:mysql-connector-java-5.1.44-bin.jar

public class Lab6{
   private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
   private static Connection conn = null;
   public static void main(String[] args){
      try{
         Lab6 T1 = new Lab6();
         T1.demo1(); 
      }
      catch(SQLException e){
         System.out.println("failed");
      }
   }
   private void demo1() throws SQLException{
      System.out.println("Attempting to login...\n");
      login();
      checkTables();
      optionSelect();
   }

   private static void checkTables(){
      int rooms = 0;
      int reservations = 0;
      try{
         DatabaseMetaData metadata = conn.getMetaData();
         ResultSet res = metadata.getCatalogs();
         while (res.next()) {
            String databaseName = res.getString(1);
            if (databaseName == "lab6_rooms"){
               rooms = 1;
            }
            if (databaseName == "lab6_reservations"){
               reservations = 1;
            }
         }
      }
      catch(Exception e){
         System.out.println(e);
      }
           System.out.println("Checking rooms tables...");
      try{
         if(rooms == 0){
            Statement s = conn.createStatement();
            s.executeUpdate("CREATE TABLE IF NOT EXISTS lab6_rooms (" +
               "RoomCode char(5) PRIMARY KEY," +
               "RoomName varchar(30) NOT NULL," +
               "Beds int(11) NOT NULL," +
               "bedType varchar(8) NOT NULL," +
               "maxOcc int(11) NOT NULL," +
               "basePrice DECIMAL(6,2) NOT NULL," +
               "decor varchar(20) NOT NULL," +
               "UNIQUE (RoomName));");  
            s.executeUpdate("INSERT INTO lab6_rooms SELECT * FROM INN.rooms;");
            System.out.println("Creating/updating room table if needed");
         }
         if(rooms == 1){
            System.out.println("Rooms table found");
         }
      }
      catch(Exception e){
         System.out.println(e);
      }
      System.out.println("Checking reservations tables...");
      try{
         if(reservations == 0){
            Statement s = conn.createStatement();
            s.executeUpdate("CREATE TABLE IF NOT EXISTS lab6_reservations (" +
               "CODE int(11) PRIMARY KEY," +
               "Room char(5) NOT NULL," +
               "CheckIn date NOT NULL," +
               "Checkout date NOT NULL," +
               "Rate DECIMAL(6,2) NOT NULL," +
               "LastName varchar(15) NOT NULL," +
               "FirstName varchar(15) NOT NULL," +
               "Adults int(11) NOT NULL," +
               "Kids int(11) NOT NULL," +
               "UNIQUE (Room, CheckIn)," +
               "UNIQUE (Room, Checkout)," +
               "FOREIGN KEY (Room) REFERENCES lab6_rooms (RoomCode));");  
            s.executeUpdate("INSERT INTO lab6_reservations SELECT CODE, Room," +
               "DATE_ADD(CheckIn, INTERVAL 8 YEAR)," +
               "DATE_ADD(Checkout, INTERVAL 8 YEAR)," +
               "Rate, LastName, FirstName, Adults, Kids FROM INN.reservations;");
            System.out.println("Creating/updating reservations table if needed");
         }
         if(reservations == 1){
            System.out.println("Reservations table exists");
         }
      }
      catch(Exception e){
         System.out.println(e);
      }
   }

   private static void optionSelect(){
      String command;

      try{
         printOptions();
         System.out.print("Input Command: ");
         while ((command = br.readLine()) != null){
            String[] tokens = command.split(" ");
            if(tokens[0].equals("1")){
               //roomCommand();
               System.out.println("1...");
            }
            else if(tokens[0].equals("2")){
               bookCommand();
               System.out.println("2...");
            }
            else if(tokens[0].equals("3")){
               searchCommand();
               System.out.println("3...");
            }
            else if(tokens[0].equals("4")){
               revenueCommand();
               //System.out.println("4...");
            }
            else if(tokens[0].equals("M")){
               printOptions();
            }
            else if(tokens[0].equals("0")){ 
               System.out.println("Exiting...");
               return;
            }
            else{
               invalidCommand();
            }
            System.out.print("Input Command: ");           
         }
      }
      catch (IOException e){
         e.printStackTrace();
      }
   }





   private static void printOptions(){
      System.out.println("\nMain Menu");
      System.out.println("[1]Rooms and Rates");
      System.out.println("[2]Book Resrvations");
      System.out.println("[3]Reservation Search");
      System.out.println("[4]Revenue");
      System.out.println("[M]ain Menu");
      System.out.println("[0]Exit\n");
   }

   private static void invalidCommand(){
      System.out.println("Invalid Command!");
      System.out.println();
   }

   private static void roomCommand(){
      //print list of rooms sorted by popularity
      //Room popularity: number of days the room has been occupied during the previous 180 days/180 round to 2 decimals
      //Next available check-in date
      //Length of the most recent stay in the room
      //Most recent check out date
   }

   private static void bookCommand(){
      String firstName="";
      String lastName="";
      String roomCode = "";
      String bedType="";
      String startDate="";
      String endDate ="";
      int childCount = -1;
      int adultCount = -1;

      System.out.println("\nCreating reservation request...\n");
      try{
         System.out.print("First Name: ");
         firstName = br.readLine();
         System.out.print("Last Name: ");
         lastName = br.readLine();
         System.out.print("Room Code: ");
         roomCode = br.readLine();
         System.out.print("Bed Type: ");
         bedType = br.readLine();
         System.out.print("Start Day: ");
         startDate = br.readLine();
         System.out.print("End Day: ");
         endDate = br.readLine();
         System.out.print("Child Count: ");
         childCount = Integer.parseInt(br.readLine());
         System.out.print("Adult Count: ");
         adultCount = Integer.parseInt(br.readLine());
      }
      catch(Exception e){
         System.out.println(e);
      }
      System.out.println(firstName + lastName + roomCode + bedType + startDate + endDate + childCount + adultCount);
      /*try{
         processReservation(firstName, lastName, roomCode, bedType, startDate, endDate, childCount, adultCount);
      }
      catch(Exception e){
         System.out.println(e);
      }*/
   }

   private static void processReservation(String firstName, String lastName, String roomCode, String bedType,
      String startDate, String endDate, int childCount, int adultCount){
      //produce list of available rooms
      //book by number options
      //if no matches, suggest 5 possible rooms/dates
      //do not overlap with another existing reservation
      //if adults + children exceeds capacity of room, throw error
      //to reserve multiple rooms, make several reservations
      //provide option to cancel current reservation request
      //after choosing a room to reserve, provide user with a confirmation screen

      System.out.println("Select a reservation");
      System.out.print("[1]");
      System.out.print("[2]");
      System.out.print("[3]");
      System.out.print("[4]");
      System.out.print("[5]");
      System.out.print("[0]Return to Main Menu");

      //display for confirmation
      //first, last name
      //room code, room name, bed tyope
      //start, end date
      //# of adults
      //# of children
      //cost of stay, rate as follows
      //    -number of weekday * room base rate +
      //    -number of weekend * 110% room base rate +
      //    -18% tourism tax on subtotal

      //display confirm or cancel
      //add to reservation table
   }

   private static void searchCommand(){
      String firstName="";
      String lastName="";
      String startDate="";
      String endDate ="";
      String roomCode = "";
      int reservationCode = -1;


      System.out.println("\nSpecify search criteria...");
      try{
         System.out.print("First Name: ");
         firstName = br.readLine();
         System.out.print("Last Name: ");
         lastName = br.readLine();
         System.out.println("Reservations between");
         System.out.print("Start Day: ");
         startDate = br.readLine();
         System.out.print("End Day: ");
         endDate = br.readLine();
         System.out.print("Room Code: ");
         roomCode = br.readLine();
         System.out.print("Reservation Code: ");
         reservationCode = Integer.parseInt(br.readLine());
         

      }
      catch(Exception e){
         System.out.println(e);
      }
      System.out.println(firstName + lastName + startDate + endDate + roomCode + reservationCode );


   }


   private static void revenueCommand(){
      String title = "Room\t|Jan\t|Feb\t|Mar\t|Apr\t|May\t|June\t|July\t|Aug\t|Sep\t|Oct\t|Nov\t|Dec";
      int count = 1;
      int i = 0;
      int j = 0;
      String[][] revenue = new String[11][14];
      revenue = setTable(revenue); 
      
      try{
         PreparedStatement pStatement = null;
         ResultSet rs = null;
         String sql = "SELECT Room, ROUND(SUM(Rate),0) AS Month " +
            "FROM lab6_rooms INNER JOIN lab6_reservations on RoomCode = Room " +
            "WHERE Checkout >= ? AND Checkout <= ? " + 
            "GROUP BY Room " +
            "ORDER BY Room;";
         pStatement = conn.prepareStatement(sql);
         while(count < 10 ){
            rs = getMonth(pStatement, rs, "2018-0"+ count + "-01", "2018-0" + count + "-31");
            i = 1;
            while(rs.next()){
               revenue[i][count] = Integer.toString(rs.getInt("Month"))+ "\t|";
               i++;
            }
            count++;
         }
         while(count < 13 ){
            rs = getMonth(pStatement, rs, "2018-"+ count + "-01", "2018-" + count + "-31");
            i = 1;
            while(rs.next()){
               revenue[i][count] = Integer.toString(rs.getInt("Month"))+ "\t|";
               i++;
            }
            count++;
         }
         rs = getMonth(pStatement, rs, "2018-01-01", "2018-12-31");
         i = 1;
         while(rs.next()){
            revenue[i][count] = Integer.toString(rs.getInt("Month"))+ "\t|";
            i++;
         }
      }
      catch(SQLException e){
         System.out.println(e);
      }

      for (i = 0; i < 11; i++){
         for(j = 0; j < 14; j++){
            System.out.print(revenue[i][j]);
         }
         System.out.print("\n");
      }  
   }
   private static String[][] setTable(String[][] revenue){
      revenue[0][0] = "|Room\t|";
      revenue[0][1] = "Jan\t|";
      revenue[0][2] = "Feb\t|";
      revenue[0][3] = "Mar\t|";
      revenue[0][4] = "Apr\t|";
      revenue[0][5] = "May\t|";
      revenue[0][6] = "June\t|";
      revenue[0][7] = "July\t|";
      revenue[0][8] = "Aug\t|";
      revenue[0][9] = "Sep\t|";
      revenue[0][10] = "Oct\t|";
      revenue[0][11] = "Nov\t|";
      revenue[0][12] = "Dec\t|";
      revenue[0][13] = "Total\t|";
      revenue[1][0] = "|AOB\t|";
      revenue[2][0] = "|CAS\t|";
      revenue[3][0] = "|FNA\t|";
      revenue[4][0] = "|HBB\t|";
      revenue[5][0] = "|IBD\t|";
      revenue[6][0] = "|IBS\t|";
      revenue[7][0] = "|MWC\t|";
      revenue[8][0] = "|RND\t|";
      revenue[9][0] = "|RTE\t|";
      revenue[10][0] = "|TAA\t|";
      return revenue;
   }

   private static ResultSet getMonth(PreparedStatement pStatement, ResultSet rs, String startDate, String endDate){
      try{
         pStatement.setString(1, startDate);
         pStatement.setString(2, endDate);
         rs = pStatement.executeQuery();
      }
      catch(SQLException e){
         System.out.println(e);
      }
      return rs;
   }

   private static void login(){
      String jdbc_url="jdbc:mysql://csc365winter2018.webredirect.org/pmalapir?";
      //jdbc:mysql://csc365winter2018.webredirect.org/pmalapir?
      String usr="pmalapir";
      String pw="365W18_010118988";

      /*try{
         br = new BufferedReader(new InputStreamReader(System.in));
         System.out.print("Url: ");
         jdbc_url = br.readLine();
         System.out.print("Username: ");
         usr = br.readLine();
         System.out.print("Password: ");
         pw = br.readLine();
      }
      catch(Exception e){
         System.out.println(e);
      }*/

      try {
         conn = DriverManager.getConnection(jdbc_url, usr, pw);
         System.out.println("\nConnection Succesful!\n");
      } 
      catch (Exception ex) {
         System.out.println("\nCould not open connection");
         System.out.println("Exiting...\n");
         System.exit(-1);
      }
   }
}
