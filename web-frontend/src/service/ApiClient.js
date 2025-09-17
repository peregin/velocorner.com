// vite inject variables starting with VITE_ to client side
const apiHost = import.meta.env.VITE_API_HOST || 'https://velocorner.com';
console.info('api host: ' + apiHost);
const webHost = import.meta.env.VITE_WEB_HOST || 'https://dev.velocorner.com';
console.info('web host: ' + webHost);

// Status and health endpoints
function status() { return get('/api/status'); }
function ping() { return get('/api/ping'); }

// Activity endpoints
function wordcloud() { return get('/api/activities/wordcloud'); }
function activityTypes() { return get('/api/activities/types'); }
function activityYears(activity) { return get(`/api/activities/${activity}/years`); }
function lastActivity() { return get('/api/activities/last'); }
function getActivity(id) { return get(`/api/activities/${id}`); }
function suggestActivities(query) { return get(`/api/activities/suggest?query=${encodeURIComponent(query)}`); }

// Athlete statistics endpoints
function profileStatistics(activity, year) { return get(`/api/athletes/statistics/profile/${activity}/${year}`); }
function yearlyHeatmap(activity) { return get(`/api/athletes/statistics/yearly/heatmap/${activity}`); }
function yearlyStatistics(action, activity) { return get(`/api/athletes/statistics/yearly/${action}/${activity}`); }
function ytdStatistics(action, activity) { return get(`/api/athletes/statistics/ytd/${action}/${activity}`); }
function dailyStatistics(action) { return get(`/api/athletes/statistics/daily/${action}`); }
function yearlyHistogram(action, activity) { return get(`/api/athletes/statistics/histogram/${action}/${activity}`); }
function topActivities(action, activity) { return get(`/api/athletes/statistics/top/${action}/${activity}`); }
function achievements(activity) { return get(`/api/athletes/statistics/achievements/${activity}`); }

// Demo endpoints
function demoWordcloud() { return get('/api/demo/wordcloud'); }
function demoYearlyStatistics(action, activity) { return get(`/api/demo/statistics/yearly/${action}/${activity}`); }
function demoYtdStatistics(action, activity) { return get(`/api/demo/statistics/ytd/${action}/${activity}`); }
function demoDailyStatistics(action) { return get(`/api/demo/statistics/daily/${action}`); }
function demoYearlyHistogram(action, activity) { return get(`/api/demo/statistics/histogram/${action}/${activity}`); }

// Brand and product endpoints
function searchBrands(query) { return get(`/api/brands/search?query=${encodeURIComponent(query)}`); }
function suggestBrands(query) { return get(`/api/brands/suggest?query=${encodeURIComponent(query)}`); }
function searchProducts(query) { return get(`/api/products/search?query=${encodeURIComponent(query)}`); }
function suggestProducts(query) { return get(`/api/products/suggest?query=${encodeURIComponent(query)}`); }
function markets() { return get('/api/products/markets'); }

// Profile endpoints
function updateUnits(unit) { return put(`/api/athletes/units/${unit}`); }

const getOptions = {
  method: 'GET',
  accept: 'application/json',
  cache: 'no-cache',
  mode: 'cors'
}

const putOptions = {
  method: 'PUT',
  accept: 'application/json',
  cache: 'no-cache',
  mode: 'cors',
  headers: {
    'Content-Type': 'application/json'
  }
}

function get(path) {
  let token = localStorage.getItem('access_token');
  let options = token ? { ...getOptions, ...{
    headers: {
      ...getOptions.headers,
      'Authorization': `Bearer ${token}`
    }
  }} : getOptions;
  return fetch(apiHost + path, options)
    .then(checkStatus)
    .then(r => r.json())
}

function put(path, data) {
  let token = localStorage.getItem('access_token');
  let options = token ? { ...putOptions, ...{
    headers: {
      ...putOptions.headers,
      'Authorization': `Bearer ${token}`
    }
  }} : putOptions;
  
  if (data) {
    options.body = JSON.stringify(data);
  }
  
  return fetch(apiHost + path, options)
    .then(checkStatus)
    .then(r => r.json())
}

function checkStatus(response) {
  if (response.status >= 200 && response.status < 300) {
    return response;
  }
  console.log(`status error ${response.status} - ${response.statusText}`)
  const error = new Error(`HTTP Error ${response}`);
  error.status = response.statusText;
  error.response = response;

  throw error;
}

const ApiClient = {
  apiHost: apiHost,
  
  // Status and health
  status: status,
  ping: ping,
  
  // Activities
  wordcloud: wordcloud,
  activityTypes: activityTypes,
  activityYears: activityYears,
  lastActivity: lastActivity,
  getActivity: getActivity,
  suggestActivities: suggestActivities,
  
  // Athlete statistics
  profileStatistics: profileStatistics,
  yearlyHeatmap: yearlyHeatmap,
  yearlyStatistics: yearlyStatistics,
  ytdStatistics: ytdStatistics,
  dailyStatistics: dailyStatistics,
  yearlyHistogram: yearlyHistogram,
  topActivities: topActivities,
  achievements: achievements,
  
  // Demo data
  demoWordcloud: demoWordcloud,
  demoYearlyStatistics: demoYearlyStatistics,
  demoYtdStatistics: demoYtdStatistics,
  demoDailyStatistics: demoDailyStatistics,
  demoYearlyHistogram: demoYearlyHistogram,
  
  // Brands and products
  searchBrands: searchBrands,
  suggestBrands: suggestBrands,
  searchProducts: searchProducts,
  suggestProducts: suggestProducts,
  markets: markets,
  
  // Profile
  updateUnits: updateUnits
}

export default ApiClient;
