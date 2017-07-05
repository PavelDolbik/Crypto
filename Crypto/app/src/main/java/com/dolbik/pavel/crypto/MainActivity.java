package com.dolbik.pavel.crypto;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;

public class MainActivity extends MvpAppCompatActivity
        implements
        MyActivityView,
        View.OnClickListener {

    @InjectPresenter MyActivityPresenter presenter;

    private EditText planText;
    private EditText password;
    private TextView encryptedText;
    private TextView decryptedText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        planText      = (EditText) findViewById(R.id.plan_text);
        password      = (EditText) findViewById(R.id.password);
        encryptedText = (TextView) findViewById(R.id.encrypted);
        decryptedText = (TextView) findViewById(R.id.decrypted);

        Button encrypt = (Button)  findViewById(R.id.button_encrypt);
        Button decrypt = (Button)  findViewById(R.id.button_decrypt);
        Button clear   = (Button)  findViewById(R.id.button_clear);

        encrypt.setOnClickListener(this);
        decrypt.setOnClickListener(this);
        clear.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_encrypt:
                presenter.encryptedData(
                        planText.getText().toString().trim(),
                        password.getText().toString().trim());
                break;
            case R.id.button_decrypt:
                presenter.decryptedData(password.getText().toString().trim());
                break;
            case R.id.button_clear:
                presenter.clear();
                break;
        }
    }


    @Override
    public void showError(int resId) {
        String error = getResources().getString(resId);
        Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void setEncryptedString(String encryptedString) {
        encryptedText.setText(encryptedString);
    }


    @Override
    public void setDecryptedString(String decryptedString) {
        decryptedText.setText(decryptedString);
    }


    @Override
    public void clear() {
        planText.setText("");
        password.setText("");
        encryptedText.setText("");
        decryptedText.setText("");
    }
}
