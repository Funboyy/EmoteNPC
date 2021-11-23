package de.funboyy.labymod.emote.npc.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import lombok.Getter;

public class NMSObject {

    @Getter private final Object object;

    public NMSObject(final Object object) {
        if (object == null) {
            throw new NullPointerException("NMSObject cannot be null");
        }

        this.object = object;
    }

    public Object getField(final String field) {
        try {
            return this.object.getClass().getField(field).get(this.object);
        } catch (final NoSuchFieldException | IllegalAccessException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public NMSMethod getMethod(final String method, final Class<?>... clazz) {
        try {
            return new NMSMethod(this.object, this.object.getClass().getMethod(method, clazz));
        } catch (final NoSuchMethodException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    public NMSDeclaredMethod getDeclaredMethod(final String method, final Class<?>... clazz) {
        if (!(this.object instanceof Class)) {
            throw new RuntimeException("Can only get DeclaredMethod from a class");
        }

        try {
            return new NMSDeclaredMethod(((Class<?>) this.object).getMethod(method, clazz));
        } catch (final NoSuchMethodException exception) {
            exception.printStackTrace();
        }

        return null;
    }

    @SuppressWarnings("all")
    public class NMSMethod {

        private final Object object;
        private final Method method;

        public NMSMethod(final Object object, final Method method) {
            if (method == null) {
                throw new NullPointerException("NMSMethod cannot be null");
            }

            this.object = object;
            this.method = method;
        }

        public Object invoke(final Object... objects) {
            try {
                return this.method.invoke(this.object, objects);
            } catch (final IllegalAccessException | InvocationTargetException exception) {
                exception.printStackTrace();
            }

            return null;
        }
    }

    @SuppressWarnings("all")
    public class NMSDeclaredMethod {

        private final Method method;

        public NMSDeclaredMethod(final Method method) {
            if (method == null) {
                throw new NullPointerException("NMSDeclaredMethod cannot be null");
            }

            this.method = method;
            this.method.setAccessible(true);
        }

        public Object invoke(final Object... objects) {
            try {
                return this.method.invoke(null, objects);
            } catch (final IllegalAccessException | InvocationTargetException exception) {
                exception.printStackTrace();
            }

            return null;
        }
    }
}
