package sk.eea.td.rest.model;

import sk.eea.td.hp_client.api.Pin;

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

        private EuropeanaFields europeanaFields;

        public Pin getPin() {
            return pin;
        }

        public void setPin(Pin pin) {
            this.pin = pin;
        }

        public EuropeanaFields getEuropeanaFields() {
            return europeanaFields;
        }

        public void setEuropeanaFields(EuropeanaFields europeanaFields) {
            this.europeanaFields = europeanaFields;
        }

        @Override public String toString() {
            return "Record{" +
                    "pin=" + pin +
                    ", europeanaFields=" + europeanaFields +
                    '}';
        }
    }

    public static class EuropeanaFields {

        private String type;

        private String year;

        private String country;

        private String rights;

        private String edmPlaceLatitude;

        private String edmPlaceLongitude;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getRights() {
            return rights;
        }

        public void setRights(String rights) {
            this.rights = rights;
        }

        public String getEdmPlaceLatitude() {
            return edmPlaceLatitude;
        }

        public void setEdmPlaceLatitude(String edmPlaceLatitude) {
            this.edmPlaceLatitude = edmPlaceLatitude;
        }

        public String getEdmPlaceLongitude() {
            return edmPlaceLongitude;
        }

        public void setEdmPlaceLongitude(String edmPlaceLongitude) {
            this.edmPlaceLongitude = edmPlaceLongitude;
        }

        @Override public String toString() {
            return "EuropeanaFields{" +
                    "type='" + type + '\'' +
                    ", year='" + year + '\'' +
                    ", country='" + country + '\'' +
                    ", rights='" + rights + '\'' +
                    ", edmPlaceLatitude='" + edmPlaceLatitude + '\'' +
                    ", edmPlaceLongitude='" + edmPlaceLongitude + '\'' +
                    '}';
        }
    }
}
