/**
 * This activity if the options menu of the app. It is the first activity the user is in and
 * will allow the user to customize their name and what type of game they want to play
 */
package com.example.jensu.tictactoe;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class OptionsActivity extends Activity {

    private Spinner playTypeSpinner;
    private TextView player2GetNameLabel, gameEndPrincipleLabel;
    private EditText player1GetNameInput, player2GetNameInput, gameEndPrincipleInput;
    private Switch isComputerSwitch;
    private Button startButton;

    private boolean isComputer;
    private int playType;
    private boolean newGame;

    private SharedPreferences savedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        Intent intent = getIntent();

        savedPref = getSharedPreferences("TTT_Prefs", MODE_PRIVATE);

        newGame = false;
        isComputer = false;
        playType = 0;

        setEditors();
        setOthers();

        player2GetNameLabel = findViewById(R.id.playerTwoGetNameLabel);
        gameEndPrincipleLabel = findViewById(R.id.gameEndPrincipleLabel);


    }

    /**
     * This method sets up all widgets other than the labels that can turn visible and invisible and
     * Edit Texts.
     */
    private void setOthers(){
            playTypeSpinner = findViewById(R.id.playTypeSpinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.play_type_array,
                    android.R.layout.simple_spinner_item);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            playTypeSpinner.setAdapter(adapter);
            playTypeSpinner.setOnItemSelectedListener(new SpinnerEventListener());

            isComputerSwitch = findViewById(R.id.isComputerSwitch);
            isComputerSwitch.setOnCheckedChangeListener(new SwitchEventListener());

            startButton = findViewById(R.id.gameStartButton);
            startButton.setOnClickListener(new ButtonEventListener());
    }

    /**
     * This method sets up all Edit Texts of the activity.
     */
    private void setEditors(){
        OnEditorActionListener editTextEventListener = new EditTextEventListener();

        player1GetNameInput = findViewById(R.id.playerOneGetNameInput);
        player1GetNameInput.setOnEditorActionListener(editTextEventListener);

        player2GetNameInput = findViewById(R.id.playerTwoGetNameInput);
        player2GetNameInput.setOnEditorActionListener(editTextEventListener);

        gameEndPrincipleInput = findViewById(R.id.gameEndPrincipleInput);
        gameEndPrincipleInput.setOnEditorActionListener(editTextEventListener);
    }

    /**
     * This class is in charge of all actions made in an Edit Text
     */
    class EditTextEventListener implements OnEditorActionListener{

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // for sanity reasons, if the user inputs 0 or lower number, it will automatically
            // change to default 1
            if (Integer.parseInt(gameEndPrincipleInput.getText().toString()) < 1){
                    gameEndPrincipleInput.setText("1");
            }
            // For the second player name input, I wanted the name to save even after a game with
            // a computer finished. So I created a new key in the SharedPreferences for it.
            else if(((EditText)v).getId() == player2GetNameInput.getId()){
                Editor prefEditor = savedPref.edit();
                prefEditor.putString("cP2Name", player2GetNameInput.getText().toString());
                prefEditor.commit();
            }
            return false;
        }
    }

    /**
     * This class is in charge of all actions made by a Switch
     */
    class SwitchEventListener implements OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // There is only one switch in the activity. This switch allows the user to choose if
            // they play against a computer or not.
            // if it isn't checked there is no computer
            if (!isChecked){
                // if it is not checked make all player 2 customs visible
                player2GetNameLabel.setVisibility(View.VISIBLE);
                player2GetNameInput.setVisibility(View.VISIBLE);
                // just so that they don't need to type, if there was a name that was used before
                // make it that name
                player2GetNameInput.setText(savedPref.getString("cP2Name", "Player 2"));
                isComputer = false;
            }
            // otherwise there is a computer so the player 2 customs will go invisible
            else{
                player2GetNameLabel.setVisibility(View.INVISIBLE);
                player2GetNameInput.setVisibility(View.INVISIBLE);
                isComputer = true;
            }
        }
    }

    /**
     * This class handles all actions made in a Spinner
     */
    class SpinnerEventListener implements OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // There is only one spinner, so the playType will change if the user changed it.
            playType = position;

            changeType(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    /**
     * This method enables the principle labels and Edit Text according to what position the
     * Spinner is currently in.
     * @param position The spinner's current position
     */
    private void changeType(int position){
        // If the position is 'infinitely', then the principle label and Edit Text become invisible
        if (position == 0){
            gameEndPrincipleInput.setVisibility(View.INVISIBLE);
            gameEndPrincipleLabel.setVisibility(View.INVISIBLE);
        }

        // otherwise they are visible/
        else {
            gameEndPrincipleLabel.setVisibility(View.VISIBLE);
            gameEndPrincipleInput.setVisibility(View.VISIBLE);
            // just so that the player doesn't need to type, use old preferences as the value.
            gameEndPrincipleInput.setText(String.valueOf(savedPref.getInt("principle", 1)));
            // According to the position change the label accordingly
            if (position == 1){
                gameEndPrincipleLabel.setText(R.string.number_of_rounds);
            }
            else {
                gameEndPrincipleLabel.setText(R.string.number_of_points);
            }
        }
    }

    /**
     * This class handles all button actions
     */
    class ButtonEventListener implements OnClickListener{

        @Override
        public void onClick(View v) {
            // There is only one button, so when clicked it switches to the game activity with all
            // the customs made in the options menu.
            Intent intent = new Intent(getActivity(), GameBoardActivity.class);
            // I couldn't find any other way to reset the entire game board, so I just changes all
            // the variables that the game activity would use into the default values of the first
            // game
            Editor prefEditor = savedPref.edit();
            prefEditor.putInt("p1Points", 0);
            prefEditor.putInt("p2Points", 0);
            prefEditor.putString("message", "Round 1");
            prefEditor.putInt("roundNumber", 0);
            prefEditor.putInt("turnNumber", 0);
            prefEditor.putBoolean("p1Turn", true);
            for (int x = 0; x < 3; x++){
                for (int y = 0; y < 3; y++){
                    prefEditor.putString("button" + x + y, "");
                }
            }
            prefEditor.commit();
            // the game has started so if the user reboots, it takes it them to the game board
            // automatically.
            newGame = true;
            startActivity(intent);
        }
    }

    /**
     * This method returns the the activity object. This is for the Intent object when switching
     * activities inside the ButtonListener class.
     * @return Activity Options activity.
     */
    private Activity getActivity(){
        return this;
    }

    @Override
    public void onResume(){
        player1GetNameInput.setText(savedPref.getString("p1Name", "Player 1"));
        isComputer = savedPref.getBoolean("isComputer", false);
        if(isComputer) {
            isComputerSwitch.setChecked(true);
            player2GetNameLabel.setVisibility(View.INVISIBLE);
            player2GetNameInput.setVisibility(View.INVISIBLE);
        }
        else{
            isComputerSwitch.setChecked(false);
            player2GetNameInput.setVisibility(View.VISIBLE);
            player2GetNameInput.setText(savedPref.getString("p2Name", "Player 2"));
            player2GetNameLabel.setVisibility(View.VISIBLE);
        }
        playType = savedPref.getInt("playType", 0);
        playTypeSpinner.setSelection(playType);
        changeType(playType);

        if (savedPref.getBoolean("newGame", false)){
            Intent intent = new Intent(getActivity(), GameBoardActivity.class);
            startActivity(intent);
        }
        super.onResume();
    }

    @Override
    public void onPause(){
        Editor prefEditor = savedPref.edit();

        prefEditor.putString("p1Name", player1GetNameInput.getText().toString());
        prefEditor.putBoolean("isComputer", isComputer);

        if(!isComputer) {
            prefEditor.putString("p2Name", player2GetNameInput.getText().toString());
        }
        else{
            prefEditor.putString("p2Name", "Computer");
        }

        prefEditor.putBoolean("newGame", newGame);
        prefEditor.putInt("playType", playType);
        prefEditor.putInt("principle", Integer.parseInt(gameEndPrincipleInput.getText().toString()));

        prefEditor.commit();
        super.onPause();
    }
}
