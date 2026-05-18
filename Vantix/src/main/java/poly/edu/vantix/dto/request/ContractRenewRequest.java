package poly.edu.vantix.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ContractRenewRequest {

    @NotNull(message = "New end date is required")
    private LocalDate newEndDate;

    public LocalDate getNewEndDate() {
        return newEndDate;
    }

    public void setNewEndDate(LocalDate newEndDate) {
        this.newEndDate = newEndDate;
    }
}
