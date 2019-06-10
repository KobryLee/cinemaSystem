package com.example.cinema.blImpl.statistics;

import com.example.cinema.bl.statistics.StatisticsService;
import com.example.cinema.data.statistics.StatisticsMapper;
import com.example.cinema.po.*;
import com.example.cinema.vo.AudiencePriceVO;
import com.example.cinema.vo.MoviePlacingRateVO;
import com.example.cinema.vo.MovieScheduleTimeVO;
import com.example.cinema.vo.MovieTotalBoxOfficeVO;
import com.example.cinema.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author fjj
 * @date 2019/4/16 1:34 PM
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    private StatisticsMapper statisticsMapper;
    @Override
    public ResponseVO getScheduleRateByDate(Date date) {
        try{
            Date requireDate = date;
            if(requireDate == null){
                requireDate = new Date();
            }
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            requireDate = simpleDateFormat.parse(simpleDateFormat.format(requireDate));

            Date nextDate = getNumDayAfterDate(requireDate, 1);
            return ResponseVO.buildSuccess(movieScheduleTimeList2MovieScheduleTimeVOList(statisticsMapper.selectMovieScheduleTimes(requireDate, nextDate)));

        }catch (Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }

    @Override
    public ResponseVO getTotalBoxOffice() {
        try {
            return ResponseVO.buildSuccess(movieTotalBoxOfficeList2MovieTotalBoxOfficeVOList(statisticsMapper.selectMovieTotalBoxOffice()));
        }catch (Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }
	
	
    @Override
    public ResponseVO getAudiencePriceSevenDays() {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date today = simpleDateFormat.parse(simpleDateFormat.format(new Date()));
            Date startDate = getNumDayAfterDate(today, -6);
            List<AudiencePriceVO> audiencePriceVOList = new ArrayList<>();
            for(int i = 0; i < 7; i++){
                AudiencePriceVO audiencePriceVO = new AudiencePriceVO();
                Date date = getNumDayAfterDate(startDate, i);
                audiencePriceVO.setDate(date);
                List<AudiencePrice> audiencePriceList = statisticsMapper.selectAudiencePrice(date, getNumDayAfterDate(date, 1));
                double totalPrice = audiencePriceList.stream().mapToDouble(item -> item.getTotalPrice()).sum();
                audiencePriceVO.setPrice(Double.parseDouble(String.format("%.2f", audiencePriceList.size() == 0 ? 0 : totalPrice / audiencePriceList.size())));
                audiencePriceVOList.add(audiencePriceVO);
            }
            return ResponseVO.buildSuccess(audiencePriceVOList);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }

    /**
     * 假设某影城设有n 个电影厅、m 个座位数，相对上座率=观众人次÷放映场次÷m÷n×100%
     * @param date
     * @return
     */
    @Override
    public ResponseVO getMoviePlacingRateByDate(Date date) {
        try {
        	List<MoviePlacingRateVO> moviePlacingRateVOList = new ArrayList<MoviePlacingRateVO>();
            List<MoviePlacingAudience> moviePlacingAudienceList = new ArrayList<MoviePlacingAudience>();

            //得到影厅数据 得到影厅数和座位数
            List<Hall> hallList = statisticsMapper.selectAllHall();
            int totalSeats = 0;
            int hallNumber = hallList.size();
            for (Hall h: hallList) {
                int row = h.getRow();
                int col = h.getColumn();
                totalSeats += row*col;
            }
            
            //得到电影上座人数列表
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            date = simpleDateFormat.parse(simpleDateFormat.format(date));
        	moviePlacingAudienceList = statisticsMapper.selectMoviePlacingAudience(date);

        	//计算上座率
        	for (MoviePlacingAudience m:moviePlacingAudienceList
                 ) {
                double audiences = m.getAudience();
                double playTimes = m.getTimes();
                double rate = audiences/playTimes/totalSeats/hallNumber;
                moviePlacingRateVOList.add(new MoviePlacingRateVO(m.getMovieName(),rate));
        	}
	        return ResponseVO.buildSuccess(moviePlacingRateVOList);
        } 
        catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.buildFailure("得到上座率失败");
        }
    }

    @Override
    public ResponseVO getPopularMovies(int days, int movieNum) {
    	 //要求见接口说明
    	try {
    		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date today = simpleDateFormat.parse(simpleDateFormat.format(new Date()));
            Date startDate = getNumDayAfterDate(today, -days);
            
            return ResponseVO.buildSuccess(movieBoxTotalOfficeList2MovieBoxOfficeVOArray(statisticsMapper.selectRecentMovieBoxOffice(startDate), movieNum)); 
    	}
    	catch (Exception e) {
    		e.printStackTrace();
            return ResponseVO.buildFailure("失败");
    	}
    }

    
	/**
     * 获得num天后的日期
     * @param oldDate
     * @param num
     * @return
     */
    Date getNumDayAfterDate(Date oldDate, int num){
        Calendar calendarTime = Calendar.getInstance();
        calendarTime.setTime(oldDate);
        calendarTime.add(Calendar.DAY_OF_YEAR, num);
        return calendarTime.getTime();
    }


    private List<MovieScheduleTimeVO> movieScheduleTimeList2MovieScheduleTimeVOList(List<MovieScheduleTime> movieScheduleTimeList){
        List<MovieScheduleTimeVO> movieScheduleTimeVOList = new ArrayList<>();
        for(MovieScheduleTime movieScheduleTime : movieScheduleTimeList){
            movieScheduleTimeVOList.add(new MovieScheduleTimeVO(movieScheduleTime));
        }
        return movieScheduleTimeVOList;
    }


    private List<MovieTotalBoxOfficeVO> movieTotalBoxOfficeList2MovieTotalBoxOfficeVOList(List<MovieTotalBoxOffice> movieTotalBoxOfficeList){
        List<MovieTotalBoxOfficeVO> movieTotalBoxOfficeVOList = new ArrayList<>();
        for(MovieTotalBoxOffice movieTotalBoxOffice : movieTotalBoxOfficeList){
            movieTotalBoxOfficeVOList.add(new MovieTotalBoxOfficeVO(movieTotalBoxOffice));
        }
        return movieTotalBoxOfficeVOList;
    }
    
    
    private MovieTotalBoxOfficeVO[] movieBoxTotalOfficeList2MovieBoxOfficeVOArray(List<MovieTotalBoxOffice> movieTotalBoxOfficeList, int movieNum) {
    	MovieTotalBoxOfficeVO[] movieTotalBoxOfficeVOArray = new MovieTotalBoxOfficeVO[movieNum];
    	for(int i = 0; i < movieNum; i++) {
    		movieTotalBoxOfficeVOArray[i] = new MovieTotalBoxOfficeVO(movieTotalBoxOfficeList.get(i));
    	}
		return movieTotalBoxOfficeVOArray;
	}
}
