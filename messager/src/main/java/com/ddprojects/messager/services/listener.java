package com.ddprojects.messager.services;

import java.util.ArrayList;

public class listener {
    public interface Observer {
        void newEvent(Object message);
    }

    private final ArrayList<Observer> observers = new ArrayList<>();

    private void notifyObservers(Object event) {
        observers.forEach(observer -> observer.newEvent(event));
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void newEvent(Object object) {
        notifyObservers(object);
    }
}
