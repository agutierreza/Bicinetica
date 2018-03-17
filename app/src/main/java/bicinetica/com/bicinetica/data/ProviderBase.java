package bicinetica.com.bicinetica.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProviderBase<T> {
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
        return items.subList(0, items.size() - 1);
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

    public void suscribe(OnListChanged<T> listener) {
        listeners.add(listener);
    }
    public void unsuscribe(OnListChanged<T> listener) {
        listeners.remove(listener);
    }

    private int findIndex(T r) {
        for (int i = 0; i < items.size(); i++) {
            if (get(i).hashCode() == r.hashCode()) {
                return i;
            }
        }
        return -1;
    }

    public interface OnListChanged<T> {
        void onItemAdded(int index, T item);
        void onItemRemoved(int index);
        //void onItemChanged(int index, T item);
    }
}
