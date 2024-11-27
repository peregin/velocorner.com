    $(document).ready(function() {
        setupWeather();
        triggerForecast(false);
    });

    function setupWeather() {
        $('#weather_button').click(function() {
            triggerForecast(true);
        });
        var weatherField = $('#weather')
        weatherField.keypress(function(e) {
            if (e.which == 13) {
                triggerForecast(true)
            }
        });
        weatherField.autocomplete({
            serviceUrl: '/api/location/suggest',
            onSelect: function (suggestion) {
                console.log('selected location: ' + suggestion.value);
                triggerForecast(true);
            }
        });
    }

    function triggerForecast(clicked) {
        var place = $('#weather').val();
        if (place) {
            forecast(place, clicked);
        } else {
            // detect capital
            $.ajax({
                dataType: 'json',
                url: "https://weather.velocorner.com/location/ip",
                success: function (data) {
                    let location = data.city+', '+data.country;
                    console.log('detected location is ' + location);
                    $('#weather').val(location);
                    forecast(location, clicked);
                }
            });
            console.log("won't request weather forecast, place is not set");
        }
    }

    function forecast(place, clicked) {
        console.log('forecast for ' + place);
        weatherForecast(place);
        windyForecast(place);
        analytics.track('Weather', {
            location: place,
            clicked: clicked
        });
    }

    function weatherForecast(place) {
        $('#weather-progress').css("visibility", "visible");

        // meteogram
        $.ajax({
            dataType: 'xml',
            url: "/api/weather/forecast/" + place,
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

        // current weather, sunrise and sunset
        $.ajax({
            dataType: 'json',
            url: "/api/weather/current/" + place,
            success: function (data) {
                $('#sunrise-sunset').css("visibility","visible");
                $('#temperature').css("visibility","visible");
                $('#weather-sunrise').html(moment.unix(data.sunriseSunset.sunrise).format('H:mm'));
                $('#weather-sunset').html(moment.unix(data.sunriseSunset.sunset).format('H:mm'));
                $('#weather-temperature').html(data.info.temp.toFixed(1));
                $('#weather-icon').attr('class', data.bootstrapIcon);
                $('#weather-icon').attr('title', data.current.description);
                $('#weather-temperature').attr('title', data.current.description); // tooltip text
                $('[data-toggle="weather-tooltip"]').tooltip(); // enable tooltip
                $('#weather-temperature-min').html(data.info.temp_min.toFixed(0));
                $('#weather-temperature-max').html(data.info.temp_max.toFixed(0));
            },
            error: function(e) {
                $('#sunrise-sunset').css("visibility","hidden");
                $('#temperature').css("visibility","hidden");
                console.error(`current weather location [${place}] not found`);
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