package model;

public class OthelloToken {

	private String[][] token;
	private boolean gameActive;
	private String currentTurn;

	public OthelloToken() {
		this.token = new String[8][8];
		this.gameActive = false;
		this.currentTurn = "●"; // 初期設定として黒を設定
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				token[i][j] = "　"; // 空白として全角スペースを使用
			}
		}
	}

	// ゲーム続行or終了を判定するメソッド
	public boolean isGameOver() {
		// すべてのセルに駒が置ける場所がない場合、またはすべてのセルが埋まった場合はゲーム終了
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (token[i][j].equals("　") && canPlaceToken(i, j)) {
					return false; // 置ける場所がある時はゲーム続行
				}
			}
		}
		return true; // 置ける場所がない、または全て埋まった場合は終了
	}

	// ターン切り替えメソッド
	public void turnSwitch() {
		if ("●".equals(currentTurn)) { // currentTurnが黒のとき
			currentTurn = "○"; // 次は白
		} else { // currentTurnが白のとき
			currentTurn = "●"; // 次は黒
		}
	}

	// 現在のターンを管理
	public String getCurrentTurn() {
		return currentTurn;
	}

	public void startGame() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				token[i][j] = "　";
			}
		}
		// 初期の駒を配置
		token[3][3] = "○";
		token[3][4] = "●";
		token[4][3] = "●";
		token[4][4] = "○";
		this.gameActive = true; // ゲームの開始としてgameActiveをtrueに設定
		this.currentTurn = "●";
	}

	// リスタート時の初期化
	public void restartGame() {
		startGame(); // 新たにゲームを開始
		// 現在のターンもリセット
		currentTurn = "●";
	}

	// 駒を反転する処理
	private void turningTokens(int row, int col) {
		String currentPlayer = currentTurn;
		String opponent = (currentPlayer.equals("●")) ? "○" : "●"; // 相手の駒

		// 反転する駒の取得
		// 反転を行う方向
		int[] directions = { -1, 0, 1 }; // 上、右、下、左、斜め
		for (int dRow : directions) {
			for (int dCol : directions) {
				if (dRow == 0 && dCol == 0)
					continue;

				int newRow = row + dRow;
				int newCol = col + dCol;

				// もし相手の駒が隣接している場合、さらに反転可能な駒をチェック
				if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8 &&
						token[newRow][newCol].equals(opponent)) {

					int step = 1;
					// 反転対象の駒を挟む
					while (true) {
						int nextRow = row + dRow * step;
						int nextCol = col + dCol * step;
						if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8)
							break;

						if (token[nextRow][nextCol].equals(currentPlayer)) {
							// 反転対象の駒を挟んだので、反転処理を行う
							for (int i = 1; i < step; i++) {
								int flipRow = row + dRow * i;
								int flipCol = col + dCol * i;
								token[flipRow][flipCol] = currentPlayer; // 駒を反転
							}
							break;
						}

						if (token[nextRow][nextCol].equals("　")) {
							break; // 空白があったら止める
						}

						step++;
					}
				}
			}
		}
	}

	public String placeToken(int row, int col) {
		if (!gameActive) {
			return "{\"error\": \"ゲームがスタートしていません。スタートボタンを押してください。\"}";
		}

		if (row < 0 || col < 0 || row >= 8 || col >= 8 || !token[row][col].equals("　")) {
			return "{\"error\": \"既に駒が配置されています。\"}";
		}

		if (!canPlaceToken(row, col)) {
			return "{\"error\": \"ルール上その場所には駒を置けません。\"}";
		}

		// 駒を配置
		token[row][col] = currentTurn;
		turningTokens(row, col); // 駒を反転
		turnSwitch(); // 次のターンへ切り替え

		String boardHtml = getBoardAsHtml();

		if (isGameOver()) {
			gameActive = false;
			String gameResultHtml = endGame(); // endGame()を呼び出して結果を取得
			currentTurn = "ゲーム終了"; // 現在のターンはゲーム終了時に変更
			return "{\"board\": \"" + boardHtml + "\", \"gameOver\": true, \"result\": \"" + gameResultHtml
					+ "\", \"currentTurn\": \"ゲーム終了\"}";
		}

		// ゲームが終了していない場合
		return "{\"board\": \"" + boardHtml + "\", \"currentTurn\": \"" + currentTurn + "\", \"gameOver\": false}";
	}

	// 駒を置けるかどうかを判定するメソッド
	private boolean canPlaceToken(int row, int col) {
		String currentPlayer = currentTurn; // 現在のプレイヤー情報
		String opponent = (currentPlayer.equals("●")) ? "○" : "●"; // 現在のターンが黒(●)なら相手は白(○)、その逆も

		// 駒があるセルに対しての処理
		if (!token[row][col].equals("　")) {
			return false; // すでに駒が置かれている場所には置けない
		}

		// クリックされた場所が置けるマスか置けないマスか
		int[] directions = { -1, 0, 1 }; // 上、右、下、左、斜めの方向
		for (int dRow : directions) {
			for (int dCol : directions) {
				if (dRow == 0 && dCol == 0)
					continue; // クリックしたマスはスキップする

				int newRow = row + dRow;
				int newCol = col + dCol;

				// クリックしたマスの隣にある駒について
				// 範囲内であるかどうかの判定
				if (newRow >= 0 && newRow < 8 && newCol >= 0 && newCol < 8 &&
						token[newRow][newCol].equals(opponent)) {

					// 挟める駒があるかどうかの判定
					int step = 1; // 移動距離
					while (true) {
						int nextRow = row + dRow * step;
						int nextCol = col + dCol * step;
						if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8)
							break; // 盤面の外に出たら終了

						if (token[nextRow][nextCol].equals(currentPlayer)) {
							return true; // 駒を挟める場所が見つかったらtrue
						}

						if (token[nextRow][nextCol].equals("　")) {
							break; // 空白があったら終了
						}

						step++;
					}
				}
			}
		}

		return false; // 置けない場所の場合falseを返す
	}

	// 黒の駒の数を取得
	public int getBlackCount() {
		int blackCount = 0;
		for (int row = 0; row < token.length; row++) {
			for (int col = 0; col < token[row].length; col++) {
				if (token[row][col].equals("●")) {
					blackCount++;
				}
			}
		}
		return blackCount;
	}

	// 白の駒の数を取得
	public int getWhiteCount() {
		int whiteCount = 0;
		for (int row = 0; row < token.length; row++) {
			for (int col = 0; col < token[row].length; col++) {
				if (token[row][col].equals("○")) {
					whiteCount++;
				}
			}
		}
		return whiteCount;
	}

	// ゲーム終了後、対戦結果を現在のターン部分に追加する
	public String getGameResultHtml() {
		String result = getGameResult(); // ゲーム結果の取得

		int blackCount = getBlackCount();
		int whiteCount = getWhiteCount();

		StringBuilder sb = new StringBuilder();
		sb.append("<p>黒の駒: ").append(blackCount).append("</p>");
		sb.append("<p>白の駒: ").append(whiteCount).append("</p>");
		sb.append("<p>ゲーム結果: ").append(result).append("</p>");

		return sb.toString(); // これが返される
	}

	// ゲーム終了後に結果をJSONで返すメソッド
	public String endGame() {
		int blackCount = getBlackCount(); // 黒の駒数
		int whiteCount = getWhiteCount(); // 白の駒数
		String gameResult = getGameResultHtml(); // 勝敗結果（「黒の勝ち」「白の勝ち」「引き分け」）

		// JSON形式で結果を返す
		return "{\"blackCount\": " + blackCount + ", \"whiteCount\": " + whiteCount + ", \"gameResult\": \""
				+ gameResult + "\"}";
	}

	public String getBoardAsHtml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<table class='OthelloBoard' border='1' style='table-layout: fixed;'>");
		sb.append("<tr><th></th><th>a</th><th>b</th><th>c</th><th>d</th><th>e</th><th>f</th><th>g</th><th>h</th></tr>");

		for (int i = 0; i < 8; i++) {
			sb.append("<tr><th>").append(i + 1).append("</th>");
			for (int j = 0; j < 8; j++) {
				String imageSrc = "";
				String onClickEvent = "onclick='placeToken(this)'";

				// 画像の指定
				if (token[i][j].equals("○")) {
					imageSrc = "/othello_game/game/images/white.png";
				} else if (token[i][j].equals("●")) {
					imageSrc = "/othello_game/game/images/black.png";
				}

				sb.append("<td class='empty' data-row='" + i + "' data-col='" + j + "' " + onClickEvent + ">");
				sb.append("<div class='piece-container'>");
				if (!imageSrc.isEmpty()) {
					sb.append("<img src='" + imageSrc + "' alt='駒' class='piece' width='50' height='50'>");
				}
				sb.append("</div>");
				sb.append("</td>");

			}
			sb.append("</tr>");
		}

		sb.append("</table>");
		return sb.toString();
	}

	// ゲーム結果を計算するメソッド
	public String getGameResult() {
		int blackCount = getBlackCount();
		int whiteCount = getWhiteCount();

		// ゲームの勝者を決定
		if (blackCount > whiteCount) {
			return "黒の勝ち";
		} else if (whiteCount > blackCount) {
			return "白の勝ち";
		} else {
			return "引き分け";
		}
	}
}
