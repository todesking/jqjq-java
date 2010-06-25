import static com.todesking.sandbox.jqjq.Jqjq.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.todesking.sandbox.jqjq.Jqjq.JqjqIterable;
import com.todesking.sandbox.jqjq.Jqjq.Tuple;
import com.todesking.sandbox.jqjq.Jqjq.WithIndex;

public class Example {
	public static void main(String[] args) throws IOException {
		reversedPascalTriangle();
	}

	private static void reversedPascalTriangle() {
		final String prompt = "input numbers: ";
		System.out.print(prompt);
		for (String line : from(System.in).eachLine()) {
			JqjqIterable<Integer> nums = from(line).split(" ").toInteger();
			while (true) {
				final String str = nums.join(" ");
				if (str.length() == 0)
					break;
				System.out.println(str);
				nums =
					nums.eachPair().transform(
						new Function<Tuple<Integer>, Integer>() {
							public Integer apply(Tuple<Integer> arg0) {
								return arg0.get(0) + arg0.get(1);
							}
						});
			}
			System.out.println(prompt);
		}
	}

	static void echo() {
		System.out.print("echo > ");
		for (String line : from(System.in).eachLine()) {
			System.out.println(line);
			System.out.print("echo > ");
		}
	}

	static void showSorted() {
		showSorted(Arrays.asList(1, 2, 3, 4));
		showSorted(Arrays.asList(1, 2, 4, 3));
	}

	private static void showSorted(final List<Integer> list) {
		final boolean sorted =
			from(list).eachPair().satisfyAll(new Predicate<Tuple<Integer>>() {
				public boolean apply(Tuple<Integer> arg0) {
					return arg0.get(0) <= arg0.get(1);
				}
			});
		System.out.println("sorted(" + list + ") = " + sorted);
	}

	static void fizzBuzz() {
		final JqjqIterable<String> fizzBuzz =
			from(1).toInfinity().transform(new Function<Integer, String>() {
				public String apply(Integer n) {
					return n % 15 == 0 ? "FizzBuzz" : n % 3 == 0
						? "Fizz"
						: n % 5 == 0 ? "Buzz" : from(n).toString();
				}
			});
		for (String s : fizzBuzz.take(100)) {
			System.out.println(s);
		}
	}

	static void print1to10() {
		for (int i : from(1).to(10)) {
			System.out.println(i);
		}
	}

	static void showFileWithLineNo() throws IOException {
		for (WithIndex<String> line : from(new File(from(System.in).readLine()))
			.eachLine()
			.withIndex()) {
			System.out.println(line.index + ": " + line.value);
		}
	}
}
