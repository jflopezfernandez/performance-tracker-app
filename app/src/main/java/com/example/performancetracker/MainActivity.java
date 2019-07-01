package com.example.performancetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    int currentStreak;
    int streaks;
    int longestStreak;
    int successfulRepetitions;
    int sessionTotal;

    double successRate;
    double averageStreakLength;

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
        updateCurrentStreakLabel();

        labelSuccessRate = (TextView) findViewById(R.id.label_session_success_rate_value);
        updateSuccessRateLabel();

        labelStreaks = (TextView) findViewById(R.id.label_streaks_count);
        updateStreaksLabel();

        labelLongestStreak = (TextView) findViewById(R.id.label_longest_streak_count);
        updateLongestStreakLabel();

        labelAverageStreakLength = (TextView) findViewById(R.id.label_average_streak_length_value);
        updateAverageStreakLengthLabel();

        labelSessionTotal = (TextView) findViewById(R.id.label_session_total_count);
        updateCurrentSessionTotalLabel();
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
        if (currentStreak > longestStreak) {
            longestStreak = currentStreak;

            // Show toast notification congratulating user
            Toast.makeText(this, "New Record!", Toast.LENGTH_SHORT).show();
        }
    }

    private void failure() {
        // Only increment the streak count if the current streak is at least 1 successful rep long.
        // Otherwise, there have been no successful repetitions this streak and is therefore not
        // actually a streak.
        if (currentStreak > 0) {
            incrementStreakCount();
            resetCurrentStreak();
        }
    }

    private void incrementCurrentStreakLength() {
        currentStreak += 1;
    }

    private void incrementTotalSuccessfulRepetitions() {
        successfulRepetitions += 1;
    }

    private void incrementTotalRepetitions() {
        sessionTotal += 1;
    }

    private void incrementStreakCount() {
        streaks += 1;
    }

    private void resetCurrentStreak() {
        currentStreak = 0;
    }

    private void resetStreakCount() {
        streaks = 0;
    }

    private void resetLongestStreak() {
        longestStreak = 0;
    }

    private void resetSuccessfulRepetitions() {
        successfulRepetitions = 0;
    }

    private void resetSessionTotal() {
        sessionTotal = 0;
    }

    /* Session statistics update functions */
    private void updateSessionStatistics() {
        updateSuccessRate();
        updateAverageStreakLength();
    }

    private void updateSuccessRate() {
        successRate = (double) successfulRepetitions / (double) sessionTotal;
    }

    private void updateAverageStreakLength() {
        averageStreakLength = (double) successfulRepetitions / (double) streaks;
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
        labelCurrentStreak.setText(Integer.toString(currentStreak));
    }

    private void updateSuccessRateLabel() {
        labelSuccessRate.setText(String.format("%.4f", successRate));
    }

    private void updateStreaksLabel() {
        labelStreaks.setText(String.format("%d", streaks));
    }

    private void updateLongestStreakLabel() {
        labelLongestStreak.setText(String.format("%d", longestStreak));
    }

    private void updateAverageStreakLengthLabel() {
        labelAverageStreakLength.setText(String.format("%.2f", averageStreakLength));
    }

    private void updateCurrentSessionTotalLabel() {
        labelSessionTotal.setText(Integer.toString(sessionTotal));
    }
}
