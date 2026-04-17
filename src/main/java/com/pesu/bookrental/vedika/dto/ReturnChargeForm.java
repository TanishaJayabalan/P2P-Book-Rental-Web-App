package com.pesu.bookrental.vedika.dto;

import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public class ReturnChargeForm {

    @DecimalMin(value = "0.0", inclusive = true, message = "Charges cannot be negative.")
    private BigDecimal extraCharges = BigDecimal.ZERO;

    public BigDecimal getExtraCharges() {
        return extraCharges;
    }

    public void setExtraCharges(BigDecimal extraCharges) {
        this.extraCharges = extraCharges;
    }
}
