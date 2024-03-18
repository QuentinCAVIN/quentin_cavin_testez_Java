package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ParkingServiceTest {

    private ParkingService parkingService = new ParkingService();

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;

    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
            when(inputReaderUtil.readSelection()).thenReturn(1);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest(){
        //GIVEN
        when(ticketDAO.getNbTicket(any())).thenReturn(1);
        //WHEN
        parkingService.processExitingVehicle();
        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        assertEquals((ticketDAO.getTicket("ANYCAR").getPrice()), Fare.CAR_RATE_PER_HOUR);

    }

    @Test
    //test de l’appel de la méthode processIncomingVehicle() où tout se déroule comme attendu.
    public void testProcessIncomingVehicle() {
        //GIVEN
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(5);
        //WHEN
        parkingService.processIncomingVehicle();
        //THEN
        verify(ticketDAO,Mockito.times(1)).saveTicket(any(Ticket.class));
    }

    @Test
    //exécution du test dans le cas où la méthode updateTicket() de ticketDAO renvoie false
    // lors de l’appel de processExitingVehicle()
    public void processExitingVehicleTestUnableUpdate(){
        //GIVEN
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        //WHEN
        parkingService.processExitingVehicle();
        //THEN
        verify(parkingSpotDAO,never()).updateParking(any(ParkingSpot.class));
        //Pour valider le test, il faudra réarranger les when() pour eviter les conflits.
        // @MockitoSettings(strictness = Strictness.LENIENT) permet de résoudre le probléme
    }

    @Test
    //test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour résultat l’obtention d’un spot
    // dont l’ID est 1 et qui est disponible.
    public void testGetNextParkingNumberIfAvailable(){
        //GIVEN
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(5);
        //WHEN
        parkingService.getNextParkingNumberIfAvailable();
        //THEN
        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
        assertEquals((parkingService.getNextParkingNumberIfAvailable()).getId(),5);
    }

    @Test
    //Test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour résultat aucun spot disponible (la méthode renvoie null).
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound(){
        //GIVEN
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(-1);
        //WHEN
        parkingService.getNextParkingNumberIfAvailable();
        //THEN
        assert(parkingService.getNextParkingNumberIfAvailable()==null);
    }

    @Test
    //Test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour résultat aucun spot (la méthode renvoie null)
    // car l’argument saisi par l’utilisateur concernant le type de véhicule est erroné (par exemple, l’utilisateur a saisi 3).
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument(){
        //GIVEN
        when(inputReaderUtil.readSelection()).thenReturn(3);
        //WHEN
        parkingService.getNextParkingNumberIfAvailable();
        //THEN
        verify(parkingSpotDAO, Mockito.times(0)).getNextAvailableSlot(any(ParkingType.class));
    }
}