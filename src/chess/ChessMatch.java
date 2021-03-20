package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import board.Board;
import board.Piece;
import board.Position;
import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Queen;
import chess.pieces.Rook;

public class ChessMatch {
	
	private Board board;
	private Color currentPlayer;
	private int turn;
	private boolean check;
	private boolean checkMate;
	private ChessPiece enPassantVulnerable;
	
	private List<Piece> piecesOnTheBoard = new ArrayList<>();
	private List<Piece> capturedPieces = new ArrayList<>();	
	
	public ChessMatch() {
		
		board = new Board(8,8);
		turn = 1;
		currentPlayer = Color.WHITE;
		check =false;
		initialSetup();
		enPassantVulnerable = null;
	}
	
	public int getTurn() {
		return turn;
	}

	public Color getCurrentPlayer() {
		return currentPlayer;
	}
	public boolean getCheck() {
		return check;
	}
	
	public boolean getCheckMate() {
		return checkMate;
	}
	
	public ChessPiece getEnPassantVulnerable() {
		return enPassantVulnerable;
	}
	
	public ChessPiece[][] getPieces(){
		ChessPiece[][] matrix = new ChessPiece[board.getRows()][board.getColumns()];
		for (int i=0; i<board.getRows(); i++) {
			for(int j=0; j<board.getColumns(); j++) {
				matrix[i][j] = (ChessPiece) board.piece(i,j);
			}
		}
		return matrix;
		
	}
	
	public boolean[][] possibleMoves(ChessPosition sourcePosition){
		Position position = sourcePosition.toPosition();
		validateSourcePosition(position);
		return board.piece(position).possibleMoves();
	}
	
	public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
	Position source = sourcePosition.toPosition();
	Position target = targetPosition.toPosition();
	validateSourcePosition(source);
	validateTargetPosition(source, target);
	Piece capturedPiece = makeMove(source,target);
	
	if(testCheck(currentPlayer)) {
		undoMove(source, target, capturedPiece);
		throw new ChessException ("Invalid move, you are not allowed to put yourself in Check");
		
	}
	
	ChessPiece movedPiece = (ChessPiece)board.piece(target);
	
	check = (testCheck(opponent(currentPlayer))) ? true : false;
	
	if(testCheckMate(opponent(currentPlayer))) {
		checkMate = true;
	}
	else {
		nextTurn();
	}
	
	// SPECIAL MOVE EN PASSANT
	if(movedPiece instanceof Pawn && (target.getRow() == source.getRow() + 2 || target.getRow() == source.getRow() - 2)) {
		enPassantVulnerable = movedPiece;
	}
	//else{ enPassantVulnerable = null; }

	return (ChessPiece) capturedPiece;
	}
	
	private Piece makeMove(Position source, Position target) {
		ChessPiece p = (ChessPiece)board.removePiece(source);
		p.increaseMoveCount();
		Piece capturedPiece = board.removePiece(target);
		board.placePiece(p,  target);
		if(capturedPiece !=null) {
			piecesOnTheBoard.remove(capturedPiece);
			capturedPieces.add(capturedPiece);
		}
		//SPECIAL MOVE CASTLING - "SMALL" CASTLING (KINGSIDE ROOK)
		if (p instanceof King && target.getColumn() == source.getColumn() +2) {
			Position sourceR = new Position(source.getRow(), source.getColumn()+3);
			Position targetR = new Position(source.getRow(), source.getColumn()+1);
			ChessPiece rook = (ChessPiece)board.removePiece(sourceR);
			board.placePiece(rook, targetR);
			rook.increaseMoveCount();
		}
		//SPECIAL MOVE CASTLING - "LONG" CASTLING (QUEENSIDE ROOK)
				if (p instanceof King && target.getColumn() == source.getColumn() -2) {
					Position sourceR = new Position(source.getRow(), source.getColumn()-4);
					Position targetR = new Position(source.getRow(), source.getColumn()-1);
					ChessPiece rook = (ChessPiece)board.removePiece(sourceR);
					board.placePiece(rook, targetR);
					rook.increaseMoveCount();
				}
		
		return capturedPiece;
	}
	
	private void undoMove(Position source, Position target, Piece capturedPiece) {
		ChessPiece p = (ChessPiece) board.removePiece(target);
		p.decreaseMoveCount();
		board.placePiece(p, source);
		
		
		if (capturedPiece !=null) {
			board.placePiece(capturedPiece, target);
			capturedPieces.remove(capturedPiece);
			piecesOnTheBoard.add(capturedPiece);
		}
	
		//UNDO SPECIAL MOVE CASTLING - "SMALL" CASTLING (KINGSIDE ROOK)
				if (p instanceof King && target.getColumn() == source.getColumn() +2) {
					Position sourceR = new Position(source.getRow(), source.getColumn()+3);
					Position targetR = new Position(source.getRow(), source.getColumn()+1);
					ChessPiece rook = (ChessPiece)board.removePiece(targetR);
					board.placePiece(rook, sourceR);
					rook.decreaseMoveCount();
				}
				//SPECIAL MOVE CASTLING - "LONG" CASTLING (QUEENSIDE ROOK)
						if (p instanceof King && target.getColumn() == source.getColumn() -2) {
							Position sourceR = new Position(source.getRow(), source.getColumn()-4);
							Position targetR = new Position(source.getRow(), source.getColumn()-1);
							ChessPiece rook = (ChessPiece)board.removePiece(targetR);
							board.placePiece(rook, sourceR);
							rook.decreaseMoveCount();
						}
	
	}
	
	private void validateTargetPosition(Position source, Position target) {
		
		if(!board.piece(source).possibleMove(target)) {
			throw new ChessException("The picked piece cannot move to chosen target position");
		}
	}
	
	private void validateSourcePosition(Position position) {
		if(!board.thereIsAPiece(position)) {
			throw new ChessException("The Source Position picked has no Piece on it");
		}
		if(currentPlayer!= ((ChessPiece)board.piece(position)).getColor()) {
			throw new ChessException ("Adversary's Piece Picked. Choose One of Yours");
		}
		if(!board.piece(position).isThereAnyPossibleMove()) {
			throw new ChessException("No Moves available for the chosen Piece");
		}
	}
	
	private boolean testCheckMate(Color color) {
		if(!testCheck(color)) {
			return false;
		}
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
		for(Piece p: list) {
			boolean[][] matrix = p.possibleMoves();
			for(int i=0; i<board.getRows(); i++) {
				for(int j=0; j<board.getColumns(); j++) {
					if(matrix[i][j]) {
						Position source = ((ChessPiece)p).getChessPosition().toPosition();
						Position target = new Position(i, j);
						Piece capturedPiece = makeMove(source, target);
						boolean testCheck = testCheck(color);
						undoMove(source, target, capturedPiece);
						if(!testCheck) {
							return false;
						}
						
					}
				}
			}
		}
	return true;
	
	}
	
	private void placeNewPiece (char column, int row, ChessPiece piece) {
		board.placePiece(piece, new ChessPosition(column,row).toPosition());
		piecesOnTheBoard.add(piece);
	}
	
	private void nextTurn() {
		turn++;
		currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
	}
	
	private Color opponent(Color color) {
		return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
	}
	
	private ChessPiece king(Color color) {
		List <Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
		
		for(Piece p : list) {
			if(p instanceof King) {
			return (ChessPiece)p;
			}
			}
		throw new IllegalStateException("Unexpected ERROR - There is no "+ color.toString().toUpperCase() + " King on the Board!");
	}
	
	private boolean testCheck(Color color) {
		Position kingPosition = king(color).getChessPosition().toPosition();
		List <Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(color)).collect(Collectors.toList());
		for(Piece p : opponentPieces) {
			boolean [][] matrix = p.possibleMoves();
			if(matrix[kingPosition.getRow()][kingPosition.getColumn()]) {
				return true;
			}
		}
		return false;
		
	}
	
	private void initialSetup() {
		
		placeNewPiece('a', 1, new Rook(board, Color.WHITE));
		placeNewPiece('b', 1, new Knight(board, Color.WHITE));
		placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
		placeNewPiece('d', 1, new Queen(board, Color.WHITE));

        placeNewPiece('e', 1, new King(board, Color.WHITE, this));
		placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
		placeNewPiece('g', 1, new Knight(board, Color.WHITE));
        placeNewPiece('h', 1, new Rook(board, Color.WHITE));
        placeNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('c', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('d', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('h', 2, new Pawn(board, Color.WHITE, this));

        placeNewPiece('a', 8, new Rook(board, Color.BLACK));
        placeNewPiece('b', 8, new Knight(board, Color.BLACK));
        placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
		placeNewPiece('d', 8, new Queen(board, Color.BLACK));

        
        placeNewPiece('e', 8, new King(board, Color.BLACK, this));
        placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('g', 8, new Knight(board, Color.BLACK));
        placeNewPiece('h', 8, new Rook(board, Color.BLACK));
        placeNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('h', 7, new Pawn(board, Color.BLACK, this));
	}

}
