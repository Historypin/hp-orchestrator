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

    public static class Pin {

        private String pinnerType;

        private String caption;

        private String description;

        private String date;

        private String license;

        private Location location;

        private String link;

        private String content;

        private String tags;

        private String remoteId;

        public String getPinnerType() {
            return pinnerType;
        }

        public void setPinnerType(String pinnerType) {
            this.pinnerType = pinnerType;
        }

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

        public String getLicense() {
            return license;
        }

        public void setLicense(String license) {
            this.license = license;
        }

        public Location getLocation() {
            return location;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getTags() {
            return tags;
        }

        public void setTags(String tags) {
            this.tags = tags;
        }

        public String getRemoteId() {
            return remoteId;
        }

        public void setRemoteId(String remoteId) {
            this.remoteId = remoteId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        @Override public String toString() {
            return "Pin{" +
                    "pinnerType='" + pinnerType + '\'' +
                    ", caption='" + caption + '\'' +
                    ", description='" + description + '\'' +
                    ", date='" + date + '\'' +
                    ", license='" + license + '\'' +
                    ", location=" + location +
                    ", link='" + link + '\'' +
                    ", content='" + content + '\'' +
                    ", tags='" + tags + '\'' +
                    ", remoteId='" + remoteId + '\'' +
                    '}';
        }
    }

    public static class Location {

        private String lat;

        private String lng;

        private String range;

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLng() {
            return lng;
        }

        public void setLng(String lng) {
            this.lng = lng;
        }

        public String getRange() {
            return range;
        }

        public void setRange(String range) {
            this.range = range;
        }

        @Override public String toString() {
            return "Location{" +
                    "lat='" + lat + '\'' +
                    ", lng='" + lng + '\'' +
                    ", range='" + range + '\'' +
                    '}';
        }
    }

    public static class EuropeanaFields {

        private String type;

        private String year;

        private String country;

        private String rights;

        private String edmIsShownBy;

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

        public String getEdmIsShownBy() {
            return edmIsShownBy;
        }

        public void setEdmIsShownBy(String edmIsShownBy) {
            this.edmIsShownBy = edmIsShownBy;
        }

        @Override public String toString() {
            return "EuropeanaFields{" +
                    "type='" + type + '\'' +
                    ", year='" + year + '\'' +
                    ", country='" + country + '\'' +
                    ", rights='" + rights + '\'' +
                    ", edmIsShownBy='" + edmIsShownBy + '\'' +
                    '}';
        }
    }
}
