import axios from 'axios';

export function getAllCategories() {
  return axios.get('http://localhost:8080/api/categories');
}

export function addCategory(data) {
  return axios.post('http://localhost:8080/api/categories', data);
}
