package tqs.sportslink.config;

import java.time.LocalTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.EquipmentRepository;
import tqs.sportslink.data.UserRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Equipment;
import tqs.sportslink.data.model.Sport;
import tqs.sportslink.data.model.User;
import tqs.sportslink.data.model.Role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            // Create test users
            User testUser = new User();
            testUser.setEmail("test@sportslink.com");
            testUser.setPassword("password123");
            testUser.setName("Test User");
            testUser.setPhone("912345678");
            testUser.setRole(Role.RENTER);
            testUser.setActive(true);
            testUser.setLatitude(40.6443);
            testUser.setLongitude(-8.6455);
            userRepository.save(testUser);

            User owner1 = new User();
            owner1.setEmail("owner@sportslink.com");
            owner1.setPassword("owner123");
            owner1.setName("João Silva");
            owner1.setPhone("913456789");
            owner1.setRole(Role.OWNER);
            owner1.setActive(true);
            owner1.setLatitude(40.6443);
            owner1.setLongitude(-8.6455);
            userRepository.save(owner1);

            User renter1 = new User();
            renter1.setEmail("maria@example.com");
            renter1.setPassword("maria123");
            renter1.setName("Maria Santos");
            renter1.setPhone("914567890");
            renter1.setRole(Role.RENTER);
            renter1.setActive(true);
            renter1.setLatitude(40.6400);
            renter1.setLongitude(-8.6500);
            userRepository.save(renter1);

            User renter2 = new User();
            renter2.setEmail("pedro@example.com");
            renter2.setPassword("pedro123");
            renter2.setName("Pedro Costa");
            renter2.setPhone("915678901");
            renter2.setRole(Role.RENTER);
            renter2.setActive(true);
            renter2.setLatitude(38.7223);
            renter2.setLongitude(-9.1393);
            userRepository.save(renter2);

            logger.info("Test users created");

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
            facility1.setLatitude(40.6443);
            facility1.setLongitude(-8.6455);
            facility1.setRating(4.5);
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
            facility2.setLatitude(40.6400);
            facility2.setLongitude(-8.6500);
            facility2.setRating(4.7);
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
            facility3.setLatitude(40.6410);
            facility3.setLongitude(-8.6470);
            facility3.setRating(4.3);
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
            facility4.setLatitude(38.7223);
            facility4.setLongitude(-9.1393);
            facility4.setRating(4.8);
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
            facility5.setLatitude(41.1579);
            facility5.setLongitude(-8.6291);
            facility5.setRating(4.2);
            facilityRepository.save(facility5);

            // Additional facilities for better variety
            Facility facility6 = new Facility();
            facility6.setName("Complexo Desportivo Municipal Aveiro");
            facility6.setSports(List.of(Sport.FOOTBALL, Sport.BASKETBALL));
            facility6.setCity(CITY_AVEIRO);
            facility6.setAddress("Rua do Desporto, 3810-000 Aveiro");
            facility6.setDescription("Complexo multi-desportivo com campos cobertos");
            facility6.setPricePerHour(22.0);
            facility6.setOpeningTime(LocalTime.parse("07:00"));
            facility6.setClosingTime(LocalTime.parse("23:00"));
            facility6.setStatus(STATUS_ACTIVE);
            facility6.setLatitude(40.6380);
            facility6.setLongitude(-8.6420);
            facility6.setRating(4.6);
            facility6.setOwner(owner1);
            facilityRepository.save(facility6);

            Facility facility7 = new Facility();
            facility7.setName("Padel Premium Lisboa");
            facility7.setSports(List.of(Sport.PADEL));
            facility7.setCity("Lisboa");
            facility7.setAddress("Av. da Liberdade, 1250-000 Lisboa");
            facility7.setDescription("Courts premium de padel com ar condicionado");
            facility7.setPricePerHour(28.0);
            facility7.setOpeningTime(LocalTime.parse("08:00"));
            facility7.setClosingTime(LocalTime.parse("00:00"));
            facility7.setStatus(STATUS_ACTIVE);
            facility7.setLatitude(38.7250);
            facility7.setLongitude(-9.1450);
            facility7.setRating(4.9);
            facilityRepository.save(facility7);

            Facility facility8 = new Facility();
            facility8.setName("Centro Ténis Porto");
            facility8.setSports(List.of(Sport.TENNIS));
            facility8.setCity("Porto");
            facility8.setAddress("Rua de Serralves, 4150-000 Porto");
            facility8.setDescription("Academia de ténis com courts profissionais");
            facility8.setPricePerHour(24.0);
            facility8.setOpeningTime(LocalTime.parse("08:00"));
            facility8.setClosingTime(LocalTime.parse("22:00"));
            facility8.setStatus(STATUS_ACTIVE);
            facility8.setLatitude(41.1600);
            facility8.setLongitude(-8.6350);
            facility8.setRating(4.4);
            facilityRepository.save(facility8);

            Facility facility9 = new Facility();
            facility9.setName("Ginásio Desportivo Coimbra");
            facility9.setSports(List.of(Sport.BASKETBALL, Sport.VOLLEYBALL));
            facility9.setCity("Coimbra");
            facility9.setAddress("Praça da República, 3000-000 Coimbra");
            facility9.setDescription("Pavilhão desportivo universitário");
            facility9.setPricePerHour(18.0);
            facility9.setOpeningTime(LocalTime.parse("09:00"));
            facility9.setClosingTime(LocalTime.parse("21:00"));
            facility9.setStatus(STATUS_ACTIVE);
            facility9.setLatitude(40.2033);
            facility9.setLongitude(-8.4103);
            facility9.setRating(4.1);
            facilityRepository.save(facility9);

            Facility facility10 = new Facility();
            facility10.setName("Football Center Braga");
            facility10.setSports(List.of(Sport.FOOTBALL));
            facility10.setCity("Braga");
            facility10.setAddress("Av. da Liberdade, 4710-000 Braga");
            facility10.setDescription("Campo sintético de última geração");
            facility10.setPricePerHour(26.0);
            facility10.setOpeningTime(LocalTime.parse("08:00"));
            facility10.setClosingTime(LocalTime.parse("23:00"));
            facility10.setStatus(STATUS_ACTIVE);
            facility10.setLatitude(41.5454);
            facility10.setLongitude(-8.4265);
            facility10.setRating(4.7);
            facility10.setOwner(owner1);
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
            facilityRepository.save(facility12);

            logger.info("Created 12 facilities total");

            // Create sample equipment for facility1 (Football)
            Equipment equipment1 = new Equipment();
            equipment1.setName("Bola de Futebol");
            equipment1.setType("Ball");
            equipment1.setDescription("Bola oficial de futebol tamanho 5");
            equipment1.setFacility(facility1);
            equipment1.setSports(List.of(Sport.FOOTBALL));
            equipment1.setQuantity(10);
            equipment1.setPricePerHour(2.0);
            equipment1.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment1);

            Equipment equipment2 = new Equipment();
            equipment2.setName("Coletes de Treino");
            equipment2.setType("Vest");
            equipment2.setDescription("Coletes coloridos para distinguir equipas");
            equipment2.setFacility(facility1);
            equipment2.setSports(List.of(Sport.FOOTBALL, Sport.BASKETBALL));
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
            equipment3.setSports(List.of(Sport.PADEL));
            equipment3.setQuantity(8);
            equipment3.setPricePerHour(5.0);
            equipment3.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment3);

            Equipment equipment4 = new Equipment();
            equipment4.setName("Bolas de Padel");
            equipment4.setType("Ball");
            equipment4.setDescription("Pack de 3 bolas de padel");
            equipment4.setFacility(facility2);
            equipment4.setSports(List.of(Sport.PADEL));
            equipment4.setQuantity(15);
            equipment4.setPricePerHour(3.0);
            equipment4.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment4);

            // Create more equipment for variety
            Equipment equipment5 = new Equipment();
            equipment5.setName("Rede de Vólei");
            equipment5.setType("Net");
            equipment5.setDescription("Rede profissional de vólei");
            equipment5.setFacility(facility5);
            equipment5.setSports(List.of(Sport.VOLLEYBALL));
            equipment5.setQuantity(2);
            equipment5.setPricePerHour(8.0);
            equipment5.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment5);

            Equipment equipment6 = new Equipment();
            equipment6.setName("Bola de Ténis");
            equipment6.setType("Ball");
            equipment6.setDescription("Bola oficial de ténis Wilson");
            equipment6.setFacility(facility3);
            equipment6.setSports(List.of(Sport.TENNIS));
            equipment6.setQuantity(20);
            equipment6.setPricePerHour(2.0);
            equipment6.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment6);

            Equipment equipment7 = new Equipment();
            equipment7.setName("Raquete de Ténis");
            equipment7.setType("Racket");
            equipment7.setDescription("Raquete profissional HEAD");
            equipment7.setFacility(facility3);
            equipment7.setSports(List.of(Sport.TENNIS));
            equipment7.setQuantity(5);
            equipment7.setPricePerHour(6.0);
            equipment7.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment7);

            Equipment equipment8 = new Equipment();
            equipment8.setName("Protetor de Joelho");
            equipment8.setType("Protection");
            equipment8.setDescription("Protetor de joelho para vólei");
            equipment8.setFacility(facility5);
            equipment8.setSports(List.of(Sport.VOLLEYBALL));
            equipment8.setQuantity(0);
            equipment8.setPricePerHour(3.0);
            equipment8.setStatus(STATUS_UNAVAILABLE);
            equipmentRepository.save(equipment8);

            // More equipment for facility6 (Football + Basketball)
            Equipment equipment9 = new Equipment();
            equipment9.setName("Bola de Basquete");
            equipment9.setType("Ball");
            equipment9.setDescription("Bola oficial NBA Spalding");
            equipment9.setFacility(facility6);
            equipment9.setSports(List.of(Sport.BASKETBALL));
            equipment9.setQuantity(8);
            equipment9.setPricePerHour(2.5);
            equipment9.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment9);

            Equipment equipment10 = new Equipment();
            equipment10.setName("Tabela de Basquete");
            equipment10.setType("Hoop");
            equipment10.setDescription("Tabela ajustável de basquete");
            equipment10.setFacility(facility6);
            equipment10.setSports(List.of(Sport.BASKETBALL));
            equipment10.setQuantity(2);
            equipment10.setPricePerHour(10.0);
            equipment10.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment10);

            // Equipment for facility7 (Premium Padel)
            Equipment equipment11 = new Equipment();
            equipment11.setName("Raquete Padel Premium");
            equipment11.setType("Racket");
            equipment11.setDescription("Raquete profissional Bullpadel");
            equipment11.setFacility(facility7);
            equipment11.setSports(List.of(Sport.PADEL));
            equipment11.setQuantity(12);
            equipment11.setPricePerHour(7.0);
            equipment11.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment11);

            Equipment equipment12 = new Equipment();
            equipment12.setName("Bolas Padel Premium");
            equipment12.setType("Ball");
            equipment12.setDescription("Pack de 3 bolas Head Padel Pro");
            equipment12.setFacility(facility7);
            equipment12.setSports(List.of(Sport.PADEL));
            equipment12.setQuantity(20);
            equipment12.setPricePerHour(4.0);
            equipment12.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment12);

            // Equipment for facility8 (Tennis Porto)
            Equipment equipment13 = new Equipment();
            equipment13.setName("Raquete Ténis Wilson");
            equipment13.setType("Racket");
            equipment13.setDescription("Raquete profissional Wilson Pro Staff");
            equipment13.setFacility(facility8);
            equipment13.setSports(List.of(Sport.TENNIS));
            equipment13.setQuantity(10);
            equipment13.setPricePerHour(6.5);
            equipment13.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment13);

            // Equipment for facility10 (Football Braga)
            Equipment equipment14 = new Equipment();
            equipment14.setName("Bola Nike Strike");
            equipment14.setType("Ball");
            equipment14.setDescription("Bola de futebol Nike Strike Team");
            equipment14.setFacility(facility10);
            equipment14.setSports(List.of(Sport.FOOTBALL));
            equipment14.setQuantity(15);
            equipment14.setPricePerHour(2.5);
            equipment14.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment14);

            Equipment equipment15 = new Equipment();
            equipment15.setName("Coletes Bicolores");
            equipment15.setType("Vest");
            equipment15.setDescription("Set de coletes reversíveis");
            equipment15.setFacility(facility10);
            equipment15.setSports(List.of(Sport.FOOTBALL, Sport.BASKETBALL));
            equipment15.setQuantity(30);
            equipment15.setPricePerHour(1.5);
            equipment15.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment15);

            Equipment equipment16 = new Equipment();
            equipment16.setName("Baliza Portátil");
            equipment16.setType("Goal");
            equipment16.setDescription("Baliza desmontável de futebol");
            equipment16.setFacility(facility10);
            equipment16.setSports(List.of(Sport.FOOTBALL));
            equipment16.setQuantity(4);
            equipment16.setPricePerHour(8.0);
            equipment16.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment16);

            // Equipment for facility12 (Padel & Tennis)
            Equipment equipment17 = new Equipment();
            equipment17.setName("Raquete Ténis Babolat");
            equipment17.setType("Racket");
            equipment17.setDescription("Raquete Babolat Pure Drive");
            equipment17.setFacility(facility12);
            equipment17.setSports(List.of(Sport.TENNIS));
            equipment17.setQuantity(6);
            equipment17.setPricePerHour(5.5);
            equipment17.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment17);

            Equipment equipment18 = new Equipment();
            equipment18.setName("Raquete Padel Nox");
            equipment18.setType("Racket");
            equipment18.setDescription("Raquete Nox ML10 Pro Cup");
            equipment18.setFacility(facility12);
            equipment18.setSports(List.of(Sport.PADEL));
            equipment18.setQuantity(8);
            equipment18.setPricePerHour(6.0);
            equipment18.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment18);

            Equipment equipment19 = new Equipment();
            equipment19.setName("Bolas Multiplas");
            equipment19.setType("Ball");
            equipment19.setDescription("Pack misto ténis e padel");
            equipment19.setFacility(facility12);
            equipment19.setSports(List.of(Sport.TENNIS, Sport.PADEL));
            equipment19.setQuantity(25);
            equipment19.setPricePerHour(2.0);
            equipment19.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment19);

            // Equipment for volleyball facilities
            Equipment equipment20 = new Equipment();
            equipment20.setName("Bola Mikasa Volleyball");
            equipment20.setType("Ball");
            equipment20.setDescription("Bola oficial de vólei Mikasa");
            equipment20.setFacility(facility9);
            equipment20.setSports(List.of(Sport.VOLLEYBALL));
            equipment20.setQuantity(12);
            equipment20.setPricePerHour(2.5);
            equipment20.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment20);

            Equipment equipment21 = new Equipment();
            equipment21.setName("Joelheiras Vólei");
            equipment21.setType("Knee pads");
            equipment21.setDescription("Proteção para joelhos Mizuno");
            equipment21.setFacility(facility9);
            equipment21.setSports(List.of(Sport.VOLLEYBALL));
            equipment21.setQuantity(16);
            equipment21.setPricePerHour(3.0);
            equipment21.setStatus(STATUS_AVAILABLE);
            equipmentRepository.save(equipment21);

            logger.info("Database initialized with sample data!");
            logger.info("   - 4 users created (1 test, 1 owner, 2 renters)");
            logger.info("   - 12 facilities created");
            logger.info("   - 21 equipment items created");
        };
    }
}
