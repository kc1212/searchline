import java.nio.file.{Files, StandardOpenOption}
import java.util

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

  "seeking in double iterator" should "suceed" in {
    val strs = ('a' to 'z').mkString
    Seq(strs * 10, strs * 100, strs * 1000).foreach(s => {
      val tempFile = Files.createTempFile("searchline", ".tmp")
      Files.write(tempFile, s.getBytes)
      val chan = Files.newByteChannel(tempFile, util.EnumSet.of(StandardOpenOption.READ))
      val chanIter = new ChanDoubleIterator(chan)
      val byteIter = new ByteDoubleIterator(s) // TODO add
      Seq(chanIter, byteIter).foreach( iter => {
        assert(iter.next() == s.head)
        assert(iter.prev() == s.head)
        assert(!iter.hasPrev)
        iter.seek(s.length - 1)
        assert(iter.hasNext)
        assert(iter.next() == s.last)
        assert(!iter.hasNext)
        assert(iter.hasPrev)
        assert(iter.prev() == s.last)
        // iterate over the whole list backwards and forwards
        iter.seek(0)
        var ctr = 0
        while (iter.hasNext) {
          assert(iter.next() == s(ctr))
          ctr += 1
          assert(iter.position == ctr)
        }
        while (iter.hasPrev) {
          ctr -= 1
          assert(iter.prev() == s(ctr))
          assert(iter.position == ctr)
        }
      })
    })
  }

  "finding the current line" should "succeed" in {
    val lines1 = List("aaa", "b")
    val iter = new ByteDoubleIterator(lines1)
    assert(Main.findCurrLine(iter) == 0)

    iter.seek(1)
    assert(Main.findCurrLine(iter) == 0)

    iter.seek(lines1.flatten.size)
    iter.seek(Main.findCurrLine(iter))
    assert(Main.readLine(iter) == lines1(1))
  }

  "finding the first line" should "succeed" in {
    Seq(shortPermutations, longPermutations).foreach(permutations => {
      permutations.foreach { x =>
        val iter = new ByteDoubleIterator(x)
        assert(Main.readLine(iter) == x.head)
      }
    })
  }

  "finding the last line" should "succeed" in {
    Seq(shortPermutations, longPermutations).foreach(permutations => {
      permutations.foreach { x =>
        val iter = new ByteDoubleIterator(x)
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
      ) yield (xs, key)
      pairs.foreach { case (xs, key) =>
        val iter = new ByteDoubleIterator(xs)
        assert(Main.binarySearch(iter, key))
      }
    })
  }

  "search" should "fail on non-existing keys" in {
    Seq(shortPermutations, longPermutations).foreach(permutations => {
      val pairs = for (
        xs <- permutations;
        key <- xs
      ) yield (xs, key.map[Char, String](c => (c-1).toChar))
      pairs.foreach { case (xs, key) =>
        val iter = new ByteDoubleIterator(xs)
        assert(!Main.binarySearch(iter, key))
      }
    })
  }
}
