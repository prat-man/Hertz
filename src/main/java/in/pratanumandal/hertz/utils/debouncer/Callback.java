package in.pratanumandal.hertz.utils.debouncer;

@FunctionalInterface
public interface Callback<T> {
    void call(T t);
}
