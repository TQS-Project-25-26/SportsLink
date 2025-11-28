package tqs.sportslink.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tqs.sportslink.data.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Busca usuário por email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Verifica se email já existe
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca usuários por role
     */
    List<User> findByRole(String role);
    
    /**
     * Busca usuários ativos
     */
    List<User> findByActiveTrue();
    
    /**
     * Busca usuários por role e status ativo
     */
    List<User> findByRoleAndActiveTrue(String role);
}
