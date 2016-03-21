package sk.eea.td.console.validation;

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;
import sk.eea.td.console.form.TaskForm;
import sk.eea.td.console.model.Destination;
import sk.eea.td.rest.validation.EuropeanaValidation;
import sk.eea.td.rest.validation.HistorypinTargetValidation;
import sk.eea.td.rest.validation.HistorypinValidation;
import sk.eea.td.rest.validation.OaipmhValidation;

import java.util.ArrayList;
import java.util.List;

public class TaskFormValidationSequenceProvider implements DefaultGroupSequenceProvider<TaskForm> {

    public List<Class<?>> getValidationGroups(TaskForm taskForm) {
        List<Class<?>> sequence = new ArrayList<>();
        sequence.add(TaskForm.class);

        if (taskForm != null) {
            if(taskForm.getDestinations() != null && taskForm.getDestinations().contains(Destination.HP)) {
                sequence.add(HistorypinTargetValidation.class);
            }

            if (TaskForm.Harvesting.EU.equals(taskForm.getHarvesting())) {
                if (TaskForm.Type.OAIPMH.equals(taskForm.getType())) {
                    sequence.add(OaipmhValidation.class);
                } else {
                    sequence.add(EuropeanaValidation.class);
                }
            } else {
                sequence.add(HistorypinValidation.class);
            }
        }
        return sequence;
    }
}
