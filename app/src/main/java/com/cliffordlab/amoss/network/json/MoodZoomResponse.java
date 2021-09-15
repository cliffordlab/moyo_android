package com.cliffordlab.amoss.network.json;

import android.content.Context;

import com.cliffordlab.amoss.R;

import java.util.HashMap;

/**
 * Created by michael on 2/3/16.
 */
public class MoodZoomResponse {
	private String anxious;
	private String elated;
	private String sad;
	private String angry;
	private String irritable;
	private String energetic;

	private boolean health;
	private boolean work;
	private boolean housing;
	private boolean relationship;
	private boolean family;
	private boolean other;

	private String text;

	public MoodZoomResponse(Context context, HashMap<String, String> mood, HashMap<String, Boolean> stress, String info) {
		setAnxious(mood.get(context.getResources().getString(R.string.anxious)));
		setAngry(mood.get(context.getResources().getString(R.string.angry)));
		setIrritable(mood.get(context.getResources().getString(R.string.irritable)));
		setElated(mood.get(context.getResources().getString(R.string.elated)));
		setSad(mood.get(context.getResources().getString(R.string.sad)));
		setEnergetic(mood.get(context.getResources().getString(R.string.energetic)));

		setHealth(stress.get(context.getResources().getString(R.string.stress_cause_1)));
		setWork(stress.get(context.getResources().getString(R.string.stress_cause_2)));
		setHousing(stress.get(context.getResources().getString(R.string.stress_cause_3)));
		setRelationship(stress.get(context.getResources().getString(R.string.stress_cause_4)));
		setFamily(stress.get(context.getResources().getString(R.string.stress_cause_5)));
		setOther(stress.get(context.getResources().getString(R.string.stress_cause_6)));

		setText(info);

	}

	public String getAnxious() {
		return anxious;
	}

	public void setAnxious(String anxious) {
		this.anxious = anxious;
	}

	public String getElated() {
		return elated;
	}

	public void setElated(String elated) {
		this.elated = elated;
	}

	public String getSad() {
		return sad;
	}

	public void setSad(String sad) {
		this.sad = sad;
	}

	public String getAngry() {
		return angry;
	}

	public void setAngry(String angry) {
		this.angry = angry;
	}

	public String getIrritable() {
		return irritable;
	}

	public void setIrritable(String irritable) {
		this.irritable = irritable;
	}

	public String getEnergetic() {
		return energetic;
	}

	public void setEnergetic(String energetic) {
		this.energetic = energetic;
	}

	public boolean isHealth() {
		return health;
	}

	public void setHealth(boolean health) {
		this.health = health;
	}

	public boolean isWork() {
		return work;
	}

	public void setWork(boolean work) {
		this.work = work;
	}

	public boolean isHousing() {
		return housing;
	}

	public void setHousing(boolean housing) {
		this.housing = housing;
	}

	public boolean isRelationship() {
		return relationship;
	}

	public void setRelationship(boolean relationship) {
		this.relationship = relationship;
	}

	public boolean isFamily() {
		return family;
	}

	public void setFamily(boolean family) {
		this.family = family;
	}

	public boolean isOther() {
		return other;
	}

	public void setOther(boolean other) {
		this.other = other;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
