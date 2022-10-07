    function triggerSearch() {
        var query = $('#search_best').val();
        console.log('searching for ' + query);
        searchPage(query);
    }

    // start searching for the query term
    function searchPage(queryToSearch) {
        // mark search in progress TODO

        // clean up existing entries
        $('#best-result .inner-results').remove();

        // trigger asynchronous search
        $.ajax({
            type: "GET",
            dataType: "json",
            url: "/api/products/search?query="+queryToSearch,
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
            $("#best-result").append(
              `    <div class="col-md-3 col-sm-6 md-margin-bottom-30">
                       <div class="product-img">
                           <a href="${entry.productUrl}"><img class="full-width img-responsive" src="${entry.imageUrl}" alt="Product"></a>
                           <a class="product-review" href="${entry.productUrl}">Quick review</a>
                           <a class="add-to-cart" href="${entry.productUrl}"><i class="fa fa-shopping-cart"></i>Visit shop</a>
                           <!-- <div class="shop-rgba-red rgba-banner">Out of stock</div>-->
                           <div class="shop-rgba-dark-green rgba-banner">New</div>
                       </div>
                       <div class="product-description product-description-brd">
                           <div class="overflow-h margin-bottom-5">
                               <div class="pull-left">
                                   <h4 class="title-price"><a href="${entry.imageUrl}">${entry.name}</a></h4>
                                   <span class="gender text-uppercase">${entry.brand.name}</span>
                                   <span class="gender">${entry.description}</span>
                               </div>
                               <div class="product-price">
                                   <span class="title-price">${entry.price.value} ${entry.price.currency}</span>
                                   <!-- <span class="title-price line-through">$99.00</span> -->
                               </div>
                           </div>
                           <ul class="list-inline product-ratings">
                               <li><i class="${entry.reviewStars > 0 ? 'rating-selected' : 'rating'} fa fa-star"></i></li>
                               <li><i class="${entry.reviewStars > 1 ? 'rating-selected' : 'rating'} fa fa-star"></i></li>
                               <li><i class="${entry.reviewStars > 2 ? 'rating-selected' : 'rating'} fa fa-star"></i></li>
                               <li><i class="${entry.reviewStars > 3 ? 'rating-selected' : 'rating'} fa fa-star"></i></li>
                               <li><i class="${entry.reviewStars > 4 ? 'rating-selected' : 'rating'} fa fa-star"></i></li>
                               <li class="like-icon"><a data-original-title="Add to wishlist" data-toggle="tooltip" data-placement="left" class="tooltips" href="#"><i class="fa fa-check"></i></a></li>
                           </ul>
                       </div>
                   </div>`);
        });

        // hide search in progress TODO
    }

    $(document).ready(function() {
        var searchField = $('#search_best')
        if (searchField.length) {
            searchField.autocomplete({
                serviceUrl: '/api/products/suggest',
                onSelect: function (suggestion) {
                    searchPage(suggestion.data);
                },
                minChars: 1,
                noCache: true
            });
            $('#search_button_best').click(function() {
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