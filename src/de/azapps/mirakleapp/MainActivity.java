package de.azapps.mirakleapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

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
import android.provider.SyncStateContract.Constants;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class MainActivity extends Activity {

	protected SQLiteDatabase db;// SQLite Database

	private String Email;
	private String password;
	private String Server_url;
	private String task_order;
	private int list_id;// -1=all_lists, 0..n list_id
	private LinearLayout task_list;
	private LinearLayout lists;
	private ArrayList<Task> shown_tasks;
	private ArrayList<List_json> shown_lists;

	private EditText input;
	private NumberPicker picker;
	private MainActivity main;

	public class sync_state {
		final public static int NOTHING = 0;
		final public static int DELETE = -1;
		final public static int ADD = 1;
		final public static int NEED_SYNC = 2;
	}

	public class Http_Mode {
		final public static int GET = 0;
		final public static int POST = 1;
		final public static int PUT = 2;
		final public static int DELETE = 3;
	}

	// EventListeners
	final OnClickListener cellTouch = new OnClickListener() {

		@Override
		public void onClick(View v) {
			list_id = (Integer) v.getTag();
			show_tasks();
		}
	};
	final OnCheckedChangeListener check_changer = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			String update = "Update tasks set done='"
					+ isChecked
					+ "', sync_state='"
					+ check_sync(Integer.parseInt(buttonView.getTag()
							.toString())) + "' where id='"
					+ buttonView.getTag() + "';";
			db.execSQL(update);
			update_list(get_list_id(Integer.parseInt(buttonView.getTag()
					.toString())));
			show_tasks();
			sync();
		}
	};

	final OnClickListener prio_popup = new OnClickListener() {
		@Override
		public void onClick(View v) {
			picker = new NumberPicker(main);
			picker.setMaxValue(4);
			picker.setMinValue(0);
			String[] t = { "-2", "-1", "0", "1", "2" };
			picker.setDisplayedValues(t);
			picker.setWrapSelectorWheel(false);
			picker.setValue(Integer.parseInt(((TextView) v).getText()
					.toString()) + 2);
			final int id = (Integer) v.getTag();
			new AlertDialog.Builder(main)
					.setTitle("Change Priority")
					.setMessage("New Task-Priority")
					.setView(picker)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String update = "Update tasks set priority='"
											+ (picker.getValue() - 2)
											+ "', sync_state='"
											+ check_sync(id)
											+ "' where id='"
											+ id + "';";
									db.execSQL(update);

									update_list(get_list_id(id));
									show_tasks();
									sync();
								}

							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Do nothing.
								}
							}).show();

		}
	};

	final OnLongClickListener change_name = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			input = new EditText(main);
			input.setText(((TextView) ((RelativeLayout) v).getChildAt(0))
					.getText());
			input.setTag(main);
			final int id = (Integer) v.getTag();
			new AlertDialog.Builder(main)
					.setTitle("Change Title")
					.setMessage("New List-Title")
					.setView(input)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String select = "Select sync_state from lists where id='"
											+ id + "';";
									int sync = sync_state.NEED_SYNC;
									Cursor c = db.rawQuery(select, null);
									c.moveToFirst();
									if (c.getCount() > 0) {
										if (c.getInt(0) == sync_state.ADD)
											sync = sync_state.ADD;
									}
									String update = "Update lists set name='"
											+ input.getText().toString()
											+ "', sync_state='" + sync
											+ "' where id='" + id + "';";
									db.execSQL(update);
									show_lists();
									sync();
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Do nothing.
								}
							}).show();
			return false;
		}
	};
	final OnClickListener change_name_task = new OnClickListener() {
		@Override
		public void onClick(View v) {
			input = new EditText(main);
			input.setText(((TextView) v).getText());
			final int id = (Integer) v.getTag();
			new AlertDialog.Builder(main)
					.setTitle("Change Name")
					.setMessage("New Task-Name")
					.setView(input)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String update = "Update tasks set name='"
											+ input.getText().toString()
											+ "', sync_state='"
											+ check_sync(id) + "' where id='"
											+ id + "';";
									db.execSQL(update);
									update_list(get_list_id(id));
									show_tasks();
									sync();
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Do nothing.
								}
							}).show();
		}
	};

	final OnTouchListener drag_drop = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) v
					.getLayoutParams();
			params.width = v.getWidth();// set fix width
			switch (event.getAction()) {
			case MotionEvent.ACTION_MOVE: {
				int marg_left = ((int) event.getRawX() - (v.getWidth() / 2)) > 0 ? (int) event
						.getRawX() - (v.getWidth() / 2)
						: 0;
				if (v.getWidth() / 3 < marg_left) {
					String select = "Select sync_state from tasks where id='"
							+ v.getTag() + "';";
					Cursor c = db.rawQuery(select, null);
					c.moveToFirst();
					if (c.getInt(0) == sync_state.ADD) {
						String delete = "Delete from tasks where id='"
								+ v.getTag() + "';";
						db.execSQL(delete);
					} else {
						String update = "Update tasks set sync_state='"
								+ sync_state.DELETE + "' where id='"
								+ v.getTag() + "';";
						db.execSQL(update);
					}
					update_list(get_list_id(Integer.parseInt(v.getTag()
							.toString())));
					show_tasks();
					sync();
					return false;
				} else {
					params.leftMargin = marg_left;
					v.setLayoutParams(params);
				}
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP: {
				if (v.getWidth() / 3 < v.getLeft()) {
					String select = "Select sync_state from tasks where id='"
							+ v.getTag() + "';";
					Cursor c = db.rawQuery(select, null);
					c.moveToFirst();
					if (c.getInt(0) == sync_state.ADD) {
						String delete = "Delete from tasks where id='"
								+ v.getTag() + "';";
						db.execSQL(delete);
					} else {
						String update = "Update tasks set sync_state='"
								+ sync_state.DELETE + "' where id='"
								+ v.getTag() + "';";
						db.execSQL(update);
					}
					update_list(get_list_id(Integer.parseInt(v.getTag()
							.toString())));
					show_tasks();
					sync();
					return false;
				} else {
					params.topMargin = 0;
					params.leftMargin = 0;
					v.setLayoutParams(params);
					return false;
				}
			}
			case MotionEvent.ACTION_DOWN: {
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
		main = this;
		setContentView(R.layout.activity_main);
		((TextView) findViewById(R.id.all_lists)).setOnClickListener(cellTouch);
		((TextView) findViewById(R.id.all_lists)).setTag(-1);
		Log.e("Main", "create");

		// Log.e("DATETIME",strDate);
		Bundle data = getIntent().getExtras();
		Email = data.getString("email");
		Server_url = data.getString("server");
		password = data.getString("password");
		db = openOrCreateDatabase("main.db",
				SQLiteDatabase.CREATE_IF_NECESSARY, null);
		final String create_tables = "CREATE TABLE IF NOT EXISTS lists("
				+ "id integer not null primary key," + "name string(255),"
				+ "user_id interger," + "created_at datetime not null,"
				+ "updated_at datetime not null," + "parent_id  integer,"
				+ "lft integer,"
				+ "rgt integer , sync_state integer default(0));";
		db.execSQL(create_tables);
		final String create_tasks = "CREATE TABLE IF NOT EXISTS tasks("
				+ "id integer not null primary key," + "name string(255),"
				+ "content string(255)," + "done boolean default(0),"
				+ "due date," + "list_id integer,"
				+ "created_at datetime not null,"
				+ "updated_at datetime not null," + "parent_id integer,"
				+ "lft integer," + "rgt integer,"
				+ "priority integer default(0),"
				+ "sync_state integer default(0));";
		db.execSQL(create_tasks);
		sync();
		shown_tasks = new ArrayList<Task>();
		shown_lists = new ArrayList<List_json>();
		task_list = (LinearLayout) findViewById(R.id.task_list);
		lists = (LinearLayout) findViewById(R.id.lists);
		task_order = "order by id asc";
		show_lists();
		show_tasks();
	}

	protected void sync() {
		String stringUrl = Server_url + "/lists.json";
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		Log.e("Main", "created");
		list_id = -1;
		if (networkInfo != null && networkInfo.isConnected()) {
			get_data temp = new get_data(Http_Mode.GET);
			temp.execute(stringUrl);
		} else {
			Log.e("NetworkState", "No network connection available.");
		}
	}

	protected int check_sync(int id) {
		String select = "Select sync_state from tasks where id='" + id + "';";
		Cursor c = db.rawQuery(select, null);
		c.moveToFirst();
		if (c.getCount() > 0) {
			if (c.getInt(0) == sync_state.ADD)
				return sync_state.ADD;
		}
		return sync_state.NEED_SYNC;
	}

	private int get_list_id(int id) {
		String select = "Select list_id from tasks where id='" + id + "';";
		Cursor c = db.rawQuery(select, null);
		c.moveToFirst();
		return (c.getCount() > 0) ? c.getInt(0) : -1;
	}

	private void update_list(int id) {
		Log.e("Update List", id + "");
		if (id >= 0) {
			String select = "Select sync_state from lists where id='" + id
					+ "';";
			Cursor c = db.rawQuery(select, null);
			c.moveToFirst();
			if (c.getCount() > 0) {
				if (c.getInt(0) == sync_state.ADD)
					return;
			}
			String update = "Update lists set sync_state='"
					+ sync_state.NEED_SYNC + "' where id='" + id + "';";
			db.execSQL(update);
		}
	}

	protected void show_lists() {
		int list_count;
		if ((list_count = ((LinearLayout) lists).getChildCount()) > 1) {
			for (int i = 1; i < list_count; i++) {
				((LinearLayout) lists).removeViewAt(1);
			}
		}

		String select = "Select * from lists where not sync_state='"
				+ sync_state.DELETE + "';";
		Cursor c = db.rawQuery(select, null);
		c.moveToFirst();
		shown_lists.clear();
		for (int i = 0; i < c.getCount(); i++) {
			/*
			 * 0 id 1 name 2 user 3 created_at 4 updated_at 5 parent_id 6 lf 7
			 * rgt
			 */
			int[] child = { 0 };
			List_json t = new List_json(c.getInt(0), c.getString(1),
					c.getInt(2), c.getString(3), c.getString(4), child);
			shown_lists.add(t);
			shown_lists.get(i).show(this, lists, cellTouch, change_name);
			c.moveToNext();
		}
		Button new_list = new Button(this);

		new_list.setText("Add List");
		new_list.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				String insert = "Insert into lists(name,updated_at,created_at,sync_state) values('New List','"
						+ sdf.format(new Date())
						+ "','"
						+ sdf.format(new Date())
						+ "','"
						+ sync_state.ADD
						+ "');";
				db.execSQL(insert);
				// update_list(list_id);
				show_lists();
				sync();
			}
		});
		new_list.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT));
		lists.addView(new_list);

	}

	protected void show_tasks() {
		// TODO implement Done/undone list
		if (((LinearLayout) task_list).getChildCount() > 0)
			((LinearLayout) task_list).removeAllViews();

		String select = "Select * from tasks where not sync_state='"
				+ sync_state.DELETE + "'";
		if (list_id != -1)
			select += " and list_id='" + list_id + "'";
		select += " " + task_order + ";";
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
			shown_tasks.get(i).show(this, task_list, check_changer, prio_popup,
					change_name_task, drag_drop);
			c.moveToNext();
		}
		if (list_id >= 0) {
			Button new_Task = new Button(this);

			new_Task.setText("Add Task");
			new_Task.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String insert = "Insert into tasks(name,done,updated_at,created_at,priority,list_id,sync_state) values('New Task','false','"
							+ sdf.format(new Date())
							+ "','"
							+ sdf.format(new Date())
							+ "','0','"
							+ list_id
							+ "', '" + sync_state.ADD + "');";
					db.execSQL(insert);
					update_list(list_id);
					show_tasks();
					sync();
				}
			});
			new_Task.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			task_list.addView(new_Task);
		}
	}

	public class get_data extends AsyncTask<String, String, String> {
		protected int Mode;
		protected List<BasicNameValuePair> Header_data;

		public get_data(int _mode) {
			this.Mode = _mode;
		}

		public get_data(int _mode, List<BasicNameValuePair> params) {
			Mode = _mode;
			Header_data = params;
		}

		@Override
		protected String doInBackground(String... urls) {

			// params comes from the execute() call: params[0] is the url.
			try {
				return downloadUrl(urls[0]);
			} catch (IOException e) {
				return "Unable to retrieve web page. URL may be invalid.";
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			switch (Mode) {
			case Http_Mode.GET:
				Gson gson = new Gson();
				if (result.indexOf("user_id") != -1) {
					set_lists(gson.fromJson(result, List_json[].class));
				} else if (result.indexOf("list_id") != -1) {
					set_tasks(gson.fromJson(result, Task[].class));
				}
				break;
			case Http_Mode.PUT:
				break;
			case Http_Mode.POST:
				gson = new Gson();
				if (result.indexOf("user_id") != -1) {
					List_json new_list = gson.fromJson(result, List_json.class);
					String select = "Select id from lists where name='"
							+ new_list.name + "' and sync_state='"
							+ sync_state.ADD + "';";
					Cursor c = db.rawQuery(select, null);
					c.moveToFirst();
					if (c.getCount() > 0) {
						String update = "Update tasks set list_id='"
								+ new_list.id + "' where list_id='"
								+ c.getInt(0) + "';";
						db.execSQL(update);
					}
					String update = "Update lists set sync_state='"
							+ sync_state.NOTHING + "', id='" + new_list.id
							+ "',updated_ad='" + new_list.updated_at
							+ "',user_id='" + new_list.user_id
							+ "', created_at='" + new_list.created_at
							+ "' where sync_state='" + sync_state.ADD
							+ "' and name='" + new_list.name + "';";
					db.execSQL(update);
				} else if (result.indexOf("list_id") != -1) {
					Task new_task = gson.fromJson(result, Task.class);
					String update = "Update tasks set sync_state='"
							+ sync_state.NOTHING + "', id='" + new_task.id
							+ "',updated_at='" + new_task.updated_at
							+ "', created_at='" + new_task.created_at
							+ "' where sync_state='" + sync_state.ADD
							+ "' and name='" + new_task.name
							+ "' and list_id='" + new_task.list_id + "';";
					db.execSQL(update);
				}
				break;
			case Http_Mode.DELETE:
				break;
			default:
				Log.e("HTTP-MODE", "Unknown Http-Mode");
				break;
			}
		}

		private String downloadUrl(String myurl) throws IOException,
				URISyntaxException {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse response = null;
			client.getCredentialsProvider().setCredentials(
					new AuthScope(null, -1),
					new UsernamePasswordCredentials(Email, password));
			switch (Mode) {
			case Http_Mode.GET:
				HttpGet get = new HttpGet();
				get.setURI(new URI(myurl));
				response = client.execute(get);
				break;
			case Http_Mode.PUT:
				HttpPut put = new HttpPut();
				put.setURI(new URI(myurl));
				put.setEntity(new UrlEncodedFormEntity(Header_data));
				response = client.execute(put);
				break;
			case Http_Mode.POST:
				HttpPost post = new HttpPost();
				post.setURI(new URI(myurl));
				post.setEntity(new UrlEncodedFormEntity(Header_data));
				response = client.execute(post);
				break;
			case Http_Mode.DELETE:
				HttpDelete delete = new HttpDelete();
				delete.setURI(new URI(myurl));
				response = client.execute(delete);
				break;
			default:
				Log.e("HTTP-MODE", "Unknown Http-Mode");
				break;
			}
			int status = response.getStatusLine().getStatusCode();
			Log.d("HTTP-Status", status + "");

			// Convert the InputStream into a string
			String data = "";
			if (status != 204) {// 204=no content
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
			} else
				data += status;
			return data;

		}

	}

	private void set_lists(List_json[] fromJson) {
		String select = "Select sync_state,updated_at from lists where id='";
		for (int i = 0; i < fromJson.length; i++) {
			Cursor c = db.rawQuery(select + fromJson[i].id + "';", null);
			c.moveToFirst();
			if (c.getCount() == 0) {
				String insert = "Insert into lists(id,name,user_id,created_at,updated_at,sync_state)values('"
						+ fromJson[i].id
						+ "','"
						+ fromJson[i].name
						+ "','"
						+ fromJson[i].user_id
						+ "','"
						+ fromJson[i].created_at
						+ "','"
						+ fromJson[i].updated_at
						+ "','"
						+ sync_state.NOTHING + "');";
				db.execSQL(insert);
				new get_data(Http_Mode.GET).execute(Server_url + "/lists/"
						+ fromJson[i].id + "/tasks.json");
			} else {
				if (c.getInt(0) != sync_state.NEED_SYNC) {
					String update = "Update lists set name='"
							+ fromJson[i].name + "', updated_at='"
							+ fromJson[i].updated_at + "' where id='"
							+ fromJson[i].id + "';";
					db.execSQL(update);
				} else if (c.getInt(0) == sync_state.NEED_SYNC) {
					Log.e("ERROR", "Data need update server and client");
					// TODO Handle this
				}
			}
		}
		// Add
		select = "Select id,name,updated_at from lists where sync_state='"
				+ sync_state.ADD + "';";
		Cursor c = db.rawQuery(select, null);
		c.moveToFirst();
		// 0=id,1=name,2=update_at
		for (int i = 0; i < c.getCount(); i++) {
			List<BasicNameValuePair> paras = new ArrayList<BasicNameValuePair>();
			paras.add(new BasicNameValuePair("list[name]", c.getString(1)));
			new get_data(Http_Mode.POST, paras).execute(Server_url
					+ "/lists.json");
			c.moveToNext();
		}

		// Update
		select = "Select id,name,updated_at from lists where sync_state='"
				+ sync_state.NEED_SYNC + "';";
		c = db.rawQuery(select, null);
		String update = "Update lists set sync_state='" + sync_state.NOTHING
				+ "' where sync_state='" + sync_state.NEED_SYNC + "';";
		c.moveToFirst();
		// 0=id,1=name,2=update_at
		for (int i = 0; i < c.getCount(); i++) {
			List<BasicNameValuePair> paras = new ArrayList<BasicNameValuePair>();
			paras.add(new BasicNameValuePair("list[name]", c.getString(1)));
			// Sync All Tasks of list
			new get_data(Http_Mode.PUT, paras).execute(Server_url + "/lists/"
					+ c.getString(0) + ".json");
			new get_data(Http_Mode.GET).execute(Server_url + "/lists/"
					+ c.getInt(0) + "/tasks.json");
			c.moveToNext();
		}

		// Delete
		select = "Select id from lists where sync_state='" + sync_state.DELETE
				+ "';";
		c = db.rawQuery(select, null);
		c.moveToFirst();
		String delete = "Delete from lists where sync_state='"
				+ sync_state.DELETE + "';";
		for (int i = 0; i < c.getCount(); i++) {
			String del_tasks = "Delete from tasks where list_id='"
					+ c.getInt(0) + "';";
			db.execSQL(del_tasks);
			new get_data(Http_Mode.DELETE).execute(Server_url + "/lists/"
					+ c.getInt(0) + ".json");
			c.moveToNext();
		}
		db.execSQL(delete);
		db.execSQL(update);
		show_lists();
	}

	public void set_tasks(Task[] fromJson) {
		String select = "Select sync_state,updated_at from tasks where id='";
		for (int i = 0; i < fromJson.length; i++) {
			Cursor c = db.rawQuery(select + fromJson[i].id + "';", null);
			c.moveToFirst();
			if (c.getCount() == 0) {
				String insert = "Insert into tasks(id,name,content,done,due,list_id,created_at,updated_at,priority,sync_state)values('"
						+ fromJson[i].id
						+ "','"
						+ fromJson[i].name
						+ "','"
						+ fromJson[i].content
						+ "','"
						+ fromJson[i].done
						+ "','"
						+ fromJson[i].due
						+ "','"
						+ fromJson[i].list_id
						+ "','"
						+ fromJson[i].created_at
						+ "','"
						+ fromJson[i].updated_at
						+ "','"
						+ fromJson[i].priority
						+ "','" + sync_state.NOTHING + "');";
				db.execSQL(insert);
			} else {
				if (c.getInt(0) != sync_state.NEED_SYNC) {
					String update = "Update tasks set name='"
							+ fromJson[i].name + "', content='"
							+ fromJson[i].content + "', done='"
							+ fromJson[i].done + "', due='" + fromJson[i].due
							+ "', updated_at='" + fromJson[i].updated_at
							+ "', priority='" + fromJson[i].priority
							+ "' where id='" + fromJson[i].id + "';";
					db.execSQL(update);
				} else if (c.getInt(0) == sync_state.NEED_SYNC) {
					Log.e("ERROR", "Data need update server and client");
					// TODO Handle this
				}
			}

		}
		// Add
		select = "Select id,name,done,content,due,updated_at,priority from tasks where sync_state='"
				+ sync_state.ADD
				+ "' and list_id='"
				+ fromJson[0].list_id
				+ "';";
		Cursor c = db.rawQuery(select, null);
		c.moveToFirst();
		// 0=id,1=name,2=done,3=content,4=due,5=update_at,6=priority
		for (int i = 0; i < c.getCount(); i++) {
			List<BasicNameValuePair> paras = new ArrayList<BasicNameValuePair>();
			paras.add(new BasicNameValuePair("task[name]", c.getString(1)));
			paras.add(new BasicNameValuePair("task[priority]", c.getString(6)));
			paras.add(new BasicNameValuePair("task[done]", c.getString(2)));
			paras.add(new BasicNameValuePair("task[due]", c.getString(4)));
			paras.add(new BasicNameValuePair("task[content]", c.getString(3)));
			new get_data(Http_Mode.POST, paras).execute(Server_url + "/lists/"
					+ fromJson[0].list_id + "/tasks.json");
			c.moveToNext();
		}
		// update
		select = "Select id,name,done,content,due,updated_at,priority from tasks where sync_state='"
				+ sync_state.NEED_SYNC
				+ "' and list_id='"
				+ fromJson[0].list_id + "';";
		c = db.rawQuery(select, null);
		String update = "Update tasks set sync_state='" + sync_state.NOTHING
				+ "' where sync_state='" + sync_state.NEED_SYNC
				+ "' and list_id='" + fromJson[0].list_id + "';";
		c.moveToFirst();
		// 0=id,1=name,2=done,3=content,4=due,5=update_at,6=priority
		for (int i = 0; i < c.getCount(); i++) {
			List<BasicNameValuePair> paras = new ArrayList<BasicNameValuePair>();
			paras.add(new BasicNameValuePair("task[name]", c.getString(1)));
			paras.add(new BasicNameValuePair("task[priority]", c.getString(6)));
			paras.add(new BasicNameValuePair("task[done]", c.getString(2)));
			paras.add(new BasicNameValuePair("task[due]", c.getString(4)));
			paras.add(new BasicNameValuePair("task[content]", c.getString(3)));
			new get_data(Http_Mode.PUT, paras).execute(Server_url + "/lists/"
					+ fromJson[0].list_id + "/tasks/" + c.getInt(0) + ".json");
			c.moveToNext();
		}

		// Delete
		select = "Select id from tasks where sync_state='" + sync_state.DELETE
				+ "' and list_id='" + fromJson[0].list_id + "';";
		c = db.rawQuery(select, null);
		c.moveToFirst();
		String delete = "Delete from tasks where sync_state='"
				+ sync_state.DELETE + "' and list_id='" + fromJson[0].list_id
				+ "';";
		for (int i = 0; i < c.getCount(); i++) {
			new get_data(Http_Mode.DELETE).execute(Server_url + "/lists/"
					+ fromJson[0].list_id + "/tasks/" + c.getInt(0) + ".json");
			c.moveToNext();
		}
		db.execSQL(delete);
		db.execSQL(update);
		show_tasks();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Log.e("Menü", "Settings");
			// TODO implement Settings
			return true;
		case R.id.menu_logout:
			Log.e("Menü", "Logout");
			logout();
			return true;
		case R.id.menu_sorting:
			final CharSequence[] items = { "ID", "Due", "Priority" };
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Change Sorting Order");
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					switch (item) {
					case 1:
						task_order = "order by due asc";
						break;
					case 2:
						task_order = "order by priority desc";
						break;
					default:
						task_order = "order by id asc";
						break;
					}
					show_tasks();
					Toast.makeText(getApplicationContext(), items[item],
							Toast.LENGTH_SHORT).show();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
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
