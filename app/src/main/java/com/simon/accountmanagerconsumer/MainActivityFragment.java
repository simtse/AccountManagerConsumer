package com.simon.accountmanagerconsumer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public static final String ACCOUNT_TYPE = "com.simon.accountsample";
    public static final String AUTH_TOKEN_TYPE = "com.simon.accountsample.aaa";
    public static final String EXTRA_ADD_ACCOUNT_ON_SUCCESS = "isAddingNewAccount";

    private static final int INTENT_LOGIN = 44;

    private AccountManager mAccountManager;
    private View mView;
    private String authToken;

    @Bind(R.id.status)
    TextView mStatusTextView;
    @Bind(R.id.accountName)
    TextView mAccountNameTextView;
    @Bind(R.id.accountAuthToken)
    TextView mAccountAuthTokenTextView;
    @Bind(R.id.accountOtherData)
    TextView mAccountOtherDataTextView;
    @Bind(R.id.startLoginButton)
    Button mLoginButton;
    @Bind(R.id.recheckButton)
    Button mRetryButton;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mAccountManager = AccountManager.get(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_main, container, false);

        ButterKnife.bind(this, mView);

        consumeAccountInfo();
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                consumeAccountInfo();
            }
        });

        return mView;
    }

    private void consumeAccountInfo() {
        // Ask for an auth token
        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
        Account account;
        if (accounts != null && accounts.length > 0) {
            account = accounts[0];
        } else {
            account = new Account("", ACCOUNT_TYPE);
        }
        mAccountManager.getAuthToken(account, AUTH_TOKEN_TYPE, null, true, authTokenCallback, null);
    }

    private AccountManagerCallback<Bundle> authTokenCallback = new AccountManagerCallback<Bundle>() {
        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            Bundle bundle;

            try {
                bundle = result.getResult();

                final Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (null != intent) {
                    mStatusTextView.setText(R.string.no_account_found);
                    resetAccountInfo();
                    setupLoginButton(intent);
                } else {
                    authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    final String accountName = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);
                    mStatusTextView.setText(R.string.hello_world);
                    mAccountNameTextView.setText("Retrieved auth token: " + authToken);
                    mAccountAuthTokenTextView.setText("Saved account name: " + accountName);
                    mAccountOtherDataTextView.setText("Saved auth token: " + bundle.toString());

                    mLoginButton.setVisibility(View.GONE);
                }
            } catch (OperationCanceledException e) {
                // If signup was cancelled, force activity termination
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setupLoginButton(final Intent intent) {
            mLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intent.putExtra(EXTRA_ADD_ACCOUNT_ON_SUCCESS, true);
                    startActivityForResult(intent, INTENT_LOGIN);
                }
            });
            mLoginButton.setVisibility(View.VISIBLE);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case INTENT_LOGIN:
                    consumeAccountInfo();
            }
        }
    }

    private void resetAccountInfo() {
        mAccountNameTextView.setText(R.string.account_name_default);
        mAccountAuthTokenTextView.setText(R.string.account_auth_token_default);
        mAccountOtherDataTextView.setText(R.string.account_other_data_default);
    }
}
