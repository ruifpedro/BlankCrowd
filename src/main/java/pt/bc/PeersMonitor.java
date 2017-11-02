package pt.bc;


import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;

public class PeersMonitor implements Serializable, PeersInterface {
	private static final ConcurrentSkipListSet<String> peerSet = new ConcurrentSkipListSet<>();

	@Override
	public void add(String peer) {
		peerSet.add(peer);
	}

	@Override
	public void remove(String peer) {
		peerSet.remove(peer);
	}

	@Override
	public boolean contains(String peer) {
		return peerSet.contains(peer);
	}

	@Override
	public Iterator<String> getIterator() {
		return peerSet.iterator();
	}

	@Override
	public int size() {
		return peerSet.size();
	}
}
