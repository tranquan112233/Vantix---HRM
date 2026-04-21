package poly.edu.vantix.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.vantix.dto.response.PageResponse;
import poly.edu.vantix.dto.response.SystemLogResponse;
import poly.edu.vantix.entity.enums.LogLevel;
import poly.edu.vantix.repository.SystemLogRepository;

import java.util.List;

@Service
public class SystemLogService {

    private final SystemLogRepository systemLogRepository;

    public SystemLogService(SystemLogRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<SystemLogResponse> searchPage(
            String keyword,
            LogLevel level,
            String module,
            Pageable pageable
    ) {
        String safeKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        String safeModule = module == null || module.isBlank() ? null : module.trim();
        return PageResponse.from(
                systemLogRepository.search(safeKeyword, level, safeModule, pageable),
                SystemLogResponse::fromEntity
        );
    }

    @Transactional(readOnly = true)
    public List<String> getModules() {
        return systemLogRepository.findModules();
    }
}
