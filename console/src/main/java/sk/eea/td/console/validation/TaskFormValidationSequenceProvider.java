package sk.eea.td.console.validation;

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;
import sk.eea.td.console.form.TaskForm;
import sk.eea.td.rest.model.Connector;
import sk.eea.td.rest.validation.EuropeanaValidation;
import sk.eea.td.rest.validation.HistorypinTargetValidation;
import sk.eea.td.rest.validation.HistorypinValidation;

import java.util.ArrayList;
import java.util.List;

public class TaskFormValidationSequenceProvider implements DefaultGroupSequenceProvider<TaskForm> {

    public List<Class<?>> getValidationGroups(TaskForm taskForm) {
        List<Class<?>> sequence = new ArrayList<>();
        sequence.add(TaskForm.class);
        if (taskForm != null) {
            if(taskForm.getTarget() != null && Connector.HISTORYPIN.equals(taskForm.getTarget())) {
                sequence.add(HistorypinTargetValidation.class);
            }

            if(taskForm.getSource() != null && Connector.EUROPEANA.equals(taskForm.getSource())) {
                sequence.add(EuropeanaValidation.class);
            }

            if(taskForm.getSource() != null && Connector.HISTORYPIN.equals(taskForm.getSource())) {
                sequence.add(HistorypinValidation.class);
            }
        }
        return sequence;
    }
}
