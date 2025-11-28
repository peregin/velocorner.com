const apiHost = import.meta.env.VITE_API_HOST || 'https://velocorner.com';
const webHost = import.meta.env.VITE_WEB_HOST || 'https://dev.velocorner.com';
console.info('api host:', apiHost);
console.info('web host:', webHost);

function request(method, path, data) {
  const token = localStorage.getItem('access_token');
  const options = {
    method,
    headers: {
      'Accept': 'application/json',
      ...(method === 'PUT' && { 'Content-Type': 'application/json' }),
      ...(token && { 'Authorization': `Bearer ${token}` })
    },
    cache: 'no-cache',
    mode: 'cors',
    ...(data && { body: JSON.stringify(data) })
  };
  
  return fetch(apiHost + path, options)
    .then(response => {
      if (response.status >= 200 && response.status < 300) return response;
      // logout when not authenticated and go to the landing page
      if (response.status === 401) {
        localStorage.removeItem('access_token');
        window.location.href = `${webHost}`;
        return;
      }
      console.log(`status error ${response.status} - ${response.statusText}`);
      const error = new Error(`HTTP Error ${response}`);
      error.status = response.statusText;
      error.response = response;
      throw error;
    })
    .then(r => r.json());
}

const get = (path) => request('GET', path);
const put = (path, data) => request('PUT', path, data);

const endpoints = {
  // Status and health
  status: () => get('/api/status'),
  ping: () => get('/api/ping'),
  logout: () => get('/api/logout/strava'),
  
  // Activities
  wordcloud: () => get('/api/activities/wordcloud'),
  activityTypes: () => get('/api/activities/types'),
  activityYears: (activity) => get(`/api/activities/${activity}/years`),
  lastActivity: () => get('/api/activities/last'),
  getActivity: (id) => get(`/api/activities/${id}`),
  suggestActivities: (query) => get(`/api/activities/suggest?query=${encodeURIComponent(query)}`),
  
  // Athlete statistics
  profileStatistics: (activity, year) => get(`/api/athletes/statistics/profile/${activity}/${year}`),
  yearlyHeatmap: (activity) => get(`/api/athletes/statistics/yearly/heatmap/${activity}`),
  yearlyStatistics: (action, activity) => get(`/api/athletes/statistics/yearly/${action}/${activity}`),
  ytdStatistics: (action, activity) => get(`/api/athletes/statistics/ytd/${action}/${activity}`),
  dailyStatistics: (action) => get(`/api/athletes/statistics/daily/${action}`),
  yearlyHistogram: (action, activity) => get(`/api/athletes/statistics/histogram/${action}/${activity}`),
  topActivities: (action, activity) => get(`/api/athletes/statistics/top/${action}/${activity}`),
  achievements: (activity) => get(`/api/athletes/statistics/achievements/${activity}`),
  
  // Athlete profile
  athleteProfile: () => get('/api/athletes/me'),
  
  // Demo endpoints
  demoWordcloud: () => get('/api/demo/wordcloud'),
  demoYearlyStatistics: (action, activity) => get(`/api/demo/statistics/yearly/${action}/${activity}`),
  demoYtdStatistics: (action, activity) => get(`/api/demo/statistics/ytd/${action}/${activity}`),
  demoDailyStatistics: (action) => get(`/api/demo/statistics/daily/${action}`),
  demoYearlyHistogram: (action, activity) => get(`/api/demo/statistics/histogram/${action}/${activity}`),
  
  // Profile
  updateUnits: (unit) => put(`/api/athletes/units/${unit}`)
};

export default { apiHost, webHost, ...endpoints };
