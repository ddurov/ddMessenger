package com.ddprojects.messager.service;

import java.util.ArrayList;

public class listener {
    public interface Observer {
        void newEvent(Object[] message);
    }

    private static final ArrayList<Observer> observers = new ArrayList<>();

    private static void notifyObservers(Object[] event) {
        observers.forEach(observer -> observer.newEvent(event));
    }

    public static void addObserver(Observer observer) {
        observers.add(observer);
    }

    public static void newEvent(Object[] object) {
        notifyObservers(object);
    }
}
