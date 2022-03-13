package de.ecconia.logicworld.issuemanager.manager.data;

public abstract class FilterEntry
{
	private final Filter filter;
	
	private Mode mode = Mode.Ignore;
	
	public FilterEntry(Filter filter)
	{
		this.filter = filter;
	}
	
	public abstract String getName();
	
	public void setMode(Mode mode)
	{
		boolean changed = mode != this.mode;
		this.mode = mode;
		if(changed)
		{
			filter.filterChanged();
		}
	}
	
	public Mode getMode()
	{
		return mode;
	}
	
	public enum Mode
	{
		Whitelist,
		Ignore,
		Blacklist,
	}
}
