--popularity
SELECT Room, ROUND(SUM(DATEDIFF(Checkout, CheckIn))/180, 2) AS popularity
FROM lab6_reservations
WHERE CheckIn < CURDATE()
AND DATEDIFF(CheckIn, CURDATE()) < 181
AND Checkout < CURDATE()
AND DATEDIFF(Checkout, CURDATE()) < 181
GROUP BY Room;

--next available check in data
SELECT Room, MIN(DATEDIFF(CheckIn, CURDATE())) AS days_until_next_checkin
FROM lab6_reservations
WHERE CheckIn > (CURDATE() + 1)
GROUP BY Room;

--most recent stay length
SELECT Room, DATEDIFF(Checkout, CheckIn) AS stay_duration
FROM lab6_reservations
WHERE (Room, Checkout) IN
   (SELECT Room, MAX(Checkout) AS recent_checkout
   FROM lab6_reservations
   WHERE Checkout < CURDATE()
   GROUP BY Room)
ORDER BY Room;

--rooms and reservations
SELECT Popularity.Room, popularity, days_until_next_checkin, stay_duration
FROM
   (SELECT Room, ROUND(SUM(DATEDIFF(Checkout, CheckIn))/180, 2) AS popularity
   FROM lab6_reservations
   WHERE CheckIn < CURDATE()
   AND DATEDIFF(CheckIn, CURDATE()) < 181
   AND Checkout < CURDATE()
   AND DATEDIFF(Checkout, CURDATE()) < 181
   GROUP BY Room) AS Popularity
INNER JOIN
   (SELECT Room, MIN(DATEDIFF(CheckIn, CURDATE())) AS days_until_next_checkin
   FROM lab6_reservations
   WHERE CheckIn > (CURDATE() + 1)
   GROUP BY Room) AS nextCheckin ON Popularity.Room = nextCheckin.Room
INNER JOIN
   (SELECT Room, DATEDIFF(Checkout, CheckIn) AS stay_duration
   FROM lab6_reservations
   WHERE (Room, Checkout) IN
      (SELECT Room, MAX(Checkout) AS recent_checkout
      FROM lab6_reservations
      WHERE Checkout < CURDATE()
      GROUP BY Room)
   ORDER BY Room) AS StayDuration ON Popularity.Room = StayDuration.Room
ORDER BY popularity DESC, days_until_next_checkin ASC, stay_duration DESC;

--reservations

--date
SELECT *
FROM lab6_reservations
INNER JOIN Rooms on Room = Room_Id
WHERE Checkout < desiredCheckIn
ORDER BY DATEDIFF(Checkout, desiredCheckIn)  

--room
SELECT *
FROM lab6_reservations
INNER JOIN Rooms on Room = Room_Id
WHERE Room = desiredRoom;

--bedtype
SELECT *
FROM lab6_reservations
INNER JOIN Rooms on Room = Room_Id
WHERE Bed_Type = desiredBedType;

/* query one at a tine following this order. date->room->bedtype.
   Each time, pulling "FROM" the previous query's result. At the
   same time, add the results to an ArrayList. At the end, output
   ArrayList value accordingly.*/

--detailed reservation information
--First Name
--Last Name
--A range of datas
--Room code
--Reservation code

SELECT *
FROM lab6_reservations
WHERE 
CODE LIKE searchCode
AND Room LIKE searchRoom
AND LastName LIKE searchLastName
AND FirstName LIKE searchFirstName
AND CheckIn BETWEEN searchDateStart AND searchDateEnd
AND Checkout BETWEEN searchDateStart AND searchDateEnd;