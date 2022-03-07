package de.ecconia.logicworld.issuemanager.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OrderedMap<K, V>
{
	private final List<V> list = new LinkedList<>();
	private final Map<K, V> map = new HashMap<>();
	
	public V put(K key, V value)
	{
		V oldValue = map.put(key, value);
		if(oldValue != null)
		{
			list.remove(oldValue);
		}
		list.add(value);
		return oldValue;
	}
	
	public List<V> getValues()
	{
		return list;
	}
	
	public boolean containsKey(K key)
	{
		return map.containsKey(key);
	}
	
	public V get(K key)
	{
		return map.get(key);
	}
	
	public void remove(K key)
	{
		V value = map.remove(key);
		list.remove(value);
	}
}
