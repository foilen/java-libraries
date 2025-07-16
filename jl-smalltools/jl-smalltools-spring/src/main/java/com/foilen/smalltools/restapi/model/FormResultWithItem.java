package com.foilen.smalltools.restapi.model;

public class FormResultWithItem<T> extends FormResult {

    private T item;

    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }

}
