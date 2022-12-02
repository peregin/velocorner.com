// react inject variables starting with REACT_APP_ to client side
const apiHost = process.env.API_HOST || process.env.REACT_APP_API_HOST || 'https://velocorner.com'
console.info('api host: ' + apiHost)

const requestOptionsGet = {
  method: 'GET',
  accept: 'application/json',
  cache: 'no-cache'
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
  return fetch('/api/login/strava', requestOptionsGet)
    .then(checkStatus)
    .then(r => r.text())
    .then(a => console.log(`LOGIN response[${a}]`))
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
