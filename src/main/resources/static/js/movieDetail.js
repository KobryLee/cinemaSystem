$(document).ready(function(){

    var movieId = parseInt(window.location.href.split('?')[1].split('&')[0].split('=')[1]);
    var userId = sessionStorage.getItem('id');
    var isLike = false;

    getMovie();
    if(sessionStorage.getItem('role') === 'admin')
        getMovieLikeChart();

    function getMovieLikeChart() {
       getRequest(
           '/movie/' + movieId + '/like/date',
           function(res){
				var data = res.content,
					dateArray = [],
                    numberArray = [];
				data.forEach(function (item) {
					dateArray.push(item.likeTime);
					numberArray.push(item.likeNum);
               });

               var myChart = echarts.init($("#like-date-chart")[0]);

               // 指定图表的配置项和数据
               var option = {
                   title: {
                       text: '想看人数变化表'
                   },
                   xAxis: {
                       type: 'category',
                       data: dateArray
                   },
                   yAxis: {
                       type: 'value'
                   },
                   series: [{
                       data: numberArray,
                       type: 'line'
                   }]
               };

               // 使用刚指定的配置项和数据显示图表。
               myChart.setOption(option);
           },
           function (error) {
               alert(error);
           }
       );
    }

    function getMovie() {
        getRequest(
            '/movie/'+movieId + '/' + userId,
            function(res){
                var data = res.content;
                isOn = (data.status == 0);
                isLike = data.islike;
                repaintMovieDetail(data);
            },
            function (error) {
                alert(error);
            }
        );
    }

    function repaintMovieDetail(movie) {
        !isLike ? $('.icon-heart').removeClass('error-text') : $('.icon-heart').addClass('error-text');
        $('#like-btn span').text(isLike ? ' 已想看' : ' 想 看');
        $('#movie-img').attr('src',movie.posterUrl);
        $('#movie-name').text(movie.name);
        $('#order-movie-name').text(movie.name);
        $('#movie-description').text(movie.description);
        $('#movie-startDate').text(new Date(movie.startDate).toLocaleDateString());
        $('#movie-type').text(movie.type);
        $('#movie-country').text(movie.country);
        $('#movie-language').text(movie.language);
        $('#movie-director').text(movie.director);
        $('#movie-starring').text(movie.starring);
        $('#movie-writer').text(movie.screenWriter);
        $('#movie-length').text(movie.length);
        if(movie.status) {
        	$("#delete-btn").attr('disabled', 'disabled');
			$("#delete-btn").html('已下架');
        }
		
		//填充修改表单
		$('#movie-name-input').attr('value', movie.name);
        $('#movie-date-input').attr('value', movie.startDate.substring(0, 10));
        $('#movie-img-input').attr('value', movie.posterUrl);
        $('#movie-description-input').attr('value', movie.description);
        $('#movie-type-input').attr('value', movie.type);
        $('#movie-length-input').attr('value', movie.length);
        $('#movie-country-input').attr('value', movie.country);
        $('#movie-star-input').attr('value', movie.starring);
        $('#movie-director-input').attr('value', movie.director);
        $('#movie-writer-input').attr('value', movie.screenWriter);
        $('#movie-language-input').attr('value', movie.language);
    }

    // user界面才有
    $('#like-btn').click(function () {
        var url = isLike ?'/movie/'+ movieId +'/unlike?userId='+ userId :'/movie/'+ movieId +'/like?userId='+ userId;
        postRequest(
			url,
			null,
			function (res) {
				isLike = !isLike;
				getMovie();
			},
			function (error) {
				alert(error);
			});
    });

    // admin界面才有
	$("#modify-confirm-btn").click(function () {
        var formData = getMovieForm();
        if(!validateMovieForm(formData)) {
        	alert("failed!")
            return;
        }
        postRequest(
            '/movie/update',
            formData,
            function (res) {
                getMovie();
                $("#modifyModal").modal('hide');
                alert('修改成功！')
            },
            function (error) {
                alert(error);
            });
    });

    function getMovieForm() {
        return {
        	id: movieId,
            name: $('#movie-name-input').val(),
            posterUrl: $('#movie-img-input').val(),
            director: $('#movie-director-input').val(),
            screenWriter: $('#movie-writer-input').val(),
            starring: $('#movie-star-input').val(),
            type: $('#movie-type-input').val(),
            country: $('#movie-country-input').val(),
            language: $('#movie-language-input').val(),
            startDate: $('#movie-date-input').val(),
            length: $('#movie-length-input').val(),
            description: $('#movie-description-input').val(),
        };
    }

    function validateMovieForm(data) {
        var isValidate = true;
        if(!data.name) {
            isValidate = false;
            $('#movie-name-input').parent('.form-group').addClass('has-error');
        }
        if(!data.posterUrl) {
            isValidate = false;
            $('#movie-img-input').parent('.form-group').addClass('has-error');
        }
        if(!data.startDate) {
            isValidate = false;
            $('#movie-date-input').parent('.form-group').addClass('has-error');
        }
        if(!data.length) {
        	isValidate = false;
        	$('#movie-length-input').parent('.form-group').addClass('has-error');
        }
        return isValidate;
    }
    
    $("#delete-confirm-btn").click(function () {
    	var url = '/movie/off/batch';
    	var formData = getBatchMovieForm();
    	postRequest(
			url,
			formData,
			function (res) {
				$("#deleteModal").modal('hide');
				$("#delete-btn").attr('disabled', 'disabled');
				$("#delete-btn").html('已下架');
				alert('下架成功!');
			},
			function (error) {
				alert(error);
			});
    });
    
    function getBatchMovieForm() {
        return {
        	movieIdList: [movieId]
        };
    }

});