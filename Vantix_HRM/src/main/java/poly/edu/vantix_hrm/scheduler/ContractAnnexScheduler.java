package poly.edu.vantix_hrm.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import poly.edu.vantix_hrm.service.ContractAnnexService;

@Component
@RequiredArgsConstructor
public class ContractAnnexScheduler {

    private final ContractAnnexService contractAnnexService;

    // Chạy mỗi ngày vào lúc 00:05 (Sau khi ContractScheduler chạy dọn dẹp HĐ lúc 00:01)
    @Scheduled(cron = "0 5 0 * * ?")
    public void autoProcessAnnexes() {
        try {
            System.out.println("⏳ Đang tiến hành kiểm tra & kích hoạt tự động Phụ lục hợp đồng...");
            contractAnnexService.autoProcessScheduledAnnexes();
            System.out.println("✅ Auto-Processed Contract Annexes chạy thành công!");
        } catch (Exception e) {
            System.err.println("❌ Lỗi trong lúc chạy Auto-Process Contract Annexes: " + e.getMessage());
        }
    }
}