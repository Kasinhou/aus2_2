package structure;

public interface IData<T> extends IRecord {
    boolean equalsTo(T comparedData);
    T createClass();
}
