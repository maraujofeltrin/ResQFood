package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * Form for placing a bid on an active auction from the pack detail page.
 */
public class BidForm {

    @NotNull(message = "{pack.detail.bid.validation.amount.notNull}")
    @Positive(message = "{pack.detail.bid.validation.amount.positive}")
    @Digits(integer = 7, fraction = 2, message = "{pack.detail.bid.validation.amount.digits}")
    private Double amount;

    public Double getAmount() {
        return amount;
    }

    public void setAmount(final Double amount) {
        this.amount = amount;
    }
}
