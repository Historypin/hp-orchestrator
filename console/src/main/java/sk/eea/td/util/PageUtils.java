package sk.eea.td.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import sk.eea.td.console.model.datatables.DataTablesInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PageUtils {

    public static Pageable getPageable(DataTablesInput input) {
        final List<Sort.Order> orders = new ArrayList<>();

        if(input.getOrder().iterator().hasNext()) {
            final Map<DataTablesInput.OrderCriteria, String> orderMap = input.getOrder().iterator().next();
            final Integer column = Integer.parseInt(orderMap.get(DataTablesInput.OrderCriteria.column));

            final Map<DataTablesInput.ColumnCriteria, String> columnMap = input.getColumns().get(column);
            final Boolean orderable = Boolean.parseBoolean(columnMap.get(DataTablesInput.ColumnCriteria.orderable));
            if (orderable) {
                final String direction = orderMap.get(DataTablesInput.OrderCriteria.dir);
                final Sort.Direction sortDirection = Sort.Direction.fromString(direction);
                String sortColumn = columnMap.get(DataTablesInput.ColumnCriteria.data);

                // quick fix for nested entities
                // TODO: change it to more transparent way
                if ("jobRunId".equals(sortColumn)) {
                    sortColumn = "jobRun.id";
                } else if("lastRunId".equals(sortColumn)) {
                    sortColumn = "lastJobRun.id";
                }

                orders.add(new Sort.Order(sortDirection, sortColumn));
            }
        }
        Sort sort = orders.isEmpty() ? null : new Sort(orders);

        if (input.getLength() == -1) {
            input.setStart(0);
            input.setLength(Integer.MAX_VALUE);
        }
        return new PageRequest(input.getStart() / input.getLength(), input.getLength(), sort);
    }
}
