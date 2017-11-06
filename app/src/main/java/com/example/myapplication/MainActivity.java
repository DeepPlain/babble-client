package com.example.myapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    ArrayList<Room> dataArr;
    private CharSequence mTitle;
    private MyAdapter mAdapter;
    String ip = "121.152.18.73";
    String port = "8888";
    Socket socket;
    static Socket sock;
    static DataOutputStream out;
    static DataInputStream in;
    static String nick;
    private boolean mPressFirstBackKey = false;
    private Timer timer;
    Button plusBtn;
    SearchView searchView;
    MenuItem menuItem;
    TextView apptitle;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        plusBtn = (Button) findViewById(R.id.plusBtn);
        plusBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, RoomMake.class);
                startActivity(myIntent);
            }
        });

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();


        actionBar = ((AppCompatActivity) this).getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2282A2")));
        View cView = getLayoutInflater().inflate(R.layout.actionbar_custom, null);

        actionBar.setCustomView(cView);
        apptitle = (TextView) cView.findViewById(R.id.apptitle);
        apptitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(apptitle.getText().toString().equals("왁자지껄")) {
                    new HttpTask().execute("전체", "empty");
                }
                else {
                    new HttpTask().execute(apptitle.getText().toString(), "empty");
                }
            }
        });



        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    public void onBackPressed() {
        if(mPressFirstBackKey == false) {
            Toast.makeText(MainActivity.this, "\'뒤로\' 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
            mPressFirstBackKey = true;
            TimerTask second = new TimerTask() {
                @Override
                public void run() {
                    timer.cancel();
                    timer = null;
                    mPressFirstBackKey = false;
                }
            };
            if(timer != null) {
                timer.cancel();
                timer = null;
            }
            timer = new Timer();
            timer.schedule(second, 2000);
        } else
            android.os.Process.killProcess(android.os.Process.myPid());
    }
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {

        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                new HttpTask().execute("전체", "empty");
                apptitle.setText("왁자지껄");
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                new HttpTask().execute("스포츠", "empty");
                apptitle.setText("스포츠");
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                new HttpTask().execute("TV톡", "empty");
                apptitle.setText("TV톡");
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                new HttpTask().execute("문화", "empty");
                apptitle.setText("문화");
                break;
            case 5:
                mTitle = "뷰티 & 스타일";
                new HttpTask().execute("뷰티 & 스타일", "empty");
                apptitle.setText("뷰티 & 스타일");
                break;
            case 6:
                mTitle = "취업 & 직장";
                new HttpTask().execute("취업 & 직장", "empty");
                apptitle.setText("취업 & 직장");
                break;
            case 7:
                mTitle = getString(R.string.title_section7);
                new HttpTask().execute("연애", "empty");
                apptitle.setText("연애");
                break;
            case 8:
                mTitle = getString(R.string.title_section8);
                new HttpTask().execute("운동", "empty");
                apptitle.setText("운동");
                break;
            case 9:
                mTitle = getString(R.string.title_section9);
                new HttpTask().execute("여행", "empty");
                apptitle.setText("여행");
                break;
            case 10:
                mTitle = getString(R.string.title_section10);
                new HttpTask().execute("일상", "empty");
                apptitle.setText("일상");
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            menuItem = menu.findItem(R.id.menu_search);
            searchView = (SearchView) menuItem.getActionView();
            int searchIconId = searchView.getContext().getResources().
                    getIdentifier("android:id/search_button", null, null);
            ImageView searchIcon = (ImageView) searchView.findViewById(searchIconId);
            searchIcon.setImageResource(R.drawable.search);

            int magId = getResources().getIdentifier("android:id/search_close_btn", null, null);
            ImageView magImage = (ImageView) searchView.findViewById(magId);
            magImage.setImageResource(R.drawable.close);
            searchView.setQueryHint("방 제목을 입력하세요");
            searchView.setOnQueryTextListener(queryTextListener);

            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            if(searchManager != null) {
                searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            }

            searchView.setIconifiedByDefault(true);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        @Override
        public boolean onQueryTextSubmit(String query) {
            int length = query.length();
            searchView.onActionViewCollapsed();
            searchView.setQuery("", false);
            apptitle.setText("왁자지껄");
            mNavigationDrawerFragment.mDrawerListView.setItemChecked(0,true);

            new HttpTask().execute(query, String.valueOf(length));


            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    class HttpTask extends AsyncTask<String, Void, String> {
        boolean threadAlive = true;
        DataOutputStream output = null;
        DataInputStream input = null;

        @Override
        protected String doInBackground(String... strings) {
            // TODO Auto-generated method stub
            try {
                sock = socket = new Socket(ip, Integer.parseInt(port));
                in = input = new DataInputStream(socket.getInputStream());
                out = output = new DataOutputStream(socket.getOutputStream());

                dataArr = new ArrayList<Room>();
                StringTokenizer token = new StringTokenizer(input.readUTF(), "%^");
                int no;
                String title;
                String category;
                String number;
                String totalNumber;

                while(token.hasMoreTokens()) {
                    no = Integer.parseInt(token.nextToken());
                    title = token.nextToken();
                    category = token.nextToken();
                    number = token.nextToken();
                    totalNumber = token.nextToken();
                    Log.d(ACTIVITY_SERVICE, strings[0]);
                    if(strings[0].equals("전체")) {
                        dataArr.add(new Room(no, title, category, number, totalNumber));
                    }
                    else if(strings[0].equals(category) && strings[1].equals("empty")) {
                        dataArr.add(new Room(no, title, category, number, totalNumber));
                    }
                    else if(!strings[1].equals("empty")) { // 검색
                        int length = Integer.parseInt(strings[1]);
                        int lastLength = title.length();
                        for(int i=0; length+i <= lastLength; i++)
                            if(strings[0].equals(title.substring(i,length+i))) {
                                dataArr.add(new Room(no, title, category, number, totalNumber));
                                break;
                            }
                    }
                }
                if(!strings[1].equals("empty")) {
                    return "search";
                }


                //결과창뿌려주기 - ui 변경시 에러
                return null;

            } catch (Exception e) {
                e.printStackTrace();
            }
            //오류시 null 반환
            return null;
        }
        //asyonTask 3번째 인자와 일치 매개변수값 -> doInBackground 리턴값이 전달됨
        //AsynoTask 는 preExcute - doInBackground - postExecute 순으로 자동으로 실행됩니다.
        //ui는 여기서 변경
        protected void onPostExecute(String value){
            super.onPostExecute(value);
            ListView roomList;
            roomList = (ListView) findViewById(R.id.roomList);
            mAdapter = new MyAdapter(MainActivity.this, R.layout.list_room, dataArr);
            roomList.setAdapter(mAdapter);
            if(value != null && dataArr.isEmpty())
                Toast.makeText(MainActivity.this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
            Log.d(ACTIVITY_SERVICE, "list");
        }

    }
    class Room {
        int no;
        String title;
        String category;
        String number;
        String totalNumber;

        public Room(int no, String title, String category, String number, String totalNumber) {
            this.no = no;
            this.title = title;
            this.category = category;
            this.number = number;
            this.totalNumber = totalNumber;
        }
    }
    class MyAdapter extends BaseAdapter {
        Context context;
        int layoutId;
        ArrayList<Room> myDataArr;
        LayoutInflater Inflater;
        MyAdapter(Context _context, int _layoutId, ArrayList<Room> _myDataArr){
            context = _context;
            layoutId = _layoutId;
            myDataArr = _myDataArr;

            Inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return myDataArr != null? myDataArr.size() : 0;
        }

        @Override
        public String getItem(int position) {
            return String.valueOf(myDataArr.get(position).no);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final int pos = position;
            // 1번 구역

            convertView = Inflater.inflate(R.layout.list_room, parent, false);
            Button title = (Button)convertView.findViewById(R.id.title);
            title.setText(myDataArr.get(position).title);
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String no = String.valueOf(myDataArr.get(position).no);
                    String title = myDataArr.get(position).title;
                    String category = myDataArr.get(position).category;
                    Intent myIntent = new Intent(MainActivity.this, RoomIn.class);
                    myIntent.putExtra("no", no);
                    myIntent.putExtra("title", title);
                    myIntent.putExtra("category", category);
                    startActivity(myIntent);
                }
            });
            TextView number = (TextView)convertView.findViewById(R.id.number);
            number.setText(myDataArr.get(position).number);
            TextView totalNumber = (TextView)convertView.findViewById(R.id.totalNumber);
            totalNumber.setText(myDataArr.get(position).totalNumber);
            TextView textView_category = (TextView)convertView.findViewById(R.id.textView_category);
            textView_category.setText(myDataArr.get(position).category);

            return convertView;
        }
    }


}
