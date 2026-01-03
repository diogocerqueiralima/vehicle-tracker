package com.github.diogocerqueiralima.application.usecases;

import com.github.diogocerqueiralima.application.commands.ReceiveLocationCommand;
import com.github.diogocerqueiralima.domain.model.Location;
import com.github.diogocerqueiralima.domain.ports.inbound.LocationUseCase;
import com.github.diogocerqueiralima.domain.ports.outbound.LocationPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class LocationUseCaseImpl implements LocationUseCase {

    private final LocationPublisher locationPublisher;

    public LocationUseCaseImpl(LocationPublisher locationPublisher) {
        this.locationPublisher = locationPublisher;
    }

    @Override
    public void receive(ReceiveLocationCommand command) {

        Instant timestamp = nmeaToInstant(command.date(), command.time());
        double latitude = nmeaCoordinateToDecimal(command.latitude(), command.latitudeDirection());
        double longitude = nmeaCoordinateToDecimal(command.longitude(), command.longitudeDirection());
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
    private Instant nmeaToInstant(int date, double time) {

        int hour = (int) (time / 10000);
        int minute = (int) ((time % 10000) / 100);
        int second = (int) (time % 100);

        int nano = (int) ((time - Math.floor(time)) * 1_000_000_000);

        int day = date / 10000;
        int month = (date % 10000) / 100;
        int year = (date % 100) + 2000;

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
    private double nmeaCoordinateToDecimal(String coordinate, String direction) {

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
