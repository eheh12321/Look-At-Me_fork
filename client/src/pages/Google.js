import userStore from '../store/userStore';
import memberstore from '../store/memberstore';
import { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import server from '../utils/CustomApi';

const Google = () => {
  const setUserId = userStore((state) => state.setUserId);
  const setNickname = userStore((state) => state.setNickname);
  const { isLogin, setisLogin } = memberstore((state) => state);
  const navigate = useNavigate();

  useEffect(() => {
    const urlParams = new URL(location.href).searchParams;
    const accessToken = urlParams.get('access_token');
    const refreshToken = urlParams.get('refresh_token');
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    setisLogin(true);

    const getMember = async () => {
      await server
        .get(`members/token`)
        .then((res) => {
          console.log(res);
          if (res) {
            setUserId(res.data.memberId);
            setNickname(res.data.nickname);
            localStorage.setItem('myId', res.data.memberId);
            localStorage.setItem('loginUserProfile', res.data.profileImageUrl);
            window.location.href = '/';
          } else {
            console.log(res);
          }
        })
        .catch((err) => {
          console.log('err');
          return err;
        });
    };
    getMember();
  }, []);
  return <></>;
};

export default Google;
