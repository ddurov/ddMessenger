package com.ddprojects.messager.service.api;

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
                        humanReadableError = "Не найдено обновление для данного продукта";
                        break;
                    case "current entity 'updates by product' not found":
                        humanReadableError = "Не найдены обновления для данного продукта";
                        break;
                }
                break;
            case "user":
                switch (errorMessage) {
                    case "current entity 'account by login' not found":
                        humanReadableError = "Аккаунт с таким логином не найден";
                        break;
                    case "current entity 'account by login' are exists":
                        humanReadableError = "Аккаунт с таким логином уже существует";
                        break;
                    case "current entity 'account by username' are exists":
                        humanReadableError = "Аккаунт с таким именем уже существует";
                        break;
                }

        }
        return humanReadableError;
    }
}
