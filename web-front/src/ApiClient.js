


function getStatus(cb) {
  const requestOptions = {
    method: 'GET',
    accept: "application/json",
    headers: authHeader()
  }
  return fetch('/api/status', requestOptions)
    .then(checkStatus)
    .then(parseJSON)
    .then(cb);
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
  const error = new Error(`HTTP Error ${response.statusText}`);
  error.status = response.statusText;
  error.response = response;
  console.log(error);
  throw error;
}

const parseJSON = (response) => response.json()

const ApiClient = {
  getSummary: getStatus
};
export default ApiClient;
