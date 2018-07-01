import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.util

object Main {
  def findLastLine(iter: DoubleIterator[Byte]): Long = {
    iter.seek(iter.size)
    findCurrLine(iter)
  }

  def findCurrLine(iter: DoubleIterator[Byte]): Long = {
    val start = iter.position
    val cnt = iter.toPrevIterator.takeWhile(_ != '\n').size
    start - cnt
  }

  def findNextLine(iter: DoubleIterator[Byte]): Long = {
    val start = iter.position
    val cnt = iter.takeWhile(_ != '\n').size
    start + cnt + 1
  }

  def readLine(chan: DoubleIterator[Byte]): String = {
    new String(chan.takeWhile(_ != '\n').toArray)
  }

  def binarySearch(iter: DoubleIterator[Byte], key: String): Boolean = {
    val high = iter.len / (key.length + 1)
    binarySearchRec(iter, 0, high, key)
  }

  def binarySearchRec(chan: DoubleIterator[Byte], low: Long, high: Long, key: String): Boolean = {
    // base case
    if (low > high) return false
    // recursive case
    val mid = (low + high) / 2
    chan.seek(mid * (key.length + 1)) // convert mid to actual position in channel
    val candidate = readLine(chan)
    if (candidate.length != key.length)
      throw new RuntimeException("key length is not equal to line length")
    key.compareTo(candidate) match {
      case x if x > 0 => binarySearchRec(chan, mid + 1, high, key)
      case x if x < 0 => binarySearchRec(chan, low, mid - 1, key)
      case _ => true
    }
  }

  def searchLine(f: Path, key: String): Boolean = {
    val chan = Files.newByteChannel(f, util.EnumSet.of(StandardOpenOption.READ))
    val iter = new ChanDoubleIterator(chan)
    binarySearch(iter, key)
  }

  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      println("invalid arguments")
      println("usage: searchline <file> <key>")
    } else {
      if (searchLine(Paths.get(args(0)), args(1))) println("ok")
      else println("not found")
    }
  }
}
