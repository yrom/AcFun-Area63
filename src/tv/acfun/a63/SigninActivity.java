package tv.acfun.a63;

import tv.acfun.a63.util.ActionBarUtil;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class SigninActivity extends SherlockActivity {
    public static final int REQUEST_SIGN_IN = 1;
    private static final String TAG = "SigninActivity";

    public static Intent createIntent(Context pkgContext) {
        return new Intent(pkgContext, SigninActivity.class);
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        getSupportActionBar().setTitle(R.string.signin);
        ActionBarUtil.setXiaomiFilterDisplayOptions(getSupportActionBar(),false);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        EditText nameView = (EditText) findViewById(R.id.user_name);
        
        EditText pwdView = (EditText) findViewById(R.id.password);
        final View btn = findViewById(R.id.btn_signin);
        btn.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "todo : sign in", 0).show();
                setResult(RESULT_OK);
                finish();
            }
        });
        pwdView.setOnEditorActionListener(new OnEditorActionListener() {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                    btn.performClick();
                    return true;
                }
                return false;
            }
        });
        
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            setResult(RESULT_CANCELED);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
