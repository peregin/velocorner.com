    function triggerSearch() {
        var query = $('#search').val();
        console.log('searching for ' + query);
        searchPage(null, query);
    }

    function triggerSuggestion(suggestion) {
        var activity = JSON.parse(suggestion);
        console.log('selected suggestion\'s activity id: ' + activity.id);
        searchPage(activity, null);
    }

    // either suggested activity or queryToSearch
    function searchPage(suggestedActivity, queryToSearch) {
        // open search page if the table is not present
        if ($('#activity-result').length <= 0) {
            console.log('redirecting to the search page');
            var queryParameter = suggestedActivity ? 'aid='+suggestedActivity.id : 'q='+queryToSearch;
            window.open('search?'+queryParameter, '_self');
        } else {
            var activities = suggestedActivity ? Array.of(suggestedActivity) : null;
            startSearch(activities, queryToSearch, null);
        }
    }

    function startSearch(activities, queryToSearch, activityIdToSearch) {
        // mark search in progress
        $('#search-progress-bar').attr('aria-valuenow', 50);
        $('#search-progress-bar').css('width', '50%');
        $('#search-progress-bar').text('50% Searching...');
        $('#search-progress').fadeIn();

        // clean up existing entries
        $('#activity-result tbody tr').remove();

        if (activities) {
            // take suggested activity
            finishSearch(activities);
        } else {
            var urlPath = queryToSearch ? "/api/activities/suggest?query=queryToSearch" : "/api/activities/"+activityIdToSearch
            // trigger asynchronous search
            if (queryToSearch) {
                $.ajax({
                    type: "GET",
                    dataType: "json",
                    url: "/api/activities/suggest?query="+queryToSearch,
                    timeout: 20000,
                    success: function(result) {
                        finishSearch(result.suggestions.map(s => JSON.parse(s.data)));
                    }
                });
            } else {
                $.ajax({
                    type: "GET",
                    dataType: "json",
                    url: "/api/activities/"+activityIdToSearch,
                    timeout: 20000,
                    success: function(result) {
                        finishSearch(Array.of(result));
                    }
                });
            }
        }
    }

    function elapsedInSeconds(secs) {
        var elapsed = moment.duration(secs, "seconds");
        var tenthOfMins = elapsed.minutes() > 9 ? '' : '0';
        return elapsed.hours()+'h:'+tenthOfMins+elapsed.minutes()+'m';
    }

    function wholeValueAndUnit(n, unit) {
        return n ? n.toFixed(0) + ' ' + unit : ""
    }

    function finishSearch(activities) {
        // populate table with the results
        console.log('populating with '+activities.length+' result(s)');
        $.each(activities, function(ix, entry) {
            $("#activity-result tbody").append(
              '<tr><td><a href="http://www.strava.com/activities/'+entry.id+'">'+moment(entry.start_date_local).format('DD MMM, YYYY, ddd HH:mm A')+'</a></td>'+
              '<td class="td-width"><a href="http://www.strava.com/activities/'+entry.id+'">'+entry.name+'</a></td>'+
              '<td class="text-right">'+(entry.distance/1000).toFixed(1)+' km</td>'+
              '<td class="text-right">'+wholeValueAndUnit(entry.total_elevation_gain, 'm')+'</td>'+
              '<td>'+elapsedInSeconds(entry.moving_time)+' / '+elapsedInSeconds(entry.elapsed_time)+'</td>'+
              '<td class="text-right">'+(entry.average_speed*3.6).toFixed(1) +' km/h</td>'+
              '<td class="text-right">'+wholeValueAndUnit(entry.average_watts, 'W')+'</td>'+
              '<td class="text-right">'+wholeValueAndUnit(entry.average_heartrate, 'hr')+'</td>'+
              '<td class="text-right">'+wholeValueAndUnit(entry.average_temp, '˚C')+'</td>'+
              '</tr>');
        });

        // hide search activities
        $('#search-progress-bar').attr('aria-valuenow', 100);
        $('#search-progress-bar').css('width', '100%');
        $('#search-progress-bar').text('Done');
        $('#search-progress').fadeOut();
    }

    $(document).ready(function() {
        var searchField = $('#search')
        searchField.autocomplete({
            serviceUrl: '/api/activities/suggest',
            onSelect: function (suggestion) {
                triggerSuggestion(suggestion.data);
            }
        });
        $('#search_button').click(function() {
            triggerSearch();
        });
        searchField.keypress(function(e) {
            if (e.which == 13) {
                triggerSearch()
            }
        });

        var urlParams = new URLSearchParams(window.location.search);
        if ($('#activity-result') && (urlParams.has('aid') || urlParams.has('q'))) {
            console.log('triggering search or suggestion');
            searchField.val(urlParams.get('q') || "");
            startSearch(null, urlParams.get('q'), urlParams.get('aid'));
        } else {
            // empty landing page, remove the progress bar
            $('#search-progress').hide();
        }
    });