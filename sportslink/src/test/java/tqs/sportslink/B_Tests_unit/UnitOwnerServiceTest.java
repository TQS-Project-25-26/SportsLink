package tqs.sportslink.B_Tests_unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import tqs.sportslink.data.EquipmentRepository;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Equipment;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.User;
import tqs.sportslink.dto.EquipmentRequestDTO;
import tqs.sportslink.dto.EquipmentResponseDTO;
import tqs.sportslink.dto.FacilityRequestDTO;
import tqs.sportslink.dto.FacilityResponseDTO;
import tqs.sportslink.service.OwnerService;
import tqs.sportslink.service.StorageService;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UnitOwnerServiceTest {

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private OwnerService ownerService;

    private User owner;
    private Facility facility;
    private FacilityRequestDTO facilityRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");

        facility = new Facility();
        facility.setId(10L);
        facility.setName("Original Name");
        facility.setOwner(owner);
        facility.setOpeningTime(LocalTime.of(8, 0));
        facility.setClosingTime(LocalTime.of(22, 0));

        owner.setFacilities(List.of(facility));

        facilityRequest = new FacilityRequestDTO();
        facilityRequest.setName("Updated Facility");
        facilityRequest.setSports(Collections.emptyList());
        facilityRequest.setCity("Aveiro");
        facilityRequest.setAddress("Campus");
        facilityRequest.setPricePerHour(15.0);
        facilityRequest.setOpeningTime("09:00");
        facilityRequest.setClosingTime("23:00");
    }

    // --- Facility Tests ---

    @Test
    void createFacility_WithImage_ShouldUploadAndSaveUrl() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        String expectedUrl = "http://minio/bucket/image.jpg";
        when(storageService.uploadFile(mockFile)).thenReturn(expectedUrl);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(facilityRepository.save(any(Facility.class))).thenAnswer(i -> {
            Facility f = i.getArgument(0);
            f.setId(100L);
            return f;
        });

        FacilityResponseDTO response = ownerService.createFacility(1L, facilityRequest, mockFile);

        assertThat(response).isNotNull();
        assertThat(response.getImageUrl()).isEqualTo(expectedUrl);
        verify(storageService).uploadFile(mockFile);
    }

    @Test
    void createFacility_WithoutImage_ShouldNotUpload() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(facilityRepository.save(any(Facility.class))).thenAnswer(i -> {
            Facility f = i.getArgument(0);
            f.setId(100L);
            return f;
        });

        FacilityResponseDTO response = ownerService.createFacility(1L, facilityRequest, null);

        assertThat(response.getImageUrl()).isNull();
        verify(storageService, never()).uploadFile(any());
    }

    @Test
    void getFacilities_ShouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        List<FacilityResponseDTO> result = ownerService.getFacilities(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Original Name");
    }

    @Test
    void updateFacility_WhenOwnerMatches_ShouldUpdate() {
        when(facilityRepository.findById(10L)).thenReturn(Optional.of(facility));
        when(facilityRepository.save(any(Facility.class))).thenReturn(facility);

        FacilityResponseDTO result = ownerService.updateFacility(1L, 10L, facilityRequest);

        assertThat(result.getName()).isEqualTo("Updated Facility");
        verify(facilityRepository).save(facility);
    }

    @Test
    void updateFacility_WhenNotOwner_ShouldThrow() {
        User otherOwner = new User();
        otherOwner.setId(2L);
        facility.setOwner(otherOwner); // Facility belongs to someone else

        when(facilityRepository.findById(10L)).thenReturn(Optional.of(facility));

        assertThatThrownBy(() -> ownerService.updateFacility(1L, 10L, facilityRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Owner does not own this facility");
    }

    // --- Equipment Tests ---

    @Test
    void addEquipment_WhenOwnerMatches_ShouldSuccess() {
        EquipmentRequestDTO equipRequest = new EquipmentRequestDTO();
        equipRequest.setName("New Ball");
        equipRequest.setType("Ball");
        equipRequest.setQuantity(10);
        equipRequest.setPricePerHour(2.0);
        equipRequest.setStatus("AVAILABLE");

        when(facilityRepository.findById(10L)).thenReturn(Optional.of(facility));
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(i -> {
            Equipment e = i.getArgument(0);
            e.setId(50L);
            return e;
        });

        EquipmentResponseDTO result = ownerService.addEquipment(1L, 10L, equipRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("New Ball");
    }

    @Test
    void addEquipment_WhenNotOwner_ShouldThrow() {
        User otherOwner = new User();
        otherOwner.setId(2L);
        facility.setOwner(otherOwner);

        when(facilityRepository.findById(10L)).thenReturn(Optional.of(facility));

        EquipmentRequestDTO equipRequest = new EquipmentRequestDTO();

        assertThatThrownBy(() -> ownerService.addEquipment(1L, 10L, equipRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Owner does not own this facility");
    }

    @Test
    void updateEquipment_WhenOwnerMatches_ShouldSuccess() {
        Equipment existing = new Equipment();
        existing.setId(50L);
        existing.setName("Old Ball");
        existing.setFacility(facility);

        EquipmentRequestDTO updateRequest = new EquipmentRequestDTO();
        updateRequest.setName("Updated Ball");
        updateRequest.setType("Ball");
        updateRequest.setQuantity(5);
        updateRequest.setPricePerHour(3.0);
        updateRequest.setStatus("AVAILABLE");

        when(equipmentRepository.findById(50L)).thenReturn(Optional.of(existing));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(existing);

        EquipmentResponseDTO result = ownerService.updateEquipment(1L, 50L, updateRequest);

        assertThat(result.getName()).isEqualTo("Updated Ball");
    }

    @Test
    void getEquipment_ShouldReturnList() {
        Equipment eq = new Equipment();
        eq.setId(50L);
        eq.setName("Ball");
        facility.setEquipments(List.of(eq));

        when(facilityRepository.findById(10L)).thenReturn(Optional.of(facility));

        List<EquipmentResponseDTO> result = ownerService.getEquipment(1L, 10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Ball");
    }

    @Test
    void createFacility_UserNotFound_ShouldThrow() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.createFacility(99L, facilityRequest, null))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Owner not found");
    }
}
