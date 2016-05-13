package sk.eea.td.console.form;

public class Place {

    private String name;

    private Boolean approved;

    public Place() {
    }

    public Place(String name, Boolean approved) {
        this.name = name;
        this.approved = approved;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    @Override
    public String toString() {
        return "Place{" +
                "name='" + name + '\'' +
                ", approved=" + approved +
                '}';
    }
}
