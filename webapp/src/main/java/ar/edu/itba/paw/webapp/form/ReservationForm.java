package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;

public class ReservationForm {

    @NotBlank
    @Size(max = 100)
    @Pattern(regexp = "^(?:[a-zA-Z]+|\\s*)$", message = "{reservation.firstName.invalid}")
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Pattern(regexp = "^[\\p{L}]+(?:\\s+[\\p{L}]+)*$", message = "{reservation.lastName.invalid}")
    private String lastName;

    @NotBlank
    @Email(message = "{reservation.email.invalid}")
    @Size(max = 255)
    private String email;

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[0-9]+$", message = "{reservation.phone.invalid}")
    private String phone;

    @NotNull
    @Min(1)
    private Integer quantity;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

}
