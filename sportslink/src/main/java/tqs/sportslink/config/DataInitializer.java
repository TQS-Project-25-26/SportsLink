package tqs.sportslink.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tqs.sportslink.data.FacilityRepository;
import tqs.sportslink.data.EquipmentRepository;
import tqs.sportslink.data.model.Facility;
import tqs.sportslink.data.model.Equipment;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(FacilityRepository facilityRepository, EquipmentRepository equipmentRepository) {
        return args -> {
            // Check if data already exists
            long count = facilityRepository.count();
            System.out.println("Current facility count: " + count);
            
            if (count > 0) {
                System.out.println("Database already has " + count + " facilities");
                // List facilities for debugging
                facilityRepository.findAll().forEach(f -> 
                    System.out.println("  - " + f.getName() + " (" + f.getSportType() + " in " + f.getCity() + ")")
                );
                return; // Data already initialized
            }
            
            System.out.println("Initializing sample data...");

            // Create sample facilities
            Facility facility1 = new Facility();
            facility1.setName("Campo de Futebol da Universidade de Aveiro");
            facility1.setSportType("Football");
            facility1.setCity("Aveiro");
            facility1.setAddress("Campus Universitário de Santiago, 3810-193 Aveiro");
            facility1.setDescription("Campo de futebol sintético com iluminação");
            facility1.setPricePerHour(25.0);
            facility1.setRating(4.5);
            facility1.setOpeningTime("08:00");
            facility1.setClosingTime("22:00");
            facility1.setStatus("ACTIVE");
            facilityRepository.save(facility1);

            Facility facility2 = new Facility();
            facility2.setName("Padel Center Aveiro");
            facility2.setSportType("Padel");
            facility2.setCity("Aveiro");
            facility2.setAddress("Rua do Clube dos Galitos, 3800-000 Aveiro");
            facility2.setDescription("Courts de padel cobertos e descobertos");
            facility2.setPricePerHour(15.0);
            facility2.setRating(4.8);
            facility2.setOpeningTime("09:00");
            facility2.setClosingTime("23:00");
            facility2.setStatus("ACTIVE");
            facilityRepository.save(facility2);

            Facility facility3 = new Facility();
            facility3.setName("Tennis Club Aveiro");
            facility3.setSportType("Tennis");
            facility3.setCity("Aveiro");
            facility3.setAddress("Av. Dr. Lourenço Peixinho, 3800-000 Aveiro");
            facility3.setDescription("Courts de ténis com piso rápido");
            facility3.setPricePerHour(20.0);
            facility3.setRating(4.2);
            facility3.setOpeningTime("08:00");
            facility3.setClosingTime("21:00");
            facility3.setStatus("ACTIVE");
            facilityRepository.save(facility3);

            Facility facility4 = new Facility();
            facility4.setName("Basketball Arena Lisboa");
            facility4.setSportType("Basketball");
            facility4.setCity("Lisboa");
            facility4.setAddress("Rua do Ouro, 1100-000 Lisboa");
            facility4.setDescription("Pavilhão coberto com bancadas");
            facility4.setPricePerHour(30.0);
            facility4.setRating(4.7);
            facility4.setOpeningTime("10:00");
            facility4.setClosingTime("22:00");
            facility4.setStatus("ACTIVE");
            facilityRepository.save(facility4);

            Facility facility5 = new Facility();
            facility5.setName("Volleyball Beach Porto");
            facility5.setSportType("Volleyball");
            facility5.setCity("Porto");
            facility5.setAddress("Praia de Matosinhos, 4450-000 Porto");
            facility5.setDescription("Campo de voleibol de praia");
            facility5.setPricePerHour(12.0);
            facility5.setRating(4.6);
            facility5.setOpeningTime("09:00");
            facility5.setClosingTime("20:00");
            facility5.setStatus("ACTIVE");
            facilityRepository.save(facility5);

            // Create sample equipment for facility1 (Football)
            Equipment equipment1 = new Equipment();
            equipment1.setName("Bola de Futebol");
            equipment1.setType("Ball");
            equipment1.setDescription("Bola oficial de futebol tamanho 5");
            equipment1.setFacility(facility1);
            equipment1.setQuantity(10);
            equipment1.setPricePerHour(2.0);
            equipment1.setStatus("AVAILABLE");
            equipmentRepository.save(equipment1);

            Equipment equipment2 = new Equipment();
            equipment2.setName("Coletes de Treino");
            equipment2.setType("Vest");
            equipment2.setDescription("Coletes coloridos para distinguir equipas");
            equipment2.setFacility(facility1);
            equipment2.setQuantity(20);
            equipment2.setPricePerHour(1.0);
            equipment2.setStatus("AVAILABLE");
            equipmentRepository.save(equipment2);

            // Create sample equipment for facility2 (Padel)
            Equipment equipment3 = new Equipment();
            equipment3.setName("Raquete de Padel");
            equipment3.setType("Racket");
            equipment3.setDescription("Raquete profissional de padel");
            equipment3.setFacility(facility2);
            equipment3.setQuantity(8);
            equipment3.setPricePerHour(5.0);
            equipment3.setStatus("AVAILABLE");
            equipmentRepository.save(equipment3);

            Equipment equipment4 = new Equipment();
            equipment4.setName("Bolas de Padel");
            equipment4.setType("Ball");
            equipment4.setDescription("Pack de 3 bolas de padel");
            equipment4.setFacility(facility2);
            equipment4.setQuantity(15);
            equipment4.setPricePerHour(3.0);
            equipment4.setStatus("AVAILABLE");
            equipmentRepository.save(equipment4);

            System.out.println("Database initialized with sample data!");
        };
    }
}
