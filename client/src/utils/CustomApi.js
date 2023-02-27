import axios from 'axios';
import userStore from '../store/userStore';
import memberstore from '../store/memberstore';
import { removeCookie } from '../utils/Cookies';

const server = axios.create({
  headers: {
    'Access-Control-Allow-Origin': 'https://myprojectsite.shop/',
  },
  baseURL: 'https://myprojectsite.shop/',
  withCredentials: true,
});

server.interceptors.request.use(function (config) {
  const user = localStorage.getItem('myId');
  if (user) {
    // 로그인이 되어있는 경우 Access Token을 같이 보낸다
    console.log('로그인 상태');
    config.headers['Authorization'] = localStorage.getItem('accessToken');
  } else {
    console.log('비로그인 상태');
  }
  return config;
});

server.interceptors.response.use(
  function (response) {
    return response;
  },
  async function (error) {
    if (
      error.response?.status === 403 &&
      error.response?.data.errorCode === 'TOKEN_EXPIRE'
    ) {
      try {
        console.log('토큰 만료 - 재발급 진행');
        const originalRequest = error.config;
        const data = await server.post('auth/reissue');
        if (data) {
          localStorage.setItem('accessToken', data.data);
          originalRequest.headers['Authorization'] = data.data;
          return await server.request(originalRequest); // 새로 받은 토큰으로 다시 이전 요청 진행
        }
      } catch (error) {
        console.log('토큰 갱신 에러 발생: ' + error); // 로그아웃 진행
      }
      return Promise.reject(error);
    }
    return Promise.reject(error);
  }
);

export default server;
