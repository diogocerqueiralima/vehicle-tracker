package com.github.diogocerqueiralima.location;

import java.time.Instant;
import java.util.UUID;

public record ReceiveLocationEvent(
        Instant timestamp, double latitude, double longitude, double altitude,
        double speed, double course, UUID deviceId
) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private Instant timestamp;
        private double latitude;
        private double longitude;
        private double altitude;
        private double speed;
        private double course;
        private UUID deviceId;

        private Builder() {
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder latitude(double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder longitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder altitude(double altitude) {
            this.altitude = altitude;
            return this;
        }

        public Builder speed(double speed) {
            this.speed = speed;
            return this;
        }

        public Builder course(double course) {
            this.course = course;
            return this;
        }

        public Builder deviceId(UUID deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public ReceiveLocationEvent build() {
            return new ReceiveLocationEvent(timestamp, latitude, longitude, altitude, speed, course, deviceId);
        }

    }

}
