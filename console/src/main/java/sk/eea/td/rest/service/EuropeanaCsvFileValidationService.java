package sk.eea.td.rest.service;

import org.springframework.stereotype.Component;
import sk.eea.td.rest.validation.CsvFileValidationException;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class EuropeanaCsvFileValidationService {

    /**
     * This pattern was created based on combination of information about EUROPEANA ID form sources:
     *
     * http://labs.europeana.eu/api/data-hierarchy
     * https://www.wikidata.org/wiki/Property:P727
     * https://github.com/europeana/europeana-client-ruby/blob/master/lib/europeana/record.rb
     *
     */
    public static final Pattern EUROPEANA_ID_PATTERN = Pattern.compile("^\\/\\d+\\/[^\\/]+$");

    public static final int FAULT_LINES_THRESHOLD = 10;

    public void validate(File file) throws CsvFileValidationException, IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            long lineNumber = 1;
            Set<Long> faultLines = new HashSet<>();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if(line.isEmpty()) { // skip empty lines
                    lineNumber++;
                    continue;
                }
                if(!EUROPEANA_ID_PATTERN.matcher(line).matches()) {
                    faultLines.add(lineNumber);
                    if(faultLines.size() >= FAULT_LINES_THRESHOLD) {
                        throw new CsvFileValidationException("Exception at validating CSV file.", true, faultLines);
                    }
                }
                lineNumber++;
            }

            if(!faultLines.isEmpty()) {
                throw new CsvFileValidationException("Exception at validating CSV file.", false, faultLines);
            }
        }
    }
}
