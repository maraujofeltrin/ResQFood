package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class PickupCodeForm {

    @NotBlank(message = "{commerce.verifyPickup.error.empty}")
    @Size(min = 5, max = 5, message = "{commerce.verifyPickup.validation.code.size}")
    @Pattern(regexp = "^[A-Za-z0-9]{5}$", message = "{commerce.verifyPickup.validation.code.pattern}")
    private String pickupCode;

    public String getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(final String pickupCode) {
        this.pickupCode = pickupCode;
    }
}
