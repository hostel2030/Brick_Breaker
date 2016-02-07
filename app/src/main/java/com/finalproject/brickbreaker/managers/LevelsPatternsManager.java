package com.finalproject.brickbreaker.managers;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.finalproject.brickbreaker.services.BrickTypes;
import com.finalproject.brickbreaker.interfaces.IOnLevelAddedListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class LevelsPatternsManager {

    private static LevelsPatternsManager instance;

    private ArrayList<BrickTypes[][]> levelsPatterns;

    ArrayList<BrickTypes[][]> customPatterns;

    public static final String PREFS_NAME = "MyPrefsFile";
    private String customPatternsKey = "patterns";
    Type collectionType = new TypeToken<List<BrickTypes[][]>>(){}.getType();
    private SharedPreferences prefs;
    private IOnLevelAddedListener levelAddedListener;

    private LevelsPatternsManager(Context context){

        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);


        levelsPatterns = new ArrayList<BrickTypes[][]>();

        BrickTypes[][] pattern = new BrickTypes[][] {
                { BrickTypes.Empty,		BrickTypes.Normal1, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Wall, 		BrickTypes.Wall, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Normal1,		BrickTypes.Empty },
                { BrickTypes.Empty,		BrickTypes.Normal1, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Normal1,		BrickTypes.Empty },
                { BrickTypes.Empty, 	BrickTypes.Normal1, 	    BrickTypes.Normal1, 	BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Normal1, 	BrickTypes.Normal1,		BrickTypes.Empty },
                { BrickTypes.Empty, 	BrickTypes.Normal2, 	    BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Normal2, 	BrickTypes.Normal2, 	BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Normal2,		BrickTypes.Empty },
                { BrickTypes.Wall, 	    BrickTypes.Normal2, 	    BrickTypes.Normal2, 	BrickTypes.Normal1, 	BrickTypes.Normal2, 	BrickTypes.Life, 		BrickTypes.Normal1,		BrickTypes.Normal2, 	BrickTypes.Normal2,		BrickTypes.Wall },
                { BrickTypes.Wall, 	    BrickTypes.Normal3, 	    BrickTypes.Empty, 	    BrickTypes.Normal1, 	BrickTypes.Ball, 		BrickTypes.Big, 	    BrickTypes.Normal1,		BrickTypes.Empty, 		BrickTypes.Normal3,		BrickTypes.Wall },
                { BrickTypes.Wall, 	    BrickTypes.Normal4, 	    BrickTypes.Empty, 		BrickTypes.Normal1, 	BrickTypes.Normal1, 	BrickTypes.Normal1, 	BrickTypes.Normal1,		BrickTypes.Empty, 		BrickTypes.Normal4,		BrickTypes.Wall },
                { BrickTypes.Wall, 	    BrickTypes.Normal2, 	    BrickTypes.Normal3, 	BrickTypes.Normal1, 	BrickTypes.Normal2, 	BrickTypes.Normal2, 	BrickTypes.Normal1,		BrickTypes.Normal3, 	BrickTypes.Normal2,		BrickTypes.Wall },
                { BrickTypes.Empty,		BrickTypes.Normal2, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Normal2, 	BrickTypes.Normal2, 	BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Normal2,		BrickTypes.Empty },
                { BrickTypes.Empty,		BrickTypes.Normal1, 		BrickTypes.Normal4, 	BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Normal4, 	BrickTypes.Normal1,		BrickTypes.Empty },
                { BrickTypes.Empty,		BrickTypes.Normal1, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Normal1,		BrickTypes.Empty },
                { BrickTypes.Empty,		BrickTypes.Normal1, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Wall, 		BrickTypes.Wall, 		BrickTypes.Empty, 		BrickTypes.Empty, 		BrickTypes.Normal1,		BrickTypes.Empty },
        };

        levelsPatterns.add(pattern.clone());
        levelsPatterns.add(pattern.clone());
        levelsPatterns.add(pattern.clone());
        levelsPatterns.add(pattern.clone());
        levelsPatterns.add(pattern.clone());
        levelsPatterns.add(pattern.clone());
        levelsPatterns.add(pattern.clone());
        levelsPatterns.add(pattern.clone());

        customPatterns = new Gson().fromJson(prefs.getString(customPatternsKey, null), collectionType);

        if(customPatterns == null)
            customPatterns = new ArrayList<>();

        levelsPatterns.addAll(customPatterns);
    }

    public static LevelsPatternsManager GetInstance(Context context){
        if(instance == null)
            instance = new LevelsPatternsManager(context);
        return instance;
    }

    public ArrayList<BrickTypes[][]> GetLevelsPatterns(){
        return levelsPatterns;
    }

    public void AddLevel(BrickTypes[][] pattern) {
        levelsPatterns.add(pattern);

        customPatterns.add(pattern);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(customPatternsKey, new Gson().toJson(customPatterns));
        editor.commit();

        if(levelAddedListener != null)
            levelAddedListener.OnLevelAdded();
    }

    public void RegisterLevelAdded(IOnLevelAddedListener levelAddedListener) {
        this.levelAddedListener = levelAddedListener;
    }
}
