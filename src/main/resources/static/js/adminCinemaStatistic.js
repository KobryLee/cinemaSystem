$(document).ready(function() {
	var placingRateDate = formatDate(new Date());
	var popularMovie_days = 30;
	var popularMovie_num = 7;
	
	initCharts();
	
	function initCharts() {
		getScheduleRate();
	    getBoxOffice();
	    getAudiencePrice();
	    
		$('#place-rate-date-input').val(placingRateDate);
		getPlacingRate();
		// 过滤条件变化后重新查询
        $('#place-rate-date-input').change (function () {
        	placingRateDate = $(this).val();
            getPlacingRate();
        });
        
        $('#popular-movie-days-input').val(popularMovie_days);
        $('#popular-movie-num-input').val(popularMovie_num);
        getPolularMovie();
        // 过滤条件变化后重新查询
        $('#popular-movie-days-input').change (function () {
        	popularMovie_days = $(this).val();
        	getPolularMovie();
        });
        $('#popular-movie-num-input').change (function () {
        	popularMovie_num = $(this).val();
        	getPolularMovie();
        });
	}

    function getScheduleRate() {

        getRequest(
            '/statistics/scheduleRate',
            function (res) {
                var data = res.content||[];
                var tableData = data.map(function (item) {
                   return {
                       value: item.time,
                       name: item.name
                   };
                });
                var nameList = data.map(function (item) {
                    return item.name;
                });
                var option = {
                    title : {
                        text: '今日排片率',
                        subtext: new Date().toLocaleDateString(),
                        x:'center'
                    },
                    tooltip : {
                        trigger: 'item',
                        formatter: "{a} <br/>{b} : {c} ({d}%)"
                    },
                    legend: {
                        x : 'center',
                        y : 'bottom',
                        data:nameList
                    },
                    toolbox: {
                        show : true,
                        feature : {
                            mark : {show: true},
                            dataView : {show: true, readOnly: false},
                            magicType : {
                                show: true,
                                type: ['pie', 'funnel']
                            },
                            restore : {show: true},
                            saveAsImage : {show: true}
                        }
                    },
                    calculable : true,
                    series : [
                        {
                            name:'面积模式',
                            type:'pie',
                            radius : [30, 110],
                            center : ['50%', '50%'],
                            roseType : 'area',
                            data:tableData
                        }
                    ]
                };
                var scheduleRateChart = echarts.init($("#schedule-rate-container")[0]);
                scheduleRateChart.setOption(option);
            },
            function (error) {
                alert(JSON.stringify(error));
            }
        );
    }

    function getBoxOffice() {

        getRequest(
            '/statistics/boxOffice/total',
            function (res) {
                var data = res.content || [];
                var tableData = data.map(function (item) {
                    return item.boxOffice;
                });
                var nameList = data.map(function (item) {
                    return item.name;
                });
                var option = {
                    title : {
                        text: '所有电影票房',
                        subtext: '截止至'+new Date().toLocaleDateString(),
                        x:'center'
                    },
                    xAxis: {
                        type: 'category',
                        data: nameList
                    },
                    yAxis: {
                        type: 'value'
                    },
                    series: [{
                        data: tableData,
                        type: 'bar'
                    }]
                };
                var scheduleRateChart = echarts.init($("#box-office-container")[0]);
                scheduleRateChart.setOption(option);
            },
            function (error) {
                alert(JSON.stringify(error));
            });
    }

    function getAudiencePrice() {
        getRequest(
            '/statistics/audience/price',
            function (res) {
                var data = res.content || [];
                var tableData = data.map(function (item) {
                    return item.price;
                });
                var nameList = data.map(function (item) {
                    return formatDate(new Date(item.date));
                });
                var option = {
                    title : {
                        text: '每日客单价',
                        x:'center'
                    },
                    xAxis: {
                        type: 'category',
                        data: nameList
                    },
                    yAxis: {
                        type: 'value'
                    },
                    series: [{
                        data: tableData,
                        type: 'bar'
                    }]
                };
                var scheduleRateChart = echarts.init($("#audience-price-container")[0]);
                scheduleRateChart.setOption(option);
            },
            function (error) {
                alert(JSON.stringify(error));
            });
    }

    function getPlacingRate() {
    	getRequest(
			'/statistics/PlacingRate?date='+placingRateDate.replace(/-/g,'/'),
			function (res) {
			    var data = res.content || [];
			    var tableData = data.map(function (item) {
			        return item.placingRate;
			    });
			    var nameList = data.map(function (item) {
			        return item.movieName;
			    });
			    var option = {
			        title : {
			            text: '上座率',
			            x:'center'
			        },
			        xAxis: {
			            type: 'category',
			            data: nameList
			        },
			        yAxis: {
			            type: 'value'
			        },
			        series: [{
			            data: tableData,
			            type: 'line'
			        }]
			    };
			    var scheduleRateChart = echarts.init($("#place-rate-container")[0]);
			    scheduleRateChart.setOption(option);
			},
			function (error) {
			    alert(JSON.stringify(error));
			});
    }

    function getPolularMovie() {
    	getRequest(
		'/statistics/popular/movie?days='+popularMovie_days+'&movieNum='+popularMovie_num,
		function (res) {
			 var data = res.content || [];
             var tableData = data.map(function (item) {
                 return {
                     value: item.boxOffice,
                     name: item.name
                 };
             });
             var nameList = data.map(function (item) {
                 return item.name;
             });
             var option = {
                 title: {
                     text: popularMovie_days + '天内最受欢迎的' + popularMovie_num + '部电影',
                     x: 'center'
                 },
                 tooltip: {
                     trigger: 'item',
                     formatter: "{a} <br/>{b} : {c} ({d}%)"
                 },
                 legend: {
                     x: 'center',
                     y: 'bottom',
                     data: nameList
                 },
                 calculable: true,
                 series: [
                     {
                         name: '最受欢迎的电影',
                         type: 'pie',
                         radius: [30, 110],
                         //center : ['50%', '50%'],
                         //roseType : 'area',
                         data: tableData,
                         itemStyle: {
                             emphasis: {
                                 shadowBlur: 10,
                                 shadowOffsetX: 0,
                                 shadowColor: 'rgba(30,144,255,0.5)'
                             }
                         }
                     }
                 ]
            };
		    var scheduleRateChart = echarts.init($("#popular-movie-container")[0]);
		    scheduleRateChart.setOption(option);
		},
		function (error) {
		    alert(JSON.stringify(error));
		});
    }
});