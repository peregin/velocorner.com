# ToDo
- add throttling
- restart service for changed images only - or publish image from ci conditionally - orb filter
- caching: add for forecast - generates way too many entries - and remove existing entries
- show last ride and similar ones (+/-15% distance, elevation)
- upgrade postgres (multirange types)
- health check, tiny go service connect uptimerobot with statuspage with a tiny go app
- recommendation for the week and month based on previous or other activities
- caching: setup ActivityStorage with ScalaCache - component search is already cached
- variation of the greeting, instead of Hello
- add QR code to the About page 
- add JMH benchmarks http://openjdk.java.net/projects/xrender/benchmarks.html
- compass rose - wind direction
- marketing campaign FB
- intercom/zendesk (or alternative) for features
- show consecutive sport days
- add last activity details in the profile (and similar rides)
- add monthly - current month (next to yearly) achievements
- add where to ride section
- add gps overlay over video section
- add garmin data field section
- PubSub (pusher or alternative) for updates

# InProgress
- gears and usage YTD - added to OAuth2
- return JWT after login
- implement JWT between Frontend and Backend

# Not sure if adds value
- map with origins and destinations flagged
- add 3D profile of the last activity gpx

# Done
- add and collect events for search
- show the time spent in search
- top 10 longest rides, top 10 most elevations 
- show current min/max temperature
- speedup node docker image creation
- show current temperature
- replace scala specs with scala test
- increase session timeout for login token
- use https://uptimerobot.com/ for monitoring and notifying about downtime
- use https://stackshare.io/companies/velocorner - added to About page
- demo profile heatmap when logged out
- use status page (https://velocorner.statuspage.io/)
- demo profile word clouds when logged out
- sitemap to help seo
- word clouds (for activity titles, descriptions)
- distribution heatmap, show the range instead of a single value
- improve landing page when not logged in: demonstrate what the site could do for you
  * detect rough location and show the weather and wind information for logged out users (when is not in the cookie)
  * show stats with sample data (heatmap, ytd)
- add years tab to the profile ytd
- units of measurement (attached to the user account)
- show active days in the last 12 months (in the current year)
- make calendar heatmap responsive
- show calendar for all activity types
- use optimized activity query for a given range
- add calendar heatmap
- same height boxes in the profile
- add active days stats
- add Merry Christmas snackbar during the holidays
- make windy frame expandable, do not show it by default (has been moved to the bottom)
- implement refresh token after access token expires
- persist gears when retrieving provider user
- distinguish the provider between consumer users for OAuth2
- Account holds Strava and Withings related info in different structures
