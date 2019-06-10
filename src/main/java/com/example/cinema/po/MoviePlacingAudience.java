package com.example.cinema.po;

public class MoviePlacingAudience {
    /**
     * 电影名称
     */
    private String movieName;
    /**
     * 当天观看该电影的观众总人数
     */
    private Integer audience;
    /**
     * 当天该电影的放映场次
     */
    private Integer times;

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public Integer getAudience() {
        return audience;
    }

    public void setAudience(Integer audience) {
        this.audience = audience;
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }
}
