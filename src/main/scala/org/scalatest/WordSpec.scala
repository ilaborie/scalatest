/*
 * Copyright 2001-2013 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatest

import words.{CanVerb, ResultOfAfterWordApplication, ShouldVerb, BehaveWord,
  MustVerb, StringVerbBlockRegistration}
import scala.collection.immutable.ListSet
import org.scalatest.exceptions.StackDepthExceptionHelper.getStackDepth
import java.util.concurrent.atomic.AtomicReference
import java.util.ConcurrentModificationException
import org.scalatest.events._
import Suite.anExceptionThatShouldCauseAnAbort
import Suite.autoTagClassAnnotations

/**
 * Facilitates a &ldquo;behavior-driven&rdquo; style of development (BDD), in which tests
 * are combined with text that specifies the behavior the tests verify.
 * 
 * <table><tr><td class="usage">
 * <strong>Recommended Usage</strong>:
 * For teams coming from specs or specs2, <code>WordSpec</code> will feel familiar, and is often the most natural way to port specsN tests to 
 * ScalaTest. <code>WordSpec</code> is very prescriptive in how text must be written, so a good fit for teams who want a high degree of discipline 
 * enforced upon their specification text.
 * </td></tr></table>
 * 
 * <p>
 * Class <code>WordSpec</code> is so named because your specification text is structured by placing words after strings.
 * Here's an example <code>WordSpec</code>:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.wordspec
 * 
 * import org.scalatest.WordSpec
 * 
 * class SetSpec extends WordSpec {
 * 
 *   "A Set" when {
 *     "empty" should {
 *       "have size 0" in {
 *         assert(Set.empty.size === 0)
 *       }
 *       
 *       "produce NoSuchElementException when head is invoked" in {
 *         intercept[NoSuchElementException] {
 *           Set.empty.head
 *         }
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * In a <code>WordSpec</code> you write a one (or more) sentence specification for each bit of behavior you wish to
 * specify and test. Each specification sentence has a
 * "subject," which is sometimes called the <em>system under test</em> (or SUT). The 
 * subject is entity being specified and tested and also serves as the subject of the sentences you write for each test. A subject
 * can be followed by one of three verbs, <code>should</code>, <code>must</code>, or <code>can</code>, and a block. Here are some
 * examples:
 * </p>
 * 
 * <pre class="stHighlight">
 * "A Stack" should {
 *   // ...
 * }
 * "An Account" must {
 *   // ...
 * }
 * "A ShippingManifest" can {
 *   // ...
 * }
 * </pre>
 * 
 * <p>
 * You can describe a subject in varying situations by using a <code>when</code> clause. A <code>when</code> clause
 * follows the subject and precedes a block. In the block after the <code>when</code>, you place strings that describe a situation or a state
 * the subject may be in using a string, each followed by a verb. Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * "A Stack" when {
 *   "empty" should {
 *     // ...
 *   }
 *   "non-empty" should {
 *     // ...
 *   }
 *   "full" should {
 *     // ...
 *   }
 * }
 * </pre>
 * 
 * <p>
 * When you are ready to finish a sentence, you write a string followed by <code>in</code> and a block that
 * contains the code of the test. Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.WordSpec
 * 
 * class StackSpec extends WordSpec {
 *   "A Stack" when {
 *     "empty" should {
 *       "be empty" in {
 *         // ...
 *       }
 *       "complain on peek" in {
 *         // ...
 *       }
 *       "complain on pop" in {
 *         // ...
 *       }
 *     }
 *     "full" should {
 *       "be full" in {
 *         // ...
 *       }
 *       "complain on push" in {
 *         // ...
 *       }
 *     }
 *   }
 * }
 * </pre>
 * 
 * <p>
 * Running the above <code>StackSpec</code> in the interpreter would yield:
 * </p>
 * 
 * <pre class="stREPL">
 * scala&gt; new StackSpec execute
 * <span class="stGreen">StackSpec:
 * A Stack
 *   when empty
 * &nbsp; - should be empty
 * &nbsp; - should complain on peek
 * &nbsp; - should complain on pop
 * &nbsp; when full
 * &nbsp; - should be full
 * &nbsp; - should complain on push</span>
 * </pre>
 *
 * <p>
 * Note that the output does not exactly match the input in an effort to maximize readability.
 * Although the <code>WordSpec</code> code is nested, which can help you eliminate any repeated phrases
 * in the specification portion of your code, the output printed moves <code>when</code> and <code>should</code>
 * down to the beginning of the next line.
 * </p>
 *
 * <p>
 * Sometimes you may wish to eliminate repeated phrases inside the block following a <code>verb</code>. Here's an example
 * in which the phrase "provide an and/or operator, which" is repeated:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.WordSpec
 * 
 * class AndOrSpec extends WordSpec {
 * 
 *   "The ScalaTest Matchers DSL" should {
 *     "provide an and operator, which returns silently when evaluating true and true" in {}
 *     "provide an and operator, which throws a TestFailedException when evaluating true and false" in {}
 *     "provide an and operator, which throws a TestFailedException when evaluating false and true" in {}
 *     "provide an and operator, which throws a TestFailedException when evaluating false and false" in {}
 *     "provide an or operator, which returns silently when evaluating true or true" in {}
 *     "provide an or operator, which returns silently when evaluating true or false" in {}
 *     "provide an or operator, which returns silently when evaluating false or true" in {}
 *     "provide an or operator, which throws a TestFailedException when evaluating false or false" in {}
 *   }
 * }
 * </pre>
 *
 * <p>
 * In such situations you can place <code>which</code> clauses inside the verb clause, like this:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.WordSpec
 * 
 * class AndOrSpec extends WordSpec {
 *
 *   "The ScalaTest Matchers DSL" should {
 *     "provide an and operator," which {
 *       "returns silently when evaluating true and true" in {}
 *       "throws a TestFailedException when evaluating true and false" in {}
 *       "throws a TestFailedException when evaluating false and true" in {}
 *       "throws a TestFailedException when evaluating false and false" in {}
 *     }
 *     "provide an or operator," which {
 *       "returns silently when evaluating true or true" in {}
 *       "returns silently when evaluating true or false" in {}
 *       "returns silently when evaluating false or true" in {}
 *       "throws a TestFailedException when evaluating false or false" in {}
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * Running the above <code>AndOrSpec</code> in the interpreter would yield:
 * </p>
 * 
 * <pre class="stREPL">
 * scala> new AndOrSpec execute
 * <span class="stGreen">AndOrSpec:
 * The ScalaTest Matchers DSL
 *   should provide an and operator, which
 * &nbsp; - returns silently when evaluating true and true
 * &nbsp; - throws a TestFailedException when evaluating true and false
 * &nbsp; - throws a TestFailedException when evaluating false and true
 * &nbsp; - throws a TestFailedException when evaluating false and false
 * &nbsp; should provide an or operator, which
 * &nbsp; - returns silently when evaluating true or true
 * &nbsp; - returns silently when evaluating true or false
 * &nbsp; - returns silently when evaluating false or true
 * &nbsp; - throws a TestFailedException when evaluating false or false</span>
 * </pre>
 * 
 * <p>
 * Note that unlike <code>when</code> and <code>should</code>/<code>must</code>/<code>can</code>, a <code>which</code> appears
 * in the output right where you put it in the input, at the end of the line, to maximize readability.
 * </p>
 *
 * <p>
 * <a name="AfterWords">If</a> a word or phrase is repeated at the beginning of each string contained in a block, you can eliminate
 * that repetition by using an <em>after word</em>. An after word is a word or phrase that you can place
 * after <code>when</code>, a verb, or
 * <code>which</code>. For example, in the previous <code>WordSpec</code>, the word "provide" is repeated
 * at the beginning of each string inside the <code>should</code> block. You can factor out this duplication
 * like this:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.WordSpec
 * 
 * class AndOrSpec extends WordSpec {
 * 
 *    def provide = afterWord("provide")
 * 
 *   "The ScalaTest Matchers DSL" should provide {
 *     "an and operator," which {
 *       "returns silently when evaluating true and true" in {}
 *       "throws a TestFailedException when evaluating true and false" in {}
 *       "that throws a TestFailedException when evaluating false and true" in {}
 *       "throws a TestFailedException when evaluating false and false" in {}
 *     }
 *     "an or operator," which {
 *       "returns silently when evaluating true or true" in {}
 *       "returns silently when evaluating true or false" in {}
 *       "returns silently when evaluating false or true" in {}
 *       "throws a TestFailedException when evaluating false or false" in {}
 *     }
 *   }
 * }
 * </pre>
 * 
 *  <p>
 *  Running the above version of <code>AndOrSpec</code> with the <code>provide</code> after word in the interpreter would give you:
 *  </p>
 * 
 * <pre class="stREPL">
 * scala&gt; new AndOrSpec execute
 * <span class="stGreen">AndOrSpec:
 * The ScalaTest Matchers DSL
 *   should provide
 *     an and operator, which
 * &nbsp;   - returns silently when evaluating true and true
 * &nbsp;   - throws a TestFailedException when evaluating true and false
 * &nbsp;   - that throws a TestFailedException when evaluating false and true
 * &nbsp;   - throws a TestFailedException when evaluating false and false
 * &nbsp;   an or operator, which
 * &nbsp;   - returns silently when evaluating true or true
 * &nbsp;   - returns silently when evaluating true or false
 * &nbsp;   - returns silently when evaluating false or true
 * &nbsp;   - throws a TestFailedException when evaluating false or false</span>
 * </pre>
 *
 * <p>
 * Once you've defined an after word, you can place it after <code>when</code>, a verb
 * (<code>should</code>, <code>must</code>, or <code>can</code>), or
 * <code>which</code>. (You can't place one after <code>in</code> or <code>is</code>, the
 * words that introduce a test.) Here's an example that has after words used in all three
 * places:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.WordSpec
 * 
 * class ScalaTestGUISpec extends WordSpec {
 * 
 *   def theUser = afterWord("the user")
 *   def display = afterWord("display")
 *   def is = afterWord("is")
 * 
 *   "The ScalaTest GUI" when theUser {
 *     "clicks on an event report in the list box" should display {
 *       "a blue background in the clicked-on row in the list box" in {}
 *       "the details for the event in the details area" in {}
 *       "a rerun button," which is {
 *         "enabled if the clicked-on event is rerunnable" in {}
 *         "disabled if the clicked-on event is not rerunnable" in {}
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * Running the previous <code>WordSpec</code> in the Scala interpreter would yield:
 * </p>
 *
 * <pre class="stREPL">
 * scala&gt; new ScalaTestGUISpec execute
 * <span class="stGreen">ScalaTestGUISpec:
 * The ScalaTest GUI
 *   when the user clicks on an event report in the list box
 *     should display
 * &nbsp;   - a blue background in the clicked-on row in the list box
 * &nbsp;   - the details for the event in the details area
 * &nbsp;     a rerun button, which is
 * &nbsp;     - enabled if the clicked-on event is rerunnable
 * &nbsp;     - disabled if the clicked-on event is not rerunnable</span>
 * </pre>
 * 
 * <p>
 * In case when you need to use different verb for a same subject, you can use <code>it</code> or <code>they</code> shorthand to avoid subject duplication:
 * </p>
 * 
 * <pre class="stHighlight">
 * "A Stack" when {
 *   // ...
 * }
 * 
 * it should {
 *   // ...
 * }
 * </pre>
 *
 * <p>
 * A <code>WordSpec</code>'s lifecycle has two phases: the <em>registration</em> phase and the
 * <em>ready</em> phase. It starts in registration phase and enters ready phase the first time
 * <code>run</code> is called on it. It then remains in ready phase for the remainder of its lifetime.
 * </p>
 *
 * <p>
 * Tests can only be registered while the <code>WordSpec</code> is
 * in its registration phase. Any attempt to register a test after the <code>WordSpec</code> has
 * entered its ready phase, <em>i.e.</em>, after <code>run</code> has been invoked on the <code>WordSpec</code>,
 * will be met with a thrown <a href="exceptions/TestRegistrationClosedException.html"><code>TestRegistrationClosedException</code></a>. The recommended style
 * of using <code>WordSpec</code> is to register tests during object construction as is done in all
 * the examples shown here. If you keep to the recommended style, you should never see a
 * <code>TestRegistrationClosedException</code>.
 * </p>
 *
 * <p>
 * <em>Note: Class <code>WordSpec</code> is in part inspired by class <code>org.specs.Specification</code>, designed by
 * Eric Torreborre for the <a href="http://code.google.com/p/specs/" target="_blank">specs framework</a>.</em>
 * </p>
 *
 * <a name="ignoredTests"></a><h2>Ignored tests</h2></a>
 *
 * To support the common use case of temporarily disabling a test, with the
 * good intention of resurrecting the test at a later time, <code>WordSpec</code> adds a method
 * <code>ignore</code> to strings that can be used instead of <code>in</code> to register a test. For example, to temporarily
 * disable the test with the name <code>"A Stack should pop values in last-in-first-out order"</code>, just
 * change &ldquo;<code>in</code>&rdquo; into &#8220;<code>ignore</code>,&#8221; like this:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.wordspec.ignore
 * 
 * import org.scalatest.WordSpec
 * 
 * class SetSpec extends WordSpec {
 *   
 *   "A Set" when {
 *     "empty" should {
 *       "have size 0" ignore {
 *         assert(Set.empty.size === 0)
 *       }
 *       
 *       "produce NoSuchElementException when head is invoked" in {
 *         intercept[NoSuchElementException] {
 *           Set.empty.head
 *         }
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * If you run this version of <code>SetSpec</code> with:
 * </p>
 *
 * <pre class="stREPL">
 * scala&gt; new SetSpec execute
 * </pre>
 *
 * <p>
 * It will run only the second test and report that the first test was ignored:
 * </p>
 *
 * <pre class="stREPL">
 * <span class="stGreen">A Set</span>
 * <span class="stGreen">  when empty</span>
 * <span class="stYellow">  - should have size 0 !!! IGNORED !!!</span>
 * <span class="stGreen">  - should should produce NoSuchElementException when head is invoked</span>
 * </pre>
 *
 * <p>
 * If you wish to temporarily ignore an entire suite of tests, you can annotate the test class with <code>@Ignore</code>, like this:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.wordspec.ignoreall
 * 
 * import org.scalatest.WordSpec
 * import org.scalatest.Ignore
 * 
 * @Ignore 
 * class SetSpec extends WordSpec {
 *   
 *   "A Set" when {
 *     "empty" should {
 *       "have size 0" in {
 *         assert(Set.empty.size === 0)
 *       }
 *       
 *       "produce NoSuchElementException when head is invoked" in {
 *         intercept[NoSuchElementException] {
 *           Set.empty.head
 *         }
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * When you mark a test class with a tag annotation, ScalaTest will mark each test defined in that class with that tag.
 * Thus, marking the <code>SetSpec</code> in the above example with the <code>@Ignore</code> tag annotation means that both tests
 * in the class will be ignored. If you run the above <code>SetSpec</code> in the Scala interpreter, you'll see:
 * </p>
 *
 * <pre class="stREPL">
 * scala&gt; new SetSpec execute
 * <span class="stGreen">SetSpec:
 * A Set
 *   when empty</span>
 * <span class="stYellow">  - should have size 0 !!! IGNORED !!!</span>
 * <span class="stYellow">  - should produce NoSuchElementException when head is invoked !!! IGNORED !!!</span>
 * </pre>
 *
 * <p>
 * Note that marking a test class as ignored won't prevent it from being discovered by ScalaTest. Ignored classes
 * will be discovered and run, and all their tests will be reported as ignored. This is intended to keep the ignored
 * class visible, to encourage the developers to eventually fix and &ldquo;un-ignore&rdquo; it. If you want to
 * prevent a class from being discovered at all, use the <a href="DoNotDiscover.html"><code>DoNotDiscover</code></a> annotation instead.
 * </p>
 *
 *
 * <a name="informers"></a><h2>Informers</h2></a>
 *
 * <p>
 * One of the parameters to <code>WordSpec</code>'s <code>run</code> method is a <a href="Reporter.html"><code>Reporter</code></a>, which
 * will collect and report information about the running suite of tests.
 * Information about suites and tests that were run, whether tests succeeded or failed, 
 * and tests that were ignored will be passed to the <code>Reporter</code> as the suite runs.
 * Most often the reporting done by default by <code>WordSpec</code>'s methods will be sufficient, but
 * occasionally you may wish to provide custom information to the <code>Reporter</code> from a test.
 * For this purpose, an <a href="Informer.html"><code>Informer</code></a> that will forward information to the current <code>Reporter</code>
 * is provided via the <code>info</code> parameterless method.
 * You can pass the extra information to the <code>Informer</code> via its <code>apply</code> method.
 * The <code>Informer</code> will then pass the information to the <code>Reporter</code> via an <a href="events/InfoProvided.html"><code>InfoProvided</code></a> event.
 * </p>
 * 
 * <p>
 * One use case for the <code>Informer</code> is to pass more information about a specification to the reporter. For example,
 * the <a href="GivenWhenThen.html"><code>GivenWhenThen</code></a> trait provides methods that use the implicit <code>info</code> provided by <code>WordSpec</code>
 * to pass such information to the reporter. Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.wordspec.info
 * 
 * import collection.mutable
 * import org.scalatest._
 * 
 * class SetSpec extends WordSpec with GivenWhenThen {
 *   
 *   "A mutable Set" should {
 *     "allow an element to be added" in {
 *       Given("an empty mutable Set")
 *       val set = mutable.Set.empty[String]
 * 
 *       When("an element is added")
 *       set += "clarity"
 * 
 *       Then("the Set should have size 1")
 *       assert(set.size === 1)
 * 
 *       And("the Set should contain the added element")
 *       assert(set.contains("clarity"))
 * 
 *       info("That's all folks!")
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * If you run this <code>WordSpec</code> from the interpreter, you will see the following output:
 * </p>
 *
 * <pre class="stREPL">
 * scala&gt; new SetSpec execute
 * <span class="stGreen">A mutable Set
 * - should allow an element to be added
 *   + Given an empty mutable Set 
 *   + When an element is added 
 *   + Then the Set should have size 1 
 *   + And the Set should contain the added element 
 *   + That's all folks!</span>
 * </pre>
 *
 * <a name="documenters"></a><h2>Documenters</h2></a>
 *
 * <p>
 * <code>WordSpec</code> also provides a <code>markup</code> method that returns a <a href="Documenter.html"><code>Documenter</code></a>, which allows you to send
 * to the <code>Reporter</code> text formatted in <a href="http://daringfireball.net/projects/markdown/" target="_blank">Markdown syntax</a>.
 * You can pass the extra information to the <code>Documenter</code> via its <code>apply</code> method.
 * The <code>Documenter</code> will then pass the information to the <code>Reporter</code> via an <a href="events/MarkupProvided.html"><code>MarkupProvided</code></a> event.
 * </p>
 *
 * <p>
 * Here's an example <code>WordSpec</code> that uses <code>markup</code>:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.wordspec.markup
 *
 * import collection.mutable
 * import org.scalatest._
 *
 * class SetSpec extends WordSpec with GivenWhenThen {
 *
 *   markup { """
 *
 * Mutable Set
 * -----------
 *
 * A set is a collection that contains no duplicate elements.
 *
 * To implement a concrete mutable set, you need to provide implementations
 * of the following methods:
 *
 *     def contains(elem: A): Boolean
 *     def iterator: Iterator[A]
 *     def += (elem: A): this.type
 *     def -= (elem: A): this.type
 *
 * If you wish that methods like `take`,
 * `drop`, `filter` return the same kind of set,
 * you should also override:
 *
 *     def empty: This
 *
 * It is also good idea to override methods `foreach` and
 * `size` for efficiency.
 *
 *   """ }
 *
 *   "A mutable Set" should {
 *     "allow an element to be added" in {
 *       Given("an empty mutable Set")
 *       val set = mutable.Set.empty[String]
 *
 *       When("an element is added")
 *       set += "clarity"
 *
 *       Then("the Set should have size 1")
 *       assert(set.size === 1)
 *
 *       And("the Set should contain the added element")
 *       assert(set.contains("clarity"))
 *
 *       markup("This test finished with a **bold** statement!")
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * Although all of ScalaTest's built-in reporters will display the markup text in some form,
 * the HTML reporter will format the markup information into HTML. Thus, the main purpose of <code>markup</code> is to
 * add nicely formatted text to HTML reports. Here's what the above <code>SetSpec</code> would look like in the HTML reporter:
 * </p>
 *
 * <img class="stScreenShot" src="../../lib/wordSpec.gif">
 *
 * <a name="notifiersAlerters"></a><h2>Notifiers and alerters</h2></a>
 *
 * <p>
 * ScalaTest records text passed to <code>info</code> and <code>markup</code> during tests, and sends the recorded text in the <code>recordedEvents</code> field of
 * test completion events like <code>TestSucceeded</code> and <code>TestFailed</code>. This allows string reporters (like the standard out reporter) to show
 * <code>info</code> and <code>markup</code> text <em>after</em> the test name in a color determined by the outcome of the test. For example, if the test fails, string
 * reporters will show the <code>info</code> and <code>markup</code> text in red. If a test succeeds, string reporters will show the <code>info</code>
 * and <code>markup</code> text in green. While this approach helps the readability of reports, it means that you can't use <code>info</code> to get status
 * updates from long running tests.
 * </p>
 *
 * <p>
 * To get immediate (<em>i.e.</em>, non-recorded) notifications from tests, you can use <code>note</code> (a <a href="Notifier.html"><code>Notifier</code></a>) and <code>alert</code>
 * (an <a href="Alerter.html"><code>Alerter</code></a>). Here's an example showing the differences:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.wordspec.note
 *
 * import collection.mutable
 * import org.scalatest._
 *
 * class SetSpec extends WordSpec {
 *
 *   "A mutable Set" should {
 *     "allow an element to be added" in {
 *
 *       info("info is recorded")
 *       markup("markup is *also* recorded")
 *       note("notes are sent immediately")
 *       alert("alerts are also sent immediately")
 *
 *       val set = mutable.Set.empty[String]
 *       set += "clarity"
 *       assert(set.size === 1)
 *       assert(set.contains("clarity"))
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * Because <code>note</code> and <code>alert</code> information is sent immediately, it will appear <em>before</em> the test name in string reporters, and its color will
 * be unrelated to the ultimate outcome of the test: <code>note</code> text will always appear in green, <code>alert</code> text will always appear in yellow.
 * Here's an example:
 * </p>
 *
 * <pre class="stREPL">
 * scala&gt; new SetSpec execute
 * <span class="stGreen">SetSpec:
 * A mutable Set
 *   + notes are sent immediately</span>
 *   <span class="stYellow">+ alerts are also sent immediately</span>
 * <span class="stGreen">- should allow an element to be added
 *   + info is recorded
 *   + markup is *also* recorded</span>
 * </pre>
 *
 * <p>
 * In summary, use <code>info</code> and <code>markup</code> for text that should form part of the specification output. Use
 * <code>note</code> and <code>alert</code> to send status notifications. (Because the HTML reporter is intended to produce a
 * readable, printable specification, <code>info</code> and <code>markup</code> text will appear in the HTML report, but
 * <code>note</code> and <code>alert</code> text will not.)
 * </p>
 *
 * <a name="pendingTests"></a><h2>Pending tests</h2></a>
 *
 * <p>
 * A <em>pending test</em> is one that has been given a name but is not yet implemented. The purpose of
 * pending tests is to facilitate a style of testing in which documentation of behavior is sketched
 * out before tests are written to verify that behavior (and often, before the behavior of
 * the system being tested is itself implemented). Such sketches form a kind of specification of
 * what tests and functionality to implement later.
 * </p>
 *
 * <p>
 * To support this style of testing, a test can be given a name that specifies one
 * bit of behavior required by the system being tested. The test can also include some code that
 * sends more information about the behavior to the reporter when the tests run. At the end of the test,
 * it can call method <code>pending</code>, which will cause it to complete abruptly with <a href="exceptions/TestPendingException.html"><code>TestPendingException</code></a>.
 * </p>
 *
 * <p>
 * Because tests in ScalaTest can be designated as pending with <code>TestPendingException</code>, both the test name and any information
 * sent to the reporter when running the test can appear in the report of a test run. (In other words,
 * the code of a pending test is executed just like any other test.) However, because the test completes abruptly
 * with <code>TestPendingException</code>, the test will be reported as pending, to indicate
 * the actual test, and possibly the functionality it is intended to test, has not yet been implemented.
 * You can mark tests as pending in a <code>WordSpec</code> like this:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.wordspec.pending
 * 
 * import org.scalatest._
 * 
 * class SetSpec extends WordSpec {
 * 
 *   "A Set" when {
 *     "empty" should {
 *       "have size 0" in (pending)
 *       
 *       "produce NoSuchElementException when head is invoked" in {
 *         intercept[NoSuchElementException] {
 *           Set.empty.head
 *         }
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * If you run this version of <code>SetSpec</code> with:
 * </p>
 *
 * <pre class="stREPL">
 * scala&gt; new SetSpec execute
 * </pre>
 *
 * <p>
 * It will run both tests but report that <code>should have size 0</code> is pending. You'll see:
 * </p>
 *
 * <pre class="stREPL">
 * <span class="stGreen">A Set</span>
 * <span class="stGreen">  when empty</span>
 * <span class="stYellow">  - should have size 0 (pending)</span>
 * <span class="stGreen">  - should produce NoSuchElementException when head is invoked</span>
 * </pre>
 * 
 * <p>
 * One difference between an ignored test and a pending one is that an ignored test is intended to be used during a
 * significant refactorings of the code under test, when tests break and you don't want to spend the time to fix
 * all of them immediately. You can mark some of those broken tests as ignored temporarily, so that you can focus the red
 * bar on just failing tests you actually want to fix immediately. Later you can go back and fix the ignored tests.
 * In other words, by ignoring some failing tests temporarily, you can more easily notice failed tests that you actually
 * want to fix. By contrast, a pending test is intended to be used before a test and/or the code under test is written.
 * Pending indicates you've decided to write a test for a bit of behavior, but either you haven't written the test yet, or
 * have only written part of it, or perhaps you've written the test but don't want to implement the behavior it tests
 * until after you've implemented a different bit of behavior you realized you need first. Thus ignored tests are designed
 * to facilitate refactoring of existing code whereas pending tests are designed to facilitate the creation of new code.
 * </p>
 *
 * <p>
 * One other difference between ignored and pending tests is that ignored tests are implemented as a test tag that is
 * excluded by default. Thus an ignored test is never executed. By contrast, a pending test is implemented as a
 * test that throws <code>TestPendingException</code> (which is what calling the <code>pending</code> method does). Thus
 * the body of pending tests are executed up until they throw <code>TestPendingException</code>. The reason for this difference
 * is that it enables your unfinished test to send <code>InfoProvided</code> messages to the reporter before it completes
 * abruptly with <code>TestPendingException</code>, as shown in the previous example on <code>Informer</code>s
 * that used the <code>GivenWhenThen</code> trait. For example, the following snippet in a <code>WordSpec</code>:
 * </p>
 *
 * <pre class="stHighlight">
 *  "The Scala language" should {
 *     "add correctly" in { 
 *       Given("two integers")
 *       When("they are added")
 *       Then("the result is the sum of the two numbers")
 *       pending
 *     }
 *     // ...
 * </pre>
 *
 * <p>
 * Would yield the following output when run in the interpreter:
 * </p>
 *
 * <pre class="stREPL">
 * <span class="stGreen">The Scala language</span>
 * <span class="stYellow">- should add correctly (pending)
 *   + Given two integers 
 *   + When they are added 
 *   + Then the result is the sum of the two numbers</span> 
 * </pre>
 *
 * <a name="taggingTests"></a><h2>Tagging tests</h2>
 *
 * A <code>WordSpec</code>'s tests may be classified into groups by <em>tagging</em> them with string names.
 * As with any suite, when executing a <code>WordSpec</code>, groups of tests can
 * optionally be included and/or excluded. To tag a <code>WordSpec</code>'s tests,
 * you pass objects that extend class <code>org.scalatest.Tag</code> to methods
 * that register tests. Class <code>Tag</code> takes one parameter, a string name.  If you have
 * created tag annotation interfaces as described in the <a href="Tag.html"><code>Tag</code> documentation</a>, then you
 * will probably want to use tag names on your test functions that match. To do so, simply 
 * pass the fully qualified names of the tag interfaces to the <code>Tag</code> constructor. For example, if you've
 * defined tag annotation interfaces with fully qualified names, <code>com.mycompany.tags.SlowTest</code> and
 * <code>com.mycompany.tags.DbTest</code>, then you could
 * create matching tags for <code>WordSpec</code>s like this:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.wordspec.tagging
 * 
 * import org.scalatest.Tag
 * 
 * object SlowTest extends Tag("com.mycompany.tags.SlowTest")
 * object DbTest extends Tag("com.mycompany.tags.DbTest")
 * </pre>
 *
 * <p>
 * Given these definitions, you could place <code>WordSpec</code> tests into groups like this:
 * </p>
 *
 * <pre class="stHighlight">
 * import org.scalatest.WordSpec
 * 
 * class SetSpec extends WordSpec {
 * 
 *   "A Set" when {
 *     "empty" should {
 *       "have size 0" taggedAs(SlowTest) in {
 *         assert(Set.empty.size === 0)
 *       }
 *       
 *       "produce NoSuchElementException when head is invoked" taggedAs(SlowTest, DbTest) in {
 *         intercept[NoSuchElementException] {
 *           Set.empty.head
 *         }
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * This code marks both tests with the <code>com.mycompany.tags.SlowTest</code> tag, 
 * and the second test with the <code>com.mycompany.tags.DbTest</code> tag.
 * </p>
 *
 * <p>
 * The <code>run</code> method takes a <a href="Filter.html"><code>Filter</code></a>, whose constructor takes an optional
 * <code>Set[String]</code> called <code>tagsToInclude</code> and a <code>Set[String]</code> called
 * <code>tagsToExclude</code>. If <code>tagsToInclude</code> is <code>None</code>, all tests will be run
 * except those those belonging to tags listed in the
 * <code>tagsToExclude</code> <code>Set</code>. If <code>tagsToInclude</code> is defined, only tests
 * belonging to tags mentioned in the <code>tagsToInclude</code> set, and not mentioned in <code>tagsToExclude</code>,
 * will be run.
 * </p>
 *
 * <p>
 * It is recommended, though not required, that you create a corresponding tag annotation when you
 * create a <code>Tag</code> object. A tag annotation allows you to tag all the tests of a <code>WordSpec</code> in
 * one stroke by annotating the class. For more information and examples, see the
 * <a href="Tag.html">documentation for class <code>Tag</code></a>.
 * </p>
 *
 * <a name="sharedFixtures"></a>
 * <h2>Shared fixtures</h2>
 *
 * <p>
 * A test <em>fixture</em> is composed of the objects and other artifacts (files, sockets, database
 * connections, <em>etc.</em>) tests use to do their work.
 * When multiple tests need to work with the same fixtures, it is important to try and avoid
 * duplicating the fixture code across those tests. The more code duplication you have in your
 * tests, the greater drag the tests will have on refactoring the actual production code.
 * </p>
 *
 * <p>
 * ScalaTest recommends three techniques to eliminate such code duplication:
 * </p>
 *
 * <ul>
 * <li>Refactor using Scala</li>
 * <li>Override <code>withFixture</code></li>
 * <li>Mix in a <em>before-and-after</em> trait</li>
 * </ul>
 *
 * <p>Each technique is geared towards helping you reduce code duplication without introducing
 * instance <code>var</code>s, shared mutable objects, or other dependencies between tests. Eliminating shared
 * mutable state across tests will make your test code easier to reason about and more amenable for parallel
 * test execution.</p><p>The following sections
 * describe these techniques, including explaining the recommended usage
 * for each. But first, here's a table summarizing the options:</p>
 *
 * <table style="border-collapse: collapse; border: 1px solid black">
 *
 * <tr>
 *   <td colspan="2" style="background-color: #CCCCCC; border-width: 1px; padding: 3px; padding-top: 7px; border: 1px solid black; text-align: left">
 *     <strong>Refactor using Scala when different tests need different fixtures.</strong>
 *   </td>
 * </tr>
 *
 * <tr>
 *   <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: right">
 *     <a href="#getFixtureMethods">get-fixture methods</a>
 *   </td>
 *   <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">
 *     The <em>extract method</em> refactor helps you create a fresh instances of mutable fixture objects in each test
 *     that needs them, but doesn't help you clean them up when you're done.
 *   </td>
 * </tr>
 *
 * <tr>
 *   <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: right">
 *     <a href="#fixtureContextObjects">fixture-context objects</a>
 *   </td>
 *   <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">
 *     By placing fixture methods and fields into traits, you can easily give each test just the newly created
 *     fixtures it needs by mixing together traits.  Use this technique when you need <em>different combinations
 *     of mutable fixture objects in different tests</em>, and don't need to clean up after.
 *   </td>
 * </tr>
 *
 * <tr>
 *   <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: right">
 *     <a href="#loanFixtureMethods">loan-fixture methods</a>
 *   </td>
 *   <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">
 *     Factor out dupicate code with the <em>loan pattern</em> when different tests need different fixtures <em>that must be cleaned up afterwards</em>.
 *   </td>
 * </tr>
 *
 * <tr>
 *   <td colspan="2" style="background-color: #CCCCCC; border-width: 1px; padding: 3px; padding-top: 7px; border: 1px solid black; text-align: left">
 *     <strong>Override <code>withFixture</code> when most or all tests need the same fixture.</strong>
 *   </td>
 * </tr>
 *
 * <tr>
 *   <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: right">
 *     <a href="#withFixtureNoArgTest">
 *       <code>withFixture(NoArgTest)</code></a>
 *     </td>
 *   <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">
 *     <p>
 *     The recommended default approach when most or all tests need the same fixture treatment. This general technique
 *     allows you, for example, to perform side effects at the beginning and end of all or most tests, 
 *     transform the outcome of tests, retry tests, make decisions based on test names, tags, or other test data.
 *     Use this technique unless:
 *     </p>
 *  <ul>
 *  <li>Different tests need different fixtures (refactor using Scala instead)</li>
 *  <li>An exception in fixture code should abort the suite, not fail the test (use a <em>before-and-after</em> trait instead)</li>
 *  <li>You have objects to pass into tests (override <code>withFixture(<em>One</em>ArgTest)</code> instead)</li>
 *  </ul>
 *  </td>
 * </tr>
 *
 * <tr>
 *   <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: right">
 *     <a href="#withFixtureOneArgTest">
 *       <code>withFixture(OneArgTest)</code>
 *     </a>
 *   </td>
 *   <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">
 *     Use when you want to pass the same fixture object or objects as a parameter into all or most tests.
 *   </td>
 * </tr>
 *
 * <tr>
 *   <td colspan="2" style="background-color: #CCCCCC; border-width: 1px; padding: 3px; padding-top: 7px; border: 1px solid black; text-align: left">
 *     <strong>Mix in a before-and-after trait when you want an aborted suite, not a failed test, if the fixture code fails.</strong>
 *   </td>
 * </tr>
 *
 * <tr>
 *   <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: right">
 *     <a href="#beforeAndAfter"><code>BeforeAndAfter</code></a>
 *   </td>
 *   <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">
 *     Use this boilerplate-buster when you need to perform the same side-effects before and/or after tests, rather than at the beginning or end of tests.
 *   </td>
 * </tr>
 *
 * <tr>
 *   <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: right">
 *     <a href="#composingFixtures"><code>BeforeAndAfterEach</code></a>
 *   </td>
 *   <td style="border-width: 1px; padding: 3px; border: 1px solid black; text-align: left">
 *     Use when you want to <em>stack traits</em> that perform the same side-effects before and/or after tests, rather than at the beginning or end of tests.
 *   </td>
 * </tr>
 *
 * </table>
 *
 * <a name="getFixtureMethods"></a>
 * <h4>Calling get-fixture methods</h4>
 *
 * <p>
 * If you need to create the same mutable fixture objects in multiple tests, and don't need to clean them up after using them, the simplest approach is to write one or
 * more <em>get-fixture</em> methods. A get-fixture method returns a new instance of a needed fixture object (or an holder object containing
 * multiple fixture objects) each time it is called. You can call a get-fixture method at the beginning of each
 * test that needs the fixture, storing the returned object or objects in local variables. Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.wordspec.getfixture
 * 
 * import org.scalatest.WordSpec
 * import collection.mutable.ListBuffer
 * 
 * class ExampleSpec extends WordSpec {
 * 
 *   def fixture = 
 *     new {
 *       val builder = new StringBuilder("ScalaTest is ")
 *       val buffer = new ListBuffer[String]
 *     }
 *   
 *   "Testing" should {
 *     "be easy" in {
 *       val f = fixture
 *       f.builder.append("easy!")
 *       assert(f.builder.toString === "ScalaTest is easy!")
 *       assert(f.buffer.isEmpty)
 *       f.buffer += "sweet"
 *     }
 *   
 *     "be fun" in {
 *       val f = fixture
 *       f.builder.append("fun!")
 *       assert(f.builder.toString === "ScalaTest is fun!")
 *       assert(f.buffer.isEmpty)
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * The &ldquo;<code>f.</code>&rdquo; in front of each use of a fixture object provides a visual indication of which objects 
 * are part of the fixture, but if you prefer, you can import the the members with &ldquo;<code>import f._</code>&rdquo; and use the names directly.
 * </p>
 *
 * <p>
 * If you need to configure fixture objects differently in different tests, you can pass configuration into the get-fixture method. For example, you could pass
 * in an initial value for a mutable fixture object as a parameter to the get-fixture method.
 * </p>
 *
 * <a name="fixtureContextObjects"></a>
 * <h4>Instantiating fixture-context objects </h4>
 *
 * <p>
 * An alternate technique that is especially useful when different tests need different combinations of fixture objects is to define the fixture objects as instance variables
 * of <em>fixture-context objects</em> whose instantiation forms the body of tests. Like get-fixture methods, fixture-context objects are only
 * appropriate if you don't need to clean up the fixtures after using them.
 * </p>
 *
 * To use this technique, you define instance variables intialized with fixture objects in traits and/or classes, then in each test instantiate an object that
 * contains just the fixture objects needed by the test. Traits allow you to mix together just the fixture objects needed by each test, whereas classes
 * allow you to pass data in via a constructor to configure the fixture objects. Here's an example in which fixture objects are partitioned into two traits
 * and each test just mixes together the traits it needs:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.wordspec.fixturecontext
 * 
 * import collection.mutable.ListBuffer
 * import org.scalatest.WordSpec
 * 
 * class ExampleSpec extends WordSpec {
 * 
 *   trait Builder {
 *     val builder = new StringBuilder("ScalaTest is ")
 *   }
 * 
 *   trait Buffer {
 *     val buffer = ListBuffer("ScalaTest", "is")
 *   }
 * 
 *   "Testing" should {
 *     // This test needs the StringBuilder fixture
 *     "be productive" in new Builder {
 *       builder.append("productive!")
 *       assert(builder.toString === "ScalaTest is productive!")
 *     }
 *   }
 * 
 *   "Test code" should {
 *     // This test needs the ListBuffer[String] fixture
 *     "be readable" in new Buffer {
 *       buffer += ("readable!")
 *       assert(buffer === List("ScalaTest", "is", "readable!"))
 *     }
 * 
 *     // This test needs both the StringBuilder and ListBuffer
 *     "be clear and concise" in new Builder with Buffer {
 *       builder.append("clear!")
 *       buffer += ("concise!")
 *       assert(builder.toString === "ScalaTest is clear!")
 *       assert(buffer === List("ScalaTest", "is", "concise!"))
 *     }
 *   }
 * }
 * </pre>
 *
 * <a name="withFixtureNoArgTest"></a>
 * <h4>Overriding <code>withFixture(NoArgTest)</code></h4>
 *
 * <p>
 * Although the get-fixture method and fixture-context object approaches take care of setting up a fixture at the beginning of each
 * test, they don't address the problem of cleaning up a fixture at the end of the test. If you just need to perform a side-effect at the beginning or end of
 * a test, and don't need to actually pass any fixture objects into the test, you can override <code>withFixture(NoArgTest)</code>, one of ScalaTest's
 * lifecycle methods defined in trait <a href="Suite.html#lifecyle-methods"><code>Suite</code></a>.
 * </p>
 *
 * <p>
 * Trait <code>Suite</code>'s implementation of <code>runTest</code> passes a no-arg test function to <code>withFixture(NoArgTest)</code>. It is <code>withFixture</code>'s
 * responsibility to invoke that test function. <code>Suite</code>'s implementation of <code>withFixture</code> simply
 * invokes the function, like this:
 * </p>
 *
 * <pre class="stHighlight">
 * // Default implementation in trait Suite
 * protected def withFixture(test: NoArgTest) = {
 *   test()
 * }
 * </pre>
 *
 * <p>
 * You can, therefore, override <code>withFixture</code> to perform setup before and/or cleanup after invoking the test function. If
 * you have cleanup to perform, you should invoke the test function inside a <code>try</code> block and perform the cleanup in
 * a <code>finally</code> clause, in case an exception propagates back through <code>withFixture</code>. (If a test fails because of an exception,
 * the test function invoked by withFixture will result in a [[org.scalatest.Failed <code>Failed</code>]] wrapping the exception. Nevertheless,
 * best practice is to perform cleanup in a finally clause just in case an exception occurs.)
 * </p>
 *
 * <p>
 * The <code>withFixture</code> method is designed to be stacked, and to enable this, you should always call the <code>super</code> implementation
 * of <code>withFixture</code>, and let it invoke the test function rather than invoking the test function directly. Instead of writing
 * &ldquo;<code>test()</code>&rdquo;, you should write &ldquo;<code>super.withFixture(test)</code>&rdquo;, like this:
 * </p>
 *
 * <pre class="stHighlight">
 * // Your implementation
 * override def withFixture(test: NoArgTest) = {
 *   // Perform setup
 *   try super.withFixture(test) // Invoke the test function
 *   finally {
 *     // Perform cleanup
 *   }
 * }
 * </pre>
 *
 * <p>
 * Here's an example in which <code>withFixture(NoArgTest)</code> is used to take a snapshot of the working directory if a test fails, and 
 * send that information to the reporter:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.wordspec.noargtest
 * 
 * import java.io.File
 * import org.scalatest._
 * 
 * class ExampleSpec extends WordSpec {
 * 
 *   override def withFixture(test: NoArgTest) = {
 * 
 *     super.withFixture(test) match {
 *       case failed: Failed =&gt;
 *         val currDir = new File(".")
 *         val fileNames = currDir.list()
 *         info("Dir snapshot: " + fileNames.mkString(", "))
 *         failed
 *       case other =&gt; other
 *     }
 *   }
 * 
 *   "This test" should {
 *     "succeed" in {
 *       assert(1 + 1 === 2)
 *     }
 * 
 *     "fail" in {
 *       assert(1 + 1 === 3)
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * Running this version of <code>ExampleSuite</code> in the interpreter in a directory with two files, <code>hello.txt</code> and <code>world.txt</code>
 * would give the following output:
 * </p>
 *
 * <pre class="stREPL">
 * scala&gt; new ExampleSuite execute
 * <span class="stGreen">ExampleSuite:
 * This test
 * - should succeed
 * <span class="stRed">- should fail *** FAILED ***
 *   2 did not equal 3 (<console>:33)
 *   + Dir snapshot: hello.txt, world.txt </span>
 * </pre>
 *
 * <p>
 * Note that the <a href="Suite$NoArgTest.html"><code>NoArgTest</code></a> passed to <code>withFixture</code>, in addition to
 * an <code>apply</code> method that executes the test, also includes the test name and the <a href="ConfigMap.html">config
 * map</a> passed to <code>runTest</code>. Thus you can also use the test name and configuration objects in your <code>withFixture</code>
 * implementation.
 * </p>
 *
 * <a name="loanFixtureMethods"></a>
 * <h4>Calling loan-fixture methods</h4>
 *
 * <p>
 * If you need to both pass a fixture object into a test <em>and</em> perform cleanup at the end of the test, you'll need to use the <em>loan pattern</em>.
 * If different tests need different fixtures that require cleanup, you can implement the loan pattern directly by writing <em>loan-fixture</em> methods.
 * A loan-fixture method takes a function whose body forms part or all of a test's code. It creates a fixture, passes it to the test code by invoking the
 * function, then cleans up the fixture after the function returns.
 * </p>
 *
 * <p>
 * The following example shows three tests that use two fixtures, a database and a file. Both require cleanup after, so each is provided via a
 * loan-fixture method. (In this example, the database is simulated with a <code>StringBuffer</code>.)
 * </p>
 *
 * <pre class="stHighlight">
 * import java.util.concurrent.ConcurrentHashMap
 * 
 * object DbServer { // Simulating a database server
 *   type Db = StringBuffer
 *   private val databases = new ConcurrentHashMap[String, Db]
 *   def createDb(name: String): Db = {
 *     val db = new StringBuffer
 *     databases.put(name, db)
 *     db
 *   }
 *   def removeDb(name: String) {
 *     databases.remove(name)
 *   }
 * }
 * 
 * import org.scalatest.WordSpec
 * import DbServer._
 * import java.util.UUID.randomUUID
 * import java.io._
 * 
 * class ExampleSpec extends WordSpec {
 * 
 *   def withDatabase(testCode: Db =&gt; Any) {
 *     val dbName = randomUUID.toString
 *     val db = createDb(dbName) // create the fixture
 *     try {
 *       db.append("ScalaTest is ") // perform setup
 *       testCode(db) // "loan" the fixture to the test
 *     }
 *     finally removeDb(dbName) // clean up the fixture
 *   }
 * 
 *   def withFile(testCode: (File, FileWriter) =&gt; Any) {
 *     val file = File.createTempFile("hello", "world") // create the fixture
 *     val writer = new FileWriter(file)
 *     try {
 *       writer.write("ScalaTest is ") // set up the fixture
 *       testCode(file, writer) // "loan" the fixture to the test
 *     }
 *     finally writer.close() // clean up the fixture
 *   }
 * 
 *   "Testing" should {
 *     // This test needs the file fixture
 *     "be productive" in withFile { (file, writer) =&gt;
 *       writer.write("productive!")
 *       writer.flush()
 *       assert(file.length === 24)
 *     }
 *   }
 *   
 *   "Test code" should {
 *     // This test needs the database fixture
 *     "be readable" in withDatabase { db =&gt;
 *       db.append("readable!")
 *       assert(db.toString === "ScalaTest is readable!")
 *     }
 * 
 *     // This test needs both the file and the database
 *     "be clear and concise" in withDatabase { db =&gt;
 *       withFile { (file, writer) =&gt; // loan-fixture methods compose
 *         db.append("clear!")
 *         writer.write("concise!")
 *         writer.flush()
 *         assert(db.toString === "ScalaTest is clear!")
 *         assert(file.length === 21)
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * As demonstrated by the last test, loan-fixture methods compose. Not only do loan-fixture methods allow you to
 * give each test the fixture it needs, they allow you to give a test multiple fixtures and clean everything up afterwards.
 * </p>
 *
 * <p>
 * Also demonstrated in this example is the technique of giving each test its own "fixture sandbox" to play in. When your fixtures
 * involve external side-effects, like creating files or databases, it is a good idea to give each file or database a unique name as is
 * done in this example. This keeps tests completely isolated, allowing you to run them in parallel if desired.
 * </p>
 *
 * </pre>
 * <a name="withFixtureOneArgTest"></a>
 * <h4>Overriding <code>withFixture(OneArgTest)</code></h4>
 *
 * <p>
 * If all or most tests need the same fixture, you can avoid some of the boilerplate of the loan-fixture method approach by using a <code>fixture.WordSpec</code>
 * and overriding <code>withFixture(OneArgTest)</code>.
 * Each test in a <code>fixture.WordSpec</code> takes a fixture as a parameter, allowing you to pass the fixture into
 * the test. You must indicate the type of the fixture parameter by specifying <code>FixtureParam</code>, and implement a
 * <code>withFixture</code> method that takes a <code>OneArgTest</code>. This <code>withFixture</code> method is responsible for
 * invoking the one-arg test function, so you can perform fixture set up before, and clean up after, invoking and passing
 * the fixture into the test function.
 * </p>
 *
 * <p>
 * To enable the stacking of traits that define <code>withFixture(NoArgTest)</code>, it is a good idea to let
 * <code>withFixture(NoArgTest)</code> invoke the test function instead of invoking the test
 * function directly. To do so, you'll need to convert the <code>OneArgTest</code> to a <code>NoArgTest</code>. You can do that by passing
 * the fixture object to the <code>toNoArgTest</code> method of <code>OneArgTest</code>. Instead of
 * writing &ldquo;<code>test(theFixture)</code>&rdquo;, you'd delegate responsibility for
 * invoking the test function to the <code>withFixture(NoArgTest)</code> method of the same instance by writing:
 * </p>
 *
 * <pre>
 * withFixture(test.toNoArgTest(theFixture))
 * </pre>
 *
 * <p>
 * Here's a complete example:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.wordspec.oneargtest
 * 
 * import org.scalatest.fixture
 * import java.io._
 * 
 * class ExampleSpec extends fixture.WordSpec {
 * 
 *   case class FixtureParam(file: File, writer: FileWriter)
 * 
 *   def withFixture(test: OneArgTest) = {
 *     val file = File.createTempFile("hello", "world") // create the fixture
 *     val writer = new FileWriter(file)
 *     val theFixture = FixtureParam(file, writer)
 * 
 *     try {
 *       writer.write("ScalaTest is ") // set up the fixture
 *       withFixture(test.toNoArgTest(theFixture)) // "loan" the fixture to the test
 *     }
 *     finally writer.close() // clean up the fixture
 *   }
 * 
 *   "Testing" should {
 *     "be easy" in { f =&gt;
 *       f.writer.write("easy!")
 *       f.writer.flush()
 *       assert(f.file.length === 18)
 *     }
 * 
 *     "be fun" in { f =&gt;
 *       f.writer.write("fun!")
 *       f.writer.flush()
 *       assert(f.file.length === 17)
 *     }
 *   } 
 * }
 * </pre>
 *
 * <p>
 * In this example, the tests actually required two fixture objects, a <code>File</code> and a <code>FileWriter</code>. In such situations you can
 * simply define the <code>FixtureParam</code> type to be a tuple containing the objects, or as is done in this example, a case class containing
 * the objects.  For more information on the <code>withFixture(OneArgTest)</code> technique, see the <a href="fixture/WordSpec.html">documentation for <code>fixture.WordSpec</code></a>.
 * </p>
 *
 * <a name="beforeAndAfter"></a>
 * <h4>Mixing in <code>BeforeAndAfter</code></h4>
 *
 * <p>
 * In all the shared fixture examples shown so far, the activities of creating, setting up, and cleaning up the fixture objects have been
 * performed <em>during</em> the test.  This means that if an exception occurs during any of these activities, it will be reported as a test failure.
 * Sometimes, however, you may want setup to happen <em>before</em> the test starts, and cleanup <em>after</em> the test has completed, so that if an
 * exception occurs during setup or cleanup, the entire suite aborts and no more tests are attempted. The simplest way to accomplish this in ScalaTest is
 * to mix in trait <a href="BeforeAndAfter.html"><code>BeforeAndAfter</code></a>.  With this trait you can denote a bit of code to run before each test
 * with <code>before</code> and/or after each test each test with <code>after</code>, like this:
 * </p>
 * 
 * <pre class="stHighlight">
 * package org.scalatest.examples.wordspec.beforeandafter
 * 
 * import org.scalatest.WordSpec
 * import org.scalatest.BeforeAndAfter
 * import collection.mutable.ListBuffer
 * 
 * class ExampleSpec extends WordSpec with BeforeAndAfter {
 * 
 *   val builder = new StringBuilder
 *   val buffer = new ListBuffer[String]
 * 
 *   before {
 *     builder.append("ScalaTest is ")
 *   }
 * 
 *   after {
 *     builder.clear()
 *     buffer.clear()
 *   }
 * 
 *   "Testing" should {
 *     "be easy" in {
 *       builder.append("easy!")
 *       assert(builder.toString === "ScalaTest is easy!")
 *       assert(buffer.isEmpty)
 *       buffer += "sweet"
 *     }
 * 
 *     "be fun" in {
 *       builder.append("fun!")
 *       assert(builder.toString === "ScalaTest is fun!")
 *       assert(buffer.isEmpty)
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * Note that the only way <code>before</code> and <code>after</code> code can communicate with test code is via some side-effecting mechanism, commonly by
 * reassigning instance <code>var</code>s or by changing the state of mutable objects held from instance <code>val</code>s (as in this example). If using
 * instance <code>var</code>s or mutable objects held from instance <code>val</code>s you wouldn't be able to run tests in parallel in the same instance
 * of the test class unless you synchronized access to the shared, mutable state. This is why ScalaTest's <code>ParallelTestExecution</code> trait extends
 * <a href="OneInstancePerTest.html"><code>OneInstancePerTest</code></a>. By running each test in its own instance of the class, each test has its own copy of the instance variables, so you
 * don't need to synchronize. If you mixed <code>ParallelTestExecution</code> into the <code>ExampleSuite</code> above, the tests would run in parallel just fine
 * without any synchronization needed on the mutable <code>StringBuilder</code> and <code>ListBuffer[String]</code> objects.
 * </p>
 *
 * <p>
 * Although <code>BeforeAndAfter</code> provides a minimal-boilerplate way to execute code before and after tests, it isn't designed to enable stackable
 * traits, because the order of execution would be non-obvious.  If you want to factor out before and after code that is common to multiple test suites, you 
 * should use trait <code>BeforeAndAfterEach</code> instead, as shown later in the next section,
 * <a href="#composingFixtures.html">composing fixtures by stacking traits</a>.
 * </p>
 *
 * <a name="composingFixtures"></a><h2>Composing fixtures by stacking traits</h2>
 *
 * <p>
 * In larger projects, teams often end up with several different fixtures that test classes need in different combinations,
 * and possibly initialized (and cleaned up) in different orders. A good way to accomplish this in ScalaTest is to factor the individual
 * fixtures into traits that can be composed using the <em>stackable trait</em> pattern. This can be done, for example, by placing
 * <code>withFixture</code> methods in several traits, each of which call <code>super.withFixture</code>. Here's an example in
 * which the <code>StringBuilder</code> and <code>ListBuffer[String]</code> fixtures used in the previous examples have been
 * factored out into two <em>stackable fixture traits</em> named <code>Builder</code> and <code>Buffer</code>:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.wordspec.composingwithfixture
 * 
 * import org.scalatest._
 * import collection.mutable.ListBuffer
 * 
 * trait Builder extends SuiteMixin { this: Suite =&gt;
 * 
 *   val builder = new StringBuilder
 * 
 *   abstract override def withFixture(test: NoArgTest) = {
 *     builder.append("ScalaTest is ")
 *     try super.withFixture(test) // To be stackable, must call super.withFixture
 *     finally builder.clear()
 *   }
 * }
 * 
 * trait Buffer extends SuiteMixin { this: Suite =&gt;
 * 
 *   val buffer = new ListBuffer[String]
 * 
 *   abstract override def withFixture(test: NoArgTest) = {
 *     try super.withFixture(test) // To be stackable, must call super.withFixture
 *     finally buffer.clear()
 *   }
 * }
 * 
 * class ExampleSpec extends WordSpec with Builder with Buffer {
 * 
 *   "Testing" should {
 *     "be easy" in {
 *       builder.append("easy!")
 *       assert(builder.toString === "ScalaTest is easy!")
 *       assert(buffer.isEmpty)
 *       buffer += "sweet"
 *     }
 * 
 *     "be fun" in {
 *       builder.append("fun!")
 *       assert(builder.toString === "ScalaTest is fun!")
 *       assert(buffer.isEmpty)
 *       buffer += "clear"
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * By mixing in both the <code>Builder</code> and <code>Buffer</code> traits, <code>ExampleSpec</code> gets both fixtures, which will be
 * initialized before each test and cleaned up after. The order the traits are mixed together determines the order of execution.
 * In this case, <code>Builder</code> is &ldquo;super&rdquo; to <code>Buffer</code>. If you wanted <code>Buffer</code> to be &ldquo;super&rdquo;
 * to <code>Builder</code>, you need only switch the order you mix them together, like this: 
 * </p>
 *
 * <pre class="stHighlight">
 * class Example2Suite extends Suite with Buffer with Builder
 * </pre>
 *
 * <p>
 * And if you only need one fixture you mix in only that trait:
 * </p>
 *
 * <pre class="stHighlight">
 * class Example3Suite extends Suite with Builder
 * </pre>
 *
 * <p>
 * Another way to create stackable fixture traits is by extending the <a href="BeforeAndAfterEach.html"><code>BeforeAndAfterEach</code></a>
 * and/or <a href="BeforeAndAfterAll.html"><code>BeforeAndAfterAll</code></a> traits.
 * <code>BeforeAndAfterEach</code> has a <code>beforeEach</code> method that will be run before each test (like JUnit's <code>setUp</code>),
 * and an <code>afterEach</code> method that will be run after (like JUnit's <code>tearDown</code>).
 * Similarly, <code>BeforeAndAfterAll</code> has a <code>beforeAll</code> method that will be run before all tests,
 * and an <code>afterAll</code> method that will be run after all tests. Here's what the previously shown example would look like if it
 * were rewritten to use the <code>BeforeAndAfterEach</code> methods instead of <code>withFixture</code>:
 * </p>
 *
 * <pre class="stHighlight">
 * package org.scalatest.examples.wordspec.composingbeforeandaftereach
 * 
 * import org.scalatest._
 * import org.scalatest.BeforeAndAfterEach
 * import collection.mutable.ListBuffer
 * 
 * trait Builder extends BeforeAndAfterEach { this: Suite =&gt;
 * 
 *   val builder = new StringBuilder
 * 
 *   override def beforeEach() {
 *     builder.append("ScalaTest is ")
 *     super.beforeEach() // To be stackable, must call super.beforeEach
 *   }
 * 
 *   override def afterEach() {
 *     try super.afterEach() // To be stackable, must call super.afterEach
 *     finally builder.clear()
 *   }
 * }
 * 
 * trait Buffer extends BeforeAndAfterEach { this: Suite =&gt;
 * 
 *   val buffer = new ListBuffer[String]
 * 
 *   override def afterEach() {
 *     try super.afterEach() // To be stackable, must call super.afterEach
 *     finally buffer.clear()
 *   }
 * }
 * 
 * class ExampleSpec extends WordSpec with Builder with Buffer {
 * 
 *   "Testing" should {
 *     "be easy" in {
 *       builder.append("easy!")
 *       assert(builder.toString === "ScalaTest is easy!")
 *       assert(buffer.isEmpty)
 *       buffer += "sweet"
 *     }
 * 
 *     "be fun" in {
 *       builder.append("fun!")
 *       assert(builder.toString === "ScalaTest is fun!")
 *       assert(buffer.isEmpty)
 *       buffer += "clear"
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * To get the same ordering as <code>withFixture</code>, place your <code>super.beforeEach</code> call at the end of each
 * <code>beforeEach</code> method, and the <code>super.afterEach</code> call at the beginning of each <code>afterEach</code>
 * method, as shown in the previous example. It is a good idea to invoke <code>super.afterEach</code> in a <code>try</code>
 * block and perform cleanup in a <code>finally</code> clause, as shown in the previous example, because this ensures the
 * cleanup code is performed even if <code>super.afterEach</code> throws an exception.
 * </p>
 *
 * <p>
 * The difference between stacking traits that extend <code>BeforeAndAfterEach</code> versus traits that implement <code>withFixture</code> is
 * that setup and cleanup code happens before and after the test in <code>BeforeAndAfterEach</code>, but at the beginning and
 * end of the test in <code>withFixture</code>. Thus if a <code>withFixture</code> method completes abruptly with an exception, it is
 * considered a failed test. By contrast, if any of the <code>beforeEach</code> or <code>afterEach</code> methods of <code>BeforeAndAfterEach</code> 
 * complete abruptly, it is considered an aborted suite, which will result in a <a href="events/SuiteAborted.html"><code>SuiteAborted</code></a> event.
 * </p>
 * 
 * <a name="sharedTests"></a><h2>Shared tests</h2>
 *
 * <p>
 * Sometimes you may want to run the same test code on different fixture objects. In other words, you may want to write tests that are "shared"
 * by different fixture objects.  To accomplish this in a <code>WordSpec</code>, you first place shared tests in <em>behavior functions</em>.
 * These behavior functions will be invoked during the construction phase of any <code>WordSpec</code> that uses them, so that the tests they
 * contain will be registered as tests in that <code>WordSpec</code>.  For example, given this stack class:
 * </p>
 *
 * <pre class="stHighlight">
 * import scala.collection.mutable.ListBuffer
 * 
 * class Stack[T] {
 *
 *   val MAX = 10
 *   private val buf = new ListBuffer[T]
 *
 *   def push(o: T) {
 *     if (!full)
 *       buf.prepend(o)
 *     else
 *       throw new IllegalStateException("can't push onto a full stack")
 *   }
 *
 *   def pop(): T = {
 *     if (!empty)
 *       buf.remove(0)
 *     else
 *       throw new IllegalStateException("can't pop an empty stack")
 *   }
 *
 *   def peek: T = {
 *     if (!empty)
 *       buf(0)
 *     else
 *       throw new IllegalStateException("can't pop an empty stack")
 *   }
 *
 *   def full: Boolean = buf.size == MAX
 *   def empty: Boolean = buf.size == 0
 *   def size = buf.size
 *
 *   override def toString = buf.mkString("Stack(", ", ", ")")
 * }
 * </pre>
 *
 * <p>
 * You may want to test the <code>Stack</code> class in different states: empty, full, with one item, with one item less than capacity,
 * <em>etc</em>. You may find you have several tests that make sense any time the stack is non-empty. Thus you'd ideally want to run
 * those same tests for three stack fixture objects: a full stack, a stack with a one item, and a stack with one item less than
 * capacity. With shared tests, you can factor these tests out into a behavior function, into which you pass the
 * stack fixture to use when running the tests. So in your <code>WordSpec</code> for stack, you'd invoke the
 * behavior function three times, passing in each of the three stack fixtures so that the shared tests are run for all three fixtures. You
 * can define a behavior function that encapsulates these shared tests inside the <code>WordSpec</code> that uses them. If they are shared
 * between different <code>WordSpec</code>s, however, you could also define them in a separate trait that is mixed into each <code>WordSpec</code>
 * that uses them.
 * </p>
 *
 * <p>
 * <a name="StackBehaviors">For</a> example, here the <code>nonEmptyStack</code> behavior function (in this case, a behavior <em>method</em>) is
 * defined in a trait along with another method containing shared tests for non-full stacks:
 * </p>
 * 
 * <pre class="stHighlight">
 * trait StackBehaviors { this: WordSpec =&gt;
 * 
 *   def nonEmptyStack(newStack: =&gt; Stack[Int], lastItemAdded: Int) {
 *
 *     "be non-empty" in {
 *       assert(!newStack.empty)
 *     }
 *
 *     "return the top item on peek" in {
 *       assert(newStack.peek === lastItemAdded)
 *     }
 *
 *     "not remove the top item on peek" in {
 *       val stack = newStack
 *       val size = stack.size
 *       assert(stack.peek === lastItemAdded)
 *       assert(stack.size === size)
 *     }
 *
 *     "remove the top item on pop" in {
 *       val stack = newStack
 *       val size = stack.size
 *       assert(stack.pop === lastItemAdded)
 *       assert(stack.size === size - 1)
 *     }
 *   }
 *
 *   def nonFullStack(newStack: =&gt; Stack[Int]) {
 *
 *     "not be full" in {
 *       assert(!newStack.full)
 *     }
 *
 *     "add to the top on push" in {
 *       val stack = newStack
 *       val size = stack.size
 *       stack.push(7)
 *       assert(stack.size === size + 1)
 *       assert(stack.peek === 7)
 *     }
 *   }
 * }
 * </pre>
 *
 *
 * <p>
 * Given these behavior functions, you could invoke them directly, but <code>WordSpec</code> offers a DSL for the purpose,
 * which looks like this:
 * </p>
 *
 * <pre class="stHighlight">
 * behave like nonEmptyStack(stackWithOneItem, lastValuePushed)
 * behave like nonFullStack(stackWithOneItem)
 * </pre>
 *
 * <p>
 * If you prefer to use an imperative style to change fixtures, for example by mixing in <code>BeforeAndAfterEach</code> and
 * reassigning a <code>stack</code> <code>var</code> in <code>beforeEach</code>, you could write your behavior functions
 * in the context of that <code>var</code>, which means you wouldn't need to pass in the stack fixture because it would be
 * in scope already inside the behavior function. In that case, your code would look like this:
 * </p>
 *
 * <pre class="stHighlight">
 * behave like nonEmptyStack // assuming lastValuePushed is also in scope inside nonEmptyStack
 * behave like nonFullStack
 * </pre>
 *
 * <p>
 * The recommended style, however, is the functional, pass-all-the-needed-values-in style. Here's an example:
 * </p>
 *
 * <pre class="stHighlight">
 * class SharedTestExampleSpec extends WordSpec with StackBehaviors {
 * 
 *   // Stack fixture creation methods
 *   def emptyStack = new Stack[Int]
 * 
 *   def fullStack = {
 *     val stack = new Stack[Int]
 *     for (i <- 0 until stack.MAX)
 *       stack.push(i)
 *     stack
 *   }
 * 
 *   def stackWithOneItem = {
 *     val stack = new Stack[Int]
 *     stack.push(9)
 *     stack
 *   }
 * 
 *   def stackWithOneItemLessThanCapacity = {
 *     val stack = new Stack[Int]
 *     for (i &lt;- 1 to 9)
 *       stack.push(i)
 *     stack
 *   }
 * 
 *   val lastValuePushed = 9
 * 
 *   "A Stack" when {
 *     "empty" should {
 *       "be empty" in {
 *         assert(emptyStack.empty)
 *       }
 * 
 *       "complain on peek" in {
 *         intercept[IllegalStateException] {
 *           emptyStack.peek
 *         }
 *       }
 *
 *       "complain on pop" in {
 *         intercept[IllegalStateException] {
 *           emptyStack.pop
 *         }
 *       }
 *     }
 * 
 *     "it contains one item" should {
 *       behave like nonEmptyStack(stackWithOneItem, lastValuePushed)
 *       behave like nonFullStack(stackWithOneItem)
 *     }
 *     
 *     "it contains one item less than capacity" should {
 *       behave like nonEmptyStack(stackWithOneItemLessThanCapacity, lastValuePushed)
 *       behave like nonFullStack(stackWithOneItemLessThanCapacity)
 *     }
 * 
 *     "full" should {
 *       "be full" in {
 *         assert(fullStack.full)
 *       }
 * 
 *       behave like nonEmptyStack(fullStack, lastValuePushed)
 * 
 *       "complain on a push" in {
 *         intercept[IllegalStateException] {
 *           fullStack.push(10)
 *         }
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * If you load these classes into the Scala interpreter (with scalatest's JAR file on the class path), and execute it,
 * you'll see:
 * </p>
 *
 * <pre class="stREPL">
 * scala&gt; new SharedTestExampleSpec execute
 * <span class="stGreen">SharedTestExampleSpec:
 * A Stack
 *   when empty
 * &nbsp; - should be empty
 * &nbsp; - should complain on peek
 * &nbsp; - should complain on pop
 * &nbsp; when it contains one item
 * &nbsp; - should be non-empty
 * &nbsp; - should return the top item on peek
 * &nbsp; - should not remove the top item on peek
 * &nbsp; - should remove the top item on pop
 * &nbsp; - should not be full
 * &nbsp; - should add to the top on push
 * &nbsp; when it contains one item less than capacity
 * &nbsp; - should be non-empty
 * &nbsp; - should return the top item on peek
 * &nbsp; - should not remove the top item on peek
 * &nbsp; - should remove the top item on pop
 * &nbsp; - should not be full
 * &nbsp; - should add to the top on push
 * &nbsp; when full
 * &nbsp; - should be full
 * &nbsp; - should be non-empty
 * &nbsp; - should return the top item on peek
 * &nbsp; - should not remove the top item on peek
 * &nbsp; - should remove the top item on pop
 * &nbsp; - should complain on a push</span>
 * </pre>
 * 
 * <p>
 * One thing to keep in mind when using shared tests is that in ScalaTest, each test in a suite must have a unique name.
 * If you register the same tests repeatedly in the same suite, one problem you may encounter is an exception at runtime
 * complaining that multiple tests are being registered with the same test name. A good way to solve this problem in a <code>WordSpec</code> is to make sure
 * each invocation of a behavior function is in the context of a different surrounding <code>when</code>, 
 * <code>should</code>/<code>must</code>/<code>can</code>, or <code>which</code> clause, because a test's name is the concatenation of its
 * surrounding clauses and after words, followed by the "spec text".
 * For example, the following code in a <code>WordSpec</code> would register a test with the name <code>"A Stack when empty should be empty"</code>:
 * </p>
 *
 * <pre class="stHighlight">
 * "A Stack" when {
 *   "empty" should {
 *     "be empty" in {
 *       assert(emptyStack.empty)
 *     }
 *   }
 * }
 * // ...
 * </pre>
 *
 * <p>
 * If the <code>"be empty"</code> test was factored out into a behavior function, it could be called repeatedly so long
 * as each invocation of the behavior function is in the context of a different surrounding <code>when</code> clauses.
 * </p>
 *
 * @author Bill Venners
 */
@Finders(Array("org.scalatest.finders.WordSpecFinder"))
class WordSpec extends WordSpecLike with ClassicTests {

  /**
   * Returns a user friendly string for this suite, composed of the
   * simple name of the class (possibly simplified further by removing dollar signs if added by the Scala interpeter) and, if this suite
   * contains nested suites, the result of invoking <code>toString</code> on each
   * of the nested suites, separated by commas and surrounded by parentheses.
   *
   * @return a user-friendly string for this suite
   */
  override def toString: String = Suite.suiteToString(None, this)
}
