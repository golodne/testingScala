package simpleTest

import org.scalatest.{FunSpec, ShouldMatchers}

class ShouldMatcherSpec extends FunSpec with ShouldMatchers {
  describe("other test") {
    it ("test list size") {
      val l1  = 2 :: 4 :: 6 :: Nil
      l1.size should be (3)
     // l1.size should equal(4) //for custom equal
    }
  }

  describe("work with string") {
    it ("test string") {
      val s = """I fell into a burning ring of fire.
                 I went down, down, down and the flames went higher"""

      s should startWith("I fell")
      s should endWith("higher")
      s should not endWith "my Internet is fast"
      s should include("down, down, down")
      s should not include("bolls down")
      s should startWith regex("I.fel+")
      s should endWith regex("h.{4}r")
      s should not endWith regex("\\d{5}")
      s should include regex("flames?")
      //s should fullyMatch regex ("""I(.|\n|\S)*higher""")
    }

    it ("test with exp") {
      (0.9 - 0.8) should be (0.1 +-(0.1))
      List.empty should be('empty)
      1 :: 2 :: 7 :: Nil should contain(7)
      (1 to 5) should have size(5)
    }

    it ("Map matchers") {
      val map = Map("Jimmy Page" -> "Led Zeppelin", "Sting" -> "The Police",
        "Aimee Mann" -> "Til\' Tuesday")

      map should contain key("Sting")
      map should contain value("The Police")
      map should not contain key("Beatles")
    }

    it ("Compound matchers") {
      val list1 = List("One","Two","Three")
      list1 should ((contain("Two")) and (not contain("Ten")))
    }

    it ("Property matchers") {

      val album = new Album("Blizzard of Ozz", 1980, new Artist("Ozzy", "Osbourne"))

      album match {
        case Album(t,y,a) =>
          t should be ("Blizzard of Ozz")
          y should be (1980)
          a should have (
            'firstName ("Ozzy"),
            'lastName ("Osbourne")
          )
      }
    }
  }

  describe("status code") {
    it ("test status site") {
      val status = 42

      status should be < (50)
      status should not be > (50)
      status should be <= (100)
      status should === (42)
      status should not equal (400)
    }
  }
}
