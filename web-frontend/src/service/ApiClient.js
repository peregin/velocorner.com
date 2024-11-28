// react inject variables starting with REACT_APP_ to client side
const apiHost = process.env.API_HOST || process.env.REACT_APP_API_HOST || 'https://velocorner.com';
console.info('api host: ' + apiHost);

function status() { return get('/api/status'); }

function search(term) { return get(`/api/products/search?query=${term}`); }

function markets() { return get('/api/products/markets'); }

function wordcloud() { return get('/api/activities/wordcloud'); }


const getOptions = {
  method: 'GET',
  accept: 'application/json',
  cache: 'no-cache',
  mode: 'cors'
}

function get(path) {
  let token = localStorage.getItem('access_token');
  let options = token ? { ...getOptions, ...{
    headers: {
      'Authorization': `Bearer ${token}`
    }
  }} : getOptions;
  return fetch(apiHost + path, options)
    .then(checkStatus) // todo: .catch(error...)
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
  status: status,
  search: search,
  markets: markets,
  wordcloud: wordcloud
}
export default ApiClient;
