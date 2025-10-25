package com.github.diogocerqueiralima.application.services;

import com.github.diogocerqueiralima.application.commands.ReceiveLocationCommand;
import com.github.diogocerqueiralima.domain.model.Location;
import com.github.diogocerqueiralima.domain.ports.inbound.LocationService;
import com.github.diogocerqueiralima.domain.ports.outbound.LocationPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class LocationServiceImpl implements LocationService {

    private final LocationPublisher locationPublisher;

    public LocationServiceImpl(LocationPublisher locationPublisher) {
        this.locationPublisher = locationPublisher;
    }

    @Override
    public void receive(ReceiveLocationCommand command) {

        Instant timestamp = nmeaToInstant(command.date(), command.time());
        double latitude = convertNMEACoordinateToDecimal(command.latitude(), command.latitudeDirection());
        double longitude = convertNMEACoordinateToDecimal(command.longitude(), command.longitudeDirection());
        double altitude = command.altitude();
        double speed = command.speed() * (1852.0 / 3600000.0); // Convert from knots to m/s
        double course = command.course();
        UUID deviceId = command.deviceId();

        Location location = new Location(timestamp, latitude, longitude, altitude, speed, course, deviceId);

        locationPublisher.publish(location);
    }

    /**
     *
     * Converts NMEA date and time strings to an Instant.
     *
     * @param date the date in NMEA format (DDMMYY)
     * @param time the time in NMEA format (HHMMSS[.sss])
     * @return the corresponding Instant
     */
    private Instant nmeaToInstant(String date, String time) {

        int hour = Integer.parseInt(time.substring(0, 2));
        int minute = Integer.parseInt(time.substring(2, 4));
        int second = Integer.parseInt(time.substring(4, 6));

        int nano = 0;
        if (time.contains(".")) {
            String fractionalPart = time.split("\\.")[1];
            nano = (int) (Double.parseDouble("0." + fractionalPart) * 1_000_000_000);
        }

        int day = Integer.parseInt(date.substring(0, 2));
        int month = Integer.parseInt(date.substring(2, 4));
        int year = Integer.parseInt(date.substring(4, 6)) + 2000;

        LocalDateTime localDateTime = LocalDateTime.of(year, month, day, hour, minute, second, nano);

        return localDateTime.toInstant(ZoneOffset.UTC);
    }

    /**
     *
     * Converts NMEA coordinate format to decimal degrees.
     *
     * @param coordinate the coordinate in NMEA format (DDMM.MMMM or DDDMM.MMMM)
     * @param direction the direction character ('N', 'S', 'E', 'W')
     * @return the coordinate in decimal degrees
     */
    private double convertNMEACoordinateToDecimal(String coordinate, String direction) {

        int degreeLength = (direction.equals("N") || direction.equals("S")) ? 2 : 3;

        double degrees = Double.parseDouble(coordinate.substring(0, degreeLength));
        double minutes = Double.parseDouble(coordinate.substring(degreeLength));

        double decimalDegrees = degrees + (minutes / 60.0);

        if (direction.equals("S") || direction.equals("W")) {
            decimalDegrees *= -1;
        }

        return decimalDegrees;
    }

}
