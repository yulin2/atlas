/* Copyright 2010 Meta Broadcast Ltd

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You may
obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. */

package org.atlasapi.util.stats;

import java.util.Comparator;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

/**
 * @author Robert Chatley (robert@metabroadcast.com)
 * @author John Ayres (john@metabroadcast.com)
 */
public class Count<T> implements Comparable<Count<T>> {

	private final T target;
	private int count;
	private final Comparator<? super T> comparator;

	public Count(T target, Comparator<? super T> comparator,  int score) {
		this.comparator = comparator;
		this.count = score;
		this.target = target;
	}

	public Count(T target) {
		this(target, 0);
	}
	
	@SuppressWarnings("unchecked")
	public Count(T target, int score) {
		this(target, (Comparator<? super T>) Ordering.natural(), score);
		if (!(target instanceof Comparable<?>)) {
			throw new IllegalArgumentException("Count requires that targets implement Comparable or provide a suitable Comparator");
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Count<?>) {
			return target.equals(((Count<?>) obj).getTarget());
		}
		return false;
	}
	
	@Override
	public int compareTo(Count<T> o) {
		if (equals(o)) {
			return 0;
		}
		if (count == o.count) {
			return comparator.compare(target, o.getTarget());
		} else {
			return Integer.valueOf(count).compareTo(o.count);
		}
	}
	
	@Override
	public int hashCode() {
		return target.hashCode();
	}

	public T getTarget() {
		return target;
	}

	public void increment() {
		count++;
	}

	public int getCount() {
		return count;
	}
	
	public static <T extends Comparable<T>> Count<T> of(T target) {
		return new Count<T>(target);
	}
	
	public static <T> Count<T> of(T target, Comparator<? super T> comparator) {
		return new Count<T>(target, comparator, 0);
	}
	
	public static <T> Function<Count<T>, T> unpackTarget() {
		return new Function<Count<T>, T>() {

			@Override
			public T apply(Count<T> count) {
				return count.getTarget();
			}
		};
	}
}

