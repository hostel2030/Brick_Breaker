package com.finalproject.brickbreaker.activities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.finalproject.brickbreaker.managers.LevelsPatternsManager;
import com.finalproject.brickbreaker.R;
import com.finalproject.brickbreaker.services.BrickTypes;
import com.finalproject.brickbreaker.services.BrickTypesHelper;
import com.finalproject.brickbreaker.services.CustomImageAdapter;
import com.finalproject.brickbreaker.services.Settings;

import java.util.ArrayList;


public class LevelBuilderActivity extends Activity{

    BrickTypes[][] levelPattern;

    ImageView selectedImageView;
    int selectedItemRow;
    int selectedItemCol;

    Spinner spinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_builder);

        InitLevelPattern();
        InitBricksGrid();
        InitTypesSpinner();
        InitAddLevel();

    }

    private void InitAddLevel(){
        final Button button = (Button) findViewById(R.id.saveLevelButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(IsValidPattern() == false){
                    Toast toast = Toast.makeText(getBaseContext(), "Invalid level, no bricks to break!",Toast.LENGTH_LONG);
                    toast.show();
                }
                else{
                    LevelsPatternsManager.GetInstance(getBaseContext()).AddLevel(levelPattern);
                    finish();
                }
            }
        });
    }

    private boolean IsValidPattern(){
        for (int i=0;i< Settings.MAX_ROWS;i++)
            for(int j=0;j<Settings.MAX_COLUMS;j++)
                if(levelPattern[i][j] != null && levelPattern[i][j] != BrickTypes.Empty && levelPattern[i][j] != BrickTypes.Wall)
                    return true;
        return false;
    }

    private void InitLevelPattern() {
        levelPattern = new BrickTypes[Settings.MAX_ROWS][Settings.MAX_COLUMS];
        for (int i=0;i<Settings.MAX_ROWS;i++)
            for(int j=0;j<Settings.MAX_COLUMS;j++)
                levelPattern[i][j] = BrickTypes.Empty;
    }

    private void InitBricksGrid(){
        final GridView gridview = (GridView) findViewById(R.id.gridview);

        gridview.setAdapter(new CustomImageAdapter(this));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                if(selectedImageView == v) return;
                if(selectedImageView != null)
                    selectedImageView.setBackgroundColor(Color.TRANSPARENT);
                selectedImageView = (ImageView) v;
                selectedItemRow = position/Settings.MAX_COLUMS;
                selectedItemCol= position - selectedItemRow*Settings.MAX_COLUMS;
                BrickTypes type = levelPattern[selectedItemRow][selectedItemCol];
                int typePosition = type.ordinal();
                spinner.setSelection(typePosition);
                selectedImageView.setBackgroundColor(Color.BLUE);
            }
        });
    }

    private void InitTypesSpinner(){
        ArrayList<String> brickTypesNames = new ArrayList<>();
        for (BrickTypes brickType : BrickTypes.values()){
            brickTypesNames.add(BrickTypesHelper.GetDisplayName(brickType));
        }

        spinner = (Spinner) findViewById(R.id.brickTypesSpinner);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, brickTypesNames);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(selectedImageView != null){
                    BrickTypes brickType = BrickTypes.values()[position];
                    selectedImageView.setImageResource(BrickTypesHelper.GetImageId(brickType));
                    levelPattern[selectedItemRow][selectedItemCol] = brickType;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

}
