package simpleTest

import org.scalatest.{FunSpec, ShouldMatchers}

  class AlbumTest extends FunSpec with ShouldMatchers {

  describe("An Album") {
    it ("can add an Artist object to the album") {
      val album = new Album("Spring",1981,new Artist("Michael","Jackson"))

      album.artist.firstName should be ("Michael")

    }
  }
}
