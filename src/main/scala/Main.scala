import java.nio.file.{Files, Path, StandardOpenOption}
import java.util

object Main {
  def lineSearcher(f: Path, key: String): Boolean = {
    val chan = Files.newByteChannel(f, util.EnumSet.of(StandardOpenOption.READ))
    val iter = new ChanDoubleIterator(chan)
    binarySearch(iter, key)
  }

  def binarySearch(iter: DoubleIterator[Byte], key: String): Boolean = {
    binarySearchRec(iter, 0, iter.len, key)
  }

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

  def binarySearchRec(chan: DoubleIterator[Byte], low: Long, high: Long, key: String): Boolean = {
    if (low > high) throw new RuntimeException("impossible state " + (low, high))

    // base condition
    chan.seek(findCurrLine(chan.seek(low)))
    val lowLine = readLine(chan)
    if (lowLine == key) return true

    chan.seek(findCurrLine(chan.seek(high)))
    val highLine = readLine(chan)
    if (highLine == key) return true

    // if we are the same line, then the key does not exist
    if (lowLine == highLine) return false

    // if there are no lines in between us, then the key does not exist
    val nextLowLine = {
      chan.seek(low)
      chan.seek(findNextLine(chan))
      readLine(chan)
    }
    if (nextLowLine == highLine) return false

    // recursive case
    val mid = (low + high) / 2
    chan.seek(mid)
    chan.seek(findCurrLine(chan))
    val candidate = readLine(chan)
    if (candidate.length != key.length) throw new RuntimeException("key length is not equal to line length")

    key.compareTo(candidate) match {
      case x if x > 0 => binarySearchRec(chan, mid, high, key)
      case x if x < 0 => binarySearchRec(chan, low, mid, key)
      case _ => true
    }
  }

  def main(args: Array[String]): Unit = {
    println("bonjour le monde")
  }
}
