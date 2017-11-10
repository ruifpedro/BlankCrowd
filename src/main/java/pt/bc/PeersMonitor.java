package pt.bc;


import java.io.Serializable;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class PeersMonitor implements Serializable, PeersInterface {
	private static final ConcurrentSkipListMap<String, ConcurrentSkipListSet<String>> peerMap = new ConcurrentSkipListMap<>();

	@Override
	public void add(String host, String peer) {
		peerMap.putIfAbsent(peer, new ConcurrentSkipListSet<>());
		peerMap.get(peer).add(peer);
	}

	@Override
	public void remove(String host) {
		peerMap.remove(host);
	}

	@Override
	public void remove(String host, String peer) {
		peerMap.getOrDefault(host, new ConcurrentSkipListSet<>()).remove(peer);
	}

	@Override
	public boolean contains(String host) {
		return peerMap.containsKey(host);
	}

	@Override
	public boolean contains(String host, String peer) {
		return peerMap.getOrDefault(host, new ConcurrentSkipListSet<>()).contains(peer);
	}

	@Override
	public NavigableSet<String> getSet() {
		return peerMap.keySet();
	}

	@Override
	public int size() {
		return peerMap.size();
	}
}
