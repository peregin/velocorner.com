
const apiHost = process.env.API_HOST || 'https://velocorner.com' // doesn't work!!!
console.info('api host: ' + apiHost)
console.info('env: ' + JSON.stringify(process.env))

function status() {
  const requestOptions = {
    method: 'GET',
    accept: 'application/json',
    cache: 'no-cache'
  }
  return fetch(apiHost + '/api/status', requestOptions)
    .then(checkStatus)
    .then(r => r.json())
}

function search(term) {
  const requestOptions = {
    method: 'GET',
    accept: 'application/json',
    cache: 'no-cache'
  }
  return fetch(apiHost + '/api/products/search?query=' + term, requestOptions)
    .then(checkStatus)
    .then(r => r.json())
}

function markets() {
  const requestOptions = {
    method: 'GET',
    accept: 'application/json'
  }
  return fetch(apiHost + '/api/products/markets', requestOptions)
    .then(checkStatus)
    .then(r => r.json())
}

function login() {
  const requestOptions = {
    method: 'GET',
    accept: "application/json"
  }
  return fetch('/api/login/strava', requestOptions)
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
