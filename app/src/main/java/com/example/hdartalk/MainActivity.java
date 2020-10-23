package com.example.hdartalk;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.example.hdartalk.adapters.NotesAdapter;
import com.example.hdartalk.auths.AccountActivity;

import com.example.hdartalk.callbacks.MainActionModeCallback;
import com.example.hdartalk.callbacks.NoteEventListener;
import com.example.hdartalk.db.AppDatabase;
import com.example.hdartalk.db.MoodsDao;
import com.example.hdartalk.db.NotesDao;
import com.example.hdartalk.db.ResourcesDao;
import com.example.hdartalk.intro.IntroActivity;
import com.example.hdartalk.model.Mood;
import com.example.hdartalk.model.Note;
import com.example.hdartalk.model.Resource;

import com.example.hdartalk.navigation.SettingsFragment;
import com.example.hdartalk.navigation.ResourcesFragment;
import com.example.hdartalk.navigation.StatisticsFragment;
import com.example.hdartalk.utils.AlarmReceiver;
import com.example.hdartalk.utils.NoteUtils;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.ActionMode;

import android.view.View;

import android.view.Menu;
import android.view.MenuItem;

import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.google.android.material.navigation.NavigationView;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;


import static com.example.hdartalk.EditNoteActivity.NOTE_EXTRA_Key;

public class MainActivity extends AppCompatActivity implements NoteEventListener, Drawer.OnDrawerItemClickListener, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private ArrayList<Note> notes;
    private NotesAdapter adapter;
    private NotesDao dao;
    private MainActionModeCallback actionModeCallback;
    private int checkedCount = 0;
    private FloatingActionButton fab;
    private SharedPreferences settings;
    public static final String THEME_Key = "app_theme";
    public static final String APP_PREFERENCES="notepad_settings";
    private int theme;

    public static final String QUERY_MOOD_PARAMETER = "MainActivity.QueryMood";
    public static final String NOTIFICATION_CHANNEL_ID = "MainActivity.NotificationChan";

    private int mCurrentMood = 1;
    private int mCurrentMoodIntensity = 1;
    private Drawer result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        theme = settings.getInt(THEME_Key, R.style.AppTheme);
        setTheme(theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPrefs.getBoolean(IntroActivity.LAUNCHED_APP_BEFORE, false)) {
            Intent intent = new Intent(this, IntroActivity.class);
            finish();
            startActivity(intent);
            setupResourcesDatabase();
        }
        registerNotificationChannel();
        //

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupNavigation(savedInstanceState, toolbar);
        // init recyclerView
        recyclerView = findViewById(R.id.notes_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // init fab Button
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onAddNewNote();
            }
        });

        dao = AppDatabase.getDatabase(this).notesDao();
    }

    //
    void registerNotificationChannel() {
        /**
         * Registers a notification channel which is required to post notifications
         * to the user. This is done repeatedly whenever the app is started but
         * there is not problem with calling .createNotificationChannel repeatedly.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID, "DailyNotification", importance);
            channel.setDescription("HdarTalk.Notifications");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    //
    /*void setupBottomNavigation() {
        /**
         * Links the BottomNavigationView element to the NavController that controls
         * the NavHost containing all the top-level UI fragments. The NavController
         * is then used to switch between UIs/fragments.
         */
     /*   BottomNavigationView bottomNav = findViewById(R.id.bottom_nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNav, navController);
    }
    */
    //
    void setupResourcesDatabase() {
        /**
         * Sets up the Resources database with all the articles.
         */
        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        ResourcesDao resDao = database.resourcesDao();

        // Depressed = 1, Sad = 2, Angry = 3, Scared = 4, Moderate = 5, Happy = 6
        final int MoodDepressed = 1;
        final int MoodSad = 2;
        final int MoodAngry = 3;
        final int MoodScared = 4;
        final int MoodModerate = 5;
        final int MoodHappy = 6;

        // Create the list of all the resources
        Resource[] allResources = {
                // Depressed
                new Resource("Coping with depression", "When you’re depressed, you can’t just will yourself to “snap out of it.” But these tips can help put you on the road to recovery.", "https://www.helpguide.org/articles/depression/coping-with-depression.htm", MoodDepressed),
                new Resource("What is depression?", "Depression is a disorder that is evidenced by excessive sadness, loss of interest in enjoyable things, and low motivation.", "https://thiswayup.org.au/how-do-you-feel/sad/", MoodDepressed),
                new Resource("Cat", "Watch this video.", "https://www.youtube.com/watch?v=xbs7FT7dXYc", MoodDepressed),
                new Resource("Depression Symptoms and Warning Signs", "Do you think you might be depressed? Here are some of the signs and symptoms to look for—and tips for getting the help you need.", "https://www.helpguide.org/articles/depression/depression-symptoms-and-warning-signs.htm", MoodDepressed),

                // Sad
                new Resource("Alone in the crowd - How loneliness affects the mind and body", "Watch this video about being lonely.", "https://www.youtube.com/watch?v=R8A7JodFx4s", MoodSad),
                new Resource("Am I Depressed or Just Really Sad?", "People often think they’re depressed when they’re sad, or sad when they’re depressed.", "https://www.vice.com/en_us/article/9kzqa7/am-i-depressed-difference-sadness-depression", MoodSad),
                new Resource("Why am I sad all the time?", "Ever felt sad or stressed for no apparent reason?", "https://au.reachout.com/articles/why-am-i-sad-all-the-time", MoodSad),
                new Resource("How do I know if I'm sad or depressed?", "If you're afraid that your depressed, there are many things you can do to help figure it out.", "https://www.7cups.com/qa-depression-3/how-do-i-know-if-im-sad-or-depressed-650/", MoodSad),

                // Angry
                new Resource("Anger Management", "Is your temper hijacking your life? These tips and techniques can help you get anger under control and express your feelings in healthier ways.", "https://www.helpguide.org/articles/relationships-communication/anger-management.htm", MoodAngry),
                new Resource("Controlling anger before it controls you", "We all know what anger is, and we've all felt it: whether as a fleeting annoyance or as full-fledged rage.", "https://www.apa.org/topics/anger/control", MoodAngry),
                new Resource("I'm Angry", "Watch this video.", "https://www.youtube.com/watch?v=vyMx7s9cThU", MoodAngry),
                new Resource("Why Am I So Angry?", "Anger can be a force for good. But ongoing, intense anger is neither helpful nor healthy. Here's how to get a grip.", "https://www.webmd.com/mental-health/features/why-am-i-so-angry#1", MoodAngry),

                // Scared
                new Resource("Phobias and Irrational Fears", "Is a phobia keeping you from doing things you’d like to do? Learn how to recognize, treat, and overcome the problem.", "https://www.helpguide.org/articles/anxiety/phobias-and-irrational-fears.htm", MoodScared),
                new Resource("I'm Scared", "The fact that you feel scared about these intrusive thought means that you need to see a psychotherapist.", "https://www.mentalhelp.net/advice/i-m-scared/", MoodScared),
                new Resource("Jeremy Zucker - Scared (Lyrics)", "Listen to song about loneliness.", "https://www.youtube.com/watch?v=iyEUvUcMHgE", MoodScared),
                new Resource("How To Stop Being So Goddamn Scared All The Time", "So, you're scared. Let's finally talk about that, shall we?", "https://ittybiz.com/how-to-stop-being-scared/", MoodScared),

                // Moderate
                new Resource("5 Steps To Avoid Complacency", "Remember the fire in the belly you felt on the way to achieving a goal?", "https://thetobincompany.com/5-steps-to-avoid-complacency/", MoodModerate),
                new Resource("How to be human: what it means to feel normal", "Leah Reich was one of the first internet advice columnists", "https://www.theverge.com/2017/2/5/14514224/how-to-be-human-depression-anxiety-feeling-normal", MoodModerate),
                new Resource("NEVER GET COMFORTABLE - Best Motivational Video", "Motivate yourself with this video", "https://www.youtube.com/watch?v=2o8fmUlHAyk", MoodModerate),
                new Resource("10 Best Things To Do With Your Free Time", "Watch this video about using your free time", "https://www.youtube.com/watch?v=afoAXho6EHs", MoodModerate),

                // Happy
                new Resource("Feeling Happy and Being Happy Aren't the Same", "Can you be wrong about whether you are happy?", "https://www.psychologytoday.com/us/blog/am-i-right/201310/feeling-happy-and-being-happy-arent-the-same", MoodHappy),
                new Resource("How to feel happier, according to neuroscientists and psychologists", "Researchers have known for decades that certain activities make us feel better, and they're just beginning to understand what happens in the brain to boost our mood.", "https://www.businessinsider.com/how-feel-happy-happier-better-2017-7", MoodHappy),
                new Resource("Pharrell Williams - Happy", "Listen to Pharrell sing about being Happy!", "https://www.youtube.com/watch?v=ZbZSe6N_BXs", MoodHappy),
                new Resource("The Science of Happiness: What Actually Makes Us Happy", "We all want to be happy. Period. In fact, I would argue that nearly everything we do, whether it’s working, marrying, running, or even filing our taxes is done with an overarching purpose: To feel happier.", "https://medium.com/@MaxWeigand/the-science-of-happiness-what-actually-makes-us-happy-78edcc9bdd58", MoodHappy),
        };

        resDao.insertAll(allResources);

        Toast.makeText(this, "Resource database loaded", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_resources:
                getSupportFragmentManager().beginTransaction().replace(R.id.resources_layout, new ResourcesFragment())
                        .commit();
                break;
            case R.id.nav_statistics:
                getSupportFragmentManager().beginTransaction().replace(R.id.static_layout, new StatisticsFragment())
                        .commit();
                break;
            case R.id.nav_preference:
                //((PreferenceActivity)getActivity()).startPreferenceFragment(new PreferencesFragment(), true);
                getSupportFragmentManager().beginTransaction().replace(R.id.settings_layout, new SettingsFragment())
                        .commit();
                break;
            case R.id.nav_notes:
                startActivity(new Intent(MainActivity.this, EditNoteActivity.class));
                break;

            case R.id.nav_profile:
                startActivity(new Intent(MainActivity.this, AccountActivity.class));
                break;
        }
        return true;
    }


    //
    class MoodDialogListener implements DialogInterface.OnClickListener {
        /**
         * Listeners for the click event when the user chooses the mood
         * that they're in and hits the submit button.
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            showMoodIntensityDialog();
        }
    }
    class MoodDialogChoiceListener implements DialogInterface.OnClickListener {
        /**
         * Listens for the event in which the user chooses a different mood
         * from the multiple choice menu.
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            // Offset the choice because it goes from 0-5
            mCurrentMood = which + 1;
        }
    }

    class MoodIntesityDialogListener implements DialogInterface.OnClickListener {
        /**
         * Listens for the event in which the user chooses a mood intensity
         * using the SeekBar then hits the submit button.
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            saveMoodToDatabase();
            Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_LONG).show();
        }
    }
    class MoodIntensityDialogSeekListener implements SeekBar.OnSeekBarChangeListener {
        /**
         * Listens for updates on the SeekBar used to get the user's current mood.
         * The data is stored which is then used to save to the local SQLite database.
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // Progress goes from 0-5 but we use 1-6
            mCurrentMoodIntensity = progress + 1;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
    //
    void showCurrentMoodDialog() {
        /**
         * Shows the user a dialog that asks them for their current mood then
         * stores the result inside of an instance variable.
         */
        CharSequence moods[] = {
                // Depressed = 1, Sad = 2, Angry = 3, Scared = 4, Moderate = 5, Happy = 6
                "Depressed", "Sad", "Angry", "Scared", "Moderate", "Happy"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme);
        builder.setTitle("How are you feeling?");
        builder.setSingleChoiceItems(moods, 0, new MoodDialogChoiceListener());
        builder.setPositiveButton("Next", new MoodDialogListener());
        builder.show();
    }

     void showMoodIntensityDialog() {
        /**
         * Shows the dialog that asks the user the intensity of the mood
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppTheme);
        SeekBar seekBar = new SeekBar(MainActivity.this);
        seekBar.setMax(10);
        seekBar.setOnSeekBarChangeListener(new MoodIntensityDialogSeekListener());

        builder.setTitle("How intense is this feeling?");
        builder.setView(seekBar);
        builder.setPositiveButton("Save", new MoodIntesityDialogListener());

        builder.show();
    }
    //

    private void setupNavigation(Bundle savedInstanceState, Toolbar toolbar) {

        // Navigation menu items
        // List<IDrawerItem> iDrawerItems = new ArrayList<>(); fix error : removed on materialdrawer 7.0.0
        List<IDrawerItem<?>> iDrawerItems = new ArrayList<>();
        iDrawerItems.add(new PrimaryDrawerItem().withName("Home").withIcon(R.drawable.ic_home_black_24dp));
        iDrawerItems.add(new PrimaryDrawerItem().withName("Notes").withIcon(R.drawable.ic_note_black_24dp));
        iDrawerItems.add(new PrimaryDrawerItem().withName("Resources").withIcon(R.drawable.ic_resources));
        iDrawerItems.add(new PrimaryDrawerItem().withName("Statistics").withIcon(R.drawable.ic_statistics));
        iDrawerItems.add(new PrimaryDrawerItem().withName("Account Settings").withIcon(R.drawable.ic_account_circle_24));





        List<IDrawerItem<?>> stockyItems = new ArrayList<>();

        SwitchDrawerItem switchDrawerItem = new SwitchDrawerItem()
                .withName("Dark Theme")
                .withChecked(theme == R.style.AppTheme_Dark)
                .withIcon(R.drawable.ic_dark_theme)
                .withOnCheckedChangeListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            settings.edit().putInt(THEME_Key, R.style.AppTheme_Dark).apply();
                        } else {
                            settings.edit().putInt(THEME_Key, R.style.AppTheme).apply();
                        }
                        // recreate app or the activity // if it's not working follow this steps
                        // MainActivity.this.recreate();
                        // this lines means wi want to close the app and open it again to change theme
                        TaskStackBuilder.create(MainActivity.this)
                                .addNextIntent(new Intent(MainActivity.this, MainActivity.class))
                                .addNextIntent(getIntent()).startActivities();
                    }

                });


        /**
         .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
        @Override
        public boolean onItemClick(@Nullable View view, int i, @NotNull IDrawerItem<?> iDrawerItem) {
        if(iDrawerItem !=null) {
        Intent intent = null;
        if (iDrawerItem.getIdentifier() == 1) {
        intent = new Intent(MainActivity.this, MainActivity.class);
        } else if (iDrawerItem.getIdentifier() == 2) {
        intent = new Intent(MainActivity.this, EditNoteActivity.class);
        } else if (iDrawerItem.getIdentifier() == 3) {
        intent = new Intent(MainActivity.this, AccountActivity.class);
        } else if (iDrawerItem.getIdentifier() == 4) {
        intent = new Intent(MainActivity.this, ResourcesFragment.class);
        } else if (iDrawerItem.getIdentifier() == 5) {
        intent = new Intent(MainActivity.this, StatisticsFragment.class);
        }
        if (intent != null) {
        MainActivity.this.startActivity(intent);
        }
        }
        return false;
        }
        })
         */


        stockyItems.add(new PrimaryDrawerItem()
                .withName("Preferences")
                .withIcon(R.drawable.ic_settings_black_24dp).withIdentifier(-1)
                /*.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(@Nullable View view, int i, @NotNull IDrawerItem<?> iDrawerItem) {
                        if(iDrawerItem !=null){
                            Intent intent = null;
                            if (iDrawerItem.getIdentifier() == -1) {
                                intent = new Intent(MainActivity.this, AccountActivity.class);
                            }
                            if (intent != null) {
                                MainActivity.this.startActivity(intent);
                            }
                        }
                        return false;
                    }
                }
            )
            */);


        stockyItems.add(switchDrawerItem);




        // navigation menu header
        AccountHeader header = new AccountHeaderBuilder().withActivity(this)
                .addProfiles(new ProfileDrawerItem()
                        .withEmail("ayoub.berd@gmail.com")
                        .withName("Ayoub's Legacy")
                        .withIcon(R.mipmap.ic_launcher_round))
                .withSavedInstance(savedInstanceState)
                .withHeaderBackground(R.drawable.ic_background)
                .withSelectionListEnabledForSingleProfile(true) // we need just one profile
                // TODO  : Add the profile for each login.... 18/10/2020
                .build();

        // Navigation drawer
        result = new DrawerBuilder()
                .withActivity(this) // activity main
                .withToolbar(toolbar) // toolbar
                .withSavedInstance(savedInstanceState) // saveInstance of activity
                .withDrawerItems(iDrawerItems) // menu items
                .withTranslucentNavigationBar(true)
                .withStickyDrawerItems(stockyItems) // footer items
                .withAccountHeader(header) // header of navigation
                .withOnDrawerItemClickListener(this) // listener for menu items click
                .build();

    }//end SetupNavigation



    private void loadNotes() {
        this.notes = new ArrayList<>();
        List<Note> list = dao.getNotes();// get All notes from DataBase
        this.notes.addAll(list);
        this.adapter = new NotesAdapter(this, this.notes);
        // set listener to adapter
        this.adapter.setListener(this);
        this.recyclerView.setAdapter(adapter);
        showEmptyView();
        // add swipe helper to recyclerView

        swipeToDeleteHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * when no notes show msg in main_layout
     */
    private void showEmptyView() {
        if (notes.size() == 0) {
            this.recyclerView.setVisibility(View.GONE);
            findViewById(R.id.empty_notes_view).setVisibility(View.VISIBLE);

        } else {
            this.recyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.empty_notes_view).setVisibility(View.GONE);
        }
    }

    /**
     * Start EditNoteActivity.class for Create New Note
     */
    private void onAddNewNote() {
        startActivity(new Intent(this, EditNoteActivity.class));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        onNavigationItemSelected(item);
        /*int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.nav_profile) {

            startActivity(new Intent(MainActivity.this, AccountActivity.class));

        }*/
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
        //
        boolean resumingFromNotification = getIntent().getBooleanExtra(QUERY_MOOD_PARAMETER, false);
        if (resumingFromNotification) {
            showCurrentMoodDialog();
        } else {
            // Show notification if opening the app
            showNotification();
        }
    }
        //


    @Override
    public void onNoteClick(Note note) {
        // TODO: 18/10/2020  note clicked : edit note
        Intent edit = new Intent(this, EditNoteActivity.class);
        edit.putExtra(NOTE_EXTRA_Key, note.getId());
        startActivity(edit);

    }

    @Override
    public void onNoteLongClick(Note note) {
        // TODO: 18/10/2020 note long clicked : delete , share ..
        note.setChecked(true);
        checkedCount = 1;
        adapter.setMultiCheckMode(true);

        // set new listener to adapter intend off MainActivity listener that we have implement
        adapter.setListener(new NoteEventListener() {
            @Override
            public void onNoteClick(Note note) {
                note.setChecked(!note.isChecked()); // inverse selected
                if (note.isChecked())
                    checkedCount++;
                else checkedCount--;

                if (checkedCount > 1) {
                    actionModeCallback.changeShareItemVisible(false);
                } else actionModeCallback.changeShareItemVisible(true);

                if (checkedCount == 0) {
                    //  finish multi select mode wen checked count =0
                    actionModeCallback.getAction().finish();
                }

                actionModeCallback.setCount(checkedCount + "/" + notes.size());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNoteLongClick(Note note) {

            }
        });

        actionModeCallback = new MainActionModeCallback() {
            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_delete_notes)
                    onDeleteMultiNotes();
                else if (menuItem.getItemId() == R.id.action_share_note)
                    onShareNote();

                actionMode.finish();
                return false;
            }

        };

        // start action mode
        startActionMode(actionModeCallback);
        // hide fab button
        fab.setVisibility(View.GONE);
        actionModeCallback.setCount(checkedCount + "/" + notes.size());
    }

    private void onShareNote() {
        // TODO: 22/10/2020  we need share just one Note not multi

        Note note = adapter.getCheckedNotes().get(0);
        // TODO: 22/10/2020 do your logic here to share note ; on social or something else
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        String notetext = note.getNoteText() + "\n\n Create on : " +
                NoteUtils.dateFromLong(note.getNoteDate()) + "\n  By :" +
                getString(R.string.app_name);
        share.putExtra(Intent.EXTRA_TEXT, notetext);
        startActivity(share);


    }

    private void onDeleteMultiNotes() {
        // TODO: 22/10/2020 delete multi notes

        List<Note> chackedNotes = adapter.getCheckedNotes();
        if (chackedNotes.size() != 0) {
            for (Note note : chackedNotes) {
                dao.deleteNote(note);
            }
            // refresh Notes
            loadNotes();
            Toast.makeText(this, chackedNotes.size() + " Note(s) Delete successfully !", Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, "No Note(s) selected", Toast.LENGTH_SHORT).show();

        //adapter.setMultiCheckMode(false);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);

        adapter.setMultiCheckMode(false); // uncheck the notes
        adapter.setListener(this); // set back the old listener
        fab.setVisibility(View.VISIBLE);
    }

    // swipe to right or to left te delete
    private ItemTouchHelper swipeToDeleteHelper = new ItemTouchHelper(
            new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    // TODO:  delete note when swipe

                    if (notes != null) {
                        // get swiped note
                        Note swipedNote = notes.get(viewHolder.getAdapterPosition());
                        if (swipedNote != null) {
                            swipeToDelete(swipedNote, viewHolder);

                        }

                    }
                }
            });

    private void swipeToDelete(final Note swipedNote, final RecyclerView.ViewHolder viewHolder) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("Delete Note?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // TODO: 25/10/2020 delete note
                        dao.deleteNote(swipedNote);
                        notes.remove(swipedNote);
                        adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                        showEmptyView();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // TODO: 25/10/2020  Undo swipe and restore swipedNote
                        recyclerView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());


                    }
                })
                .setCancelable(false)
                .create().show();

    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        // TODO: 25/10/2020  Edite onItemClick to change from activity to another and fragment to fragment....
        Toast.makeText(this, "" + position, Toast.LENGTH_SHORT).show();
        return false;
    }


    //
    void saveMoodToDatabase() {
        /**
         * Saves the current mood state to the SQLite database.
         */
        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        MoodsDao moodDao = database.moodsDao();

        // Get the last entered date
        List<Mood> allMoods = moodDao.getAll();
        int numberOfMoods = allMoods.size();
        long lastEnteredDate = 0;
        if (numberOfMoods > 0) {
            lastEnteredDate = allMoods.get(numberOfMoods - 1).mooddate;
        }

        // Create the new Mood
        Mood currentMood = new Mood(lastEnteredDate + 1, mCurrentMood, mCurrentMoodIntensity);
        moodDao.insert(currentMood);

        // Ask the user (again)
        showNotification();
    }

    void showNotification() {
        /**
         * Sets the alarm to display a notification in the notification bar asking the user to hit
         * the notification so that they get prompted to enter their mood. The notification is
         * shown 3 seconds after requested for demo purposes.
         * TODO: Remove.
         */
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmMgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        if (alarmMgr != null) {
            alarmMgr.set(
                    AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + 30 * 1000, // 1000ms = 1s
                    pendingIntent
            );
        }
    }
    //

    //
}