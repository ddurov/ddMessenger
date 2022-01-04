package com.eviger;

import java.util.ArrayList;

public class z_listener {
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
