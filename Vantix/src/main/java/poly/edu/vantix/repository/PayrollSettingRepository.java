package poly.edu.vantix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import poly.edu.vantix.entity.PayrollSetting;

import java.util.Optional;

public interface PayrollSettingRepository extends JpaRepository<PayrollSetting, Long> {

    Optional<PayrollSetting> findBySettingKey(String settingKey);
}
