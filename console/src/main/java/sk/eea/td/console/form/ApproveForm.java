package sk.eea.td.console.form;

import java.util.List;

public class ApproveForm {

    private List<ApproveItem> items;

    public List<ApproveItem> getItems() {
        return items;
    }

    public void setItems(List<ApproveItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "ApproveForm{" +
                "items=" + items +
                '}';
    }
}
