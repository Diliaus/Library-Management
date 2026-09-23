package ir.ac.kntu.model;

import ir.ac.kntu.exception.InvalidSystemConfigurationException;

public class SystemConfiguration {

    private static SystemConfiguration instance;

    private int maxBorrowDays;
    private double fineRatePerDay;
    private int reservationExpiryDays;
    private int maxSimultaneousReservations;

    private SystemConfiguration() {
        this.maxBorrowDays = 14;
        this.fineRatePerDay = 5000.0;
        this.reservationExpiryDays = 3;
        this.maxSimultaneousReservations = 3;
    }

    public static synchronized SystemConfiguration getInstance() {
        if (instance == null) {
            instance = new SystemConfiguration();
        }
        return instance;
    }

    public int getMaxBorrowDays() {
        return maxBorrowDays;
    }

    public void setMaxBorrowDays(int maxBorrowDays) {
        if (maxBorrowDays <= 0) {
            throw new InvalidSystemConfigurationException("Max borrow days must be positive");
        }
        this.maxBorrowDays = maxBorrowDays;
    }

    public double getFineRatePerDay() {
        return fineRatePerDay;
    }

    public void setFineRatePerDay(double fineRatePerDay) {
        if (fineRatePerDay < 0) {
            throw new InvalidSystemConfigurationException("Fine rate cannot be negative");
        }
        this.fineRatePerDay = fineRatePerDay;
    }

    public int getReservationExpiryDays() {
        return reservationExpiryDays;
    }

    public void setReservationExpiryDays(int reservationExpiryDays) {
        if (reservationExpiryDays <= 0) {
            throw new InvalidSystemConfigurationException("Reservation expiry days must be positive");
        }
        this.reservationExpiryDays = reservationExpiryDays;
    }

    public int getMaxSimultaneousReservations() {
        return maxSimultaneousReservations;
    }

    public void setMaxSimultaneousReservations(int maxSimultaneousReservations) {
        if (maxSimultaneousReservations <= 0) {
            throw new InvalidSystemConfigurationException("Max simultaneous reservations must be positive");
        }
        this.maxSimultaneousReservations = maxSimultaneousReservations;
    }
}