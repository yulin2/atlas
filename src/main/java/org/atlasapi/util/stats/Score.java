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
public class Score<T> implements Comparable<Score<T>> {

	private final T target;
	private double score;
	private final Comparator<? super T> comparator;

	public Score(T target, Comparator<? super T> comparator, double score) {
		this.comparator = comparator;
		this.score = score;
		this.target = target;
	}

	public Score(T target) {
		this(target, 0.0);
	}
	
	@SuppressWarnings("unchecked")
	public Score(T target, double score) {
		this(target, (Comparator<? super T>) Ordering.natural(), score);
		if (!(target instanceof Comparable<?>)) {
			throw new IllegalArgumentException("Score requires that targets implement Comparable or provide a suitable Comparator");
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Score<?>) {
			return target.equals(((Score<?>) obj).getTarget());
		}
		return false;
	}
	
	@Override
	public int compareTo(Score<T> o) {
		if (equals(o)) {
			return 0;
		}
		if (score == o.score) {
			return comparator.compare(target, o.getTarget());
		} else {
			return Double.valueOf(score).compareTo(o.score);
		}
	}
	
	@Override
	public int hashCode() {
		return target.hashCode();
	}

	public T getTarget() {
		return target;
	}

	public double getScore() {
		return score;
	}
	
	public static <T extends Comparable<T>> Score<T> of(T target) {
		return new Score<T>(target);
	}
	
	public static <T> Score<T> of(T target, Comparator<? super T> comparator) {
		return new Score<T>(target, comparator, 0);
	}
	
	public static <T> Function<Score<T>, T> unpackTarget() {
		return new Function<Score<T>, T>() {

			@Override
			public T apply(Score<T> score) {
				return score.getTarget();
			}
		};
	}
}

