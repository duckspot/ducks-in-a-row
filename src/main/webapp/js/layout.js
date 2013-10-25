// run when document ready
$(function() {
    
    // pass in globals for $ and DateUtil
    (function ($, DateUtil) {

            /**
	     * Change an empty table into a clickable calendar.
	     * @param {type} date
	     */
	    $.fn.calendar = function (date, callback) {
	        var calendarTable = this,
	        html = DateUtil.calendarHtml(date);
	        // set html
	        calendarTable.html(html);
	        calendarTable.click(function (eventObject) {
	            var targetId = eventObject.target.id,
	                newDate = new Date();
	            if (targetId.slice(0, 5) === "date_") {
	                newDate.setTime(parseInt(targetId.slice(5), 10));
	                html = DateUtil.calendarHtml(newDate);
	                calendarTable.html(html);
	                if (typeof(callback) === "function") {
	                    callback(newDate);
	                }
	            }
	        });
	    };

            // setup clock variables in enclosure	    
            var i18n = { dayOfWeek: "EEEE,", date: "MMMM d, yyyy", time: "h:mm:ss a" },
                seconds = -1,
                date = -1;

            /**
	     * Update fields in #clock .time, .dayOfWeek, and .date, and 
	     * setTimeout to repeat in 500 ms.  Don't update if clock unchanged.
	     */
	    function updateClock() {
	        var now = new Date(),
	        ms = 1000 - now.getMilliseconds();
	        if (now.getSeconds() != seconds) {
	            seconds = now.getSeconds();
	            $("#clock").find(".time").html(DateUtil.formatDate(now, i18n.time));
	            if (now.getDate() != date) {
	                $("#clock")
	                    .find(".dayOfWeek")
	                    .html(DateUtil.formatDate(now, i18n.dayOfWeek))
	                    .end()
	                    .find(".date")
	                    .html(DateUtil.formatDate(now, i18n.date));
	            }
	        }
	        setTimeout(function () { updateClock(); }, ms);
	    }

	    // start the clock
	    updateClock();

            // click on clock navigates to today
            $("#clock").click(function() {
                window.location.href = "/day";
            });

	    // setup day Title
	    $("#dayTitle").html(DateUtil.formatDate(new Date(),"EEEE, MMMM d, yyyy"));

	    // setup calendar and have it update day title
	    $("#calendar").calendar(new Date(), function (date) {
	        $("#dayTitle").html(DateUtil.formatDate(date,"EEEE, MMMM d, yyyy"));
                window.location.href = "/day/"+date.getTime();
	    });

	// Packages used
    }(jQuery, DateUtil));
});