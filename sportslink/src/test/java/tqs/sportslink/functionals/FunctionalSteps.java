package tqs.sportslink.functionals;

import io.cucumber.java.After;
import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class FunctionalSteps {

    @LocalServerPort
    private int port;

    private FirefoxDriver driver;
    private WebDriverWait wait;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    // Cards de campos: qualquer .card dentro das zonas de resultados
    private static final By FACILITY_CARD =
            By.cssSelector("#featured .card, #nearbyCarousel .card");

    // ------------------ SETUP ------------------

    private void initDriverIfNeeded() {
        if (driver != null) {
            return;
        }

        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        // options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @After
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ------------------------------
    // Scenario 1 + 2
    // ------------------------------

    @Given("I am on the main search page")
    public void i_am_on_main_page() {
        initDriverIfNeeded();
        driver.get(getBaseUrl() + "/pages/main_page_user.html");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchBtn")));
    }

    @When("I select the sport {string} from the dropdown")
    public void i_select_sport_dropdown(String sport) {
        WebElement dropdown = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("sportFilter"))
        );
        // Com <select> é mais claro assim
        new Select(dropdown).selectByVisibleText(sport);
    }

    @When("I enter {string} into the location field")
    public void enter_location(String location) {
        // No teu HTML o campo é searchInput (sport OR location)
        WebElement input = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("searchInput"))
        );
        input.clear();
        input.sendKeys(location);
    }

    @When("I press the search button")
    public void press_search_button() {
        driver.findElement(By.id("searchBtn")).click();
    }

    @Then("I should see facilities related to {string}")
    public void facilities_related_to(String sport) {
        wait.until(d -> !d.findElements(FACILITY_CARD).isEmpty());

        // Ajuste para diferença de idioma entre o feature e o texto real
        String expected = sport.toLowerCase();
        if (expected.equals("football")) {
            expected = "futebol";   // é o que aparece no card
        }

        final String expectedText = expected;

        boolean match = driver.findElements(FACILITY_CARD)
                .stream()
                .anyMatch(card -> card.getText().toLowerCase().contains(expectedText));

        assertTrue(match, "No facilities found containing sport: " + sport);
    }

    @Then("I should see facilities located in {string}")
    public void facilities_in_location(String location) {
        wait.until(d -> !d.findElements(FACILITY_CARD).isEmpty());

        boolean match = driver.findElements(FACILITY_CARD)
                .stream()
                .anyMatch(card -> card.getText().toLowerCase().contains(location.toLowerCase()));

        assertTrue(match, "No facilities found in location: " + location);
    }

    // ------------------------------
    // Scenario 3
    // ------------------------------

    @Given("I performed a search for {string} in {string}")
    public void performed_search(String sport, String location) {
        i_am_on_main_page();
        i_select_sport_dropdown(sport);
        enter_location(location);
        press_search_button();
    }

    @When("I click on the first facility result")
    public void click_first_result() {
        WebElement firstCard = wait.until(driver -> {
            var cards = driver.findElements(FACILITY_CARD);
            return cards.isEmpty() ? null : cards.get(0);
        });

        // Se os cards tiverem um botão próprio (ex: .viewBtn), podes ajustar aqui.
        // Por agora, clicamos no próprio card.
        firstCard.click();
    }

    @Then("I should be on the facility details page")
    public void on_field_detail() {
        wait.until(ExpectedConditions.urlContains("field_detail"));
        assertTrue(driver.getCurrentUrl().contains("id="));
    }

    // ------------------------------
    // Scenario 4
    // ------------------------------

    @Given("I am on the facility details page for facility {int}")
    public void on_facility_details_page(int id) {
        initDriverIfNeeded();
        driver.get(getBaseUrl() + "/pages/field_detail.html?id=" + id);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("field-name")));
    }

    @When("I click the button to view all equipment")
    public void click_view_equipment() {
        driver.findElement(By.id("btn-view-equipments")).click();
        wait.until(ExpectedConditions.urlContains("equipments.html"));
    }

    @Then("I should see at least one equipment card")
    public void see_equipment_cards() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".equipment-card")));
        assertFalse(driver.findElements(By.cssSelector(".equipment-card")).isEmpty());
    }

    // ------------------------------
    // Scenario 5
    // ------------------------------

    @Given("I am viewing equipment for facility {int}")
    public void viewing_equipment(int id) {
        initDriverIfNeeded();
        driver.get(getBaseUrl() + "/pages/equipments.html?facilityId=" + id);
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".equipment-card")));
    }

    @When("I select at least one available equipment item")
    public void select_equipment() {
        boolean selected = false;
        for (WebElement card : driver.findElements(By.cssSelector(".equipment-card"))) {
            if (!card.getAttribute("class").contains("unavailable")) {
                card.click();
                selected = true;
                break;
            }
        }
        assertTrue(selected, "No available equipment found.");
    }

    @When("I press the continue button")
    public void press_continue() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.id("continue-btn")));
        btn.click();
    }

    @Then("I should be on the booking page")
    public void on_booking_page() {
        wait.until(ExpectedConditions.urlContains("booking.html"));
    }

    // ------------------------------
    // Scenario 6
    // ------------------------------

    @Given("I am on the booking page for facility {int}")
    public void on_booking_page(int id) {
        initDriverIfNeeded();
        driver.get(getBaseUrl() + "/pages/booking.html?facilityId=" + id);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("btn-confirm-booking")));
    }

    @When("I fill the booking form with valid data")
    public void fill_booking_form() {
        driver.findElement(By.id("user-name")).clear();
        driver.findElement(By.id("user-name")).sendKeys("Test User");

        driver.findElement(By.id("user-email")).clear();
        driver.findElement(By.id("user-email")).sendKeys("test@example.com");

        driver.findElement(By.id("user-phone")).clear();
        driver.findElement(By.id("user-phone")).sendKeys("987654321");
    }

    @When("I confirm the booking")
    public void confirm_booking() {
        driver.findElement(By.id("btn-confirm-booking")).click();
    }

    @Then("a booking confirmation modal should appear with an ID")
    public void booking_modal() {
        WebElement modal = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("successModal"))
        );
        String id = modal.findElement(By.id("booking-id")).getText().trim();
        assertFalse(id.isBlank());
    }
}
