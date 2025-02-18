package othelloGame;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.OthelloToken;

public class GameServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		OthelloToken game = (OthelloToken) session.getAttribute("game");

		String action = request.getParameter("action");

		// System.out.println("Received action: " + action); // 受け取ったアクションを表示

		if ("startGame".equals(action)) {
			if (game == null) {
				game = new OthelloToken();
				session.setAttribute("game", game);
			}
			game.startGame();
			session.setAttribute("gameActive", true);

			response.setContentType("application/json");
			String boardHtml = game.getBoardAsHtml();
			String currentTurn = game.getCurrentTurn();
			response.getWriter().write("{\"board\": \"" + boardHtml + "\", \"currentTurn\": \"" + currentTurn + "\"}");
		} else if ("restartGame".equals(action)) {
			if ("restartGame".equals(action)) {
				if (game != null) {
					game.startGame(); // 新しいゲームを開始
				}
				session.setAttribute("gameActive", true); // ゲーム状態をアクティブに設定
				session.setAttribute("currentTurn", "●"); // ターンを黒（●）に初期化
				session.setAttribute("gameResult", null); // ゲーム結果をリセット

				response.setContentType("application/json");
				String boardHtml = game.getBoardAsHtml();
				String currentTurn = game.getCurrentTurn();
				response.getWriter()
						.write("{\"board\": \"" + boardHtml + "\", \"currentTurn\": \"" + currentTurn + "\"}");
			}

		} else if ("placeToken".equals(action)) {

			// ゲームがまだ開始されていない場合
			if (session.getAttribute("gameActive") == null || !(boolean) session.getAttribute("gameActive")) {
				response.setContentType("application/json");
				response.getWriter().write("{\"error\": \"ゲームがスタートしていません。スタートボタンを押してください。\"}");
				return;
			}

			try {
				String rowStr = request.getParameter("row");
				String colStr = request.getParameter("col");

				if (rowStr == null || colStr == null) {
					response.setContentType("application/json");
					response.getWriter().write("{\"error\": \"行または列が正しく送信されていません。\"}");
					return;
				}

				int row = Integer.parseInt(rowStr);
				int col = Integer.parseInt(colStr);

				System.out.println("リクエストデータ：行" + row + ",列：" + col);

				// 駒を配置する処理
				String result = game.placeToken(row, col);
				System.out.println("placeToken 結果: " + result); // placeTokenの結果を表示

				// ゲーム終了後の処理
				if (game.isGameOver()) {
					System.out.println("ゲーム終了が確認されました。"); // ゲーム終了が確認されたことをログに追加
					session.setAttribute("gameActive", false);

					int blackCount = game.getBlackCount();
					int whiteCount = game.getWhiteCount();
					String winner = "";

					if (blackCount > whiteCount) {
						winner = "黒のプレイヤーの勝利";
					} else if (blackCount < whiteCount) {
						winner = "白のプレイヤーの勝利";
					} else {
						winner = "引き分け";
					}

					// ゲーム終了時に結果と盤面を返す
					response.setContentType("application/json");
					response.getWriter()
							.write("{\"winner\": \"" + winner + "\", \"blackCount\": " + blackCount
									+ ", \"whiteCount\": " + whiteCount + ", \"gameOver\": true, \"board\": \""
									+ game.getBoardAsHtml() + "\"}");
					return;
				}

				// ゲームが終了していない場合のレスポンス
				response.setContentType("application/json");
				response.getWriter().write(result);

			} catch (NumberFormatException e) {
				// 無効な入力があった場合のエラーハンドリング
				response.setContentType("application/json");
				response.getWriter().write("{\"error\": \"無効な入力です。行と列には整数を入力してください。\"}");
			} catch (Exception e) {
				// その他の例外をキャッチして、エラーメッセージを返す
				response.setContentType("application/json");
				response.getWriter().write("{\"error\": \"サーバーエラーが発生しました: " + e.getMessage() + "\"}");
			}
		} else if (game.isGameOver()) {
			// ゲーム結果を計算
			String gameResult = game.getGameResult(); // getGameResult() メソッドを呼び出して結果を取得

			// ゲーム結果をセッションにセット
			request.setAttribute("gameResult", gameResult);

			// ゲーム終了のフラグをセッションにセット
			request.setAttribute("gameOver", "true");

			// 盤面のHTMLをセット
			request.setAttribute("boardHtml", game.getBoardAsHtml());
			request.setAttribute("currentTurn", game.getCurrentTurn());

			// ゲームの結果表示に必要な情報も渡します
			request.setAttribute("blackCount", game.getBlackCount());
			request.setAttribute("whiteCount", game.getWhiteCount());
		}

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		OthelloToken game = (OthelloToken) session.getAttribute("game");

		// ゲームが開始されていない場合のエラーハンドリング
		if (game == null || session.getAttribute("gameActive") == null
				|| !(boolean) session.getAttribute("gameActive")) {
			response.setContentType("application/json");
			response.getWriter().write("{\"error\": \"ゲームが開始されていません。スタートボタンを押してください。\"}");
		} else {
			String boardHtml = game.getBoardAsHtml();
			String currentTurn = game.getCurrentTurn();
			response.setContentType("application/json");
			response.getWriter().write("{\"board\": \"" + boardHtml + "\", \"currentTurn\": \"" + currentTurn + "\"}");
		}
	}
}
