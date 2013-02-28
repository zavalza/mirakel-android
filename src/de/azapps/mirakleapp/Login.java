package de.azapps.mirakleapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;

import de.azapps.mirakleapp.MainActivity.get_data;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class Login extends Activity {
	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */
	private static final String[] DUMMY_CREDENTIALS = new String[] {
			"foo@example.com:hello", "bar@example.com:world" };

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private CheckBox checkBox;

	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private SQLiteDatabase db;

	private String Server_url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		db = openOrCreateDatabase("main.db",
				SQLiteDatabase.CREATE_IF_NECESSARY, null);
		String create_table = "CREATE TABLE IF NOT EXISTS users("
				+ "id integer not null primary key,"
				+ "email string(255) not null,"
				+ "encrypted_password string(255) not null,"
				+ "remember_me boolean );";
		db.execSQL(create_table);
		String create_settings = "CREATE TABLE IF NOT EXISTS settings("
				+ "server_url string(255)," + "id int not null primary key);";
		db.execSQL(create_settings);

		String select = "Select server_url From settings;";
		Cursor c = db.rawQuery(select, null);
		if (c.getCount() != 0) {
			c.moveToFirst();
			Server_url = c.getString(0);
		} else {
			Server_url = "http://192.168.10.105:3000";
			String insert = "Insert into settings(server_url,id) values(\""
					+ Server_url + "\",'1');";
			db.execSQL(insert);
		}

		// Set up the login form.
		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);
		checkBox = (CheckBox) findViewById(R.id.remember_me);
		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
		// TODO How to encrypt pw in db??
		String select2 = "Select email,encrypted_password from users where remember_me='TRUE';";
		Cursor c2 = db.rawQuery(select2, null);
		c2.moveToFirst();
		if (c2.getCount() != 0) {
			mEmail = c2.getString(0);
			mPassword = c2.getString(1);
			// TODO Implement Disable Option
			start_main();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {

			String stringUrl = Server_url + "/lists.json";
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {

				new authetificate().execute(stringUrl);
				mLoginStatusMessageView
						.setText(R.string.login_progress_signing_in);
				showProgress(true);
				mAuthTask = new UserLoginTask();
				mAuthTask.execute((Void) null);
			} else {
				Log.e("NetworkState", "No network connection available.");
				String select = "Select encrypted_password from users where email='"
						+ mEmail + "';";
				Cursor c = db.rawQuery(select, null);
				if (c.getCount() != 0) {
					c.moveToFirst();
					if (c.getString(0).equals(mPassword)) {
						String remember = "";
						if (checkBox.isChecked())
							remember = "TRUE";
						else
							remember = "FALSE";
						Log.e("Sucess", "Login_sucess");
						String update = "Update users Set remember_me='"
								+ remember + "' where email='" + mEmail + "';";
						db.execSQL(update);
						start_main();
					} else {
						Log.e("Login faild", "falsches PW");
						mPasswordView
								.setError(getString(R.string.error_incorrect_password));
						focusView = mPasswordView;
						focusView.requestFocus();
					}
				}

			}
			// TODO authentifikate user with onlinedb

			// String add= "Insert into users";
		}
	}

	void start_main() {
		mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
		showProgress(true);
		mAuthTask = new UserLoginTask();
		mAuthTask.execute((Void) null);
		Bundle data = new Bundle();
		data.putString("email", mEmail);
		data.putString("server", Server_url);
		data.putString("password", mPassword);
		Intent main = new Intent(this, MainActivity.class);
		main.putExtras(data);
		startActivity(main);
	}

	public class authetificate extends AsyncTask<String, Integer, Integer> {
		@Override
		protected Integer doInBackground(String... urls) {

			// params comes from the execute() call: params[0] is the url.
			try {
				return downloadUrl(urls[0]);
			} catch (IOException e) {
				Log.e("URL", "Unable to retrieve web page. URL may be invalid.");
				Log.e("URL", urls[0]);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return -1;
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(Integer result) {
			// textView.setText(result);
			Log.e("Status", result + "");
			if (result == 200 || result == 302) {
				Log.e("Sucess", "Login_sucess");
				String remember = "";
				if (checkBox.isChecked())
					remember = "TRUE";
				else
					remember = "FALSE";
				String update = "Update users Set remember_me='" + remember
						+ "',encrypted_password='" + mPassword
						+ "' where email='" + mEmail + "';";
				db.execSQL(update);
				start_main();
			} else {
				Log.e("Login faild", "falsches PW");
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				View focusView = mPasswordView;
				focusView.requestFocus();
			}
		}

		private int downloadUrl(String myurl) throws IOException,
				URISyntaxException {
			// Setup Connection
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			// Log.e("Url", myurl);
			request.setURI(new URI(myurl));
			// Log.e("Mail", Email);
			// Log.e("Pwd", password);
			client.getCredentialsProvider().setCredentials(
					new AuthScope(null, -1),
					new UsernamePasswordCredentials(mEmail, mPassword));
			// Get Data
			HttpResponse response = client.execute(request);
			return response.getStatusLine().getStatusCode();
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_forgot_password:
			Log.e("Menü", "Forgot Password");
			// newGame();
			return true;
		case R.id.menu_change_server:
			Log.e("Menü", "Change_Server");
			change_url();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	void change_url() {
		// TODO make change
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.

			try {
				// Simulate network access.
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				return false;
			}

			for (String credential : DUMMY_CREDENTIALS) {
				String[] pieces = credential.split(":");
				if (pieces[0].equals(mEmail)) {
					// Account exists, return true if the password matches.
					return pieces[1].equals(mPassword);
				}
			}

			// TODO: register the new account here.
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				finish();
			} else {
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
