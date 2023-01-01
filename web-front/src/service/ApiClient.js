// react inject variables starting with REACT_APP_ to client side
const apiHost = process.env.API_HOST || process.env.REACT_APP_API_HOST || 'https://velocorner.com'
console.info('api host: ' + apiHost)

const requestOptionsGet = {
  method: 'GET',
  accept: 'application/json',
  cache: 'no-cache',
  mode: 'cors'
}

function status() {
  return fetch(apiHost + '/api/status', requestOptionsGet)
    .then(checkStatus)
    .then(r => r.json())
}

function search(term) {
  return fetch(apiHost + '/api/products/search?query=' + term, requestOptionsGet)
    .then(checkStatus)
    .then(r => r.json())
}

function markets() {
  return fetch(apiHost + '/api/products/markets', requestOptionsGet)
    .then(checkStatus)
    .then(r => r.json())
}

function login() {
  let callback = encodeURI('http://localhost:3000/oauth/strava');
  let scope = encodeURI('read,activity:read,profile:read_all');
  let url = 'https://www.strava.com/api/v3/oauth/authorize?client_id=4486&redirect_uri='+callback+'&response_type=code&approval_prompt=auto&scope='+scope;
  window.location = url;
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
  status: status,
  search: search,
  markets: markets,
  login: login,
}
export default ApiClient;
