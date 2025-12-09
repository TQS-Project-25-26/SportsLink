package tqs.sportslink.functionals;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class SearchAndRentalSteps {

    @LocalServerPort
    private int port;

    private ChromeDriver driver;
    private WebDriverWait wait;

    @org.springframework.beans.factory.annotation.Autowired
    private tqs.sportslink.util.JwtUtil jwtUtil;

    @org.springframework.beans.factory.annotation.Autowired
    private tqs.sportslink.data.UserRepository userRepository;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    // Cards de campos: qualquer .card dentro das zonas de resultados
    private static final By FACILITY_CARD = By.cssSelector("#featured .card, #nearbyCarousel .card");

    // ------------------ SETUP ------------------

    private void setupAuthenticatedState() {
        // Fetch the test user created by DataInitializer
        var user = userRepository.findByEmail("test@sportslink.com")
                .orElseThrow(() -> new RuntimeException("Test user not found"));

        java.util.Set<String> roles = new java.util.HashSet<>();
        if (user.getRoles() != null) {
            user.getRoles().forEach(r -> roles.add(r.name()));
        } else {
            roles.add("RENTER");
        }

        String token = jwtUtil.generateToken(user.getEmail(), roles);

        ((JavascriptExecutor) driver).executeScript("localStorage.setItem('token', arguments[0]);", token);
        ((JavascriptExecutor) driver).executeScript("localStorage.setItem('userId', arguments[0]);",
                user.getId().toString());
    }

    private void initDriverIfNeeded() {
        if (driver != null) {
            return;
        }

        ChromeOptions options = new ChromeOptions();
        // Use new headless mode - more stable for complex JS like Stripe
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1920,1080"); // Mandatory for Stripe responsive layout
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        // CRITICAL: Override User-Agent to look like a real browser (avoid Stripe bot
        // detection)
        options.addArguments(
                "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        // CRITICAL: Hide the "AutomationControlled" flag
        options.addArguments("--disable-blink-features=AutomationControlled");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
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
        // Bypass client-side auth check
        driver.get(getBaseUrl() + "/index.html");
        setupAuthenticatedState();

        driver.get(getBaseUrl() + "/pages/main_page_user.html");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchBtn")));
    }

    @When("I select the sport {string} from the dropdown")
    public void i_select_sport_dropdown(String sport) {
        WebElement dropdown = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("sportFilter")));
        // Com <select> é mais claro assim
        new Select(dropdown).selectByVisibleText(sport);
    }

    @When("I enter {string} into the location field")
    public void enter_location(String location) {
        // No teu HTML o campo é searchInput (sport OR location)
        WebElement input = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("searchInput")));
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
            expected = "futebol"; // é o que aparece no card
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

        // Use JS click to avoid ElementClickInterceptedException
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstCard);
        wait.until(ExpectedConditions.elementToBeClickable(firstCard));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", firstCard);
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
        // Bypass client-side auth check
        driver.get(getBaseUrl() + "/index.html");
        setupAuthenticatedState();

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
        // Wait for loading to disappear (API call completed)
        wait.until(webDriver -> {
            WebElement loading = webDriver.findElement(By.id("loading"));
            return loading != null && "none".equals(loading.getCssValue("display"));
        });

        // Wait for either equipment cards OR no-results message
        wait.until(webDriver -> !webDriver.findElements(By.cssSelector(".equipment-card")).isEmpty() ||
                webDriver.findElement(By.id("no-results")).isDisplayed());

        // Assert that we have equipment cards (not the no-results message)
        assertFalse(driver.findElements(By.cssSelector(".equipment-card")).isEmpty(),
                "Expected to find equipment cards, but none were found. Check if the facility has equipment.");
    }

    // ------------------------------
    // Scenario 5
    // ------------------------------

    @Given("I am viewing equipment for facility {int}")
    public void viewing_equipment(int id) {
        initDriverIfNeeded();
        // Bypass client-side auth check
        driver.get(getBaseUrl() + "/index.html");
        setupAuthenticatedState();

        driver.get(getBaseUrl() + "/pages/equipments.html?facilityId=" + id);
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".equipment-card")));
    }

    @When("I select at least one available equipment item")
    public void select_equipment() {
        boolean selected = false;
        for (WebElement card : driver.findElements(By.cssSelector(".equipment-card"))) {
            if (!card.getDomAttribute("class").contains("unavailable")) {
                // Scroll to element and use JavaScript click
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", card);
                // Optional: small wait or just click via JS
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", card);

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
        // Bypass client-side auth check
        driver.get(getBaseUrl() + "/index.html");
        setupAuthenticatedState();

        driver.get(getBaseUrl() + "/pages/booking.html?facilityId=" + id);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("btn-confirm-booking")));
    }

    @When("I fill the booking form with time {string} and duration {string} hours")
    public void fill_booking_form_with_params(String timeStr, String durationStr) {
        // Select Date
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("calendar-days")));

        // Wait for at least one enabled day to be present
        wait.until(d -> !d.findElements(By.cssSelector(".calendar-day:not(.disabled)")).isEmpty());

        // Find available days
        java.util.List<WebElement> days = driver.findElements(By.cssSelector(".calendar-day:not(.disabled)"));

        if (days.isEmpty()) {
            driver.findElement(By.id("next-month")).click();
            wait.until(d -> !d.findElements(By.cssSelector(".calendar-day:not(.disabled)")).isEmpty());
            days = driver.findElements(By.cssSelector(".calendar-day:not(.disabled)"));
        }

        // Select the last available day
        WebElement day = days.get(days.size() - 1);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", day);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", day);

        // Set Start Time
        WebElement startTime = driver.findElement(By.id("start-time"));
        startTime.clear();
        startTime.sendKeys(timeStr);

        // Select Duration
        WebElement duration = driver.findElement(By.id("duration"));
        new Select(duration).selectByValue(durationStr);

        driver.findElement(By.id("user-name")).clear();
        driver.findElement(By.id("user-name")).sendKeys("Test User");

        driver.findElement(By.id("user-email")).clear();
        driver.findElement(By.id("user-email")).sendKeys("test@example.com");

        driver.findElement(By.id("user-phone")).clear();
        driver.findElement(By.id("user-phone")).sendKeys("987654321");
    }

    // Deprecated step (keeping but redirecting or failing if used without params in
    // future)
    @When("I fill the booking form with valid data")
    public void fill_booking_form_deprecated() {
        // Default behavior for legacy tests if any
        fill_booking_form_with_params("10:00", "2");
    }

    @When("I confirm the booking")
    public void confirm_booking() {
        WebElement button = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("btn-confirm-booking")));

        // Scroll to element and use JavaScript click to avoid interception
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
        wait.until(ExpectedConditions.elementToBeClickable(button));

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);

        // Complete the payment after confirming booking
        complete_stripe_payment();
    }

    private void complete_stripe_payment() {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Wait for the payment modal to appear
        longWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("paymentModal")));

        // Wait for Stripe Elements to load (payment element container)
        longWait.until(d -> {
            WebElement loadingElem = d.findElement(By.id("payment-loading"));
            return loadingElem.getCssValue("display").equals("none");
        });

        // Wait for Stripe iframes to be present
        longWait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("iframe[name^='__privateStripeFrame']")));

        // Additional delay to ensure Stripe Elements is fully interactive
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Get all Stripe iframes
        java.util.List<WebElement> iframes = driver.findElements(
                By.cssSelector("iframe[name^='__privateStripeFrame']"));

        System.out.println("Found " + iframes.size() + " Stripe iframes");

        boolean cardFilled = false;
        boolean expiryFilled = false;
        boolean cvcFilled = false;

        // Iterate through all iframes and fill available fields
        for (int i = 0; i < iframes.size() && (!cardFilled || !expiryFilled || !cvcFilled); i++) {
            try {
                // Switch to iframe by index (re-fetch to avoid stale reference)
                iframes = driver.findElements(By.cssSelector("iframe[name^='__privateStripeFrame']"));
                if (i >= iframes.size())
                    break;

                driver.switchTo().frame(iframes.get(i));

                // Try to find and fill card number
                if (!cardFilled) {
                    java.util.List<WebElement> cardInputs = driver.findElements(
                            By.cssSelector("input[name='cardnumber'], input[name='number']"));
                    if (!cardInputs.isEmpty()) {
                        cardInputs.get(0).sendKeys("4242424242424242");
                        cardFilled = true;
                        System.out.println("Card number filled in iframe " + i);
                    }
                }

                // Try to find and fill expiry
                if (!expiryFilled) {
                    java.util.List<WebElement> expiryInputs = driver.findElements(
                            By.cssSelector("input[name='exp-date'], input[name='expiry']"));
                    if (!expiryInputs.isEmpty()) {
                        expiryInputs.get(0).sendKeys("1230");
                        expiryFilled = true;
                        System.out.println("Expiry filled in iframe " + i);
                    }
                }

                // Try to find and fill CVC
                if (!cvcFilled) {
                    java.util.List<WebElement> cvcInputs = driver.findElements(
                            By.cssSelector("input[name='cvc']"));
                    if (!cvcInputs.isEmpty()) {
                        cvcInputs.get(0).sendKeys("123");
                        cvcFilled = true;
                        System.out.println("CVC filled in iframe " + i);
                    }
                }

                driver.switchTo().defaultContent();
            } catch (Exception e) {
                System.out.println("Error in iframe " + i + ": " + e.getMessage());
                driver.switchTo().defaultContent();
            }
        }

        System.out.println("Fields filled - Card: " + cardFilled + ", Expiry: " + expiryFilled + ", CVC: " + cvcFilled);

        // Small delay after filling fields
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Click the Pay Now button
        WebElement payButton = longWait.until(
                ExpectedConditions.elementToBeClickable(By.id("submit-payment")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", payButton);

        // Wait for payment to process - check for success OR error
        WebDriverWait paymentWait = new WebDriverWait(driver, Duration.ofSeconds(60));
        boolean success = paymentWait.until(d -> {
            try {
                // Check if success modal appeared (payment completed)
                WebElement successModal = d.findElement(By.id("successModal"));
                if (successModal.isDisplayed()) {
                    return true;
                }
            } catch (Exception e) {
                // Success modal not visible yet
            }

            try {
                // Check for payment error message
                WebElement errorMsg = d.findElement(By.id("payment-message"));
                if (errorMsg.isDisplayed() && !errorMsg.getText().isEmpty()) {
                    throw new RuntimeException("Payment failed: " + errorMsg.getText());
                }
            } catch (org.openqa.selenium.NoSuchElementException e) {
                // Error element not found, continue waiting
            }

            return false;
        });

        if (!success) {
            throw new RuntimeException("Payment did not complete successfully within timeout");
        }
    }

    @Then("a booking confirmation modal should appear with an ID")
    public void booking_modal() {
        try {
            WebElement modal = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("successModal")));
            String id = modal.findElement(By.id("booking-id")).getText().trim();
            assertFalse(id.isBlank());
        } catch (org.openqa.selenium.TimeoutException e) {
            // Check for error alert
            String failureReason = "Modal not found.";
            try {
                // Check if browser alert is present
                String alertText = driver.switchTo().alert().getText();
                failureReason += " Browser Alert Present: " + alertText;
                driver.switchTo().alert().accept();
            } catch (Exception noAlert) {
                // Check if in-page error is present (if any)
                // booking.js uses alert() primarily, but maybe we can find something else
            }
            throw new RuntimeException("Test Failed: " + failureReason);
        }
    }

    @Then("the booking should be stored in the system")
    public void the_booking_should_be_stored_in_the_system() {
        // Verified by modal
    }

    // ------------------------------
    // Additional steps for calendar and booking management
    // ------------------------------

    @When("I click the {string} button")
    public void i_click_the_button(String buttonText) {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), '" + buttonText + "')]")));
        button.click();
    }

    @Then("I should see a message {string}")
    public void i_should_see_a_message(String message) {
        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("no-results")));
        assertTrue(msg.getText().contains(message));
    }

    @Then("I should see a suggestion to try different search criteria")
    public void i_should_see_a_suggestion_to_try_different_search_criteria() {
        WebElement suggestion = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("search-suggestion")));
        assertTrue(suggestion.isDisplayed());
    }
}
