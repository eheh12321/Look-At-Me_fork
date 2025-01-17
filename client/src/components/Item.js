import { ReactComponent as Rent } from '../svg/Rent.svg';
import styled from 'styled-components';
import Avatar from '../components/Avatar';
import { useParams, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import server from '../utils/CustomApi';

const Item = () => {
  const params = useParams();
  const [itemData, setItemData] = useState([]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await server.get(`boards/` + [params.boardId]);
        setItemData(response.data);
      } catch (err) {
        return err;
      }
    };
    fetchData();
  }, []);
  const onMoveLink = (link) => {
    window.open(link, '', '');
  };
  return (
    <SItemContainer>
      {itemData.products?.map((item) => (
        <div
          className="item_box"
          key={item.productId}
          role="presentation"
          onClick={() => {
            if (
              confirm('외부 상품 구매 사이트로 이동합니다. 계속하시겠습니까?')
            ) {
              onMoveLink(item.link);
            }
          }}
        >
          <div className="item_picture">
            <Avatar image={item.productImage} />
          </div>
          <div className="item_info">
            <div className="item_sale">
              <div className="item_info_name">
                <span>브랜드 :</span>
                <span>제품명 :</span>
                <span className="products-price">₩ {item.price}</span>
              </div>
              <div className="item_info_content">
                <span> {item.brand}</span>
                <span> {item.productName}</span>
                <span className="products-size"> {item.rental.size}</span>
              </div>
            </div>
            {item.rental.available === true ? (
              <div className="item_rent">
                <Rent />
                <div className="rent_price">
                  <p> rental price : </p>
                  <p>{item.rental?.rentalPrice}</p>
                </div>
              </div>
            ) : null}
          </div>
        </div>
      ))}
    </SItemContainer>
  );
};
const SItemContainer = styled.div`
  .item_box {
    height: 65px;
    margin-bottom: 13px;
    display: flex;
    align-items: center;
    padding: 10px;
    border: 1px solid gray;
    border-radius: 5px;
    cursor: pointer;

    .item_info {
      width: 90%;
      display: flex;
      justify-content: space-between;
      margin-left: 10px;

      .item_sale {
        display: flex;
        justify-content: center;
        margin-left: 5px;
        .item_info_name {
          display: flex;
          flex-direction: column;
          justify-content: center;
          /* align-items: center; */
          font-weight: bold;
          /* text-align: center; */
          margin-right: 10px;
          span {
            margin: 1px 0px 1px 0px;
          }
          .products-price {
            font-weight: normal;
            /* color: gray; */
            font-size: 13px;
          }
        }
        .item_info_content {
          display: flex;
          flex-direction: column;
          justify-content: center;
          font-size: 15px;
          /* margin-top: 2px; */
          span {
            margin: 1px 0px 2px 0px;
          }
          .products-size {
            /* color: gray; */
            font-size: 13px;
          }
        }
      }
      .item_rent {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        svg {
          margin-top: 10px;
        }
      }
      .rent_price {
        display: flex;
        margin-right: 15px;
        font-size: 15px;
      }
    }
  }
  .item_picture {
    width: 80px;
    height: 80px;
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
`;
export default Item;
