package sk.eea.td.hp_client.dto;

import sk.eea.td.hp_client.api.Location;

public class PlacesResponseDTO {

    private Long id;

    private Bounds bounds;

    public static class Bounds {

        private Location sw;

        private Location ne;

        public Location getSw() {
            return sw;
        }

        public void setSw(Location sw) {
            this.sw = sw;
        }

        public Location getNe() {
            return ne;
        }

        public void setNe(Location ne) {
            this.ne = ne;
        }

        @Override public String toString() {
            return "Bounds{" +
                    "sw=" + sw +
                    ", ne=" + ne +
                    '}';
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Bounds getBounds() {
        return bounds;
    }

    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    @Override public String toString() {
        return "PlacesResponseDTO{" +
                "id=" + id +
                ", bounds=" + bounds +
                '}';
    }
}
