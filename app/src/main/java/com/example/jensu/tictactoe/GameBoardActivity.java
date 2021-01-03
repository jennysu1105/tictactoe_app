/**
 * This activity is the game playing window. In this activity uses rules and customs provided by
 * the user input in the options menu.
 *
 * @author Jenny Su
 * @version May 27 2019
 */
package com.example.jensu.tictactoe;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class GameBoardActivity extends Activity {

    private Button[][] boardButtons = new Button[3][3];
    private TextView player1NameLabel, player1PointLabel, player2NameLabel, player2PointLabel;
    private Button newGameButton;
    private TextView winnerMessage;
    // This variable keeps track if the game has already started so when the app boots up, it will
    // go straight to this activity.
    private boolean newGame;

    private boolean player1Turn = true;
    private int turnCount;

    private int player1Points;
    private int player2Points;
    private String message;

    private int numberOfRounds;
    private boolean isComputer;
    private int playType;
    private int principle;
    private String winner = null;

    private SharedPreferences savedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_board);

        setInitialValues();
        setWidgets();
    }

    /**
     * This method sets up the starting values of the current game.
     */
    private void setInitialValues(){
        newGame = true;
        turnCount = 0;
        player1Points = 0;
        player2Points = 0;
        message = "Round 1";

        numberOfRounds = 0;
        savedPref = getSharedPreferences( "TTT_Prefs", MODE_PRIVATE );
    }

    /**
     * This method sets up all the buttons and labels needed for the Activity.
     */
    private void setWidgets(){
        Intent intent = getIntent();

        OnClickListener buttonEventListener = new ButtonEventListener();
        for (int x = 0; x < 3; x++){
            for (int y = 0; y < 3; y++) {
                String name = "boardButton" + x + y;
                int res = getResources().getIdentifier(name, "id", getPackageName());
                boardButtons[x][y] = findViewById(res);
                boardButtons[x][y].setOnClickListener(buttonEventListener);
            }
        }
        player1NameLabel = findViewById(R.id.playerOneNameLabel);
        player1PointLabel = findViewById(R.id.playerOnePointAmout);

        player2NameLabel = findViewById(R.id.playerTwoNameLabel);
        player2PointLabel = findViewById(R.id.playerTwoPointAmount);

        winnerMessage = findViewById(R.id.winnerMessageLabel);

        newGameButton = findViewById(R.id.newGameButton);
        newGameButton.setOnClickListener(buttonEventListener);
    }

    /**
     * This class takes care of all actions related to buttons.
     * @see OnClickListener
     */
    class ButtonEventListener implements OnClickListener{
        @Override
        public void onClick(View v) {
            // When new game button is pressed
            if (v.getId() == R.id.newGameButton){
                // Start the options activity
                Intent intent = new Intent(getActivity(), OptionsActivity.class);
                newGame = false;
                startActivity(intent);
            }

            // If a board button was pressed check if it is occupies. If it is not, then proceed
            // with the move
            else if(((Button) v).getText().toString().equals("")){
                // Change the text to the current player symbol
                if (player1Turn) {
                    ((Button) v).setText("O");
                }
                // This is just for player vs player games.
                else {
                    ((Button) v).setText("X");
                }
                // change player turns
                player1Turn = !player1Turn;
                // add to turn count
                turnCount += 1;
                // check for winner
                winnerCheck();
                // if there is a computer, check if someone already won. If there has not been
                // a winner yet, call the computer move method.
                if (isComputer && !player1Turn && (turnCount != 0) &&
                        (message.equals("Round " + (numberOfRounds + 1)))) {
                    computerMove();
                }
            }
        }
    }

    /**
     * This method returns the the activity object. This is for the Intent object when switching
     * activities inside the ButtonListener class.
     * @return Activity Board game activity.
     */
    private Activity getActivity(){
        return this;
    }

    /**
     * This method checks if there is an overall winner and calls he hasWon method to actually check
     * if there is a winner for the current round. If there is, this method will act accordingly, and
     * if there is not, then it will just return to the method it came from.
     */
    private void winnerCheck(){
        // check if there was a winner
        winner = hasWon();

        // if not return
        if (winner.equals("")){
            return;
        }

        // otherwise, update the points
        updatePoints();

        // if playType is not infinitely, then check the playType and check if the principle has
        // been met
        if (playType != 0){
            // for the playType principle for the best of type, since the round has not been reset
            // yet and that is where the numberOfRounds variable updates, add by one.
            if ((playType == 1 && principle == numberOfRounds + 1) ||
                    (playType == 2 && (principle == (player1Points) ||
                            principle == (player2Points)))){
                // if the principle was met, find the overall winner
                // this would be true for best of type and first one to, so there is no point in
                // checking for the winner any way
                if (player1Points > player2Points){
                    winner = "O";
                }
                else if (player1Points < player2Points){
                    winner = "X";
                }
                else{
                    winner = "TIE";
                }
                // end the game
                endGame();
            }
            // if the principle was not met, reset the game board and update information
            else{
                reset();
            }
        }
        // if the playType is infinite, reset not matter what.
        else{
            reset();
        }
    }

    /**
     * This method controls the computer's move. In this game, the computer move is not that smart
     * and is simply just randomly finding a spot to move on
     */
    private void computerMove(){
        int x = 0;
        int y = 0;
        do{
            // find random spot
            x = (int)(Math.random()*3);
            y = (int)(Math.random()*3);
            // check if it is free
            if (boardButtons[x][y].getText().toString().equals("")){
                // set symbol
                if (player1Turn) {
                    boardButtons[x][y].setText("O");
                }
                else {
                    boardButtons[x][y].setText("X");
                }
                // switch turns
                player1Turn = !player1Turn;
                // add to player count
                turnCount += 1;
                // check if someone won
                winnerCheck();
                //break
                break;
            }
            // keep going until there is a valid move
        } while (true);
    }

    /**
     * This method checks if anyone has won the round. It returns "O" for player 1, "X" for player
     * 2, "N" for when all spots are taken (tie), and "" for if there was no winner.
     * @return String the winner, if there was none, ""
     */
    private String hasWon(){

        // Set up string array
        String[][] grid = new String[3][3];
        for (int x = 0; x < 3; x++){
            for (int y = 0; y < 3; y++){
                grid[x][y] = boardButtons[x][y].getText().toString();
            }
        }

        // check all columns and rows for a winner.
        for (int x = 0; x < 3; x++){
            if (grid[x][0].equals(grid[x][1]) && grid[x][1].equals(grid[x][2]) && !grid[x][0].equals("")){
                return grid[x][0];
            }
            if (grid[0][x].equals(grid[1][x]) && grid[1][x].equals(grid[2][x]) && !grid[0][x].equals("")){
                return grid[0][x];
            }
        }
        // check diagonals
        if (grid[0][0].equals(grid[1][1]) && grid[1][1].equals(grid[2][2]) && !grid[0][0].equals("")){
            return grid[0][0];
        }
        if (grid[2][0].equals(grid[1][1]) && grid[1][1].equals(grid[0][2]) && !grid[2][0].equals("")){
            return grid[2][0];
        }
        // check if there are any spots left
        if (turnCount == 9){
            return "N";
        }
        // if nothing in met above, no one won
        return "";
    }

    /**
     * This method ends the game when there is an overall winner. It disables all board buttons so
     * that they are unclickable and changes the message to a winner message
     */
    private void endGame(){
        // disable all game board buttons
        for (int x = 0; x < 3; x++){
            for (int y = 0; y < 3; y++){
                boardButtons[x][y].setClickable(false);
            }
        }

        // find the winner and change the label accordingly
        if (winner.equals("O")){
            message = player1NameLabel.getText().toString().substring(0,
                    player1NameLabel.getText().toString().length()-1) + " WINS!";
        }
        else if (winner.equals("X")){
            message = player2NameLabel.getText().toString().substring(0,
                    player2NameLabel.getText().toString().length()-1) + " WINS!";
        }
        else {
            message = "TIE!";
        }
        // update all text fields accordingly
        updateTextFields();
    }

    /**
     * This method resets the game board. It is called whenever there is no overall winner.
     */
    private void reset(){
        // add to number of rounds
        numberOfRounds += 1;

        // update round message
        message = "Round " + (numberOfRounds + 1);
        // update text fields
        updateTextFields();
        // make it player 1 turn again
        player1Turn = true;

        // reset turn count
        turnCount = 0;

        // update all board buttons so that there is nothing in them
        for (int x = 0; x < 3; x++){
            for (int y = 0; y < 3; y++){
                boardButtons[x][y].setText("");
            }
        }
    }

    /**
     * This method updates the points after evey round using information from the hasWon method and
     * player1Turn variable.
     */
    private void updatePoints(){
        if (winner.equals("N")){
            return;
        }
        // Due to the logic of the game, when player 1 wins, it will not be their turn
        else if (!player1Turn){
            player1Points += 1;
        }
        else {
            player2Points += 1;
        }
    }

    /**
     * This method updates the text fields.
     */
    private void updateTextFields(){
        player1PointLabel.setText(String.valueOf(player1Points));
        player2PointLabel.setText(String.valueOf(player2Points));
        winnerMessage.setText(message);
    }

    @Override
    public void onResume() {
        String name;
        player1Turn = savedPref.getBoolean("p1Turn", true);
        player1Points = savedPref.getInt("p1Points", 0);

        name = savedPref.getString("p1Name", "Player 1") + ":";
        player1NameLabel.setText(name);
        player2Points = savedPref.getInt("p2Points", 0);

        name = savedPref.getString("p2Name", "Player 2") + ":";
        player2NameLabel.setText(name);
        isComputer = player2NameLabel.getText().toString().equals("Computer:");
        numberOfRounds = savedPref.getInt("roundNumber", 0);
        turnCount = savedPref.getInt("turnNumber", 0);
        message = savedPref.getString("message", "Round" + numberOfRounds + 1);
        playType = savedPref.getInt("playType", 0);
        principle = savedPref.getInt("principle", 0);

        for (int x = 0; x < 3; x++){
            for (int y = 0; y < 3; y++){
                boardButtons[x][y].setText(savedPref.getString("button" + x + y, ""));
            }
        }
        updateTextFields();
        super.onResume();
    }

    @Override
    public void onPause(){
        Editor prefEditor = savedPref.edit();

        String name;
        prefEditor.putBoolean("newGame", newGame);
        prefEditor.putBoolean("p1Turn", player1Turn);
        prefEditor.putString("p1Name", player1NameLabel.getText().toString().substring(0,
                player1NameLabel.getText().toString().length()-1));
        prefEditor.putInt("p1Points", player1Points);
        prefEditor.putString("p2Name", player2NameLabel.getText().toString().substring(0,
                player2NameLabel.getText().toString().length()-1));
        prefEditor.putInt("p2Points", player2Points);
        prefEditor.putString("message", message);
        prefEditor.putInt("roundNumber", numberOfRounds);
        prefEditor.putInt("turnNumber", turnCount);
        prefEditor.putInt("playType", playType);
        prefEditor.putInt("principle", principle);

        for (int x = 0; x < 3; x++){
            for (int y = 0; y < 3; y++){
                prefEditor.putString("button" + x + y, boardButtons[x][y].getText().toString());
            }
        }

        prefEditor.commit();

        super.onPause();
    }
}