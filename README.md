# WildRank - Android App


[![Build Status](https://travis-ci.org/nwalters512/wildrank-android.png?branch=master)](https://travis-ci.org/nwalters512/wildrank-android)

An Android app for scouting at FIRST Robotics Competition created by Team 111 WildStang.

## Usage
WildRank is designed to serve as a platform upon which scouting systems can be built on. WildRank provides match scouting, pit scouting, notetaking, and data analysis. The framework offers checkboxes, counters, text boxes, number fields, and dropdown spinners to be used for data collection.

This app relies on a companion desktop app, which can be found at [this repository](https://github.com/nwalters512/wildrank-desktop). That app downlaods the appropriate list of matches/teams for a given event and puts them on a flash drive, which is then synced to all the tablets to configure them for the given event.

### Syncing
WildRank is designed to function without any internet connection after the initial setup. Syncing is done via a flash drive using a USB OTG (On-The-Go) adapter. The adapter is required to connect he flash drive to Android devices. Note that this means that your android device(s) must suppport the OTG protocol; if they do not, you may be able to root your device in order to add support.

For the implementation of syncing, see ```DataManager.syncWithFlashDrive(...)```.

### Important note about how WildRank stores data
In WildRank, every piece of user-generated is stored as a JSON string in a text file. For instance, the results of match 32 for team 111 would be stored in ```/matches/32/111.json```. This approach was selected over something like an SQL database to allow for the easy changing of models and easy syncing between devices.

### Configuring WildRank for your team
WildRank is designed to be easy to update for each new game. Most of the app can stay the same year-to-year: team lists, storing data, syncing data, notetaking, etc. Most changes that will need to be made are done via the XML layout files.

#### Configuring match scouting
The relevant XML files for match scouting are:
 * ```fragment_scout_autonomous.xml```
 * ```fragment_scout_teleop.xml```
 * ```fragment_scout_post_match.xml```

To add a field to a scouting page, simply include one of the ```JSONSerializableView```s in the appropriate layout file. The included views are:
 * ```SerializableCounterView```
 * ```SerializableCheckboxView```
 * ```SerializableNumberView```
 * ```SerializableSpinnerView```
 * ```SerializableTextView```

If the functionality you want isn't provided by any of the included widgets, you can make your own view that implements ```IJSONSerializable```. See any of the included views for an example of how such a class should behave.

Each defined view in the XML layout files should provide a ```key``` and ```label``` attribute. The label is used (obviously) as a lable for the view. The key is used to tell the framework how to store the value when the match results are saved. It should consist of a hyphenated string of levels in a JSON object hierarchy. For instance, a view with a key of ```autonomous-hot_high``` would be saved as the following JSON object:

```json
{
    "autonomous": {
        "hot_high": VALUE
    }
}
```

An example layout file is included below. It contains two columns. The first is titled "General" and has a single checkbox for recording if a robot moves. The second is titled "Scoring" and has a counter that counts how many balls were scored in the hot high goal.

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:custom="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/light_gray" >

    <!-- General -->

    <LinearLayout
        android:id="@+id/general"
        android:layout_width="250dp"
        android:layout_height="wrap_content"
        android:layout_alignParentLeft="true"
        android:layout_alignParentTop="true"
        android:layout_marginRight="10dp"
        android:orientation="vertical" >
        
        <TextView
            android:id="@+id/general_title"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="General"
            android:textSize="30sp" />

        <org.wildstang.wildrank.android.customviews.SerializableCheckboxView
            android:id="@+id/autonomous_move"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            custom:key="autonomous-move"
            custom:label="Moved" />
    </LinearLayout>

    <!-- Scoring -->

    <LinearLayout
        android:id="@+id/scoring"
        android:layout_width="250dp"
        android:layout_height="wrap_content"
        android:layout_alignParentTop="true"
        android:layout_toRightOf="@id/general"
        android:orientation="vertical" >

        <TextView
            android:id="@+id/scoring_title"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Scoring"
            android:textSize="30sp" />

        <org.wildstang.wildrank.android.customviews.SerializableCounterView
            android:id="@+id/autonomous_hot_goal_high_scored"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            custom:key="autonomous-scored_hot_high"
            custom:label="Hot Goal High" />
    </LinearLayout>

</RelativeLayout>
```

A possible output, given this layout, for match 32 of team 111 would look like this:

```
{
    "team_numner": 111,
    "match_number": 32,
    "scouter_id": "SCOUTER NAME",
    "scoring" {
        "autonomous": {
            "move": true,
            "scored_hot_high": 2
        }
    }
}
```

#### Configuring pit scouting
Pt scouting is handled very similarly to match scouting. Simply incude any number of ```JSONSerializableView```s in the layout, providing a ```key``` and ```label``` for each one. All changes for pit scouting should be made in ```fragment_scout_pit.xml```

#### Configuring the pit summary
The Team Summary mode provides the ability to show a summary of the pit data that was collected for the selected team. This is done via ```TemplatedTextView```s. A ```TamplatedTextView``` will allow you to dynamically substitute in loaded data. It will use any text eclosed by double braces ```{{...}}``` as a key to get a value from a JSON file. It works similary to the ```key``` from pit/match scouting. The best way to explain this is to provide an example.

Say I have the following pit data JSON object and I want to display the ```robot_weight``` field from it:

```json
{
    "team_number": 111,
    "scouted_by": "SCOUTER NAME",
    "scoring": {
        "robot_weight": 120
    }
}
```

I would create a ```TemplatedTextView``` in my layout and add a ```text``` attribute to the view (note that this should be in the project xml namespace, not the android one; for instance, you might use ```custom:text``` instead of ```android:text```). The ```text``` attribute should consist of a hyphenated string of levels in a JSON object hierarchy. So, to display the robot weight, I would add the attribute ```custom:text="&lt;b&gt;Weight:&lt;/b&gt; {{scoring-robot_weight}} lbs"```. This would display as

**Weight**: 120 lbs
 
Note that the ```text``` attribute is parsed as HTML, so you can use basic HTML formatting in your text strings.

#### Configuring the data view
Team Summary mode also provides basic data analysis, which is handled similarly to the pit summary. It uses ```DataView``` views with an ```expression``` attribute. It provides support for addition, subtraction, multiplication, and division, as well as an average function that will evaluate to the average of the specified field for all matches for the given team.

For instance, take the following:

```xml
custom:expression="AVERAGE(teleop-scored_low) + 10*AVERAGE(teleop-scored_high)"
```

This would evaluate to the average teleop low goals per match plus 10 times the average high goals per match, giving a very basic average score. Note that the key system is the same: a piece of data that was collected with the key ```teleop-scored_high``` is accessed via the same key.
 
#Contributing
Want to add features, fix bugs, or just poke around the code? No problem!

1. Set up your development environment if you haven't used Android Studio before ([see below](#setup))
2. Fork this repository, import the project to your IDE, and create a branch for your changes
3. Make, commit, and push your changes to your branch
4. Submit a pull request here and we'll review it and get it added in!

For more detailed instructions, checkout [GitHub's Guide to Contributing](https://guides.github.com/activities/contributing-to-open-source/)

#Environment Setup<a name="setup"></a>


1. Ensure that you have git installed and that it is added to your system's PATH variable. You should be open you system's shell, navigate to a git repository (like this one), run ```git status``` and get data back.
2. If you haven't already, make sure you have the Android development environment set up. You will need to have [Android Studio](https://developer.android.com/sdk/installing/studio.html) installed (this also required the [JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html)).
3. Make sure you read through some [Tips and Tricks](https://developer.android.com/sdk/installing/studio-tips.html) for developing with Android Studio.
4. Use the [Android SDK Manager](https://developer.android.com/tools/help/sdk-manager.html) to download the correct versions of the Android libraries. You will need to download the Android SDK Tools, Android SDK Platform-Tools, and the SDK Platform for Android version 4.4 (API level 19). If you have already downloaded these, double check and make sure they've been updated to the latest version.
5. If you have an Android device you want to test on, make sure that you have [enabled USB Debugging](http://stackoverflow.com/questions/16707137/how-to-find-and-turn-on-usb-debugging-mode-on-nexus-4) in your Settings menu. Otherwise, [configure a Virtual Device](https://developer.android.com/tools/devices/managing-avds.html) to debug with (you will have to also download the ARM System image from the SDK manager to use a virtual device).
