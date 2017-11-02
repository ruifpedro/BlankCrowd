package pt.bc;

import java.util.Iterator;

/**
 * Created by Force on 30/10/2017.
 */
public interface PeersInterface {
	void add(String peer);

	void remove(String peer);

	boolean contains(String peer);

	Iterator<String> getIterator();

	int size();
}
