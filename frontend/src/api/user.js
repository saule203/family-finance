import axios from 'axios'

const BASE_URL = 'http://localhost:8080/api/users'

export function register(data) {
  return axios.post(`${BASE_URL}/register`, data)
}

export function login(data) {
  return axios.post(`${BASE_URL}/login`, data)
}
