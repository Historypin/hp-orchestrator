package sk.eea.td.console.model.dto;

import java.util.List;

public class ReviewDTOWrapper {

    private List<ReviewDTO> reviews;

    public ReviewDTOWrapper(List<ReviewDTO> reviews) {
        this.reviews = reviews;
    }

    public List<ReviewDTO> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewDTO> reviews) {
        this.reviews = reviews;
    }

    @Override
    public String toString() {
        return "ReviewDTOWrapper{" +
                "reviews=" + reviews +
                '}';
    }
}
