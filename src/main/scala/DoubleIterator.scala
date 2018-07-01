import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel

import scala.collection.mutable

abstract class DoubleIterator[T] extends Iterator[T] {

  class PrevIter(dblIter: DoubleIterator[T]) extends Iterator[T] {
    override def hasNext: Boolean = dblIter.hasPrev

    override def next(): T = dblIter.prev()
  }

  protected var ctr: Long = 0

  override def hasNext: Boolean = ctr < len

  def hasPrev: Boolean = ctr > 0

  def seek(position: Long): DoubleIterator[T] = {
    if (position > len || position < 0)
      throw new IndexOutOfBoundsException("you are seeing too far ahead")
    else
      ctr = position
    this
  }

  def toPrevIterator: Iterator[T] = new PrevIter(this)

  def position: Long = ctr

  def prev(): T

  def len: Long
}

class ByteDoubleIterator(s: Array[Byte]) extends DoubleIterator[Byte] {
  def this(str: String) = {
    this(str.getBytes)
  }

  override def prev(): Byte = {
    ctr -= 1
    s(ctr.toInt)
  }

  override def next(): Byte = {
    val b = s(ctr.toInt)
    ctr += 1
    b
  }

  override def len: Long = s.length

  override def toString(): String = new String(s)
}

class ChanDoubleIterator(chan: SeekableByteChannel) extends DoubleIterator[Byte] {

  private class WindowManager(chan: SeekableByteChannel) {
    private val windowSize = 512
    private val windows = mutable.Map[Long, Array[Byte]]()

    def read(pos: Long): Byte = {
      val k = posToWindow(pos)
      windows get k match {
        case Some(b) =>
          b((pos % windowSize.toLong).toInt)
        case None =>
          chan.position(k)
          val buf = ByteBuffer.allocate(windowSize)
          val n = chan.read(buf)
          val arr = buf.array().take(n)
          windows += (k -> arr)
          arr((pos % windowSize.toLong).toInt)
      }
    }

    private def posToWindow(pos: Long): Long =
      (pos / windowSize) * windowSize
  }

  private val wm = new WindowManager(chan)

  override def prev(): Byte = {
    ctr -= 1
    wm.read(ctr)
  }

  override def next(): Byte = {
    val b = wm.read(ctr)
    ctr += 1
    b
  }

  override def len: Long = chan.size
}
