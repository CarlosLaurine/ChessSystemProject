package application;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

public class ChessProgram {

	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);
		ChessMatch chessMatch = new ChessMatch();
		List<ChessPiece> captured = new ArrayList<>();

		while (!chessMatch.getCheckMate()) {
			try {
				UI.clearScreen();
				UI.printMatch(chessMatch, captured);
				System.out.println();
				System.out.println("Source: ");
				ChessPosition source = UI.readChessPosition(sc);

				boolean[][] possibleMoves = chessMatch.possibleMoves(source);
				UI.clearScreen();
				UI.printBoard(chessMatch.getPieces(), possibleMoves);

				System.out.println();
				System.out.println("Target: ");
				ChessPosition target = UI.readChessPosition(sc);

				ChessPiece capturedPiece = chessMatch.performChessMove(source, target);

				if (capturedPiece != null) {
					captured.add(capturedPiece);
				}

				if (chessMatch.getPromoted() != null) {

					System.out.println("Enter piece for promotion [B]/[H]/[R]/[Q]".toUpperCase());
					String promType = sc.nextLine().toUpperCase();
					while (!promType.equals("B") && !promType.equals("H") && !promType.equals("R") && !promType.equals("Q")) {
						System.out.println(
								"Invalid Input!! Enter a correct value for piece promotion [B]/[H]/[R]/[Q]".toUpperCase());
						promType = sc.nextLine().toUpperCase();
					}

					chessMatch.replacePromotedPiece(promType);
				}
			} catch (ChessException e) {
				System.out.println(e.getMessage());
				sc.nextLine();
			} catch (InputMismatchException e) {
				System.out.println(e.getMessage());
				sc.nextLine();
			}
		}

		UI.clearScreen();
		UI.printMatch(chessMatch, captured);

	}
}
