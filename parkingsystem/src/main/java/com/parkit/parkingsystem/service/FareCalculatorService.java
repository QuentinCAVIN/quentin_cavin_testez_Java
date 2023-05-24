package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        double inMillis = ticket.getInTime().getTime();
        double outMillis = ticket.getOutTime().getTime();

        double duration = (outMillis - inMillis) / (1000 * 60 * 60);
        if (duration <= 0.5) {
            ticket.setPrice(0);
        } else {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    if (discount == false) { // ajouter les Math round au codio
                        ticket.setPrice(Math.round(duration * Fare.CAR_RATE_PER_HOUR *100.0)/100.0);
                    } else {
                        ticket.setPrice(Math.round(((duration * Fare.CAR_RATE_PER_HOUR) - (duration * Fare.CAR_RATE_PER_HOUR * 0.05))*100.0) /100.0);
                    }
                    break;
                }
                case BIKE: {
                    if (discount == false) {
                        ticket.setPrice(Math.round(duration * Fare.BIKE_RATE_PER_HOUR * 100.0)/100.0);
                    } else {
                        ticket.setPrice(Math.round(((duration * Fare.BIKE_RATE_PER_HOUR) - (duration * Fare.BIKE_RATE_PER_HOUR * 0.05))*100.0)/100.0);
                    }
                    break;
                }

                default:
                    throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
    }

    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}