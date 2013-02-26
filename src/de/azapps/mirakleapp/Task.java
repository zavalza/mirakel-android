/**
 * 
 */
package de.azapps.mirakleapp;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

	public void show(MainActivity main, LinearLayout task_list, OnCheckedChangeListener check_change, OnClickListener prio_popup,OnClickListener change_name, OnTouchListener drag) {
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
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		adaptLayout.setMargins(0, 0, 0, 0);
		box.setLayoutParams(adaptLayout);
		box.setBackgroundColor(Color.WHITE);
		box.setPadding(50, 15, 50, 5);
		// box.s
		TextView name = new TextView(main);
		TextView prio = new TextView(main);
		
		RelativeLayout.LayoutParams lp ;
		lp= new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		name.setLayoutParams(lp);
		lp= new RelativeLayout.LayoutParams(50, LayoutParams.MATCH_PARENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		prio.setLayoutParams(lp);
		prio.setGravity(Gravity.RIGHT);
		switch(priority){
			case -2:
				box.setBackgroundColor(Color.parseColor("#006400"));//Darkgreen
				break;
			case -1:
				box.setBackgroundColor(Color.GREEN);
				break;
			case 1:
				box.setBackgroundColor(Color.parseColor("#FF8C00"));//Orange
				break;
			case 2:
				box.setBackgroundColor(Color.RED);
				break;
			default:
				box.setBackgroundColor(Color.YELLOW);
		}
		
		prio.setGravity(Gravity.RIGHT);

		name.setTextSize(1, 20);
		name.setGravity(Gravity.CENTER_HORIZONTAL);
		name.setText(this.name);
		name.setTag(id);
		name.setPadding(10, 0, 10, 0);
		name.setOnClickListener(change_name);
		
		prio.setText(this.priority + "");
		prio.setTextSize(1, 20);
		prio.setTag(id);
		prio.setOnClickListener(prio_popup);

		CheckBox done = new CheckBox(main);
		done.setGravity(Gravity.LEFT);
		done.setChecked(this.done);
		done.setWidth(10);
		done.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		done.setOnCheckedChangeListener(check_change);
		done.setTag(id);
		
		border.setOnTouchListener(drag);
		//box.setOnTouchListener(drag);
		border.setTag(id);
		
		box.addView(prio);
		box.addView(done);
		box.addView(name);

		border.addView(box);
		task_list.addView(border);
	}

}
