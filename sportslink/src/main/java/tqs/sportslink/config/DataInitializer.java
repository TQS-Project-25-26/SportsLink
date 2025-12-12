package tqs.sportslink.config;

import java.time.LocalTime;
import java.util.List;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tqs.sportslink.data.EquipmentRepository;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Equipment;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Role;
import tqs.sportslink.data.model.Sport;
import tqs.sportslink.data.model.User;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String CITY_AVEIRO = "Aveiro";
    private static final String OWNER_EMAIL = "owner@sportslink.com";
    private static final String USER_EMAIL = "test@sportslink.com";
    private static final String MINIO_BASE_URL = "http://192.168.160.31:9000/sportslink-images/imagensMinIO/";
    private static final String TIME_EXAMPLE_1 = "08:00";
    private static final String TIME_EXAMPLE_2 = "22:00";
    private static final String TIME_EXAMPLE_3 = "09:00";
    private static final String TIME_EXAMPLE_4 = "23:00";
    private static final String PADEL_JPEG = "padel_pitch.jpeg";
    private static final String CONFIRMED = "CONFIRMED";
    private static final String COMPLETED = "COMPLETED";
    private static final String ACCESSORY = "Accessory";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();


    // 🔐 Passwords via environment variables (defaults keep behavior unchanged)
    private static final String ADMIN_PASSWORD =
            System.getenv().getOrDefault("SPORTSLINK_ADMIN_PASSWORD", "pwdAdmin");

    private static final String DEFAULT_PASSWORD =
            System.getenv().getOrDefault("SPORTSLINK_DEFAULT_PASSWORD", "password123");

    @Bean
    CommandLineRunner initDatabase(
            FacilityRepository facilityRepository,
            EquipmentRepository equipmentRepository,
            UserRepository userRepository,
            tqs.sportslink.data.RentalRepository rentalRepository) {

        return args -> {
            var passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

            createAdminIfMissing(userRepository, passwordEncoder);

            long count = logAndGetFacilityCount(facilityRepository);

            if (count > 0) {
                logger.info("Database already has {} facilities. Skipping facility creation.", count);
                return;
            }

            logger.info("Initializing sample data...");

            createOwnerAndTestUserIfMissing(userRepository, passwordEncoder);

            List<Facility> facilities = createFacilities(facilityRepository, userRepository);

            logger.info("Created {} facilities total", facilities.size());

            populateEquipmentForFacilities(facilities, equipmentRepository);

            assignOwnerToFacilities(userRepository, facilities);

            facilityRepository.saveAll(facilities);

            seedRentals(userRepository, facilityRepository, rentalRepository);

            logSeedSummary(facilities);
        };
    }

    private void createAdminIfMissing(UserRepository userRepository,
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder) {

        if (!userRepository.existsByEmail("admin@admin.com")) {
            User adminUser = new User();
            adminUser.setEmail("admin@admin.com");
            adminUser.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
            adminUser.setName("Admin User");
            adminUser.getRoles().add(Role.ADMIN);
            adminUser.setActive(true);
            userRepository.save(adminUser);

            // 🔒 Do NOT log passwords
            logger.info("Admin user created: admin@admin.com");
        } else {
            logger.info("Admin user already exists");
        }
    }

    private long logAndGetFacilityCount(FacilityRepository facilityRepository) {
        long count = facilityRepository.count();
        logger.info("Current facility count: {}", count);
        return count;
    }

    private void createOwnerAndTestUserIfMissing(
            UserRepository userRepository,
            org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder) {

        if (userRepository.findByEmail(OWNER_EMAIL).isEmpty()) {
            User ownerUser = new User();
            ownerUser.setEmail(OWNER_EMAIL);
            ownerUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
            ownerUser.setName("Owner User");
            ownerUser.setPhone("911111111");
            ownerUser.getRoles().add(Role.OWNER);
            ownerUser.getRoles().add(Role.RENTER);
            ownerUser.setActive(true);
            userRepository.save(ownerUser);

            logger.info("Owner user created: {} (id={})", OWNER_EMAIL, ownerUser.getId());
        }

        if (userRepository.findByEmail(USER_EMAIL).isEmpty()) {
            User testUser = new User();
            testUser.setEmail(USER_EMAIL);
            testUser.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
            testUser.setName("Test User");
            testUser.setPhone("912345678");
            testUser.getRoles().add(Role.RENTER);
            testUser.setActive(true);
            testUser.setLatitude(40.6443);
            testUser.setLongitude(-8.6455);
            userRepository.save(testUser);

            logger.info("Test user created: {} (id={})", USER_EMAIL, testUser.getId());
        }
    }



    private List<Facility> createFacilities(FacilityRepository facilityRepository, UserRepository userRepository) {
        Facility facility1 = new Facility();
        facility1.setName("Campo de Futebol da Universidade de Aveiro");
        facility1.setSports(List.of(Sport.FOOTBALL));
        facility1.setCity(CITY_AVEIRO);
        facility1.setAddress("Campus Universitário de Santiago, 3810-193 Aveiro");
        facility1.setDescription("Campo de futebol sintético com iluminação");
        facility1.setPricePerHour(25.0);
        facility1.setOpeningTime(LocalTime.parse(TIME_EXAMPLE_1));
        facility1.setClosingTime(LocalTime.parse(TIME_EXAMPLE_2));
        facility1.setStatus(STATUS_ACTIVE);
        facility1.setLatitude(40.6443);
        facility1.setLongitude(-8.6455);
        facility1.setRating(4.5);
        facility1.setImageUrl(MINIO_BASE_URL + "campo_Universidade_Aveiro.jpeg");
        facilityRepository.save(facility1);

        Facility facility2 = new Facility();
        facility2.setName("Padel Center Aveiro");
        facility2.setSports(List.of(Sport.PADEL));
        facility2.setCity(CITY_AVEIRO);
        facility2.setAddress("Rua do Clube dos Galitos, 3800-000 Aveiro");
        facility2.setDescription("Courts de padel cobertos e descobertos");
        facility2.setPricePerHour(15.0);
        facility2.setOpeningTime(LocalTime.parse(TIME_EXAMPLE_3));
        facility2.setClosingTime(LocalTime.parse(TIME_EXAMPLE_4));
        facility2.setStatus(STATUS_ACTIVE);
        facility2.setLatitude(40.6400);
        facility2.setLongitude(-8.6500);
        facility2.setRating(4.7);
        facility2.setImageUrl(MINIO_BASE_URL + PADEL_JPEG);
        facilityRepository.save(facility2);

        Facility facility3 = new Facility();
        facility3.setName("Tennis Club Aveiro");
        facility3.setSports(List.of(Sport.TENNIS));
        facility3.setCity(CITY_AVEIRO);
        facility3.setAddress("Av. Dr. Lourenço Peixinho, 3800-000 Aveiro");
        facility3.setDescription("Courts de ténis com piso rápido");
        facility3.setPricePerHour(20.0);
        facility3.setOpeningTime(LocalTime.parse(TIME_EXAMPLE_1));
        facility3.setClosingTime(LocalTime.parse("21:00"));
        facility3.setStatus(STATUS_ACTIVE);
        facility3.setLatitude(40.6410);
        facility3.setLongitude(-8.6470);
        facility3.setRating(4.3);
        facility3.setImageUrl(MINIO_BASE_URL + "tennis_pitch.jpeg");
        facilityRepository.save(facility3);

        Facility facility4 = new Facility();
        facility4.setName("Basketball Arena Lisboa");
        facility4.setSports(List.of(Sport.BASKETBALL));
        facility4.setCity("Lisboa");
        facility4.setAddress("Rua do Ouro, 1100-000 Lisboa");
        facility4.setDescription("Pavilhão coberto com bancadas");
        facility4.setPricePerHour(30.0);
        facility4.setOpeningTime(LocalTime.parse("10:00"));
        facility4.setClosingTime(LocalTime.parse(TIME_EXAMPLE_2));
        facility4.setStatus(STATUS_ACTIVE);
        facility4.setLatitude(38.7223);
        facility4.setLongitude(-9.1393);
        facility4.setRating(4.8);
        facility4.setImageUrl(MINIO_BASE_URL + "basktball_court.jpeg");
        facilityRepository.save(facility4);

        Facility facility5 = new Facility();
        facility5.setName("Volleyball Beach Porto");
        facility5.setSports(List.of(Sport.VOLLEYBALL));
        facility5.setCity("Porto");
        facility5.setAddress("Praia de Matosinhos, 4450-000 Porto");
        facility5.setDescription("Campo de voleibol de praia");
        facility5.setPricePerHour(12.0);
        facility5.setOpeningTime(LocalTime.parse(TIME_EXAMPLE_3));
        facility5.setClosingTime(LocalTime.parse("20:00"));
        facility5.setStatus(STATUS_ACTIVE);
        facility5.setLatitude(41.1579);
        facility5.setLongitude(-8.6291);
        facility5.setRating(4.2);
        facility5.setImageUrl(MINIO_BASE_URL + "beach_volyeball.jpg");
        facilityRepository.save(facility5);

        Facility facility6 = new Facility();
        facility6.setName("Complexo Desportivo Municipal Aveiro");
        facility6.setSports(List.of(Sport.FOOTBALL, Sport.BASKETBALL));
        facility6.setCity(CITY_AVEIRO);
        facility6.setAddress("Rua do Desporto, 3810-000 Aveiro");
        facility6.setDescription("Complexo multi-desportivo com campos cobertos");
        facility6.setPricePerHour(22.0);
        facility6.setOpeningTime(LocalTime.parse("07:00"));
        facility6.setClosingTime(LocalTime.parse(TIME_EXAMPLE_4));
        facility6.setStatus(STATUS_ACTIVE);
        facility6.setLatitude(40.6380);
        facility6.setLongitude(-8.6420);
        facility6.setRating(4.6);
        facility6.setOwner(userRepository.findByEmail(OWNER_EMAIL).get());
        facility6.setImageUrl(MINIO_BASE_URL + "football_pitch.png");
        facilityRepository.save(facility6);

        Facility facility7 = new Facility();
        facility7.setName("Padel Premium Lisboa");
        facility7.setSports(List.of(Sport.PADEL));
        facility7.setCity("Lisboa");
        facility7.setAddress("Av. da Liberdade, 1250-000 Lisboa");
        facility7.setDescription("Courts premium de padel com ar condicionado");
        facility7.setPricePerHour(28.0);
        facility7.setOpeningTime(LocalTime.parse(TIME_EXAMPLE_1));
        facility7.setClosingTime(LocalTime.parse("23:59"));
        facility7.setStatus(STATUS_ACTIVE);
        facility7.setLatitude(38.7250);
        facility7.setLongitude(-9.1450);
        facility7.setRating(4.9);
        facility7.setImageUrl(MINIO_BASE_URL + PADEL_JPEG);
        facilityRepository.save(facility7);

        Facility facility8 = new Facility();
        facility8.setName("Centro Ténis Porto");
        facility8.setSports(List.of(Sport.TENNIS));
        facility8.setCity("Porto");
        facility8.setAddress("Rua de Serralves, 4150-000 Porto");
        facility8.setDescription("Academia de ténis com courts profissionais");
        facility8.setPricePerHour(24.0);
        facility8.setOpeningTime(LocalTime.parse(TIME_EXAMPLE_1));
        facility8.setClosingTime(LocalTime.parse(TIME_EXAMPLE_2));
        facility8.setStatus(STATUS_ACTIVE);
        facility8.setLatitude(41.1600);
        facility8.setLongitude(-8.6350);
        facility8.setRating(4.4);
        facility8.setImageUrl(MINIO_BASE_URL + "tennis_pitch.jpeg");
        facilityRepository.save(facility8);

        Facility facility9 = new Facility();
        facility9.setName("Ginásio Desportivo Coimbra");
        facility9.setSports(List.of(Sport.BASKETBALL, Sport.VOLLEYBALL));
        facility9.setCity("Coimbra");
        facility9.setAddress("Praça da República, 3000-000 Coimbra");
        facility9.setDescription("Pavilhão desportivo universitário");
        facility9.setPricePerHour(18.0);
        facility9.setOpeningTime(LocalTime.parse(TIME_EXAMPLE_3));
        facility9.setClosingTime(LocalTime.parse("21:00"));
        facility9.setStatus(STATUS_ACTIVE);
        facility9.setLatitude(40.2033);
        facility9.setLongitude(-8.4103);
        facility9.setRating(4.1);
        facility9.setImageUrl(MINIO_BASE_URL + "basktball_court.jpeg");
        facilityRepository.save(facility9);

        Facility facility10 = new Facility();
        facility10.setName("Football Center Braga");
        facility10.setSports(List.of(Sport.FOOTBALL));
        facility10.setCity("Braga");
        facility10.setAddress("Av. da Liberdade, 4710-000 Braga");
        facility10.setDescription("Campo sintético de última geração");
        facility10.setPricePerHour(26.0);
        facility10.setOpeningTime(LocalTime.parse(TIME_EXAMPLE_1));
        facility10.setClosingTime(LocalTime.parse(TIME_EXAMPLE_4));
        facility10.setStatus(STATUS_ACTIVE);
        facility10.setLatitude(41.5454);
        facility10.setLongitude(-8.4265);
        facility10.setRating(4.7);
        facility10.setOwner(userRepository.findByEmail(OWNER_EMAIL).get());
        facility10.setImageUrl(MINIO_BASE_URL + "football_pitch.png");
        facilityRepository.save(facility10);

        Facility facility11 = new Facility();
        facility11.setName("Beach Volleyball Cascais");
        facility11.setSports(List.of(Sport.VOLLEYBALL));
        facility11.setCity("Cascais");
        facility11.setAddress("Praia de Carcavelos, 2775-000 Cascais");
        facility11.setDescription("Campos de vólei na praia");
        facility11.setPricePerHour(14.0);
        facility11.setOpeningTime(LocalTime.parse("10:00"));
        facility11.setClosingTime(LocalTime.parse("19:00"));
        facility11.setStatus(STATUS_ACTIVE);
        facility11.setLatitude(38.6833);
        facility11.setLongitude(-9.3500);
        facility11.setRating(4.3);
        facility11.setImageUrl(MINIO_BASE_URL + "beach_volyeball.jpg");
        facilityRepository.save(facility11);

        Facility facility12 = new Facility();
        facility12.setName("Padel & Tennis Club Aveiro");
        facility12.setSports(List.of(Sport.PADEL, Sport.TENNIS));
        facility12.setCity(CITY_AVEIRO);
        facility12.setAddress("Rua dos Desportos, 3800-100 Aveiro");
        facility12.setDescription("Clube com courts de padel e ténis");
        facility12.setPricePerHour(19.0);
        facility12.setOpeningTime(LocalTime.parse("07:30"));
        facility12.setClosingTime(LocalTime.parse("22:30"));
        facility12.setStatus(STATUS_ACTIVE);
        facility12.setLatitude(40.6500);
        facility12.setLongitude(-8.6400);
        facility12.setRating(4.5);
        facility12.setImageUrl(MINIO_BASE_URL + PADEL_JPEG);
        facilityRepository.save(facility12);

        return List.of(facility1, facility2, facility3, facility4, facility5,
                facility6, facility7, facility8, facility9, facility10, facility11, facility12);
    }

    private void populateEquipmentForFacilities(List<Facility> facilities, EquipmentRepository equipmentRepository) {
        for (Facility f : facilities) {
            createEquipmentForFacility(f, equipmentRepository);
        }
    }

    private void assignOwnerToFacilities(UserRepository userRepository, List<Facility> facilities) {
        User currentOwner = userRepository.findByEmail(OWNER_EMAIL).orElse(null);
        if (currentOwner == null) {
            return;
        }

        // Same set of facilities as your original code (1,2,3,4,5,10,6,7)
        setOwnerIfPresent(facilities, 0, currentOwner);  // facility1
        setOwnerIfPresent(facilities, 1, currentOwner);  // facility2
        setOwnerIfPresent(facilities, 2, currentOwner);  // facility3
        setOwnerIfPresent(facilities, 3, currentOwner);  // facility4
        setOwnerIfPresent(facilities, 4, currentOwner);  // facility5
        setOwnerIfPresent(facilities, 9, currentOwner);  // facility10
        setOwnerIfPresent(facilities, 5, currentOwner);  // facility6
        setOwnerIfPresent(facilities, 6, currentOwner);  // facility7
    }

    private void setOwnerIfPresent(List<Facility> facilities, int index, User owner) {
        if (index >= 0 && index < facilities.size()) {
            facilities.get(index).setOwner(owner);
        }
    }

    private void logSeedSummary(List<Facility> facilities) {
        logger.info("Database initialized with sample data!");
        logger.info("   - 1 owner user created");
        logger.info("   - 1 renter test user created");
        logger.info("   - {} facilities created", facilities.size());
        logger.info("   - Equipment population complete");
    }



    private void seedRentals(UserRepository userRepository, FacilityRepository facilityRepository,
            tqs.sportslink.data.RentalRepository rentalRepository) {

        User renter = userRepository.findByEmail(USER_EMAIL)
                .orElse(userRepository.findAll().stream().filter(u -> u.getRoles().contains(Role.RENTER)).findFirst()
                        .orElse(null));

        List<Facility> facilities = facilityRepository.findAll();
        if (renter == null || facilities.isEmpty())
            return;

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // Sample rentals
        createRental(renter, facilities.get(0), now.minusDays(5), now.minusDays(5).plusHours(1), COMPLETED, 25.0,
                rentalRepository);
        createRental(renter, facilities.get(1), now.minusDays(2), now.minusDays(2).plusHours(2), COMPLETED, 30.0,
                rentalRepository);
        createRental(renter, facilities.get(2), now.plusDays(1), now.plusDays(1).plusHours(1), CONFIRMED, 20.0,
                rentalRepository);
        createRental(renter, facilities.get(1), now.plusDays(2), now.plusDays(2).plusHours(1), "PENDING", 15.0,
                rentalRepository);
        createRental(renter, facilities.get(3), now.minusDays(1), now.minusDays(1).plusHours(2), "CANCELLED", 60.0,
                rentalRepository);
        createRental(renter, facilities.get(4), now.plusDays(5), now.plusDays(5).plusHours(3), CONFIRMED, 36.0,
                rentalRepository);
        createRental(renter, facilities.get(0), now.plusHours(2), now.plusHours(3), CONFIRMED, 25.0,
                rentalRepository);
        createRental(renter, facilities.get(5), now.minusDays(10), now.minusDays(10).plusHours(1), COMPLETED, 22.0,
                rentalRepository);

        // ===========================================================================================
        // MASS BOOKINGS FOR OWNER FACILITY (To test Suggestions/Analytics)
        // ===========================================================================================
        Facility ownerFacility = facilities.get(0); // Facility 1 (Aveiro Football)
        for (int i = 0; i < 50; i++) {
            // Spread bookings: some past, some future
            // Randomize days offset between -30 and +30
            long daysOffset = SECURE_RANDOM.nextLong(61) - 30; // range [-30, 30]
            int hour = 9 + SECURE_RANDOM.nextInt(10);           // range [9, 18]

            java.time.LocalDateTime start = now.plusDays(daysOffset).withHour(hour).withMinute(0).withSecond(0)
                    .withNano(0);
            java.time.LocalDateTime end = start.plusHours(1);

            String status = CONFIRMED;
            if (daysOffset < 0)
                status = COMPLETED;

            createRental(renter, ownerFacility, start, end, status, 25.0, rentalRepository);
        }
    }

    private void createRental(User user, Facility facility, java.time.LocalDateTime start, java.time.LocalDateTime end,
            String status, Double price, tqs.sportslink.data.RentalRepository repo) {
        tqs.sportslink.data.model.Rental r = new tqs.sportslink.data.model.Rental();
        r.setUser(user);
        r.setFacility(facility);
        r.setStartTime(start);
        r.setEndTime(end);
        r.setStatus(status);
        r.setTotalPrice(price);
        repo.save(r);
    }

    private void createEquipmentForFacility(Facility f, EquipmentRepository repo) {
        if (f.getSports() == null)
            return;

        for (Sport sport : f.getSports()) {
            // Create 2 items per sport
            createItem(f, sport, 1, repo);
            createItem(f, sport, 2, repo);
        }
    }

    private void createItem(Facility f, Sport s, int variant, EquipmentRepository repo) {
        Equipment e = new Equipment();
        e.setFacility(f);
        e.setSports(List.of(s));
        e.setStatus(STATUS_AVAILABLE);

        applyPreset(e, s, variant);

        repo.save(e);
    }

    private void applyPreset(Equipment e, Sport s, int variant) {
        if (variant == 1) {
            applyVariant1(e, s);
        } else {
            applyVariant2(e, s, variant);
        }
    }

    private void applyVariant1(Equipment e, Sport s) {
        switch (s) {
            case FOOTBALL -> setEquipment(e, "Bola de Futebol Oficial", "Ball",
                    "Bola de futebol tamanho 5 homologada.", 20, 3.0);
            case PADEL -> setEquipment(e, "Raquete de Padel Pro", "Racket",
                    "Raquete de carbono para nível intermédio/avançado.", 12, 5.0);
            case TENNIS -> setEquipment(e, "Raquete Wilson Tennis", "Racket",
                    "Raquete de ténis leve e resistente.", 10, 6.0);
            case BASKETBALL -> setEquipment(e, "Bola de Basquete NBA", "Ball",
                    "Bola de basquete oficial indoor/outdoor.", 15, 3.5);
            case VOLLEYBALL -> setEquipment(e, "Bola de Vólei Pro", "Ball",
                    "Bola de vólei soft touch.", 12, 3.0);
            case SWIMMING -> setEquipment(e, "Prancha de Natação", ACCESSORY,
                    "Prancha ergonómica para treino de pernas.", 25, 1.0);
            default -> setEquipment(e, "Equipamento Genérico 1", "Other",
                    "Equipamento desportivo diverso.", 5, 1.0);
        }
    }

    private void applyVariant2(Equipment e, Sport s, int variant) {
        switch (s) {
            case FOOTBALL -> setEquipment(e, "Coletes de Treino", "Vest",
                    "Conjunto de 10 coletes para equipas.", 10, 2.0);
            case PADEL -> setEquipment(e, "Pack Bolas Padel", "Ball",
                    "Tubo com 3 bolas de padel novas.", 30, 4.0);
            case TENNIS -> setEquipment(e, "Cesto de Bolas", "Ball",
                    "Cesto com 50 bolas para treino.", 5, 8.0);
            case BASKETBALL -> setEquipment(e, "Cone de Treino", ACCESSORY,
                    "Conjunto de cones para exercícios de drible.", 8, 1.5);
            case VOLLEYBALL -> setEquipment(e, "Joelheiras de Proteção", "Protection",
                    "Par de joelheiras profissionais.", 20, 2.5);
            case SWIMMING -> setEquipment(e, "Óculos de Natação", ACCESSORY,
                    "Óculos anti-embaciamento ajustáveis.", 15, 2.0);
            default -> setEquipment(e, "Equipamento Genérico " + variant, "Other",
                    "Equipamento desportivo diverso.", 5, 1.0);
        }
    }

    private void setEquipment(Equipment e, String name, String type, String description, int quantity, double pricePerHour) {
        e.setName(name);
        e.setType(type);
        e.setDescription(description);
        e.setQuantity(quantity);
        e.setPricePerHour(pricePerHour);
    }


}
