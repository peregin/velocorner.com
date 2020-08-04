    $(document).ready(function() {
        $('#weather_button').click(function() {
            triggerForecast();
        });
        $('#weather').keypress(function(e) {
            if (e.which == 13) {
                triggerForecast()
            }
        });

        triggerForecast();
    });

    function triggerForecast() {
        var place = $('#weather').val();
        if (place) {
            console.log('forecast for ' + place);
            weatherForecast(place);
        } else {
            console.log("won't request weather forecast, place is not set");
        }
    }

    function weatherForecast(place) {
        $('#weather-progress').css("visibility","visible");

        // meteogram
        $.ajax({
            dataType: 'xml',
            url: "/api/weather/forecast/" + place + "?mode=xml",
            success: function (xml) {
                $('#weather-progress').css("visibility","hidden");
                $('#weather-container').css('display', 'block');
                window.meteogram = new Meteogram(xml, 'weather-container');
            },
            error: function(e) {
                $('#weather-progress').css("visibility","hidden");
                console.error("meteogram with " + e);
            }
        });

        // sunrise and sunset
        $.ajax({
            dataType: 'json',
            url: "/api/weather/sunrise/" + place,
            success: function (data) {
                $('#sunrise-sunset').css("visibility","visible");
                $('#weather-sunrise').html(moment.unix(data.sunrise).format('H:mm'));
                $('#weather-sunset').html(moment.unix(data.sunset).format('H:mm'));
            }
        });

    }