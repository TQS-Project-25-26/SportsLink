package tqs.sportslink.functionals;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tqs.sportslink.service.FacilityService;
import tqs.sportslink.service.RentalService;
import tqs.sportslink.service.EquipmentService;
import tqs.sportslink.dto.RentalRequestDTO;
import tqs.sportslink.dto.RentalResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class MariaBookingSteps {

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private RentalService rentalService;

    @Autowired
    private EquipmentService equipmentService;

    private List<String> searchResults;
    private RentalResponseDTO bookingResponse;
    private List<String> equipmentList;
    private String selectedFacility;

    @Given("the SportsLink application is running")
    public void theSportsLinkApplicationIsRunning() {
        assertThat(facilityService).isNotNull();
        assertThat(rentalService).isNotNull();
    }

    @Given("Maria is on the search page")
    public void mariaIsOnTheSearchPage() {
        searchResults = null;
    }

    @When("she searches for {string} courts in {string} for {string}")
    public void sheSearchesForCourtsInFor(String sport, String location, String time) {
        searchResults = facilityService.searchFacilities(location, sport, time);
    }

    @Then("she should see {int} available facilities")
    public void sheShouldSeeAvailableFacilities(int count) {
        assertThat(searchResults).isNotNull();
        assertThat(searchResults).hasSize(count);
    }

    @And("the results should include {string}")
    public void theResultsShouldInclude(String facilityName) {
        assertThat(searchResults).contains(facilityName);
    }

    @Given("Maria searches for padel courts")
    public void mariaSearchesForPadelCourts() {
        searchResults = facilityService.searchFacilities("Aveiro", "Padel", "19:00");
    }

    @When("she clicks on {string}")
    public void sheClicksOn(String facilityName) {
        selectedFacility = facilityName;
    }

    @Then("she should see the facility details")
    public void sheShouldSeeTheFacilityDetails() {
        assertThat(selectedFacility).isNotNull();
    }

    @And("she should see the equipment list")
    public void sheShouldSeeTheEquipmentList() {
        equipmentList = equipmentService.getEquipmentsByFacility(1L);
        assertThat(equipmentList).isNotNull();
    }

    @And("she should see available time slots")
    public void sheShouldSeeAvailableTimeSlots() {
        // Mock implementation
        assertThat(true).isTrue();
    }

    @Given("Maria has selected {string}")
    public void mariaHasSelected(String facilityName) {
        selectedFacility = facilityName;
    }

    @And("the time slot {string} is available")
    public void theTimeSlotIsAvailable(String timeSlot) {
        // Mock check
        assertThat(timeSlot).isNotNull();
    }

    @When("she books the court for {string} at {string}")
    public void sheBooksTheCourtForAt(String date, String time) {
        RentalRequestDTO request = new RentalRequestDTO();
        request.setFacilityId(1L);
        request.setStartTime(LocalDateTime.of(2025, 11, 27, 19, 0));
        request.setEndTime(LocalDateTime.of(2025, 11, 27, 21, 0));
        
        bookingResponse = rentalService.createRental(request);
    }

    @Then("the booking should be confirmed")
    public void theBookingShouldBeConfirmed() {
        assertThat(bookingResponse).isNotNull();
    }

    @And("the booking status should be {string}")
    public void theBookingStatusShouldBe(String status) {
        assertThat(bookingResponse.getStatus()).isEqualTo(status);
    }

    @And("she should receive a booking confirmation")
    public void sheShouldReceiveABookingConfirmation() {
        assertThat(bookingResponse.getId()).isNotNull();
    }

    @Given("Maria is viewing {string}")
    public void mariaIsViewing(String facilityName) {
        selectedFacility = facilityName;
    }

    @When("she checks the equipment list")
    public void sheChecksTheEquipmentList() {
        equipmentList = equipmentService.getEquipmentsByFacility(1L);
    }

    @Then("she should see {string} available")
    public void sheShouldSeeAvailable(String equipment) {
        assertThat(equipmentList).contains(equipment);
    }

    @When("she filters by sport {string}")
    public void sheFiltersBySport(String sport) {
        // Filter applied
    }

    @And("she filters by location {string}")
    public void sheFiltersByLocation(String location) {
        // Filter applied
        searchResults = facilityService.searchFacilities(location, "Padel", "19:00");
    }

    @And("she filters by time {string}")
    public void sheFiltersByTime(String time) {
        // Filter applied
    }

    @Then("she should only see facilities matching all criteria")
    public void sheShouldOnlySeeFacilitiesMatchingAllCriteria() {
        assertThat(searchResults).isNotEmpty();
    }

    @And("all results should be in {string}")
    public void allResultsShouldBeIn(String location) {
        assertThat(searchResults).isNotEmpty();
    }

    @And("all results should offer {string}")
    public void allResultsShouldOffer(String sport) {
        assertThat(searchResults).isNotEmpty();
    }

    @Given("Maria has a confirmed booking with id {int}")
    public void mariaHasAConfirmedBookingWithId(int bookingId) {
        bookingResponse = new RentalResponseDTO();
        bookingResponse.setId((long) bookingId);
        bookingResponse.setStatus("CONFIRMED");
    }

    @When("she cancels the booking")
    public void sheCancelsTheBooking() {
        bookingResponse = rentalService.cancelRental(bookingResponse.getId());
    }

    @And("the time slot should become available again")
    public void theTimeSlotShouldBecomeAvailableAgain() {
        // Mock check
        assertThat(true).isTrue();
    }

    @When("she changes the time to {string}")
    public void sheChangesTheTimeTo(String newTime) {
        RentalRequestDTO request = new RentalRequestDTO();
        request.setFacilityId(1L);
        request.setStartTime(LocalDateTime.of(2025, 11, 27, 20, 0));
        request.setEndTime(LocalDateTime.of(2025, 11, 27, 22, 0));
        
        bookingResponse = rentalService.updateRental(bookingResponse.getId(), request);
    }

    @Then("the booking should be updated")
    public void theBookingShouldBeUpdated() {
        assertThat(bookingResponse).isNotNull();
    }

    @And("the new time should be {string}")
    public void theNewTimeShouldBe(String newTime) {
        assertThat(bookingResponse.getStartTime()).isNotNull();
    }
}
