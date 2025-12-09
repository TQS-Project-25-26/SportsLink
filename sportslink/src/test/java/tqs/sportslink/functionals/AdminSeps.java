package tqs.sportslink.functionals;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Sport;
import java.util.List;
import tqs.sportslink.util.JwtUtil;

public class AdminSeps {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;

    @org.springframework.beans.factory.annotation.Autowired
    private JwtUtil jwtUtil;

    @org.springframework.beans.factory.annotation.Autowired
    private UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private FacilityRepository facilityRepository;

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
        js = (JavascriptExecutor) driver;
    }

    @After
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ============================================
    // GIVEN Steps
    // ============================================

    @Given("I exist as an admin with email {string} and password {string}")
    public void ensureAdminExists(String email, String password) {
        initDriverIfNeeded();
        // Setup is handled by DataInitializer usually, but we ensure DB state if needed
        // (optional)
        // Here we assume the user exists as per DataInitializer or integration test
        // setup
    }

    @Given("I exist as a renter with email {string} and password {string}")
    public void ensureRenterExists(String email, String password) {
        initDriverIfNeeded();
        // Assuming user exists via DataInitializer
    }

    @Given("I am logged in as admin")
    public void loginAsAdmin() {
        initDriverIfNeeded();
        driver.get(getBaseUrl() + "/index.html");

        // Manual login flow as per selenium export
        driver.findElement(By.id("email")).click();
        driver.findElement(By.id("email")).clear();
        driver.findElement(By.id("email")).sendKeys("admin@admin.com");

        driver.findElement(By.id("password")).click();
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys("pwdAdmin");

        driver.findElement(By.id("submitBtn")).click();

        // Wait for redirect to dashboard
        wait.until(ExpectedConditions.urlContains("admin.html"));
    }

    @Given("a user {string} exists and is active")
    public void ensureUserActive(String email) {
        // In a real test we might interact with DB to ensure state,
        // but for functional UI test we assume DataInitializer did its job.
    }

    @Given("a facility {string} exists")
    public void ensureFacilityExists(String name) {
        if (!facilityRepository.existsByName(name)) {
            Facility f = new Facility();
            f.setName(name);
            f.setCity("Test City");
            f.setAddress("Test Address");
            f.setPricePerHour(10.0);
            f.setLatitude(0.0);
            f.setLongitude(0.0);
            f.setStatus("ACTIVE");
            f.setSports(List.of(Sport.FOOTBALL));
            facilityRepository.save(f);
        }
    }

    // ============================================
    // WHEN Steps
    // ============================================

    @When("I login as {string} with password {string}")
    public void login(String email, String password) {
        initDriverIfNeeded();
        driver.get(getBaseUrl() + "/index.html");

        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        emailInput.clear();
        emailInput.sendKeys(email);

        WebElement passInput = driver.findElement(By.id("password"));
        passInput.clear();
        passInput.sendKeys(password);

        driver.findElement(By.id("submitBtn")).click();
    }

    @When("I navigate to the users management page")
    public void navigateToUsers() {
        WebElement navUsers = wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-users")));
        navUsers.click();
        // Wait for table to load
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#users-table-body tr")));
    }

    @When("I click to block user {string}")
    public void blockUser(String email) {
        // Find row with email
        // Selenium export used: driver.findElement(By.cssSelector("tr:nth-child(3) >
        // td:nth-child(6) > .btn-danger")).click();
        // We make it more dynamic by searching the row

        WebElement row = driver.findElement(By.xpath("//tr[td[contains(text(), '" + email + "')]]"));
        WebElement blockBtn = row.findElement(By.cssSelector(".btn-danger")); // Assuming 'block' button is red/danger

        blockBtn.click();

        // Confirmation alert
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
    }

    @When("I navigate to the facilities management page")
    public void navigateToFacilities() {
        WebElement navFacilities = wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-facilities")));
        navFacilities.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#facilities-table-body tr")));
    }

    @When("I click to delete facility {string}")
    public void deleteFacility(String facilityName) {
        // Wait for data to load first
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".loading")));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("facilities-table-body"), facilityName));

        // Find row by name
        WebElement row = driver.findElement(By.xpath("//tr[td[contains(text(), '" + facilityName + "')]]"));
        WebElement deleteBtn = row.findElement(By.cssSelector(".btn-danger"));

        deleteBtn.click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
    }

    @When("I try to access the admin dashboard")
    public void tryAccessDashboard() {
        // Direct Navigation
        driver.get(getBaseUrl() + "/admin.html");
    }

    // ============================================
    // THEN Steps
    // ============================================

    @Then("I should be redirected to the admin dashboard")
    public void checkDashboardRedirect() {
        wait.until(ExpectedConditions.urlContains("admin.html"));
        // Check for specific admin elements (e.g. stats nav)
        assertTrue(driver.findElements(By.id("nav-stats")).size() > 0);
    }

    @Then("I should see system statistics")
    public void checkStats() {
        WebElement navStats = wait.until(ExpectedConditions.elementToBeClickable(By.id("nav-stats")));
        navStats.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rentalsBySportChart")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rentalsByStatusChart")));
    }

    @Then("I should see a list of users")
    public void checkUserList() {
        WebElement table = driver.findElement(By.id("users-table-body"));
        assertTrue(table.findElements(By.tagName("tr")).size() > 0);
    }

    @Then("I should see user {string}")
    public void checkUserExists(String email) {
        WebElement table = driver.findElement(By.id("users-table-body"));
        assertTrue(table.getText().contains(email));
    }

    @Then("the user {string} should be marked as inactive")
    public void checkUserInactive(String email) {
        // Wait for potential page refresh first
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        // Use a retry mechanism or explicit wait to handle
        // StaleElementReferenceException
        wait.until(driver -> {
            try {
                WebElement row = driver.findElement(By.xpath("//tr[td[contains(text(), '" + email + "')]]"));
                // Check for visual indication (Activate implies it is currently
                // Inactive/Blocked)
                // Or check the status text column if available
                return row.getText().contains("Inactive") || row.getText().contains("Activate");
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                return false; // Retry
            }
        });
    }

    @Then("I should see a list of facilities")
    public void checkFacilitiesList() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".loading")));
        WebElement table = driver.findElement(By.id("facilities-table-body"));
        assertTrue(table.findElements(By.tagName("tr")).size() > 0);
    }

    @Then("I should see facility {string}")
    public void checkFacilityExists(String name) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("facilities-table-body"), name));
        WebElement table = driver.findElement(By.id("facilities-table-body"));
        assertTrue(table.getText().contains(name));
    }

    @Then("the facility {string} should no longer appear in the list")
    public void checkFacilityDeleted(String name) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        WebElement table = driver.findElement(By.id("facilities-table-body"));
        assertFalse(table.getText().contains(name));
    }

    @Then("I should stay on the home page")
    public void checkHomePage() {
        // Main page user or index
        assertTrue(driver.getCurrentUrl().contains("index.html")
                || driver.getCurrentUrl().contains("main_page_user.html"));
    }

    @Then("I should not see the admin dashboard")
    public void checkNoAdminDashboard() {
        assertFalse(driver.getCurrentUrl().contains("admin.html"));
    }
}
