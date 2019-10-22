package ru.dimasokol.game.tictactoe;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String ARG_SCORE_XS = "score_xs";
    private static final String ARG_SCORE_OS = "score_os";

    private TicTacToeView mBoard;
    private View mResultsLayout, mRestartButton, mQuitButton;
    private TextView mResultTextView, mXsScoreTextView, mOsScoreTextView;

    private int mScoreCrosses = 0;
    private int mScoreCircles = 0;

    private boolean hasMoves = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mScoreCrosses = savedInstanceState.getInt(ARG_SCORE_XS);
            mScoreCircles = savedInstanceState.getInt(ARG_SCORE_OS);
        }

        mBoard = findViewById(R.id.game_board);
        mResultsLayout = findViewById(R.id.results_layout);

        mRestartButton = findViewById(R.id.button_restart);
        mQuitButton = findViewById(R.id.button_quit);

        mResultTextView = findViewById(R.id.game_result_text);
        mXsScoreTextView = findViewById(R.id.score_crosses);
        mOsScoreTextView = findViewById(R.id.score_circles);

        mRestartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideResults();
                mBoard.restartGame(true);
            }
        });

        mQuitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBoard.setOnGameChangeListener(new TicTacToeView.OnGameChangeListener() {
            @Override
            public void onAnyMove(TicTacToeField.Figure player) {
                hasMoves = true;
            }

            @Override
            public void onWin(TicTacToeField.Figure winner) {
                if (hasMoves) {
                    if (winner == TicTacToeField.Figure.CROSS) {
                        mScoreCrosses++;
                    } else {
                        mScoreCircles++;
                    }
                }

                showResults(winner, hasMoves);
            }

            @Override
            public void onDraw() {
                showResults(TicTacToeField.Figure.NONE, hasMoves);
            }
        });

        showScores();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(ARG_SCORE_XS, mScoreCrosses);
        outState.putInt(ARG_SCORE_OS, mScoreCircles);
    }

    private void showResults(TicTacToeField.Figure winner, boolean animate) {
        if (animate) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.results_appearance);
            animation.setInterpolator(new OvershootInterpolator(1.5f));
            mResultsLayout.startAnimation(animation);
        }

        mResultsLayout.setVisibility(View.VISIBLE);

        switch (winner) {
            case CROSS:
                mResultTextView.setText(R.string.result_crosses);
                break;
            case CIRCLE:
                mResultTextView.setText(R.string.result_circles);
                break;
            case NONE:
                mResultTextView.setText(R.string.result_draw);
                break;
        }

        showScores();
    }

    private void hideResults() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.results_disappearance);
        animation.setInterpolator(new AnticipateInterpolator(1.5f));
        mResultsLayout.setVisibility(View.VISIBLE);
        mResultsLayout.startAnimation(animation);
        mResultsLayout.setVisibility(View.INVISIBLE);
    }

    private void showScores() {
        mXsScoreTextView.setText(Integer.toString(mScoreCrosses));
        mOsScoreTextView.setText(Integer.toString(mScoreCircles));
    }
}
