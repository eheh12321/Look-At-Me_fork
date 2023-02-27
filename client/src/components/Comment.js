/* eslint-disable react/prop-types */
import styled from 'styled-components';
import Avatar from '../components/Avatar';
import { HiOutlinePaperAirplane } from 'react-icons/hi';
import { useParams } from 'react-router-dom';
import { useState, useEffect } from 'react';
import userStore from '../store/userStore';
import Pagination from './Pagination';
import server from '../utils/CustomApi';

const BREAK_POINT_PC = 1300;
const token = localStorage.getItem('accessToken');
const Comment = ({ boardId, profile }) => {
  // Paging
  const [limit, setLimit] = useState(5);
  const [page, setPage] = useState(1);

  const params = useParams();
  const url = 'https://myprojectsite.shop/comment';
  const [commentData, setCommentData] = useState([]);
  const [contentValue, setContentValue] = useState('');
  const { nickname } = userStore((state) => state);

  const onContentChange = (e) => {
    setContentValue(e.currentTarget.value);
  };
  const onPostComment = (val) => {
    const data = JSON.stringify({
      boardId: boardId,
      content: val,
    });
    server
      .post(`comment`, data, {
        headers: {
          'Content-Type': 'application/json',
        },
      })
      .then((res) => {
        document.getElementById('test').value = '';
        if (res) {
          fetchCommentData();
        }
      })
      .catch((err) => {
        return err;
      });
  };
  const fetchCommentData = async () => {
    try {
      let offset = (page - 1) * limit;
      let p = page;
      if (commentData.pageInfoDto?.totalElements < offset) {
        setPage(commentData.pageInfoDto?.totalPages); // 마지막 페이지 검색
        p = Math.ceil(commentData.pageInfoDto?.totalElements / limit);
      }
      const res = await server.get(
        `comment/board/${params.boardId}?page=${p}&size=${limit}`
      );
      setCommentData(res.data);
    } catch (err) {
      return err;
    }
  };
  const filtering = async () => {
    // 로그인 한 회원의 댓글만 수정/삭제 버튼이 보이도록 반복문
    const elems = document.getElementsByClassName('comment_box');
    for (var i = 0; i < elems.length; i++) {
      if (nickname != elems[i].children[0].children[1].innerHTML) {
        elems[i].children[1].style.display = 'none';
      }
    }
  };
  useEffect(() => {
    fetchCommentData();
  }, [page, limit]);
  useEffect(() => {
    filtering();
  }, [commentData]);
  const onDelteComment = (id) => {
    if (window.confirm('삭제 하시겠습니까?')) {
      server
        .delete(`comment/${id}`)
        .then((res) => {
          if (res) {
            fetchCommentData();
          }
        })
        .catch((err) => console.log('Error', err.message));
    }
  };
  const isReadonly = token == null ? true : false;
  const commentPlaceholder = isReadonly
    ? '로그인 하고 댓글을 달아보세요 :D'
    : '댓글 달기...';

  //댓글 수정부분
  const [revise, setRevise] = useState(''); //댓글 수정창에 입력한 값이 저장
  const [editCommentId, setEditCommentId] = useState(''); // 현재수정중인 CommentId '' 값이면 댓글 수정중 아님
  const onChangeInput = (e) => {
    setRevise(e.target.value);
  };

  // 댓글수정 저장
  const onSave = async (id) => {
    try {
      server.patch(`comment/${id}`, {
        content: revise,
      });
    } catch (err) {
      window.alert(err);
    }
    setEditCommentId('');
    // 저장시 현재 수정중인 Comment의 Id를 ''으로 변경하여 초기화
    setCommentData((prev) => {
      return {
        data: prev.data.map((comment) => {
          if (comment.commentId === id) {
            return {
              ...comment,
              content: revise,
            };
          }
          return comment;
        }),
        pageInfoDto: prev.pageInfoDto,
      };
    });
  };

  return (
    <SWrapper>
      <div className="comment_count">
        <span>댓글 </span>
        {commentData.pageInfoDto?.totalElements}
        <label className="comment_paging_label">
          페이지 당 댓글 수:&nbsp;
          <select
            type="number"
            value={limit}
            onChange={({ target: { value } }) => setLimit(Number(value))}
          >
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="15">15</option>
            <option value="20">20</option>
            <option value="25">25</option>
          </select>
        </label>
      </div>
      <div className="line"></div>
      <form className="commentWrap">
        <div className="my_avatar">
          <Avatar image={profile} />
        </div>
        <div className="user-name"></div>
        <div className="comment-input">
          <input
            id="test"
            type="text"
            placeholder={commentPlaceholder}
            onChange={onContentChange}
            readOnly={isReadonly}
          />

          <button
            type="button"
            disabled={!contentValue}
            onClick={() => {
              onPostComment(contentValue);
            }}
          >
            <HiOutlinePaperAirplane />
          </button>
        </div>
      </form>

      <div className="comment_container">
        {commentData &&
          commentData.data?.map((comment) => (
            <div className="comment_box" key={comment.commentId}>
              <div className="comment-left">
                <div className="user_avatar">
                  <Avatar image={comment.profileImageUrl} />
                </div>
                <div className="user_name">{comment.nickname}</div>
                {editCommentId === comment.commentId ? ( //현재 수정중인 Comment와 동일한CommentId를 가지고있는지?
                  <SInput value={revise} onChange={onChangeInput}></SInput>
                ) : (
                  <div className="comment_content">{comment.content}</div>
                )}
              </div>
              <div className="comment-right">
                {editCommentId === comment.commentId ? (
                  <SSave onClick={() => onSave(comment.commentId)}> 저장</SSave>
                ) : (
                  <>
                    <span
                      role="presentation"
                      //추가부분
                      onClick={() => {
                        if (comment.nickname === nickname) {
                          setRevise(comment.content);
                          setEditCommentId(comment.commentId);
                        }
                      }}
                    >
                      수정
                    </span>
                    <span
                      role="presentation"
                      //추가부분
                      onClick={() => {
                        if (comment.nickname === nickname) {
                          onDelteComment(comment.commentId);
                        }
                      }}
                    >
                      삭제
                    </span>
                  </>
                )}
              </div>
            </div>
          ))}
      </div>
      <footer>
        <Pagination
          total={commentData.pageInfoDto?.totalElements}
          limit={limit}
          page={page}
          setPage={setPage}
        />
      </footer>
    </SWrapper>
  );
};

const SWrapper = styled.div`
  width: 100%;
  height: 50%;
  padding-top: 8px;

  .line {
    width: 100%;
    text-align: center;
    border-bottom: 1px solid gray;
    line-height: 0.1em;
    margin: 10px 0;
  }
  .commentWrap {
    display: flex;
    margin-bottom: 10px;
    justify-content: center;
    align-items: center;
    .my_avatar {
      width: 30px;
      height: 30px;
      object-fit: cover;
      position: relative;
      overflow: hidden;
      margin-right: 10px;

      img {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 100%;
        height: 100%;
      }
    }
    .comment-input {
      display: flex;
      width: 100%;
      justify-content: center;
      border: 1px solid lightgray;
      /* background-color: white; */
      /* background-color: #f7f5ec; */
      margin-left: 2px;
      border-radius: 5px;

      input {
        width: 90%;
        height: 4vh;
        border: none;
        /* border-bottom: 1px solid gray; */
        /* background-color: #f7f5ec; */
        &:focus {
          outline: none;
        }
      }
      svg {
        font-size: 25px;
        transform: rotate(90deg);
        margin-top: 5px;
      }
    }
    button {
      background-color: white;
      border: none;
      cursor: pointer;
    }
  }
  .comment_container {
    max-height: 25vh; // 한 스크롤에 댓글 5개 출력

    @media only screen and (max-width: ${BREAK_POINT_PC}px) {
      & {
        height: 85px;
      }
    }
    overflow: auto;
    .comment_box {
      display: flex;
      height: 5vh;
      align-items: center;
      justify-content: space-between;
      .comment-left {
        display: flex;
      }
      .comment-right {
        margin-right: 10px;
        font-size: 12px;
        color: gray;
        width: 80px;
        justify-content: center;
        display: flex;
        span {
          margin: 0px 5px;
          cursor: pointer;
          :hover {
            color: black;
          }
        }
      }
    }
    .user_avatar {
      width: 30px;
      height: 30px;
      object-fit: cover;
      position: relative;
      overflow: hidden;

      img {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 100%;
        height: 100%;
      }
    }
    .user_name {
      margin: 3px 10px;
      font-size: 16px;
      font-weight: bold;
      span {
        :hover {
          color: black;
          cursor: pointer;
        }
      }
    }
    .comment_content {
      margin-top: 5px;
      font-size: 14px;
      width: 26vw;
      height: 100%;
    }
  }
  .comment_paging_label {
    float: right;
  }
`;
const SSave = styled.div`
  /* width: 70px;
  border-radius: 20px; */
`;
const SInput = styled.input`
  width: 22vw;
  display: flex;
  /* justify-content: center; */
  font-size: 14px;
`;

export default Comment;
