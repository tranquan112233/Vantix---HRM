package poly.edu.vantix.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ContractExpirationJob {

    private static final Logger log = LoggerFactory.getLogger(ContractExpirationJob.class);

    private final ContractService contractService;

    public ContractExpirationJob(ContractService contractService) {
        this.contractService = contractService;
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void expireContracts() {
        int expired = contractService.expireElapsedActiveContracts();
        if (expired > 0) {
            log.info("Auto-expired {} contract(s)", expired);
        }
    }
}
