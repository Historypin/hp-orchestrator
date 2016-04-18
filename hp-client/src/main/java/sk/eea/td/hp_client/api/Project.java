package sk.eea.td.hp_client.api;

public class Project {

    private String title;

    private Location location;

    public Project() {
    }

    public Project(String title, Location location) {
        this.title = title;
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override public String toString() {
        return "Project{" +
                "title='" + title + '\'' +
                ", location=" + location +
                '}';
    }
}
