package tablut;

import java.util.*;

import static tablut.Piece.*;
import static tablut.Square.*;

import static tablut.Move.mv;


/** The state of a Tablut Game.
 *  @author skyler
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 9;

    /** The throne (or castle) square and its four surrounding squares.. */
    static final Square THRONE = sq(4, 4),
        NTHRONE = sq(4, 5),
        STHRONE = sq(4, 3),
        WTHRONE = sq(3, 4),
        ETHRONE = sq(5, 4);

    /** Initial positions of attackers. */
    static final Square[] INITIAL_ATTACKERS = {
        sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
        sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
        sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
        sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /** Initial positions of defenders of the king. */
    static final Square[] INITIAL_DEFENDERS = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE,
        sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        init();
    }

    /** Clears the board to the initial position. */
    void init() {

        map = new HashMap<Square, Piece>();
        for (Square sq: INITIAL_ATTACKERS) {
            map.put(sq, BLACK);
        }
        for (Square sq: INITIAL_DEFENDERS) {
            map.put(sq, WHITE);
        }
        king = sq(4, 4);
        map.put(king, KING);
        for (int i = 0; i <= 8; i++) {
            for (int j = 0; j <= 8; j++) {
                if (!map.containsKey(sq(i, j))) {
                    map.put(sq(i, j), EMPTY);
                }
            }
        }

        board = new Piece[9][9];

        for (Square keys : map.keySet()) {
            board[keys.col()][keys.row()] = map.get(keys);
        }
    }

    /** @param n for moveLimit
     * Set the move limit to LIM.  It is an error if 2*LIM <= moveCount(). */
    void setMoveLimit(int n) {
        moveLimit = n;
    }

    /** Return a Piece representing whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the winner in the current position, or null if there is no winner
     *  yet. */
    Piece winner() {
        return _winner;
    }

    /** Returns true iff this is a win due to a repeated position. */
    boolean repeatedPosition() {
        if (_repeated && _winner == BLACK) {
            System.out.println("* Black wins");
        } else if (_repeated && _winner == WHITE) {
            System.out.println("* White wins");
        }
        return _repeated;
    }

    /** Record current position and set winner() next mover if the current
     *  position is a repeat. */
    void checkRepeated() {
        Move check = listOfMoves.peek();
        if (map.containsKey(check)) {
            _repeated = true;
            _winner = map.get(check.from()).opponent();
        }
        _repeated = false;
    }


    /** Return the number of moves since the initial position that have not been
     *  undone. */
    int moveCount() {
        return _moveCount;
    }

    /** Return location of the king. */
    Square kingPosition() {
        return king;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return board[col][row];
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        if (p == KING) {
            map.put(kingPosition(), EMPTY);
            king = s;
        }
        board[s.col()][s.row()] = p;
    }

    /** Set square S to P and record for undoing. */
    final void revPut(Piece p, Square s) {
        listOfBoards.push(encodedBoard());
        put(p, s);
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /** Return true iff FROM - TO is an unblocked rook move on the current
     *  board.  For this to be true, FROM-TO must be a rook move and the
     *  squares along it, other than FROM, must be empty. */
    boolean isUnblockedMove(Square from, Square to) {
        int dir = from.direction(to);
        SqList rook = ROOK_SQUARES[from.index()][dir];
        int indexTo = rook.indexOf(to);
        for (int i = indexTo; i >= 0; i--) {
            if (map.get(rook.get(i)) != EMPTY) {
                return false;
            }
        }
        return true;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }

    /** Return true iff FROM-TO is a valid move. */
    boolean isLegal(Square from, Square to) {
        Move move = mv(from, to);
        if (move == null) {
            System.out.println("Invalid Move");
            return false;
        }
        if (to == kingPosition() || map.get(to) != EMPTY) {
            System.out.println("Invalid Move");
            return false;
        }
        List<Move> legal = legalMoves(map.get(from));
        return legal.contains(move);
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /** Move FROM-TO, assuming this is a legal move. */
    void makeMove(Square from, Square to) {
        assert isLegal(from, to);

        if (kingPosition().isEdge()) {
            _winner = WHITE;
            listOfMoves.push(mv(kingPosition(), sq(0, 0)));
            System.out.println(winner());
            checkRepeated();
            return;
        }

        Piece side = map.get(from);
        if (_turn == side.opponent()) {
            return;
        }
        listOfMoves.push(mv(from, to));
        revPut(map.get(from), to);
        map.put(from, EMPTY);
        map.put(to, side);
        board[from.col()][from.row()] = EMPTY;
        legalMoves(side);
        for (Square sq : map.keySet()) {
            if (map.get(sq) == side.opponent()) {
                if (to.isRookMove(sq)) {
                    Square captureAble = to.rookMove(to.direction(sq), 2);
                    if (to.adjacent(sq) && ((map.get(captureAble) == side)
                            || (captureAble == THRONE
                            && map.get(THRONE) == EMPTY))) {
                        capture(to, captureAble);
                    }
                }
            }
        }

        _turn = _turn.opponent();
        _moveCount += 1;
        checkRepeated();
    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /** Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     *  SQ0 and the necessary conditions are satisfied. */
    private void capture(Square sq0, Square sq2) {
        Square between = sq0.between(sq2);
        map.put(between, EMPTY);
        board[between.col()][between.row()] = EMPTY;
        if (map.get(between) == KING) {
            _winner = BLACK;
        }
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (_moveCount > 0) {
            undoPosition();
        }
    }

    /** Remove record of current position in the set of positions encountered,
     *  unless it is a repeated position or we are at the first move. */
    private void undoPosition() {
        String undoingBoard = listOfBoards.pop();
        listOfMoves.pop();
        for (Square sq : map.keySet()) {
            char piece = undoingBoard.charAt(sq.index() + 1);
            if (piece == '-') {
                map.put(sq, EMPTY);
            }
            if (piece == 'B') {
                map.put(sq, BLACK);
            }
            if (piece == 'W') {
                map.put(sq, WHITE);
            }
            if (piece == 'K') {
                map.put(sq, KING);
            }
            board[sq.col()][sq.row()] = map.get(sq);
        }
        _moveCount -= 1;
        _turn = _turn.opponent();
        _repeated = false;
    }

    /** Clear the undo stack and board-position counts. Does not modify the
     *  current position or win status. */
    void clearUndo() {
        while (!listOfBoards.isEmpty()) {
            listOfMoves.pop();
            listOfBoards.pop();
        }
    }

    /** Return a new mutable list of all legal moves on the current board for
     *  SIDE (ignoring whose turn it is at the moment). */
    List<Move> legalMoves(Piece side) {
        legalMovesArr = new ArrayList<Move>();
        HashSet<Square> pieceSide = pieceLocations(side);
        for (Square pieces : pieceSide) {
            for (int i = 0; i <= 8; i++) {
                Move x = mv(pieces, sq(pieces.col(), i));
                legalMovesArr.add(x);
            }
            for (int j = 0; j <= 8; j++) {
                Move y = mv(pieces, sq(j, pieces.row()));
                legalMovesArr.add(y);
            }
            while (legalMovesArr.remove(null));
        }
        return legalMovesArr;
    }

    /** Return true iff SIDE has a legal move. */
    boolean hasMove(Piece side) {
        List<Move> listOfLegalMoves = legalMoves(side);
        return listOfLegalMoves != null;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /** Return a text representation of this Board.  If COORDINATES, then row
     *  and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /** Return the locations of all pieces on SIDE. */
    HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> squareSides = new HashSet<Square>();
        for (Square mapSquare : map.keySet()) {
            if (get(mapSquare) == side) {
                squareSides.add(mapSquare);
            }
        }
        return squareSides;
    }

    /** Return the contents of _board in the order of SQUARE_LIST as a sequence
     *  of characters: the toString values of the current turn and Pieces. */
    private String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /** Piece whose turn it is (WHITE or BLACK). */
    private Piece _turn = BLACK;
    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;
    /** Number of (still undone) moves since initial position. */
    private int _moveCount;
    /** True when current board is a repeated position (ending the game). */
    private boolean _repeated;
    /** 1. */
    private Square king;
    /** 2. */
    private Piece[][] board;
    /** 3. */
    private Stack<Move> listOfMoves = new Stack<Move>();
    /** 4. */
    private Stack<String> listOfBoards = new Stack<String>();
    /** 5. */
    private HashMap<Square, Piece> map;
    /** 6. */
    private List<Move> legalMovesArr;
    /** 7. */
    private int moveLimit = 5;

}
