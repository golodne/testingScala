package simpleTest

import org.scalatest.{FunSpec, MustMatchers}

class MustMatchersSpec  extends FunSpec with MustMatchers  {
  describe ("test1") {
    it ("work with list") {
      val list1 = 1 :: 2 :: 3 :: Nil
      list1.size must be (3)

      val answerToLife = 42
      answerToLife must be < (50)

    }

    it ("test2") {

    }
  }


}
