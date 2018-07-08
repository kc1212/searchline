import org.scalacheck.Gen
import org.scalatest.{Matchers, PropSpec}
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class LindIndexTest extends PropSpec with GeneratorDrivenPropertyChecks with Matchers {
  property("index should start at line") {
    val stringlists = Gen.listOf(Gen.alphaNumStr)
    forAll (stringlists) { s: List[String] =>
      val iter = new ByteDoubleIterator(s.mkString("\n").getBytes)
      val idx = LineIndex.newIndex(iter)
      s.size.shouldEqual(idx.size)
      s.zip(idx).foreach { case (line, i) =>
        line.shouldEqual(SearchLine.readLine(iter.seek(i)))
      }
    }
  }
}
