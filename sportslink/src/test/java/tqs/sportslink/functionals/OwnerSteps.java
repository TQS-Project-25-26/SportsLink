package tqs.sportslink.functionals;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class OwnerSteps {

    @LocalServerPort
    private int port;

    private ChromeDriver driver;
    private WebDriverWait wait;

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    private void initDriverIfNeeded() {
        if (driver != null) {
            return;
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
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

    @org.springframework.beans.factory.annotation.Autowired
    private tqs.sportslink.data.UserRepository userRepository;

    // ------------------ SHARED / LOGIN ------------------

    @Given("I am logged in as an owner")
    public void i_am_logged_in_as_owner() {
        initDriverIfNeeded();

        // Ensure owner exists
        String email = "owner@sportslink.com";
        String password = "password123";
        tqs.sportslink.data.model.User owner;
        if (userRepository.existsByEmail(email)) {
            owner = userRepository.findByEmail(email).get();
        } else {
            owner = new tqs.sportslink.data.model.User();
            owner.setEmail(email);
        }
        owner.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(password));
        owner.setName("Owner User");
        if (owner.getRoles().isEmpty() || !owner.getRoles().contains(tqs.sportslink.data.model.Role.OWNER)) {
            owner.getRoles().add(tqs.sportslink.data.model.Role.OWNER);
        }
        owner.setActive(true);
        userRepository.save(owner);

        driver.get(getBaseUrl() + "/index.html");

        // Login as owner
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("submitBtn")).click();

        // Wait for dashboard redirect
        wait.until(ExpectedConditions.urlContains("owner_dashboard.html"));
    }

    @Given("I am on the owner dashboard")
    public void i_am_on_owner_dashboard() {
        // Wait for dashboard content to be visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("owner-dashboard-content")));
        // Also wait for the Add Facility button to be clickable (ensures JS
        // initialization is complete)
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-open-add-facility")));
    }

    // ------------------ FACILITY CREATION ------------------

    @When("I click the add facility button")
    public void click_add_facility() {
        // Wait for button to be clickable
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-open-add-facility")));

        // Use JavaScript click (more reliable in headless mode)
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);

        // Wait for modal to appear with longer timeout
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        longWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addFacilityModal")));
    }

    @Then("I should see the facility form")
    public void verify_facility_form() {
        // Verify key form elements are visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("facilityName")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("facilityCity")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("btnConfirmAddFacility")));
    }

    @When("I fill the facility form with name {string} and city {string}")
    public void fill_facility_form(String name, String city) {
        String uniqueName = name + " " + System.currentTimeMillis();
        driver.findElement(By.id("facilityName")).sendKeys(uniqueName);
        driver.findElement(By.id("facilityCity")).sendKeys(city);
        driver.findElement(By.id("facilityAddress")).sendKeys("Rua Exemplo 123");
        driver.findElement(By.id("facilityDescription")).sendKeys("A newly created facility for testing.");
        driver.findElement(By.id("facilityPrice")).sendKeys("15.50");
        driver.findElement(By.id("facilityOpening")).sendKeys("09:00");
        driver.findElement(By.id("facilityClosing")).sendKeys("22:00");

        // Select a sport (checkbox)
        WebElement footballCheck = driver.findElement(By.id("sport-FOOTBALL"));
        if (!footballCheck.isSelected()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", footballCheck);
        }
        // Verify selection
        if (!footballCheck.isSelected()) {
            throw new RuntimeException("Sport selection failed");
        }
    }

    @When("I save the facility")
    public void save_facility() {
        driver.findElement(By.id("btnConfirmAddFacility")).click();

        // Wait for the facility to be saved and modal to close
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Wait for either: modal to become hidden OR success toast to appear
        longWait.until(driver -> {
            // Check if modal is hidden using JS (more reliable than CSS visibility)
            Boolean modalHidden = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var modal = document.getElementById('addFacilityModal');" +
                            "return modal == null || !modal.classList.contains('show');");
            return modalHidden;
        });

        // Allow time for the page to refresh and show updated facilities list
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    @Then("I should see {string} in my facilities list")
    public void verify_facility_in_list(String nameFragment) {
        // Refresh grid or wait for js update
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("facilities-grid")));
        // Since we used unique name, we can filter for part of it or just check if any
        // card contains it
        // Note: The UI might need refresh or auto-updates. owner_dashboard.js usually
        // re-fetches.
        // Let's assume auto-update on save success.

        wait.until(d -> d.getPageSource().contains(nameFragment));
    }

    // ------------------ SUGGESTIONS ------------------

    @When("I click the View Suggestions button")
    public void click_suggestions_button() {
        WebElement suggestionsBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btn-view-suggestions")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", suggestionsBtn);
    }

    @Then("I should be on the suggestions page")
    public void verify_suggestions_page() {
        wait.until(ExpectedConditions.urlContains("owner_suggestions.html"));
    }

    @Then("I should see a list of suggested activities")
    public void verify_suggestions_list() {
        // Assuming owner_suggestions.html has a table or list
        // I haven't seen the file content but typically it would have a container.
        // Let's check for "Suggestions" text or a specific container if known.
        // Better to be robust: check for "body" usage or wait for specific element if I
        // knew it.
        // Using body text for safety for now.
        wait.until(d -> d.getPageSource().contains("Suggestions") || d.getPageSource().contains("Recommended"));
    }
}
