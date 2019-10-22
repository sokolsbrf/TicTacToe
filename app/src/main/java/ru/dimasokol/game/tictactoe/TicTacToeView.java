package ru.dimasokol.game.tictactoe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

public class TicTacToeView extends View {

    private static final int MIN_SIZE = 3;

    private final Point mDisplaySize = new Point();

    private int mCells = MIN_SIZE;

    private OnGameChangeListener mListener = new SimpleOnGameChangeListener();

    private TicTacToeField mField;
    private boolean[][] mWinnerMatrix;

    private Paint mOutlinePaint;
    private Paint mGridPaint;

    private int mOutlineStrokeWidth;
    private int mGridStrokeWidth;

    private int mAreaLeft = 0;
    private int mAreaTop = 0;
    private int mAreaRight = 0;
    private int mAreaBottom = 0;
    private int mAreaSize = 0;

    private int mOutlinesColor = Color.BLACK;
    private int mGridColor = Color.BLACK;

    private Drawable mCircle;
    private Drawable mCross;

    private ValueAnimator mOutlineAnimator;
    private ValueAnimator mGridAnimator;
    private ValueAnimator mCellAnimator;
    private ValueAnimator mGameEndAnimator;
    private ValueAnimator mGameRestartAnimator;

    private int mLastRow = -1;
    private int mLastCol = -1;
    private TicTacToeField.Figure mLastFigure = TicTacToeField.Figure.NONE;

    private WorkingMode mMode = WorkingMode.JUST_LANDED;
    private ValueAnimator.AnimatorUpdateListener mRepaintingListener;

    private GestureDetector mGestureDetector;
    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            int cellSize = mAreaSize / mCells;
            int xInside = (int) (e.getX() - mAreaLeft);
            int yInside = (int) (e.getY() - mAreaTop);

            int row = yInside / cellSize;
            int col = xInside / cellSize;

            if (row >= 0 && row < mCells && col >= 0 && col < mCells) {
                TicTacToeField.Figure figure = (mLastFigure == TicTacToeField.Figure.CROSS)? TicTacToeField.Figure.CIRCLE : TicTacToeField.Figure.CROSS;

                if (mField.setFigure(row, col, figure)) {
                    mLastFigure = figure;
                    mListener.onAnyMove(figure);
                    checkWinners();
                }

                mLastCol = col;
                mLastRow = row;

                mCellAnimator.cancel();
                mCellAnimator.start();
            }

            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return mMode == WorkingMode.GAME
                    && e.getX() >= mAreaLeft && e.getX() <= mAreaRight
                    && e.getY() >= mAreaTop && e.getY() <= mAreaBottom;
        }
    };

    private boolean checkWinners() {
        TicTacToeField.Figure winner = mField.getWinner();

        if (winner == TicTacToeField.Figure.NONE) {
            if (mField.isFull()) {
                mListener.onDraw();
                return true;
            }
        } else {
            mListener.onWin(winner);
            mWinnerMatrix = mField.getWinnerMatrix();
            mMode = WorkingMode.WINNER;
            return true;
        }

        return false;
    }

    public TicTacToeView(Context context) {
        super(context);
        init(context, null);
    }

    public TicTacToeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TicTacToeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public TicTacToeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public void restartGame(boolean animate) {
        if (animate) {
            mMode = WorkingMode.RESTARTING;
            mGameRestartAnimator.cancel();
            mGameRestartAnimator.start();
        } else {
            mMode = WorkingMode.GAME;
            mField = new TicTacToeField(mCells);
        }

        invalidate();
    }

    public void setOnGameChangeListener(OnGameChangeListener listener) {
        mListener = listener != null? listener : new SimpleOnGameChangeListener();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        postInvalidateDelayed(50);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);

        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);

        getDisplay().getSize(mDisplaySize);

        int width = mDisplaySize.x;
        int height = mDisplaySize.y;

        // Быстрый случай измерений для одинаковых режимов
        if (wMode == MeasureSpec.UNSPECIFIED && hMode == MeasureSpec.UNSPECIFIED) {
            height = width = Math.min(mDisplaySize.x, mDisplaySize.y);
        } else if (wMode == MeasureSpec.EXACTLY && hMode == MeasureSpec.EXACTLY) {
            width = wSize;
            height = hSize;
        } else if (wMode == MeasureSpec.AT_MOST && hMode == MeasureSpec.AT_MOST) {
            height = width = Math.min(wSize, hSize);
        } else {
            // Разные режимы по разным сторонам, измерим поштучно
            int maxSize = Math.min(width, height);
            width = singleMeasurement(wSize, maxSize, wMode);
            height = singleMeasurement(hSize, maxSize, hMode);
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mAreaSize = Math.min(w, h);

        mAreaLeft = (w - mAreaSize) / 2;
        mAreaTop = (h - mAreaSize) / 2;
        mAreaRight = mAreaSize + mAreaLeft;
        mAreaBottom = mAreaSize + mAreaTop;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            mField.setFigure(0, 0, TicTacToeField.Figure.CIRCLE);
            mField.setFigure(1, 1, TicTacToeField.Figure.CROSS);
            drawGrid(canvas, 1f);
            drawOutline(canvas, 1f);
            drawCells(canvas);
            return;
        }

        switch (mMode) {
            case JUST_LANDED:
                mMode = WorkingMode.DRAWING_OULINES;
                mOutlineAnimator.start();
                return;
            case DRAWING_GRID:
                drawOutline(canvas, 1f);
                drawGrid(canvas, (Float) mGridAnimator.getAnimatedValue());
                return;
            case DRAWING_OULINES:
                drawOutline(canvas, (Float) mOutlineAnimator.getAnimatedValue());
                return;
            case GAME:
            case WINNER:
            case GAME_ENDED:
            case RESTARTING:
                drawOutline(canvas, 1f);
                drawGrid(canvas, 1f);
                drawCells(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.mField = mField;
        state.mFigure = mLastFigure.ordinal();
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        SavedState myState = (SavedState) state;
        mField = myState.mField;
        mLastFigure = TicTacToeField.Figure.values()[myState.mFigure];

        if (checkWinners()) {
            mMode = WorkingMode.GAME_ENDED;
        } else {
            mMode = WorkingMode.GAME;
        }
    }

    private void init(Context context, AttributeSet attrs) {

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TicTacToeView, 0, 0);

        try {
            mOutlinesColor = a.getColor(R.styleable.TicTacToeView_boardOutlinesColor, mOutlinesColor);
            mGridColor = a.getColor(R.styleable.TicTacToeView_boardGridColor, mGridColor);
        } finally {
            a.recycle();
        }

        mGestureDetector = new GestureDetector(context, mGestureListener);

        mCircle = context.getDrawable(R.drawable.circle_animated);
        mCross = context.getDrawable(R.drawable.cross_animated);

        mField = new TicTacToeField(mCells);
        mOutlineStrokeWidth = context.getResources().getDimensionPixelSize(R.dimen.board_stroke_width);
        mGridStrokeWidth = context.getResources().getDimensionPixelSize(R.dimen.grid_stroke_width);

        mOutlinePaint = new Paint();
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setStrokeWidth(mOutlineStrokeWidth);
        mOutlinePaint.setColor(mOutlinesColor);

        mGridPaint = new Paint(mOutlinePaint);
        mGridPaint.setStrokeWidth(mGridStrokeWidth);
        mGridPaint.setColor(mGridColor);

        mRepaintingListener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        };

        mOutlineAnimator = ValueAnimator.ofFloat(0f, 1f);
        mOutlineAnimator.setInterpolator(new AccelerateInterpolator());
        mOutlineAnimator.setDuration(context.getResources().getInteger(R.integer.ouline_animation_time));
        mOutlineAnimator.addUpdateListener(mRepaintingListener);
        mOutlineAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mMode = WorkingMode.DRAWING_GRID;
                mOutlineAnimator.removeAllUpdateListeners();
                mOutlineAnimator.removeAllListeners();
                mGridAnimator.start();
            }
        });

        mGridAnimator = ValueAnimator.ofFloat(0f, 1f);
        mGridAnimator.setInterpolator(new DecelerateInterpolator());
        mGridAnimator.setDuration(context.getResources().getInteger(R.integer.ouline_animation_time));
        mGridAnimator.addUpdateListener(mRepaintingListener);
        mGridAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mMode = WorkingMode.GAME;
                mGridAnimator.removeAllListeners();
                mGridAnimator.removeAllUpdateListeners();
                invalidate();
            }
        });

        mCellAnimator = ValueAnimator.ofInt(0, 10000);
        mCellAnimator.setInterpolator(new OvershootInterpolator());
        mCellAnimator.setDuration(context.getResources().getInteger(R.integer.ouline_animation_time));
        mCellAnimator.addUpdateListener(mRepaintingListener);
        mCellAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mMode == WorkingMode.WINNER) {
                    mGameEndAnimator.start();
                }
            }
        });

        mGameEndAnimator = ValueAnimator.ofInt(6000, 2000);
        mGameEndAnimator.setInterpolator(new CycleInterpolator(3));
        mGameEndAnimator.setDuration(context.getResources().getInteger(R.integer.winner_animation_time));
        mGameEndAnimator.addUpdateListener(mRepaintingListener);
        mGameEndAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mMode = (mMode == WorkingMode.WINNER)? WorkingMode.GAME_ENDED : mMode;
                invalidate();
            }
        });

        mGameRestartAnimator = ValueAnimator.ofInt(10000, 0);
        mGameRestartAnimator.setInterpolator(new AnticipateInterpolator());
        mGameRestartAnimator.setDuration(context.getResources().getInteger(R.integer.ouline_animation_time));
        mGameRestartAnimator.addUpdateListener(mRepaintingListener);
        mGameRestartAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mMode = WorkingMode.GAME;
                mField = new TicTacToeField(mCells);
                mLastFigure = TicTacToeField.Figure.CIRCLE;
                invalidate();
            }
        });
    }

    private int singleMeasurement(int size, int maxSize, int mode) {
        switch (mode) {
            case MeasureSpec.UNSPECIFIED:
                return maxSize;
            case MeasureSpec.AT_MOST:
                return Math.min(size, maxSize);
        }

        return size;
    }

    private void drawOutline(Canvas canvas, float progress) {
        int size = (int) (progress * mAreaSize);

        int pad = mOutlineStrokeWidth / 2;

        canvas.drawLine(
                mAreaLeft + pad,
                mAreaTop + pad,
                mAreaLeft + size,
                mAreaTop + pad,
                mOutlinePaint);

        canvas.drawLine(
                mAreaRight - pad,
                mAreaTop + pad,
                mAreaRight - pad,
                mAreaTop + size,
                mOutlinePaint);

        canvas.drawLine(
                mAreaRight - pad,
                mAreaBottom - pad,
                mAreaRight - size,
                mAreaBottom - pad,
                mOutlinePaint);

        canvas.drawLine(
                mAreaLeft + pad,
                mAreaBottom - pad,
                mAreaLeft + pad,
                mAreaBottom - size,
                mOutlinePaint);
    }

    /**
     * Рисует сетку с анимацией
     *
     * @param canvas Канва
     * @param progress Прогресс 0-1
     */
    private void drawGrid(Canvas canvas, float progress) {
        int cellSize = mAreaSize / mCells;
        int size = (int) (progress * mAreaSize);

        for (int i = 0; i < mCells - 1; i++) {
            int offset = cellSize * (i + 1);

            canvas.drawLine(mAreaLeft, mAreaTop + offset, mAreaLeft + size, mAreaTop + offset, mGridPaint);
            canvas.drawLine(mAreaLeft + offset, mAreaTop, mAreaLeft + offset, mAreaTop + size, mGridPaint);
        }
    }

    private void drawCells(Canvas canvas) {

        int cellSize = (mAreaSize - (mGridStrokeWidth * (mCells - 1)) - (mOutlineStrokeWidth * 2)) / mCells;

        for (int row = 0; row < mCells; row++) {
            for (int col = 0; col < mCells; col++) {
                int x = mAreaLeft + mOutlineStrokeWidth + (mGridStrokeWidth * col) + (cellSize * col);
                int y = mAreaTop + mOutlineStrokeWidth + (mGridStrokeWidth * row) + (cellSize * row);

                if (!mField.isEmptyCell(row, col)) {
                    Drawable drawable = mField.getFigure(row, col) == TicTacToeField.Figure.CROSS? mCross : mCircle;
                    int level = (mLastCol == col && mLastRow == row)? (int) mCellAnimator.getAnimatedValue() : 10000;

                    if (mMode == WorkingMode.WINNER && mWinnerMatrix[row][col] && !mCellAnimator.isRunning()) {
                        level = (int) mGameEndAnimator.getAnimatedValue();
                    }

                    if (mMode == WorkingMode.RESTARTING) {
                        level = (int) mGameRestartAnimator.getAnimatedValue();
                    }

                    drawable.setLevel(level);
                    drawable.setBounds(x, y, x + cellSize, y + cellSize);
                    drawable.draw(canvas);
                }
            }
        }
    }

    public interface OnGameChangeListener {

        void onAnyMove(TicTacToeField.Figure player);

        void onWin(TicTacToeField.Figure winner);

        void onDraw();
    }

    /**
     * Пустая реализация для наследования при необходимости использовать лишь один метод, и для
     * использования как null object
     */
    public class SimpleOnGameChangeListener implements OnGameChangeListener {

        @Override
        public void onAnyMove(TicTacToeField.Figure player) {
        }

        @Override
        public void onWin(TicTacToeField.Figure winner) {
        }

        @Override
        public void onDraw() {

        }
    }

    private enum WorkingMode {
        /**
         * Неопределённое начальное состояние
         */
        JUST_LANDED,
        /**
         * Рисуются внешние границы
         */
        DRAWING_OULINES,
        /**
         * Рисуется сетка
         */
        DRAWING_GRID,
        /**
         * Режим игры
         */
        GAME,
        /**
         * Есть победитель
         */
        WINNER,
        /**
         * Игра окончена
         */
        GAME_ENDED,
        /**
         * Игра рестартится
         */
        RESTARTING
    }

    private static class SavedState extends BaseSavedState {
        private TicTacToeField mField;
        private int mFigure;

        public SavedState(Parcel source) {
            super(source);
            mField = source.readParcelable(getClass().getClassLoader());
            mFigure = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeParcelable(mField, 0);
            out.writeInt(mFigure);
        }
    }
}