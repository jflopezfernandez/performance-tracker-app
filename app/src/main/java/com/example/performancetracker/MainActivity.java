package com.example.performancetracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    // UI Controls
    Button buttonPlus;
    Button buttonMinus;
    Button buttonSaveSessionData;
    Button buttonReset;

    TextView labelCurrentStreak;
    TextView labelSuccessRate;
    TextView labelStreaks;
    TextView labelLongestStreak;
    TextView labelAverageStreakLength;
    TextView labelSessionTotal;
    TextView labelSuccessfulRepetitions;
    TextView labelStreakStandardDeviation;

    // Audio Controller
    MediaPlayer mediaPlayer;

    // Stat variables
    BigInteger currentStreak;
    BigInteger streakCount;
    BigInteger longestStreak;
    BigInteger successfulRepetitions;
    BigInteger sessionTotal;

    BigDecimal successRate;
    BigDecimal averageStreakLength;
    BigDecimal streakStandardDeviation;
    
    Vector<BigInteger> streaks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize streaks vector
        streaks = new Vector<BigInteger>(50, 50);

        initializeUserInterface();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Initialize session variables
        resetCurrentSession();

        // Initialize media controller
        initializeMediaController();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // If the user has terminated the application, release allocated resources
        if (this.isFinishing()) {
            // Free media player resources and memory
            mediaPlayer.release();
        }
    }

    private void initializeUserInterface() {
        buttonPlus = (Button) findViewById(R.id.button_plus);

        buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Add event listener to trigger 'beep()'
                addRepetition(true);
            }
        });

        buttonMinus = (Button) findViewById(R.id.button_minus);

        buttonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Add event listener to trigger 'beep()'
                addRepetition(false);
            }
        });

        buttonSaveSessionData = (Button) findViewById(R.id.button_save_session_data);

        buttonSaveSessionData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSessionData();
            }
        });

        // TODO: Fix and re-enable
        buttonSaveSessionData.setEnabled(false);

        buttonReset = (Button) findViewById(R.id.button_reset);

        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetCurrentSession();
            }
        });

        labelCurrentStreak = (TextView) findViewById(R.id.label_current_streak);
        labelSuccessRate = (TextView) findViewById(R.id.label_session_success_rate_value);
        labelStreaks = (TextView) findViewById(R.id.label_streaks_count);
        labelLongestStreak = (TextView) findViewById(R.id.label_longest_streak_count);
        labelAverageStreakLength = (TextView) findViewById(R.id.label_average_streak_length_value);
        labelSessionTotal = (TextView) findViewById(R.id.label_session_total_count);
        labelSuccessfulRepetitions = (TextView) findViewById(R.id.label_total_successful_repetitions_count);
        labelStreakStandardDeviation = (TextView) findViewById(R.id.label_streak_standard_deviation_value);
    }

    private void initializeMediaController() {
        mediaPlayer = MediaPlayer.create(this, R.raw.beep);
    }

    private void beep() {
        mediaPlayer.start();
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

            // TODO: Make this less annoying
            // Show toast notification congratulating user
            // Toast.makeText(this, "New Record!", Toast.LENGTH_SHORT).show();
        }
    }

    private void failure() {
        // Only increment the streak count if the current streak is at least 1 successful rep long.
        // Otherwise, there have been no successful repetitions this streak and is therefore not
        // actually a streak.
        if (currentStreakIsGreaterThanZero()) {
            // Add current streak to streaks array
            streaks.add(currentStreak);

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
        streakCount = streakCount.add(BigInteger.ONE);
    }

    private void resetCurrentStreak() {
        currentStreak = BigInteger.ZERO;
    }

    private void resetStreakCount() {
        streakCount = BigInteger.ZERO;
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
        updateStreakStandardDeviation();
    }

    private void updateSuccessRate() {
        if (sessionTotal.compareTo(BigInteger.ZERO) == 0) {
            successRate = BigDecimal.ZERO;
            return;
        }

        successRate = (new BigDecimal(successfulRepetitions, MathContext.UNLIMITED)).setScale(4, RoundingMode.HALF_UP).divide(new BigDecimal(sessionTotal, MathContext.UNLIMITED).setScale(4, RoundingMode.HALF_UP), RoundingMode.HALF_UP);
    }

    private void updateAverageStreakLength() {
        if (streakCount.compareTo(BigInteger.ZERO) == 0) {
            averageStreakLength = BigDecimal.ZERO;
            return;
        }

        averageStreakLength = (new BigDecimal(successfulRepetitions, MathContext.UNLIMITED)).setScale(2, RoundingMode.HALF_UP).divide(new BigDecimal(streakCount, MathContext.UNLIMITED).setScale(2, RoundingMode.HALF_UP), RoundingMode.HALF_UP);
    }

    // Source: (https://stackoverflow.com/questions/13649703/square-root-of-bigdecimal-in-java)
    private BigDecimal SquareRoot(BigDecimal x, final int scale) {
        BigDecimal x0 = new BigDecimal("0");
        BigDecimal x1 = new BigDecimal(Math.sqrt(x.doubleValue()));

        while (!x0.equals(x1)) {
            x0 = x1;
            x1 = x.divide(x0, scale, BigDecimal.ROUND_HALF_UP);
            x1 = x1.add(x0);
            x1 = x1.divide(BigDecimal.ONE.add(BigDecimal.ONE), scale, BigDecimal.ROUND_FLOOR);
        }

        return x1;
    }

    private void updateStreakStandardDeviation() {
        // For the degenerate case where there are either zero or one values in the streaks array,
        // simply return zero for the standard deviation, since we would otherwise run into an
        // arithmetic exception.
        if (streaks.size() < 2) {
            streakStandardDeviation = BigDecimal.ZERO;

            return;
        }

        BigDecimal streakSum = new BigDecimal(0.0);

        for (BigInteger streak : streaks) {
            BigDecimal x_minus_mean = (new BigDecimal(streak, 4)).subtract(averageStreakLength);
            x_minus_mean = x_minus_mean.multiply(x_minus_mean);

            streakSum = streakSum.add(x_minus_mean);
        }

        streakSum = streakSum.divide((new BigDecimal(streaks.size() - 1)), RoundingMode.HALF_UP);
        // streakStandardDeviation = streakSum.setScale(4, RoundingMode.HALF_UP).pow(-2, MathContext.UNLIMITED);
        streakSum = streakSum.setScale(4, RoundingMode.HALF_UP);
        streakStandardDeviation = SquareRoot(streakSum, streakSum.scale());
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
        updateSuccessfulRepetitionsLabel();
        updateStreakStandardDeviationLabel();
    }

    private void updateCurrentStreakLabel() {
        labelCurrentStreak.setText(String.format("%s", currentStreak));
    }

    private void updateSuccessRateLabel() {
        labelSuccessRate.setText(String.format("%s", successRate));
    }

    private void updateStreaksLabel() {
        labelStreaks.setText(String.format("%s", streakCount));
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

    private void updateSuccessfulRepetitionsLabel() {
        labelSuccessfulRepetitions.setText(String.format("%s", successfulRepetitions));
    }

    private void updateStreakStandardDeviationLabel() {
        labelStreakStandardDeviation.setText(String.format("%s", streakStandardDeviation));
    }

    /* Save data to file */
    private String createSessionDataFileName() {
        return "session-" + (new Date().toString().split(" G")[0].replace(' ', '_').toLowerCase());
    }

    private void saveSessionData() {
        // File directory = getApplicationContext().getFilesDir();

        final String filename = createSessionDataFileName();

//        File sessionDataOutputFile = new File(this.getFilesDir(), filename);
//
//        try {
//            if (sessionDataOutputFile.createNewFile()) {
//                Log.e("ERROR", "Failed to create file");
//            }
//        } catch (IOException ioException) {
//            ioException.printStackTrace();
//        }

        // File sessionDataOutputFile = new File(Environment.getDataDirectory(), filename);

        // File sessionDataOutputFile = new File(directory, filename);

        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.e("ERROR", "No external storage mounted");

            return;
        }

        final String publicDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath();
        final String appFileDirectory = this.getFilesDir().getPath();
        final String appContextFileDir = getApplicationContext().getFilesDir().getPath();

        Log.i("publicDirectoryPath", publicDirectoryPath);
        Log.i("appFileDirectory", appFileDirectory);
        Log.i("appContextFileDir", appContextFileDir);

        File sessionDataOutputFile = new File(this.getFilesDir(), filename);

        try {
            if (!sessionDataOutputFile.createNewFile()) {
                Log.e("Error", "Could not create file");
                return;
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
            Log.e("Could not create file", ioException.toString());
        }

        if (!sessionDataOutputFile.canWrite()) {
            Log.e("Cannot write", filename);

            return;
        }

        //

        //File sessionDataOutputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), filename);

        FileOutputStream sessionDataOutputStream;

        try {
            sessionDataOutputStream = new FileOutputStream(sessionDataOutputFile);

            Log.i("SessionDataOutputStream", sessionDataOutputStream.getChannel().toString());

            final String stringSuccessRate = "Success Rate: " + successRate + "\n";
            final String stringSuccessfulAttempts = "Successful Attempts: " + successfulRepetitions + "\n";
            final String stringTotalAttempts = "Total Attempts: " + sessionTotal + "\n";
            final String stringLongestStreak = "Longest Streak: " + longestStreak + "\n";
            final String stringAverageStreakLength = "Average Streak: " + averageStreakLength + "\n";
            final String stringStreaks = "Streaks: " + streakCount + "\n";

            sessionDataOutputStream.write(stringSuccessRate.getBytes());
            sessionDataOutputStream.write(stringSuccessfulAttempts.getBytes());
            sessionDataOutputStream.write(stringTotalAttempts.getBytes());
            sessionDataOutputStream.write(stringLongestStreak.getBytes());
            sessionDataOutputStream.write(stringAverageStreakLength.getBytes());
            sessionDataOutputStream.write(stringStreaks.getBytes());
            sessionDataOutputStream.write("\n".getBytes());

            sessionDataOutputStream.close();
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();

            Log.e("FileNotFoundException", fileNotFoundException.toString());
        } catch (IOException ioException) {
            ioException.printStackTrace();

            Log.e("IOException", ioException.toString());
        }
    }
}
