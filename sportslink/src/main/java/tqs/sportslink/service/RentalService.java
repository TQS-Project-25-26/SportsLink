package tqs.sportslink.service;

import org.springframework.stereotype.Service;
import tqs.sportslink.dto.RentalRequestDTO;
import tqs.sportslink.dto.RentalResponseDTO;



@Service
public class RentalService {

    public RentalResponseDTO createRental(RentalRequestDTO request) {
        RentalResponseDTO response = new RentalResponseDTO();
        return response;
    }

    public RentalResponseDTO cancelRental(Long rentalId) {
        RentalResponseDTO response = new RentalResponseDTO();
        return response;
    }

    public RentalResponseDTO updateRental(Long rentalId, RentalRequestDTO request) {
        RentalResponseDTO response = new RentalResponseDTO();
        return response;
    }

    public RentalResponseDTO getRentalStatus(Long rentalId) {
        RentalResponseDTO response = new RentalResponseDTO();
        return response;
    }
}
