import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("javadoc")
public class ArgumentMapTest {

	@Nested
	public class FlagTests {

		@ParameterizedTest(name = "[{index}: \"{0}\"]")
		@ValueSource(strings = {
				"-a",
				"-1",
				"-hello",
				"--world",
				"-space ",
				"\t-tab\t"
		})
		public void testValidFlags(String flag) {
			boolean actual = ArgumentMap.isFlag(flag);
			Assertions.assertTrue(actual, flag);
		}

		@ParameterizedTest(name = "[{index}: \"{0}\"]")
		@ValueSource(strings = {
				"1",
				"a-b-c",
				"hello",
				"hello world",
				"",
				" ",
				"\t",
				"-",
				"- ",
				"-\t",
				"-\t \n"
		})
		public void testInvalidFlags(String flag) {
			boolean actual = ArgumentMap.isFlag(flag);
			Assertions.assertFalse(actual, flag);
		}

		@Test
		public void testNullFlag() {
			boolean actual = ArgumentMap.isFlag(null);
			Assertions.assertFalse(actual, "null");
		}
	}

	@Nested
	public class ValueTests {
		@ParameterizedTest(name = "[{index}: \"{0}\"]")
		@ValueSource(strings = {
				"1",
				"a-b-c",
				"hello",
				"hello world",
				" a",
				"\ta"
		})
		public void testValidValues(String value) {
			boolean actual = ArgumentMap.isValue(value);
			Assertions.assertTrue(actual, value);
		}

		@ParameterizedTest(name = "[{index}: \"{0}\"]")
		@ValueSource(strings = {
				"-a",
				"-1",
				"-hello",
				"--world",
				"",
				" ",
				"\t",
				" \t\n",
				"-",
				"- "
		})
		public void testInvalidValues(String value) {
			boolean actual = ArgumentMap.isValue(value);
			Assertions.assertFalse(actual, value);
		}

		@Test
		public void testNullValue() {
			boolean actual = ArgumentMap.isValue(null);
			Assertions.assertFalse(actual, "null");
		}
	}

	@Nested
	public class CountTests {

		@Test
		public void testOneFlag() {
			String[] args = { "-loquat" };
			int expected = 1;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		@Test
		public void testOnePair() {
			String[] args = { "-grape", "raisin" };
			int expected = 1;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		@Test
		public void testTwoFlags() {
			String[] args = { "-tomato", "-potato" };
			int expected = 2;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		@Test
		public void testOnlyValue() {
			String[] args = { "rhubarb" };
			int expected = 0;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		@Test
		public void testTwoValues() {
			String[] args = { "constant", "change" };

			int expected = 0;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		@Test
		public void testPineapple() {
			String[] args = { "pine", "-apple" };

			int expected = 1;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		@Test
		public void testSquash() {
			String[] args = {
					"-aubergine", "eggplant",
					"-courgette", "zucchini"
			};

			int expected = 2;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		@Test
		public void testFruit() {
			String[] args = {
					"-tangerine", "satsuma",
					"-tangerine", "clementine",
					"-tangerine", "mandarin"
			};

			int expected = 1;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		@Test
		public void testEmpty() {
			String[] args = {};

			int expected = 0;
			int actual = new ArgumentMap(args).numFlags();
			assertEquals(expected, actual);
		}

		// it is okay to throw a null pointer exception here
		@Test
		public void testNull() {
			String[] args = null;

			assertThrows(
					java.lang.NullPointerException.class,
					() -> new ArgumentMap(args).numFlags()
			);
		}
	}

	@Nested
	public class ParseTests {

		private ArgumentMap map;
		private String debug;

		@BeforeEach
		public void setup() {
			String[] args = {
					"-a", "42",
					"-b", "bat", "cat",
					"-d",
					"-e", "elk",
					"-e",
					"-f"
			};

			this.map = new ArgumentMap();
			this.map.parse(args);

			this.debug = "\n" + this.map.toString() + "\n";
		}

		@AfterEach
		public void teardown() {
			this.map = null;
		}

		@Test
		public void testNumFlags() {
			int expected = 5;
			int actual = this.map.numFlags();

			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testHasFlag() {
			assertTrue(this.map.hasFlag("-d"), this.debug);
		}

		@Test
		public void testHasLastFlag() {
			assertTrue(this.map.hasFlag("-f"), this.debug);
		}

		@Test
		public void testHasntFlag() {
			assertFalse(this.map.hasFlag("-g"), this.debug);
		}

		@Test
		public void testHasValue() {
			assertTrue(this.map.hasValue("-a"), this.debug);
		}

		@Test
		public void testHasFlagNoValue() {
			assertFalse(this.map.hasValue("-d"), this.debug);
		}

		@Test
		public void testNoFlagNoValue() {
			assertFalse(this.map.hasValue("-g"), this.debug);
		}

		@Test
		public void testGetValueExists() {
			String expected = "bat";
			String actual = this.map.getString("-b");
			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testGetValueNull() {
			String expected = null;
			String actual = this.map.getString("-d");
			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testGetValueNoFlag() {
			String expected = null;
			String actual = this.map.getString("-g");
			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testGetValueRepeatedFlag() {
			String expected = null;
			String actual = this.map.getString("-e");
			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testGetDefaultExists() {
			String expected = "bat";
			String actual = this.map.getString("-b", "bee");
			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testGetDefaultNull() {
			String expected = "dog";
			String actual = this.map.getString("-d", "dog");
			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testGetDefaultMissing() {
			String expected = "goat";
			String actual = this.map.getString("-g", "goat");
			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testDoubleParse() {
			String[] args = {
					"-a", "42", "-b", "bat", "cat",
					"-d", "-e", "elk", "-e", "-f"
			};
			this.map.parse(args);

			int expected = 5;
			int actual = this.map.numFlags();

			assertEquals(expected, actual, this.debug);
		}

		@Test
		public void testGetValidPath() {
			String[] args = { "-p", "." };
			ArgumentMap map = new ArgumentMap(args);

			Path expected = Paths.get(".");
			Path actual = map.getPath("-p");
			assertEquals(expected, actual);
		}

		@Test
		public void testGetInValidPath() {
			String[] args = { "-p" };
			ArgumentMap map = new ArgumentMap(args);

			Path expected = null;
			Path actual = map.getPath("-p");
			assertEquals(expected, actual);
		}
	}
}
