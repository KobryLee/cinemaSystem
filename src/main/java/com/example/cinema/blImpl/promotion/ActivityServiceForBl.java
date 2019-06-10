package com.example.cinema.blImpl.promotion;


import com.example.cinema.po.Activity;

import java.sql.Timestamp;
import java.util.List;

public interface ActivityServiceForBl {

    List<Activity> selectActivityByTimeAndMovie(Timestamp timestamp,int movieId);

}
