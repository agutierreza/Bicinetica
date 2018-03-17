package bicinetica.com.bicinetica.data;

import java.util.ArrayList;
import java.util.List;

public abstract class ProviderBase<T> {
    private List<T> items = new ArrayList<>();

    private List<OnListChanged<T>> listeners = new ArrayList<>();

    protected ProviderBase() { }

    public int getItemCount() {
        return items.size();
    }

    public T get(int i) {
        return items.get(i);
    }

    public List<T> getAll() {
        return items.subList(0, items.size());
        //return items.subList(0, items.size() - 1);
    }

    public void add(T record) {
        items.add(record);

        for (OnListChanged<T> listener : listeners) {
            listener.onItemAdded(items.size(), record);
        }
    }

    public void insert(int index, T item) {
        items.add(index, item);

        for (OnListChanged<T> listener : listeners) {
            listener.onItemAdded(index, item);
        }
    }

    public void remove(T record) {
        int i = findIndex(record);
        items.remove(i);

        for (OnListChanged<T> listener : listeners) {
            listener.onItemRemoved(i);
        }
    }

    public boolean contains(SensorData sensorData) {
        return items.contains(sensorData);
    }

    public void suscribe(OnListChanged<T> listener) {
        listeners.add(listener);
    }
    public void unsuscribe(OnListChanged<T> listener) {
        listeners.remove(listener);
    }

    public abstract int findIndex(T item);

    public interface OnListChanged<T> {
        void onItemAdded(int index, T item);
        void onItemRemoved(int index);
        //void onItemChanged(int index, T item);
    }
}
