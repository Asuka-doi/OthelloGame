<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="model.OthelloToken"%>
<%@ page session="true"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>オセロゲーム</title>
    <link rel="stylesheet" href="Game.css">
    <link rel="icon" type="image/x-icon" href="/othello_game/game/images/black.png">
</head>
<body>
    <div class="StartBoard">
        <h1>オセロゲーム</h1>

        <div id="turn-info">
            <p id="current-turn">現在のターン：</p>  <!-- 初期ターン（黒） -->
            <p id="game-result" style="display: none;"></p> <!-- ゲーム結果（非表示） -->
        </div>

        <!-- 結果表示をタイトル直下に配置 -->
        <div id="resultDisplay" style="display: none;">
            <p>黒の駒： <span id="blackCount">0</span>　
            白の駒： <span id="whiteCount">0</span></p>
            <p>対戦結果： <span id="gameResult">引き分け</span></p>
        </div>

        <!-- エラー時のメッセージ表示 -->
        <c:if test="${not empty sessionScope.errorMessage}">
            <div
                style="color: red; font-weight: bold; padding: 10px; border: 1px solid red; margin-bottom: 15px;">
                ${sessionScope.errorMessage}</div>
            <c:set var="dummy"
                value="${sessionScope.removeAttribute('errorMessage')}" />
        </c:if>

        <!-- Javaコード -->
        <!-- ゲームの状態 -->
        <%
            // ゲームの状態をセッションから取得
            OthelloToken game = (OthelloToken) session.getAttribute("game");
            boolean gameStarted = (Boolean) session.getAttribute("gameActive") != null
                    && (Boolean) session.getAttribute("gameActive");
            // ゲームが存在しない場合、新しいゲームを開始して保存
            if (game == null) {
                game = new OthelloToken();
                session.setAttribute("game", game);
                game.startGame();
            }
            // ゲーム盤面のHTMLを生成
            String boardHtml = game.getBoardAsHtml();
            String currentTurn = game.getCurrentTurn();
            String gameOver = game.isGameOver() ? "true" : "false"; // ゲームが終了しているかどうか
            String gameResult = "";
            if (game.isGameOver()) {
                gameResult = game.getGameResultHtml(); // ゲーム終了後の結果を取得
            }
        %>
        <!-- 生成した盤面を表示する -->
        <div id="boardContainer">
            <%=boardHtml%>
        </div>

    </div>

    <div class="button">
        <button id="startButton" onclick="startGame()" <%=gameStarted ? "disabled" : ""%>>スタート！</button>
        <div class="message">
            先攻が黒の駒を操作し、後攻が白の駒を操作します。<br> スタートボタンを押すとゲームスタートです。
        </div>
    </div>
    
    <!-- 結果を表示するためのHTML -->
    <div id="resultDisplay" style="display: none;">
        <p>黒の駒： <span id="blackCount">0</span></p>
        <p>白の駒： <span id="whiteCount">0</span></p>
        <p>対戦結果： <span id="gameResult">引き分け</span></p>
    </div>

    <!-- JavaScript -->
    <script type="text/javascript">
        let gameActive = false;
        let game = null;

        // リスタートボタンのクリック時の処理
        document.getElementById("startButton").addEventListener("click", function() {
            // ゲームをリセット（リスタート）
            console.log("ゲームがリスタートされました");
            // ゲーム開始処理を呼び出す
            startGame();
            
            // ゲーム中の表示に切り替え
            document.getElementById("current-turn").style.display = "block";  // 現在のターンを表示
            document.getElementById("resultDisplay").style.display = "none";  // 結果を非表示
        });

        // スタートゲーム処理
        function startGame() {
            console.log("スタートボタンが押されました");
            console.log("現在の盤面：", gameActive);

            fetch("/othello_game/game/GameServlet", {  // ゲーム開始リクエストをサーバーに送る
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: 'action=startGame'  // ゲーム開始アクション
            })
            .then(response => response.json())
            .then(data => {
                console.log("ゲーム開始レスポンス:", data);  // レスポンスをデバッグ表示

                if (data.board) {
                    game = data.game;
                    document.getElementById("boardContainer").innerHTML = data.board;
                    updateTurnDisplay(data.currentTurn);
                    gameActive = true;  // ゲームが開始されたら gameActive を true に
                    document.getElementById("startButton").disabled = true;  // スタートボタンを無効化
                    document.getElementById("startButton").textContent = "リスタート！";
                    setupBoardClickListeners();  // 盤面のクリックリスナーを設定
                }
                console.log("現在の盤面：", gameActive);
            })
            .catch(error => {
                console.error('Error:', error);
                alert('エラーが発生しました。コンソールを確認してください。');
            })
            .finally(() => {
                // 処理終了後にセルを再度有効化（ゲーム進行後など）
            });
        }

        // 盤面クリックに対するイベントリスナー
        let setUp = false; // リスナーが設定されたかどうかのフラグ

        function setupBoardClickListeners() {
            if (setUp) return; // すでにリスナーが設定されている場合は何もしない

            let cells = document.querySelectorAll('.empty');
            console.log("セルを取得しました：",cells);
            
            cells.forEach(cell => {
                // すでにクリックされたセルにはリスナーを設定しないようにする
                if (!cell.hasAttribute('data-clicked')) {
                    cell.addEventListener('click', function() {
                        console.log("セルがクリックされました: 行 = " + this.getAttribute('data-row') + ", 列 = " + this.getAttribute('data-col'));

                        // スタートボタンが押されていなかった時
                        if (!gameActive) {
                            alert("ゲームが開始されていません。スタートボタンを押してください.");
                            return;
                        }

                        // 空白や不可視文字を除去
                        let rowStr = this.getAttribute('data-row').trim();
                        let colStr = this.getAttribute('data-col').trim();

                        // 行と列を数値として取得
                        let row = parseInt(rowStr, 10);  // 行番号
                        let col = parseInt(colStr, 10);  // 列番号

                        if (isNaN(row) || isNaN(col)) {
                            console.error("無効な行または列:", row, col);  // エラーメッセージを追加
                            alert('無効なセルがクリックされました。');
                            return;
                        }

                        placeToken(this);  // 駒を配置
                    });
                    cell.setAttribute('data-clicked', 'true');  // クリックされたセルにはリスナーを設定済みとしてマーク
                }
            });

            setUp = true; // リスナーを設定済みにマーク
        }

        // 駒を配置する関数（既存の placeToken 関数内）        
        function placeToken(cell) {
            // スタートボタンが押されていない場合
            if (!gameActive) {
                alert("ゲームが開始されていません。スタートボタンを押してください。");
                return;
            }

            // セルがすでに無効化されている場合、何もしない
            if (cell.classList.contains('disabled')) {
                console.log("このセルはすでに無効化されています:", cell);
                return;
            }

            // セルを無効化
            cell.classList.add('disabled');

            let row = parseInt(cell.getAttribute('data-row'), 10);
            let col = parseInt(cell.getAttribute('data-col'), 10);

            if (isNaN(row) || isNaN(col)) {
                alert('無効なセルがクリックされました。');
                return;
            }

            // サーバーへのリクエスト
            let params = new URLSearchParams();
            params.append('action', 'placeToken');
            params.append('row', row);
            params.append('col', col);

            fetch("/othello_game/game/GameServlet", {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: params.toString()
            })
            .then(response => response.json())
            .then(data => {
                console.log("駒配置レスポンス:", data);

                // エラーハンドリング: もし置けない場所だった場合
                if (data.error) {
                    alert(data.error);  // エラーメッセージを表示
                    return;  // ターンを更新せず、処理を終了
                }

                // ターンを更新
                updateTurnDisplay(data.currentTurn);

                // 盤面の更新
                document.getElementById("boardContainer").innerHTML = data.board;

                // ゲーム終了時の処理
                if (data.gameOver) {
                    console.log("ゲーム終了時のデータ:", data);
                    console.log("gameResult：", data.winner);
                    alert("ゲーム終了！");
                    
                    // 結果表示の更新
                    updateResultDisplay(data.blackCount, data.whiteCount, data.winner);
                    
                    // ボタンの更新
                    document.getElementById("startButton").textContent = "リスタート！";
                    document.getElementById("startButton").disabled = false;
                }

            })
            .catch(error => {
                console.error('Error:', error);
                alert('エラーが発生しました。コンソールを確認してください。');
            })
            .finally(() => {
                // 無効化解除
                cell.classList.remove('disabled');
            });
        }

        // 現在のターン表示を更新する関数
        function updateTurnDisplay(currentTurn) {
            let turnDisplay = document.getElementById("current-turn");
            if (turnDisplay) {
                turnDisplay.textContent = "現在のターン： " + currentTurn;
            }
        }

        // 結果を表示するための処理
        function updateResultDisplay(blackCount, whiteCount, gameResult) {
            console.log("gameResult の更新:", gameResult);  // gameResultがどの段階でどう変化するか確認
            let resultElement = document.getElementById('gameResult');
            
            if (resultElement) {
                // gameResult が undefined の場合、代わりに空文字にするか
                resultElement.textContent = gameResult || '結果が表示できません';  
            } else {
                console.log("gameResultの表示要素が見つかりませんでした！");
            }

            document.getElementById("blackCount").textContent = blackCount;
            document.getElementById("whiteCount").textContent = whiteCount;

            // 結果表示を表示
            document.getElementById("resultDisplay").style.display = "block";

            // 現在のターン表示を非表示にする
            document.getElementById("current-turn").style.display = "none";
        }
    </script>
</body>
</html>
