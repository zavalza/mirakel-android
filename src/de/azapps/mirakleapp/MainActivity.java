package de.azapps.mirakleapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.gson.Gson;

public class MainActivity extends Activity {

	protected SQLiteDatabase db;// SQLite Database

	private String Email; 
	private String password;
	private String Server_url;
	private int list_id;// -1=all_lists, 0..n list_id
	private LinearLayout task_list;
	private LinearLayout lists;
	private ArrayList<Task> shown_tasks;
	private ArrayList<List_json> shown_lists;
	
	
	private EditText input ;
	private NumberPicker picker;
	private MainActivity main;
	
	//EventListeners
	final OnClickListener cellTouch = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			list_id=(Integer) v.getTag();
			show_tasks();
		}
	}; 
	final OnCheckedChangeListener check_changer =new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			String update="Update tasks set done='"+isChecked+"' where id='"+buttonView.getTag()+"';";
            db.execSQL(update);
            show_tasks();
		}
	};
	
	final OnClickListener prio_popup =new OnClickListener() {
		@Override
		public void onClick(View v) {
			picker=new NumberPicker(main);
			picker.setMaxValue(4);
			picker.setMinValue(0);
			String[] t={"-2","-1","0","1","2"};
			picker.setDisplayedValues(t);
			picker.setWrapSelectorWheel(false);
			picker.setValue(Integer.parseInt(((TextView)v).getText().toString())+2);
			final int id=(Integer)v.getTag();
			new AlertDialog.Builder(main)
		    .setTitle("Change Priority")
		    .setMessage("New Task-Priority")
		    .setView(picker)
		    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            String update="Update tasks set priority='"+(picker.getValue()-2)+"' where id='"+id+"';";
		            db.execSQL(update);
		            show_tasks();
		        }
		    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            // Do nothing.
		        }
		    }).show();
			
		}
	};
	
	final OnLongClickListener change_name = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			input=new EditText(main);
			input.setText(((TextView)((RelativeLayout)v).getChildAt(0)).getText());
			input.setTag(main);
			final int id=(Integer)v.getTag();
			new AlertDialog.Builder(main)
		    .setTitle("Change Title")
		    .setMessage("New List-Title")
		    .setView(input)
		    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            String value = input.getText().toString();
		            String update="Update lists set name='"+value+"' where id='"+id+"';";
		            db.execSQL(update);
		            show_lists();
		        }
		    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            // Do nothing.
		        }
		    }).show();
			return false;
		}
	}; 
	final OnClickListener change_name_task = new OnClickListener() {
		@Override
		public void onClick(View v) {
			input=new EditText(main);
			input.setText(((TextView)v).getText());
			final int id=(Integer)v.getTag();
			new AlertDialog.Builder(main)
		    .setTitle("Change Name")
		    .setMessage("New Task-Name")
		    .setView(input)
		    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            String value = input.getText().toString();
		            String update="Update tasks set name='"+value+"' where id='"+id+"';";
		            db.execSQL(update);
		            show_tasks();
		        }
		    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
		            // Do nothing.
		        }
		    }).show();
		}
	};
	
	final OnTouchListener drag_drop=new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v.getLayoutParams();
			
            switch(event.getAction())
            {
               case MotionEvent.ACTION_MOVE:
               {
            	   int marg_left=((int)event.getRawX() - (v.getWidth()/2))>0?(int)event.getRawX() - (v.getWidth()/2):0;
            	   if(v.getWidth()<marg_left){
              		 String delete="Delete from tasks where id='"+v.getTag()+"';";
              		 db.execSQL(delete);
              		 show_tasks();
              		 return false;
              	 }else{
                 	params.leftMargin = marg_left;
                 	v.setLayoutParams(params);
              	 }
                 break;
               }
               case MotionEvent.ACTION_CANCEL:
               case MotionEvent.ACTION_UP:
               {
            	 if(v.getWidth()<v.getLeft()){
            		 String delete="Delete from tasks where id='"+v.getTag()+"';";
            		 db.execSQL(delete);
            		 show_tasks();
            		 return false;
            	 }else{
            		params.topMargin = 0;
                 	params.leftMargin = 0;
                 	v.setLayoutParams(params);
                 	break;
            	 }
               }
               case MotionEvent.ACTION_DOWN:
               {
                v.setLayoutParams(params);
                break;
               }
            }
			return true;
		}
	}; 


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		main=this;
		setContentView(R.layout.activity_main);
		((TextView)findViewById(R.id.all_lists)).setOnClickListener(cellTouch);
		((TextView)findViewById(R.id.all_lists)).setTag(-1);
		Log.e("Main", "create");
		Bundle data = getIntent().getExtras();
		Email = data.getString("email");
		Server_url = data.getString("server");
		password=data.getString("password");
		db = openOrCreateDatabase("main.db",
				SQLiteDatabase.CREATE_IF_NECESSARY, null);
		final String create_tables = "CREATE TABLE IF NOT EXISTS lists("
				+ "id integer not null primary key," + "name string(255),"
				+ "user_id interger," + "created_at datetime not null,"
				+ "updated_at datetime not null," + "parent_id  integer,"
				+ "lft integer," + "rgt integer);";
		db.execSQL(create_tables);
		final String create_tasks = "CREATE TABLE IF NOT EXISTS tasks("
				+ "id integer not null primary key," + "name string(255),"
				+ "content string(255)," + "done boolean default(0),"
				+ "due date," + "list_id integer,"
				+ "created_at datetime not null,"
				+ "updated_at datetime not null," + "parent_id integer,"
				+ "lft integer," + "rgt integer,"
				+ "priority integer default(0));";
		db.execSQL(create_tasks);
		String stringUrl = Server_url + "/lists.json";
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		Log.e("Main", "created");
		list_id = -1;
		if (networkInfo != null && networkInfo.isConnected()) {
			new get_data().execute(stringUrl);
		} else {
			Log.e("NetworkState", "No network connection available.");
		}
		shown_tasks = new ArrayList<Task>();
		shown_lists=new ArrayList<List_json>();
		task_list = (LinearLayout) findViewById(R.id.task_list);
		lists=(LinearLayout)findViewById(R.id.lists);
		//TODO add do static xml

		show_lists();
		show_tasks();
	}

	protected void show_lists() {
		Log.e("DEBUG","Show lists "+((LinearLayout)lists).getChildCount());
		int list_count;
		if((list_count=((LinearLayout)lists).getChildCount()) > 1) {
			for(int i=1;i<list_count;i++){
				((LinearLayout) lists).removeViewAt(1);
			}
		}
		
		String select = "Select * from lists";
		Cursor c = db.rawQuery(select, null);
		c.moveToFirst();
		shown_lists.clear();
		for (int i = 0; i < c.getCount(); i++) {
			/*
			 * 0 id 1 name 2 user 3 created_at 4 updated_at 5 parent_id 6 lf 7 rgt 
			 */
			int[] child={0};
			List_json t = new List_json(c.getInt(0), c.getString(1), c.getInt(2), c.getString(3), c.getString(4), child);
			shown_lists.add(t);
			shown_lists.get(i).show(this, lists,cellTouch,change_name);
			c.moveToNext();
		}
		
	}

	protected void show_tasks() {
		// TODO implement Done/undone list
		if(((LinearLayout) task_list).getChildCount() > 0) 
		    ((LinearLayout) task_list).removeAllViews(); 
		String select = "Select * from tasks";
		if (list_id != -1)
			select += " where list_id='" + list_id + "';";
		Cursor c = db.rawQuery(select, null);
		c.moveToFirst();
		shown_tasks.clear();
		for (int i = 0; i < c.getCount(); i++) {
			/*
			 * 0 id 1 name 2 content 3 done 4 due 5 list_id 6 create_at 7
			 * updated_at 8 parent_id 9 lft 10 rgt 11 priority
			 */
			boolean done = (c.getString(3).toLowerCase().equals("true"));
			Task t = new Task(c.getString(2), c.getString(6), c.getString(4),
					c.getString(1), c.getString(7), c.getInt(0), c.getInt(5),
					c.getInt(11), done);
			shown_tasks.add(t);
			shown_tasks.get(i).show(this, task_list,check_changer,prio_popup,change_name_task,drag_drop);
			c.moveToNext();
		}
	}

	public class get_data extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... urls) {

			// params comes from the execute() call: params[0] is the url.
			try {
				return downloadUrl(urls[0]);
			} catch (IOException e) {
				return "Unable to retrieve web page. URL may be invalid.";
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "";
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			Gson gson = new Gson();
			Log.e("Result", result);
			if (result.indexOf("user_id") != -1) {
				set_lists(gson.fromJson(result, List_json[].class));
				Log.d("Debug", "lists");
			} else if (result.indexOf("list_id") != -1) {
				set_tasks(gson.fromJson(result, Task[].class));
				Log.d("Debug", "tasks");
			}

			// textView.setText(result);
		}

		private String downloadUrl(String myurl) throws IOException,URISyntaxException {
			// Setup Connection
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			// Log.e("Url", myurl);
			request.setURI(new URI(myurl));
			// Log.e("Mail", Email);
			// Log.e("Pwd", password);
			client.getCredentialsProvider().setCredentials(
					new AuthScope(null, -1),
					new UsernamePasswordCredentials(Email, password));
			// Get Data
			HttpResponse response = client.execute(request);
			int status = response.getStatusLine().getStatusCode();
			Log.d("HTTP-Status", status + "");

			// Convert the InputStream into a string
			String data = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer("");
			String l = "";
			String nl = System.getProperty("line.separator");
			while ((l = in.readLine()) != null) {
				sb.append(l + nl);
			}
			in.close();
			data = sb.toString();
			return data;

		}

	}

	private void set_lists(List_json[] fromJson) {
		String select = "Select * from lists";
		Cursor c=db.rawQuery(select, null);
		c.moveToFirst();
		int lists_in_db=c.getCount();
		select+=" where id='";
		int list_server=0;
		for (int i = 0; i < fromJson.length; i++) {
			c = db.rawQuery(select + fromJson[i].id + "';", null);
			c.moveToFirst();
			if (c.getCount() == 0) {
				String insert = "Insert into lists(id,name,user_id,created_at,updated_at)values('"
						+ fromJson[i].id
						+ "','"
						+ fromJson[i].name
						+ "','"
						+ fromJson[i].user_id
						+ "','"
						+ fromJson[i].created_at
						+ "','" + fromJson[i].updated_at + "');";
				db.execSQL(insert);
				new get_data().execute(Server_url + "/lists/" + fromJson[i].id
						+ "/tasks.json");
			} else {
				list_server++;
				// TODO Merge Lists
			}
		}
		if(list_server<lists_in_db){
			//TODO search and delete lists
		}
		show_tasks();
	}

	public void set_tasks(Task[] fromJson) {
		String select = "Select * from tasks";
		Cursor c=db.rawQuery(select+" where list_id='"+fromJson[0].list_id+"';", null);
		c.moveToFirst();
		int tasks_in_db=c.getCount();
		select+=" where id='";
		int from_server=0;
		for (int i = 0; i < fromJson.length; i++) {
			c = db.rawQuery(select + fromJson[i].id + "';", null);
			c.moveToFirst();
			if (c.getCount() == 0) {
				String insert = "Insert into tasks(id,name,content,done,due,list_id,created_at,updated_at,priority)values('"
						+ fromJson[i].id + "','"
						+ fromJson[i].name + "','"
						+ fromJson[i].content + "','"
						+ fromJson[i].done + "','"
						+ fromJson[i].due + "','"
						+ fromJson[i].list_id + "','"
						+ fromJson[i].created_at + "','"
						+ fromJson[i].updated_at + "','"
						+ fromJson[i].priority + "');";
				db.execSQL(insert);
			} else {
				from_server++;
				// TODO Merge Tasks
			}
		}
		if(from_server<tasks_in_db){
			//TODO search and delete
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Log.e("Menü", "Settings");
			//TODO implement Settings
			return true;
		case R.id.menu_logout:
			Log.e("Menü", "Logout");
			logout();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	void logout() {
		String update = "Update users Set remember_me='FALSE' where email='"
				+ Email + "';";
		db.execSQL(update);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
