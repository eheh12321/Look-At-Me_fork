import axios from 'axios';

const server = axios.create({
  baseURL: 'https://myprojectsite.shop/',
});

server.interceptors.request.use(function (config) {
  const user = localStorage.getItem('myId');
  if (!user) {
    // 로그인 되어있지 않는 경우
    console.log('비로그인 상태');
    config.headers['Authorization'] = null;
    config.headers['Refresh'] = null;
    return config;
  }
  // 로그인이 되어있는 경우 Access Token을 같이 보낸다
  console.log('로그인 상태');
  config.headers['Authorization'] = localStorage.getItem('accessToken');
  return config;
});

server.interceptors.response.use(
  function (response) {
    return response;
  },
  async function (error) {
    if (error.response?.status === 403) {
      try {
        console.log('토큰 만료 - 재발급 진행');
        const originalRequest = error.config;
        // 토큰 재발급을 위해 refresh token 같이 전달
        const data = await server.post(
          'auth/reissue',
          {},
          {
            headers: { Refresh: localStorage.getItem('refreshToken') },
          }
        );
        if (data) {
          localStorage.setItem('accessToken', data.data);
          originalRequest.headers['Authorization'] = data.data;
          return await server.request(originalRequest); // 새로 받은 토큰으로 다시 이전 요청 진행
        }
      } catch (error) {
        console.log('토큰 갱신 에러 발생: ' + error);
      }
      return Promise.reject(error);
    }
    return Promise.reject(error);
  }
);

export default server;
