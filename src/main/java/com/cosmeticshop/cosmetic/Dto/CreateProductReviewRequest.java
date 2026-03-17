package com.cosmeticshop.cosmetic.Dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateProductReviewRequest {

    @NotNull(message = "rating không được để trống")
    @Min(value = 1, message = "rating phải từ 1 đến 5")
    @Max(value = 5, message = "rating phải từ 1 đến 5")
    private Integer rating;

    @NotBlank(message = "comment không được để trống")
    @Size(max = 2000, message = "comment không được quá 2000 ký tự")
    private String comment;

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
