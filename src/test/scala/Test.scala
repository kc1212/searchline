import org.scalatest.FlatSpec

import scala.language.implicitConversions

class Test extends FlatSpec {

  private def permute(xs: List[String]): List[List[String]] =
    xs.scanLeft(List(): List[String])((b, p) => p :: b)
    .tail.tail.map(_.reverse)

  private val short = List("3", "5", "7", "9")
  private val shortPermutations = permute(short)
  private val long = short.map(_ * 200)
  private val longPermutations = permute(long)

  implicit def list2str(xs: List[String]): String = {
    xs.mkString("\n")
  }

  "finding the current line" should "succeed" in {
    val lines1 = List("aaa", "b")
    val iter = new ByteIterator(lines1)
    assert(Main.findCurrLine(iter) == 0)
  }

  "finding the first line" should "succeed" in {
    Seq(shortPermutations, longPermutations).foreach(permutations => {
      permutations.foreach { x =>
        val iter = new ByteIterator(x)
        assert(Main.readLine(iter) == x.head)
      }
    })
  }

  "finding the last line" should "succeed" in {
    Seq(shortPermutations, longPermutations).foreach(permutations => {
      permutations.foreach { x =>
        val iter = new ByteIterator(x)
        val lastIdx = Main.findLastLine(iter)
        assert(Main.readLine(iter.seek(lastIdx)) == x.last)
      }
    })
  }

  "search" should "succeed for elemts in the list" in {
    Seq(shortPermutations, longPermutations).foreach(permutations => {
      val pairs = for (
        xs <- permutations;
        key <- xs
      ) yield(xs, key)
      pairs.foreach { case (xs, key) =>
        println(xs, key)
        val iter = new ByteIterator(xs)
        assert(Main.binarySearch(iter, key))
      }
    })
  }
}
