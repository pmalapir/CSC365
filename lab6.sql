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