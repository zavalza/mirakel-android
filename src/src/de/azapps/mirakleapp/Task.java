/**
 * 
 */
package de.azapps.mirakleapp;

import android.graphics.Color;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * @author weiznich
 * 
 */
public class Task {
	public String content;
	public String created_at;
	public boolean done;
	public String due;
	public int id;
	public int list_id;
	public String name;
	public int priority;
	public String updated_at;

	public Task(String con, String crea, String due, String name, String upd,
			int id, int list, int prio, boolean done) {
		this.content = con;
		this.created_at = crea;
		this.due = due;
		this.name = name;
		this.updated_at = upd;
		this.id = id;
		this.list_id = list;
		this.priority = prio;
		this.done = done;
	}

	public Task() {
	}

	public void show(MainActivity main, LinearLayout task_list) {
		FrameLayout border = new FrameLayout(main);
		border.setBackgroundColor(Color.BLACK);
		border.setPadding(4, 4, 4, 2);

		FrameLayout.LayoutParams myParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		myParams.setMargins(0, 0, 0, 10);
		border.setLayoutParams(myParams);

		RelativeLayout box = new RelativeLayout(main);
		RelativeLayout.LayoutParams adaptLayout = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.MATCH_PARENT);
		adaptLayout.setMargins(0, 0, 0, 0);
		box.setLayoutParams(adaptLayout);
		box.setBackgroundColor(Color.WHITE);
		box.setPadding(50, 15, 50, 5);
		// box.s
		TextView name = new TextView(main);
		TextView prio = new TextView(main);

		name.setLayoutParams(new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));
		prio.setLayoutParams(new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));

		prio.setTextSize(1, 14);
		prio.setTextColor(Color.RED);
		prio.setGravity(Gravity.RIGHT);

		name.setTextSize(1, 14);
		name.setGravity(Gravity.CENTER);
		name.setText(this.name);
		prio.setText(this.priority + "");

		CheckBox done = new CheckBox(main);
		done.setGravity(Gravity.LEFT);
		done.setChecked(this.done);
		done.setWidth(10);
		done.setLayoutParams(new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f));

		box.addView(done);
		box.addView(prio);
		box.addView(name);

		border.addView(box);
		task_list.addView(border);
	}

}
