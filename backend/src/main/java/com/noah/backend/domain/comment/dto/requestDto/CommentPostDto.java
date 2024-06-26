package com.noah.backend.domain.comment.dto.requestDto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentPostDto {

    @Setter
    private String content;

    @Setter
    private Long member;
    @Setter
    private Long review;

    private Long memberId;
    private Long reviewId;
}
