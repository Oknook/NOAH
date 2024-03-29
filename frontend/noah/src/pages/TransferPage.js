import { useLocation, useNavigate, useParams } from "react-router-dom";
import Header from "./../components/common/Header";
import { useState, useEffect } from "react";
import MyAccount from "./../components/common/MyAccount";
import styles from "./TransferPage.module.css";
import Button from "../components/common/Button";
import { ReactComponent as TransferArrow } from "./../assets/Icon/TransferArrow.svg";
import { getAccount } from "../api/account/Account";
import { depositGroupAccount } from "../api/groupaccount/GroupAccount";
import showToast from "../components/common/Toast";

export default function TransferPage() {
  const location = useLocation();
  const [seq, setSeq] = useState(0); // 페이지 관리를 위해
  // const [accountNumber, setAccountNumber] = useState(""); // 내 계좌번호 기억
  const { travelId } = useParams();
  const [amount, setAmount] = useState(0); // 입금 금액
  const navigate = useNavigate();
  const [warningText, setWarningText] = useState("");
  const [accounts, setAccounts] = useState([]);
  const [accountId, setAccountId] = useState(null);
  const [selectAccount, setSelectAccount] = useState([]);

  const { title, bankName, accountNumber } = location.state || {};

  // Header의 LeftIcon 클릭 이벤트 핸들러
  const handleLeftIconClick = () => {
    if (seq === 0) {
      navigate("/home"); // seq가 0이면 /home으로 리다이렉트
    } else {
      setSeq(seq - 1); // 그 외의 경우, seq 값을 하나 줄임
    }
  };

  const handleAccountClick = (account) => {
    setSeq(1);
    setAccountId(account.accountId);
    setSelectAccount(account);
  };

  const handleTransfer = async () => {
    // 최종 송금 코드 작성
    if (!accountId || !travelId || !amount) {
      console.error("필수 정보가 누락되었습니다.");
      return;
    }

    const object = {
      accountId,
      travelId,
      amount,
    };

    try {
      const response = await depositGroupAccount(object);
      console.log("송금 성공", response);
      showToast("입금이 성공적으로 완료되었습니다.");
      navigate("/home");
    } catch (error) {
      setWarningText("잔액이 부족합니다.");
      console.log("송금 실패", error);
    }
  };

  useEffect(() => {
    (async () => {
      try {
        const res = await getAccount();
        console.log(res);
        setAccounts(res.data);
      } catch (e) {
        console.log(e);
      }
    })();
  }, []);

  return (
    <>
      {seq === 0 && (
        <>
          <Header
            LeftIcon="Arrow"
            Title="내 계좌"
            onClick={handleLeftIconClick}
          />
          {accounts
            .filter((account) => account.type !== "공동계좌") // 공동계좌만 불러옴
            .map((account) => (
              <MyAccount
                key={account.accountId} // 고유 key 값으로 accountId 사용
                type={account.bankName} // 조건에 따른 type 결정
                accountNumber={account.accountNumber}
                sum={account.amount}
                onClick={() => handleAccountClick(account)}
              />
            ))}
        </>
      )}
      {seq === 1 && (
        <>
          <Header
            LeftIcon="Arrow"
            Title="금액 입력"
            onClick={handleLeftIconClick}
          />
          <div className={styles.amount}>
            {new Intl.NumberFormat("ko-KR").format(amount)} 원
          </div>
          <div className={styles.keypadContainer}>
            <div className={styles.keypadLine}>
              <div
                className={styles.keypad}
                onClick={() => setAmount((prev) => prev + "7")}
              >
                7
              </div>
              <div
                className={styles.keypad}
                onClick={() => setAmount((prev) => prev + "8")}
              >
                8
              </div>
              <div
                className={styles.keypad}
                onClick={() => setAmount((prev) => prev + "9")}
              >
                9
              </div>
            </div>
            <div className={styles.keypadLine}>
              <div
                className={styles.keypad}
                onClick={() => setAmount((prev) => prev + "4")}
              >
                4
              </div>
              <div
                className={styles.keypad}
                onClick={() => setAmount((prev) => prev + "5")}
              >
                5
              </div>
              <div
                className={styles.keypad}
                onClick={() => setAmount((prev) => prev + "6")}
              >
                6
              </div>
            </div>
            <div className={styles.keypadLine}>
              <div
                className={styles.keypad}
                onClick={() => setAmount((prev) => prev + "1")}
              >
                1
              </div>
              <div
                className={styles.keypad}
                onClick={() => setAmount((prev) => prev + "2")}
              >
                2
              </div>
              <div
                className={styles.keypad}
                onClick={() => setAmount((prev) => prev + "3")}
              >
                3
              </div>
            </div>
            <div className={styles.keypadLine}>
              <div
                className={styles.keypad}
                onClick={() => setAmount((prev) => prev.slice(0, -1))}
              >
                ←
              </div>
              <div
                className={styles.keypad}
                onClick={() => setAmount((prev) => prev + "0")}
              >
                0
              </div>
              <div
                className={styles.keypad}
                style={{ fontSize: "4.45vw" }}
                onClick={() => setSeq(2)}
              >
                확인
              </div>
            </div>
          </div>
        </>
      )}
      {seq === 2 && (
        <>
          <Header
            LeftIcon="Arrow"
            Title="입금 정보 확인"
            onClick={handleLeftIconClick}
          />
          <div className={styles.informationContainer}>
            <div className={styles.accountBorder}>
              <div className={styles.labelLarge}>내 계좌</div>
              <div className={styles.paragraphMedium}>
                {selectAccount.bankName} {selectAccount.accountNumber}
              </div>
            </div>
            <div style={{ margin: "2.22vw 0" }}>
              <TransferArrow style={{ width: "6.67vw", height: "6.67vw" }} />
            </div>
            <div className={styles.accountBorder}>
              <div className={styles.labelLarge}>{title}</div>
              <div className={styles.paragraphMedium}>
                {bankName} {accountNumber}
              </div>
            </div>
          </div>
          <div style={{ marginRight: "7vw" }}>
            <div
              className={styles.amount}
              style={{ textAlign: "right", marginTop: "5vw" }}
            >
              {new Intl.NumberFormat("ko-KR").format(amount)} 원
            </div>

            <div
              className={styles.paragraphMedium}
              style={{ textAlign: "right", marginBottom: "5vw" }}
            >
              을 송금합니다.
            </div>
          </div>
          <div onClick={handleTransfer}>
            <Button buttonText="송금" {...warningText} />
          </div>
        </>
      )}
    </>
  );
}
