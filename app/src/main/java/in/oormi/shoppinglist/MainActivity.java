package in.oormi.shoppinglist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArrayList<GroupInfo> allTaskList = new ArrayList<GroupInfo>();

    private CustomAdapter listAdapter;
    private ExpandableListView expList;
    public DatabaseHandler db = new DatabaseHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                       // .setAction("Action", null).show();
                editGroupDialog(-1);
            }
        });

        if (!loadDb()) initData();
        calcEstimate();
        Toast.makeText(this, getString(R.string.longpress), Toast.LENGTH_LONG).show();

        expList = (ExpandableListView) findViewById(R.id.expList);
        listAdapter = new CustomAdapter(MainActivity.this, allTaskList);
        expList.setAdapter(listAdapter);

        final SwipeDetector swipeDetector = new SwipeDetector();
        expList.setOnTouchListener(swipeDetector);

        expList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (swipeDetector.swipeDetected()){
                    //let onclicklistener handle it
                } else {
                    int itemType = ExpandableListView.getPackedPositionType(id);

                    if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                        //int childPosition = ExpandableListView.getPackedPositionChild(id);
                        int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                        //editChildDialog(groupPosition, childPosition);
                        editGroupDialog(groupPosition);
                        return true; //true if we consumed the click, false if not

                    } else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                        int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                        editGroupDialog(groupPosition);
                        return true; //true if we consumed the click, false if not

                    } else {
                        // null item; we don't consume the click
                        return false;
                    }
                }
                return false;
            }
        });

        expList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (swipeDetector.swipeDetected()){
                    allTaskList.get(groupPosition).setEnabled(!allTaskList.get(groupPosition).getEnabled());
                    expList.collapseGroup(groupPosition);
                    listAdapter.notifyDataSetChanged();
                    db.updateTask(allTaskList.get(groupPosition));
                    calcEstimate();
                    return true;
                } else {
                    return false;
                }
                //return true;
            }
        });


    }

    private void calcEstimate() {
        float tcost = 0.0f;
        for (int ntask = 0; ntask < allTaskList.size(); ntask++) {
            if (allTaskList.get(ntask).getEnabled()) {
                String scost = allTaskList.get(ntask).getDetailsList().get(0).getCost();
                if (scost.isEmpty()) scost = "0";
                if (scost.equals(".")) scost = "0";
                float cost = Float.parseFloat(scost);
                tcost += cost;
            }
        }

        TextView est = (TextView) findViewById(R.id.textViewCarry);
        est.setText(String.format("Estimated Cost : %.2f", tcost));
    }

    private void initData() {

        allTaskList.clear();

        String[] defaultTasks = getResources().getStringArray(R.array.defaultTasksStringArray);
        //String[] defTimes = getResources().getStringArray(R.array.defaultTimeArray);

        int[] idArray = {R.array.defaultDetStringArray0, R.array.defaultDetStringArray1,
                R.array.defaultDetStringArray2, R.array.defaultDetStringArray3,
                R.array.defaultDetStringArray4, R.array.defaultDetStringArray5,
                R.array.defaultDetStringArray6, R.array.defaultDetStringArray7,
                R.array.defaultDetStringArray8, R.array.defaultDetStringArray9};

        ArrayList<String[]> defDetailsAll = new ArrayList<>();
        for (int id = 0; id < idArray.length; id++) {
            defDetailsAll.add(getResources().getStringArray(idArray[id]));
        }

        for (int ntask = 0; ntask < defaultTasks.length; ntask++) {
            if (ntask < defDetailsAll.size()) {
                addTasktoExpList(defaultTasks[ntask], defDetailsAll.get(ntask), ntask);
            }
        }

        try {
            db.resetDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (int ntask = 0; ntask < allTaskList.size(); ntask++) {
            db.addData(allTaskList.get(ntask));
        }
        Toast.makeText(this, getString(R.string.initmsg), Toast.LENGTH_SHORT).show();
    }

    private boolean loadDb() {
        int tcount = db.getTaskCount();
        if (tcount < 1) return false;
        List<GroupInfo> allTasks = db.getAllTasks();
        allTaskList.addAll(allTasks);
        //for(GroupInfo task: allTaskList) detailsMap.put(task.getTask(), task);

        //Toast.makeText(this, getString(R.string.dbmsg), Toast.LENGTH_SHORT).show();

        return true;
    }

    //private void addTasktoExpList(String taskName, String taskDetail, String delay, int at1, int at2){
    private void addTasktoExpList(String taskName, String[] taskDetail, int at1) {
        GroupInfo task = new GroupInfo();
        task.setTask(taskName, at1);
        allTaskList.add(at1, task);

        ArrayList<ChildInfo> detailsList = task.getDetailsList();
        detailsList.clear();
        ChildInfo detailInfo = new ChildInfo();
        detailInfo.setQuantity(taskDetail[0]);
        detailInfo.setCost(taskDetail[1]);
        detailInfo.setNotes(taskDetail[2]);
        detailsList.add(detailInfo);
        task.setDetailsList(detailsList);
    }

    private void editGroupDialog(final int groupPos) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(params);

        layout.setGravity(Gravity.CLIP_VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setBackgroundColor(ContextCompat.getColor(getBaseContext(),
                R.color.colorDialogLayout));

        final TextView tv = new TextView(this);
        tv.setText(getString(R.string.editGroupDialogTitle));
        tv.setPadding(40, 40, 40, 40);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);

        //final CheckBox chNew = new CheckBox(this);
        //chNew.setText(R.string.checkboxAdd);
        //layout.addView(chNew);

        final EditText etTaskName = new EditText(this);
        etTaskName.setHint(R.string.hintTaskName);
        if(groupPos>=0) etTaskName.setText(allTaskList.get(groupPos).getTask());
        layout.addView(etTaskName);

        final EditText etQty = new EditText(this);
        etQty.setHint(getString(R.string.hintQty));
        etQty.setInputType(InputType.TYPE_CLASS_NUMBER);
        if(groupPos>=0) etQty.setText(allTaskList.get(groupPos).getDetailsList().get(0).getQuantity());
        layout.addView(etQty);

        final EditText etCost = new EditText(this);
        etCost.setHint(R.string.hintCost);
        etCost.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
        if(groupPos>=0) etCost.setText(allTaskList.get(groupPos).getDetailsList().get(0).getCost());
        layout.addView(etCost);

        final EditText etNotes = new EditText(this);
        etNotes.setHint(R.string.hintNotes);
        if(groupPos>=0) etNotes.setText(allTaskList.get(groupPos).getDetailsList().get(0).getNotes());
        layout.addView(etNotes);

/*
        chNew.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etTaskName.setText(null);
                } else {
                    etTaskName.setText(allTaskList.get(groupPos).getTask());
                }
            }
        });
*/

        alertDialogBuilder.setView(layout);
        alertDialogBuilder.setCustomTitle(tv);

        alertDialogBuilder.setNegativeButton(R.string.editCancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        alertDialogBuilder.setPositiveButton(R.string.editOk, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                String etStr1 = etTaskName.getText().toString();
                String etStr2 = etQty.getText().toString();
                String etStr3 = etCost.getText().toString();
                String etStr4 = etNotes.getText().toString();
                boolean newStepAdded = false;
                //String oldName = allTaskList.get(groupPos).getTask();

                //if (!checkTime(etStr3)) etStr3 = "00:00";
                //if (etStr2.length() < 1) etStr2 = "1";

                if (etStr1.length() > 0) {
                    if (groupPos<0) {
                        int gPos = allTaskList.size();
                        String [] str = {etStr2, etStr3, etStr4};
                        addTasktoExpList(etStr1,str, gPos);
                        db.insertData(gPos, allTaskList.get(gPos));
                    } else {
                        //if (allTaskList.get(groupPos).getTask().equals(etStr1)) {//details changed
                            //addTasktoExpList(etStr1, etStr2, etStr3, groupPos, -1);
                            allTaskList.get(groupPos).getDetailsList().get(0).setQuantity(etStr2);
                            allTaskList.get(groupPos).getDetailsList().get(0).setCost(etStr3);
                            allTaskList.get(groupPos).getDetailsList().get(0).setNotes(etStr4);
                            db.updateStep(allTaskList.get(groupPos), 0);
                       // } else {//only name changed
                            //detailsMap.put(etStr1,
                            //detailsMap.remove(allTaskList.get(groupPos).getTask()));
                            allTaskList.get(groupPos).setTask(etStr1, groupPos);
                            db.updateTask(allTaskList.get(groupPos));
                        //}
                        listAdapter.notifyDataSetChanged();
                    }

                    listAdapter.notifyDataSetChanged();
                    calcEstimate();
                }
                newStepAdded = true;
                if (newStepAdded) setStatus(groupPos, -1);
            }
        });

        alertDialogBuilder.setNeutralButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //detailsMap.remove(allTaskList.get(groupPos).getTask());
                db.deleteTask(groupPos, allTaskList.get(groupPos));
                allTaskList.remove(groupPos);
                for (int s = 0; s < allTaskList.size(); s++) {
                    allTaskList.get(s).setTaskId(s);
                }

                if (allTaskList.size() < 1) {
                    //addTasktoExpList("No Name", "No Action", "00:00", 0, -1);
                    db.insertData(0, allTaskList.get(0));
                }
                listAdapter.notifyDataSetChanged();
                calcEstimate();
            }
        });

        AlertDialog edTaskDialog = alertDialogBuilder.create();
        edTaskDialog.getWindow().setBackgroundDrawableResource(R.color.colorDialog);
        try {
            edTaskDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setStatus(int gp, int cp) {
        if (gp<0) gp = allTaskList.size()-1;
        for (ChildInfo ch : allTaskList.get(gp).getDetailsList()) {
            ch.hasError = false;
            ch.isNew = false;
            //if (ch.getDelay().equals("00:00")) ch.hasError = true;
            //if (ch.getDescription().equals("No Action")) ch.hasError = true;
        }

        if (cp < 0) cp = allTaskList.get(gp).getDetailsList().size() - 1;
        allTaskList.get(gp).getDetailsList().get(cp).isNew = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_info) {
            Intent i = new Intent(this, ResourceShow.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.action_share) {
            return true;
        }

        if (id == R.id.action_reset) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            initData();
                            listAdapter.notifyDataSetChanged();
                            calcEstimate();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Confirm Data Reset");
            builder.setMessage("All changes will be lost. Are you sure?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ShareActionProvider mShareActionProvider;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider)  MenuItemCompat.getActionProvider(item);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=in.oormi.shoppinglist");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Check out this app!");
        setShareIntent(shareIntent);
        return true;
    }

    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

}
