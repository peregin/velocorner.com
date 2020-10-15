    $(document).ready(function() {
        setupWeather();
        triggerForecast();
    });

    function setupWeather() {
        $('#weather_button').click(function() {
            triggerForecast();
        });
        var weatherField = $('#weather')
        weatherField.keypress(function(e) {
            if (e.which == 13) {
                triggerForecast()
            }
        });
        weatherField.autocomplete({
            serviceUrl: '/api/weather/suggest',
            onSelect: function (suggestion) {
                console.log('selected location: ' + suggestion.value);
                triggerForecast();
            }
        });
    }

    function triggerForecast() {
        var place = $('#weather').val();
        if (place) {
            console.log('forecast for ' + place);
            weatherForecast(place);
            windyForecast(place);
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

    function windyForecast(place) {
        var windy = $('#windy')
        if (windy) {
            let enabled = windy.attr('windy-enabled');
            let key = windy.attr('windy-key');
            if (enabled === 'true' && key.length > 0) {
                console.log("windy forecast enabled", enabled);
                $.ajax({
                    dataType: 'json',
                    url: "/api/location/geo/" + place,
                    success: function (geo) {
                        windy.css('width','100%');
                        windy.css('height','250px');
                        console.log("windy geo position", geo);
                        const options = {
                            key: key,
                            // Put additional console output or not
                            verbose: false,
                            hourFormat: '12h',
                            // Optional: Initial state of the map
                            lat: geo.latitude,
                            lon: geo.longitude,
                            zoom: 11,
                        };
                        // Initialize Windy API
                        windyInit(options, windyAPI => {
                            // windyAPI is ready, and contain 'map', 'store',
                            // 'picker' and other useful stuff

                            // .map is instance of Leaflet map
                            const { picker, overlays, broadcast, map } = windyAPI;
                            overlays.wind.setMetric('km/h');

                            L.popup()
                                .setLatLng([geo.latitude, geo.longitude])
                                .setContent(place)
                                .openOn(map);

                            // Wait since weather is rendered
                            broadcast.once('redrawFinished', () => {
                                picker.open({ lat: geo.latitude, lon: geo.longitude });
                                // Opening of a picker (async)
                            });
                        });
                    },
                    error: function(e) {
                        windy.css('height','0px');
                        console.error("location not found", place);
                    }
                });
            }
        }
    }