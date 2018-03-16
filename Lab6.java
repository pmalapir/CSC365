import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;


import java.util.*;
import java.lang.*;
import java.math.*;
import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.LinkedHashMap;
import java.time.LocalDate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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
               roomCommand();
               //System.out.println("1...");
            }
            else if(tokens[0].equals("2")){
               bookCommand();
               //System.out.println("2...");
            }
            else if(tokens[0].equals("3")){
               searchCommand();
               //System.out.println("3...");
            }
            else if(tokens[0].equals("4")){
               revenueCommand();
               //System.out.println("4...");
            }
            else if(tokens[0].equals("M")||tokens[0].equals("m")){
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
      String roomCode = "";
      String roomName = "";
      int beds = 0;
      String bedType = "";
      int maxOcc = 0;
      double basePrice = 0;
      String decor = "";
      double popularity = 0;
      int nextCheckin = 0;
      int stayDuration = 0;
      String bufferOne = "\t|";
      String bufferTwo = "\t|";
      Statement s = null;
      try{
         s = conn.createStatement();
         String sql = "SELECT lab6_rooms.*, popularity, days_until_next_checkin, stay_duration " +
            "FROM lab6_rooms " +
            "INNER JOIN " +
               "(SELECT Room, ROUND(SUM(DATEDIFF(Checkout, CheckIn))/180, 2) AS popularity " +
               "FROM lab6_reservations " +
               "WHERE CheckIn < CURDATE() " +
               "AND DATEDIFF(CheckIn, CURDATE()) < 181 " +
               "AND Checkout < CURDATE() " +
               "AND DATEDIFF(Checkout, CURDATE()) < 181 " +
               "GROUP BY Room) AS Popularity ON Popularity.Room = RoomCode " +
            "INNER JOIN " +
               "(SELECT Room, MIN(DATEDIFF(CheckIn, CURDATE())) AS days_until_next_checkin " +
               "FROM lab6_reservations " +
               "WHERE CheckIn > (CURDATE() + 1) " +
               "GROUP BY Room) AS nextCheckin ON Popularity.Room = nextCheckin.Room " +
            "INNER JOIN " +
               "(SELECT Room, DATEDIFF(Checkout, CheckIn) AS stay_duration " +
               "FROM lab6_reservations " +
               "WHERE (Room, Checkout) IN " +
                  "(SELECT Room, MAX(Checkout) AS recent_checkout " +
                  "FROM lab6_reservations " +
                  "WHERE Checkout < CURDATE() " +
                  "GROUP BY Room) " +
               "ORDER BY Room) AS StayDuration ON Popularity.Room = StayDuration.Room " +
            "ORDER BY popularity DESC, days_until_next_checkin ASC, stay_duration DESC;";
         ResultSet rs = s.executeQuery(sql);
         System.out.println("RoomCode \t|RoomName \t\t\t|Beds \t|bedType \t|maxOcc\t|basePrice" +
            " \t|decor \t\t|popularity \t|next_checkin\t|last_stay_duration");
         while(rs.next()){
            roomCode = rs.getString("RoomCode");
            roomName = rs.getString("RoomName");
            beds = rs.getInt("Beds");
            bedType = rs.getString("bedType");
            maxOcc = rs.getInt("maxOcc");
            basePrice = rs.getDouble("basePrice");
            decor = rs.getString("decor");
            popularity = rs.getDouble("popularity");
            nextCheckin = rs.getInt("days_until_next_checkin");
            stayDuration = rs.getInt("stay_duration");

            if(roomName.equals("Immutable before decorum")){
               bufferOne = "\t|";
            }
            else{
               bufferOne = "\t\t|";
            }

            if(decor.equals("traditional")){
               bufferTwo = "\t|";
            }
            else{
               bufferTwo = "\t\t|";
            }
            System.out.println("|" + roomCode + "\t\t|" + roomName + bufferOne + beds + "\t|" + bedType + "\t\t|" + 
               maxOcc + "\t|" + basePrice + "\t\t|" + decor + bufferTwo + popularity + "\t\t|" + nextCheckin + "\t\t|" + 
               stayDuration);
         }
      }
      catch(SQLException e){
         System.out.println(e);
      }
   }

   private static void bookCommand(){
      String firstName="";
      String lastName="";
      String roomCode = "";
      String bedType="";
      String startDate="";
      String endDate ="";
      String childCount = "";
      String adultCount = "";

      System.out.println("\nCreating reservation request...\n");
      try{
         System.out.print("First Name: ");
         firstName = br.readLine();
         System.out.print("Last Name: ");
         lastName = br.readLine();
         System.out.println("Reservations between");
         System.out.print("Start Date [YYYY-MM-DD]: ");
         startDate = br.readLine();
         System.out.print("End Date [YYYY-MM-DD]: ");
         endDate = br.readLine();
         System.out.println("| Room Code | Room Name                | Bed Type");
         System.out.println("| AOB       | Abscond or bolster       | Queen");
         System.out.println("| CAS       | Convoke and sanguine     | King");
         System.out.println("| FNA       | Frugal not apropos       | King");
         System.out.println("| HBB       | Harbinger but bequest    | Queen");
         System.out.println("| IBD       | Immutable before decorum | Queen");
         System.out.println("| IBS       | Interim but salutary     | King");
         System.out.println("| MWC       | Mendicant with cryptic   | Double");
         System.out.println("| RND       | Recluse and defiance     | King");
         System.out.println("| RTE       | Riddle to exculpate      | Queen");
         System.out.println("| TAA       | Thrift and accolade      | Double");
         System.out.println("| Any       | No preference            | ");
         System.out.print("Desired Room Code: ");
         roomCode = br.readLine();
         System.out.print("Bed Type [Double, Queen, King, Any]: ");
         bedType = br.readLine();
         System.out.print("Number of Adults: ");
         adultCount = br.readLine(); 
         System.out.print("Number of Children: ");
         childCount = br.readLine();
      }
      catch(Exception e){
         System.out.println(e);
      }
      try{
         processReservation(firstName, lastName, roomCode, bedType, startDate, endDate,
          childCount, adultCount);
      }
      catch(Exception e){
         System.out.println(e);
      }
   }

   private static void processReservation(String firstName, String lastName, String roomCode, String bedType,
      String startDate, String endDate, String childCount, String adultCount){

      ArrayList<Room> roomResults = new ArrayList<Room>();
      int numRes = 0;
      Room temp = new Room();
      ArrayList<String> whereStatements = new ArrayList<String>();
      String query = "";
      Statement s = null;
      int i;
      int j;
      String reservationChoice;
      int choice;
      try{
         // this creates the list of rooms for referencing
         while (numRes < 5) {
            ArrayList<Room> roomInfo = new ArrayList<Room>();
            ArrayList<Room> currResv = new ArrayList<Room>();

            query = "SELECT * " +
                    "FROM lab6_rooms ;";

            s = conn.createStatement();
            ResultSet rs = s.executeQuery(query);
            
            while (rs.next()){
               Room room = new Room(rs.getString("RoomCode"), 
                                    rs.getString("RoomName"), 
                                    rs.getInt("Beds"), 
                                    rs.getString("bedType"), 
                                    rs.getInt("maxOcc"), 
                                    rs.getDouble("basePrice"), 
                                    rs.getString("decor"),
                                    startDate,
                                    endDate);
   
               roomInfo.add(room);  // Room class created at the bottom
            }  

            query = "SELECT RoomCode, RoomName, Beds, bedType, maxOcc, basePrice, decor " +
                     "FROM lab6_reservations join lab6_rooms on RoomCode = room " +
                     "WHERE ((CheckIn BETWEEN \"" + startDate + "\" AND \"" + endDate + "\") " + 
                     "OR (Checkout BETWEEN \"" + startDate + "\" AND \"" + endDate + "\")) ;";

            rs = s.executeQuery(query);
   
            while (rs.next()){
               Room room = new Room(rs.getString("RoomCode"), 
                                    rs.getString("RoomName"), 
                                    rs.getInt("Beds"), 
                                    rs.getString("bedType"), 
                                    rs.getInt("maxOcc"), 
                                    rs.getDouble("basePrice"), 
                                    rs.getString("decor"),
                                    "",
                                    "");
                                    
               currResv.add(room);  // Room class created at the bottom
            }

            for(i = 0; i<currResv.size(); i++){
               for(j = 0; j<roomInfo.size(); j++){
                  if(currResv.get(i).roomCode.equals(roomInfo.get(j).roomCode)){
                     roomInfo.remove(j);   
                  } 
               }
            }

            // bed type
            for (i = 0; i < roomInfo.size(); i++){
               for(j = i; j < roomInfo.size(); j++){
                  if((roomInfo.get(j).bedType).equals(bedType)){ 
                     temp = roomInfo.get(i);
                     roomInfo.set(i, roomInfo.get(j));
                     roomInfo.set(j, temp);
                  }
               }
            }
   
            // room code
            for (i = 0; i < roomInfo.size(); i++){
               for(j = i; j < roomInfo.size(); j++){
                  if((roomInfo.get(j).roomCode).equals(roomCode)){ 
                     temp = roomInfo.get(i);
                     roomInfo.set(i, roomInfo.get(j));
                     roomInfo.set(j, temp);
                  }
               }
            }

            for(i = 0; i < roomInfo.size(); i++){
               roomResults.add(roomInfo.get(i));
               numRes++;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date1 = LocalDate.parse(startDate, formatter);
            LocalDate date2 = LocalDate.parse(endDate, formatter);
            date1 = date1.plus(1, ChronoUnit.DAYS);
            date2 = date2.plus(1, ChronoUnit.DAYS);
            startDate = date1.format(formatter);
            endDate = date2.format(formatter);

            // String startDateTemp = startDate.substring(8, startDate.length());
            // int startDay = Integer.parseInt(startDateTemp);
            // startDay++;
            // if(startDay < 10){
            //    startDate = startDate.substring(0, 8);
            //    startDate = startDate.concat("0");
            //    startDate = startDate + Integer.toString(startDay);   
            // }
            // else{
            //    startDate = startDate.substring(0, 8) + Integer.toString(startDay);
            // }

            // String endDateTemp = endDate.substring(8, endDate.length());
            // int endDay = Integer.parseInt(endDateTemp);
            // endDay++;
            // if(endDay < 10){
            //    endDate = endDate.substring(0, 8);
            //    endDate = endDate.concat("0");
            //    endDate = endDate + Integer.toString(endDay);   
            // }
            // else{
            //    endDate = endDate.substring(0, 8) + Integer.toString(endDay);
            // }
         }
      }
      catch(Exception e){
         System.out.println(e);
      }


      System.out.format("\t|%-10s|%-10s|%-8s|%-8s|%-10s|%-15s|%-15s\n","Roomcode", "Bed Count", 
         "Bed Type", "Max Occ", "Base Price", "Start Date", "End date");
      System.out.print("[1]\t");
      roomResults.get(0).printRoom();
      System.out.println();
      System.out.print("[2]\t");
      roomResults.get(1).printRoom();
      System.out.println();
      System.out.print("[3]\t");
      roomResults.get(2).printRoom();
      System.out.println();
      System.out.print("[4]\t");
      roomResults.get(3).printRoom();
      System.out.println();
      System.out.print("[5]\t");
      roomResults.get(4).printRoom();
      System.out.println();
      System.out.println("[0] Return to Main Menu");
      System.out.print("Select a reservation: ");
      
      try{
         reservationChoice = br.readLine();
         choice = Integer.parseInt(reservationChoice);
         makeReservation(firstName, lastName, adultCount, childCount, roomResults.get(choice-1));
      }
      catch(Exception e){
         System.out.println(e);
      }  
   }

   private static void makeReservation(String firstName, String lastName, String adultCount, 
      String childCount, Room room){

      double weekday = 0;
      double weekend = 0;
      double price = 0;
      String choice = "";
      String insert = "";
      Statement s = null;
      ResultSet rs = null;
      String query = "";
      int max = 0;
      int roomOcc;

      double weekendRate = room.basePrice * 1.1;
      price = calcPrice(room.startDate, room.endDate, room.basePrice, weekendRate);

      price = price * 1.18;

      int aCount = Integer.parseInt(adultCount);
      int cCount = Integer.parseInt(childCount);

      if((cCount + aCount) > room.maxOcc){
        System.out.println("**Notice: Your group exceeds the maximum occupancy for this room **");
      }

      System.out.println("\nReview reservation before submitting");
      System.out.println("Name: " + firstName + " " + lastName);
      System.out.println("Room details:");
      System.out.println("\tRoom Code: " + room.roomCode);
      System.out.println("\tRoom Name: " + room.roomName);
      System.out.println("\tBed Type: " + room.bedType);
      System.out.println("From: " + room.startDate + " To: " + room.endDate);
      System.out.println("Adult Count: " + adultCount);
      System.out.println("Child Count: " + childCount);
      System.out.println("Price: $" + price); 

      System.out.println("\n[0] No");
      System.out.println("[1] Yes");
      System.out.print("Would you like to book the reservation?: ");
      try{
         choice = br.readLine();

         if(choice.equals("1")){
            s = conn.createStatement();
            query = "select MAX(CODE) from lab6_reservations;";
            rs = s.executeQuery(query);
            if (rs.next()){
               max = rs.getInt("MAX(CODE)");
            }
            max++;

            insert = "INSERT into lab6_reservations " +
                     "VALUES (" + max + ", \"" + room.roomCode + "\", " + "\"" + room.startDate + "\", " + 
                     "\"" + room.endDate + "\", " + price +  ", \"" + lastName.toUpperCase() + "\", " + 
                     "\"" + firstName.toUpperCase() + "\", " + adultCount + ", " + childCount + ");";
            s.executeUpdate(insert);
            System.out.println("Reservation created!");
         }
      }
      catch(Exception e){
         System.out.println(e);
      }
   }

   private static double calcPrice(String startDate, String endDate, double weekdayRate, double weekendRate)
   {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      LocalDate date1 = LocalDate.parse(startDate, formatter);
      LocalDate date2 = LocalDate.parse(endDate, formatter);
      double price = 0;

      while (date1.isEqual(date2.plus(1,ChronoUnit.DAYS)) == false)
      {
         int day = date1.getDayOfWeek().getValue();

         if (day == 6 || day == 7)
         {
            price += weekendRate;
         }
         else
         {
            price += weekdayRate;
         }

         date1 = date1.plus(1, ChronoUnit.DAYS);
      }

      return price;
   }

   private static void searchCommand(){
      ArrayList<String> whereStatements = new ArrayList<String>();

      String firstName="";
      String lastName="";
      String startDate="";
      String endDate ="";
      String roomCode = "";
      String reservationCode = "";
      int resCode = 0;
      double rate = 0;
      int adults = 0;
      int kids = 0;
      String str1;
      Statement s = null;

      String query = "SELECT * " + 
                     "FROM lab6_reservations " +
                     "WHERE ";

      System.out.println("\nSpecify search criteria...");

      try{
         s = conn.createStatement();
         System.out.print("First Name: ");
         firstName = br.readLine();
         System.out.print("Last Name: ");
         lastName = br.readLine();
         System.out.println("Reservations between");
         System.out.print("Start Date: ");
         startDate = br.readLine();
         System.out.print("End Date: ");
         endDate = br.readLine();
         System.out.print("Room Code: ");
         roomCode = br.readLine();
         System.out.print("Reservation Code: ");
         reservationCode = br.readLine(); 

         if (firstName.toLowerCase().equals("any") == false) {
            str1 = "FirstName LIKE \"" + firstName + "%\" ";
            whereStatements.add(str1);
         }

         if (lastName.toLowerCase().equals("any") == false) {
            str1 = "LastName LIKE \"" + lastName + "%\" ";
            whereStatements.add(str1);
         }

         if (startDate.toLowerCase().equals("any") == false){
            str1 = "CheckIn >= \"" + startDate + "\" ";
            whereStatements.add(str1);
         }

         if(endDate.toLowerCase().equals("any") == false){
            str1 = "Checkout <= \"" + endDate + "\" ";
            whereStatements.add(str1);
         }

         if (roomCode.toLowerCase().equals("any") == false) {
            str1 = "Room LIKE \"" + roomCode + "%\" ";
            whereStatements.add(str1);
         }

         if (reservationCode.toLowerCase().equals("any") == false) {
            str1 = "CODE LIKE \"" + reservationCode + "%\" ";
            whereStatements.add(str1);
         }

         whereStatements.add(";");

         if (whereStatements.size() == 1) {
             query = "SELECT * " + 
                      "FROM lab6_reservations ;";
         }
         
         else{
            for (int i=0; i<whereStatements.size() - 1; i++){
               if (i != 0) {
                  query = query + "AND ";
               }
               query = query.concat(whereStatements.get(i));            
            }
         }
         ResultSet rs = s.executeQuery(query);
         System.out.format("|%-7s|%-5s|%-10s|%-10s|%-8s|%-14s|%-14s|%-6s|%-3s\n", "CODE", "Room", "Checkin", "Checkout", 
            "Rate", "LastName", "FirstName", "Adults", "Kids");
         while(rs.next()){
            resCode = rs.getInt("CODE");
            roomCode = rs.getString("Room");
            startDate = rs.getString("CheckIn");
            endDate = rs.getString("Checkout");
            rate = rs.getDouble("Rate");
            lastName = rs.getString("LastName");
            firstName = rs.getString("FirstName");
            adults = rs.getInt("Adults");
            kids = rs.getInt("Kids");

            System.out.format("|%-7d|%-5s|%-10s|%-10s|%8.2f|%-14s|%-14s|%-6d|%-3d\n", resCode, roomCode, 
               startDate, endDate, rate, lastName, firstName, adults, kids);
         }
      }
      catch(Exception e){
         System.out.println(e);
      }
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
      // String jdbc_url="jdbc:mysql://csc365winter2018.webredirect.org/pmalapir?";
      // String usr="pmalapir";
      // String pw="365W18_010118988";

      //uncomment to run on input  
      String jdbc_url = "";
      String usr="";
      String pw = "";
      try{
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
      }

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

   private static class Room
   {
      public String roomCode;
      public String roomName;
      public int beds;
      public String bedType;
      public int maxOcc;
      public double basePrice;
      public String decor;
      public String startDate;
      public String endDate;

      public Room(String roomCode, String roomName, int beds, String bedType, int maxOcc, 
         double basePrice, String decor, String startDate, String endDate)
      {
         this.roomCode = roomCode;
         this.roomName = roomName;
         this.beds = beds;
         this.bedType = bedType;
         this.maxOcc = maxOcc;
         this.basePrice = basePrice;
         this.decor = decor;
         this.startDate = startDate;
         this.endDate = endDate;
      }


      public Room()
      {
         this.roomCode = "";
         this.roomName = "";
         this.beds = 0;
         this.bedType = "";
         this.maxOcc = 0;
         this.basePrice = 0;
         this.decor = "";
         this.startDate = "";
         this.endDate = "";
      }

      public void printRoom(){
         System.out.format("|%-10s|%-10d|%-8s|%-8d|%10.2f|%-15s|%-15s", this.roomCode, this.beds, this.bedType,
            this.maxOcc, this.basePrice, this.startDate, this.endDate);
      }
   }
}
