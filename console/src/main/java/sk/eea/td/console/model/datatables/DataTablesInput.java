package sk.eea.td.console.model.datatables;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataTablesInput {

	/**
	 * Draw counter. This is used by DataTables to ensure that the Ajax returns
	 * from server-side processing requests are drawn in sequence by DataTables
	 * (Ajax requests are asynchronous and thus can return out of sequence).
	 * This is used as part of the draw return parameter (see below).
	 */
	@NotNull
	@Min(0)
	private Integer draw;

	/**
	 * Paging first record indicator. This is the start point in the current
	 * data set (0 index based - i.e. 0 is the first record).
	 */
	@NotNull
	@Min(0)
	private Integer start;

	/**
	 * Number of records that the table can display in the current draw. It is
	 * expected that the number of records returned will be equal to this
	 * number, unless the server has fewer records to return. Note that this can
	 * be -1 to indicate that all records should be returned (although that
	 * negates any benefits of server-side processing!)
	 */
	@NotNull
	@Min(-1)
	private Integer length;

	@NotNull
	private Map<SearchCriteria, String> search;

	@NotEmpty
	private List<Map<ColumnCriteria, String>> columns;

	@NotEmpty
	private List<Map<OrderCriteria, String>> order;

	public enum SearchCriteria {
		value,
		regex
	}
	public enum OrderCriteria {
		column,
		dir
	}
	public enum ColumnCriteria {
		data,
		name,
		searchable,
		orderable,
		searchValue,
		searchRegex
	}

	public Integer getDraw() {
		return draw;
	}

	public void setDraw(Integer draw) {
		this.draw = draw;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Map<SearchCriteria, String> getSearch() {
		return search;
	}

	public void setSearch(Map<SearchCriteria, String> search) {
		this.search = search;
	}

	public List<Map<ColumnCriteria, String>> getColumns() {
		return columns;
	}

	public void setColumns(List<Map<ColumnCriteria, String>> columns) {
		this.columns = columns;
	}

	public List<Map<OrderCriteria, String>> getOrder() {
		return order;
	}

	public void setOrder(List<Map<OrderCriteria, String>> order) {
		this.order = order;
	}

	@Override public String toString() {
		return "DataTablesInput{" +
				"draw=" + draw +
				", start=" + start +
				", length=" + length +
				", search=" + search +
				", columns=" + columns +
				", order=" + order +
				'}';
	}
}
