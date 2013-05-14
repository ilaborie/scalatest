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

import org.scalautils.Equality
import org.scalautils.StringNormalizations._
import SharedHelpers._

class OptionShouldContainOneOfSpec extends Spec with Matchers {

  object `an Option` {

    val fumSome: Option[String] = Some("fum")
    val toSome: Option[String] = Some("to")

    object `when used with contain oneOf (...) syntax` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        fumSome should newContain newOneOf ("fee", "fie", "foe", "fum")
        val e1 = intercept[TestFailedException] {
          fumSome should newContain newOneOf ("happy", "birthday", "to", "you")
        }
        e1.failedCodeFileName.get should be ("OptionShouldContainOneOfSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message.get should be (Resources("didNotContainOneOfElements", fumSome, "\"happy\", \"birthday\", \"to\", \"you\""))
      }
      def `should use the implicit Equality in scope` {
        implicit val e = new Equality[String] {
          def areEqual(a: String, b: Any): Boolean = a != b
        }
        fumSome should newContain newOneOf ("happy", "birthday", "to", "you")
        intercept[TestFailedException] {
          fumSome should newContain newOneOf ("fum", "fum", "fum", "fum")
        }
      }
      def `should use an explicitly provided Equality` {
        val invertedEquality = new Equality[String] {
          def areEqual(a: String, b: Any): Boolean = a != b
        }
        (fumSome should newContain newOneOf ("happy", "birthday", "to", "you")) (decided by invertedEquality)
        intercept[TestFailedException] {
          (fumSome should newContain newOneOf ("fum", "fum", "fum", "fum")) (decided by invertedEquality)
        }
        intercept[TestFailedException] {
          fumSome should newContain newOneOf (" FEE ", " FIE ", " FOE ", " FUM ")
        }
        (fumSome should newContain newOneOf (" FEE ", " FIE ", " FOE ", " FUM ")) (after being lowerCased and trimmed)
      }
    }

    object `when used with (contain oneOf (...)) syntax` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {

        fumSome should (newContain newOneOf ("fee", "fie", "foe", "fum"))
        val e1 = intercept[TestFailedException] {
          fumSome should (newContain newOneOf ("happy", "birthday", "to", "you"))
        }
        e1.failedCodeFileName.get should be ("OptionShouldContainOneOfSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message.get should be (Resources("didNotContainOneOfElements", fumSome, "\"happy\", \"birthday\", \"to\", \"you\""))
      }
      def `should use the implicit Equality in scope` {
        implicit val e = new Equality[String] {
          def areEqual(a: String, b: Any): Boolean = a != b
        }
        fumSome should (newContain newOneOf ("happy", "birthday", "to", "you"))
        intercept[TestFailedException] {
          fumSome should (newContain newOneOf ("fum", "fum", "fum", "fum"))
        }
      }
      def `should use an explicitly provided Equality` {
        val invertedEquality = new Equality[String] {
          def areEqual(a: String, b: Any): Boolean = a != b
        }
        (fumSome should (newContain newOneOf ("happy", "birthday", "to", "you"))) (decided by invertedEquality)
        intercept[TestFailedException] {
          (fumSome should (newContain newOneOf ("fum", "fum", "fum", "fum"))) (decided by invertedEquality)
        }
        intercept[TestFailedException] {
          fumSome should (newContain newOneOf (" FEE ", " FIE ", " FOE ", " FUM "))
        }
        (fumSome should (newContain newOneOf (" FEE ", " FIE ", " FOE ", " FUM "))) (after being lowerCased and trimmed)
      }
    }

/*
 I purposely don't want to support this syntax:

        fumSome should newContain (newOneOf ("fee", "fie", "foe", "fum"))
        fumSome should (newContain (newOneOf ("fee", "fie", "foe", "fum")))

 Reason is that I don't want people putting parentheses between contain and oneOf, etc. This will not compile.
*/
    object `when used with not contain oneOf (...) syntax` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        toSome should not newContain newOneOf ("fee", "fie", "foe", "fum")
        val e1 = intercept[TestFailedException] {
          toSome should not newContain newOneOf ("happy", "birthday", "to", "you")
        }
        e1.failedCodeFileName.get should be ("OptionShouldContainOneOfSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message.get should be (Resources("containedOneOfElements", toSome, "\"happy\", \"birthday\", \"to\", \"you\""))
      }
      def `should use the implicit Equality in scope` {
        implicit val e = new Equality[String] {
          def areEqual(a: String, b: Any): Boolean = a != b
        }
        toSome should not newContain newOneOf ("to", "to", "to", "to")
        intercept[TestFailedException] {
          toSome should not newContain newOneOf ("fee", "fie", "foe", "fum")
        }
      }
      def `should use an explicitly provided Equality` {
        val invertedEquality = new Equality[String] {
          def areEqual(a: String, b: Any): Boolean = a != b
        }
        (toSome should not newContain newOneOf ("to", "to", "to", "to")) (decided by invertedEquality)
        intercept[TestFailedException] {
          (toSome should not newContain newOneOf ("fee", "fie", "foe", "fum")) (decided by invertedEquality)
        }
        toSome should not newContain newOneOf (" TO ", " TO ", " TO ", " TO ")
        intercept[TestFailedException] {
          (toSome should not newContain newOneOf (" TO ", " TO ", " TO ", " TO ")) (after being lowerCased and trimmed)
        }
      }
    }

/*
Interesting, of these three, the top one does happen to compile and run:

        toSome should not newContain (newOneOf ("fee", "fie", "foe", "fum"))
        // toSome should not (newContain (newOneOf ("fee", "fie", "foe", "fum")))
        // toSome should (not (newContain (newOneOf ("fee", "fie", "foe", "fum"))))

The bottom two don't, but still I don't want to support that in general.
*/
    object `when used with (not contain oneOf (...)) syntax` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        toSome should (not newContain newOneOf ("fee", "fie", "foe", "fum"))
        val e1 = intercept[TestFailedException] {
          toSome should (not newContain newOneOf ("happy", "birthday", "to", "you"))
        }
        e1.failedCodeFileName.get should be ("OptionShouldContainOneOfSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message.get should be (Resources("containedOneOfElements", toSome, "\"happy\", \"birthday\", \"to\", \"you\""))
      }
      def `should use the implicit Equality in scope` {
        implicit val e = new Equality[String] {
          def areEqual(a: String, b: Any): Boolean = a != b
        }
        toSome should (not newContain newOneOf ("to", "to", "to", "to"))
        intercept[TestFailedException] {
          toSome should (not newContain newOneOf ("fee", "fie", "foe", "fum"))
        }
      }
      def `should use an explicitly provided Equality` {
        val invertedEquality = new Equality[String] {
          def areEqual(a: String, b: Any): Boolean = a != b
        }
        (toSome should (not newContain newOneOf ("to", "to", "to", "to"))) (decided by invertedEquality)
        intercept[TestFailedException] {
          (toSome should (not newContain newOneOf ("fee", "fie", "foe", "fum"))) (decided by invertedEquality)
        }
        toSome should (not newContain newOneOf (" TO ", " TO ", " TO ", " TO "))
        intercept[TestFailedException] {
          (toSome should (not newContain newOneOf (" TO ", " TO ", " TO ", " TO "))) (after being lowerCased and trimmed)
        }
      }
    }
  }
  object `a collection of Options` {

    val some1s: Vector[Option[Int]] = Vector(Some(1), Some(1), Some(1))
    val somes: Vector[Option[Int]] = Vector(Some(1), Some(1), Some(2))
    val nones: Vector[Option[Int]] = Vector(None, None, None)
    val somesNone: Vector[Option[Int]] = Vector(Some(1), Some(1), None)
    val hiSomes: Vector[Option[String]] = Vector(Some("hi"), Some("hi"), Some("hi"))

    object `when used with contain oneOf (...) syntax` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        pending
/*
        all (some1s) should newContain newOneOf (1)
        atLeast (2, somes) should newContain newOneOf (1)
        atMost (2, somes) should newContain newOneOf (2)
        no (somes) should newContain newOneOf (3)
        no (nones) should newContain newOneOf (1)
        no (somesNone) should newContain newOneOf (3)

        val e1 = intercept[TestFailedException] {
          all (somes) should newContain newOneOf (1)
        }
        e1.failedCodeFileName.get should be ("OptionShouldContainSpec.scala")
        e1.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e1.message should be (Some("'all' inspection failed, because: \n" +
                                   "  at index 2, Some(2) did not contain element 1 (OptionShouldContainSpec.scala:" + (thisLineNumber - 5) + ") \n" +
                                   "in Vector(Some(1), Some(1), Some(2))"))

        val e2 = intercept[TestFailedException] {
          all (nones) should newContain newOneOf ("ho")
        }
        e2.failedCodeFileName.get should be ("OptionShouldContainSpec.scala")
        e2.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e2.message should be (Some("'all' inspection failed, because: \n" +
                                   "  at index 0, None did not contain element \"ho\" (OptionShouldContainSpec.scala:" + (thisLineNumber - 5) + ") \n" +
                                   "in Vector(None, None, None)"))

        val e4 = intercept[TestFailedException] {
          all (somes) should newContain newOneOf (1)
        }
        e4.failedCodeFileName.get should be ("OptionShouldContainSpec.scala")
        e4.failedCodeLineNumber.get should be (thisLineNumber - 3)
        e4.message should be (Some("'all' inspection failed, because: \n" +
                                   "  at index 2, Some(2) did not contain element 1 (OptionShouldContainSpec.scala:" + (thisLineNumber - 5) + ") \n" +
                                   "in Vector(Some(1), Some(1), Some(2))"))
*/
      }

      def `should use the implicit Equality in scope` {
        pending
      }
      def `should use an explicitly provided Equality` {
        pending
      }
    }

    object `when used with (contain oneOf (...)) syntax` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        pending
      }
      def `should use the implicit Equality in scope` {
        pending
      }
      def `should use an explicitly provided Equality` {
        pending
      }
    }

/*
 I purposely don't want to support this syntax:

        fumSome should newContain (newOneOf ("fee", "fie", "foe", "fum"))
        fumSome should (newContain (newOneOf ("fee", "fie", "foe", "fum")))

 Reason is that I don't want people putting parentheses between contain and oneOf, etc. This will not compile.
*/
    object `when used with not contain oneOf (...) syntax` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        pending
      }
      def `should use the implicit Equality in scope` {
        pending
      }
      def `should use an explicitly provided Equality` {
        pending
      }
    }

/*
Interesting, of these three, the top one does happen to compile and run:

        toSome should not newContain (newOneOf ("fee", "fie", "foe", "fum"))
        // toSome should not (newContain (newOneOf ("fee", "fie", "foe", "fum")))
        // toSome should (not (newContain (newOneOf ("fee", "fie", "foe", "fum"))))

The bottom two don't, but still I don't want to support that in general.
*/
    object `when used with (not contain oneOf (...)) syntax` {

      def `should do nothing if valid, else throw a TFE with an appropriate error message` {
        pending
      }
      def `should use the implicit Equality in scope` {
        pending
      }
      def `should use an explicitly provided Equality` {
        pending
      }
    }
  }
}
