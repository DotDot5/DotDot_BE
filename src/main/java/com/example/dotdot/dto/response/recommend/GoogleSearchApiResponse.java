package com.example.dotdot.dto.response.recommend;

import lombok.Getter;

import java.util.List;

@Getter
public class GoogleSearchApiResponse {
    private List<Item> items;

    @Getter
    public static class Item {
        private String title;
        private String link;
        private String snippet;
//        private String publishedDate;
    }
}
