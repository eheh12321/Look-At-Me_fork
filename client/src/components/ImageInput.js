import styled from 'styled-components';
import PropTypes from 'prop-types';
import React, { useState } from 'react';

//Postupload.js에서 props로 받아온 것
const ImageInput = ({ imgFile, setImgFile }) => {
  // const [imgFile, setImgFile] = useState([]); // 이미지 배열
  const [fileImage, setFileImage] = useState('');
  // 이미지 상대경로 저장
  const onChangeImg = (e) => {
    const imageLists = e.target.files[0];
    let imageUrlLists = [...imgFile, imageLists];
    // 미리보기 기능은 위의 결과값에서 없는 상대경로가 필요- URL.createObjectUrl() 메소드
    //미리보기 기능
    let reader = new FileReader();
    if (imageLists) {
      reader.readAsDataURL(imageLists);
    }
    reader.onloadend = () => {
      const resultImage = reader.result;
      setFileImage(resultImage);
    };

    if (imageUrlLists.length > 1) {
      // 이미지첨부 1장 제한
      imageUrlLists = imageUrlLists.slice(0, 1);
    }
    setImgFile(imageUrlLists);
    console.log(imageUrlLists);
  };

  return (
    <SWrapper>
      <div className="image-upload ">
        <label htmlFor="input-file">
          <div className="btn-upload">사진 업로드</div>
        </label>
        <input
          type="file"
          id="input-file"
          onChange={onChangeImg} // 파일이 추가되면 이벤트 발생
          // multiple // 파일 여러개 선택 가능
          accept="image/*" //모든 이미지 파일형식
        />
        <div className="preview">
          {fileImage && <img src={fileImage} alt="preview-img" />}
        </div>
      </div>
    </SWrapper>
  );
};

ImageInput.propTypes = {
  imgFile: PropTypes.array,
  setImgFile: PropTypes.func,
};

const SWrapper = styled.div`
  width: 50%;
  height: 41vh;
  .image-upload {
    margin: 10px;
  }
  .image-upload > input {
    display: none;
  }
  //파일 업로드버튼
  .btn-upload {
    width: 85px;
    height: 30px;
    /* padding: 7px 14px; */
    background-color: #d9d4a6;
    border-radius: 4px;
    color: black;
    font-weight: 600;
    font-size: 13px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
  }
  img {
    width: 95%;
    height: 290px;
    margin: 20px 0px 20px 5px;
  }
`;

export default ImageInput;
