package com.ddprojects.messager.service.api;

import com.ddprojects.messager.R;
import com.ddprojects.messager.service.fakeContext;

public class APIException extends Throwable {
    private final int code;
    public APIException(String errorMessage, int errorCode) {
        super(errorMessage);
        this.code = errorCode;
    }

    public int getCode() {
        return code;
    }

    public static String translate(String method, String errorMessage) {
        String humanReadableError = "API error: " + errorMessage;
        switch (method) {
            case "updates":
                switch (errorMessage) {
                    case "current entity 'update by product' not found":
                        humanReadableError = fakeContext.getInstance().
                                getString(R.string.error_update_item_by_product_not_found);
                        break;
                    case "current entity 'updates by product' not found":
                        humanReadableError = fakeContext.getInstance().
                                getString(R.string.error_update_items_by_product_not_found);
                        break;
                }
                break;
            case "email":
                switch (errorMessage) {
                    case "parameter 'code' are invalid":
                        humanReadableError = fakeContext.getInstance().
                                getString(R.string.error_email_code_invalid);
                        break;
                    case "parameter 'hash' are invalid":
                        humanReadableError = fakeContext.getInstance().
                                getString(R.string.error_developer_eblan);
                        break;
                }
            case "user":
                switch (errorMessage) {
                    case "current entity 'account by login' not found":
                        humanReadableError = fakeContext.getInstance().
                                getString(R.string.error_welcome_account_login_not_found);
                        break;
                    case "current entity 'account by login' are exists":
                        humanReadableError = fakeContext.getInstance().
                                getString(R.string.error_welcome_account_login_are_exists);
                        break;
                    case "current entity 'account by username' are exists":
                        humanReadableError = fakeContext.getInstance().
                                getString(R.string.error_welcome_account_username_are_exists);
                        break;
                    case "parameter 'password' are invalid":
                        humanReadableError = fakeContext.getInstance().
                                getString(R.string.error_welcome_account_password_invalid);
                }
                break;

        }
        return humanReadableError;
    }
}
