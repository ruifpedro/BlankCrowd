package pt.bc;

import java.util.NavigableSet;

public interface PeersInterface {
	void add(String host, String peer);

	void remove(String peer);

	void remove(String host, String peer);

	boolean contains(String peer);

	boolean contains(String host, String peer);

	NavigableSet<String> getSet();

	int size();


}
