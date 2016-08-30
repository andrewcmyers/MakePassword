package edu.cornell.cs.apl.makepassword;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	// model
	boolean shuffle = true, alpha, numericonly;
	boolean showpwd = false;
	String passphrase;
	String password = "";
	static final int KEYLEN = 20;
	byte[] material = new byte[KEYLEN];
	int key_length = 0;
	final static String keyfilename = "pwdkey";
	
	// view
	EditText input;
	TextView output;
	LinearLayout list;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		screen1();
	}
	
	void screen1() {
		list = new LinearLayout(this);
		list.setOrientation(LinearLayout.VERTICAL);
		setContentView(list);
		
		final LogView log = new LogView();
		readKeyFile(log);
		if (log.errors()) return;
		
		TextView prompt = new TextView(this); prompt.setText("passphrase: ");
		TextView pp_input = new EditText(this);
		LinearLayout pp_entry = new LinearLayout(this);
		pp_entry.setOrientation(LinearLayout.HORIZONTAL);
		pp_input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

		pp_entry.addView(prompt);
		pp_entry.addView(pp_input);
		list.addView(pp_entry);
		
		pp_input.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {			
					passphrase = ((TextView) v).getText().toString();
					screen2();
				}
				return false;
			}			
		});
	}
	
	private void readKeyFile(LogView log) {
		try {
			FileOutputStream foo = openFileOutput("put_keyfile_here", 0);		
			Writer w = new PrintWriter(foo);
			try {
				w.append("dummy content");
				w.close();
			} catch (IOException e1) {
				log.log("I/O exception writing");
			}	
			try {
				FileInputStream keyfile = openFileInput(keyfilename);
				key_length = keyfile.read(material);
				//keyinfo.setText("Found key file (read " + key_length + " bytes)");
			} catch (FileNotFoundException _1) {			
				findExternalKeyfile(log);
			} catch (IOException e) {
				log.log("IO error reading key file");
			}
		} catch (FileNotFoundException e1) {
			log.log("Cannot write to dummy file.");
		}
	}

	boolean findExternalKeyfile(final LogView log) {
		log.log("Error: Key file " + keyfilename + " not found. Looking for key file in downloads.");
		File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

		log.log("External storage state: " + Environment.getExternalStorageState());
		log.log("Looking in: " + downloads.getPath());
		final String path = downloads.getPath().toString() + "/" + keyfilename;
		log.log(path + " exists: " + new File(path).exists());
		File[] files = downloads.listFiles();
		// XXX don't really need to scan all files in downloads dir.
		if (files != null) {
			for (File f : files) {
				if (f.getName().equals(keyfilename)) {
					log.log("found → " + f.getPath());
					try {
						FileInputStream i = new FileInputStream(f);
						final File kf = f;
						key_length = i.read(material);
						i.close();
						log.success();
						Button importb = new Button(this);
						importb.setText("import key file");
						list.addView(importb);
						importb.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								try {
									FileOutputStream o = openFileOutput(keyfilename, 0);
									try {
										o.write(material, 0, key_length);
										o.close();
										if (!kf.delete()) {
											log.log("Could not delete " + path);
										}
									} catch (IOException e) {
										log.log("I/O exception " + e.getMessage());
									}
								} catch (FileNotFoundException e) {
									log.log("File not found exception" + e.getMessage());
								}
							}
						});
						return true;
					} catch (IOException _2) {
						log.log("Could not read data from " + f.getPath());
						return false;
					}
				}
			}
		} else {
			log.log("Download directory " + downloads.getPath() + " doesn't seem to exist!?");
		}
		return false;
	}

	class LogView extends TextView {
		boolean display = false, error = false;
		LogView() { super(MainActivity.this); }
		public boolean errors() { return error; }
		public void log(String x) {
			if (!display) {
				display = true;
				list.addView(this);
				error = true;
				setText(x);
			} else {
				setText(getText().toString() + "\n" + x);
			}
		}
		public void success() {
			error = false;
		}
	}
	
	void screen2() {
		list.removeAllViews();
		input = new EditText(this);
		input.setLines(1);
		output = new TextView(this);		
		input.addTextChangedListener(new TextWatcher() {
			@Override public void afterTextChanged(Editable arg0) {}
			@Override public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
			@Override public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				generatePassword();
			}
		});
		
		CheckBox shuffle_box, alpha_box, numericonly_box, showpwd_box;
		Button copy = new Button(this); copy.setText("copy");
		Button clear = new Button(this); clear.setText("clear");
		Button erase = new Button(this); erase.setText("erase key file");
		
		list.addView(shuffle_box = new CheckBox(this)); shuffle_box.setText("no shuffle?");
		list.addView(alpha_box = new CheckBox(this));   alpha_box.setText("alpha?");
		list.addView(numericonly_box = new CheckBox(this)); numericonly_box.setText("numeric only?");
		list.addView(showpwd_box = new CheckBox(this)); showpwd_box.setText("show password?");
		list.addView(input);
		list.addView(output);
		LinearLayout buttons = new LinearLayout(this);
		buttons.setOrientation(LinearLayout.HORIZONTAL);
		buttons.addView(copy);
		Space sp1 = new Space(this), sp2 = new Space(this);
		sp1.setMinimumWidth(100); sp2.setMinimumHeight(100);
		buttons.addView(sp1);
		buttons.addView(clear);
	
		list.addView(buttons);
		list.addView(sp2);
		list.addView(erase);
		
		shuffle_box.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				shuffle = !isChecked;
				generatePassword();
			}
		});
		alpha_box.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				alpha = isChecked;
				generatePassword();
			}
		});
		numericonly_box.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				numericonly = isChecked;
				generatePassword();
			}
		});
		showpwd_box.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				showpwd = isChecked;
				generatePassword();
			}
		});
		copy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String pwd = password;
				ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				ClipData d = ClipData.newPlainText(ClipDescription.MIMETYPE_TEXT_PLAIN, pwd);
				cm.setPrimaryClip(d);
				input.setText("");
				output.setText("(copied)");
			}
		});
		clear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				password = "";
				output.setText("");
				input.setText("");
			}
		});
		erase.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final File kf = new File(getFilesDir().getPath() + "/" + keyfilename);
				if (kf.exists()) {
					final Button really = new Button(MainActivity.this);
					final Space sp3 = new Space(MainActivity.this); sp3.setMinimumHeight(100);
					list.addView(sp3);
					list.addView(really);
					really.setText("really erase it! (cannot be undone)");
					really.setOnClickListener(new OnClickListener() {
						@Override public void onClick(View v) {
							kf.delete();
							screen1();
						}
					});
					new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
					    @Override public void run() {
					    	list.removeView(sp3);
					        list.removeView(really);
					    }
					}, 3000);
				} else {
					TextView nosuchfile = new TextView(MainActivity.this);
					nosuchfile.setText("No file " + kf.getPath() + " found");
					list.addView(nosuchfile);
				}
			}
		});
	}
	void generatePassword() {
		try {
			String cleartext = input.getText().toString();
			if (cleartext.length() == 0) {
				output.setText("");
				return;
			}
			Pwgen pwgen = new Pwgen();
			password = pwgen.generate(cleartext, material, key_length, passphrase, shuffle, alpha, numericonly,
					new Procedure<String>() {
						public void apply(String msg) {
							throw new Error(msg);
						}
					});
			if (showpwd)
				output.setText(password);
			else
				output.setText("********");
		} catch (Error e) {
			output.setText("Error: " + e.getMessage());
		}
	}
}
