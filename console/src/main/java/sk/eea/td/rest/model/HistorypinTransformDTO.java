package sk.eea.td.rest.model;

import java.util.List;

public class HistorypinTransformDTO {

    List<Record> records;

    public List<Record> getRecords() {
        return records;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    @Override public String toString() {
        return "TransformationResponseDTO{" +
                "records=" + records +
                '}';
    }

    public static class Record {

        private Pin pin;

        private Remote remote;

        public Pin getPin() {
            return pin;
        }

        public void setPin(Pin pin) {
            this.pin = pin;
        }

        public Remote getRemote() {
            return remote;
        }

        public void setRemote(Remote remote) {
            this.remote = remote;
        }

        @Override public String toString() {
            return "Record{" +
                    "pin=" + pin +
                    ", remote=" + remote +
                    '}';
        }
    }

    public static class Pin {

        private String type;

        private String caption;

        private String date;

        private String country;

        private String license;

        private String preview;

        private String landingPage;

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getLicense() {
            return license;
        }

        public void setLicense(String license) {
            this.license = license;
        }

        public String getPreview() {
            return preview;
        }

        public void setPreview(String preview) {
            this.preview = preview;
        }

        public String getLandingPage() {
            return landingPage;
        }

        public void setLandingPage(String landingPage) {
            this.landingPage = landingPage;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override public String toString() {
            return "Pin{" +
                    "type='" + type + '\'' +
                    ", caption='" + caption + '\'' +
                    ", date='" + date + '\'' +
                    ", country='" + country + '\'' +
                    ", license='" + license + '\'' +
                    ", preview='" + preview + '\'' +
                    ", landingPage='" + landingPage + '\'' +
                    '}';
        }
    }

    public static class Remote {

        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override public String toString() {
            return "Remote{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }
}
