package br.android.brapi.annotation;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClickListenerRegister {

    private static Map<Integer, View.OnClickListener> REGISTERED = new ConcurrentHashMap<>();


    public static void unregister(Activity act) {
        Class<? extends Activity> cls = act.getClass();
        for (Field f : cls.getDeclaredFields()) {
            f.setAccessible(true);
            if(f.isAnnotationPresent(ClickListener.class)){
                ClickListener cl = f.getAnnotation(ClickListener.class);
                REGISTERED.remove(cl.id());
                act.findViewById(cl.id()).setOnClickListener(null);
            }
        }
        for (Method m : cls.getDeclaredMethods()) {
            m.setAccessible(true);
            if(m.isAnnotationPresent(ClickListener.class)){
                ClickListener cl = m.getAnnotation(ClickListener.class);
                REGISTERED.remove(cl.id());
                act.findViewById(cl.id()).setOnClickListener(null);
            }
        }
    }

    public static void registr(Activity act, final boolean replace) {
        Class<? extends Activity> cls = act.getClass();
        for (Field f : cls.getDeclaredFields()) {
            f.setAccessible(true);
            if (f.isAnnotationPresent(ClickListener.class)
                    && View.OnClickListener.class.isAssignableFrom(f.getType())) {
                ClickListener cl = f.getAnnotation(ClickListener.class);
                try {
                    register(act, cl.id(), (View.OnClickListener) f.get(act), replace);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        for (Method m : cls.getDeclaredMethods()) {
            m.setAccessible(true);
            if (m.isAnnotationPresent(ClickListener.class)
                    && m.getReturnType() == void.class
                    && m.getParameterTypes().length == 1
                    && m.getParameterTypes()[0] == View.class
                    ) {
                ClickListener cl = m.getAnnotation(ClickListener.class);
                register(act, cl.id(), (v) -> {
                    try {
                        m.invoke(act, v);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }, replace);
            }
        }
    }

    private static void register(Activity act, int id, View.OnClickListener listener, boolean replace) {
        View.OnClickListener lser = REGISTERED.get(id);
        if (lser != null && !replace) {
            View.OnClickListener old = lser;
            lser = (view) -> {
                old.onClick(view);
                listener.onClick(view);
            };
        } else {
            lser = listener;
        }
        REGISTERED.put(id, lser);
        View v = act.findViewById(id);
        v.setOnClickListener(lser);
    }

    private ClickListenerRegister() {
    }
}
