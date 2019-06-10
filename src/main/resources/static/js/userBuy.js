$(document).ready(function () {
    getMovieList();

    function getMovieList() {
        getRequest(
            '/ticket/get/' + sessionStorage.getItem('id'),
            function (res) {
                renderTicketList(res.content);
            },
            function (error) {
                alert(error);
            });
    }

    // TODO:填空
    function renderTicketList(list) {
    	$('.ticket-on-table').empty();
        var ticketDomStr = "";
        list.forEach(function (ticket) {
        	ticketDomStr +=
        		"<tr>" +
            	"<td>" + ticket.movieName + "</td>" +
            	"<td>" + ticket.hallName + "</td>" +
        		"<td>" + getSeats(ticket.seats) + "</td>" +
       			"<td>" + getDate(ticket.startTime) + "</td>" +
       			"<td>" + getDate(ticket.endTime) + "</td>" +
       			"<td>" + (ticket.state == 1 ? "已完成" : (!ticket.state ? "未完成" : "已失效")) + "</td>" +
                "</tr>";
        });
        $('.ticket-on-table').append(ticketDomStr);
    }
    
    function getDate(time) {
    	var date = new Date(time);
    	var res = formatDate(date);
    	res += " " + formatTime(date);
    	return res;
	}
    
    function getSeats(list) {
    	var res = "";
    	list.forEach(function (seat) {
    		res += (seat.rowIndex+1) + "排"
    		res += (seat.columnIndex+1) + "座";
    	});
    	return res;
    }

});