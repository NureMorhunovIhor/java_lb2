package ua.nure.pv;

import java.util.ArrayList;
import java.util.List;

public class HashTableImpl implements HashTable {

	private static class Entry {
		int key;
		Object value;
		boolean occupied;
		public Entry(int key, Object value, boolean occupied) {
			this.key = key;
			this.value = value;
			this.occupied = occupied;
		}
	}

	private static final int INITIAL_SIZE = 4;

	private static final int MAX_SIZE = 16;

	private static final int MIN_SIZE = 2;
	private int size;
	private Entry[] table;
	public HashTableImpl() {
		table = new Entry[INITIAL_SIZE];
		size = 0;
	}
	@Override
	public void insert(int key, Object value) {
		if (size == MAX_SIZE) {
			throw new IllegalStateException("Max capacity reached. Cannot insert more elements.");
		}

		int hash = hash(key);
		int startHash = hash;
		while (table[hash] != null && table[hash].occupied && table[hash].key != key) {
			hash = (hash + 1) % table.length;
			if (hash == startHash) {
				resizeTableAndInsert(key, value);
				return;
			}
		}

		if (table[hash] == null || !table[hash].occupied) {
			size++;
		}

		table[hash] = new Entry(key, value, true);
	}

	private void resizeTableAndInsert(int key, Object value) {
		int newSize = Math.min(table.length * 2, MAX_SIZE);

		Entry[] oldTable = table;
		table = new Entry[newSize];
		size = 0;

		for (Entry entry : oldTable) {
			if (entry != null && entry.occupied) {
				int hash = hash(entry.key);
				while (table[hash] != null) {
					hash = (hash + 1) % table.length;
				}
				table[hash] = entry;
				size++;
			}
		}

		insert(key, value);
	}


	private void checkResizeDown() {
		int filledCount = countFilledEntries();
		int currentSize = table.length ;

		if (filledCount <= currentSize / 4 && currentSize > MIN_SIZE) {
			int newSize = Math.max(currentSize / 2, MIN_SIZE);
			Entry[] oldTable = table;
			table = new Entry[newSize];
			size = 0;
			for (Entry entry : oldTable) {
				if (entry != null && entry.occupied) {
					insert(entry.key, entry.value);
				}
			}
		}
	}


	private int countFilledEntries() {
		int count = 0;
		for (Entry entry : table) {
			if (entry != null && entry.occupied) {
				count++;
			}
		}
		return count;
	}
	@Override
	public Object search(int key) {
		int hash = hash(key);
		int startHash = hash;

		while (table[hash] != null) {
			if (table[hash].occupied && table[hash].key == key) {
				return table[hash].value;
			}
			hash = (hash + 1) % table.length;

			if (hash == startHash) {
				break;
			}
		}
		return null;
	}


	@Override
	public void remove(int key) {
		int hash = hash(key);
		int startHash = hash;

		while (table[hash] != null) {
			if (table[hash].occupied && table[hash].key == key) {
				table[hash].occupied = false;
				table[hash].value = null;
				size--;
				checkResizeDown();
				return;
			}

			hash = (hash + 1) % table.length;

			if (hash == startHash) {
				break;
			}
		}
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public int[] keys() {
		List<Integer> keyList = new ArrayList<>();
		for (Entry entry : table) {
			if (entry == null || !entry.occupied) {
				keyList.add(0);
			} else {
				keyList.add(entry.key);
			}
		}
		return keyList.stream().mapToInt(Integer::intValue).toArray();
	}

	private int hash(int key) {
		return Math.abs(key % table.length);
	}

}
