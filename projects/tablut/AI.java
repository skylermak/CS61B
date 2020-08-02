package tablut;

import static tablut.Piece.*;

import java.util.List;

/** A Player that automatically generates moves.
 *  @author skyler
 */
class AI extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A position-score magnitude indicating a forced win in a subsequent
     *  move.  This differs from WINNING_VALUE to avoid putting off wins. */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move move = findMove();
        System.out.println("* " + move);
        return move.toString();
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        if (myPiece() == WHITE) {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        } else {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        Move best;
        best = null;
        int bestsofar = -INFTY;

        if (depth == 0) {
            return staticScore(board);
        }

        List<Move> moves = board.legalMoves(myPiece());
        for (Move move : moves) {
            if (sense == 1) {
                bestsofar = miniMax(board, move, depth, sense, alpha, beta);
                alpha = Math.max(bestsofar, alpha);
                best = move;
                if (beta <= alpha) {
                    break;
                }
            } else if (sense == -1) {
                bestsofar = miniMax(board, move, depth, sense, alpha, beta);
                beta = Math.min(bestsofar, beta);
                best = move;
                if (beta <= alpha) {
                    break;
                }
            }
        }
        if (saveMove) {
            _lastFoundMove = best;
        }
        return bestsofar;
    }

    /**
     * @param board current board.
     * @param move move.
     * @param depth search depth.
     * @param sense white or black.
     * @param alpha alpha.
     * @param beta beta.
     * @return value
     * miniMax function.
     * */
    private int miniMax(Board board, Move move, int depth,
                       int sense, int alpha, int beta) {
        Board b = new Board(board);
        b.makeMove(move);
        if (b.repeatedPosition() && sense == 1) {
            return INFTY;
        } else if (b.repeatedPosition() && sense == -1) {
            return -INFTY;
        }
        int x = findMove(b, depth - 1, false, sense * (-1), alpha, beta);
        board.undo();
        return x;
    }

    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private static int maxDepth(Board board) {
        return 3;
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        int white = 0;
        int black = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board.get(i, j)
                        != EMPTY && board.get(i, j).equals(WHITE)) {
                    white += 1;
                } else if (board.get(i, j)
                        != EMPTY && board.get(i, j).equals(BLACK)) {
                    black += 1;
                }
            }
        }
        return (white - black);
    }
}

