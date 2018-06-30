import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.util

/**
  * SeekIterator adds the seek method to the standard iterator.
  * Unlike a normal iterator, we allow the possibility to seek to a
  * new location, backwards or forwards. However, the user is
  * responsible for seeking to valid position, otherwise the
  * behaviour is not defined.
  *
  * @tparam T an element in the iterator
  */
trait SeekIterator[T] extends Iterator[T] {
  def seek(position: Long): SeekIterator[T]

  def position: Long

  def len: Long
}

class ByteIterator(s: String) extends SeekIterator[Byte] {
  private var ctr: Long = 0

  override def hasNext: Boolean = ctr < len

  override def next(): Byte = {
    // might not be ok in general, but this class is for testing
    val b = s(ctr.toInt).toByte
    ctr += 1
    b
  }

  override def seek(position: Long): ByteIterator = {
    if (position >= len || position < 0)
      throw new IndexOutOfBoundsException("you are seeing too far ahead")
    else
      ctr = position
    this
  }

  def position: Long = ctr

  def len: Long = s.length
}

class ChanIterator(chan: SeekableByteChannel) extends SeekIterator[Byte] {
  private val buf = ByteBuffer.allocate(128)
  read()

  override def hasNext: Boolean = {
    if (buf.hasRemaining) true
    else {
      // this part is a bit strange as it changes the internal state
      // but I can't think of a better way to write hasNext
      if (read() > 0) true
      else false
    }
  }

  override def next(): Byte = {
    if (buf.hasRemaining) buf.get
    else {
      if (read() > 0) buf.get
      else throw new IndexOutOfBoundsException
    }
  }

  override def seek(position: Long): ChanIterator = {
    chan.position(position)
    read()
    this
  }

  override def position: Long = {
    chan.position - buf.remaining
  }

  override def len: Long = chan.size

  private def read(): Long = {
    buf.clear
    val n = chan.read(buf)
    buf.position(0)
    buf.limit(n max 0)
    n
  }
}

object Main {
  def lineSearcher(f: Path, key: String): Boolean = {
    val chan = Files.newByteChannel(f, util.EnumSet.of(StandardOpenOption.READ))
    val iter = new ChanIterator(chan)
    binarySearch(iter, key)
  }

  def binarySearch(iter: SeekIterator[Byte], key: String): Boolean = {
    val high = findLastLine(iter)
    binarySearchRec(iter, 0, high, key)
  }

  def findLastLine(iter: SeekIterator[Byte]): Long = {
    // FIXME: might not find the right line if n is too low!
    val tot = iter.len
    val n = 1028L min tot
    iter.seek(tot - n)
    val loc = iter.toList.reverse.takeWhile(_ != '\n').size
    tot - loc
  }

  def findCurrLine(iter: SeekIterator[Byte]): Long = {
    // FIXME: might not find the right line if n is too low!
    val blockSize = 1028
    val start = iter.position - blockSize // might be negative
    iter.seek(start max 0)
    val xs = iter.take({
      if (start > 0) blockSize else blockSize + start
    }.toInt).toList
    val end = (start max 0) - xs.size
    end - xs.reverse.takeWhile(_ != '\n').size
  }

  def findNextLine(iter: SeekIterator[Byte]): Long = {
    val currPos = iter.position
    val cnt = iter.takeWhile(_ != '\n').size
    currPos + cnt + 1 // doesn't work for windows
  }

  def readLine(chan: SeekIterator[Byte]): String = {
    new String(chan.takeWhile(_ != '\n').toArray) // doesn't work for windows
  }

  /**
    * Invariant that we maintain is that low, high and mid are always at the start of a line.
    */
  def binarySearchRec(chan: SeekIterator[Byte], low: Long, high: Long, key: String): Boolean = {
    val nextLow = findNextLine(chan.seek(low))
    if (nextLow > high) throw new RuntimeException("impossible state " + (low, nextLow, high))
    // base case
    if (nextLow == high) {
      if (readLine(chan.seek(low)) != key && readLine(chan.seek(nextLow)) != key) false
      else true
    } else {
      // position to tentative middle point
      chan.seek((low + high) / 2)
      val mid = findNextLine(chan)
      chan.seek(mid)
      val line = readLine(chan)
      key.compareTo(line) match {
        case x if x > 0 => binarySearchRec(chan, mid, high, key)
        case x if x < 0 => binarySearchRec(chan, low, mid, key)
        case _ => true
      }
    }
  }

  def main(args: Array[String]): Unit = {
    println("bonjour le monde")
  }
}
