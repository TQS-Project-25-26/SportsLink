package tqs.sportslink.functionals;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Duration;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
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
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Sport;
import java.util.List;

public class AdminSteps {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;


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
        tqs.sportslink.data.model.User admin;
        if (userRepository.existsByEmail(email)) {
            admin = userRepository.findByEmail(email).get();
        } else {
            admin = new tqs.sportslink.data.model.User();
            admin.setEmail(email);
        }
        admin.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(password));
        admin.setName("Admin User");
        // Ensure role
        if (admin.getRoles().size() == 0 || !admin.getRoles().contains(tqs.sportslink.data.model.Role.ADMIN)) {
            admin.getRoles().add(tqs.sportslink.data.model.Role.ADMIN);
        }
        admin.setActive(true);
        userRepository.save(admin);
    }

    @Given("I exist as a renter with email {string} and password {string}")
    public void ensureRenterExists(String email, String password) {
        initDriverIfNeeded();
        tqs.sportslink.data.model.User renter;
        if (userRepository.existsByEmail(email)) {
            renter = userRepository.findByEmail(email).get();
        } else {
            renter = new tqs.sportslink.data.model.User();
            renter.setEmail(email);
        }
        renter.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(password));
        renter.setName("Renter User");
        if (renter.getRoles().size() == 0 || !renter.getRoles().contains(tqs.sportslink.data.model.Role.RENTER)) {
            renter.getRoles().add(tqs.sportslink.data.model.Role.RENTER);
        }
        renter.setActive(true);
        userRepository.save(renter);
    }

    @Given("I am logged in as admin")
    public void loginAsAdmin() {
        initDriverIfNeeded();
        // Ensure admin exists first (default credentials)
        ensureAdminExists("admin@admin.com", "pwdAdmin");

        driver.get(getBaseUrl() + "/index.html");

        // Simple wait for page load
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));

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
        tqs.sportslink.data.model.User user;
        if (userRepository.existsByEmail(email)) {
            user = userRepository.findByEmail(email).get();
        } else {
            user = new tqs.sportslink.data.model.User();
            user.setEmail(email);
        }
        user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("password"));
        if (user.getName() == null)
            user.setName("Test User");
        if (user.getRoles().isEmpty())
            user.getRoles().add(tqs.sportslink.data.model.Role.RENTER);
        user.setActive(true);
        userRepository.save(user);
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

        // 1. Esperar que a tabela das facilities deixe de mostrar "Loading
        // facilities..."
        wait.until(ExpectedConditions.not(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("facilities-table-body"), "Loading facilities...")));

        boolean found = false;

        while (true) {
            // 2. Obter o tbody da página atual
            WebElement tbody = driver.findElement(By.id("facilities-table-body"));

            // 3. Ver se nesta página existe a facility
            if (tbody.getText().contains(facilityName)) {
                // Encontrar a row específica
                WebElement row = tbody.findElement(By.xpath(
                        ".//tr[td[contains(normalize-space(.), '" + facilityName + "')]]"));

                WebElement deleteBtn = row.findElement(By.cssSelector(".btn-danger"));
                deleteBtn.click();

                // Confirmar o alert do browser
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                alert.accept();

                found = true;
                break;
            }

            // 4. Se não encontrámos nesta página, tentar ir para a próxima

            // Botão Next dentro de #facilities-pagination que NÃO esteja disabled
            java.util.List<WebElement> nextButtons = driver.findElements(
                    By.xpath(
                            "//div[@id='facilities-pagination']//button[normalize-space()='Next' and not(@disabled)]"));

            // Se não há Next ativo, não há mais páginas → sair do ciclo
            if (nextButtons.isEmpty()) {
                break;
            }

            // Clicar em Next
            WebElement next = nextButtons.get(0);
            // Guardar o texto atual da tabela para detetar mudança de página
            String oldTableText = tbody.getText();

            next.click();

            // Esperar até o conteúdo da tabela mudar (nova página renderizada)
            wait.until(d -> {
                WebElement newTbody = driver.findElement(By.id("facilities-table-body"));
                return !newTbody.getText().equals(oldTableText);
            });
        }

        // 5. Falhar explicitamente se nunca encontrámos
        org.junit.jupiter.api.Assertions.assertTrue(
                found,
                "Facility '" + facilityName + "' was not found in any facilities table page");
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

        // Use a retry mechanism or explicit wait to handle
        // StaleElementReferenceException
        wait.until(d -> {
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
    public void checkFacilityDeleted(String facilityName) {

        // 1. Espera até o DELETE terminar e a tabela ser recarregada
        wait.until(ExpectedConditions.not(
                ExpectedConditions.textToBePresentInElementLocated(
                        By.id("facilities-table-body"), facilityName)));

        boolean found = false;

        while (true) {

            WebElement tbody = driver.findElement(By.id("facilities-table-body"));

            // 2. Verifica se nesta página ainda existe
            if (tbody.getText().contains(facilityName)) {
                found = true;
                break;
            }

            // 3. Procurar botão NEXT que não esteja disabled
            List<WebElement> nextButtons = driver.findElements(
                    By.xpath(
                            "//div[@id='facilities-pagination']//button[normalize-space()='Next' and not(@disabled)]"));

            // 4. Se não há Next → acabaram as páginas
            if (nextButtons.isEmpty()) {
                break;
            }

            // 5. Clicar NEXT e esperar a página carregar
            WebElement next = nextButtons.get(0);
            String oldTable = tbody.getText();
            next.click();

            wait.until(d -> {
                String newTable = driver.findElement(By.id("facilities-table-body")).getText();
                return !newTable.equals(oldTable); // tabela mudou → nova página
            });
        }

        // 6. Se ainda existe em alguma página → falha
        assertFalse(
                found,
                "Facility '" + facilityName + "' still appears in one of the facilities pages after deletion!");
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
