package ua.nure.pv;

public interface HashTable {

    void insert(int key, Object value);

    Object search(int key);

    void remove (int key);

    int size();

    int[] keys();

    static HashTable getInstance(){
    	return new HashTableImpl();
    }

}

