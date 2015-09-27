package com.star3.checkers.services;


import com.star3.checkers.models.Color;
import com.star3.checkers.models.Pawn;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Validation violations:
 * #1 more than one pawns of current player moved
 * #2 pawn moved vertically
 * #3 pawn moved horizontally
 * #4 pawn moved more than one spot further
 * #5 pawn moved on used spot
 * #6 not checking pawn addition - fucks given: 0
 */
public class MoveValidator {
    private Pawn[][] oldBoard;
    private Pawn[][] newBoard;
    private int[][] diffBoard;
    private Pawn oldMovedPawn;
    private Pawn newMovedPawn;
    private static final int BOARD_SIDE_LENGTH = 8;
    private static final int NOT_CHANGED = 0;
    private static final int CHANGED_COLOR = 1;
    private static final int CURRENT_COLOR_DEFICIENCY = 2;
    private static final int ENEMY_COLOR_DEFICIENCY = 3;
    private static final int MOVED_PAWN_DESTINATION = 4;
    private static final Color BEGINS_FROM_TOP = Color.RED;
    private static final Color BEGINS_FROM_BOTTOM = Color.BLUE;


    private Color currentPlayer;

    public MoveValidator() {

    }

    public MoveValidator(Pawn oldBoard[][], Pawn newBoard[][], Color currentPlayer) {
        initialize(oldBoard, newBoard, currentPlayer);
    }

    public void initialize(Pawn oldBoard[][], Pawn newBoard[][], Color currentPlayer) {
        this.currentPlayer = currentPlayer;
        this.oldBoard = Arrays.copyOf(oldBoard, oldBoard.length);
        this.newBoard = Arrays.copyOf(newBoard, newBoard.length);
        this.diffBoard = compareBoards();
    }

    private int[][] compareBoards() {
        int[][] result = new int[BOARD_SIDE_LENGTH][BOARD_SIDE_LENGTH];
        for (int[] row : result)
            Arrays.fill(row, 0);

        for (int i = 0; i < BOARD_SIDE_LENGTH; i++) {
            for (int j = 0; j < BOARD_SIDE_LENGTH; j++) {

                if (oldBoard[i][j] == null && newBoard[i][j] == null || (oldBoard[i][j] != null && newBoard[i][j] != null && oldBoard[i][j].getColor().equals(newBoard[i][j].getColor()))) {
                    result[i][j] = NOT_CHANGED;
                } else if (oldBoard[i][j] != null && newBoard[i][j] != null && !(oldBoard[i][j].getColor().equals(newBoard[i][j].getColor()))) {
                    result[i][j] = CHANGED_COLOR;
                } else if (oldBoard[i][j] != null && oldBoard[i][j].getColor().equals(currentPlayer) && newBoard[i][j] == null) {
                    result[i][j] = CURRENT_COLOR_DEFICIENCY;
                } else if (oldBoard[i][j] != null && !oldBoard[i][j].getColor().equals(currentPlayer) && newBoard[i][j] == null) {
                    result[i][j] = ENEMY_COLOR_DEFICIENCY;

                } else if (oldBoard[i][j] == null && newBoard[i][j] != null && newBoard[i][j].getColor().equals(currentPlayer)) {
                    result[i][j] = MOVED_PAWN_DESTINATION;

                }
            }
        }

        return result;


    }

    private boolean validatePawnsNumber() {
        int currPlayerPawnsDeficiencyCounter = 0;
        int movedPawnDestCounter = 0;

        Pawn tmpOld = null;
        Pawn tmpNew = null;


        for (int j = 0; j < BOARD_SIDE_LENGTH; j++) {
            for (int i = 0; i < BOARD_SIDE_LENGTH; i++) {
                if (diffBoard[j][i] == CURRENT_COLOR_DEFICIENCY) {
                    currPlayerPawnsDeficiencyCounter++;
                    tmpOld = new Pawn(j, i, currentPlayer);

                }
                if (diffBoard[j][i] == MOVED_PAWN_DESTINATION) {
                    movedPawnDestCounter++;
                    tmpNew = new Pawn(j, i, currentPlayer);

                }
                if (diffBoard[j][i] == CHANGED_COLOR || currPlayerPawnsDeficiencyCounter > 1 || movedPawnDestCounter > 1) {
                    return false;
                }
            }
        }
        oldMovedPawn = tmpOld;
        newMovedPawn = tmpNew;

        if (oldMovedPawn != null && newMovedPawn != null)
            return true;
        else
            return false;
    }

    private int getEnemyColorDeficiencyNumber() {
        int countEnemyColorDeficiency = 0;
        for (int j = 0; j < BOARD_SIDE_LENGTH; j++) {
            for (int i = 0; i < BOARD_SIDE_LENGTH; i++) {
                if (diffBoard[j][i] == ENEMY_COLOR_DEFICIENCY) {
                    countEnemyColorDeficiency++;
                }
            }
        }
        return countEnemyColorDeficiency;
    }

    private boolean properCurrentPlayerMove() {
        boolean numbers = validatePawnsNumber();
        boolean closestDiagonal = false;
        int normalJumps = 0;
        boolean berserkMove = false;
        if (numbers) {
            closestDiagonal = checkDiagonalMove(oldMovedPawn.getX(), oldMovedPawn.getY(), newMovedPawn.getX(), newMovedPawn.getY(), 1);
            berserkMove = checkBerserkModeMove(oldMovedPawn.getX(), oldMovedPawn.getY(), newMovedPawn.getX(), newMovedPawn.getY(), currentPlayer);
            normalJumps = multipleJumps(oldMovedPawn.getX(), oldMovedPawn.getY(), newMovedPawn.getX(), newMovedPawn.getY(), null);
        }

        int enemyDeficiency = getEnemyColorDeficiencyNumber();

        if ((closestDiagonal && enemyDeficiency == 0) || normalJumps == enemyDeficiency || berserkMove && enemyDeficiency == 1) {
            return true;
        } else {

        }
        return false;
    }


    public boolean validate() {
        boolean properPlayerMove = properCurrentPlayerMove();

        return properPlayerMove;
    }

    private boolean checkBerserkModeMove(int xRef, int yRef, int x, int y, Color player) {
        if (player != null && player.equals(BEGINS_FROM_TOP) && yRef == BOARD_SIDE_LENGTH - 1) {
            if (yRef == BOARD_SIDE_LENGTH - 1) {
                for (int i = 1; i < BOARD_SIDE_LENGTH; i++) {
                    if (checkDiagonalMove(xRef, yRef, x, y, i))
                        return true;
                }
            }
        }

        if (player != null && player.equals(BEGINS_FROM_BOTTOM)) {
            if (yRef == 0) {
                for (int i = 1; i < BOARD_SIDE_LENGTH; i++) {
                    if (checkDiagonalMove(xRef, yRef, x, y, i))
                        return true;
                }
            }
        }
        return false;

    }

    private static boolean checkDiagonalMove(int xRef, int yRef, int x, int y, int dist) {
        if (x == xRef - dist || x == xRef + dist) {
            if (y == yRef - dist || y == yRef + dist) {
                return true;
            }
        }
        return false;
    }

    private static int distanceCalc(int a, int b) {
        if (a > b) {
            return a - b;
        } else
            return b - a;
    }

    private boolean isOnDiagonal(int xRef, int yRef, int xEnemy, int yEnemy) {
        int xDirection = 1;
        int yDirection = 1;
        int distance = distanceCalc(xRef, xEnemy);
        if (xRef >= 0 && yRef >= 0 && xEnemy >= 0 && yEnemy >= 0) {

            if (xEnemy < xRef) {
                xDirection = -1;
            }
            if (yEnemy < yRef) {
                yDirection = -1;
            }

            if (yRef + distance * yDirection == yEnemy) {
                return true;
            }
        }
        return false;
    }

    private boolean checkEnemyPawnsDeficiency(int xRef, int yRef, int xEnemy, int yEnemy) {

        int distance = distanceCalc(xRef, xEnemy);
        if (distance > 0 && isOnDiagonal(xRef, yRef, xEnemy, yEnemy) && diffBoard[xEnemy][yEnemy] == ENEMY_COLOR_DEFICIENCY) {
            return true;
        }
        return false;
    }

    private int multipleJumps(int xRef, int yRef, int destX, int destY, JumpDirection jmpDir) {
        int result = 0;

        JumpDirection direction = getJumpDirection(xRef, yRef, jmpDir);
        if (direction != null) {
            switch (direction) {
                case UP_RIGHT:
                    xRef -= 2;
                    yRef += 2;
                    break;
                case UP_LEFT:
                    xRef -= 2;
                    yRef -= 2;
                    break;
                case DOWN_RIGHT:
                    xRef += 2;
                    yRef += 2;
                    break;
                case DOWN_LEFT:
                    xRef += 2;
                    yRef -= 2;
                    break;
            }

            if (xRef == destX && yRef == destY) {
                result = 1;
            } else {
                result = 1 + multipleJumps(xRef, yRef, destX, destY, direction);
            }
        }

        return result;
    }

    private JumpDirection getJumpDirection(int xRef, int yRef, JumpDirection lastJump) {
        JumpDirection result = null;
        if (!JumpDirection.UP_LEFT.equals(lastJump) && isOnBoard(xRef + 1, yRef + 1) && checkEnemyPawnsDeficiency(xRef, yRef, xRef + 1, yRef + 1)) {
            result = JumpDirection.DOWN_RIGHT;
        } else if (!JumpDirection.DOWN_RIGHT.equals(lastJump) && isOnBoard(xRef - 1, yRef - 1) && checkEnemyPawnsDeficiency(xRef, yRef, xRef - 1, yRef - 1)) {
            result = JumpDirection.UP_LEFT;
        } else if (!JumpDirection.UP_RIGHT.equals(lastJump) && isOnBoard(xRef + 1, yRef - 1) && checkEnemyPawnsDeficiency(xRef, yRef, xRef + 1, yRef - 1)) {
            result = JumpDirection.DOWN_LEFT;
        } else if (!JumpDirection.DOWN_LEFT.equals(lastJump) && isOnBoard(xRef - 1, yRef + 1) && checkEnemyPawnsDeficiency(xRef, yRef, xRef - 1, yRef + 1)) {
            result = JumpDirection.UP_RIGHT;
        }

        return result;
    }

    private boolean isOnBoard(int x, int y) {
        return (x >= 0 && x <= 7 && y >= 0 && y <= 7) ? true : false;
    }

    enum JumpDirection {
        UP_RIGHT, UP_LEFT, DOWN_RIGHT, DOWN_LEFT
    }

}
