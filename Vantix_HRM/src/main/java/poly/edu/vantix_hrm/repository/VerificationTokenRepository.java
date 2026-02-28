package poly.edu.vantix_hrm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import poly.edu.vantix_hrm.entity.VerificationToken;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken,Integer>{

    Optional<VerificationToken> findByTokenAndTokenTypeAndUsedFalse(
            String token,
            VerificationToken.TokenType tokenType
    );
}