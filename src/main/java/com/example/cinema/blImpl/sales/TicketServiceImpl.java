package com.example.cinema.blImpl.sales;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cinema.bl.sales.TicketService;
import com.example.cinema.blImpl.management.hall.HallServiceForBl;
import com.example.cinema.blImpl.management.schedule.MovieServiceForBl;
import com.example.cinema.blImpl.management.schedule.ScheduleServiceForBl;
import com.example.cinema.blImpl.promotion.ActivityServiceForBl;
import com.example.cinema.blImpl.promotion.CouponServiceForBl;
import com.example.cinema.blImpl.promotion.VIPServiceForBl;
import com.example.cinema.data.sales.TicketMapper;
import com.example.cinema.po.*;
import com.example.cinema.vo.*;

/**
 * Created by liying on 2019/4/16.
 */
@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    TicketMapper ticketMapper;
    @Autowired
    MovieServiceForBl movieService;
    @Autowired
    ScheduleServiceForBl scheduleService;
    @Autowired
    HallServiceForBl hallService;
    @Autowired
    CouponServiceForBl couponService;
    @Autowired
    ActivityServiceForBl activityService;
    @Autowired
    VIPServiceForBl vipService;

    @Override
    @Transactional
    public ResponseVO addTicket(TicketForm ticketForm) {
    	try{
            int userId=ticketForm.getUserId();
            int scheduleId=ticketForm.getScheduleId();
            List<SeatForm> seats = ticketForm.getSeats();
            List<Integer> tickeIdList = new ArrayList<Integer>();
            
            for(SeatForm s: seats){
                Ticket ticket= new Ticket();
                ticket.setUserId(userId) ;
                ticket.setScheduleId(scheduleId);
                ticket.setColumnIndex(s.getColumnIndex());
                ticket.setRowIndex(s.getRowIndex());
                ticket.setState(0);
                
                ticketMapper.insertTicket(ticket);

                tickeIdList.add(ticket.getId());
            }//对于每一张ticket都在数据库中添加一个ticket对象

            return ResponseVO.buildSuccess(tickeIdList);
        }
        catch (Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("选座失败！");
        }
    }

    @Override
    @Transactional
    public ResponseVO completeTicket(List<Integer> id, int couponId) {
    	try{
            List<Ticket> tickets = new ArrayList<Ticket>();
            for(Integer i: id){
                tickets.add(ticketMapper.selectTicketById(i));
            }
            List<TicketVO> ticketVOs = new ArrayList<TicketVO>();
            Coupon coupon = couponService.getCouponById(couponId);
            int movieId = tickets.get(0).getScheduleId(); //通过ticket寻找movieId，由于多张tickets都只对应1部电影，因此只要取第一张ticket
            int userId = tickets.get(0).getUserId();

            double total = 0;
            Timestamp timestamp = tickets.get(0).getTime();

            for(Ticket t: tickets){
                int scheduleId = t.getScheduleId();
                ScheduleItem schedule = scheduleService.getScheduleItemById(scheduleId);
                double fare = schedule.getFare();

                ticketMapper.updateTicketState(t.getId(), 1);   // 改变ticket状态为已购买（"1"）
                t.setState(1);

                TicketVO ticketVO = t.getVO();
                ticketVOs.add(ticketVO);  //构造ticketVO后加入一个ticketVOS的列表
                total += fare;
            }

            List<Activity> activities = activityService.selectActivityByTimeAndMovie(timestamp, movieId);
            //根据时间和电影ID在数据库中寻找满足条件的活动
            List<Coupon> couponsToGive=new ArrayList<Coupon>();  //构造根据活动赠送的优惠券列表
            for(Activity i:activities){

                if(!couponService.existCouponUser(i.getCoupon().getId(), userId)){
                    couponsToGive.add(i.getCoupon());
                    ticketMapper.addCoupon(i.getCoupon().getId(), userId);
                }

            }//添加赠送的优惠券


            TicketWithCouponVO ticketWithCouponVO = new TicketWithCouponVO();
            ticketWithCouponVO.setCoupons(couponsToGive);
            ticketWithCouponVO.setTicketVOList(ticketVOs);
            ticketWithCouponVO.setTotal(total);
            //构造ticketWithCouponVO作为ResponseVO中的content

            if(coupon.getTargetAmount()<=total){
                couponService.deleteCoupon(couponId,userId);
                return ResponseVO.buildSuccess(ticketWithCouponVO);

            }//校验优惠券（默认前端已经做好根据时间筛选优惠券的操作，即这里选择的优惠券是在优惠期限以内的）
            return ResponseVO.buildFailure("总额低于门槛");

        }catch (Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }//返回的是一个TicketWithCouponVO,里面包含了js文件里面的TicketVOList、没有优惠后的总价total，以及赠送的coupons列表

    @Override
    public ResponseVO getBySchedule(int scheduleId) {
        try {
            List<Ticket> tickets = ticketMapper.selectTicketsBySchedule(scheduleId);
            ScheduleItem schedule = scheduleService.getScheduleItemById(scheduleId);
            Hall hall = hallService.getHallById(schedule.getHallId());
            int[][] seats=new int[hall.getRow()][hall.getColumn()];
            tickets.stream().forEach(ticket -> {
                seats[ticket.getRowIndex()][ticket.getColumnIndex()]=1;
            });
            ScheduleWithSeatVO scheduleWithSeatVO = new ScheduleWithSeatVO();
            scheduleWithSeatVO.setScheduleItem(schedule);
            scheduleWithSeatVO.setSeats(seats);
            return ResponseVO.buildSuccess(scheduleWithSeatVO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }

    @Override
    public ResponseVO getTicketByUser(int userId) {
    	try {
    		List<Ticket> tickets = ticketMapper.selectTicketByUser(userId);
    		List<TicketForm> ticketForms = new ArrayList<TicketForm>();
    		tickets.stream().forEach(ticket -> {
				int scheduleId = ticket.getScheduleId();
				ScheduleItem scheduleItem = scheduleService.getScheduleItemById(scheduleId);
				TicketForm ticketForm = new TicketForm(userId, scheduleId, scheduleItem);
				ticketForm.addSeat(ticket.getColumnIndex(), ticket.getRowIndex());
				ticketForm.setState(ticket.getState());
				ticketForms.add(ticketForm);
    		});
    		
    		return ResponseVO.buildSuccess(ticketForms);
        }
    	catch (Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }

    @Override
    @Transactional
    public ResponseVO completeByVIPCard(List<Integer> id, int couponId) {
    	try{
            Coupon coupon = couponService.getCouponById(couponId);
            List<Ticket> tickets = new ArrayList<Ticket>();

            for(int i: id){
                tickets.add(ticketMapper.selectTicketById(i));
            }
            int movieId=tickets.get(0).getScheduleId();
            int userId=tickets.get(0).getUserId();
            VIPCard vipCard = vipService.selectCardByUserId(userId);
            List<TicketVO> ticketVOS=new ArrayList<>();
            Timestamp timestamp=tickets.get(0).getTime();
            double total=0;

            for(Ticket t: tickets){
                int scheduleId=t.getScheduleId();
                ScheduleItem schedule=scheduleService.getScheduleItemById(scheduleId);
                double fare=schedule.getFare();
                total+=fare;
            }

            List<Activity> activities = activityService.selectActivityByTimeAndMovie(timestamp, movieId);
            TicketWithCouponVO ticketWithCouponVO=new TicketWithCouponVO();

            List<Coupon> couponsToGive=new ArrayList<>();
            for(Activity i:activities){
                if(!couponService.existCouponUser(i.getCoupon().getId(),userId)){
                    ticketMapper.addCoupon(i.getCoupon().getId(),userId);
                    couponsToGive.add(i.getCoupon());
                }
            }


            

            double payment;
            double discountAmount = 0;
            double targetAmount = 0;

            if(coupon!=null){
                discountAmount = coupon.getDiscountAmount();
                targetAmount = coupon.getTargetAmount();
            }
            
            if(targetAmount <= total) {
                payment = total - discountAmount;
            }
            else{
                System.out.println("总额未达到优惠券使用门槛"); 
                payment = total;
            }
            
            if(vipCard.getBalance() >= payment){
                vipCard.setBalance(vipCard.getBalance() - payment);
                ticketMapper.VIPPay(userId,payment);  //会员卡扣费
                couponService.deleteCoupon(couponId,userId);
                System.out.println("付费成功");
                for (Ticket t : tickets) {
                    t.setState(1);
                    ticketMapper.updateTicketState(t.getId(),1);
                    TicketVO ticketVO = t.getVO();
                    ticketVOS.add(ticketVO);
                }
                ticketWithCouponVO.setCoupons(couponsToGive);
                ticketWithCouponVO.setTicketVOList(ticketVOS);
                ticketWithCouponVO.setTotal(total);
                return ResponseVO.buildSuccess(ticketWithCouponVO);
            }
            else{
                return ResponseVO.buildFailure("支付失败");
            }
        }
    	catch (Exception e){
            e.printStackTrace();
            return ResponseVO.buildFailure("失败");
        }
    }

    @Override
    public ResponseVO cancelTicket(List<Integer> id) {
    	 try {
             for (int i: id) {
                 ticketMapper.updateTicketState(i, 2);
             }
             return ResponseVO.buildSuccess();
         }
    	 catch (Exception e) {
             e.printStackTrace();
             return ResponseVO.buildFailure("失败!");
         }
    }


}
