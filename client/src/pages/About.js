import React from 'react';
import styled from 'styled-components';
import { BsPencilSquare, BsBookmarkHeart } from 'react-icons/bs';
import { AiOutlineMessage } from 'react-icons/ai';

const About = () => {
  return (
    <Swrapper>
      <div className="container">
        <div className="title">Our Team : Look At Me</div>
        <div className="body">
          나만의 옷 스타일을 뽐내고 싶지 않나요?
          <br /> 특별한 날에 입을 옷을 사기에 부담스러우시진 않나요?
        </div>
        <div className="main">
          Look at me는 자신이 입은 옷을 게시하여 자랑도 하고, 렌탈까지 가능한
          소셜 공유 사이트입니다.
        </div>
        <Service>
          <div className="service">
            <BsPencilSquare />
            내가 바로 패셔니스타! <br /> 게시글을 작성하여 나만의 옷 스타일을
            뽐내주세요.
          </div>
          <div className="service">
            <AiOutlineMessage />
            작성자가 입은 옷 스타일이 마음에 든다면?
            <br /> 채팅을 걸어 렌탈에 대해 대화를 나눠보세요.
          </div>
          <div className="service">
            <BsBookmarkHeart />
            이 옷 스타일은 너무 마음에 드는데? 다음에 나도 이렇게 입어야지!
            <br /> 마음에 드는 코디를 저장해보세요.
          </div>
        </Service>
      </div>
    </Swrapper>
  );
};
const Swrapper = styled.div`
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  color: #565656;
  .container {
    height: 100vh;
  }
  .title {
    margin-top: 100px;
    font-size: 55px;
  }
  .body {
    font-size: 30px;
    margin-top: 50px;
    font-weight: 500;
  }
  .main {
    margin-top: 30px;
    font-size: 20px;
  }
`;
const Service = styled.div`
  width: 80%;
  margin-top: 30px;
  display: flex;
  box-sizing: border-box;
  flex-direction: column;
  svg {
    font-size: 34px;
    margin-right: 20px;
  }
  .service {
    margin-top: 30px;
    display: flex;
    align-items: center;
  }
`;
export default About;