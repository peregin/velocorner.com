


function getStatus(cb) {
  const requestOptions = {
    method: 'GET',
    accept: "application/json",
    headers: authHeader()
  }
  return fetch('/api/status', requestOptions)
    .then(checkStatus)
    .then(r => r.json())
    .then(cb)
}

function test() {
  let url = 'https://www.strava.com/api/v3/oauth/authorize?client_id=4486&redirect_uri=http%3A%2F%2Flocalhost%3A9001%2Fauthorize%2Fstrava&response_type=code&approval_prompt=auto&scope=read%2Cactivity%3Aread'
  let resp = window.location = url
  console.log(resp)
  //window.location = '/'
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

// Utility functions
function authHeader() {
  // return authorization header with jwt token
  const currentUser = {
    token: 'jwtToken'
  }
  if (currentUser && currentUser.token) {
    return { 'Authorization': `Bearer ${currentUser.token}` }
  } else {
    return {}
  }
}

function checkStatus(response) {
  if (response.status >= 200 && response.status < 300) {
    return response;
  }
  const error = new Error(`HTTP Error ${response.statusText}`)
  error.status = response.statusText;
  error.response = response;
  console.log(error);
  throw error;
}

const ApiClient = {
  getStatus: getStatus,
  login: login,
  test: test,
}
export default ApiClient;
