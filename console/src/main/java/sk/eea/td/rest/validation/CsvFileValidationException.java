package sk.eea.td.rest.validation;

import java.util.Set;

/**
 * Exception which is result of failed validation of CSV file.
 * Exception contains information about failed lines. If too many fault are detected, rest of errors are ignored,
 * and 'faultLinesOverflow' flag is raised.
 */
public class CsvFileValidationException extends Exception {

    private boolean faultLinesOverflow;

    private Set<Long> faultLines;

    public CsvFileValidationException(String msg, boolean faultLinesOverflow, Set<Long> faultLines) {
        super(msg);
        this.faultLinesOverflow = faultLinesOverflow;
        this.faultLines = faultLines;
    }

    public boolean isFaultLinesOverflow() {
        return faultLinesOverflow;
    }

    public void setFaultLinesOverflow(boolean faultLinesOverflow) {
        this.faultLinesOverflow = faultLinesOverflow;
    }

    public Set<Long> getFaultLines() {
        return faultLines;
    }

    public void setFaultLines(Set<Long> faultLines) {
        this.faultLines = faultLines;
    }
}
