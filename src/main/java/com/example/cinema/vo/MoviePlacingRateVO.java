package com.example.cinema.vo;

public class MoviePlacingRateVO {
	
	
    /**
     * 电影名称
     */
    private String movieName;
    /**
     * 上座率
     */
    private Double moviePlacingRate;


    public MoviePlacingRateVO(String movieName, Double moviePlacingRate) {
        this.movieName = movieName;
        this.moviePlacingRate = moviePlacingRate;
    }

    public String getMovieName() {
		return movieName;
	}
    
	public void setMovieName(String movieName) {
		this.movieName = movieName;
	}
	
    public Double getMoviePlacingRate() {
        return moviePlacingRate;
    }

    public void setMoviePlacingRate(Double moviePlacingRate) {
        this.moviePlacingRate = moviePlacingRate;
    }
	
}

