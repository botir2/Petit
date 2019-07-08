package open.it.com.petit.Register.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import open.it.com.petit.R;

/**
 * Created by user on 2017-10-30.
 */

public class MasterPopup extends Activity implements View.OnClickListener {
    private EditText pwEdit;
    private Button cancleBtn;
    private Button confirmBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.petit_feeder_master);

        pwEdit = (EditText) findViewById(R.id.petit_feeder_master_pw);
        cancleBtn = (Button) findViewById(R.id.petit_feeder_master_cancle);
        confirmBtn = (Button) findViewById(R.id.petit_feeder_master_confirm);

        cancleBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.petit_feeder_master_cancle:
                finish();
                break;
            case R.id.petit_feeder_master_confirm:
                passwordConfirm();
                break;
        }
    }

    private void passwordConfirm() {
        String pw = pwEdit.getText().toString();
        if (pw.equals("")) {
            Toast.makeText(this, "빈칸 없이 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!(pw.length() >= 6 && pw.length() <= 10)) {
            Toast.makeText(this, "비밀번호는 6-10자 제한입니다.\n 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
            return ;
        }

        Intent intent = new Intent(MasterPopup.this, AddQrPopup.class);
        intent.putExtra("MasterPw", pw);
        intent.putExtra("Ssid", getIntent().getStringExtra("Ssid"));
        intent.putExtra("WifiPw", getIntent().getStringExtra("WifiPw"));
        startActivity(intent);
        finish();
    }
}
