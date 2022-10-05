    function triggerSearch() {
        var query = $('#search_brand').val();
        console.log('searching for ' + query);
        searchPage(query);
    }

    // start searching for the query term
    function searchPage(queryToSearch) {
        // mark search in progress TODO

        // clean up existing entries
        $('#brand-result .inner-results').remove();

        // trigger asynchronous search
        $.ajax({
            type: "GET",
            dataType: "json",
            url: "/api/brands/search?query="+queryToSearch,
            timeout: 20000,
            success: function(result) {
                finishSearch(result);
            }
        });
    }

    function finishSearch(markets) {
        // populate table with the results
        console.log('populating with '+markets.length+' result(s)');
        var resultText = markets.length == 1 ? 'result' : 'results';
        $('#results-number').text(markets.length+' '+resultText);

        $.each(markets, function(ix, entry) {
            $("#brand-result").append(
              '<div class="inner-results">'+
              '    <hr>'+
              '    <h3><a href="'+entry.marketplace.url+'">'+entry.marketplace.name+'</a></h3>'+
              '    <div class="overflow-h">'+
              '       <img src="'+entry.marketplace.logoUrl+'" alt="">'+
              '       <div class="overflow-a">'+
              '           <a href="'+entry.url+'">'+entry.brand.name+'</a>'+
              '       </div>'+
              '    </div>'+
              '<div>');
        });

        // hide search in progress TODO
    }

    $(document).ready(function() {
        var searchField = $('#search_brand')
        if (searchField.length) {
            searchField.autocomplete({
                serviceUrl: '/api/brands/suggest',
                onSelect: function (suggestion) {
                    searchPage(suggestion.data);
                },
                minChars: 1,
                noCache: true
            });
            $('#search_button_brand').click(function() {
                triggerSearch();
            });
            searchField.keypress(function(e) {
                if (e.which == 13) {
                    triggerSearch()
                }
            });

            // empty landing page, remove results counter
            //$('#results-number').hide();
        }
    });