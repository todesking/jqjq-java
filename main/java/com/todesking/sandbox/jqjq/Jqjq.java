package com.todesking.sandbox.jqjq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;

public class Jqjq {
	public static <T> JqjqIterable<T> toIterable(final T value) {
		return new JqjqIterable<T>() {
			@SuppressWarnings("unchecked")
			@Override
			public Iterator<T> iterator() {
				return Lists.newArrayList(value).iterator();
			}
		};
	}

	public static JqjqFile from(File file) {
		return new JqjqFile(file);
	}

	public static <T> JqjqIterable<T> from(Iterable<T> iterable) {
		return JqjqIterable.from(iterable);
	}

	public static JqjqInputStream from(InputStream inputStream) {
		return new JqjqInputStream(inputStream);
	}

	public static JqjqInt from(int n) {
		return new JqjqInt(n);
	}

	public static JqjqBufferedReader from(BufferedReader bufferedReader) {
		return new JqjqBufferedReader(bufferedReader);
	}

	public static JqjqString from(String str) {
		return new JqjqString(str);
	}

	public static class JqjqString {
		public JqjqString(String str) {
			this.str = str;
		}

		private final String str;

		public JqjqStringIterable split(String sep) {
			return new JqjqStringIterable(Arrays.asList(StringUtils
				.splitPreserveAllTokens(str, sep)));
		}
	}

	public static class JqjqStringIterable extends JqjqIterable<String> {
		public JqjqStringIterable(Iterable<String> source) {
			this.source = source;
		}

		private final Iterable<String> source;

		public JqjqIterable<Integer> toInteger() {
			return transform(new Function<String, Integer>() {
				public Integer apply(String str) {
					return Integer.parseInt(str);
				}
			});
		}

		@Override
		public Iterator<String> iterator() {
			return source.iterator();
		}
	}

	public static class JqjqInt {
		public JqjqInt(int n) {
			this.value = n;
		}

		private final int value;

		public JqjqIterable<Integer> toInfinity() {
			final int start = value;
			return new JqjqIterable<Integer>() {
				@Override
				public Iterator<Integer> iterator() {
					return new UnmodifiableIterator<Integer>() {
						private int i = start;

						public boolean hasNext() {
							return true;
						}

						public Integer next() {
							return i++;
						}
					};
				}
			};
		}

		public JqjqIterable<Integer> to(int m) {
			final int start = value;
			final int end = m;

			return new JqjqIterable<Integer>() {
				@Override
				public Iterator<Integer> iterator() {
					return new UnmodifiableIterator<Integer>() {
						private int i = start;

						public boolean hasNext() {
							return i <= end;
						}

						public Integer next() {
							return i++;
						}
					};
				}
			};
		}

		@Override
		public String toString() {
			return "" + value;
		}
	}

	public static class JqjqInputStream {
		public JqjqInputStream(InputStream source) {
			this.source = source;
		}

		private final InputStream source;

		public String readLine() throws IOException {
			return new BufferedReader(new InputStreamReader(source), 1)
				.readLine();
		}

		public JqjqIterable<String> eachLine() {
			return from(new BufferedReader(new InputStreamReader(source)))
				.eachLine();
		}

		public Scanner toScanner() {
			return new Scanner(this.source);
		}
	}

	public static class WithIndex<T> {
		public WithIndex(int index, T value) {
			this.index = index;
			this.value = value;
		}

		public final int index;
		public final T value;
	}

	public static class Tuple<T> {
		public Tuple(T... values) {
			this.values = values;
		}

		private final T[] values;

		public T get(int i) {
			return values[i];
		}
	}

	public static abstract class JqjqIterable<T> implements Iterable<T> {
		public static <T> JqjqIterable<T> from(final Iterable<T> target) {
			if (target instanceof JqjqIterable<?>)
				return (JqjqIterable<T>) target;
			return new JqjqIterable<T>() {
				@Override
				public Iterator<T> iterator() {
					return target.iterator();
				}
			};
		}

		public JqjqIterable<T> take(final int n) {
			return new JqjqIterable<T>() {
				@Override
				public Iterator<T> iterator() {
					return new UnmodifiableIterator<T>() {
						private final Iterator<T> source =
							JqjqIterable.this.iterator();
						int i = 0;

						public boolean hasNext() {
							return source.hasNext() && i < n;
						}

						public T next() {
							i++;
							return source.next();
						}
					};
				}
			};
		}

		public boolean satisfyAll(Predicate<T> pred) {
			for (T value : this)
				if (!pred.apply(value))
					return false;
			return true;
		}

		public boolean satisfyAny(Predicate<T> pred) {
			return !satisfyAll(Predicates.not(pred));
		}

		public JqjqIterable<Tuple<T>> eachPair() {
			return eachTuple(2);
		}

		private JqjqIterable<Tuple<T>> eachTuple(final int n) {
			return new JqjqIterable<Tuple<T>>() {
				@Override
				public Iterator<Tuple<T>> iterator() {
					return new UnmodifiableIterator<Tuple<T>>() {
						private final Iterator<T> source =
							JqjqIterable.this.iterator();
						final LinkedList<T> values = Lists.newLinkedList();
						{
							for (int i = 0; source.hasNext() && i < n; i++)
								values.addLast(source.next());
						}

						public boolean hasNext() {
							return values.size() == n;
						}

						public Tuple<T> next() {
							@SuppressWarnings("unchecked")
							final Tuple<T> result =
								new Tuple<T>((T[]) values.toArray());
							values.removeFirst();
							if (source.hasNext())
								values.addLast(source.next());
							return result;
						}
					};
				}
			};
		}

		public <U> JqjqIterable<U> transform(final Function<T, U> fun) {
			return new JqjqIterable<U>() {
				@Override
				public Iterator<U> iterator() {
					return Iterators.transform(
						JqjqIterable.this.iterator(),
						fun);
				}
			};
		}

		public abstract Iterator<T> iterator();

		public JqjqIterable<WithIndex<T>> withIndex() {
			return new JqjqIterable<WithIndex<T>>() {
				public Iterator<WithIndex<T>> iterator() {
					return Iterators.transform(
						JqjqIterable.this.iterator(),
						new Function<T, WithIndex<T>>() {
							private int index = 0;

							public WithIndex<T> apply(T arg0) {
								return new WithIndex<T>(index++, arg0);
							}
						});
				}
			};
		}

		public String join(String sep) {
			return StringUtils.join(iterator(), sep);
		}

		public JqjqIterable<T> appendFirst(T value) {
			return from(Iterables.concat(toIterable(value), this));
		}

		public JqjqIterable<T> appendLast(T value) {
			return from(Iterables.concat(this, toIterable(value)));
		}
	}

	public static class JqjqBufferedReader {
		public JqjqBufferedReader(BufferedReader reader) {
			this.reader = reader;
		}

		private final BufferedReader reader;

		public JqjqIterable<String> eachLine() {
			return new JqjqIterable<String>() {
				public Iterator<String> iterator() {
					return new UnmodifiableIterator<String>() {
						private String nextLine;
						private boolean preparedNext = false;

						private void prepareNext() {
							if (preparedNext)
								return;
							try {
								nextLine = reader.readLine();
								if (nextLine == null)
									reader.close();
							} catch (IOException e) {
								try {
									reader.close();
								} catch (IOException ignored) {
								}
								throw new RuntimeException(e);
							}
							preparedNext = true;
						}

						public boolean hasNext() {
							prepareNext();
							return nextLine != null;
						}

						public String next() {
							prepareNext();
							final String currentLine = nextLine;
							preparedNext = false;
							return currentLine;
						}
					};
				}
			};
		}

	}

	public static class JqjqFile {
		public JqjqFile(File file) {
			this.file = file;
		}

		final File file;

		public BufferedReader openAsBufferedReader()
				throws FileNotFoundException {
			return new BufferedReader(new InputStreamReader(
				new FileInputStream(file)));
		}

		public JqjqIterable<String> eachLine() throws FileNotFoundException {
			return from(openAsBufferedReader()).eachLine();
		}
	}
}
