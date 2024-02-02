package com.ddprojects.messager.service.api;

import com.ddprojects.messager.R;
import com.ddprojects.messager.service.app;

public class APIException extends Exception {
    private final int code;
    public APIException(String errorMessage, int errorCode) {
        super(errorMessage);
        this.code = errorCode;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        String humanReadableError = "API error: " + super.getMessage();

        switch (humanReadableError) {
            case "current entity 'update by product' not found":
                humanReadableError = app.getInstance().
                        getString(R.string.error_update_item_by_product_not_found);
                break;
            case "current entity 'updates by product' not found":
                humanReadableError = app.getInstance().
                        getString(R.string.error_update_items_by_product_not_found);
                break;
            case "parameter 'code' are invalid":
                humanReadableError = app.getInstance().
                        getString(R.string.error_email_code_invalid);
                break;
            case "parameter 'hash' are invalid":
                humanReadableError = app.getInstance().
                        getString(R.string.error_developer_eblan);
                break;
            case "current entity 'account by login' not found":
                humanReadableError = app.getInstance().
                        getString(R.string.error_welcome_account_login_not_found);
                break;
            case "current entity 'account by login' are exists":
                humanReadableError = app.getInstance().
                        getString(R.string.error_welcome_account_login_are_exists);
                break;
            case "current entity 'account by username' are exists":
                humanReadableError = app.getInstance().
                        getString(R.string.error_welcome_account_username_are_exists);
                break;
            case "parameter 'password' are invalid":
                humanReadableError = app.getInstance().
                        getString(R.string.error_welcome_account_password_invalid);
                break;
        }

        return humanReadableError;
    }
}
