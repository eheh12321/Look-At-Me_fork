/* eslint-disable react/prop-types */
import styled from 'styled-components';
import Avatar from '../components/Avatar';
import { useState, useEffect } from 'react';
import server from '../utils/CustomApi';
import { HiOutlinePaperAirplane } from 'react-icons/hi';
import { BREAK_POINT_PC, BREAK_POINT_TABLET } from '../constants/index';

const Chat = () => {
  const [chatData, setChatData] = useState([]); //get
  const [sentData, setSentData] = useState([]); //post
  const [listData, setListData] = useState([]); //list get
  const [idData, setIdData] = useState('');
  const [sentName, setSentName] = useState('');
  const [sentPicture, setSentPicture] = useState('');
  const url = 'https://myprojectsite.shop';
  const sentId = JSON.parse(localStorage.getItem('sentId'));
  const name = JSON.parse(localStorage.getItem('name'));
  const myId = JSON.parse(localStorage.getItem('myId'));
  const token = localStorage.getItem('accessToken');
  const profile = JSON.parse(localStorage.getItem('profile'));

  const onChatChange = (e) => {
    setSentData(e.currentTarget.value);
  };
  useEffect(() => {
    fetchData();
    fetchListClickData();
    fetchListData();
  }, [sentId, idData]);
  useEffect(() => {
    //데이터가 없을때 받는사람
    if (chatData?.length === 0) {
      setSentName(name);
      setSentPicture(profile);
    } else if (chatData?.length !== 0 && myId === chatData[0]?.senderId) {
      setSentName(chatData[0]?.receiverNickname);
      setSentPicture(chatData[0]?.receiverProfileImageUrl);
    } else if (chatData?.length !== 0 && myId !== chatData[0]?.senderId) {
      setSentName(chatData[0]?.senderNickname);
      setSentPicture(chatData[0]?.senderProfileImageUrl);
    }
  }, [chatData]);
  //게시물에서 채틸누르면 데이터 받는거
  const fetchData = async () => {
    try {
      const response = await server.get(
        `message/received/${idData}?page=1&size=100`
      );
      setChatData(response.data.data);
      console.log(response.data.data);
    } catch (err) {
      return err;
    }
  };
  //채팅목록에서 누르면 데이터 받는거
  const fetchListClickData = async () => {
    try {
      const response = await server.get(
        `/message/received/${sentId}?page=1&size=100`
      );
      setChatData(response.data.data);
    } catch (err) {
      return err;
    }
  };

  //목록전체를 받는거
  const fetchListData = async () => {
    try {
      const response = await server.get(`/message/room`);
      setListData(response.data);
    } catch (err) {
      return err;
    }
  };

  const onPostChat = () => {
    const data = JSON.stringify({
      content: sentData,
      receiverNickname: sentName,
    });
    server
      .post(`/message`, data, {
        headers: {
          'Content-Type': 'application/json',
        },
      })
      .then((res) => {
        if (res) {
          fetchData();
          fetchListData();
          fetchListClickData();
        }
      })
      .catch((err) => {
        return err;
      });
  };

  return (
    <>
      <SWrapper>
        <div className="chat">
          <div className="chatlist-container">
            <p>Chatting List</p>
            {listData.map((chat) => (
              <SChatList key={chat.messageRoom}>
                {myId === chat.senderId ? (
                  <div
                    className="chat-box"
                    role="presentation"
                    onClick={() => setIdData(chat.receiverId)}
                  >
                    <Avatar size="45px" image={chat.receiverProfileImageUrl} />
                    <div className="right content">
                      <div className="user-name">{chat.receiverNickname}</div>
                      <div className="content_and_time">
                        <div className="chat-last_content">{chat.content}</div>
                        <div className="chat-time">
                          {new Date(chat.createdDate).toLocaleString()}
                        </div>
                      </div>
                    </div>
                  </div>
                ) : (
                  <div
                    className="chat-box"
                    role="presentation"
                    onClick={() => setIdData(chat.senderId)}
                  >
                    <Avatar size="45px" image={chat.senderProfileImageUrl} />
                    <div className="right content">
                      <div className="user-name">{chat.senderNickname}</div>
                      <div className="content_and_time">
                        <div className="chat-last_content">{chat.content}</div>
                        <div className="chat-time">
                          {new Date(chat.createdDate).toLocaleString()}
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </SChatList>
            ))}
          </div>
          <div className="chat-container">
            <div className="top">
              <Avatar size="40px" image={sentPicture} />
              <span className="user-name">{sentName}</span>
            </div>
            <SContent>
              {chatData.map((chat) => (
                <div className="chat-content" key={chat.messageId}>
                  <Avatar size="40px" image={chat.senderProfileImageUrl} />
                  <div className="user-content">
                    <div className="user-content_top">
                      <span className="user-name">{chat.senderNickname}</span>
                      <span className="user-send-time">
                        {new Date(chat.createdDate).toLocaleString()}
                      </span>
                    </div>
                    <div className="send-content">{chat.content}</div>
                  </div>
                </div>
              ))}
            </SContent>
            <SInputContent>
              <textarea rows="1" cols="33" onChange={onChatChange} />
              <button disabled={!sentData} onClick={onPostChat}>
                <HiOutlinePaperAirplane />
              </button>
            </SInputContent>
          </div>
        </div>
      </SWrapper>
    </>
  );
};

const SWrapper = styled.div`
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 20px;
  @media only screen and (max-width: ${BREAK_POINT_TABLET}px) {
    width: 350px;
    margin-left: 70px;
  }

  .chat {
    display: flex;
    margin-left: 200px;
  }
  p {
    margin-left: 40px;
    font-size: 25px;
  }
  .chatlist-container {
    margin: 10px 0px 50px 0px;
    width: 330px;
    height: 75vh;
    /* background-color: #e1f2f9; */
    border-radius: 10px 0px 0px 10px;
    display: flex;
    flex-direction: column;
    border: 1px solid gray;
    overflow: auto;
    &::-webkit-scrollbar {
      width: 7px;
      background-color: white;
    }
    &::-webkit-scrollbar-thumb {
      background: lightgray;
      border-radius: 5px;
    }
  }
  .chat-container {
    margin: 10px 0px 50px 0px;
    /* padding-left: 10px; */
    width: 400px;
    height: 75vh;
    /* background-color: #e1f2f9; */
    border-radius: 0px 10px 10px 0px;
    border: 1px solid gray;
    display: flex;
    flex-direction: column;
    justify-content: center;
    @media only screen and (max-width: ${BREAK_POINT_PC}px) {
      & {
        width: 350px;
      }
    }
    .top {
      display: flex;
      align-items: center;
      /* width: 105%; */
      font-size: 26px;
      padding-bottom: 13px;
      padding-top: 10px;
      justify-content: flex-start; //추가
      padding-left: 15px;
      border-bottom: 1px solid lightgray;
      margin-top: -22px;
      .user-name {
        padding-left: 20px;
      }
    }
  }
`;
const SChatList = styled.div`
  display: flex;
  justify-content: center;
  border-top: 1px solid lightgray;
  border-bottom: 1px solid lightgray;
  &:focus {
    background-color: gray;
  }
  .chat-box {
    cursor: pointer;
    width: 85%;
    height: 8.5vh;
    background-color: white;
    display: flex;
    align-items: center;
    justify-content: felx-start;
    padding-left: 10px;
    /* margin-bottom: 10px; */
    border-radius: 10px;
    /* box-shadow: 3px 3px 5px gray; */

    .content {
      margin-left: 10px;
      flex-grow: 1;
    }
    .content_and_time {
      margin-top: 5px;
      width: 90%;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .chat-time {
      font-size: 10px;
    }
  }
`;

const SContent = styled.div`
  width: 100%;
  height: 55vh;
  background-color: white;
  margin: 10px 0px 20px 0px;
  overflow: auto;
  border-radius: 8px; //추가
  .chat-content {
    display: flex;
    margin: 15px 0px 5px 15px;
    font-size: 18px;
    align-items: center;
    .user-content {
      padding: 5px;
      margin-left: 10px;
      width: 300px;
      background-color: #e1f2f9;
      border-radius: 10px 10px 10px 10px;
      .user-name {
        font-weight: bold;
        margin-right: 8px;
      }
      .user-send-time {
        font-size: 12px;
      }
    }
  }
`;
const SInputContent = styled.div`
  width: 90%;
  height: 5vh;
  background-color: white;
  /* margin-bottom: 30px; */
  margin-left: 15px;
  display: flex;
  border: 1px solid gray;
  border-radius: 8px; //추가
  textarea {
    border: none;
    resize: none;
    background-color: white;
    font-size: 20px;
    border-radius: 8px; //추가

    &:focus {
      outline: none;
    }
  }
  svg {
    font-size: 30px; //추가
    margin: 3px 10px 0px 20px; //추가
    transform: rotate(90deg);
  }
  button {
    background-color: white;
    border: none;
    border-radius: 8px;
  }
`;
export default Chat;
