package sk.eea.td.rest.validation;

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;
import sk.eea.td.rest.model.HarvestRequest;

import java.util.ArrayList;
import java.util.List;

public class HarvestRequestValidationSequenceProvider implements DefaultGroupSequenceProvider<HarvestRequest> {

    public List<Class<?>> getValidationGroups(HarvestRequest harvestRequest) {
        List<Class<?>> sequence = new ArrayList<>();
        sequence.add(HarvestRequest.class);

        if (harvestRequest != null) {
            switch (harvestRequest.getConnector()) {
                case EUROPEANA:
                    sequence.add(EuropeanaValidation.class);
                    break;
                case HISTORYPIN:
                    sequence.add(HistorypinValidation.class);
                    break;
                default:
                    throw new IllegalStateException("Validation logic for connector: " + harvestRequest.getConnector() + " is missing!");
            }
        }
        return sequence;
    }

}
