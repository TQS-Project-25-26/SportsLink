package tqs.sportslink.config;

import java.time.LocalTime;
import java.util.List;

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
    private static final String STATUS_UNAVAILABLE = "UNAVAILABLE";
    private static final String CITY_AVEIRO = "Aveiro";

    @Bean
    CommandLineRunner initDatabase(FacilityRepository facilityRepository, EquipmentRepository equipmentRepository, UserRepository userRepository) {
        return args -> {
            // Check if data already exists
            long count = facilityRepository.count();
            logger.info("Current facility count: {}", count);
            
            if (count > 0) {
                logger.info("Database already has {} facilities", count);
                // List facilities for debugging
                facilityRepository.findAll().forEach(f -> 
                    logger.info("  - {} ({} in {})", f.getName(), f.getSports(), f.getCity())
                );
                return; // Data already initialized
            }
            
            logger.info("Initializing sample data...");

            // Create test user for functional tests
            User testUser = new User();
            testUser.setEmail("test@sportslink.com");
            testUser.setPassword("password123"); // Plain text password for testing
            testUser.setName("Test User");
            testUser.setPhone("912345678");
            testUser.setRole(Role.RENTER);
            testUser.setActive(true);
            userRepository.save(testUser);
            logger.info("Test user created: test@sportslink.com / password123");

            // Create sample facilities
            Facility facility1 = new Facility();
            facility1.setName("Campo de Futebol da Universidade de Aveiro");
            facility1.setSports(List.of(Sport.FOOTBALL));
            facility1.setCity(CITY_AVEIRO);
            facility1.setAddress("Campus Universitário de Santiago, 3810-193 Aveiro");
            facility1.setDescription("Campo de futebol sintético com iluminação");
            facility1.setPricePerHour(25.0);
            facility1.setOpeningTime(LocalTime.parse("08:00"));
            facility1.setClosingTime(LocalTime.parse("22:00"));
            facility1.setStatus(STATUS_ACTIVE);
            facilityRepository.save(facility1);

            Facility facility2 = new Facility();
            facility2.setName("Padel Center Aveiro");
            facility2.setSports(List.of(Sport.PADEL));
            facility2.setCity(CITY_AVEIRO);
            facility2.setAddress("Rua do Clube dos Galitos, 3800-000 Aveiro");
            facility2.setDescription("Courts de padel cobertos e descobertos");
            facility2.setPricePerHour(15.0);
            facility2.setOpeningTime(LocalTime.parse("09:00"));
            facility2.setClosingTime(LocalTime.parse("23:00"));
            facility2.setStatus(STATUS_ACTIVE);
            facilityRepository.save(facility2);

            Facility facility3 = new Facility();
            facility3.setName("Tennis Club Aveiro");
            facility3.setSports(List.of(Sport.TENNIS));
            facility3.setCity(CITY_AVEIRO);
            facility3.setAddress("Av. Dr. Lourenço Peixinho, 3800-000 Aveiro");
            facility3.setDescription("Courts de ténis com piso rápido");
            facility3.setPricePerHour(20.0);
            facility3.setOpeningTime(LocalTime.parse("08:00"));
            facility3.setClosingTime(LocalTime.parse("21:00"));
            facility3.setStatus(STATUS_ACTIVE);
            facilityRepository.save(facility3);

            Facility facility4 = new Facility();
            facility4.setName("Basketball Arena Lisboa");
            facility4.setSports(List.of(Sport.BASKETBALL));
            facility4.setCity("Lisboa");
            facility4.setAddress("Rua do Ouro, 1100-000 Lisboa");
            facility4.setDescription("Pavilhão coberto com bancadas");
            facility4.setPricePerHour(30.0);
            facility4.setOpeningTime(LocalTime.parse("10:00"));
            facility4.setClosingTime(LocalTime.parse("22:00"));
            facility4.setStatus(STATUS_ACTIVE);
            facilityRepository.save(facility4);

            Facility facility5 = new Facility();
            facility5.setName("Volleyball Beach Porto");
            facility5.setSports(List.of(Sport.VOLLEYBALL));
            facility5.setCity("Porto");
            facility5.setAddress("Praia de Matosinhos, 4450-000 Porto");
            facility5.setDescription("Campo de voleibol de praia");
            facility5.setPricePerHour(12.0);
            facility5.setOpeningTime(LocalTime.parse("09:00"));
            facility5.setClosingTime(LocalTime.parse("20:00"));
            facility5.setStatus(STATUS_ACTIVE);
            facilityRepository.save(facility5);

            // Create sample equipment for facility1 (Football)
            Equipment equipment1 = new Equipment();
            equipment1.setName("Bola de Futebol");
            equipment1.setType("Ball");
            equipment1.setDescription("Bola oficial de futebol tamanho 5");
            equipment1.setFacility(facility1);
            equipment1.setQuantity(10);
            equipment1.setPricePerHour(2.0);
            equipment1.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment1);

            Equipment equipment2 = new Equipment();
            equipment2.setName("Coletes de Treino");
            equipment2.setType("Vest");
            equipment2.setDescription("Coletes coloridos para distinguir equipas");
            equipment2.setFacility(facility1);
            equipment2.setQuantity(20);
            equipment2.setPricePerHour(1.0);
            equipment2.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment2);

            // Create sample equipment for facility2 (Padel)
            Equipment equipment3 = new Equipment();
            equipment3.setName("Raquete de Padel");
            equipment3.setType("Racket");
            equipment3.setDescription("Raquete profissional de padel");
            equipment3.setFacility(facility2);
            equipment3.setQuantity(8);
            equipment3.setPricePerHour(5.0);
            equipment3.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment3);

            Equipment equipment4 = new Equipment();
            equipment4.setName("Bolas de Padel");
            equipment4.setType("Ball");
            equipment4.setDescription("Pack de 3 bolas de padel");
            equipment4.setFacility(facility2);
            equipment4.setQuantity(15);
            equipment4.setPricePerHour(3.0);
            equipment4.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment4);

            // Create sample equipment for facility3 (Tennis)
            Equipment equipment6 = new Equipment();
            equipment6.setName("Bola de Ténis");
            equipment6.setType("Ball");
            equipment6.setDescription("Bola oficial de ténis Wilson");
            equipment6.setFacility(facility3);
            equipment6.setQuantity(20);
            equipment6.setPricePerHour(2.0);
            equipment6.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment6);

            Equipment equipment7 = new Equipment();
            equipment7.setName("Raquete de Ténis");
            equipment7.setType("Racket");
            equipment7.setDescription("Raquete profissional HEAD");
            equipment7.setFacility(facility3);
            equipment7.setQuantity(5);
            equipment7.setPricePerHour(6.0);
            equipment7.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment7);

            // Create sample equipment for facility4 (Basketball)
            Equipment equipment8 = new Equipment();
            equipment8.setName("Bola de Basquete");
            equipment8.setType("Ball");
            equipment8.setDescription("Bola oficial Spalding NBA");
            equipment8.setFacility(facility4);
            equipment8.setQuantity(12);
            equipment8.setPricePerHour(3.0);
            equipment8.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment8);

            Equipment equipment9 = new Equipment();
            equipment9.setName("Colete de Treino");
            equipment9.setType("Vest");
            equipment9.setDescription("Coletes reversíveis para treino");
            equipment9.setFacility(facility4);
            equipment9.setQuantity(24);
            equipment9.setPricePerHour(1.5);
            equipment9.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment9);

            // Create more equipment for variety
            Equipment equipment5 = new Equipment();
            equipment5.setName("Rede de Vólei");
            equipment5.setType("Net");
            equipment5.setDescription("Rede profissional de vólei");
            equipment5.setFacility(facility5);
            equipment5.setQuantity(2);
            equipment5.setPricePerHour(8.0);
            equipment5.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment5);

            Equipment equipment10 = new Equipment();
            equipment10.setName("Protetor de Joelho");
            equipment10.setType("Protection");
            equipment10.setDescription("Protetor de joelho para vólei");
            equipment10.setFacility(facility5);
            equipment10.setQuantity(0);
            equipment10.setPricePerHour(3.0);
            equipment10.setStatus(STATUS_UNAVAILABLE);
            equipmentRepository.save(equipment10);

            logger.info("Database initialized with sample data!");
            logger.info("   - 1 test user created");
            logger.info("   - 5 facilities created");
            logger.info("   - 10 equipments created");
        };
    }
}
