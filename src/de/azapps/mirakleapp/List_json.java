/**
 * 
 */
package de.azapps.mirakleapp;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View.OnClickListener;
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
public class List_json {

	public int id;
	public String name;
	public int user_id;
	public String created_at;
	public String updated_at;
	public int[] children;

	public List_json() {
	}

	public List_json(int id, String name, int user_id, String created_at,
			String updated_at, int[] children) {
		this.id = id;
		this.children = children;
		this.created_at = created_at;
		this.name = name;
		this.updated_at = updated_at;
		this.user_id = user_id;
	}

	public void show(MainActivity main, LinearLayout lists,
			OnClickListener cellTouch) {
		FrameLayout border = new FrameLayout(main);
		border.setBackgroundColor(Color.BLACK);
		border.setPadding(4, 4, 4, 4);

		FrameLayout.LayoutParams myParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
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
		name.setLayoutParams(new TableLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1f));

		name.setTextSize(1, 14);
		name.setGravity(Gravity.CENTER);
		name.setText(this.name);
		box.setTag(id);
		box.setOnClickListener(cellTouch);

		box.addView(name);

		border.addView(box);
		lists.addView(border);
	}

}
