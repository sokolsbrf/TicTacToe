package ru.dimasokol.game.tictactoe;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TicTacToeFieldTest {

    @Test
    public void getFigure() {
        TicTacToeField field = new TicTacToeField(3);
        assertEquals(TicTacToeField.Figure.NONE, field.getFigure(0, 1));
        field.setFigure(0, 1, TicTacToeField.Figure.CROSS);

        assertEquals(TicTacToeField.Figure.CROSS, field.getFigure(0, 1));
        field.setFigure(0, 1, TicTacToeField.Figure.CIRCLE);

        assertEquals(TicTacToeField.Figure.CROSS, field.getFigure(0, 1));
    }

    @Test
    public void getWinner() {
        TicTacToeField field = new TicTacToeField(3);
        field.setFigure(0, 0, TicTacToeField.Figure.CROSS);
        field.setFigure(1, 0, TicTacToeField.Figure.CROSS);
        field.setFigure(2, 0, TicTacToeField.Figure.CROSS);
        assertEquals(TicTacToeField.Figure.CROSS, field.getWinner());
        assertArrayEquals(new boolean[] {true, false, false}, field.getWinnerMatrix()[0]);
        assertArrayEquals(new boolean[] {true, false, false}, field.getWinnerMatrix()[1]);
        assertArrayEquals(new boolean[] {true, false, false}, field.getWinnerMatrix()[2]);

        field = new TicTacToeField(3);
        field.setFigure(1, 0, TicTacToeField.Figure.CROSS);
        field.setFigure(1, 1, TicTacToeField.Figure.CROSS);
        field.setFigure(1, 2, TicTacToeField.Figure.CROSS);
        assertEquals(TicTacToeField.Figure.CROSS, field.getWinner());
        assertArrayEquals(new boolean[] {false, false, false}, field.getWinnerMatrix()[0]);
        assertArrayEquals(new boolean[] {true, true, true}, field.getWinnerMatrix()[1]);
        assertArrayEquals(new boolean[] {false, false, false}, field.getWinnerMatrix()[2]);

        field = new TicTacToeField(3);
        field.setFigure(0, 0, TicTacToeField.Figure.CROSS);
        field.setFigure(1, 1, TicTacToeField.Figure.CROSS);
        field.setFigure(2, 2, TicTacToeField.Figure.CROSS);
        assertEquals(TicTacToeField.Figure.CROSS, field.getWinner());
        assertArrayEquals(new boolean[] {true,  false, false}, field.getWinnerMatrix()[0]);
        assertArrayEquals(new boolean[] {false, true,  false}, field.getWinnerMatrix()[1]);
        assertArrayEquals(new boolean[] {false, false, true}, field.getWinnerMatrix()[2]);

        field = new TicTacToeField(3);
        field.setFigure(0, 2, TicTacToeField.Figure.CROSS);
        field.setFigure(1, 1, TicTacToeField.Figure.CROSS);
        field.setFigure(2, 0, TicTacToeField.Figure.CROSS);
        assertEquals(TicTacToeField.Figure.CROSS, field.getWinner());
        assertArrayEquals(new boolean[] {false, false, true}, field.getWinnerMatrix()[0]);
        assertArrayEquals(new boolean[] {false, true,  false}, field.getWinnerMatrix()[1]);
        assertArrayEquals(new boolean[] {true,  false, false}, field.getWinnerMatrix()[2]);

        field = new TicTacToeField(3);
        field.setFigure(2, 0, TicTacToeField.Figure.CROSS);
        field.setFigure(2, 1, TicTacToeField.Figure.CROSS);
        field.setFigure(2, 2, TicTacToeField.Figure.CROSS);
        assertEquals(TicTacToeField.Figure.CROSS, field.getWinner());
        assertArrayEquals(new boolean[] {false, false, false}, field.getWinnerMatrix()[0]);
        assertArrayEquals(new boolean[] {false, false, false}, field.getWinnerMatrix()[1]);
        assertArrayEquals(new boolean[] {true, true, true}, field.getWinnerMatrix()[2]);
    }

    @Test
    public void getWinner_noWinner() {
        TicTacToeField field = new TicTacToeField(3);
        field.setFigure(1, 0, TicTacToeField.Figure.CROSS);
        field.setFigure(1, 1, TicTacToeField.Figure.CIRCLE);
        field.setFigure(1, 2, TicTacToeField.Figure.CROSS);
        assertEquals(TicTacToeField.Figure.NONE, field.getWinner());
    }
}