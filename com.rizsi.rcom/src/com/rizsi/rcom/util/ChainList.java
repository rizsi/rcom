package com.rizsi.rcom.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import hu.qgears.commons.CompatFunction;
import hu.qgears.commons.UtilComma;
import hu.qgears.commons.UtilString;

public class ChainList<T> extends ArrayList<T> {
	private static final long serialVersionUID = 1L;
	
	public ChainList() {
		super();
	}

	public ChainList(Collection<? extends T> c) {
		super(c);
	}

	@SafeVarargs
	public ChainList(T... elements) {
		super();
		for(T t :elements)
		{
			add(t);
		}
	}

	public ChainList<T> addc(T object)
	{
		add(object);
		return this;
	}
	@SafeVarargs
	final public ChainList<T> addcs(T... objects)
	{
		for(T o: objects)
		{
			add(o);
		}
		return this;
	}

	public ChainList<T> addcall(List<T> l) {
		for(T o: l)
		{
			add(o);
		}
		return this;
	}

	public String concat(String string) {
		return UtilString.concat(this, new UtilComma(" "), new CompatFunction<T, String>() {
			@Override
			public String apply(T t) {
				return ""+t;
			}
		});
	}
}
