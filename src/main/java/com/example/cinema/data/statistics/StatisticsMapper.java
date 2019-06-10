package com.example.cinema.data.statistics;

import com.example.cinema.po.AudiencePrice;
import com.example.cinema.po.Hall;
import com.example.cinema.po.MoviePlacingAudience;
import com.example.cinema.po.MovieScheduleTime;
import com.example.cinema.po.MovieTotalBoxOffice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @author fjj
 * @date 2019/4/16 1:43 PM
 */
@Mapper
public interface StatisticsMapper {
    /**
     * 查询date日期每部电影的排片次数
     * @param date
     * @return
     */
    List<MovieScheduleTime> selectMovieScheduleTimes(@Param("date") Date date, @Param("nextDate") Date nextDate);

    /**
     * 查询所有电影的总票房（包括已经下架的，降序排列）
     * @return
     */
    List<MovieTotalBoxOffice> selectMovieTotalBoxOffice();

    /**
     * 查询某天每个客户的购票金额
     * @param date
     * @param nextDate
     * @return
     */
    List<AudiencePrice> selectAudiencePrice(@Param("date") Date date, @Param("nextDate") Date nextDate);
    
    /**
     * 查询某天排片的观众人次
     * @param date
     * @return
     */
    List<MoviePlacingAudience> selectMoviePlacingAudience(@Param("date") Date date);
    
    /**
     * 查询所有影厅
     * @return
     */
    List<Hall> selectAllHall();
    
    /**
     * 查询给定日期后电影的总票房（降序排列）
     * @param startDate
     * @return
     */
	List<MovieTotalBoxOffice> selectRecentMovieBoxOffice(@Param("date") Date startDate);
}
