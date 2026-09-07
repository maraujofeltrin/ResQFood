package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.webapp.validation.constraints.DateLessOrEqual;
import ar.edu.itba.paw.webapp.validation.constraints.PastOrPresentDate;

@DateLessOrEqual(first = "fromDate", second = "toDate")
public class MetricsFilterForm {

    @PastOrPresentDate
    private String fromDate;
    @PastOrPresentDate
    private String toDate;

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(final String fromDate) {
        this.fromDate = fromDate;
    }

    public String getFrom() {
        return fromDate;
    }

    public void setFrom(final String from) {
        this.fromDate = from;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(final String toDate) {
        this.toDate = toDate;
    }

    public String getTo() {
        return toDate;
    }

    public void setTo(final String to) {
        this.toDate = to;
    }
}
