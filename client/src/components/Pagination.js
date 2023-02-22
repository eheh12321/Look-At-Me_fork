import styled from 'styled-components';
import PropTypes from 'prop-types';

Pagination.propTypes = {
  total: PropTypes.number,
  limit: PropTypes.number,
  page: PropTypes.number,
  setPage: PropTypes.func,
};

function Pagination({ total, limit, page, setPage }) {
  if (total == undefined) total = limit;
  const numPages = Math.ceil(total / limit);
  return (
    <>
      <Nav>
        {/* 댓글이 없으면 페이지네이션 버튼 출력 X */}
        <Button
          onClick={() => setPage(page - 1)}
          disabled={page === 1}
          hidden={total === 0}
        >
          &lt;
        </Button>
        {Array(numPages)
          .fill()
          .map((_, i) => (
            <Button
              key={i + 1}
              onClick={() => setPage(i + 1)}
              aria-current={page === i + 1 ? 'page' : null}
            >
              {i + 1}
            </Button>
          ))}
        <Button
          onClick={() => setPage(page + 1)}
          disabled={page === numPages}
          hidden={total === 0}
        >
          &gt;
        </Button>
      </Nav>
    </>
  );
}

const Nav = styled.nav`
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
  margin: 16px;
`;

const Button = styled.button`
  border: none;
  border-radius: 8px;
  padding: 8px;
  margin: 0;
  background: #353945;
  color: white;
  font-size: 1rem;

  &:hover {
    background: tomato;
    cursor: pointer;
    transform: translateY(-2px);
  }

  &[disabled] {
    background: grey;
    cursor: revert;
    transform: revert;
  }

  &[aria-current] {
    background: #d8cdb0;
    color: #353945;
    font-weight: bold;
    cursor: revert;
    transform: revert;
  }
`;

export default Pagination;
