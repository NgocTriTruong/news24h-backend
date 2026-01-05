package com.news24h.news24hbackend.entity;

public enum FootballLeague {
    CHAMPIONS_LEAGUE("cup-c1", "Champions League",
            "https://www.24h.com.vn/bong-da/bang-xep-hang-cup-c1-champions-league-c48a400193.html",
            "https://www.24h.com.vn/bong-da/lich-thi-dau-cup-c1-champions-league-c48a465411.html",
            "https://www.24h.com.vn/bong-da/ket-qua-thi-dau-cup-c1-champions-league-c48a398193.html"),
    PREMIER_LEAGUE("ngoai-hang-anh", "Ngoại hạng Anh",
            "https://www.24h.com.vn/bong-da/bang-xep-hang-bong-da-anh-c48a466585.html",
            "https://www.24h.com.vn/bong-da/lich-thi-dau-bong-da-anh-c48a466567.html",
            "https://www.24h.com.vn/bong-da/ket-qua-bong-da-ngoai-hang-anh-c48a397633.html"),
    LA_LIGA("la-liga", "La Liga",
            "https://www.24h.com.vn/bong-da/bang-xep-hang-bong-da-tay-ban-nha-c48a468129.html",
            "https://www.24h.com.vn/bong-da/lich-thi-dau-bong-da-tay-ban-nha-c48a468110.html",
            "https://www.24h.com.vn/bong-da/ket-qua-thi-dau-bong-da-tay-ban-nha-c48a398196.html"),
    SERIE_A("serie-a", "Serie A",
            "https://www.24h.com.vn/bong-da/bang-xep-hang-bong-da-y-c48a394572.html",
            "https://www.24h.com.vn/bong-da/lich-thi-dau-bong-da-y-c48a394137.html",
            "https://www.24h.com.vn/bong-da/ket-qua-thi-dau-bong-da-y-c48a395045.html"),
    BUNDESLIGA("bundesliga", "Bundesliga",
            "https://www.24h.com.vn/bang-xep-hang-bong-da/bang-xep-hang-bong-da-duc-bundesliga-c295a467117.html",
            "https://www.24h.com.vn/bong-da-duc/lich-thi-dau-bong-da-duc-bundesliga-c152a467108.html",
            "https://www.24h.com.vn/bong-da/ket-qua-thi-dau-bong-da-duc-bundesliga-c48a396039.html"),
    LIGUE_1("ligue-1", "Ligue 1",
            "https://www.24h.com.vn/bong-da/bang-xep-hang-bong-da-phap-c48a394574.html",
            "https://www.24h.com.vn/bong-da/lich-thi-dau-bong-da-phap-c48a394560.html",
            "https://www.24h.com.vn/ket-qua-bong-da/ket-qua-thi-dau-bong-da-phap-c140a396314.html"),
    V_LEAGUE("v-league", "V.League 1",
            "https://www.24h.com.vn/bong-da/bang-xep-hang-v-league-c48a427024.html",
            "https://www.24h.com.vn/bong-da/lich-thi-dau-v-league-c48a417155.html",
            "https://www.24h.com.vn/bong-da/ket-qua-bong-da-v-league-c48a427014.html");

    private final String code;
    private final String name;
    private final String standingsUrl;
    private final String scheduleUrl;
    private final String resultsUrl;

    FootballLeague(String code, String name, String standingsUrl, String scheduleUrl, String resultsUrl) {
        this.code = code;
        this.name = name;
        this.standingsUrl = standingsUrl;
        this.scheduleUrl = scheduleUrl;
        this.resultsUrl = resultsUrl;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getStandingsUrl() {
        // Trang chính của giải có cả bảng xếp hạng và lịch thi đấu
        return standingsUrl;
    }

    public String getScheduleUrl() {
        return scheduleUrl;
    }

    public String getResultsUrl() {
        return resultsUrl;
    }

    public static FootballLeague fromCode(String code) {
        for (FootballLeague league : values()) {
            if (league.code.equals(code)) {
                return league;
            }
        }
        return null;
    }
}
