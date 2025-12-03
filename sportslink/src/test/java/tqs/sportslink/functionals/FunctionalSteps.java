package tqs.sportslink.functionals;

import io.cucumber.java.After;
import io.cucumber.java.en.*;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;
import org.springframework.boot.test.web.server.LocalServerPort;


import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class FunctionalSteps {

    @LocalServerPort
    private int port;

    private ChromeDriver driver;
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

        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run in headless mode for CI
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
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
        // Wait for the page to process the search and load results
        wait.until(webDriver -> !webDriver.findElements(FACILITY_CARD).isEmpty() || 
                        webDriver.findElements(By.cssSelector(".no-results")).size() > 0);
    }

    @Then("I should see facilities related to {string}")
    public void facilities_related_to(String sport) {
        // Ajuste para diferença de idioma entre o feature e o texto real
        String expected = sport.toLowerCase();
        if (expected.equals("football")) {
            expected = "futebol";   // é o que aparece no card
        }

        final String expectedText = expected;

        // Wait for cards and re-fetch them to avoid stale references
        // Increase timeout and add more robust waiting
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        longWait.until(d -> {
            var cards = d.findElements(FACILITY_CARD);
            if (cards.isEmpty()) {
                return false;
            }
            // Check if any card contains the expected text
            return cards.stream().anyMatch(card -> {
                try {
                    String text = card.getText().toLowerCase();
                    return text.contains(expectedText);
                } catch (StaleElementReferenceException e) {
                    return false;
                }
            });
        });
        
        // Final verification
        boolean match = driver.findElements(FACILITY_CARD)
                .stream()
                .anyMatch(card -> {
                    try {
                        return card.getText().toLowerCase().contains(expectedText);
                    } catch (StaleElementReferenceException e) {
                        return false;
                    }
                });

        assertTrue(match, "No facilities found containing sport: " + sport);
    }

    @Then("I should see facilities located in {string}")
    public void facilities_in_location(String location) {
        // Increase timeout and add more robust waiting
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        longWait.until(d -> {
            var cards = d.findElements(FACILITY_CARD);
            if (cards.isEmpty()) {
                return false;
            }
            // Check if any card contains the expected location
            return cards.stream().anyMatch(card -> {
                try {
                    String text = card.getText().toLowerCase();
                    return text.contains(location.toLowerCase());
                } catch (StaleElementReferenceException e) {
                    return false;
                }
            });
        });

        boolean match = driver.findElements(FACILITY_CARD)
                .stream()
                .anyMatch(card -> {
                    try {
                        return card.getText().toLowerCase().contains(location.toLowerCase());
                    } catch (StaleElementReferenceException e) {
                        return false;
                    }
                });

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
        WebElement firstCard = wait.until(webDriver -> {
            var cards = webDriver.findElements(FACILITY_CARD);
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
        WebElement button = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("btn-view-equipments")));
        
        // Scroll to element and use JavaScript click to avoid interception
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
        wait.until(ExpectedConditions.elementToBeClickable(button));
        
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
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
            if (!card.getDomAttribute("class").contains("unavailable")) {
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
        WebElement button = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("btn-confirm-booking")));
        
        // Scroll to element and use JavaScript click to avoid interception
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
        wait.until(ExpectedConditions.elementToBeClickable(button));
        
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
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
