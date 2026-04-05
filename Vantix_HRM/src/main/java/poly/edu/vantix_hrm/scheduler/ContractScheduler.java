package poly.edu.vantix_hrm.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import poly.edu.vantix_hrm.entity.Contract;
import poly.edu.vantix_hrm.repository.ContractsRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ContractScheduler {

    @Autowired
    private ContractsRepository contractsRepository;
    @Scheduled(cron = "0 1 0 * * ?")
    public void autoExpireContracts() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        List<Contract> expiredContracts = contractsRepository.findByStatusAndEndDateBefore(Contract.ContractStatus.ACTIVE, today);
        if (expiredContracts.isEmpty()) {
            return;
        }
        for (Contract contract : expiredContracts) {
            try {
                contract.setStatus(Contract.ContractStatus.EXPIRED);
                contractsRepository.save(contract);
                System.out.println("✅ Auto-Expired hợp đồng: " + contract.getContractId() + " của NV: " + contract.getEmployee().getId());
            } catch (Exception e) {
                System.err.println("❌ Lỗi Auto-Expired hợp đồng " + contract.getContractId() + ": " + e.getMessage());
            }
        }
    }
}