DROP SCHEMA IF EXISTS uber cascade;
CREATE SCHEMA uber;
SET search_path TO uber, public;

-- -- A person who is registered as a client of the company's driving services.
-- CREATE TABLE Client (
--   client_id integer PRIMARY KEY,
--   surname varchar(25) NOT NULL,
--   firstname varchar(15) NOT NULL,
--   email varchar(30)
-- ) ;

-- -- A driver for the company.  dob is their date of birth.
-- -- Trained indicates whether or not they attended the optional new-driver 
-- -- training.  vehicle is the vehicle that this driver gives rides in.
-- -- A driver can have only one vehicle associated with them.
-- CREATE TABLE Driver (
--   driver_id integer PRIMARY KEY,
--   surname varchar(25) NOT NULL,
--   firstname varchar(15) NOT NULL,
--   dob date,
--   address varchar NOT NULL,
--   vehicle varchar(8) NOT NULL,
--   trained boolean default false
-- ) ;

-- -- The name, such as 'Pearson Airport', associated with a location.
-- CREATE TABLE Place (
--    name varchar(30) PRIMARY KEY,
--    location point NOT NULL
-- ) ;

-- A row in this table indicates that a driver has indicated at
-- this time that they are available to pick up a user.
CREATE TABLE Available (
  driver_id integer NOT NULL REFERENCES Driver,
  datetime timestamp,
  location point,
  PRIMARY KEY (driver_id, datetime)
) ;


-- -- Requests for a ride, and associated events

-- -- A request for a ride.  source is where the client wants to be
-- -- picked up from, and destination is where they want to be driven to.
-- CREATE TABLE Request (
--   request_id integer PRIMARY KEY,
--   client_id integer NOT NULL REFERENCES Client,
--   datetime timestamp NOT NULL,
--   source varchar(30) NOT NULL References Place(name),
--   destination varchar(30) NOT NULL References Place(name)
-- ) ;

-- -- A row in this table indicates that a driver was dispatched to
-- -- pick up a client, in response to their request.  car_location is where
-- -- their car was at the time when the driver was dispatched.
-- CREATE TABLE Dispatch (
--   request_id integer PRIMARY KEY REFERENCES Request,
--   driver_id integer NOT NULL REFERENCES Driver,
--   car_location point,
--   datetime timestamp
-- ) ;

-- A row in this table indicates that the client who made this request was
-- picked up at this time.
CREATE TABLE Pickup (
  request_id integer PRIMARY KEY NOT NULL REFERENCES Dispatch,
  datetime timestamp NOT NULL
) ;

-- -- A row in this table indicates that the client who made this request was
-- -- dropped off at this time.
-- CREATE TABLE Dropoff (
--   request_id integer PRIMARY KEY NOT NULL REFERENCES Pickup,
--   datetime timestamp NOT NULL
-- ) ;


-- -- To do with money

-- -- This table must have a single row indicating the current rates.
-- -- base is the cost for being picked up, and per_mile is the additional 
-- -- cost for every mile travelled.
-- CREATE TABLE Rates (
--   base real NOT NULL,
--   per_mile real NOT NULL
-- );

-- -- This client associated with this request was billed this amount for the 
-- -- ride.
-- CREATE TABLE Billed (
--   request_id integer PRIMARY KEY REFERENCES Dropoff,
--   amount real NOT NULL
-- ) ;


-- -- To do with Ratings

-- -- The possible values of a rating.
-- CREATE DOMAIN score AS smallint 
--    DEFAULT NULL
--    CHECK (VALUE >= 1 AND VALUE <= 5);

-- -- The driver who gave the ride associated with this dropoff
-- -- was given this rating by the client who had the ride.
-- CREATE TABLE DriverRating (
--    request_id integer PRIMARY KEY REFERENCES Dropoff,
--    rating score NOT NULL
-- ) ;

-- -- The client who had the ride associated with this dropoff
-- -- was given this rating by the driver who gave the ride.
-- CREATE TABLE ClientRating (
--    request_id integer PRIMARY KEY REFERENCES Dropoff,
--    rating score NOT NULL
-- ) ;

-- insert into client values
-- (99, 'Mason', 'Daisy', 'daisy@kitchen.com'),
-- (100, 'Crawley', 'Violet', 'dowager@dower-house.org'),
-- (88, 'Branson', 'Tom', 'branson@gmail.com');


-- insert into driver values
-- (12345, 'Snow', 'Jon', 'January 1, 1990', 'The Wall', 'BGSW 420', false),
-- (22222, 'Tyrion', 'Lannister', 'January 1, 1990', 'Kings Landing', 'ABCD 123', false);


insert into available values
(12345, '2016-01-08 04:05', '(1, 2)');


-- -- Locations are specified as longitude and latitude (in that order), in degrees.
-- insert into place values
-- ('highclere castle', '(1.361, 51.3267)'),
-- ('dower house', '(-0.4632, 51.3552)'),
-- ('eaton centre', '(79.3803,43.654)'),
-- ('cn tower', '(79.3871,43.6426)'),
-- ('north york civic centre', '(79.4146,43.7673)'),
-- ('pearson international airport', '(79.6306,43.6767)'),
-- ('utsc', '(79.1856,43.7836)');


-- insert into request values
-- -- (9, 100, '2016-01-08 04:11', 'eaton centre', 'pearson international airport')
-- (1, 99, '2016-01-08 04:10', 'eaton centre', 'pearson international airport'),
-- -- 2013
-- (2, 100, '2013-02-01 08:00', 'dower house', 'highclere castle'),
-- (3, 100, '2013-02-02 08:00', 'dower house', 'highclere castle'),
-- (4, 100, '2013-02-03 08:00', 'highclere castle', 'dower house'),
-- -- 2014
-- (5, 100, '2014-07-01 08:00', 'dower house', 'pearson international airport'),
-- (6, 100, '2014-07-02 08:00', 'pearson international airport', 'eaton centre'),
-- (7, 100, '2014-07-03 08:00', 'eaton centre', 'cn tower'),
-- -- 2015
-- (8, 100, '2015-07-01 08:00', 'cn tower', 'pearson international airport');


-- insert into dispatch values
-- -- (1, 12345, '(1, 4)', '2016-01-08 04:11'),
-- (2, 22222, '(5, 5)', '2013-02-01 08:05'),
-- (3, 22222, '(5, 5)', '2013-02-02 08:05'),
-- (4, 22222, '(5, 5)', '2013-02-03 08:05'),
-- (5, 22222, '(5, 5)', '2014-07-01 08:05'),
-- (6, 22222, '(5, 5)', '2014-07-02 08:05'),
-- (7, 22222, '(5, 5)', '2014-07-03 08:05'),
-- (8, 22222, '(5, 5)', '2015-07-01 08:05');


insert into pickup values
-- (1, '2016-01-08 04:14'),
(2, '2013-02-01 08:06'),
(3, '2013-02-02 08:06'),
(4, '2013-02-03 08:06'),
(5, '2014-07-01 08:06'),
(6, '2014-07-02 08:06'),
(7, '2014-07-03 08:06'),
(8, '2015-07-01 08:06');


-- insert into dropoff values
-- -- (1, '2016-01-08 04:14'),
-- (2, '2013-02-01 08:16'),
-- (3, '2013-02-02 08:16'),
-- (4, '2013-02-03 08:16'),
-- (5, '2014-07-01 08:16'),
-- (6, '2014-07-02 08:16'),
-- (7, '2014-07-03 08:16'),
-- (8, '2015-07-01 08:16');


-- insert into rates values
-- (3.2, .55);


-- insert into billed values
-- -- (1, 8.5),
-- (2, 255.2),
-- (3, 105.4),
-- (4, 175.5),
-- (5, 5.1),
-- (6, 5.8),
-- (7, 6.2),
-- (8, 5.7);


-- insert into driverrating values
-- (1, 5);

-- insert into clientrating values
-- (1, 4);

