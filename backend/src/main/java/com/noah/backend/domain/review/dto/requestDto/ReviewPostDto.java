package com.noah.backend.domain.review.dto.requestDto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewPostDto {

    private Long review_id;
    private int expense;
    private String country;
    private int people;
    private Date start_date;
    private Date end_date;

}
