/*
    Java Libraries https://github.com/foilen/java-libraries
    Copyright (c) 2015-2023 Foilen (https://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.smalltools.tuple;

/**
 * To keep one object's instance that you can mutate. Useful when you need methods to change the reference of an object that is passed by parameters.
 */
public class ObjectHolder<O> {

    private O object;

    public ObjectHolder() {
    }

    public ObjectHolder(O object) {
        this.object = object;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ObjectHolder other = (ObjectHolder) obj;
        if (object == null) {
            if (other.object != null) {
                return false;
            }
        } else if (!object.equals(other.object)) {
            return false;
        }
        return true;
    }

    public O get() {
        return object;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((object == null) ? 0 : object.hashCode());
        return result;
    }

    public ObjectHolder<O> set(O object) {
        this.object = object;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ObjectHolder [object=");
        builder.append(object);
        builder.append("]");
        return builder.toString();
    }

}
