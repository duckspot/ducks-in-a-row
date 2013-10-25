DateUtil = (function () {

    'use strict';

    var constants = {
        msInHour: 1000 * 60 * 60,
        msInDay:  1000 * 60 * 60 * 24
    };

    /**
     * Values that may change based on Locale.
     * @type type
     */
    var i18n = {
        weekStarts: 0,   // 0 - means week starts on Sunday        
        dayNames: [
            'Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday',
            'Saturday'
        ],
        monthNames: [
            'January', 'February', 'March', 'April', 'May', 'June', 'July',
            'August', 'September', 'October', 'November', 'December'
        ]
    };

    /**
     * Creates a fixed width string from value, padding on the left with the fill 
     * character (default '0') or truncating on the left, so that it is length 
     * characters long.
     * 
     * @param value
     * @param {Number} length
     * @param {String} fill
     * @returns {String}
     * code from jQuery In Action, Second Edition, pg 212
     */
    function toFixedWidth(value, length, fill) {
        var result = (value || '').toString(),
            padding = length - result.length,
            n;
        fill = fill || '0';
        if (padding < 0) {
            result = result.substr(-padding);
        } else {
            for (n = 0; n < padding; n += 1) {
                result = fill + result;
            }
        }
        return result;
    }

    /**
     * Formats date accoring to pattern.
     * 
     * @param {Date} date
     * @param {String} pattern
     * @returns {String} formatted date
     */
    function formatDate(date, pattern) {
        var result = [],
            matched;

        while (pattern.length > 0) {
            formatDate.patternParts.lastIndex = 0;
            matched = formatDate.patternParts.exec(pattern);
            if (matched) {
                result.push(
                    formatDate.patternValue[matched[0]].call(this, date)
                );
                pattern = pattern.slice(matched[0].length);
            } else {
                result.push(pattern.charAt(0));
                pattern = pattern.slice(1);
            }
        }
        return result.join('');
    }

    formatDate.patternParts =
        /^(yy(yy)?|M(M(M(M)?)?)?|d(d)?|EEE(E)?|a|H(H)?|h(h)?|m(m)?|s(s)?|S)/;

    formatDate.patternValue = {
        yy: function (date) {
            return toFixedWidth(date.getFullYear(), 2);
        },
        yyyy: function (date) {
            return date.getFullYear().toString();
        },
        MMMM: function (date) {
            return i18n.monthNames[date.getMonth()];
        },
        MMM: function (date) {
            return i18n.monthNames[date.getMonth()].substr(0, 3);
        },
        MM: function (date) {
            return toFixedWidth(date.getMonth() + 1, 2);
        },
        M: function (date) {
            return date.getMonth() + 1;
        },
        dd: function (date) {
            return toFixedWidth(date.getDate(), 2);
        },
        d: function (date) {
            return date.getDate();
        },
        EEEE: function (date) {
            return i18n.dayNames[date.getDay()];
        },
        EEE: function (date) {
            return i18n.dayNames[date.getDay()].substr(0, 3);
        },
        HH: function (date) {
            return toFixedWidth(date.getHours(), 2);
        },
        H: function (date) {
            return date.getHours();
        },
        hh: function (date) {
            var hours = date.getHours() % 12;
            return toFixedWidth(hours === 0 ? 12 : hours, 2);
        },
        h: function (date) {
            var hours = date.getHours() % 12;
            return hours === 0 ? 12 : hours;
        },
        mm: function (date) {
            return toFixedWidth(date.getMinutes(), 2);
        },
        m: function (date) {
            return date.getMinutes();
        },
        ss: function (date) {
            return toFixedWidth(date.getSeconds(), 2);
        },
        s: function (date) {
            return date.getSeconds();
        },
        S: function (date) {
            return toFixedWidth(date.getMilliseconds(), 3);
        },
        A: function (date) {
            return date.getHours() < 12 ? 'AM' : 'PM';
        },
        a: function (date) {
            return date.getHours() < 12 ? 'am' : 'pm';
        }
    };

    /**
     * Copy a date value, and blank our the hours, minutes, etc.
     * 
     * @param {type} date
     * @returns {Date}
     */
    function copyDate(date) {
        var dt = new Date(date);
        dt.setHours(0);
        dt.setTime(Math.floor(dt / constants.msInHour) * constants.msInHour);
        return dt;
    }

    /**
     * Number of blank squares on first row a months calendar.
     * 
     * @param {Date} date
     * @returns {Number}
     */
    function daysToSkip(date) {

        var dt = copyDate(date),
            result = 7;

        dt.setDate(1);
        while (dt.getDay() != i18n.weekStarts) {
            result -= 1;
            dt.setTime(dt.getTime() + constants.msInDay);
        }

        return result % 7;
    }

    /**
     * The number of days in a month.
     * @param {type} date
     * @returns {Number}
     */
    function daysInMonth(date) {
        var dt = copyDate(date),
            month = dt.getMonth();
        dt.setDate(1);
        if (month === 11) {
            dt.setYear(dt.getYear() + 1);
            dt.setMonth(0);
        } else {
            dt.setMonth(month + 1);
        }
        dt.setTime(dt.getTime() - constants.msInDay);
        return dt.getDate();
    }

    function monthYear(date) {
        var dt = copyDate(date),
            month = dt.getMonth(),
            year = dt.getFullYear();
        return i18n.monthNames[month] + ' ' + year;
    }

    function calendarHtml(date) {
        var dt = copyDate(date),
            selDay = dt.getDate(),
            skip = daysToSkip(date),
            lastDay = daysInMonth(date),
            today = copyDate(new Date()).getTime(),
            html = '',
            day = 0,
            week = 1,
            dayOfWeek = 0,
            classes = '';

        // month and year first row of table
        html += '<tr><td class="previousMonth">&lt;</td>' +
            '<td colspan="5" class="monthYear">' + monthYear(dt) +
            '</td><td class="nextMonth">&gt;</td></tr>\n<tr>';

        // days of week second row of table
        i18n.dayNames.forEach(function (dayName) {
            html += '<td class="dayName">' + dayName.slice(0, 3) + '</td>';
        });
        html += '</tr>\n';

        // days of the month
        while (day <= lastDay) {
            html += '<tr>';
            for (dayOfWeek = 0; dayOfWeek < 7 && day <= lastDay;
                    dayOfWeek += 1) {
                if (week === 1 && dayOfWeek < skip) {
                    html += '<td></td>';
                    day = 0; // all blank squares are days before 1
                } else {
                    dt.setDate(day);
                    if (dt.getTime() === today) {
                        if (day === selDay) {
                            classes = 'day today selected';
                        } else {
                            classes = 'day today';
                        }
                    } else {
                        if (day === selDay) {
                            classes = 'day selected';
                        } else {
                            classes = 'day';
                        }
                    }
                    html += '<td id="date_' + dt.getTime() + '" class="' +
                        classes + '">' + toFixedWidth(dt.getDate(), 2) +
                        '</td>';
                }
                day += 1;
            }
            html += '</tr>\n';
            week += 1;
        }
        return html;
    }

    // exports to DateUtil
    return {
        formatDate: formatDate,
        calendarHtml: calendarHtml
    };

}());