package tqs.sportslink.service;

import org.springframework.stereotype.Service;

import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.EquipmentRepository;
import tqs.sportslink.data.UserRepository;

import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Equipment;
import tqs.sportslink.data.model.User;

import tqs.sportslink.dto.FacilityRequestDTO;
import tqs.sportslink.dto.FacilityResponseDTO;
import tqs.sportslink.dto.EquipmentRequestDTO;
import tqs.sportslink.dto.EquipmentResponseDTO;

import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OwnerService {

    private static final Logger logger = LoggerFactory.getLogger(OwnerService.class);

        private final FacilityRepository facilityRepository;
        private final EquipmentRepository equipmentRepository;
        private final UserRepository userRepository;
        private final StorageService storageService;

        private static final String FACILITY_NOT_FOUND = "Facility not found";

        public OwnerService(FacilityRepository facilityRepository,
                        EquipmentRepository equipmentRepository,
                        UserRepository userRepository,
                        StorageService storageService) {
                this.facilityRepository = facilityRepository;
                this.equipmentRepository = equipmentRepository;
                this.userRepository = userRepository;
                this.storageService = storageService;
        }

        // ============================
        // FACILITY MANAGEMENT
        // ============================

        public FacilityResponseDTO createFacility(Long ownerId, FacilityRequestDTO dto,
                        org.springframework.web.multipart.MultipartFile imageFile) {

                User owner = userRepository.findById(ownerId)
                                .orElseThrow(() -> new NoSuchElementException("Owner not found"));

                Facility facility = new Facility();
                facility.setName(dto.getName());
                facility.setSports(dto.getSports());
                facility.setCity(dto.getCity());
                facility.setAddress(dto.getAddress());
                facility.setDescription(dto.getDescription());
                facility.setPricePerHour(dto.getPricePerHour());
                facility.setOpeningTime(LocalTime.parse(dto.getOpeningTime()));
                facility.setClosingTime(LocalTime.parse(dto.getClosingTime()));
                facility.setOwner(owner);

                if (imageFile != null && !imageFile.isEmpty()) {
                        String imageUrl = storageService.uploadFile(imageFile);
                        facility.setImageUrl(imageUrl);
                }

                Facility saved = facilityRepository.save(facility);
                logger.info("Owner {} created new facility: {} (id={})", ownerId, saved.getName(), saved.getId());

                String openingTimeStr = saved.getOpeningTime() != null ? saved.getOpeningTime().toString() : null;
                String closingTimeStr = saved.getClosingTime() != null ? saved.getClosingTime().toString() : null;

                return new FacilityResponseDTO(
                                saved.getId(),
                                saved.getName(),
                                saved.getImageUrl(),
                                saved.getSports(),
                                saved.getCity(),
                                saved.getAddress(),
                                saved.getDescription(),
                                saved.getPricePerHour(),
                                saved.getRating(),
                                openingTimeStr,
                                closingTimeStr);
        }

        public List<FacilityResponseDTO> getFacilities(Long ownerId) {

                User owner = userRepository.findById(ownerId)
                                .orElseThrow(() -> new NoSuchElementException("Owner not found"));

                List<Facility> facilities = owner.getFacilities();

                return facilities.stream()
                                .filter(f -> !"DELETED".equals(f.getStatus()))
                                .map(f -> {
                                        String openingTimeStr = f.getOpeningTime() != null
                                                        ? f.getOpeningTime().toString()
                                                        : null;
                                        String closingTimeStr = f.getClosingTime() != null
                                                        ? f.getClosingTime().toString()
                                                        : null;

                                        return new FacilityResponseDTO(
                                                        f.getId(),
                                                        f.getName(),
                                                        f.getImageUrl(),
                                                        f.getSports(),
                                                        f.getCity(),
                                                        f.getAddress(),
                                                        f.getDescription(),
                                                        f.getPricePerHour(),
                                                        f.getRating(),
                                                        openingTimeStr,
                                                        closingTimeStr);
                                })
                                .toList();
        }

        public void deleteFacility(Long ownerId, Long facilityId) {
                Facility facility = facilityRepository.findById(facilityId)
                                .orElseThrow(() -> new NoSuchElementException(FACILITY_NOT_FOUND));

                if (!facility.getOwner().getId().equals(ownerId)) {
                        throw new IllegalArgumentException("Owner does not own this facility");
                }

                facility.setStatus("DELETED");
                facilityRepository.save(facility);
                logger.info("Owner {} deleted facility id={}", ownerId, facilityId);
        }

        public FacilityResponseDTO updateFacility(Long ownerId, Long facilityId, FacilityRequestDTO dto) {

                Facility facility = facilityRepository.findById(facilityId)
                                .orElseThrow(() -> new NoSuchElementException(FACILITY_NOT_FOUND));

                if (!facility.getOwner().getId().equals(ownerId)) {
                        throw new IllegalArgumentException("Owner does not own this facility");
                }

                facility.setName(dto.getName());
                facility.setSports(dto.getSports());
                facility.setCity(dto.getCity());
                facility.setAddress(dto.getAddress());
                facility.setDescription(dto.getDescription());
                facility.setPricePerHour(dto.getPricePerHour());
                facility.setOpeningTime(LocalTime.parse(dto.getOpeningTime()));
                facility.setClosingTime(LocalTime.parse(dto.getClosingTime()));

                Facility saved = facilityRepository.save(facility);
                logger.info("Owner {} updated facility id={}", ownerId, facilityId);

                String openingTimeStr = saved.getOpeningTime() != null ? saved.getOpeningTime().toString() : null;
                String closingTimeStr = saved.getClosingTime() != null ? saved.getClosingTime().toString() : null;

                return new FacilityResponseDTO(
                                saved.getId(),
                                saved.getName(),
                                saved.getImageUrl(),
                                saved.getSports(),
                                saved.getCity(),
                                saved.getAddress(),
                                saved.getDescription(),
                                saved.getPricePerHour(),
                                saved.getRating(),
                                openingTimeStr,
                                closingTimeStr);
        }

        // ============================
        // EQUIPMENT MANAGEMENT
        // ============================

        public EquipmentResponseDTO addEquipment(Long ownerId, Long facilityId, EquipmentRequestDTO dto) {

                Facility facility = facilityRepository.findById(facilityId)
                                .orElseThrow(() -> new NoSuchElementException(FACILITY_NOT_FOUND));

                if (!facility.getOwner().getId().equals(ownerId)) {
                        throw new IllegalArgumentException("Owner does not own this facility");
                }

                Equipment equipment = new Equipment();
                equipment.setName(dto.getName());
                equipment.setType(dto.getType());
                equipment.setDescription(dto.getDescription());
                equipment.setQuantity(dto.getQuantity());
                equipment.setPricePerHour(dto.getPricePerHour());
                equipment.setStatus(dto.getStatus());
                equipment.setFacility(facility);

                Equipment saved = equipmentRepository.save(equipment);
                logger.info("Owner {} added equipment {} to facility {}", ownerId, saved.getName(), facilityId);

                return new EquipmentResponseDTO(
                                saved.getId(),
                                saved.getName(),
                                saved.getType(),
                                saved.getDescription(),
                                saved.getQuantity(),
                                saved.getPricePerHour(),
                                saved.getStatus());
        }

        public List<EquipmentResponseDTO> getEquipment(Long ownerId, Long facilityId) {

                Facility facility = facilityRepository.findById(facilityId)
                                .orElseThrow(() -> new NoSuchElementException(FACILITY_NOT_FOUND));

                if (!facility.getOwner().getId().equals(ownerId)) {
                        throw new IllegalArgumentException("Owner does not own this facility");
                }

                return facility.getEquipments()
                                .stream()
                                .map(eq -> new EquipmentResponseDTO(
                                                eq.getId(),
                                                eq.getName(),
                                                eq.getType(),
                                                eq.getDescription(),
                                                eq.getQuantity(),
                                                eq.getPricePerHour(),
                                                eq.getStatus()))
                                .toList();
        }

        public EquipmentResponseDTO updateEquipment(Long ownerId, Long equipmentId, EquipmentRequestDTO dto) {

                Equipment equipment = equipmentRepository.findById(equipmentId)
                                .orElseThrow(() -> new NoSuchElementException("Equipment not found"));

                if (!equipment.getFacility().getOwner().getId().equals(ownerId)) {
                        throw new IllegalArgumentException("Owner does not own this equipment");
                }

                equipment.setName(dto.getName());
                equipment.setType(dto.getType());
                equipment.setDescription(dto.getDescription());
                equipment.setQuantity(dto.getQuantity());
                equipment.setPricePerHour(dto.getPricePerHour());
                equipment.setStatus(dto.getStatus());

                Equipment saved = equipmentRepository.save(equipment);
                logger.info("Owner {} updated equipment id={}", ownerId, equipmentId);

                return new EquipmentResponseDTO(
                                saved.getId(),
                                saved.getName(),
                                saved.getType(),
                                saved.getDescription(),
                                saved.getQuantity(),
                                saved.getPricePerHour(),
                                saved.getStatus());
        }
}
