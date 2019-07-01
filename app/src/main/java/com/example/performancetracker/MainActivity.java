package com.example.performancetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

public class MainActivity extends AppCompatActivity {

    // UI Controls
    Button buttonPlus;
    Button buttonMinus;
    Button buttonReset;

    TextView labelCurrentStreak;
    TextView labelSuccessRate;
    TextView labelStreaks;
    TextView labelLongestStreak;
    TextView labelAverageStreakLength;
    TextView labelSessionTotal;

    // Stat variables
    BigInteger currentStreak;
    BigInteger streaks;
    BigInteger longestStreak;
    BigInteger successfulRepetitions;
    BigInteger sessionTotal;

    BigDecimal successRate;
    BigDecimal averageStreakLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUserInterface();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Initialize session variables
        resetCurrentSession();
    }

    private void initializeUserInterface() {
        buttonPlus = (Button) findViewById(R.id.button_plus);

        buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRepetition(true);
            }
        });

        buttonMinus = (Button) findViewById(R.id.button_minus);

        buttonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRepetition(false);
            }
        });

        buttonReset = (Button) findViewById(R.id.button_reset);

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetCurrentSession();
            }
        });

        labelCurrentStreak = (TextView) findViewById(R.id.label_current_streak);
        //updateCurrentStreakLabel();

        labelSuccessRate = (TextView) findViewById(R.id.label_session_success_rate_value);
        //updateSuccessRateLabel();

        labelStreaks = (TextView) findViewById(R.id.label_streaks_count);
        //updateStreaksLabel();

        labelLongestStreak = (TextView) findViewById(R.id.label_longest_streak_count);
        //updateLongestStreakLabel();

        labelAverageStreakLength = (TextView) findViewById(R.id.label_average_streak_length_value);
        //updateAverageStreakLengthLabel();

        labelSessionTotal = (TextView) findViewById(R.id.label_session_total_count);
        //updateCurrentSessionTotalLabel();
    }

    /* Repetition-related functions */
    private void addRepetition(boolean successful) {
        incrementTotalRepetitions();

        if (successful) {
            success();
        } else {
            failure();
        }

        // Update session statistics
        updateSessionStatistics();

        // Update labels
        updateUI();
    }

    private void success() {
        incrementTotalSuccessfulRepetitions();
        incrementCurrentStreakLength();

        // // If the current streak is a new record, replace the record with the current streak
        if (currentStreakIsLongerThanCurrentRecord()) {
            longestStreak = currentStreak;

            // Show toast notification congratulating user
            Toast.makeText(this, "New Record!", Toast.LENGTH_SHORT).show();
        }
    }

    private void failure() {
        // Only increment the streak count if the current streak is at least 1 successful rep long.
        // Otherwise, there have been no successful repetitions this streak and is therefore not
        // actually a streak.
        if (currentStreakIsGreaterThanZero()) {
            incrementStreakCount();
            resetCurrentStreak();
        }
    }

    private void incrementCurrentStreakLength() {
        currentStreak = currentStreak.add(BigInteger.ONE);
    }

    private void incrementTotalSuccessfulRepetitions() {
        successfulRepetitions = successfulRepetitions.add(BigInteger.ONE);
    }

    private void incrementTotalRepetitions() {
        sessionTotal = sessionTotal.add(BigInteger.ONE);
    }

    private void incrementStreakCount() {
        streaks = streaks.add(BigInteger.ONE);
    }

    private void resetCurrentStreak() {
        currentStreak = BigInteger.ZERO;
    }

    private void resetStreakCount() {
        streaks = BigInteger.ZERO;
    }

    private void resetLongestStreak() {
        longestStreak = BigInteger.ZERO;
    }

    private void resetSuccessfulRepetitions() {
        successfulRepetitions = BigInteger.ZERO;
    }

    private void resetSessionTotal() {
        sessionTotal = BigInteger.ZERO;
    }

    private boolean currentStreakIsGreaterThanZero() {
        return (currentStreak.compareTo(BigInteger.ZERO) > 0);
    }

    private boolean currentStreakIsLongerThanCurrentRecord() {
        return (currentStreak.compareTo(longestStreak) > 0);
    }

    /* Session statistics update functions */
    private void updateSessionStatistics() {
        updateSuccessRate();
        updateAverageStreakLength();
    }

    private void updateSuccessRate() {
        if (sessionTotal.compareTo(BigInteger.ZERO) == 0) {
            successRate = BigDecimal.ZERO;
            return;
        }

        successRate = (new BigDecimal(successfulRepetitions, MathContext.UNLIMITED)).setScale(4, RoundingMode.HALF_UP).divide(new BigDecimal(sessionTotal, MathContext.UNLIMITED).setScale(4, RoundingMode.HALF_UP), RoundingMode.HALF_UP);
    }

    private void updateAverageStreakLength() {
        if (streaks.compareTo(BigInteger.ZERO) == 0) {
            averageStreakLength = BigDecimal.ZERO;
            return;
        }

        averageStreakLength = (new BigDecimal(successfulRepetitions, MathContext.UNLIMITED)).setScale(2, RoundingMode.HALF_UP).divide(new BigDecimal(streaks, MathContext.UNLIMITED).setScale(2, RoundingMode.HALF_UP), RoundingMode.HALF_UP);
    }

    private void resetCurrentSession() {
        // Reset current session variable values
        resetCurrentStreak();
        resetStreakCount();
        resetLongestStreak();
        resetSuccessfulRepetitions();
        resetSessionTotal();

        // Update session stats using recently cleared session variables
        updateSessionStatistics();

        // Update UI after resetting session so labels show zero values
        updateUI();
    }

    /* User-Interface functions */
    private void updateUI() {
        updateCurrentStreakLabel();
        updateSuccessRateLabel();
        updateStreaksLabel();
        updateLongestStreakLabel();
        updateAverageStreakLengthLabel();
        updateCurrentSessionTotalLabel();
    }

    private void updateCurrentStreakLabel() {
        labelCurrentStreak.setText(String.format("%s", currentStreak));
    }

    private void updateSuccessRateLabel() {
        labelSuccessRate.setText(String.format("%s", successRate));
    }

    private void updateStreaksLabel() {
        labelStreaks.setText(String.format("%s", streaks));
    }

    private void updateLongestStreakLabel() {
        labelLongestStreak.setText(String.format("%s", longestStreak));
    }

    private void updateAverageStreakLengthLabel() {
        labelAverageStreakLength.setText(String.format("%s", averageStreakLength));
    }

    private void updateCurrentSessionTotalLabel() {
        labelSessionTotal.setText(String.format("%s", sessionTotal));
    }
}
