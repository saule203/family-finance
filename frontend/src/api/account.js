import axios from 'axios';

export function getAllAccounts() {
  return axios.get('http://localhost:8080/api/accounts');
}

export function addAccount(data) {
  return axios.post('http://localhost:8080/api/accounts', data);
}
