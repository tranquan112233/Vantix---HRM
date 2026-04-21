package poly.edu.vantix.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import poly.edu.vantix.dto.response.PageResponse;
import poly.edu.vantix.dto.response.SystemLogResponse;
import poly.edu.vantix.entity.enums.LogLevel;
import poly.edu.vantix.service.SystemLogService;
import poly.edu.vantix.util.PageableUtils;

import java.util.List;

@RestController
@RequestMapping("/api/system-logs")
@PreAuthorize("hasAuthority('SYSTEM_LOG_VIEW')")
public class SystemLogController {

    private final SystemLogService systemLogService;

    public SystemLogController(SystemLogService systemLogService) {
        this.systemLogService = systemLogService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<SystemLogResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LogLevel level,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(systemLogService.searchPage(
                keyword,
                level,
                module,
                PageableUtils.from(page, size)
        ));
    }

    @GetMapping("/modules")
    public ResponseEntity<List<String>> modules() {
        return ResponseEntity.ok(systemLogService.getModules());
    }
}
