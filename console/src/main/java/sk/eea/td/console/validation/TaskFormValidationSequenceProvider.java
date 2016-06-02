package sk.eea.td.console.validation;

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;
import sk.eea.td.console.form.TaskForm;
import sk.eea.td.console.model.Connector;
import sk.eea.td.rest.validation.*;

import java.util.ArrayList;
import java.util.List;

public class TaskFormValidationSequenceProvider implements DefaultGroupSequenceProvider<TaskForm> {

    public List<Class<?>> getValidationGroups(TaskForm taskForm) {
        List<Class<?>> sequence = new ArrayList<>();
        sequence.add(TaskForm.class);
        if (taskForm != null && taskForm.getFlow() != null) {
            switch (taskForm.getFlow()){
                case FLOW_1:
                    sequence.add(Flow1Validation.class);
                    break;
                case FLOW_2:
                    sequence.add(Flow2Validation.class);
                    break;
                case FLOW_4:
                    sequence.add(Flow4Validation.class);
                    break;
                case FLOW_5:
                    sequence.add(Flow5Validation.class);
                    break;
                case FLOW_6:
                    sequence.add(Flow6Validation.class);
                    break;
            }
        }
        return sequence;
    }
}
