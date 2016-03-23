package sk.eea.td.hp_client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class ListingsResponseDTO {

    private int limit;

    private int count;

    private int page;

    private List<Result> results;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    @Override public String toString() {
        return "ListingsResponseDTO{" +
                "limit=" + limit +
                ", count=" + count +
                ", page=" + page +
                ", results=" + results +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Result {

        @JsonProperty("node_type")
        private String nodeType;

        private Long id;

        public String getNodeType() {
            return nodeType;
        }

        public void setNodeType(String nodeType) {
            this.nodeType = nodeType;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        @Override public String toString() {
            return "Result{" +
                    "nodeType='" + nodeType + '\'' +
                    ", id=" + id +
                    '}';
        }
    }
}
