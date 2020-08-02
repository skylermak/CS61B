package tablut;

import org.junit.Test;
import static tablut.Square.sq;

import ucb.junit.textui;

/** The suite of all JUnit tests for the enigma package.
 *  @author skyler
 */
public class UnitTest {

    /**
     * Run the JUnit tests in this package. Add xxxTest.class entries to
     * the arguments of runClasses to run other JUnit tests.
     */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    @Test
    public void checkBoard() {
        Board board = new Board();
    }

    @Test
    public void check() {
        System.out.println(new Board());
    }

    @Test
    public void checkPut() {
        Board board = new Board();
        board.revPut(Piece.KING, sq(1, 1));
        System.out.println(board);
        System.out.println(board.get(1, 1));
        System.out.println(board.kingPosition());
    }

    @Test
    public void checkGet() {
        Board board = new Board();
        System.out.println(board.get(2, 2));
    }

    @Test
    public void checkKingPosition() {
        Board board = new Board();
        System.out.println(board.kingPosition());
    }

    @Test
    public void checkPiecesLocation() {
        Board board = new Board();
        System.out.println(board.pieceLocations(Piece.BLACK));
        System.out.println(board.pieceLocations(Piece.WHITE));
    }

    @Test
    public void checkRookMoves() {
        Board board = new Board();
        System.out.println(board);
        System.out.println(board.legalMoves(Piece.WHITE));
    }

    @Test
    public void checkHasMove() {
        Board board = new Board();
        System.out.println(board.hasMove(Piece.WHITE));
    }

    @Test
    public void checkIsLegal() {
        Board board = new Board();
        System.out.println(board.isLegal((sq(4, 2)), sq(5, 2)));
    }

    @Test
    public void checkMakeMove() {
        Board board = new Board();
        System.out.println(board);
        board.makeMove(sq(4, 1), sq(5, 1));
        System.out.println(board);
        System.out.println(board.moveCount());
    }

    /**
     * fix this... doesn't allow repeated positions in the first place.)
     */
    @Test
    public void checkRepeated() {
        Board board = new Board();
        System.out.println(board);
        board.makeMove(sq(4, 7), sq(5, 7));
        System.out.println(board);
        board.makeMove(sq(4, 2), sq(5, 2));
        System.out.println(board);
        board.makeMove(sq(5, 7), sq(6, 7));
        System.out.println(board);
        board.makeMove(sq(5, 2), sq(6, 2));
        System.out.println(board);
        board.makeMove(sq(6, 7), sq(7, 7));
        System.out.println(board);
        board.makeMove(sq(6, 2), sq(6, 1));
        System.out.println(board);
        board.makeMove(sq(7, 7), sq(8, 7));
        System.out.println(board);
        board.makeMove(sq(4, 4), sq(4, 2));
        System.out.println(board);
        board.undo();
        System.out.println(board);
        System.out.println(board.moveCount());
        board.checkRepeated();
        System.out.println(board.repeatedPosition());
        System.out.println(board.kingPosition());
        board.clearUndo();
        System.out.println(board);
        System.out.println(board.isUnblockedMove(sq(4, 1), sq(6, 1)));
    }

    @Test
    public void checkCapture() {
        Board board = new Board();
        Square s1 = sq(7, 5);
        Square s2 = sq(7, 3);
        System.out.println(board);
        board.makeMove(sq(0, 3), sq(1, 3));
        System.out.println(board);
        board.makeMove(sq(4, 5), s1);
        System.out.println(board);
        board.makeMove(sq(1, 3), sq(2, 3));
        System.out.println(board);
        board.makeMove(sq(4, 3), s2);
        System.out.println(board);
        board.undo();
        System.out.println(board);
    }

    @Test
    public void checkUndoAgain() {
        Board board = new Board();
        board.makeMove(sq(4, 1), sq(5, 1));
        System.out.println(board);
        board.makeMove(sq(4, 2), sq(5, 2));
        System.out.println(board);
        board.makeMove(sq(5, 1), sq(6, 1));
        System.out.println(board);
        board.makeMove(sq(5, 2), sq(6, 2));
        System.out.println(board);
        board.undo();
        System.out.println(board);
        System.out.println(board.turn());
        board.makeMove(sq(5, 2), sq(6, 2));
        System.out.println(board);
        board.makeMove(sq(6, 1), sq(7, 1));
        System.out.println(board);
        board.undo();
        System.out.println(board);
        System.out.println(board.turn());
    }

    @Test
    public void checkWin() {
        Board board = new Board();
        board.makeMove(sq(4, 1), sq(5, 1));
        System.out.println(board);
        board.makeMove(sq(4, 3), sq(5, 3));
        System.out.println(board);
        board.makeMove(sq(5, 1), sq(6, 1));
        System.out.println(board);
        board.makeMove(sq(4, 2), sq(5, 2));
        System.out.println(board);
        board.makeMove(sq(6, 1), sq(7, 1));
        System.out.println(board);
        board.makeMove(sq(4, 4), sq(4, 2));
        System.out.println(board);
        board.makeMove(sq(7, 1), sq(8, 1));
        System.out.println(board);
        board.makeMove(sq(4, 2), sq(0, 2));
        System.out.println(board);
        board.makeMove(sq(8, 1), sq(8, 2));
        System.out.println(board);
        board.makeMove(sq(0, 2), sq(0, 1));
        System.out.println(board);
    }
}



